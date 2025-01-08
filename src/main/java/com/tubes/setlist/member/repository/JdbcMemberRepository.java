package com.tubes.setlist.member.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.GenreView;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addArtist(String artist_name) {
        String sql = "INSERT INTO artists (artist_name) VALUES (?)";
        jdbcTemplate.update(sql, artist_name);
    }
    
    @Override
    public void addCategories(String category_name) {
        String sql = "INSERT INTO categories (category_name) VALUES (?)";
        jdbcTemplate.update(sql, category_name);        
    }

    @Override
    public void addCategoriesArtist(Long id_artist, Long id_category) {
        String sql = "INSERT INTO artists_categories (id_artist, id_category) VALUES (?, ?)";
        jdbcTemplate.update(sql, id_artist, id_category);        
    }

    @Override
    public Categories findIdCategory(String category_name) {
        String sql = "SELECT * FROM categories WHERE category_name = ?";
        List<Categories> categories = jdbcTemplate.query(sql, this::mapRowToCategories, category_name);
        return categories.get(0);
    }

    @Override
    public List<Artists> findArtistsByName(String name) {
        String sql = "SELECT * FROM artists where artist_name = ?";
        List<Artists> artist = jdbcTemplate.query(sql, this::mapRowToArtists, name);
        return artist;
    }

    @Override
    public List<ArtistView> findArtistsByNameAndGenre(String name, String genre) {
        String sql = """
            WITH artist_categories AS (
                SELECT a.id_artist, 
                       a.artist_name,
                       a.is_deleted,
                       string_agg(c.category_name, ', ') as categories
                FROM artists a
                LEFT JOIN artists_categories ac ON a.id_artist = ac.id_artist
                LEFT JOIN categories c ON ac.id_category = c.id_category
                WHERE LOWER(a.artist_name) LIKE LOWER(?)
                  AND (CAST(? AS VARCHAR) IS NULL OR EXISTS (
                      SELECT 1 FROM artists_categories ac2
                      JOIN categories c2 ON ac2.id_category = c2.id_category
                      WHERE ac2.id_artist = a.id_artist
                      AND LOWER(c2.category_name) = LOWER(CAST(? AS VARCHAR))
                  ))
                  AND NOT a.is_deleted
                GROUP BY a.id_artist, a.artist_name, a.is_deleted
            )
            SELECT * FROM artist_categories
            ORDER BY artist_name
        """;
        
        return jdbcTemplate.query(
            sql, 
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                List.of(rs.getString("categories").split(", ")),
                rs.getBoolean("is_deleted")
            ),
            "%" + name + "%",
            genre,
            genre
        );
    }

    @Override
    public List<GenreView> findAllGenre() {
        String sql = "SELECT category_name FROM categories";
        List<GenreView> listGenre = jdbcTemplate.query(sql, this::mapRowToGenre);
        return listGenre;
    }

    private GenreView mapRowToGenre(ResultSet resultset, int rowNum) throws SQLException {
        return new GenreView(
            resultset.getString("category_name"));
    }

    private Categories mapRowToCategories(ResultSet resultSet, int rowNum) throws SQLException {
        return new Categories(
            resultSet.getLong("id_category"),
            resultSet.getString("category_name")
        );
    }

    private Artists mapRowToArtists(ResultSet resultSet, int rowNum) throws SQLException {
        return new Artists(
            resultSet.getLong("id_artist"), 
            resultSet.getString("artist_name")
        );
    }
    
    
    
}
