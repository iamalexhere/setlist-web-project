package com.tubes.setlist.guest.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import java.time.LocalDate;
import java.util.List;
import com.tubes.setlist.guest.model.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/mockup_new.sql"})
public class JdbcGuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @Test
    void findArtistsByName_ShouldReturnMatchingArtists() {
        // Given
        String searchName = "Coldplay";

        // When
        List<ArtistView> artists = guestRepository.findArtistsByName(searchName);

        // Then
        assertFalse(artists.isEmpty());
        ArtistView coldplay = artists.get(0);
        assertEquals("Coldplay", coldplay.getArtistName());
        assertTrue(coldplay.getCategories().contains("Pop"));
        assertTrue(coldplay.getCategories().contains("Rock"));
    }

    @Test
    void findArtistById_ShouldReturnCorrectArtist() {
        // Given
        Long artistId = 2L; // Coldplay

        // When
        ArtistView artist = guestRepository.findArtistById(artistId);

        // Then
        assertNotNull(artist);
        assertEquals(artistId, artist.getIdArtist());
        assertEquals("Coldplay", artist.getArtistName());
        assertTrue(artist.getCategories().contains("Pop"));
        assertTrue(artist.getCategories().contains("Rock"));
    }

    @Test
    void findEventsByArtist_ShouldReturnArtistEvents() {
        // Given
        Long artistId = 2L; // Coldplay

        // When
        List<EventView> events = guestRepository.findEventsByArtist(artistId);

        // Then
        assertFalse(events.isEmpty());
        assertTrue(events.stream()
            .anyMatch(event -> event.getEventName().equals("Rock Concert 2024")));
    }

    @Test
    void findEventsByDateAndLocation_ShouldReturnMatchingEvents() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        String location = "New York";

        // When
        List<EventView> events = guestRepository.findEventsByDateAndLocation(
            startDate, endDate, location);

        // Then
        assertFalse(events.isEmpty());
        assertTrue(events.stream()
            .anyMatch(event -> event.getEventName().equals("Rock Concert 2024")));
        events.forEach(event -> {
            assertTrue(event.getEventDate().isAfter(startDate.minusDays(1)));
            assertTrue(event.getEventDate().isBefore(endDate.plusDays(1)));
            assertEquals("New York", event.getCityName());
        });
    }

    @Test
    void findEventById_ShouldReturnCorrectEvent() {
        // Given
        Long eventId = 2L; // Rock Concert 2024

        // When
        EventView event = guestRepository.findEventById(eventId);

        // Then
        assertNotNull(event);
        assertEquals(eventId, event.getIdEvent());
        assertEquals("Rock Concert 2024", event.getEventName());
        assertEquals("New York", event.getCityName());
    }

    @Test
    void findSetlistsByEvent_ShouldReturnEventSetlists() {
        // Given
        Long eventId = 2L; // Rock Concert 2024

        // When
        List<SetlistView> setlists = guestRepository.findSetlistsByEvent(eventId);

        // Then
        assertFalse(setlists.isEmpty());
        assertTrue(setlists.stream()
            .anyMatch(setlist -> setlist.getArtistName().equals("Coldplay")));
        assertTrue(setlists.stream()
            .anyMatch(setlist -> setlist.getArtistName().equals("Metallica")));
    }

    @Test
    void findSetlistById_ShouldReturnCorrectSetlist() {
        // Given
        Long setlistId = 2L; // Coldplay MSG Concert

        // When
        SetlistView setlist = guestRepository.findSetlistById(setlistId);

        // Then
        assertNotNull(setlist);
        assertEquals(setlistId, setlist.getIdSetlist());
        assertEquals("Coldplay MSG Concert", setlist.getSetlistName());
        assertEquals("Coldplay", setlist.getArtistName());
        assertTrue(setlist.getSongs().contains("Fix You"));
        assertTrue(setlist.getSongs().contains("Yellow"));
    }

    @Test
    void findAllEvents_ShouldReturnAllNonDeletedEvents() {
        // When
        List<EventView> events = guestRepository.findAllEvents();

        // Then
        assertFalse(events.isEmpty());
        assertTrue(events.size() >= 4); // Based on mockup data
        
        // Verify event details
        events.forEach(event -> {
            assertNotNull(event.getEventName());
            assertNotNull(event.getVenueName());
            assertNotNull(event.getCityName());
            assertNotNull(event.getEventDate());
            assertFalse(event.isDeleted());
            assertNotNull(event.getArtists()); // Verify artists list is present
        });

        // Verify specific event exists
        assertTrue(events.stream()
            .anyMatch(event -> event.getEventName().equals("Rock Concert 2024")));
    }

    @Test
    void searchEvents_WithNameQuery_ShouldReturnMatchingEvents() {
        // Given
        String query = "Rock";
        
        // When
        List<EventView> events = guestRepository.searchEvents(
            query, null, null, null);

        // Then
        assertTrue(!events.isEmpty(), "Should return at least one event");
        events.forEach(event -> 
            assertTrue(event.getEventName().toLowerCase()
                .contains(query.toLowerCase()),
                "Event name should contain search query"));
    }

    @Test
    void searchEvents_WithDateRange_ShouldReturnEventsInRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 8, 31);

        // When
        List<EventView> events = guestRepository.searchEvents(
            "", startDate, endDate, null);

        // Then
        assertTrue(!events.isEmpty(), "Should return at least one event");
        events.forEach(event -> {
            assertTrue(event.getEventDate().isAfter(startDate.minusDays(1)),
                "Event date should be after start date");
            assertTrue(event.getEventDate().isBefore(endDate.plusDays(1)),
                "Event date should be before end date");
        });
    }

    @Test
    void searchEvents_WithLocation_ShouldReturnEventsInCity() {
        // Given
        String location = "New York";

        // When
        List<EventView> events = guestRepository.searchEvents(
            "", null, null, location);

        // Then
        assertTrue(!events.isEmpty(), "Should return at least one event");
        events.forEach(event -> 
            assertEquals(location, event.getCityName(),
                "Event city should match search location"));
    }

    @Test
    void searchEvents_WithAllCriteria_ShouldReturnMatchingEvents() {
        // Given
        String query = "Rock";
        LocalDate startDate = LocalDate.of(2024, 8, 1);
        LocalDate endDate = LocalDate.of(2024, 8, 31);
        String location = "New York";

        // When
        List<EventView> events = guestRepository.searchEvents(
            query, startDate, endDate, location);

        // Then
        assertTrue(!events.isEmpty(), "Should return at least one event");
        events.forEach(event -> {
            assertTrue(event.getEventName().toLowerCase()
                .contains(query.toLowerCase()),
                "Event name should contain search query");
            assertTrue(event.getEventDate().isAfter(startDate.minusDays(1)),
                "Event date should be after start date");
            assertTrue(event.getEventDate().isBefore(endDate.plusDays(1)),
                "Event date should be before end date");
            assertEquals(location, event.getCityName(),
                "Event city should match search location");
            assertNotNull(event.getArtists()); // Verify artists list is present
        });
    }

    @Test
    void searchEvents_WithNoMatches_ShouldReturnEmptyList() {
        // Given
        String query = "NonexistentEvent";

        // When
        List<EventView> events = guestRepository.searchEvents(
            query, null, null, null);

        // Then
        assertTrue(events.isEmpty());
    }

    @Test
    public void findSetlistById_WithValidId_ShouldReturnSetlist() {
        // Arrange
        Long setlistId = 1L;

        // Act
        SetlistView setlist = guestRepository.findSetlistById(setlistId);

        // Assert
        assertNotNull(setlist);
        assertEquals("Konser Sheila On 7 Jakarta", setlist.getSetlistName());
        assertEquals("Sheila On 7", setlist.getArtistName());
        assertEquals("Konser Musik Indonesia", setlist.getEventName());
        assertFalse(setlist.isDeleted());
        assertNotNull(setlist.getSongs());
    }

    @Test
    public void findSetlistsByEvent_WithValidEventId_ShouldReturnSetlists() {
        // Arrange
        Long eventId = 1L;

        // Act
        List<SetlistView> setlists = guestRepository.findSetlistsByEvent(eventId);

        // Assert
        assertFalse(setlists.isEmpty());
        assertEquals("Konser Musik Indonesia", setlists.get(0).getEventName());
        assertFalse(setlists.get(0).isDeleted());
    }

    @Test
    public void findSetlistsByArtist_WithValidArtistId_ShouldReturnSetlists() {
        // Arrange
        Long artistId = 1L;

        // Act
        List<SetlistView> setlists = guestRepository.findSetlistsByArtist(artistId);

        // Assert
        assertFalse(setlists.isEmpty());
        assertEquals("Sheila On 7", setlists.get(0).getArtistName());
        assertFalse(setlists.get(0).isDeleted());
    }

}
