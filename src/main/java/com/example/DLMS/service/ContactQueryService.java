package com.example.DLMS.service;

import com.example.DLMS.entity.ContactQuery;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.QueryStatus;
import com.example.DLMS.repository.ContactQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactQueryService {

    private final ContactQueryRepository contactQueryRepository;

    @Transactional
    public ContactQuery submitQuery(ContactQuery query, User loggedInUser) {
        if (loggedInUser != null) {
            query.setUser(loggedInUser);
            if (query.getName() == null || query.getName().isBlank()) {
                query.setName(loggedInUser.getFullName());
            }
            if (query.getEmail() == null || query.getEmail().isBlank()) {
                query.setEmail(loggedInUser.getEmail());
            }
        }
        query.setStatus(QueryStatus.PENDING);
        return contactQueryRepository.save(query);
    }

    public List<ContactQuery> getAllQueries() {
        return contactQueryRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ContactQuery> getPendingQueries() {
        return contactQueryRepository.findByStatusOrderByCreatedAtDesc(QueryStatus.PENDING);
    }

    @Transactional
    public ContactQuery resolveQuery(Long id, String adminReply) {
        ContactQuery query = contactQueryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Query not found with id: " + id));

        query.setStatus(QueryStatus.RESOLVED);
        query.setAdminReply(adminReply != null ? adminReply.trim() : "Resolved by Administrator");
        return contactQueryRepository.save(query);
    }

    public long countPendingQueries() {
        return contactQueryRepository.countByStatus(QueryStatus.PENDING);
    }
}

