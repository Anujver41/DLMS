package com.example.DLMS.controller;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.Category;
import com.example.DLMS.entity.ContactQuery;
import com.example.DLMS.entity.User;
import com.example.DLMS.security.CustomUserDetails;
import com.example.DLMS.service.BookService;
import com.example.DLMS.service.CategoryService;
import com.example.DLMS.service.ContactQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final ContactQueryService contactQueryService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalBooks", bookService.countTotalBooks());
        model.addAttribute("availableBooks", bookService.countAvailableBooks());
        model.addAttribute("featuredBooks", bookService.getAllBooks().stream().limit(6).toList());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "categoryId", required = false) Long categoryId,
                          Model model) {
        List<Book> books = bookService.searchAndFilter(keyword, categoryId);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("keyword", keyword);
        return "catalog";
    }

    @GetMapping("/contact")
    public String contactPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (!model.containsAttribute("contactQuery")) {
            ContactQuery query = new ContactQuery();
            if (userDetails != null) {
                query.setName(userDetails.getFullName());
                query.setEmail(userDetails.getUsername());
            }
            model.addAttribute("contactQuery", query);
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("contactQuery") ContactQuery contactQuery,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "contact";
        }

        User user = (userDetails != null) ? userDetails.getUser() : null;
        contactQueryService.submitQuery(contactQuery, user);
        redirectAttributes.addFlashAttribute("successMessage", "Your query has been submitted successfully! Our librarian team will review it shortly.");
        return "redirect:/contact";
    }
}

