package com.tubes.setlist.member.repository;

import java.util.List;
import java.util.Map;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;

public interface MemberRepository {
    void addArtist(String artist_name);
    void addCategories(String category_name);
    void addCategoriesArtist(Long id_artist, Long id_category);

    List<Artists> findArtistsByName(String name);
    List<Artists> findArtistsByNameAndGenre(String name, String genre, int page, int size);
    Map<String, Long> getGenreCounts();
    long getTotalArtists(String name, String genre);

    List<Categories> findAllGenre();
    Categories findIdCategory(String category_name);

}
