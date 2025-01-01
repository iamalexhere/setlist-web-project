package com.tubes.setlist.guest.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.tubes.setlist.guest.model.*;

public interface GuestRepository {
    // Artist related queries
    List<ArtistView> findArtistsByName(String name);
    List<ArtistView> findArtistsByNameAndGenre(String name, String genre);
    ArtistView findArtistById(Long id);
    
    // Event related queries
    List<EventView> findEventsByArtist(Long artistId);
    List<EventView> findEventsByDateAndLocation(LocalDate startDate, LocalDate endDate, String location);
    EventView findEventById(Long id);
    
    // Setlist related queries
    List<SetlistView> findSetlistsByArtist(Long artistId);
    List<SetlistView> findSetlistsByEvent(Long eventId);
    SetlistView findSetlistById(Long id);
    
    // Genre related queries
    Map<String, Integer> getGenreCounts();
}
