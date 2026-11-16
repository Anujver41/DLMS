package com.example.DLMS.service;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.Reservation;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.IssueStatus;
import com.example.DLMS.enums.ReservationStatus;
import com.example.DLMS.repository.BookIssueRepository;
import com.example.DLMS.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookIssueRepository bookIssueRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional
    public Reservation reserveBook(Long userId, Long bookId) {
        User user = userService.getById(userId);
        Book book = bookService.getById(bookId);

        if (book.getAvailableQuantity() > 0) {
            throw new IllegalStateException("Copies of this book are currently available in the library. You can borrow it directly without reservation.");
        }

        if (bookIssueRepository.existsByBookAndUserAndStatus(book, user, IssueStatus.ISSUED) ||
            bookIssueRepository.existsByBookAndUserAndStatus(book, user, IssueStatus.OVERDUE)) {
            throw new IllegalStateException("You already have an active loan for this book.");
        }

        if (reservationRepository.existsByBookAndUserAndStatus(book, user, ReservationStatus.PENDING)) {
            throw new IllegalStateException("You already have an active reservation for this book.");
        }

        Reservation reservation = Reservation.builder()
                .book(book)
                .user(user)
                .status(ReservationStatus.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        if (!reservation.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new IllegalStateException("You are not authorized to cancel this reservation.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUserOrderByReservationDateDesc(user);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public long countPendingReservations() {
        return reservationRepository.countByStatus(ReservationStatus.PENDING);
    }
}

