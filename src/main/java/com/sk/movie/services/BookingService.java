package com.sk.movie.services;

import com.sk.movie.dto.BookingRequest;
import com.sk.movie.dto.BookingResponse;
import com.sk.movie.entities.*;
import com.sk.movie.exceptions.*;
import com.sk.movie.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private PromotionService promotionService;

    //     * Creates a new booking with concurrency safety //
    //     * Uses SERIALIZABLE isolation level and pessimistic locking to ensure //
    //     * that seats cannot be double-booked even under high concurrency //

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Lock the show to prevent concurrent modifications
        Show show = showRepository.findByIdWithLock(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Validate show is active and in future
        if (!show.getIsActive()) {
            throw new BusinessException("Show is not active");
        }
        if (show.getShowTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot book seats for past shows");
        }

        // Validate seat availability
        int requestedSeats = request.getSeatIds().size();
        if (requestedSeats == 0) {
            throw new BusinessException("At least one seat must be selected");
        }
        if (requestedSeats > show.getAvailableSeats()) {
            throw new InsufficientSeatsException(
                    "Only " + show.getAvailableSeats() + " seats available, requested " + requestedSeats
            );
        }

        // Lock and validate specific seats
        List<Seat> seats = seatRepository.findAvailableSeatsWithLock(request.getSeatIds());
        if (seats.size() != requestedSeats) {
            throw new SeatAlreadyBookedException("One or more selected seats are no longer available");
        }

        // Verify all seats belong to the correct show
        boolean allSeatsValid = seats.stream()
                .allMatch(seat -> seat.getShow().getShowId().equals(show.getShowId()));
        if (!allSeatsValid) {
            throw new BusinessException("Invalid seat selection for this show");
        }

        // Calculate pricing
        BigDecimal basePrice = show.getPricePerSeat();
        BigDecimal totalAmount = basePrice.multiply(BigDecimal.valueOf(requestedSeats));
        BigDecimal discountAmount = BigDecimal.ZERO;
        PromoCode appliedPromo = null;

        // Apply promo code if provided
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            appliedPromo = validateAndApplyPromo(
                    request.getPromoCode(), user, totalAmount, requestedSeats
            );
            discountAmount = calculateDiscount(appliedPromo, basePrice, requestedSeats);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // Create booking //

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingReference(generateBookingReference());
        booking.setTotalSeats(requestedSeats);
        booking.setTotalAmount(totalAmount);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking.setPromoCode(appliedPromo);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.getSeats().addAll(seats);

        // Mark seats as booked
        seats.forEach(seat -> seat.setIsBooked(true));
        seatRepository.saveAll(seats);

        // Update show available seats
        show.setAvailableSeats(show.getAvailableSeats() - requestedSeats);
        showRepository.save(show);

        // Save booking
        booking = bookingRepository.save(booking);

        // Update promo code usage if applied
        if (appliedPromo != null) {
            appliedPromo.setCurrentUses(appliedPromo.getCurrentUses() + 1);
            promoCodeRepository.save(appliedPromo);
        }

        return mapToBookingResponse(booking);
    }

    //Validates promo code and checks user eligibility//

    private PromoCode validateAndApplyPromo(String promoCodeStr, User user,
                                            BigDecimal totalAmount, int seatCount) {
        // Find promo code
        PromoCode promoCode = promoCodeRepository.findByCodeAndIsActiveTrue(promoCodeStr)
                .orElseThrow(() -> new InvalidPromoCodeException("Invalid or inactive promo code"));

        // Check validity period
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promoCode.getValidFrom()) || now.isAfter(promoCode.getValidUntil())) {
            throw new InvalidPromoCodeException("Promo code has expired");
        }

        // Check usage limit
        if (promoCode.getMaxUses() != null &&
                promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
            throw new InvalidPromoCodeException("Promo code usage limit reached");
        }

        // Check user eligibility (>5 bookings OR >1500 spent)
        if (!promotionService.isUserEligibleForPromo(user.getUserId())) {
            throw new InvalidPromoCodeException(
                    "You are not eligible for promotions. Complete 5+ bookings or spend ₹1500+ to qualify"
            );
        }

        // Special validation for FREE_SEAT discount
        if (promoCode.getDiscountType() == DiscountType.FREE_SEAT && seatCount < 2) {
            throw new BusinessException("Free seat promotion requires booking at least 2 seats");
        }

        return promoCode;
    }

    //Calculates discount amount based on promo type//

    private BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal pricePerSeat, int seatCount) {
        switch (promoCode.getDiscountType()) {
            case FREE_SEAT:
                // One seat free
                return pricePerSeat.multiply(BigDecimal.ONE);
            case FLAT_DISCOUNT:
                // Flat 250 discount (or promo value)
                return promoCode.getDiscountValue();
            default:
                return BigDecimal.ZERO;
        }
    }


    //Generates unique booking reference//

    private String generateBookingReference() {
        return "BKG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    //Retrieves all bookings for a specific user//

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserUserIdOrderByBookingTimeDesc(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    //Retrieves all bookings (Admin only)//

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    //Cancels a booking and releases seats//

    @Transactional
    public void cancelBooking(Long bookingId, Long userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Authorization check
        if (!isAdmin && !booking.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own bookings");
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }

        // Update booking status
        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Release seats
        booking.getSeats().forEach(seat -> seat.setIsBooked(false));

        // Update show available seats
        Show show = booking.getShow();
        show.setAvailableSeats(show.getAvailableSeats() + booking.getTotalSeats());

        bookingRepository.save(booking);
    }

    //Maps Booking entity to BookingResponse DTO//

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setBookingReference(booking.getBookingReference());
        response.setMovieTitle(booking.getShow().getMovie().getTitle());
        response.setShowTime(booking.getShow().getShowTime());
        response.setScreenName(booking.getShow().getScreenName());
        response.setTotalSeats(booking.getTotalSeats());
        response.setTotalAmount(booking.getTotalAmount());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setFinalAmount(booking.getFinalAmount());
        response.setBookingStatus(booking.getBookingStatus().toString());
        response.setBookingTime(booking.getBookingTime());
        response.setSeatNumbers(
                booking.getSeats().stream()
                        .map(seat -> seat.getRowName() + seat.getSeatNumber())
                        .toList()
        );
        if (booking.getPromoCode() != null) {
            response.setPromoCodeUsed(booking.getPromoCode().getCode());
        }
        return response;
    }
}
