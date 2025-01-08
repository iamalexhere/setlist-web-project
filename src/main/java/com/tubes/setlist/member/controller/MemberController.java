package com.tubes.setlist.member.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    MemberRepository repo;
    
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
    public String setlists(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "member/setlists";
    }
    
    @GetMapping("/add-setlist")
    public String addSetlist(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "member/add-setlist";
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
        @RequestParam("description") String description,
        @RequestParam("image") MultipartFile image,
        Model model
    ) {
        List<Artists> nameArtist = this.repo.findArtistsByName(artistName);
        Long idCategory = this.repo.findIdCategory(genreName).getId_category();
        if(!nameArtist.isEmpty()) {
            Long idArtist = nameArtist.get(0).getIdArtist();
            if(!this.repo.findArtistsByNameAndGenre(artistName, genreName, 1, 1).isEmpty()) {
                model.addAttribute("error", "The artist " + "'" + artistName + "'" + " is already associated with the genre " + "'" + genreName + "'" + ".");
                return "member/add-artist";
            }
            else {
                this.repo.addCategoriesArtist(idArtist, idCategory);
            }
        }
        else {
            this.repo.addArtist(artistName);
            Long idArtist = this.repo.findArtistsByName(artistName).get(0).getIdArtist();
            this.repo.addCategoriesArtist(idArtist, idCategory);
        }
        model.addAttribute("accept", "The artist " + "'" + artistName + "'" + " has been successfully associated with the genre " + "'"+ genreName + "'" + ".");
        return "member/add-artist";
    }
}
