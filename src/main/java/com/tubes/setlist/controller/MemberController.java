package com.tubes.setlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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
        List<DummyArtist> artists = getDummyArtists();
        model.addAttribute("artists", artists);
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

    // Dummy data for testing
    private List<DummyArtist> getDummyArtists() {
        List<DummyArtist> artists = new ArrayList<>();
        artists.add(new DummyArtist(1L, "Taylor Swift", "Pop", 
            "https://example.com/taylor.jpg", 150, 300));
        artists.add(new DummyArtist(2L, "Ed Sheeran", "Pop/Folk", 
            "https://example.com/ed.jpg", 100, 200));
        artists.add(new DummyArtist(3L, "Coldplay", "Alternative Rock", 
            "https://example.com/coldplay.jpg", 120, 250));
        return artists;
    }

    // Dummy Artist class for testing
    private static class DummyArtist {
        private Long id;
        private String name;
        private String genre;
        private String imageUrl;
        private int showCount;
        private int setlistCount;

        public DummyArtist(Long id, String name, String genre, String imageUrl, 
                          int showCount, int setlistCount) {
            this.id = id;
            this.name = name;
            this.genre = genre;
            this.imageUrl = imageUrl;
            this.showCount = showCount;
            this.setlistCount = setlistCount;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getGenre() { return genre; }
        public String getImageUrl() { return imageUrl; }
        public int getShowCount() { return showCount; }
        public int getSetlistCount() { return setlistCount; }
    }
}
