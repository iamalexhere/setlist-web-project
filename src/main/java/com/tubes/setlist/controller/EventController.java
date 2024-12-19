package com.tubes.setlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/events")
public class EventController {
    
    @GetMapping("")
    public String listEvents(Model model) {
        // TODO: Add event data from database
        // For now, we'll use sample data
        return "guest/events";
    }
}
