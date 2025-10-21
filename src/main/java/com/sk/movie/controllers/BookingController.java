package com.sk.movie.controllers;

import com.sk.movie.dto.BookingRequest;
import com.sk.movie.dto.BookingResponse;
import com.sk.movie.entities.UserRole;
import com.sk.movie.security.RequiresRole;
import com.sk.movie.security.CurrentUser;
import com.sk.movie.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

// Controller for booking operations //

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Create a new booking (Customer only) //

    @PostMapping
    @RequiresRole(UserRole.CUSTOMER)
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @CurrentUser Long userId) {
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get user's own bookings (Customer) //

    @GetMapping("/my-bookings")
    @RequiresRole(UserRole.CUSTOMER)
    public ResponseEntity<List<BookingResponse>> getMyBookings(@CurrentUser Long userId) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    // Get all bookings (Admin only) //

    @GetMapping
    @RequiresRole(UserRole.ADMIN)
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Cancel a booking //

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @CurrentUser Long userId,
            @RequestAttribute("userRole") UserRole role) {
        boolean isAdmin = role == UserRole.ADMIN;
        bookingService.cancelBooking(bookingId, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
