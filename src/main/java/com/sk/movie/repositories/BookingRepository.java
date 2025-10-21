package com.sk.movie.repositories;

import com.sk.movie.entities.Booking;
import com.sk.movie.entities.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserUserIdOrderByBookingTimeDesc(Long userId);

    List<Booking> findByShowShowIdAndBookingStatus(Long showId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.userId = :userId AND b.bookingStatus = 'CONFIRMED'")
    Long countConfirmedBookingsByUser(Long userId);

    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.user.userId = :userId AND b.bookingStatus = 'CONFIRMED'")
    BigDecimal getTotalSpendingByUser(Long userId);
}
