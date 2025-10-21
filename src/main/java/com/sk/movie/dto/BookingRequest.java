package com.sk.movie.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BookingRequest {
    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> seatIds;

    private String promoCode; // Optional

    // Getters and setters
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
}
