package com.tubes.setlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/artists")
public class ArtistController {
    
    @GetMapping("")
    public String listArtists(Model model) {
        // TODO: Add artist data from database
        // For now, we'll add some sample data
        return "guest/artists";
    }
}
