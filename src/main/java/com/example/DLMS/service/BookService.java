package com.example.DLMS.service;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.Category;
import com.example.DLMS.repository.BookIssueRepository;
import com.example.DLMS.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookIssueRepository bookIssueRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
    }

    public List<Book> searchAndFilter(String keyword, Long categoryId) {
        return bookRepository.searchAndFilter(keyword, categoryId);
    }

    @Transactional
    public Book addBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn().trim())) {
            throw new IllegalArgumentException("A book with ISBN '" + book.getIsbn() + "' already exists.");
        }
        book.setIsbn(book.getIsbn().trim());
        book.setAvailableQuantity(book.getTotalQuantity());
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book updatedData, Long categoryId) {
        Book existing = getById(id);

        if (!existing.getIsbn().equalsIgnoreCase(updatedData.getIsbn().trim()) &&
                bookRepository.existsByIsbn(updatedData.getIsbn().trim())) {
            throw new IllegalArgumentException("A book with ISBN '" + updatedData.getIsbn() + "' already exists.");
        }

        int quantityDiff = updatedData.getTotalQuantity() - existing.getTotalQuantity();
        int newAvailable = existing.getAvailableQuantity() + quantityDiff;
        if (newAvailable < 0) {
            throw new IllegalArgumentException("Cannot reduce total quantity below the number of currently issued copies.");
        }

        existing.setTitle(updatedData.getTitle());
        existing.setAuthor(updatedData.getAuthor());
        existing.setIsbn(updatedData.getIsbn().trim());
        existing.setTotalQuantity(updatedData.getTotalQuantity());
        existing.setAvailableQuantity(newAvailable);
        existing.setRackNumber(updatedData.getRackNumber());
        existing.setDescription(updatedData.getDescription());

        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getById(id);
        if (book.getAvailableQuantity() < book.getTotalQuantity()) {
            throw new IllegalStateException("Cannot delete book '" + book.getTitle() + "' because copies are currently issued to members.");
        }
        bookRepository.delete(book);
    }

    public long countTotalBooks() {
        return bookRepository.count();
    }

    public long countAvailableBooks() {
        return bookRepository.countByAvailableQuantityGreaterThan(0);
    }
}

