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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
        ArtistView artist = new ArtistView(1L, "Coldplay", null, null, "https://picsum.photos/200/300", coldplayCategories, false);
        
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
                .andExpect(model().attributeExists("searchedArtists"))
                .andExpect(model().attributeExists("genreCounts"))
                .andExpect(model().attributeExists("selectedGenre"))
                .andExpect(model().attributeExists("searchQuery"))
                .andExpect(model().attributeExists("hasSearch"));
    }

    @Test
    void listArtists_WithSearchCriteria_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<String> coldplayCategories = Arrays.asList("Pop", "Rock");
        ArtistView artist = new ArtistView(1L, "Coldplay", null, null, "https://picsum.photos/200/300", coldplayCategories, false);
        
        when(guestRepository.findArtistsByNameAndGenre(anyString(), anyString()))
            .thenReturn(Arrays.asList(artist));
        
        when(guestRepository.getGenreCounts())
            .thenReturn(Map.of(
                "Pop", 5,
                "Rock", 3
            ));

        // When & Then
        mockMvc.perform(get("/guest/artists")
                .param("name", "Coldplay")
                .param("genre", "Rock"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/artists"))
                .andExpect(model().attributeExists("searchedArtists"))
                .andExpect(model().attribute("searchQuery", "Coldplay"))
                .andExpect(model().attribute("selectedGenre", "Rock"))
                .andExpect(model().attribute("hasSearch", true));
    }

    @Test
    void listArtists_WithEmptyResults_ShouldShowNoResults() throws Exception {
        // Given
        when(guestRepository.findArtistsByNameAndGenre(anyString(), anyString()))
            .thenReturn(List.of());
        
        when(guestRepository.getGenreCounts())
            .thenReturn(Map.of(
                "Pop", 5,
                "Rock", 3
            ));

        // When & Then
        mockMvc.perform(get("/guest/artists")
                .param("name", "NonexistentArtist"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/artists"))
                .andExpect(model().attribute("searchedArtists", List.of()))
                .andExpect(model().attribute("hasSearch", true));
    }

    @Test
    void viewArtist_WithValidId_ShouldReturnArtistDetails() throws Exception {
        // Given
        Long artistId = 1L;
        List<String> coldplayCategories = Arrays.asList("Pop", "Rock");
        ArtistView artist = new ArtistView(artistId, "Coldplay", null, null, "https://picsum.photos/200/300", coldplayCategories, false);
        
        when(guestRepository.findArtistById(artistId))
            .thenReturn(artist);

        when(guestRepository.findEventsByArtist(artistId))
            .thenReturn(List.of());

        when(guestRepository.findSetlistsByArtist(artistId))
            .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/guest/artists/{id}", artistId))
            .andExpect(status().isOk())
            .andExpect(view().name("guest/artist-detail"))
            .andExpect(model().attribute("artist", artist))
            .andExpect(model().attribute("events", List.of()))
            .andExpect(model().attribute("setlists", List.of()));
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
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        SetlistView setlist = new SetlistView(
            1L, 
            "Main Set", 
            1L,
            "Coldplay",
            1L,
            "Rock Concert 2024",
            eventDate,
            null, 
            LocalDateTime.now(), 
            Arrays.asList("Fix You", "Yellow"), 
            false
        );

        when(guestRepository.findEventById(eventId))
            .thenReturn(event);
        when(guestRepository.findSetlistsByEvent(eventId))
            .thenReturn(Arrays.asList(setlist));

        // When & Then
        mockMvc.perform(get("/guest/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/event-detail"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("setlists"));
    }

    @Test
    void viewSetlist_ShouldReturnSetlistView() throws Exception {
        // Given
        Long setlistId = 1L;
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        SetlistView setlist = new SetlistView(
            setlistId, 
            "Coldplay MSG Concert", 
            1L,
            "Coldplay",
            1L,
            "Rock Concert 2024",
            eventDate,
            "proof.jpg", 
            LocalDateTime.now(), 
            Arrays.asList("Fix You", "Yellow"), 
            false
        );

        when(guestRepository.findSetlistById(setlistId))
            .thenReturn(setlist);

        // When & Then
        mockMvc.perform(get("/guest/setlists/{id}", setlistId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/setlist-detail"))
                .andExpect(model().attributeExists("setlist"));
    }

    @Test
    public void viewSetlist_WithValidId_ShouldReturnSetlistPage() throws Exception {
        // Arrange
        Long setlistId = 1L;
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        SetlistView setlist = new SetlistView(
            setlistId,
            "Taylor Swift Eras Tour Jakarta Night 1",
            1L,
            "Taylor Swift",
            1L,
            "Eras Tour Jakarta",
            eventDate,
            "proof.jpg",
            LocalDateTime.now(),
            List.of("Love Story", "Cruel Summer", "Anti-Hero"),
            false
        );
        when(guestRepository.findSetlistById(setlistId)).thenReturn(setlist);

        // Act & Assert
        mockMvc.perform(get("/guest/setlists/" + setlistId))
            .andExpect(status().isOk())
            .andExpect(view().name("guest/setlist-detail"))
            .andExpect(model().attribute("setlist", setlist));
    }

    @Test
    public void listSetlistsByEvent_WithValidEventId_ShouldReturnSetlists() throws Exception {
        // Arrange
        Long eventId = 1L;
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        SetlistView setlist1 = new SetlistView(
            1L, 
            "Night 1",
            1L,
            "Taylor Swift",
            eventId,
            "Eras Tour Jakarta",
            eventDate,
            "proof.jpg",
            LocalDateTime.now(),
            List.of("Love Story", "Cruel Summer"),
            false
        );
        SetlistView setlist2 = new SetlistView(
            2L, 
            "Night 2",
            1L,
            "Taylor Swift",
            eventId,
            "Eras Tour Jakarta",
            eventDate.plusDays(1),
            "proof2.jpg",
            LocalDateTime.now(),
            List.of("Shake It Off", "Anti-Hero"),
            false
        );
        when(guestRepository.findSetlistsByEvent(eventId))
            .thenReturn(List.of(setlist1, setlist2));

        // Act & Assert
        mockMvc.perform(get("/guest/events/" + eventId + "/setlists"))
            .andExpect(status().isOk())
            .andExpect(view().name("guest/setlists"))
            .andExpect(model().attribute("setlistList", List.of(setlist1, setlist2)));
    }

    @Test
    public void listSetlistsByArtist_WithValidArtistId_ShouldReturnSetlists() throws Exception {
        // Arrange
        Long artistId = 1L;
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        SetlistView setlist1 = new SetlistView(
            1L, 
            "MSG Night 1",
            artistId,
            "Taylor Swift",
            1L,
            "MSG Concert",
            eventDate,
            "proof.jpg",
            LocalDateTime.now(),
            List.of("Love Story", "Cruel Summer"),
            false
        );
        SetlistView setlist2 = new SetlistView(
            2L, 
            "MSG Night 2",
            artistId,
            "Taylor Swift",
            2L,
            "MSG Concert",
            eventDate.plusDays(1),
            "proof2.jpg",
            LocalDateTime.now(),
            List.of("Shake It Off", "Anti-Hero"),
            false
        );
        when(guestRepository.findSetlistsByArtist(artistId))
            .thenReturn(List.of(setlist1, setlist2));

        // Act & Assert
        mockMvc.perform(get("/guest/artists/" + artistId + "/setlists"))
            .andExpect(status().isOk())
            .andExpect(view().name("guest/setlists"))
            .andExpect(model().attribute("setlistList", List.of(setlist1, setlist2)));
    }

    @Test
    public void exploreSetlists_WithArtistSearch_ShouldReturnSetlists() throws Exception {
        // Arrange
        Long artistId = 1L;
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        ArtistView artist = new ArtistView(artistId, "Taylor Swift", null, null, "https://picsum.photos/200/300", List.of("Pop"), false);
        SetlistView setlist = new SetlistView(
            1L,
            "MSG Night 1",
            artistId,
            "Taylor Swift",
            1L,
            "MSG Concert",
            eventDate,
            "proof.jpg",
            LocalDateTime.now(),
            List.of("Love Story", "Cruel Summer"),
            false
        );

        when(guestRepository.findArtistsByName("Taylor Swift"))
            .thenReturn(List.of(artist));
        when(guestRepository.findSetlistsByArtist(artistId))
            .thenReturn(List.of(setlist));

        // Act & Assert
        mockMvc.perform(get("/guest/setlists")
                .param("artist", "Taylor Swift"))
            .andExpect(status().isOk())
            .andExpect(view().name("guest/setlists"))
            .andExpect(model().attribute("artist", "Taylor Swift"))
            .andExpect(model().attribute("setlistList", List.of(setlist)));
    }

    @Test
    void viewEvents_WithNoParams_ShouldReturnAllEvents() throws Exception {
        // Given
        List<String> artists = Arrays.asList("Coldplay", "Metallica");
        EventView event = new EventView(1L, "Rock Concert 2024",
            LocalDate.of(2024, 8, 15), "MSG", "New York", false, artists);
        
        when(guestRepository.findAllEvents())
            .thenReturn(Arrays.asList(event));

        // When & Then
        mockMvc.perform(get("/guest/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("query", is("")))
                .andExpect(model().attribute("startDate", nullValue()))
                .andExpect(model().attribute("endDate", nullValue()))
                .andExpect(model().attribute("location", is("")));
    }

    @Test
    void viewEvents_WithNoResults_ShouldReturnEmptyList() throws Exception {
        // Given
        when(guestRepository.searchEvents(anyString(), any(), any(), anyString()))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/guest/events")
                .param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("query", is("nonexistent")))
                .andExpect(model().attribute("startDate", nullValue()))
                .andExpect(model().attribute("endDate", nullValue()))
                .andExpect(model().attribute("location", is("")));
    }

    @Test
    void viewEvents_WithQueryOnly_ShouldReturnMatchingEvents() throws Exception {
        // Given
        List<String> artists = Arrays.asList("The Beatles");
        EventView event = new EventView(1L, "Summer Music Fest",
            LocalDate.of(2024, 9, 1), "Wembley Stadium", "London", false, artists);
        
        when(guestRepository.searchEvents(anyString(), any(), any(), anyString()))
            .thenReturn(Arrays.asList(event));

        // When & Then
        mockMvc.perform(get("/guest/events")
                .param("query", "summer")
                .param("startDate", "")
                .param("endDate", "")
                .param("location", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("query", is("summer")))
                .andExpect(model().attribute("startDate", nullValue()))
                .andExpect(model().attribute("endDate", nullValue()))
                .andExpect(model().attribute("location", is("")));
    }

    @Test
    void viewEvents_WithDatesOnly_ShouldReturnEventsInDateRange() throws Exception {
        // Given
        List<String> artists = Arrays.asList("Coldplay", "Metallica");
        EventView event = new EventView(1L, "Rock Concert 2024",
            LocalDate.of(2024, 8, 15), "MSG", "New York", false, artists);
        
        when(guestRepository.searchEvents(anyString(), any(), any(), anyString()))
            .thenReturn(Arrays.asList(event));

        LocalDate startDate = LocalDate.of(2024, 8, 1);
        LocalDate endDate = LocalDate.of(2024, 8, 31);

        // When & Then
        mockMvc.perform(get("/guest/events")
                .param("query", "")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("location", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("query", is("")))
                .andExpect(model().attribute("startDate", is(startDate)))
                .andExpect(model().attribute("endDate", is(endDate)))
                .andExpect(model().attribute("location", is("")));
    }

    @Test
    void viewEvents_WithLocationOnly_ShouldReturnEventsInLocation() throws Exception {
        // Given
        List<String> artists = Arrays.asList("Coldplay", "Metallica");
        EventView event = new EventView(1L, "Rock Concert 2024",
            LocalDate.of(2024, 8, 15), "MSG", "New York", false, artists);
        
        when(guestRepository.searchEvents(anyString(), any(), any(), anyString()))
            .thenReturn(Arrays.asList(event));

        // When & Then
        mockMvc.perform(get("/guest/events")
                .param("query", "")
                .param("startDate", "")
                .param("endDate", "")
                .param("location", "New York"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("query", is("")))
                .andExpect(model().attribute("startDate", nullValue()))
                .andExpect(model().attribute("endDate", nullValue()))
                .andExpect(model().attribute("location", is("New York")));
    }

    @Test
    void viewEvent_ShouldReturnEventDetailView() throws Exception {
        // Given
        Long eventId = 1L;
        EventView event = new EventView(eventId, "Rock Concert 2024",
            LocalDate.of(2024, 8, 15), "MSG", "New York", false);
        LocalDate eventDate = LocalDate.of(2024, 8, 15);
        SetlistView setlist = new SetlistView(
            1L, 
            "Main Set", 
            1L,
            "Coldplay",
            1L,
            "Rock Concert 2024",
            eventDate,
            null, 
            LocalDateTime.now(), 
            Arrays.asList("Fix You", "Yellow"), 
            false
        );

        when(guestRepository.findEventById(eventId))
            .thenReturn(event);
        when(guestRepository.findSetlistsByEvent(eventId))
            .thenReturn(Arrays.asList(setlist));

        // When & Then
        mockMvc.perform(get("/guest/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/event-detail"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("setlists"));
    }
}
