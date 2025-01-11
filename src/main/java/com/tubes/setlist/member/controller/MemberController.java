package com.tubes.setlist.member.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.Edit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.EventsVenues;
import com.tubes.setlist.member.model.GenreView;
import com.tubes.setlist.member.model.Venues;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Songs;
import com.tubes.setlist.member.model.Setlist;

import com.tubes.setlist.member.repository.MemberRepository;
import com.tubes.setlist.member.service.FileStorageService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    MemberRepository repo;

    @Autowired
    private FileStorageService fileStorageService;
    
    private boolean checkMemberAccess(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equals("Member");
    }
    
    private void addUserAttributes(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "member/dashboard";
    }
    
    @GetMapping("/artists")
    public String artists(
        HttpSession session,
        Model model,
        @RequestParam(defaultValue = "") String name,
        @RequestParam(required = false) String genre,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "6") int size
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        // Get genre counts for filter
        Map<String, Long> genreCounts = repo.getGenreCounts();

        // First get all matching artists to calculate total correctly
        List<Artists> allMatchingArtists = repo.findArtistsByNameAndGenre(name, genre, 1, Integer.MAX_VALUE);
        int totalArtists = allMatchingArtists.size();
        
        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalArtists / size);
        
        // Ensure page is within bounds
        page = Math.max(1, Math.min(page, totalPages));
        
        // Get paginated artists
        List<Artists> artists = repo.findArtistsByNameAndGenre(name, genre, page, size);

        // Add all necessary attributes to model
        model.addAttribute("artists", artists);
        model.addAttribute("genreCounts", genreCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("searchQuery", name);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("hasSearch", !name.isEmpty() || genre != null);

        return "member/artists";
    }
    
    @GetMapping("/add-artist")
    public String addArtist(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        List<GenreView> categories = this.repo.findAllGenre();

        model.addAttribute("genres", categories);
        return "member/add-artist";
    }
    
    @GetMapping("/setlists")
    public String setlists(
        HttpSession session,
        Model model,
        @RequestParam(defaultValue = "") String artist,
        @RequestParam(defaultValue = "") String event,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int size
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        // Get total setlists count for pagination
        long totalSetlists = repo.getTotalSetlists(artist, event);
        int totalPages = (int) Math.ceil((double) totalSetlists / size);
        
        // Ensure page is within bounds
        page = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        // Get paginated setlists
        List<Setlist> setlists = repo.findSetlists(artist, event, page, size);

        // Add model attributes
        model.addAttribute("setlists", setlists);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("artist", artist);
        model.addAttribute("event", event);
        model.addAttribute("hasSearch", !artist.isEmpty() || !event.isEmpty());

        return "member/setlists";
    }
    
    @GetMapping("/add-setlist")
    public String addSetlist(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        // Get list of artists for dropdown
        List<Artists> artists = repo.findArtistsByName("");
        model.addAttribute("artists", artists);

        // Get list of events for dropdown
        List<EventsVenues> events = repo.findAllEvents();
        model.addAttribute("events", events);

        return "member/add-setlist";
    }

    @GetMapping("/api/songs/by-artist/{artistId}")
    @ResponseBody
    public List<Songs> getSongsByArtist(@PathVariable Long artistId) {
        return repo.findSongsByArtist(artistId);
    }

    @GetMapping("/setlists/{id}")
    public String getSetlistDetail(@PathVariable Long id, Model model, HttpSession session) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        
        Setlist setlist = repo.findSetlistById(id);
        if (setlist == null) {
            return "redirect:/member/setlists";
        }
        
        model.addAttribute("setlist", setlist);
        return "member/setlist-detail";
    }

    @PostMapping("/setlists/add")
    public String addSetlist(
        @RequestParam("name") String setlistName,
        @RequestParam("artistId") Long artistId,
        @RequestParam("eventId") Long eventId,
        @RequestParam(value = "songIds", required = false) List<Long> songIds,
        @RequestParam(value = "proof", required = false) MultipartFile proof,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        try {
            String proofFilename = null;
            String proofOriginalFilename = null;

            if (proof != null && !proof.isEmpty()) {
                proofFilename = fileStorageService.storeFile(proof);
                proofOriginalFilename = proof.getOriginalFilename();
            }

            // Add setlist
            Long setlistId = repo.addSetlist(setlistName, artistId, eventId, songIds, proofFilename, proofOriginalFilename);
            
            // Add edit record for draft status
            Long userId = (Long) session.getAttribute("userId");
            repo.saveEdit(new Edit(setlistId, null, userId, "Initial draft creation", "draft"));

            return "redirect:/member/setlists";
        } catch (Exception e) {
            // Repopulate dropdowns in case of error
            List<Artists> artists = repo.findArtistsByName("");
            List<EventsVenues> events = repo.findAllEvents();
            model.addAttribute("artists", artists);
            model.addAttribute("events", events);
            model.addAttribute("error", "An error occurred while adding the setlist: " + e.getMessage());
            return "member/add-setlist";
        }
    }


    @GetMapping("/setlists/{id}/edit")
    public String editSetlist(
        @PathVariable Long id,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        Setlist setlist = repo.findSetlistById(id);
        if (setlist == null) {
            return "redirect:/member/setlists";
        }

        // Get list of artists for dropdown
        List<Artists> artists = repo.findArtistsByName("");
        model.addAttribute("artists", artists);

        // Get list of events for dropdown
        List<EventsVenues> events = repo.findAllEvents();
        model.addAttribute("events", events);

        // Get all available songs for the artist
        List<Songs> availableSongs = repo.findSongsByArtist(setlist.getIdArtist());
        model.addAttribute("songs", availableSongs);

        // Get songs that are currently in the setlist
        List<Long> setlistSongIds = repo.getSetlistSongs(id);
        List<Songs> selectedSongs = availableSongs.stream()
            .filter(song -> setlistSongIds.contains(song.getIdSong()))
            .collect(Collectors.toList());
        
        // Pass both available and selected songs to the view
        model.addAttribute("currentSongs", selectedSongs);
        
        // Convert songs to JSON for JavaScript
        model.addAttribute("songsJson", availableSongs);

        model.addAttribute("setlist", setlist);
        return "member/edit-setlist";
    }

    @PostMapping("/setlists/{id}/edit")
    public String updateSetlist(
        @PathVariable Long id,
        @RequestParam("name") String setlistName,
        @RequestParam("artistId") Long artistId,
        @RequestParam("eventId") Long eventId,
        @RequestParam(value = "songIds", required = false) List<Long> songIds,
        @RequestParam(value = "proof", required = false) MultipartFile proof,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        try {
            String proofFilename = null;
            String proofOriginalFilename = null;

            if (proof != null && !proof.isEmpty()) {
                proofFilename = fileStorageService.storeFile(proof);
                proofOriginalFilename = proof.getOriginalFilename();
            }

            repo.updateSetlist(id, setlistName, artistId, eventId, songIds, proofFilename, proofOriginalFilename);
            return "redirect:/member/setlists";
        } catch (Exception e) {
            Setlist setlist = repo.findSetlistById(id);
            List<Artists> artists = repo.findArtistsByName("");
            List<EventsVenues> events = repo.findAllEvents();
            List<Songs> currentSongs = repo.findSongsByArtist(setlist.getIdArtist());

            model.addAttribute("setlist", setlist);
            model.addAttribute("artists", artists);
            model.addAttribute("events", events);
            model.addAttribute("songs", currentSongs);
            model.addAttribute("currentSongs", currentSongs.stream()
                .filter(song -> repo.getSetlistSongs(id).contains(song.getIdSong()))
                .collect(Collectors.toList()));
            model.addAttribute("error", "An error occurred while updating the setlist: " + e.getMessage());
            
            return "member/edit-setlist";
        }
    }
    
    @GetMapping("/shows")
    public String shows(
        HttpSession session, 
        @RequestParam(defaultValue = "") String query,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "9") int size,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        List<EventsVenues> events = this.repo.findFilteredEvents(query, startDate, endDate);
        
        model.addAttribute("shows", events);
        model.addAttribute("query", query);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "member/shows";
    }
    
    @GetMapping("/shows/add")
    public String showAddShowForm(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        // Add necessary data for dropdowns
        List<Artists> artists = repo.findArtistsByName("");
        List<Venues> venues = repo.findAllVenues();
        model.addAttribute("artists", artists);
        model.addAttribute("venues", venues);
        
        return "member/add-show";
    }

    @PostMapping("/shows/add")
    public String memberAddShow(
            @RequestParam("eventName") String eventName,
            @RequestParam("artistId") Long artistId,
            @RequestParam("venueId") Long venueId,
            @RequestParam("eventDate") LocalDate eventDate,
            HttpSession session,
            Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        try {
            Optional<Events> existingEvent = repo.searchEvents(venueId, eventName, eventDate);
            if (existingEvent.isPresent()) {
                List<Artists> artists = repo.findArtistsByName("");
                List<Venues> venues = repo.findAllVenues();
                model.addAttribute("artists", artists);
                model.addAttribute("venues", venues);
                model.addAttribute("error", "A show with this name already exists at this venue on this date");
                return "member/add-show";
            }

            repo.addEvents(venueId, eventName, eventDate);
            return "redirect:/member/shows";
        } catch (Exception e) {
            List<Artists> artists = repo.findArtistsByName("");
            List<Venues> venues = repo.findAllVenues();
            model.addAttribute("artists", artists);
            model.addAttribute("venues", venues);
            model.addAttribute("error", "An error occurred while adding the show: " + e.getMessage());
            return "member/add-show";
        }
    }

    @GetMapping("/artists/{id}/edit")
    public String editArtist(
        @PathVariable Long id,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        Artists artist = repo.findArtistById(id);
        if (artist == null) {
            return "redirect:/member/artists";
        }

        model.addAttribute("artist", artist);
        return "member/edit-artist";
    }

    @PostMapping("/artists/{id}/edit")
    public String updateArtist(
        @PathVariable Long id,
        @RequestParam("artistName") String artistName,
        @RequestParam(value = "image", required = false) MultipartFile image,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            String imageFilename = null;
            String originalFilename = null;

            if (image != null && !image.isEmpty()) {
                imageFilename = fileStorageService.storeFile(image);
                originalFilename = image.getOriginalFilename();
            }

            repo.updateArtist(id, artistName, imageFilename, originalFilename);
            return "redirect:/member/artists";
        } catch (Exception e) {
            Artists artist = repo.findArtistById(id);
            model.addAttribute("artist", artist);
            model.addAttribute("error", "An error occurred while updating the artist: " + e.getMessage());
            return "member/edit-artist";
        }
    }

    @GetMapping("/artists/{id}/shows")
    public String getArtistShows(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int size,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        Artists artist = repo.findArtistById(id);
        if (artist == null) {
            return "redirect:/member/artists";
        }

        // Get events for this artist
        List<Events> events = repo.findEventsByArtist(id);

        // Get setlists for this artist
        List<Setlist> setlists = repo.findSetlists(artist.getArtistName(), "", page, size);
        long totalSetlists = repo.getTotalSetlists(artist.getArtistName(), "");
        int totalPages = (int) Math.ceil((double) totalSetlists / size);

        model.addAttribute("artist", artist);
        model.addAttribute("events", events);
        model.addAttribute("setlists", setlists);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
        return "member/artist-shows";
    }

    @PostMapping("/artists/{id}/delete")
    public String deleteArtist(
        @PathVariable Long id,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }

        try {
            // Get artist name before deletion for the message
            Artists artist = repo.findArtistById(id);
            if (artist == null) {
                redirectAttributes.addFlashAttribute("error", "Artist not found");
                return "redirect:/member/artists";
            }

            String artistName = artist.getArtistName();
            repo.deleteArtist(id);
            redirectAttributes.addFlashAttribute("success", "Artist '" + artistName + "' has been successfully deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete artist: " + e.getMessage());
        }

        return "redirect:/member/artists";
    }

    @PostMapping("/shows/{id}/delete") 
    public String deleteShow(
        @PathVariable Long id,
        HttpSession session
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }

        try {
            this.repo.deleteEvent(id);
        } catch (Exception e) {
            // Handle error if needed
        }

        return "redirect:/member/shows";
    }

    @GetMapping("/shows/{id}/edit")
    public String editShows(
        @PathVariable Long id,
        HttpSession session,
        Model model
    ) {
        if(!checkMemberAccess(session)) {
            return "redirect/auth/login";
        }
        addUserAttributes(session, model);
        EventsVenues event = this.repo.findEventsById(id);
        List<Venues> venues = this.repo.findAllVenues();

        if(event==null) {
            return "redirect:/member/shows";
        }

        model.addAttribute("events", event);
        model.addAttribute("venues", venues);

        return "member/edit-show";

    }

    @PostMapping("/shows/{id}/edit")
    public String updateShows(
        @PathVariable Long id,
        @RequestParam("eventName") String eventName,
        @RequestParam("eventDate") LocalDate eventDate,
        @RequestParam("venueId") Long venueId,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            repo.updateEvent(id, venueId, eventName, eventDate);
            return "redirect:/member/shows";
        } catch (Exception e) {
            EventsVenues event = repo.findEventsById(id);
            List<Venues> venues = repo.findAllVenues();
            model.addAttribute("events", event);
            model.addAttribute("venues", venues);
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "member/edit-show";
        }
    }

    @PostMapping("/artists/add")
    public String memberAddArtist(
        @RequestParam("name") String artistName,
        @RequestParam("genreNames") String genreNamesStr,
        @RequestParam(value = "image", required = false) MultipartFile image,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        List<GenreView> categories = this.repo.findAllGenre();
        model.addAttribute("genres", categories);

        try {
            // Split comma-separated genre names
            List<String> genreNames = Arrays.asList(genreNamesStr.split(","));
            if (genreNames.isEmpty()) {
                model.addAttribute("error", "Please select at least one genre");
                return "member/add-artist";
            }

            List<Artists> nameArtist = this.repo.findArtistsByName(artistName);
            Long idArtist;

            if (!nameArtist.isEmpty()) {
                // Artist exists, get their ID
                idArtist = nameArtist.get(0).getIdArtist();
            } else {
                // Create new artist
                String imageFilename = null;
                String originalFilename = null;

                if (image != null && !image.isEmpty()) {
                    imageFilename = fileStorageService.storeFile(image);
                    originalFilename = image.getOriginalFilename();
                }

                this.repo.addArtist(artistName, imageFilename, originalFilename);
                idArtist = this.repo.findArtistsByName(artistName).get(0).getIdArtist();
            }

            // Process each genre
            List<String> addedGenres = new ArrayList<>();
            List<String> existingGenres = new ArrayList<>();

            for (String genreName : genreNames) {
                Long idCategory = this.repo.findIdCategory(genreName).getId_category();
                
                // Check if this genre is already associated
                if (this.repo.findArtistsByNameAndGenre(artistName, genreName, 1, 1).isEmpty()) {
                    this.repo.addCategoriesArtist(idArtist, idCategory);
                    addedGenres.add(genreName);
                } else {
                    existingGenres.add(genreName);
                }
            }

            // Prepare success/warning message
            StringBuilder message = new StringBuilder();
            if (!addedGenres.isEmpty()) {
                message.append("Successfully added genres: ").append(String.join(", ", addedGenres));
            }
            if (!existingGenres.isEmpty()) {
                if (message.length() > 0) message.append("\n");
                message.append("Artist already associated with genres: ").append(String.join(", ", existingGenres));
            }

            if (!addedGenres.isEmpty()) {
                model.addAttribute("accept", message.toString());
            } else {
                model.addAttribute("error", message.toString());
            }
            
            return "member/add-artist";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while adding the artist: " + e.getMessage());
            return "member/add-artist";
        }
    }

    @GetMapping("/songs")
    public String songs(
        HttpSession session,
        Model model,
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "12") int size
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        // Get total songs count for pagination
        long totalSongs = repo.getTotalSongs(name);
        int totalPages = (int) Math.ceil((double) totalSongs / size);
        
        // Ensure page is within bounds
        page = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        // Get paginated songs
        List<Songs> songs = repo.findSongsByName(name, page, size);

        // Add model attributes
        model.addAttribute("songs", songs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("searchQuery", name);
        model.addAttribute("hasSearch", !name.isEmpty());

        return "member/songs";
    }

    @GetMapping("/add-song")
    public String addSong(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        // Get list of artists for dropdown
        List<Artists> artists = repo.findArtistsByName("");
        model.addAttribute("artists", artists);

        return "member/add-song";
    }

    @PostMapping("/songs/add")
    public String addSong(
        @RequestParam("name") String songName,
        @RequestParam("artistId") Long artistId,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        try {
            repo.addSong(songName, artistId);
            return "redirect:/member/songs";
        } catch (Exception e) {
            List<Artists> artists = repo.findArtistsByName("");
            model.addAttribute("artists", artists);
            model.addAttribute("error", "An error occurred while adding the song: " + e.getMessage());
            return "member/add-song";
        }
    }

    @GetMapping("/songs/{id}/edit")
    public String editSong(
        @PathVariable Long id,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        Songs song = repo.findSongById(id);
        if (song == null) {
            return "redirect:/member/songs";
        }

        // Get list of artists for dropdown
        List<Artists> artists = repo.findArtistsByName("");
        model.addAttribute("artists", artists);
        model.addAttribute("song", song);
        return "member/edit-song";
    }

    @PostMapping("/songs/{id}/edit")
    public String updateSong(
        @PathVariable Long id,
        @RequestParam("name") String songName,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            repo.updateSong(id, songName);
            return "redirect:/member/songs";
        } catch (Exception e) {
            Songs song = repo.findSongById(id);
            model.addAttribute("song", song);
            model.addAttribute("error", "An error occurred while updating the song: " + e.getMessage());
            return "member/edit-song";
        }
    }

    @PostMapping("/songs/{id}/delete")
    public String deleteSong(
        @PathVariable Long id,
        HttpSession session
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }

        try {
            repo.deleteSong(id);
        } catch (Exception e) {
            // Handle error if needed
        }

        return "redirect:/member/songs";
    }

}
