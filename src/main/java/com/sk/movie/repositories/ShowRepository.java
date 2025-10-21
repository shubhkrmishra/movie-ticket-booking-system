package com.sk.movie.repositories;


import com.sk.movie.entities.Show;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByMovieMovieIdAndIsActiveTrue(Long movieId);

    List<Show> findByShowTimeBetweenAndIsActiveTrue(LocalDateTime start, LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Show s WHERE s.showId = :showId")
    Optional<Show> findByIdWithLock(Long showId);

    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId " +
            "AND s.showTime >= :startTime AND s.isActive = true " +
            "ORDER BY s.showTime")
    List<Show> findUpcomingShowsForMovie(Long movieId, LocalDateTime startTime);
}
