package com.sk.movie.repositories;

import com.sk.movie.entities.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowShowIdAndIsBookedFalse(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.seatId IN :seatIds AND s.isBooked = false")
    List<Seat> findAvailableSeatsWithLock(List<Long> seatIds);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.show.showId = :showId AND s.isBooked = false")
    Integer countAvailableSeats(Long showId);
}
