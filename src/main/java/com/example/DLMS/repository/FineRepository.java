package com.example.DLMS.repository;

import com.example.DLMS.entity.Fine;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserOrderByStatusAsc(User user);
    List<Fine> findByStatus(FineStatus status);
    List<Fine> findAllByOrderByStatusAsc();
    boolean existsByUserAndStatus(User user, FineStatus status);

    @Query("SELECT COALESCE(SUM(f.fineAmount), 0) FROM Fine f WHERE f.status = 'PAID'")
    BigDecimal sumTotalPaidFines();

    @Query("SELECT COALESCE(SUM(f.fineAmount), 0) FROM Fine f WHERE f.status = 'UNPAID'")
    BigDecimal sumTotalUnpaidFines();

    @Query("SELECT COALESCE(SUM(f.fineAmount), 0) FROM Fine f WHERE f.user = :user AND f.status = 'UNPAID'")
    BigDecimal sumUnpaidFinesByUser(@Param("user") User user);
}

