package com.tubes.setlist.member.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tubes.setlist.guest.model.ArtistView;
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
    List<ArtistView> findArtistsByNameAndGenre(String name, String genre);

    //genre
    List<GenreView> findAllGenre();
    Categories findIdCategory(String category_name);

    //venue
    List<Venues> findAllVenues();
    List<Venues> findVenuesById(Long idVenue);

    //events
    Optional<Events> searchEvents(Long venueId, String showName, LocalDate date);
}
