package com.tubes.setlist.guest.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import java.time.LocalDate;
import java.util.List;
import com.tubes.setlist.guest.model.*;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/mockup_new.sql"})
public class JdbcGuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @Test
    void findArtistsByName_ShouldReturnMatchingArtists() {
        // Given
        String searchName = "Sheila";

        // When
        List<ArtistView> artists = guestRepository.findArtistsByName(searchName);

        // Then
        assertFalse(artists.isEmpty());
        assertTrue(artists.stream()
            .anyMatch(artist -> artist.getArtistName().toLowerCase()
                .contains(searchName.toLowerCase())));
    }

    @Test
    void findArtistById_ShouldReturnCorrectArtist() {
        // Given
        Long artistId = 1L; // Sheila On 7

        // When
        ArtistView artist = guestRepository.findArtistById(artistId);

        // Then
        assertNotNull(artist);
        assertEquals(artistId, artist.getIdArtist());
        assertEquals("Sheila On 7", artist.getArtistName());
    }

    @Test
    void findEventsByArtist_ShouldReturnArtistEvents() {
        // Given
        Long artistId = 1L; // Sheila On 7 has event in Jakarta

        // When
        List<EventView> events = guestRepository.findEventsByArtist(artistId);

        // Then
        assertFalse(events.isEmpty());
        assertTrue(events.stream()
            .anyMatch(event -> event.getEventName().contains("Konser")));
    }

    @Test
    void findEventsByDateAndLocation_ShouldReturnMatchingEvents() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        String location = "Jakarta";

        // When
        List<EventView> events = guestRepository.findEventsByDateAndLocation(
            startDate, endDate, location);

        // Then
        assertFalse(events.isEmpty());
        events.forEach(event -> {
            assertTrue(event.getEventDate().isAfter(startDate.minusDays(1)));
            assertTrue(event.getEventDate().isBefore(endDate.plusDays(1)));
            assertTrue(event.getCityName().toLowerCase()
                .contains(location.toLowerCase()));
        });
    }

    @Test
    void findEventById_ShouldReturnCorrectEvent() {
        // Given
        Long eventId = 1L; // Konser Musik Indonesia

        // When
        EventView event = guestRepository.findEventById(eventId);

        // Then
        assertNotNull(event);
        assertEquals(eventId, event.getIdEvent());
        assertEquals("Konser Musik Indonesia", event.getEventName());
    }

    @Test
    void findSetlistsByEvent_ShouldReturnEventSetlists() {
        // Given
        Long eventId = 1L; // Konser Musik Indonesia

        // When
        List<SetlistView> setlists = guestRepository.findSetlistsByEvent(eventId);

        // Then
        assertFalse(setlists.isEmpty());
        assertTrue(setlists.stream()
            .anyMatch(setlist -> setlist.getSetlistName().contains("Sheila On 7")));
    }

    @Test
    void findSetlistById_ShouldReturnCorrectSetlist() {
        // Given
        Long setlistId = 1L; // Konser Sheila On 7 Jakarta

        // When
        SetlistView setlist = guestRepository.findSetlistById(setlistId);

        // Then
        assertNotNull(setlist);
        assertEquals(setlistId, setlist.getIdSetlist());
        assertTrue(setlist.getSetlistName().contains("Sheila On 7"));
        assertFalse(setlist.getSongs().isEmpty());
    }
}
