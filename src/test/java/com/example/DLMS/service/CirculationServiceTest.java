package com.example.DLMS.service;

import com.example.DLMS.entity.*;
import com.example.DLMS.enums.FineStatus;
import com.example.DLMS.enums.IssueStatus;
import com.example.DLMS.enums.Role;
import com.example.DLMS.repository.BookIssueRepository;
import com.example.DLMS.repository.BookRepository;
import com.example.DLMS.repository.FineRepository;
import com.example.DLMS.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CirculationServiceTest {

    @Mock
    private BookIssueRepository bookIssueRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private CirculationService circulationService;

    private User sampleUser;
    private Book sampleBook;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(circulationService, "loanDurationDays", 14);
        ReflectionTestUtils.setField(circulationService, "maxBooksPerUser", 3);
        ReflectionTestUtils.setField(circulationService, "dailyFineRate", BigDecimal.valueOf(5.00));

        sampleUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        sampleCategory = Category.builder().id(1L).name("Computer Science").build();

        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Architecture")
                .author("Robert C. Martin")
                .isbn("978-0134494166")
                .category(sampleCategory)
                .totalQuantity(3)
                .availableQuantity(3)
                .build();
    }

    @Test
    @DisplayName("Should successfully issue book when stock is available and member is eligible")
    void issueBook_Success() {
        when(userService.getById(1L)).thenReturn(sampleUser);
        when(fineRepository.existsByUserAndStatus(sampleUser, FineStatus.UNPAID)).thenReturn(false);
        when(bookIssueRepository.countByUserAndStatus(sampleUser, IssueStatus.ISSUED)).thenReturn(0L);
        when(bookIssueRepository.countByUserAndStatus(sampleUser, IssueStatus.OVERDUE)).thenReturn(0L);
        when(bookService.getById(1L)).thenReturn(sampleBook);
        when(bookIssueRepository.existsByBookAndUserAndStatus(sampleBook, sampleUser, IssueStatus.ISSUED)).thenReturn(false);
        when(bookIssueRepository.existsByBookAndUserAndStatus(sampleBook, sampleUser, IssueStatus.OVERDUE)).thenReturn(false);
        when(bookIssueRepository.save(any(BookIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookIssue issue = circulationService.issueBook(1L, 1L);

        assertNotNull(issue);
        assertEquals(IssueStatus.ISSUED, issue.getStatus());
        assertEquals(2, sampleBook.getAvailableQuantity(), "Available copies should decrement by 1");
        assertEquals(LocalDate.now().plusDays(14), issue.getDueDate());
        verify(bookRepository, times(1)).save(sampleBook);
        verify(bookIssueRepository, times(1)).save(any(BookIssue.class));
    }

    @Test
    @DisplayName("Should throw exception when attempting to issue out-of-stock book")
    void issueBook_OutOfStock_ThrowsException() {
        sampleBook.setAvailableQuantity(0);

        when(userService.getById(1L)).thenReturn(sampleUser);
        when(fineRepository.existsByUserAndStatus(sampleUser, FineStatus.UNPAID)).thenReturn(false);
        when(bookIssueRepository.countByUserAndStatus(sampleUser, IssueStatus.ISSUED)).thenReturn(0L);
        when(bookIssueRepository.countByUserAndStatus(sampleUser, IssueStatus.OVERDUE)).thenReturn(0L);
        when(bookService.getById(1L)).thenReturn(sampleBook);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                circulationService.issueBook(1L, 1L));

        assertTrue(ex.getMessage().contains("out of stock"));
    }

    @Test
    @DisplayName("Should calculate fine of ₹5/day when returning an overdue book")
    void returnBook_Overdue_CalculatesFine() {
        LocalDate issuedDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = issuedDate.plusDays(14); // 6 days ago

        BookIssue activeIssue = BookIssue.builder()
                .id(10L)
                .book(sampleBook)
                .user(sampleUser)
                .issueDate(issuedDate)
                .dueDate(dueDate)
                .status(IssueStatus.ISSUED)
                .build();

        sampleBook.setAvailableQuantity(2);

        when(bookIssueRepository.findById(10L)).thenReturn(Optional.of(activeIssue));
        when(bookIssueRepository.save(any(BookIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.findFirstByBookAndStatusOrderByReservationDateAsc(sampleBook, com.example.DLMS.enums.ReservationStatus.PENDING))
                .thenReturn(Optional.empty());

        BookIssue returnedIssue = circulationService.returnBook(10L);

        assertEquals(IssueStatus.RETURNED, returnedIssue.getStatus());
        assertEquals(3, sampleBook.getAvailableQuantity(), "Available copies should increment back");
        assertNotNull(returnedIssue.getFine(), "Fine should be generated for overdue return");
        assertEquals(6, returnedIssue.getFine().getOverdueDays());
        assertEquals(new BigDecimal("30.00"), returnedIssue.getFine().getFineAmount(), "Fine should be 6 * 5 = ₹30");
        verify(fineRepository, times(1)).save(any(Fine.class));
    }
}

