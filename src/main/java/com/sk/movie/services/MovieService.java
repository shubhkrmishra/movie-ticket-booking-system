package com.sk.movie.services;

import com.sk.movie.dto.MovieRequest;
import com.sk.movie.dto.MovieResponse;
import com.sk.movie.entities.Movie;
import com.sk.movie.exceptions.ResourceNotFoundException;
import com.sk.movie.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Transactional
    public MovieResponse addMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setRating(request.getRating());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setIsActive(true);

        movie = movieRepository.save(movie);
        return mapToMovieResponse(movie);
    }

    @Transactional
    public MovieResponse updateMovie(Long movieId, MovieRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setRating(request.getRating());
        movie.setReleaseDate(request.getReleaseDate());

        movie = movieRepository.save(movie);
        return mapToMovieResponse(movie);
    }

    @Transactional
    public void deleteMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        movie.setIsActive(false);
        movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getAllActiveMovies() {
        return movieRepository.findByIsActiveTrue().stream()
                .map(this::mapToMovieResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToMovieResponse)
                .collect(Collectors.toList());
    }

    private MovieResponse mapToMovieResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setMovieId(movie.getMovieId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setGenre(movie.getGenre());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setRating(movie.getRating());
        response.setReleaseDate(movie.getReleaseDate());
        response.setActive(movie.getIsActive());
        return response;
    }
}
