package com.tubes.setlist.member.repository;

import java.util.List;
import java.util.Map;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Songs;
import com.tubes.setlist.member.model.Setlist;
import com.tubes.setlist.member.model.Events;
public interface MemberRepository {
    void addArtist(String artist_name, String imageFilename, String imageOriginalFilename);
    void addCategories(String category_name);
    void addCategoriesArtist(Long id_artist, Long id_category);

    List<Artists> findArtistsByName(String name);
    List<Artists> findArtistsByNameAndGenre(String name, String genre, int page, int size);
    Map<String, Long> getGenreCounts();
    long getTotalArtists(String name, String genre);

    List<Categories> findAllGenre();
    Categories findIdCategory(String category_name);

    // Add new methods for songs
    void addSong(String songName, Long artistId);
    List<Songs> findSongsByArtist(Long artistId);
    List<Songs> findSongsByName(String name, int page, int size);
    long getTotalSongs(String name);
    void updateSong(Long songId, String songName);
    void deleteSong(Long songId);
    Songs findSongById(Long songId);

    // Setlist methods
    List<Setlist> findSetlists(String artist, String event, int page, int size);
    long getTotalSetlists(String artist, String event);
    Setlist findSetlistById(Long id);
    void addSetlist(String name, Long artistId, Long eventId, List<Long> songIds, String proofFilename, String proofOriginalFilename);
    void updateSetlist(Long id, String name, Long artistId, Long eventId, List<Long> songIds, String proofFilename, String proofOriginalFilename);
    void deleteSetlist(Long id);
    List<Long> getSetlistSongs(Long idSetlist);

    // Add new method for events
    List<Events> findAllEvents();
}
