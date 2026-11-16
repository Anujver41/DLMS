package com.example.DLMS.repository;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.Reservation;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus status);
    List<Reservation> findByUserOrderByReservationDateDesc(User user);
    boolean existsByBookAndUserAndStatus(Book book, User user, ReservationStatus status);
    long countByStatus(ReservationStatus status);
    Optional<Reservation> findFirstByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus status);
}

