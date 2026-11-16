package com.example.DLMS.repository;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.BookIssue;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookIssueRepository extends JpaRepository<BookIssue, Long> {
    List<BookIssue> findByUserOrderByIssueDateDesc(User user);
    List<BookIssue> findByUserAndStatus(User user, IssueStatus status);
    long countByUserAndStatus(User user, IssueStatus status);
    List<BookIssue> findByStatus(IssueStatus status);
    List<BookIssue> findAllByOrderByIssueDateDesc();
    List<BookIssue> findByDueDateBeforeAndStatus(LocalDate date, IssueStatus status);
    boolean existsByBookAndUserAndStatus(Book book, User user, IssueStatus status);

    @Query("SELECT COUNT(bi) FROM BookIssue bi WHERE bi.status = 'ISSUED' OR bi.status = 'OVERDUE'")
    long countActiveIssues();
}

