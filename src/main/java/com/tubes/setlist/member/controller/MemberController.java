package com.tubes.setlist.member.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.EventsVenues;
import com.tubes.setlist.member.model.GenreView;
import com.tubes.setlist.member.model.Venues;
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
    public String artists(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "member/artists";
    }
    
    @GetMapping("/add-artist")
    public String addArtist(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        List<GenreView> genre = this.repo.findAllGenre();

        model.addAttribute("genres", genre);
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
    public String shows(HttpSession session, @RequestParam(required = false) String keyword, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        List<EventsVenues> events;

        if(keyword==null || keyword.isEmpty()) {
            events = this.repo.findAllEvents();
            model.addAttribute("events", events);
            return "member/shows";
        }

        else {
            events = this.repo.searchEventsByKeyword(keyword);
        }

        model.addAttribute("events", events);
        model.addAttribute("keyword", keyword != null ? keyword : "");

        return "member/shows";
    }
    
    @GetMapping("/add-show")
    public String addShow(HttpSession session, Model model) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        List<Artists> artists = this.repo.findAllArtists();
        List<Venues> venues = this.repo.findAllVenues();

        model.addAttribute("artist", artists);
        model.addAttribute("venues", venues);

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
            if(!this.repo.findArtistsByNameAndGenre(artistName, genreName).isEmpty()) {
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

    @PostMapping("/shows/add")
    public String memberAddShow(
        @RequestParam("showName") String showName,
        @RequestParam("artistId") String artistName,
        @RequestParam("venueId") Long venueId,
        @RequestParam("date") LocalDate date,
        @RequestParam("time") LocalTime time,
        @RequestParam("tourName") String tourName,
        @RequestParam("description") String description,
        Model model
    ) {

        String nameVenue = this.repo.findVenuesById(venueId).get(0).getVenueName();
        Optional<Events> searchEvent = this.repo.searchEvents(venueId, showName, date);

        if(!searchEvent.isPresent()) {
            this.repo.addEvents(venueId, showName, date);
            model.addAttribute("accept", "The show " + "'" + showName + "'" + " has been successfully associated with the venue " + "'"+ nameVenue + "'" + ".");
            return "member/add-show";
        }

        model.addAttribute("error", "The show " + "'" + showName + "'" + " is already associated with the venue " + "'" + nameVenue + "'" + ".");
        return "member/add-show";
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
        @RequestParam("name") String eventName,
        @RequestParam("eventDate") LocalDate eventDate,
        @RequestParam("venueId") Long venueId, 
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (!checkMemberAccess(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            this.repo.updateEvent(id, venueId, eventName, eventDate);
            String venuesName = this.repo.findVenuesById(venueId).get(0).getVenueName() + " - " + this.repo.findVenuesById(venueId).get(0).getVenueCity();
            model.addAttribute("accept", "The show " + "'" + eventName + "'" 
            + " has been successfully updated with the new date " + "'"+ eventDate + "'" 
            + " and venue " + "'" + venuesName + "'" + ".");
            return "member/shows";
        } catch (Exception e) {
            EventsVenues event = this.repo.findEventsById(id);
            model.addAttribute("events", event);
            model.addAttribute("error", "An error occurred while updating the events: " + e.getMessage());
            return "member/edit-show";
        }
    }

    @PostMapping("/shows/{id}/delete")
    public String deleteSong(
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
}
