package com.tubes.setlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Controller
@RequestMapping("/member")
public class MemberController {

    // Dashboard
    @GetMapping("")
    public String dashboard() {
        return "member/dashboard";
    }

    // Artist Management
    @GetMapping("/artists")
    public String artists(Model model) {
        // TODO: Replace with actual data from service
        return "member/artists";
    }

    @GetMapping("/artists/add")
    public String showAddArtistForm() {
        return "member/add-artist";
    }

    @PostMapping("/artists/add")
    public String addArtist(@RequestParam("name") String name,
                           @RequestParam("genre") String genre,
                           @RequestParam("description") String description,
                           @RequestParam(value = "image", required = false) MultipartFile image,
                           @RequestParam(value = "socialLinks", required = false) String socialLinks) {
        // TODO: Implement artist creation logic
        return "redirect:/member/artists";
    }

    // Show Management
    @GetMapping("/shows")
    public String shows(Model model) {
        // TODO: Replace with actual data from service
        // List<DummyShow> shows = getDummyShows();
        // model.addAttribute("shows", shows);
        return "member/shows";
    }

    @GetMapping("/shows/add")
    public String showAddShowForm(Model model) {
        return "member/add-show";
    }

    @PostMapping("/shows/add")
    public String addShow(@RequestParam("artistId") Long artistId,
                         @RequestParam("venueId") Long venueId,
                         @RequestParam("date") String date,
                         @RequestParam("time") String time,
                         @RequestParam(value = "tourName", required = false) String tourName,
                         @RequestParam(value = "description", required = false) String description) {
        // TODO: Implement show creation logic
        return "redirect:/member/shows";
    }

    // Setlist Management
    @GetMapping("/setlists")
    public String setlists(Model model) {
        return "member/setlists";
    }

    @GetMapping("/setlists/add")
    public String showAddSetlistForm(Model model) {
        return "member/add-setlist";
    }

    @PostMapping("/setlists/add")
    public String addSetlist(@RequestParam("showId") Long showId,
                           @RequestParam("songData") String songData,
                           @RequestParam(value = "notes", required = false) String notes,
                           @RequestParam(value = "proof", required = false) MultipartFile proof) {
        // TODO: Parse songData JSON and implement setlist creation logic
        return "redirect:/member/setlists";
    }

    @GetMapping("/setlists/{id}/comments")
    public String setlistComments(@PathVariable("id") Long setlistId, Model model) {
        return "member/setlist-comments";
    }

    @PostMapping("/setlists/{id}/comments")
    public String addSetlistComment(@PathVariable("id") Long setlistId,
                                  @RequestParam("comment") String comment) {
        return "redirect:/member/setlists/" + setlistId + "/comments";
    }

    // Song Search API
    @GetMapping("/api/songs/search")
    @ResponseBody
    public List<SongDTO> searchSongs(@RequestParam("q") String query) {
        // TODO: Implement actual song search from database
        // This is just a dummy implementation for testing
        if (query.trim().isEmpty()) {
            return emptyList();
        }

        List<SongDTO> results = new ArrayList<>();
        results.add(new SongDTO(1L, "Shake It Off", "3:39"));
        results.add(new SongDTO(2L, "Love Story", "3:55"));
        results.add(new SongDTO(3L, "Blank Space", "3:51"));
        return results.stream()
                .filter(song -> song.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    // DTO for song search results
    private static class SongDTO {
        private Long id;
        private String title;
        private String duration;

        public SongDTO(Long id, String title, String duration) {
            this.id = id;
            this.title = title;
            this.duration = duration;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDuration() { return duration; }
    }
}
