package com.tubes.setlist.member.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.GenreView;
import com.tubes.setlist.member.model.Venues;

public interface MemberRepository {

    //add artis & genre
    void addArtist(String artist_name);
    void addCategories(String category_name);
    void addCategoriesArtist(Long id_artist, Long id_category);

    void addEvents(Long idVenue, String eventName, LocalDate eventDate);

    //artist
    List<Artists> findAllArtists();
    List<Artists> findArtistsByName(String name);
    List<Artists> findArtistsByNameAndGenre(String name, String genre, int page, int size);
    Map<String, Long> getGenreCounts();
    long getTotalArtists(String name, String genre);

    List<Categories> findAllGenre();
    Categories findIdCategory(String category_name);

    //venue
    List<Venues> findAllVenues();
    List<Venues> findVenuesById(Long idVenue);

    //events
    Optional<Events> searchEvents(Long venueId, String showName, LocalDate date);
}
