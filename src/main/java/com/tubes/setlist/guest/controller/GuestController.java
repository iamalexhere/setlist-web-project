package com.tubes.setlist.guest.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import com.tubes.setlist.guest.model.EventListView;
import com.tubes.setlist.guest.model.EventView;
import com.tubes.setlist.guest.model.SetlistView;
import com.tubes.setlist.guest.repository.GuestRepository;

@Controller
@RequestMapping("/guest")
public class GuestController {
    @Autowired
    private GuestRepository guestRepository;

    @GetMapping("/artists")
    public String listArtists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        // Get all genres with counts
        Map<String, Integer> genreCounts = guestRepository.getGenreCounts();
        
        // Get artists based on search criteria or get all if no criteria
        List<ArtistView> searchedArtists;
        if ((name != null && !name.isEmpty()) || (genre != null && !genre.isEmpty())) {
            searchedArtists = guestRepository.findArtistsByNameAndGenre(
                name != null ? name : "", 
                genre != null && !genre.isEmpty() ? genre : null
            );
        } else {
            searchedArtists = guestRepository.findArtistsByName("");
        }
        
        // Handle pagination
        int totalArtists = searchedArtists.size();
        int totalPages = (int) Math.ceil((double) totalArtists / size);
        
        // Ensure page is within bounds
        page = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalArtists);
        
        List<ArtistView> paginatedArtists = searchedArtists.subList(
            Math.min(start, totalArtists), 
            Math.min(end, totalArtists)
        );

        // Add all necessary attributes to the model
        model.addAttribute("activePage", "artists");
        model.addAttribute("searchedArtists", paginatedArtists);
        model.addAttribute("searchQuery", name != null ? name : "");
        model.addAttribute("selectedGenre", genre != null ? genre : "");
        model.addAttribute("genreCounts", genreCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasSearch", name != null || genre != null);
        
        return "guest/artists";
    }

    @GetMapping("/artists/{id}")
    public String viewArtist(@PathVariable Long id, Model model) {
        ArtistView artist = guestRepository.findArtistById(id);
        List<EventView> events = guestRepository.findEventsByArtist(id);
        List<SetlistView> setlists = guestRepository.findSetlistsByArtist(id);
        
        model.addAttribute("artist", artist);
        model.addAttribute("events", events);
        model.addAttribute("setlists", setlists);
        return "guest/artist-detail";
    }

    @GetMapping("/events")
    public String viewEvents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String location,
            Model model) {
        
        List<EventListView> events;
        if (query != null || startDate != null || endDate != null || location != null) {
            events = guestRepository.searchEvents(query, startDate, endDate, location);
        } else {
            events = guestRepository.findAllEvents();
        }

        // Always set all model attributes
        model.addAttribute("activePage", "events");
        model.addAttribute("events", events);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("startDate", startDate != null ? startDate : LocalDate.now());
        model.addAttribute("endDate", endDate != null ? endDate : LocalDate.now().plusMonths(6));
        model.addAttribute("location", location != null ? location : "");

        return "guest/events";
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        EventView event = guestRepository.findEventById(id);
        List<SetlistView> setlists = guestRepository.findSetlistsByEvent(id);

        model.addAttribute("event", event);
        model.addAttribute("setlists", setlists);

        return "guest/event-detail";
    }

    @GetMapping("/setlists/{id}")
    public String viewSetlist(@PathVariable Long id, Model model) {
        SetlistView setlist = guestRepository.findSetlistById(id);
        model.addAttribute("setlist", setlist);
        return "guest/setlist-detail";
    }
}
