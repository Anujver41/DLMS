package com.example.DLMS.service;

import com.example.DLMS.entity.Fine;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.FineStatus;
import com.example.DLMS.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;

    public List<Fine> getAllFines() {
        return fineRepository.findAllByOrderByStatusAsc();
    }

    public List<Fine> getFinesByUser(User user) {
        return fineRepository.findByUserOrderByStatusAsc(user);
    }

    public List<Fine> getUnpaidFines() {
        return fineRepository.findByStatus(FineStatus.UNPAID);
    }

    @Transactional
    public Fine markFineAsPaid(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine record not found with id: " + fineId));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new IllegalStateException("This fine is already marked as paid.");
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidDate(LocalDateTime.now());
        return fineRepository.save(fine);
    }

    public BigDecimal getTotalPaidAmount() {
        return fineRepository.sumTotalPaidFines();
    }

    public BigDecimal getTotalUnpaidAmount() {
        return fineRepository.sumTotalUnpaidFines();
    }

    public BigDecimal getUnpaidAmountForUser(User user) {
        return fineRepository.sumUnpaidFinesByUser(user);
    }
}

