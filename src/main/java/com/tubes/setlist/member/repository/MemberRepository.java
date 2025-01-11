package com.tubes.setlist.member.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.EventsVenues;
import com.tubes.setlist.member.model.GenreView;
import com.tubes.setlist.member.model.Venues;
import com.tubes.setlist.member.model.Songs;
import com.tubes.setlist.member.model.Setlist;
import com.tubes.setlist.member.model.Events;

public interface MemberRepository {
  //add artis & genre
    void addArtist(String artist_name);
    void addArtist(String artist_name, String imageFilename, String imageOriginalFilename);
    void addCategories(String category_name);
    void addCategoriesArtist(Long id_artist, Long id_category);

    void addEvents(Long idVenue, String eventName, LocalDate eventDate);

    //artist
    List<Artists> findAllArtists();
    List<Artists> findArtistsByName(String name);
    List<Artists> findArtistsByNameAndGenre(String name, String genre, int page, int size);
    Map<String, Long> getGenreCounts();
    long getTotalArtists(String name, String genre);

    //genre
    List<GenreView> findAllGenre();
    Categories findIdCategory(String category_name);

    //venue
    List<Venues> findAllVenues();
    List<Venues> findVenuesById(Long idVenue);

    //events
    Optional<Events> searchEvents(Long venueId, String showName, LocalDate date);
    List<EventsVenues> searchEventsByKeyword(String keyword);
    List<EventsVenues> findAllEvents();
    EventsVenues findEventsById(Long eventId);
    void updateEvent(Long eventId, Long idVenue, String eventName, LocalDate eventDate);
    void deleteEvent(Long eventId);
    List<Categories> findAllGenre();
    Categories findIdCategory(String category_name);

    void addSong(String songName, Long artistId);
    List<Songs> findSongsByArtist(Long artistId);
    List<Songs> findSongsByName(String name, int page, int size);
    long getTotalSongs(String name);
    void updateSong(Long songId, String songName);
    void deleteSong(Long songId);
    Songs findSongById(Long songId);

    List<Setlist> findSetlists(String artist, String event, int page, int size);
    long getTotalSetlists(String artist, String event);
    Setlist findSetlistById(Long id);
    void addSetlist(String name, Long artistId, Long eventId, List<Long> songIds, String proofFilename, String proofOriginalFilename);
    void updateSetlist(Long id, String name, Long artistId, Long eventId, List<Long> songIds, String proofFilename, String proofOriginalFilename);
    void deleteSetlist(Long id);
    List<Long> getSetlistSongs(Long idSetlist);

    List<Events> findAllEvents();
    List<Events> findEventsByArtist(Long artistId);
    
    Artists findArtistById(Long id);
    void updateArtist(Long id, String artistName, String imageFilename, String imageOriginalFilename);
    void deleteArtist(Long id);
}
