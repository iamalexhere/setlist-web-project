package com.tubes.setlist.member.repository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;

@SpringBootTest
@Sql({"/schema.sql", "/mockup_new.sql"})
@Transactional
public class JdbcMemberRepositoryTest {

    @Autowired
    private JdbcMemberRepository memberRepository;

    @Test
    public void testAddArtist() {
        // Given
        String artistName = "Test Artist";

        // When
        memberRepository.addArtist(artistName, null, null);
        List<Artists> foundArtists = memberRepository.findArtistsByName(artistName);

        // Then
        assertFalse(foundArtists.isEmpty());
        assertEquals(artistName, foundArtists.get(0).getArtistName());
        assertNotNull(foundArtists.get(0).getImageUrl());
        assertEquals("https://picsum.photos/200/300", foundArtists.get(0).getImageUrl());
    }

    @Test
    public void testAddArtistWithImage() {
        // Given
        String artistName = "Test Artist With Image";
        String imageFilename = "test-image.jpg";
        String originalFilename = "original-test-image.jpg";

        // When
        memberRepository.addArtist(artistName, imageFilename, originalFilename);
        List<Artists> foundArtists = memberRepository.findArtistsByName(artistName);

        // Then
        assertFalse(foundArtists.isEmpty());
        Artists artist = foundArtists.get(0);
        assertEquals(artistName, artist.getArtistName());
        assertEquals(imageFilename, artist.getImageFilename());
        assertEquals(originalFilename, artist.getImageOriginalFilename());
        assertEquals("/images/artists/" + imageFilename, artist.getImageUrl());
    }

    @Test
    public void testAddCategories() {
        // Given
        String categoryName = "Test Genre";

        // When
        memberRepository.addCategories(categoryName);
        Categories category = memberRepository.findIdCategory(categoryName);

        // Then
        assertNotNull(category);
        assertEquals(categoryName, category.getCategory_name());
    }

    @Test
    public void testAddCategoriesArtist() {
        // Given
        String artistName = "Test Artist";
        String categoryName = "Test Genre";
        memberRepository.addArtist(artistName, null, null);
        memberRepository.addCategories(categoryName);

        List<Artists> artists = memberRepository.findArtistsByName(artistName);
        Categories category = memberRepository.findIdCategory(categoryName);

        // When
        memberRepository.addCategoriesArtist(artists.get(0).getIdArtist(), category.getId_category());
        List<Artists> artistWithCategory = memberRepository.findArtistsByName(artistName);

        // Then
        assertFalse(artistWithCategory.isEmpty());
        assertTrue(artistWithCategory.get(0).getCategories().contains(categoryName));
    }

    @Test
    public void testFindArtistsByName() {
        // Given - using existing data from mockup_new.sql
        String artistName = "Coldplay";

        // When
        List<Artists> artists = memberRepository.findArtistsByName(artistName);

        // Then
        assertFalse(artists.isEmpty());
        assertEquals(artistName, artists.get(0).getArtistName());
        assertTrue(artists.get(0).getCategories().contains("Pop"));
        assertTrue(artists.get(0).getCategories().contains("Rock"));
        assertNotNull(artists.get(0).getImageUrl());
        assertEquals("https://picsum.photos/200/300", artists.get(0).getImageUrl());
    }

    @Test
    public void testFindArtistsByNameAndGenre() {
        // Given - using existing data from mockup_new.sql
        String artistName = "";
        String genre = "Rock";
        int page = 1;
        int size = 10;

        // When
        List<Artists> artists = memberRepository.findArtistsByNameAndGenre(artistName, genre, page, size);

        // Then
        assertFalse(artists.isEmpty());
        assertTrue(artists.stream().allMatch(artist -> 
            artist.getCategories().contains(genre)));
        assertTrue(artists.stream().allMatch(artist -> 
            artist.getImageUrl() != null && artist.getImageUrl().equals("https://picsum.photos/200/300")));
    }

    @Test
    public void testGetGenreCounts() {
        // When
        Map<String, Long> genreCounts = memberRepository.getGenreCounts();

        // Then
        assertFalse(genreCounts.isEmpty());
        assertTrue(genreCounts.containsKey("Rock"));
        assertTrue(genreCounts.containsKey("Pop"));
    }

    @Test
    public void testGetTotalArtists() {
        // Given
        String name = "";
        String genre = "Rock";

        // When
        long total = memberRepository.getTotalArtists(name, genre);

        // Then
        assertTrue(total > 0);
    }

    @Test
    public void testFindAllGenre() {
        // When
        List<Categories> genres = memberRepository.findAllGenre();

        // Then
        assertFalse(genres.isEmpty());
        assertTrue(genres.stream().anyMatch(genre -> 
            genre.getCategory_name().equals("Rock")));
        assertTrue(genres.stream().anyMatch(genre -> 
            genre.getCategory_name().equals("Pop")));
    }
}