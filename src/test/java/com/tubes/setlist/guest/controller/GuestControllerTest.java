package com.tubes.setlist.guest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    void listArtists_ShouldReturnArtistsView() throws Exception {
        // Given
        List<String> coldplayCategories = Arrays.asList("Pop", "Rock");
        ArtistView artist = new ArtistView(1L, "Coldplay", coldplayCategories, false);
        
        when(guestRepository.findArtistsByName(anyString()))
            .thenReturn(Arrays.asList(artist));
        
        when(guestRepository.findArtistsByNameAndGenre(anyString(), any()))
            .thenReturn(Arrays.asList(artist));
        
        when(guestRepository.getGenreCounts())
            .thenReturn(Map.of(
                "Pop", 5,
                "Rock", 3
            ));

        // When & Then
        mockMvc.perform(get("/guest/artists"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/artists"))
                .andExpect(model().attributeExists("featuredArtists"))
                .andExpect(model().attributeExists("searchedArtists"))
                .andExpect(model().attributeExists("genreCounts"))
                .andExpect(model().attributeExists("selectedGenre"))
                .andExpect(model().attributeExists("hasSearch"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    void viewArtist_ShouldReturnArtistView() throws Exception {
        // Given
        Long artistId = 1L;
        List<String> coldplayCategories = Arrays.asList("Pop", "Rock");
        ArtistView artist = new ArtistView(artistId, "Coldplay", coldplayCategories, false);
        EventView event = new EventView(1L, "Rock Concert 2024", 
            LocalDate.of(2024, 8, 15), "MSG", "New York", false);

        when(guestRepository.findArtistById(artistId))
            .thenReturn(artist);
        when(guestRepository.findEventsByArtist(artistId))
            .thenReturn(Arrays.asList(event));
        when(guestRepository.findSetlistsByArtist(artistId))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/guest/artists/{id}", artistId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/artist-detail"))
                .andExpect(model().attributeExists("artist"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("setlists"));
    }

    @Test
    void listEvents_ShouldReturnEventsView() throws Exception {
        // Given
        EventView event = new EventView(1L, "Rock Concert 2024", 
            LocalDate.of(2024, 8, 15), "MSG", "New York", false);
        when(guestRepository.findEventsByDateAndLocation(any(), any(), anyString()))
            .thenReturn(Arrays.asList(event));

        // When & Then
        mockMvc.perform(get("/guest/events")
                .param("startDate", "2024-07-01")
                .param("endDate", "2024-12-31")
                .param("location", "New York"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    void viewEvent_ShouldReturnEventView() throws Exception {
        // Given
        Long eventId = 1L;
        EventView event = new EventView(eventId, "Rock Concert 2024", 
            LocalDate.of(2024, 8, 15), "MSG", "New York", false);
        SetlistView setlist = new SetlistView(1L, "Coldplay MSG Concert", 
            "Coldplay", "Rock Concert 2024", "proof.jpg", 
            LocalDateTime.now(), Arrays.asList("Fix You", "Yellow"), false);

        when(guestRepository.findEventById(eventId))
            .thenReturn(event);
        when(guestRepository.findSetlistsByEvent(eventId))
            .thenReturn(Arrays.asList(setlist));

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
        SetlistView setlist = new SetlistView(setlistId, "Coldplay MSG Concert", 
            "Coldplay", "Rock Concert 2024", "proof.jpg", 
            LocalDateTime.now(), Arrays.asList("Fix You", "Yellow"), false);

        when(guestRepository.findSetlistById(setlistId))
            .thenReturn(setlist);

        // When & Then
        mockMvc.perform(get("/guest/setlists/{id}", setlistId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/setlists"))
                .andExpect(model().attributeExists("setlists"));
    }
}
