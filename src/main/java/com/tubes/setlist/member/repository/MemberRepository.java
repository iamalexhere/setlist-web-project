package com.tubes.setlist.member.repository;

import java.util.List;
import java.util.Optional;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.GenreView;

public interface MemberRepository {
    void addArtist(String artist_name);
    void addCategories(String category_name);
    void addCategoriesArtist(Long id_artist, Long id_category);

    List<Artists> findArtistsByName(String name);
    List<ArtistView> findArtistsByNameAndGenre(String name, String genre);

    List<GenreView> findAllGenre();
    Categories findIdCategory(String category_name);

}
