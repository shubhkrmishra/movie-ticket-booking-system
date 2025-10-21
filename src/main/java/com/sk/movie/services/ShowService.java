package com.sk.movie.services;

import com.sk.movie.dto.ShowRequest;
import com.sk.movie.dto.ShowResponse;
import com.sk.movie.entities.Movie;
import com.sk.movie.entities.Show;
import com.sk.movie.repositories.MovieRepository;
import com.sk.movie.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private MovieRepository movieRepository;

    public ShowResponse addShow(ShowRequest req) {
        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Show show = new Show();
        show.setMovie(movie);
        show.setShowTime(req.getShowTime());
        show.setScreenName(req.getScreenName());
        show.setPricePerSeat(BigDecimal.valueOf(req.getPricePerSeat()));
        show.setTotalSeats(req.getTotalSeats());
        show.setAvailableSeats(req.getTotalSeats());
        showRepository.save(show);
        return map(show);
    }

    public List<ShowResponse> listShows() {
        return showRepository.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    private ShowResponse map(Show s) {
        ShowResponse r = new ShowResponse();
        r.setShowId(s.getShowId());
        r.setMovieTitle(s.getMovie().getTitle());
        r.setShowTime(s.getShowTime());
        r.setScreenName(s.getScreenName());
        r.setTotalSeats(s.getTotalSeats());
        r.setAvailableSeats(s.getAvailableSeats());
        r.setPricePerSeat(s.getPricePerSeat().doubleValue());
        return r;
    }
}
