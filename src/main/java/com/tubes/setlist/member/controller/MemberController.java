package com.tubes.setlist.member.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Comments;
import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.Setlist;
import com.tubes.setlist.member.model.Songs;
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
        @RequestParam(defaultValue = "4") int size
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
        page = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        // Now get just the page we need
        List<Artists> paginatedArtists = repo.findArtistsByNameAndGenre(name, genre, page, size);

        // Add all necessary attributes to model
        model.addAttribute("artists", paginatedArtists);
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

        List<Categories> categories = this.repo.findAllGenre();

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
        List<Events> events = repo.findAllEvents();
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

            repo.addSetlist(setlistName, artistId, eventId, songIds, proofFilename, proofOriginalFilename);
            return "redirect:/member/setlists";
        } catch (Exception e) {
            // Repopulate dropdowns in case of error
            List<Artists> artists = repo.findArtistsByName("");
            List<Events> events = repo.findAllEvents();
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
        List<Events> events = repo.findAllEvents();
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
            List<Events> events = repo.findAllEvents();
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
    public String shows(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "member/shows";
    }
    
    @GetMapping("/add-show")
    public String addShow(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "member/add-show";
    }

    @PostMapping("/artists/add")
    public String memberAddArtist(
        @RequestParam("name") String artistName,
        @RequestParam("genreName") String genreName,
        @RequestParam(value = "image", required = false) MultipartFile image,
        HttpSession session,
        Model model
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        List<Categories> categories = this.repo.findAllGenre();
        model.addAttribute("genres", categories);

        try {
            List<Artists> nameArtist = this.repo.findArtistsByName(artistName);
            Long idCategory = this.repo.findIdCategory(genreName).getId_category();

            // Handle existing artist case
            if (!nameArtist.isEmpty()) {
                Long idArtist = nameArtist.get(0).getIdArtist();
                if (!this.repo.findArtistsByNameAndGenre(artistName, genreName, 1, 1).isEmpty()) {
                    model.addAttribute("error", "The artist '" + artistName + "' is already associated with the genre '" + genreName + "'.");
                    return "member/add-artist";
                } else {
                    this.repo.addCategoriesArtist(idArtist, idCategory);
                }
            } else {
                // Handle new artist case
                String imageFilename = null;
                String originalFilename = null;

                if (image != null && !image.isEmpty()) {
                    imageFilename = fileStorageService.storeFile(image);
                    originalFilename = image.getOriginalFilename();
                }

                this.repo.addArtist(artistName, imageFilename, originalFilename);
                Long idArtist = this.repo.findArtistsByName(artistName).get(0).getIdArtist();
                this.repo.addCategoriesArtist(idArtist, idCategory);
            }

            model.addAttribute("accept", "The artist '" + artistName + "' has been successfully associated with the genre '" + genreName + "'.");
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
        @RequestParam(defaultValue = "8") int size
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

    @GetMapping("/setlists/{setlistName}/comments")
    public String getCommentsBySetlistName(@PathVariable String setlistName, HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";  // Redirect ke login jika pengguna tidak terautentikasi
        }

        addUserAttributes(session, model);  // Menambahkan atribut pengguna ke model

        List<Comments> comments = repo.findCommentsBySetlistName(setlistName);  // Mendapatkan komentar berdasarkan nama setlist
        model.addAttribute("comments", comments);  // Menambahkan komentar ke model
        model.addAttribute("setlistName", setlistName);  // Menambahkan nama setlist ke model

        return "member/setlists";  // Mengembalikan tampilan untuk menampilkan komentar
    }


    @PostMapping("/setlists/{setlistName}/comments")
    public String addCommentToSetlist(@PathVariable String setlistName, @RequestParam("commentText") String commentText, HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";  // Redirect ke login jika pengguna tidak terautentikasi
        }

        try {
            // Menambahkan komentar baru untuk setlist
            
            repo.addComment(commentText);
            
            // Redirect kembali ke halaman komentar setlist yang sama
            return "redirect:/setlists/" + setlistName + "/comments";
        } catch (Exception e) {
            List<Comments> comments = repo.findCommentsBySetlistName(setlistName);  // Mendapatkan komentar saat ini jika terjadi error
            model.addAttribute("comments", comments);  // Menambahkan komentar ke model
            model.addAttribute("setlistName", setlistName);  // Menambahkan nama setlist ke model
            model.addAttribute("error", "An error occurred while adding the comment: " + e.getMessage());  // Menambahkan pesan error

            return "member/setlists";  // Mengembalikan tampilan dengan error
        }
    }


}
