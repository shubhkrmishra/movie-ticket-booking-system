package com.sk.movie.controllers;

import com.sk.movie.dto.ShowRequest;
import com.sk.movie.dto.ShowResponse;
import com.sk.movie.services.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {
    @Autowired
    private ShowService showService;

    @PostMapping
    public ShowResponse add(@RequestBody ShowRequest req) { return showService.addShow(req); }

    @GetMapping
    public List<ShowResponse> list() { return showService.listShows(); }
}