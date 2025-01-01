package com.tubes.setlist.guest.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tubes.setlist.guest.repository.GuestRepository;

@Controller
@RequestMapping("/guest")
public class GuestController {
    
    @Autowired
    private GuestRepository guestRepository;

    @GetMapping("/artists")
    public String searchArtists(@RequestParam(required = false) String name, Model model) {
        if (name != null && !name.trim().isEmpty()) {
            model.addAttribute("artists", guestRepository.findArtistsByName(name));
        }
        return "guest/artists";
    }

    @GetMapping("/artists/{id}")
    public String viewArtist(@PathVariable Long id, Model model) {
        model.addAttribute("artist", guestRepository.findArtistById(id));
        model.addAttribute("events", guestRepository.findEventsByArtist(id));
        return "guest/artist";
    }

    @GetMapping("/events")
    public String searchEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String location,
            Model model) {
        
        if (startDate != null && endDate != null && location != null && !location.trim().isEmpty()) {
            model.addAttribute("events", 
                guestRepository.findEventsByDateAndLocation(startDate, endDate, location));
        }
        return "guest/events";
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        model.addAttribute("event", guestRepository.findEventById(id));
        model.addAttribute("setlists", guestRepository.findSetlistsByEvent(id));
        return "guest/events";
    }

    @GetMapping("/setlists/{id}")
    public String viewSetlist(@PathVariable Long id, Model model) {
        model.addAttribute("setlists", guestRepository.findSetlistById(id));
        return "guest/setlists";
    }
}
