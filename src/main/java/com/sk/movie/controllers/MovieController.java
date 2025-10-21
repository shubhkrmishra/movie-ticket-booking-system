package com.sk.movie.controllers;


// Controller for movie management operations //

import com.sk.movie.dto.MovieRequest;
import com.sk.movie.dto.MovieResponse;
import com.sk.movie.security.RequiresRole;
import com.sk.movie.entities.UserRole;
import com.sk.movie.services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    // Add a new movie (Admin only) //

    @PostMapping
    @RequiresRole(UserRole.ADMIN)
    public ResponseEntity<MovieResponse> addMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.addMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update existing movie (Admin only) //

    @PutMapping("/{movieId}")
    @RequiresRole(UserRole.ADMIN)
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long movieId,
            @Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.updateMovie(movieId, request);
        return ResponseEntity.ok(response);
    }

    // Soft delete movie (Admin only) //

    @DeleteMapping("/{movieId}")
    @RequiresRole(UserRole.ADMIN)
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    // Get all active movies (Public access) //

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        List<MovieResponse> movies = movieService.getAllActiveMovies();
        return ResponseEntity.ok(movies);
    }

    // Search movies by title (Public access) //

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String title) {
        List<MovieResponse> movies = movieService.searchMovies(title);
        return ResponseEntity.ok(movies);
    }
}
