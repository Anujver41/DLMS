package com.example.DLMS.config;

import com.example.DLMS.entity.Book;
import com.example.DLMS.entity.Category;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.Role;
import com.example.DLMS.repository.BookRepository;
import com.example.DLMS.repository.CategoryRepository;
import com.example.DLMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCategoriesAndBooks();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@dlms.com")) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@dlms.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("+91-9876543210")
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("Default Admin created: admin@dlms.com / admin123");
        }

        if (!userRepository.existsByEmail("user@dlms.com")) {
            User demoUser = User.builder()
                    .fullName("Rahul Sharma")
                    .email("user@dlms.com")
                    .password(passwordEncoder.encode("user123"))
                    .phone("+91-9123456780")
                    .role(Role.ROLE_USER)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(demoUser);
            log.info("Default Demo User created: user@dlms.com / user123");
        }
    }
    
    private void seedCategoriesAndBooks() {
        if (categoryRepository.count() == 0) {
            Category cs = categoryRepository.save(Category.builder()
                    .name("Computer Science & Programming")
                    .description("Core programming languages, algorithms, data structures, and computer theory")
                    .build());

            Category se = categoryRepository.save(Category.builder()
                    .name("Software Architecture & Engineering")
                    .description("Design patterns, clean code, microservices, and system architecture")
                    .build());

            Category ai = categoryRepository.save(Category.builder()
                    .name("Artificial Intelligence & ML")
                    .description("Machine learning, deep learning, NLP, and intelligent agents")
                    .build());

            Category fiction = categoryRepository.save(Category.builder()
                    .name("Fiction & Novels")
                    .description("Classic and contemporary literary fiction, sci-fi, and thrillers")
                    .build());

            if (bookRepository.count() == 0) {
                bookRepository.saveAll(List.of(
                    Book.builder()
                        .title("Clean Code: A Handbook of Agile Software Craftsmanship")
                        .author("Robert C. Martin")
                        .isbn("978-0132350884")
                        .category(se)
                        .totalQuantity(5)
                        .availableQuantity(5)
                        .rackNumber("RACK-SE-01")
                        .description("Even bad code can function. But if code isn't clean, it can bring a development organization to its knees.")
                        .createdAt(LocalDateTime.now())
                        .build(),

                    Book.builder()
                        .title("Effective Java (3rd Edition)")
                        .author("Joshua Bloch")
                        .isbn("978-0134685991")
                        .category(cs)
                        .totalQuantity(4)
                        .availableQuantity(4)
                        .rackNumber("RACK-CS-02")
                        .description("The definitive guide to best practices in the Java programming language.")
                        .createdAt(LocalDateTime.now())
                        .build(),

                    Book.builder()
                        .title("Spring Boot in Action")
                        .author("Craig Walls")
                        .isbn("978-1617292545")
                        .category(se)
                        .totalQuantity(3)
                        .availableQuantity(3)
                        .rackNumber("RACK-SE-03")
                        .description("A hands-on guide to building enterprise microservices and web applications with Spring Boot.")
                        .createdAt(LocalDateTime.now())
                        .build(),

                    Book.builder()
                        .title("Designing Data-Intensive Applications")
                        .author("Martin Kleppmann")
                        .isbn("978-1449373320")
                        .category(se)
                        .totalQuantity(2)
                        .availableQuantity(2)
                        .rackNumber("RACK-SE-04")
                        .description("The big ideas behind reliable, scalable, and maintainable systems.")
                        .createdAt(LocalDateTime.now())
                        .build(),

                    Book.builder()
                        .title("Artificial Intelligence: A Modern Approach")
                        .author("Stuart Russell, Peter Norvig")
                        .isbn("978-0136042594")
                        .category(ai)
                        .totalQuantity(3)
                        .availableQuantity(3)
                        .rackNumber("RACK-AI-01")
                        .description("The leading comprehensive introduction to the theory and practice of artificial intelligence.")
                        .createdAt(LocalDateTime.now())
                        .build(),

                    Book.builder()
                        .title("The Pragmatic Programmer")
                        .author("Andrew Hunt, David Thomas")
                        .isbn("978-0201616224")
                        .category(se)
                        .totalQuantity(1)
                        .availableQuantity(1)
                        .rackNumber("RACK-SE-05")
                        .description("One of the most significant books on software craftsmanship and professional software development.")
                        .createdAt(LocalDateTime.now())
                        .build(),

                    Book.builder()
                        .title("1984")
                        .author("George Orwell")
                        .isbn("978-0451524935")
                        .category(fiction)
                        .totalQuantity(4)
                        .availableQuantity(4)
                        .rackNumber("RACK-FIC-01")
                        .description("A dystopian social science fiction novel and cautionary tale about totalitarianism.")
                        .createdAt(LocalDateTime.now())
                        .build()
                ));
                log.info("Default library catalog populated with 7 essential books across 4 categories.");
            }
        }
    }
}

