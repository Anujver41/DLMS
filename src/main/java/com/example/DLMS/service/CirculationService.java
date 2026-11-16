package com.example.DLMS.service;

import com.example.DLMS.entity.*;
import com.example.DLMS.enums.FineStatus;
import com.example.DLMS.enums.IssueStatus;
import com.example.DLMS.enums.ReservationStatus;
import com.example.DLMS.repository.BookIssueRepository;
import com.example.DLMS.repository.BookRepository;
import com.example.DLMS.repository.FineRepository;
import com.example.DLMS.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CirculationService {

    private final BookIssueRepository bookIssueRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final BookService bookService;

    @Value("${library.loan.duration-days:14}")
    private int loanDurationDays;

    @Value("${library.loan.max-books-per-user:3}")
    private int maxBooksPerUser;

    @Value("${library.fine.daily-rate:5.00}")
    private BigDecimal dailyFineRate;

    @Transactional
    public BookIssue issueBook(Long userId, Long bookId) {
        User user = userService.getById(userId);
        if (!user.isActive()) {
            throw new IllegalStateException("Your account is deactivated. Please contact the administrator.");
        }

        // Check if member has unpaid overdue fines
        if (fineRepository.existsByUserAndStatus(user, FineStatus.UNPAID)) {
            throw new IllegalStateException("You have unpaid overdue fines. Please clear outstanding fines before borrowing new books.");
        }

        // Check active loan limit (max 3)
        long activeCount = bookIssueRepository.countByUserAndStatus(user, IssueStatus.ISSUED)
                + bookIssueRepository.countByUserAndStatus(user, IssueStatus.OVERDUE);
        if (activeCount >= maxBooksPerUser) {
            throw new IllegalStateException("You have reached the maximum borrow limit (" + maxBooksPerUser + " books).");
        }

        Book book = bookService.getById(bookId);

        // Check if user already has an active issue of this book
        if (bookIssueRepository.existsByBookAndUserAndStatus(book, user, IssueStatus.ISSUED) ||
            bookIssueRepository.existsByBookAndUserAndStatus(book, user, IssueStatus.OVERDUE)) {
            throw new IllegalStateException("You already have an active loan for this book.");
        }

        // Check availability
        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("This book is currently out of stock. You can place an advance booking instead.");
        }

        // Decrement available copies
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        LocalDate now = LocalDate.now();
        BookIssue issue = BookIssue.builder()
                .book(book)
                .user(user)
                .issueDate(now)
                .dueDate(now.plusDays(loanDurationDays))
                .status(IssueStatus.ISSUED)
                .build();

        return bookIssueRepository.save(issue);
    }

    @Transactional
    public BookIssue returnBook(Long issueId) {
        BookIssue issue = bookIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Loan record not found with id: " + issueId));

        if (issue.getStatus() == IssueStatus.RETURNED) {
            throw new IllegalStateException("This book has already been marked as returned.");
        }

        LocalDate returnDate = LocalDate.now();
        issue.setReturnDate(returnDate);
        issue.setStatus(IssueStatus.RETURNED);

        Book book = issue.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        // Calculate fine if overdue (₹5 per day overdue)
        if (returnDate.isAfter(issue.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(issue.getDueDate(), returnDate);
            if (overdueDays > 0) {
                BigDecimal fineAmount = dailyFineRate.multiply(BigDecimal.valueOf(overdueDays));
                Fine fine = Fine.builder()
                        .bookIssue(issue)
                        .user(issue.getUser())
                        .fineAmount(fineAmount)
                        .overdueDays((int) overdueDays)
                        .status(FineStatus.UNPAID)
                        .build();
                fineRepository.save(fine);
                issue.setFine(fine);
            }
        }

        // Fulfill next pending reservation if present
        Optional<Reservation> nextReservation = reservationRepository.findFirstByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.PENDING);
        if (nextReservation.isPresent()) {
            Reservation res = nextReservation.get();
            res.setStatus(ReservationStatus.FULFILLED);
            reservationRepository.save(res);
        }

        return bookIssueRepository.save(issue);
    }

    public List<BookIssue> getAllIssues() {
        return bookIssueRepository.findAllByOrderByIssueDateDesc();
    }

    public List<BookIssue> getIssuesByUser(User user) {
        return bookIssueRepository.findByUserOrderByIssueDateDesc(user);
    }

    public List<BookIssue> getActiveIssuesByUser(User user) {
        List<BookIssue> issued = bookIssueRepository.findByUserAndStatus(user, IssueStatus.ISSUED);
        issued.addAll(bookIssueRepository.findByUserAndStatus(user, IssueStatus.OVERDUE));
        return issued;
    }

    public long countActiveIssues() {
        return bookIssueRepository.countActiveIssues();
    }

    @Transactional
    public void refreshOverdueStatus() {
        LocalDate today = LocalDate.now();
        List<BookIssue> overdueCandidates = bookIssueRepository.findByDueDateBeforeAndStatus(today, IssueStatus.ISSUED);
        for (BookIssue issue : overdueCandidates) {
            issue.setStatus(IssueStatus.OVERDUE);
            bookIssueRepository.save(issue);
        }
    }
}

