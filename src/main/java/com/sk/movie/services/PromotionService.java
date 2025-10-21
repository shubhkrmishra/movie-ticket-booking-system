package com.sk.movie.services;

import com.sk.movie.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

// Service handling promotion eligibility checks //

@Service
public class PromotionService {
    private static final Long MIN_BOOKINGS_FOR_PROMO = 5L;
    private static final BigDecimal MIN_SPENDING_FOR_PROMO = new BigDecimal("1500");

    @Autowired
    private BookingRepository bookingRepository;

    //     * Checks if user is eligible for promotions //
    //     * Eligibility criteria: >5 bookings OR >1500 total spending //

    @Transactional(readOnly = true)
    public boolean isUserEligibleForPromo(Long userId) {
        Long bookingCount = bookingRepository.countConfirmedBookingsByUser(userId);
        BigDecimal totalSpending = bookingRepository.getTotalSpendingByUser(userId);

        return bookingCount > MIN_BOOKINGS_FOR_PROMO ||
                totalSpending.compareTo(MIN_SPENDING_FOR_PROMO) > 0;
    }

    // Gets user's booking statistics for promotion tracking //

    @Transactional(readOnly = true)
    public PromotionEligibility getPromotionEligibility(Long userId) {
        Long bookingCount = bookingRepository.countConfirmedBookingsByUser(userId);
        BigDecimal totalSpending = bookingRepository.getTotalSpendingByUser(userId);

        boolean eligible = bookingCount > MIN_BOOKINGS_FOR_PROMO ||
                totalSpending.compareTo(MIN_SPENDING_FOR_PROMO) > 0;

        return new PromotionEligibility(eligible, bookingCount, totalSpending);
    }

    // Inner class for eligibility response
    public static class PromotionEligibility {
        private boolean eligible;
        private Long totalBookings;
        private BigDecimal totalSpending;

        public PromotionEligibility(boolean eligible, Long totalBookings, BigDecimal totalSpending) {
            this.eligible = eligible;
            this.totalBookings = totalBookings;
            this.totalSpending = totalSpending;
        }

        public boolean isEligible() { return eligible; }
        public Long getTotalBookings() { return totalBookings; }
        public BigDecimal getTotalSpending() { return totalSpending; }
    }

}
