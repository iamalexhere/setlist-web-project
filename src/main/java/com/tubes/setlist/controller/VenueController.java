package com.tubes.setlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/venues")
public class VenueController {
    
    @GetMapping("")
    public String listVenues(Model model) {
        // TODO: Add venue data from database
        // For now, we'll use sample data
        return "guest/venues";
    }
}
