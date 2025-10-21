package com.sk.movie.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(nullable = false, length = 10)
    private String seatNumber;

    @Column(nullable = false, length = 5)
    private String rowName;

    @Column(nullable = false)
    private Boolean isBooked = false;

    @Version
    private Long version; // Optimistic locking

    // Getters and setters
    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public Show getShow() { return show; }
    public void setShow(Show show) { this.show = show; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getRowName() { return rowName; }
    public void setRowName(String rowName) { this.rowName = rowName; }

    public Boolean getIsBooked() { return isBooked; }
    public void setIsBooked(Boolean isBooked) { this.isBooked = isBooked; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
