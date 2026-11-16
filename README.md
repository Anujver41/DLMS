# Digital Library Management System (DLMS)

A full-stack, enterprise-grade **Digital Library Management System** developed with **Java 21**, **Spring Boot 4 / 3**, **Spring Data JPA**, **Spring Security**, **MySQL**, and **HTML5 / CSS3 / Thymeleaf / Bootstrap 5**.

---

## 🚀 Key Features

### 👑 Admin Module
- **Role-Based Authentication**: Secure admin access (`ROLE_ADMIN`) with dedicated administrative dashboards.
- **Book Inventory Management**: Add new books, modify existing details, update rack/shelf locations, track real-time stock, and safely delete books (with integrity checks against active loans).
- **Circulation & Loan Monitoring**: View all issued books across members, issue dates, due dates, and real-time overdue statuses.
- **Member Management**: View all registered readers and toggle account active/suspended statuses.
- **Automated Fine Settlement**: View outstanding overdue penalties and record payments to mark fines as `PAID`.
- **Query / Support Desk**: Review inquiries and messages submitted by readers with resolution notes.

### 👤 Member / User Module
- **Registration & Authentication**: User sign-up with BCrypt password encryption and instant login (`ROLE_USER`).
- **Interactive Catalog & Multi-Filter Search**: Search by title, author, or ISBN; filter by category; real-time stock badges.
- **Book Issuance**: Borrow books with automated stock decrement, max borrow limit enforcement (3 books), and 14-day loan duration.
- **Book Return**: Return books with automated inventory replenishment and instant overdue penalty generation.
- **Automated Fine Engine**: Calculates fines at **₹5.00 per day** overdue. Accounts with unpaid fines are restricted from borrowing new books until cleared.
- **Advance Booking / Reservation**: Queue an advance reservation when a book is out of stock (`availableQuantity == 0`). When returned, the queue automatically advances.
- **Contact & Help Form**: Submit inquiries to the library administration directly from the portal.
- **Personal Dashboard**: View currently borrowed books, countdown to due dates, fine receipts, reservation statuses, and complete borrowing history.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Spring Boot, Spring MVC, Spring Data JPA |
| **Security** | Spring Security 6 (BCrypt Password Hashing, CSRF Protection, Role-based redirects) |
| **Persistence** | MySQL 8.x + Hibernate ORM |
| **Frontend UI** | HTML5, CSS3, Thymeleaf Templates, Bootstrap 5.3, Bootstrap Icons |
| **Java Platform** | Java 21 LTS |
| **Build Tool** | Apache Maven |

---

## 📋 Pre-Configured Demo Accounts

Upon initial startup with an empty database, `DataInitializer` seeds the database with sample categories, 7 essential books, and the following accounts:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@dlms.com` | `admin123` |
| **Member** | `user@dlms.com` | `user123` |

---

## ⚙️ Configuration (`application.properties`)

```properties
# Server Port
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/dlms_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Business Parameters
library.loan.duration-days=14
library.loan.max-books-per-user=3
library.fine.daily-rate=5.00
```

---

## 🏃 Running the Application

### 1. Ensure MySQL is Running
Make sure your local MySQL server is running on port `3306` with credentials matching `application.properties` (or update them accordingly).

### 2. Build and Run
```bash
# Using Maven
mvn spring-boot:run
```

Once started, open your browser and navigate to:
```
http://localhost:8081/
```

---

## 📂 Project Structure

```
d:\Projects\DLMS\
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/example/DLMS/
    │   │   ├── DlmsApplication.java
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java        # Auto-seeds Admin, Demo User, Categories, Books
    │   │   │   └── SecurityConfig.java         # Spring Security 6 RBAC configuration
    │   │   ├── controller/
    │   │   │   ├── AdminController.java        # Admin portal routes & actions
    │   │   │   ├── AuthController.java         # Login, Register, Logout
    │   │   │   ├── HomeController.java         # Catalog, Search, Contact
    │   │   │   └── UserController.java         # Borrow, Return, Reserve, Dashboard
    │   │   ├── dto/
    │   │   │   └── UserRegistrationDto.java
    │   │   ├── entity/
    │   │   │   ├── Book.java
    │   │   │   ├── BookIssue.java
    │   │   │   ├── Category.java
    │   │   │   ├── ContactQuery.java
    │   │   │   ├── Fine.java
    │   │   │   ├── Reservation.java
    │   │   │   └── User.java
    │   │   ├── enums/
    │   │   │   ├── FineStatus.java
    │   │   │   ├── IssueStatus.java
    │   │   │   ├── QueryStatus.java
    │   │   │   ├── ReservationStatus.java
    │   │   │   └── Role.java
    │   │   ├── exception/
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/                     # Spring Data JPA Repositories
    │   │   ├── security/
    │   │   │   ├── CustomAuthenticationSuccessHandler.java
    │   │   │   ├── CustomUserDetails.java
    │   │   │   └── CustomUserDetailsService.java
    │   │   └── service/                        # Transactional Business Services
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/style.css
    │       └── templates/
    │           ├── admin/                      # Admin views (Dashboard, Books, Loans, Fines, Queries, Members)
    │           ├── auth/                       # Login & Register views
    │           ├── error/                      # 403 & 500 error views
    │           ├── fragments/                  # Layout, Navbar, Alerts, Footer
    │           ├── catalog.html
    │           ├── contact.html
    │           ├── index.html
    │           └── user/dashboard.html
    └── test/
        └── java/com/example/DLMS/service/CirculationServiceTest.java
```

