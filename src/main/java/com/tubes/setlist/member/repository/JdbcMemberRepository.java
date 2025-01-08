package com.tubes.setlist.member.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addArtist(String artist_name, String imageFilename, String imageOriginalFilename) {
        String sql = "INSERT INTO artists (artist_name, image_filename, image_original_filename, image_url) VALUES (?, ?, ?, ?)";
        String imageUrl = imageFilename != null ? "/images/artists/" + imageFilename : "https://picsum.photos/200/300";
        jdbcTemplate.update(sql, artist_name, imageFilename, imageOriginalFilename, imageUrl);
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
        String sql = """
            WITH artist_categories AS (
                SELECT a.id_artist, 
                       a.artist_name,
                       a.image_filename,
                       a.image_original_filename,
                       a.image_url,
                       a.is_deleted,
                       string_agg(c.category_name, ', ') as categories
                FROM artists a
                LEFT JOIN artists_categories ac ON a.id_artist = ac.id_artist
                LEFT JOIN categories c ON ac.id_category = c.id_category
                WHERE LOWER(a.artist_name) LIKE LOWER(?)
                GROUP BY a.id_artist, a.artist_name, a.image_filename, a.image_original_filename, a.image_url, a.is_deleted
            )
            SELECT * FROM artist_categories
            ORDER BY artist_name
        """;
        
        return jdbcTemplate.query(sql, this::mapRowToArtists, "%" + name + "%");
    }

    @Override
    public List<Artists> findArtistsByNameAndGenre(String name, String genre, int page, int size) {
        String sql = """
            WITH artist_categories AS (
                SELECT a.id_artist, 
                    a.artist_name,
                    a.image_filename,
                    a.image_original_filename,
                    a.image_url,
                    a.is_deleted,
                    string_agg(c.category_name, ', ') as categories
                FROM artists a
                LEFT JOIN artists_categories ac ON a.id_artist = ac.id_artist
                LEFT JOIN categories c ON ac.id_category = c.id_category
                WHERE LOWER(a.artist_name) LIKE LOWER(?)
                AND (
                    COALESCE(CAST(? AS VARCHAR), '') = '' 
                    OR EXISTS (
                        SELECT 1 FROM artists_categories ac2
                        JOIN categories c2 ON ac2.id_category = c2.id_category
                        WHERE ac2.id_artist = a.id_artist
                        AND LOWER(c2.category_name) = LOWER(CAST(? AS VARCHAR))
                    )
                )
                AND NOT a.is_deleted
                GROUP BY a.id_artist, a.artist_name, a.image_filename, a.image_original_filename, a.image_url, a.is_deleted
            )
            SELECT * FROM artist_categories
            ORDER BY artist_name
            LIMIT ? OFFSET ?
        """;
        
        int offset = (page - 1) * size;
        
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new Artists(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getString("image_filename"),
                rs.getString("image_original_filename"),
                rs.getString("image_url"),
                rs.getString("categories") != null ? Arrays.asList(rs.getString("categories").split(", ")) : List.of(),
                rs.getBoolean("is_deleted")
            ),
            "%" + name + "%",
            genre,
            genre,
            size,
            offset
        );
    }

    @Override
    public Map<String, Long> getGenreCounts() {
        String sql = """
            SELECT c.category_name, COUNT(DISTINCT a.id_artist) as count
            FROM categories c
            LEFT JOIN artists_categories ac ON c.id_category = ac.id_category
            LEFT JOIN artists a ON ac.id_artist = a.id_artist AND NOT a.is_deleted
            GROUP BY c.category_name
            ORDER BY c.category_name
        """;
        
        Map<String, Long> genreCounts = new LinkedHashMap<>();
        jdbcTemplate.query(sql, (rs) -> {
            genreCounts.put(rs.getString("category_name"), rs.getLong("count"));
        });
        
        return genreCounts;
    }

    @Override
    public long getTotalArtists(String name, String genre) {
        String sql = """
            SELECT COUNT(DISTINCT a.id_artist) as total
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
        """;
        
        return jdbcTemplate.queryForObject(
            sql, 
            Long.class,
            "%" + name + "%",
            genre,
            genre
        );
    }

    @Override
    public List<Categories> findAllGenre() {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, this::mapRowToCategories);
    }

    private Categories mapRowToCategories(ResultSet resultSet, int rowNum) throws SQLException {
        return new Categories(
            resultSet.getLong("id_category"),
            resultSet.getString("category_name")
        );
    }

    private Artists mapRowToArtists(ResultSet resultSet, int rowNum) throws SQLException {
        String categoriesStr = resultSet.getString("categories");
        List<String> categoriesList = categoriesStr != null ? 
            Arrays.asList(categoriesStr.split(", ")) : 
            List.of();
            
        return new Artists(
            resultSet.getLong("id_artist"),
            resultSet.getString("artist_name"),
            resultSet.getString("image_filename"),
            resultSet.getString("image_original_filename"),
            resultSet.getString("image_url"),
            categoriesList,
            resultSet.getBoolean("is_deleted")
        );
    }
}
