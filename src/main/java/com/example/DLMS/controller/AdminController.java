package com.example.DLMS.controller;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.Category;
import com.example.DLMS.entity.Fine;
import com.example.DLMS.entity.User;
import com.example.DLMS.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final CirculationService circulationService;
    private final FineService fineService;
    private final UserService userService;
    private final ContactQueryService contactQueryService;
    private final ReservationService reservationService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Refresh any overdue status
        circulationService.refreshOverdueStatus();

        model.addAttribute("totalBooks", bookService.countTotalBooks());
        model.addAttribute("availableBooks", bookService.countAvailableBooks());
        model.addAttribute("totalMembers", userService.countMembers());
        model.addAttribute("activeLoans", circulationService.countActiveIssues());
        model.addAttribute("totalPaidFines", fineService.getTotalPaidAmount());
        model.addAttribute("totalUnpaidFines", fineService.getTotalUnpaidAmount());
        model.addAttribute("pendingQueries", contactQueryService.countPendingQueries());
        model.addAttribute("pendingReservations", reservationService.countPendingReservations());
        model.addAttribute("recentLoans", circulationService.getAllIssues().stream().limit(5).toList());

        return "admin/dashboard";
    }

    // ------------------- BOOK MANAGEMENT -------------------

    @GetMapping("/books")
    public String manageBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", new Book());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/book-form";
    }

    @PostMapping("/books/new")
    public String saveBook(@Valid @ModelAttribute("book") Book book,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/book-form";
        }

        try {
            bookService.addBook(book);
            redirectAttributes.addFlashAttribute("successMessage", "Book '" + book.getTitle() + "' added successfully!");
            return "redirect:/admin/books";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("isbn", "error.book", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/book-form";
        }
    }

    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getById(id);
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/book-form";
    }

    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/book-form";
        }

        try {
            bookService.updateBook(id, book, book.getCategory().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Book '" + book.getTitle() + "' updated successfully!");
            return "redirect:/admin/books";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/book-form";
        }
    }

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/books";
    }

    // ------------------- ISSUED BOOKS & LOANS -------------------

    @GetMapping("/issued-books")
    public String viewIssuedBooks(Model model) {
        circulationService.refreshOverdueStatus();
        model.addAttribute("issues", circulationService.getAllIssues());
        return "admin/issued-books";
    }

    // ------------------- MEMBER MANAGEMENT -------------------

    @GetMapping("/members")
    public String viewMembers(Model model) {
        model.addAttribute("members", userService.getAllMembers());
        return "admin/members";
    }

    @PostMapping("/members/toggle/{id}")
    public String toggleMemberStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserActive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Member status updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/members";
    }

    // ------------------- FINE MANAGEMENT -------------------

    @GetMapping("/fines")
    public String viewFines(Model model) {
        model.addAttribute("fines", fineService.getAllFines());
        model.addAttribute("totalPaid", fineService.getTotalPaidAmount());
        model.addAttribute("totalUnpaid", fineService.getTotalUnpaidAmount());
        return "admin/fines";
    }

    @PostMapping("/fines/pay/{id}")
    public String markFineAsPaid(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Fine fine = fineService.markFineAsPaid(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Fine of ₹" + fine.getFineAmount() + " for member '" + fine.getUser().getFullName() + "' marked as PAID.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/fines";
    }

    // ------------------- CONTACT / QUERIES -------------------

    @GetMapping("/queries")
    public String viewQueries(Model model) {
        model.addAttribute("queries", contactQueryService.getAllQueries());
        return "admin/queries";
    }

    @PostMapping("/queries/resolve/{id}")
    public String resolveQuery(@PathVariable("id") Long id,
                               @RequestParam(value = "adminReply", required = false) String adminReply,
                               RedirectAttributes redirectAttributes) {
        try {
            contactQueryService.resolveQuery(id, adminReply);
            redirectAttributes.addFlashAttribute("successMessage", "Query marked as RESOLVED.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/queries";
    }
}

