Movie Ticket Booking System
Overview

The Movie Ticket Booking System is a backend project developed using Spring Boot (Java 17) and MySQL.
It allows users to search and book movie tickets online while providing admin capabilities for managing movies and shows.
The project enforces Role-Based Access Control (RBAC), supports promotional discounts, and ensures safe concurrent seat booking through transactional and locking mechanisms.

Features
Admin

Add, update, delete, and view movies and shows.

View all customer bookings.

Customer

Register/login using JWT-based authentication.

Search movies and shows.

View available seats.

Book one or more seats.

Apply promo codes for discounts.

View personal booking history.

Additional Features

Concurrency-safe seat booking using Pessimistic Locking and Unique Constraints.

Promotions:

Eligible users (more than 5 bookings or total spend > ₹1500) can use:

FREE_SEAT promo → 1 free seat.

FLAT_250 promo → ₹250 discount.

Error handling with proper HTTP status codes and JSON responses.

Role-Based Access Control (RBAC) using Spring Security & JWT.

Project Architecture

Architecture Pattern: Layered Architecture
Layers:

Controller → Service → Repository → Entity (JPA)


Technologies Used

Category	Technology
Language	Java 17
Framework	Spring Boot 3.x
Security	Spring Security + JWT
ORM	Spring Data JPA (Hibernate)
Database	MySQL 8+
Validation	Jakarta Validation (JSR-380)
Build Tool	Maven
Tools	Lombok, Swagger (springdoc-openapi)

System Design
Entity Relationship Diagram (Simplified)
User (1) ────< (M) Booking >──── (1) Show >──── (1) Movie
                  │
                  │
                  v
                BookedSeat >──── (1) Seat

Tables

users → store user info with role (ADMIN/CUSTOMER)

movies → movie master data

shows → scheduled movie shows

seats → seats per show

bookings → booking header with totals and promo

booked_seats → booked seats per booking

promo_codes → available promotional codes

Booking Flow
1. Customer selects show & seats

User picks available seats from /api/shows/{id}/seats.

2. API call: /api/bookings

Request Example

{
  "showId": 101,
  "seatNumbers": ["A1", "A2"],
  "promoCode": "FREESEAT1"
}

3. Validation steps

Validate show timing and seat availability.

Lock selected seats using @Lock(PESSIMISTIC_WRITE) to prevent concurrent conflicts.

If any seat is already booked → return 409 Conflict.

Validate and apply promo (if eligible).

4. Booking confirmation

Response Example

{
  "bookingId": 501,
  "seats": ["A1", "A2"],
  "subtotal": 500.00,
  "discount": 250.00,
  "total": 250.00,
  "promoApplied": "FLAT250",
  "status": "CONFIRMED"
}

Security & Authentication

Users register via /api/auth/register (default role: CUSTOMER)

Login via /api/auth/login

JWT token returned in response → used for all subsequent requests in headers:

Authorization: Bearer <token>


Access Control:

/api/admin/** → ADMIN only

/api/bookings/** → CUSTOMER only

/api/movies/**, /api/shows/** → open (read-only)

Concurrency Handling

To prevent double booking:

Seats are locked using Pessimistic Lock (@Lock(LockModeType.PESSIMISTIC_WRITE)) during booking.

Each seat has a unique constraint in booked_seats table.

If two users try to book the same seat simultaneously:

One transaction succeeds.

The other fails with 409 Conflict: Seat already booked.

Promo Logic
Condition	Eligible
Total bookings > 5	✅ Yes
OR Total amount spent > ₹1500	✅ Yes
Types

FREE_SEAT → Discount = 1 seat’s price.

FLAT_250 → ₹250 off (max up to total amount).

Validation

Expired or inactive promo codes are rejected.

Ineligible users cannot apply promo codes.

API Endpoints
Authentication
Method	Endpoint	Description
POST	/api/auth/register	Register a new customer
POST	/api/auth/login	Login and get JWT token

Admin
Method	Endpoint	Description
POST	/api/admin/movies	Add a movie
PUT	/api/admin/movies/{id}	Update movie
DELETE	/api/admin/movies/{id}	Delete movie
POST	/api/admin/shows	Add a show
GET	/api/admin/bookings	View all bookings

Customer
Method	Endpoint	Description
GET	/api/movies	Search movies
GET	/api/shows?movieId={id}	Get shows by movie
GET	/api/shows/{id}/seats	View available seats
POST	/api/bookings	Book tickets (with promo)
GET	/api/bookings/me	View own bookings

Setup & Run Instructions
Prerequisites

Java 17+

Maven 3+

MySQL 8+

Steps to Run

Clone the repository

git clone https://github.com/yourusername/movie-ticket-booking-system.git
cd movie-ticket-booking-system


Create Database

CREATE DATABASE moviebooking;


Configure DB in application.yml

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/movie_ticket_booking
    username: root
    password: yourpassword
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
jwt:
  secret: your-secret-key
  expiration-minutes: 120


Build & Run

mvn clean install
mvn spring-boot:run


Access API Docs

http://localhost:8080/swagger-ui.html

Testing
Concurrency Test

Simulated multiple users booking the same seat:

Only one booking succeeds.

Second returns HTTP 409 Conflict.

Promo Test

User with 6 bookings applied FREE_SEAT successfully.

New user rejected with “Not eligible for promo”.

Folder Structure
src/main/java/com/moviebooking
│
├── controller/
├── service/
├── repository/
├── entity/
└── config/

Key Highlights

✅ Clean, modular code with layered architecture
✅ JWT authentication and RBAC
✅ Concurrency-safe seat booking
✅ Promo code and eligibility logic
✅ Well-documented REST APIs
✅ Error-handling and validation
✅ Ready to deploy with MySQL

Author

Shubham Kumar
Java Full Stack Developer (4+ Years Experience)

🏁 Conclusion

This project demonstrates backend design, concurrency handling, and RBAC in a real-world application context.
It is optimized for clarity, scalability, and robustness, making it a strong demonstration of backend development proficiency.