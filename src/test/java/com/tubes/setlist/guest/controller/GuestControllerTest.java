package com.tubes.setlist.guest.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.tubes.setlist.guest.model.*;
import com.tubes.setlist.guest.repository.GuestRepository;

@WebMvcTest(GuestController.class)
public class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuestRepository guestRepository;

    @Test
    void searchArtists_ShouldReturnArtistsView() throws Exception {
        // Given
        ArtistView artist = new ArtistView(1L, "Coldplay", "Rock", false);
        when(guestRepository.findArtistsByName("Coldplay"))
            .thenReturn(Arrays.asList(artist));

        // When & Then
        mockMvc.perform(get("/guest/artists")
                .param("name", "Coldplay"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/artists"))
                .andExpect(model().attributeExists("artists"));
    }

    @Test
    void viewArtist_ShouldReturnArtistView() throws Exception {
        // Given
        Long artistId = 1L;
        ArtistView artist = new ArtistView(artistId, "Coldplay", "Rock", false);
        when(guestRepository.findArtistById(artistId)).thenReturn(artist);

        // When & Then
        mockMvc.perform(get("/guest/artists/{id}", artistId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/artist"))
                .andExpect(model().attributeExists("artist"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    void searchEvents_ShouldReturnEventsView() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        String location = "Jakarta";

        // When & Then
        mockMvc.perform(get("/guest/events")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("location", location))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"));
    }

    @Test
    void viewEvent_ShouldReturnEventView() throws Exception {
        // Given
        Long eventId = 1L;
        EventView event = new EventView(eventId, "Music Festival", 
            LocalDate.now(), "Stadium", "Jakarta", false);
        when(guestRepository.findEventById(eventId)).thenReturn(event);

        // When & Then
        mockMvc.perform(get("/guest/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("setlists"));
    }

    @Test
    void viewSetlist_ShouldReturnSetlistView() throws Exception {
        // Given
        Long setlistId = 1L;
        SetlistView setlist = new SetlistView(setlistId, "Concert Setlist", 
            "Coldplay", "Music Festival", "proof.jpg", null, 
            Arrays.asList("Song 1", "Song 2"), false);
        when(guestRepository.findSetlistById(setlistId)).thenReturn(setlist);

        // When & Then
        mockMvc.perform(get("/guest/setlists/{id}", setlistId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/setlists"))
                .andExpect(model().attributeExists("setlists"));
    }
}
