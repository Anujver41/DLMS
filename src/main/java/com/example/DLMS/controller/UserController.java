package com.example.DLMS.controller;

import com.example.DLMS.entity.BookIssue;
import com.example.DLMS.entity.Fine;
import com.example.DLMS.entity.Reservation;
import com.example.DLMS.entity.User;
import com.example.DLMS.security.CustomUserDetails;
import com.example.DLMS.service.CirculationService;
import com.example.DLMS.service.FineService;
import com.example.DLMS.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final CirculationService circulationService;
    private final FineService fineService;
    private final ReservationService reservationService;

    @GetMapping("/user/dashboard")
    public String userDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        // Refresh overdue statuses
        circulationService.refreshOverdueStatus();

        List<BookIssue> activeLoans = circulationService.getActiveIssuesByUser(user);
        List<BookIssue> loanHistory = circulationService.getIssuesByUser(user);
        List<Fine> fines = fineService.getFinesByUser(user);
        List<Reservation> reservations = reservationService.getReservationsByUser(user);
        BigDecimal unpaidFineTotal = fineService.getUnpaidAmountForUser(user);

        model.addAttribute("user", user);
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("loanHistory", loanHistory);
        model.addAttribute("fines", fines);
        model.addAttribute("reservations", reservations);
        model.addAttribute("unpaidFineTotal", unpaidFineTotal);

        return "user/dashboard";
    }

    @PostMapping("/books/issue/{bookId}")
    public String issueBook(@PathVariable("bookId") Long bookId,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            BookIssue issue = circulationService.issueBook(userDetails.getId(), bookId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Success! '" + issue.getBook().getTitle() + "' has been issued to your account. Due date is " + issue.getDueDate() + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/catalog";
    }

    @PostMapping("/books/return/{issueId}")
    public String returnBook(@PathVariable("issueId") Long issueId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            BookIssue issue = circulationService.returnBook(issueId);
            if (issue.getFine() != null && issue.getFine().getFineAmount().compareTo(BigDecimal.ZERO) > 0) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Book returned! Note: It was overdue by " + issue.getFine().getOverdueDays() +
                        " days. A fine of ₹" + issue.getFine().getFineAmount() + " has been added to your account.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thank you! '" + issue.getBook().getTitle() + "' was successfully returned on time.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/user/dashboard";
    }

    @PostMapping("/reserve/{bookId}")
    public String reserveBook(@PathVariable("bookId") Long bookId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.reserveBook(userDetails.getId(), bookId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Advance booking confirmed for '" + reservation.getBook().getTitle() + "'. You will be notified once a copy is returned.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/catalog";
    }

    @PostMapping("/reserve/cancel/{reservationId}")
    public String cancelReservation(@PathVariable("reservationId") Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(reservationId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/user/dashboard";
    }
}

