package com.tubes.setlist.guest.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.guest.repository.GuestRepository;

@Controller
@RequestMapping("/guest")
public class GuestController {
    @Autowired
    private GuestRepository guestRepository;

    @GetMapping("/artists")
    public String listArtists(@RequestParam(required = false) String name, Model model) {
        // Get featured artists (first 3 artists)
        List<ArtistView> allArtists = guestRepository.findArtistsByName("");
        List<ArtistView> featuredArtists = allArtists.stream()
            .limit(3)
            .collect(Collectors.toList());
        
        // Get searched artists if name parameter exists
        List<ArtistView> searchedArtists = name != null ? 
            guestRepository.findArtistsByName(name) : 
            allArtists;

        model.addAttribute("featuredArtists", featuredArtists);
        model.addAttribute("searchedArtists", searchedArtists);
        model.addAttribute("searchQuery", name);
        return "guest/artists";
    }

    @GetMapping("/artists/{id}")
    public String viewArtist(@PathVariable Long id, Model model) {
        model.addAttribute("artist", guestRepository.findArtistById(id));
        model.addAttribute("events", guestRepository.findEventsByArtist(id));
        return "guest/artists";
    }

    @GetMapping("/events")
    public String listEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String location,
            Model model) {
        if (startDate != null && endDate != null && location != null) {
            model.addAttribute("events", guestRepository.findEventsByDateAndLocation(startDate, endDate, location));
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
