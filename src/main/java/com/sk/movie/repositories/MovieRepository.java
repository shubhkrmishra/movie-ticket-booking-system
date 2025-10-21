package com.sk.movie.repositories;

import com.sk.movie.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByIsActiveTrue();
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByGenreAndIsActiveTrue(String genre);
}
