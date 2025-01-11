package com.tubes.setlist.member.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Comment;
import com.tubes.setlist.member.model.Edit;
import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.EventsVenues;
import com.tubes.setlist.member.model.GenreView;
import com.tubes.setlist.member.model.Venues;
import com.tubes.setlist.member.model.Songs;
import com.tubes.setlist.member.model.Setlist;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addArtist(String artist_name) {
        addArtist(artist_name, null, null);
    }

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
    public List<Artists> findAllArtists() {
        String sql = "SELECT id_artist, artist_name FROM artists";
        return jdbcTemplate.query(sql, this::mapRowToArtists);
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
            "%" + name + "%", genre, genre, size, offset
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
            "%" + name + "%", genre, genre
        );
    }

    @Override
    public List<GenreView> findAllGenre() {
        String sql = "SELECT category_name FROM categories";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public void addSong(String songName, Long artistId) {
        String sql = "INSERT INTO songs (song_name, id_artist) VALUES (?, ?)";
        jdbcTemplate.update(sql, songName, artistId);
    }

    @Override
    public List<Songs> findSongsByArtist(Long artistId) {
        String sql = """
            SELECT s.id_song, s.id_artist, s.song_name, a.artist_name 
            FROM songs s
            JOIN artists a ON s.id_artist = a.id_artist
            WHERE s.id_artist = ?
            ORDER BY s.song_name
        """;
        return jdbcTemplate.query(sql, this::mapRowToSongs, artistId);
    }

    @Override
    public List<Songs> findSongsByName(String name, int page, int size) {
        String sql = """
            SELECT s.id_song, s.id_artist, s.song_name, a.artist_name 
            FROM songs s
            JOIN artists a ON s.id_artist = a.id_artist
            WHERE LOWER(s.song_name) LIKE LOWER(?)
            ORDER BY s.song_name
            LIMIT ? OFFSET ?
        """;
        int offset = (page - 1) * size;
        return jdbcTemplate.query(sql, this::mapRowToSongs, "%" + name + "%", size, offset);
    }

    @Override
    public long getTotalSongs(String name) {
        String sql = """
            SELECT COUNT(*) FROM songs s
            WHERE LOWER(s.song_name) LIKE LOWER(?)
        """;
        return jdbcTemplate.queryForObject(sql, Long.class, "%" + name + "%");
    }

    @Override
    public void updateSong(Long songId, String songName) {
        String sql = "UPDATE songs SET song_name = ? WHERE id_song = ?";
        jdbcTemplate.update(sql, songName, songId);
    }

    @Override
    public void deleteSong(Long songId) {
        String sql = "DELETE FROM songs WHERE id_song = ?";
        jdbcTemplate.update(sql, songId);
    }

    @Override
    public Songs findSongById(Long songId) {
        String sql = """
            SELECT s.id_song, s.id_artist, s.song_name, a.artist_name 
            FROM songs s
            JOIN artists a ON s.id_artist = a.id_artist
            WHERE s.id_song = ?
        """;
        List<Songs> songs = jdbcTemplate.query(sql, this::mapRowToSongs, songId);
        return songs.isEmpty() ? null : songs.get(0);
    }

    @Override
    public List<Setlist> findSetlists(String artist, String event, int page, int size) {
        String sql = """
            WITH setlist_songs AS (
                SELECT s.id_setlist,
                       string_agg(sg.song_name, ', ' ORDER BY ss.id_song) as songs
                FROM setlists s
                LEFT JOIN setlists_songs ss ON s.id_setlist = ss.id_setlist
                LEFT JOIN songs sg ON ss.id_song = sg.id_song
                GROUP BY s.id_setlist
            )
            SELECT s.*, a.artist_name, e.event_name, e.event_date, ss.songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlist_songs ss ON s.id_setlist = ss.id_setlist
            WHERE LOWER(a.artist_name) LIKE LOWER(?)
            AND LOWER(e.event_name) LIKE LOWER(?)
            AND NOT s.is_deleted
            ORDER BY e.event_date DESC, s.setlist_name
            LIMIT ? OFFSET ?
        """;
        
        int offset = (page - 1) * size;
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new Setlist(
                rs.getLong("id_setlist"),
                rs.getLong("id_artist"),
                rs.getLong("id_event"),
                rs.getString("setlist_name"),
                rs.getString("artist_name"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("proof_filename"),
                rs.getString("proof_original_filename"),
                rs.getString("proof_url"),
                rs.getTimestamp("proof_upload_time") != null ? rs.getTimestamp("proof_upload_time").toLocalDateTime() : null,
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("songs") != null ? Arrays.asList(rs.getString("songs").split(", ")) : List.of(),
                rs.getBoolean("is_deleted")
            ),
            "%" + artist + "%", "%" + event + "%", size, offset
        );
    }

    @Override
    public long getTotalSetlists(String artist, String event) {
        String sql = """
            SELECT COUNT(*)
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            WHERE LOWER(a.artist_name) LIKE LOWER(?)
            AND LOWER(e.event_name) LIKE LOWER(?)
            AND NOT s.is_deleted
        """;
        
        return jdbcTemplate.queryForObject(sql, Long.class, 
            "%" + artist + "%", "%" + event + "%");
    }

    @Override
    public Setlist findSetlistById(Long id) {
        String sql = """
            WITH setlist_songs AS (
                SELECT s.id_setlist,
                       string_agg(sg.song_name, ', ' ORDER BY ss.id_song) as songs
                FROM setlists s
                LEFT JOIN setlists_songs ss ON s.id_setlist = ss.id_setlist
                LEFT JOIN songs sg ON ss.id_song = sg.id_song
                WHERE s.id_setlist = ?
                GROUP BY s.id_setlist
            )
            SELECT s.*, a.artist_name, e.event_name, e.event_date, ss.songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlist_songs ss ON s.id_setlist = ss.id_setlist
            WHERE s.id_setlist = ? AND NOT s.is_deleted
        """;
        
        try {
            return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new Setlist(
                    rs.getLong("id_setlist"),
                    rs.getLong("id_artist"),
                    rs.getLong("id_event"),
                    rs.getString("setlist_name"),
                    rs.getString("artist_name"),
                    rs.getString("event_name"),
                    rs.getDate("event_date").toLocalDate(),
                    rs.getString("proof_filename"),
                    rs.getString("proof_original_filename"),
                    rs.getString("proof_url"),
                    rs.getTimestamp("proof_upload_time") != null ? rs.getTimestamp("proof_upload_time").toLocalDateTime() : null,
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getString("songs") != null ? Arrays.asList(rs.getString("songs").split(", ")) : List.of(),
                    rs.getBoolean("is_deleted")
                ),
                id, id
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addSetlist(String name, Long artistId, Long eventId, 
                          List<Long> songIds, String proofFilename, 
                          String proofOriginalFilename) {
        // Insert setlist
        String sql = """
            INSERT INTO setlists (id_artist, id_event, setlist_name, 
                                proof_filename, proof_original_filename, proof_url)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id_setlist
        """;
        
        String proofUrl = proofFilename != null ? 
            "/images/setlists/" + proofFilename : null;
        
        Long setlistId = jdbcTemplate.queryForObject(sql, Long.class,
            artistId, eventId, name, proofFilename, proofOriginalFilename, proofUrl);
        
        // Insert setlist songs
        if (songIds != null && !songIds.isEmpty()) {
            String songsSql = "INSERT INTO setlists_songs (id_setlist, id_song) VALUES (?, ?)";
            for (Long songId : songIds) {
                jdbcTemplate.update(songsSql, setlistId, songId);
            }
        }
    }

    @Override
    public void updateSetlist(Long id, String name, Long artistId, Long eventId, 
                            List<Long> songIds, String proofFilename, 
                            String proofOriginalFilename) {
        // Update setlist
        String sql = """
            UPDATE setlists 
            SET setlist_name = ?, id_artist = ?, id_event = ?,
                proof_filename = COALESCE(?, proof_filename),
                proof_original_filename = COALESCE(?, proof_original_filename),
                proof_url = COALESCE(?, proof_url)
            WHERE id_setlist = ?
        """;
        
        String proofUrl = proofFilename != null ? 
            "/images/setlists/" + proofFilename : null;
        
        jdbcTemplate.update(sql, name, artistId, eventId,
            proofFilename, proofOriginalFilename, proofUrl, id);
        
        // Update songs
        if (songIds != null) {
            // Delete existing songs
            jdbcTemplate.update("DELETE FROM setlists_songs WHERE id_setlist = ?", id);
            
            // Insert new songs
            if (!songIds.isEmpty()) {
                String songsSql = "INSERT INTO setlists_songs (id_setlist, id_song) VALUES (?, ?)";
                for (Long songId : songIds) {
                    jdbcTemplate.update(songsSql, id, songId);
                }
            }
        }
    }

    @Override
    public void deleteSetlist(Long id) {
        String sql = "UPDATE setlists SET is_deleted = true WHERE id_setlist = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Long> getSetlistSongs(Long idSetlist) {
        String sql = "SELECT id_song FROM setlists_songs WHERE id_setlist = ?";
        return jdbcTemplate.queryForList(sql, Long.class, idSetlist);
    }

    @Override
    public List<Venues> findAllVenues() {
        String sql = "SELECT * FROM venues";
        return jdbcTemplate.query(sql, this::mapRowToVenues);
    }

    @Override
    public List<Venues> findVenuesById(Long idVenue) {
        String sql = "SELECT * FROM venues where id_venue = ?";
        return jdbcTemplate.query(sql, this::mapRowToVenues, idVenue);
    }

    @Override
    public Optional<Events> searchEvents(Long venueId, String showName, LocalDate date) {
        String sql = "SELECT * FROM events WHERE id_venue = ? AND event_name = ? AND event_date = ? AND NOT is_deleted";
        try {
            List<Events> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Events(
                    rs.getLong("id_event"),
                    rs.getLong("id_venue"),
                    rs.getString("event_name"),
                    rs.getDate("event_date").toLocalDate(),
                    rs.getBoolean("is_deleted")
                ),
                venueId, showName, date
            );
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void addEvents(Long idVenue, String eventName, LocalDate eventDate) {
        String sql = "INSERT INTO events (id_venue, event_name, event_date) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, idVenue, eventName, eventDate);
    }

    @Override
    public List<EventsVenues> searchEventsByKeyword(String keyword) {
        String sql = """
            SELECT s.id_event, v.id_venue, s.event_name, s.event_date, v.venue_name, v.city_name 
            FROM events s 
            JOIN venues v ON s.id_venue = v.id_venue
            WHERE (LOWER(s.event_name) LIKE LOWER(?) AND NOT s.is_deleted)
            OR LOWER(v.venue_name) LIKE LOWER(?) 
            OR CAST(s.event_date AS TEXT) LIKE ?
        """;

        String searchKeyword = "%" + keyword + "%";

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new EventsVenues(
                rs.getLong("id_event"),
                rs.getLong("id_venue"), 
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name")
            ),
            searchKeyword, searchKeyword, searchKeyword
        );
    }

    @Override
    public List<EventsVenues> findAllEvents() {
        return findFilteredEvents("", null, null);
    }

    @Override
    public List<EventsVenues> findFilteredEvents(String query, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT e.id_event, e.id_venue, e.event_name, e.event_date, ");
        sql.append("v.venue_name, v.city_name, ");
        sql.append("a.artist_name, ");
        sql.append("(SELECT COUNT(*) FROM setlists_songs ss ");
        sql.append("JOIN setlists s ON ss.id_setlist = s.id_setlist ");
        sql.append("WHERE s.id_event = e.id_event) as song_count ");
        sql.append("FROM events e ");
        sql.append("INNER JOIN venues v ON e.id_venue = v.id_venue ");
        sql.append("LEFT JOIN setlists s ON e.id_event = s.id_event ");
        sql.append("LEFT JOIN artists a ON s.id_artist = a.id_artist ");
        sql.append("WHERE e.is_deleted = false ");

        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(e.event_name) LIKE LOWER(?) OR LOWER(v.venue_name) LIKE LOWER(?)) ");
            String searchPattern = "%" + query.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (startDate != null) {
            sql.append("AND e.event_date >= ? ");
            params.add(startDate);
        }

        if (endDate != null) {
            sql.append("AND e.event_date <= ? ");
            params.add(endDate);
        }

        sql.append("ORDER BY e.event_date DESC");

        return jdbcTemplate.query(
            sql.toString(),
            params.toArray(),
            (rs, rowNum) -> new EventsVenues(
                rs.getLong("id_event"),
                rs.getLong("id_venue"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getString("artist_name"),
                rs.getInt("song_count")
            )
        );
    }


    @Override
    public EventsVenues findEventsById(Long eventId) {
        String sql = """
            SELECT e.id_event, v.id_venue, e.event_name, e.event_date, v.venue_name, v.city_name 
            FROM events e 
            INNER JOIN venues v ON e.id_venue = v.id_venue
            WHERE e.id_event = ? AND is_deleted = false
        """;
        List<EventsVenues> event = jdbcTemplate.query(sql, this::mapRowToEventsVenues, eventId);
        return event.isEmpty() ? null : event.get(0);
    }

    @Override
    public void updateEvent(Long eventId, Long idVenue, String eventName, LocalDate eventDate) {
        String sql = "UPDATE events SET event_name = ?, event_date = ?, id_venue = ? WHERE id_event = ?";
        jdbcTemplate.update(sql, eventName, eventDate, idVenue, eventId);
    }

    @Override
    public void deleteEvent(Long eventId) {
        String sql = "UPDATE events SET is_deleted = true WHERE id_event = ?";
        jdbcTemplate.update(sql, eventId);
    }


    private GenreView mapRowToGenre(ResultSet resultset, int rowNum) throws SQLException {
        return new GenreView(
            resultset.getString("category_name")
        );
    }

    @Override
    public void deleteArtist(Long id) {
        try {
            // Start with child tables first
            
            // 1. Delete from setlists_songs for any setlists of this artist
            String setlistSongsSql = """
                DELETE FROM setlists_songs 
                WHERE id_setlist IN (SELECT id_setlist FROM setlists WHERE id_artist = ?)
            """;
            jdbcTemplate.update(setlistSongsSql, id);

            // 2. Delete from setlists
            String setlistSql = "DELETE FROM setlists WHERE id_artist = ?";
            jdbcTemplate.update(setlistSql, id);

            // 3. Delete from songs
            String songSql = "DELETE FROM songs WHERE id_artist = ?";
            jdbcTemplate.update(songSql, id);

            // 4. Delete from artists_categories
            String categorySql = "DELETE FROM artists_categories WHERE id_artist = ?";
            jdbcTemplate.update(categorySql, id);

            // 5. Finally delete the artist
            String artistSql = "DELETE FROM artists WHERE id_artist = ?";
            jdbcTemplate.update(artistSql, id);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete artist: " + e.getMessage());
        }
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

    private Songs mapRowToSongs(ResultSet resultSet, int rowNum) throws SQLException {
        return new Songs(
            resultSet.getLong("id_song"),
            resultSet.getLong("id_artist"),
            resultSet.getString("song_name"),
            resultSet.getString("artist_name")
        );
    }

    private Venues mapRowToVenues(ResultSet resultSet, int rowNum) throws SQLException {
        return new Venues(
            resultSet.getLong("id_venue"),
            resultSet.getString("venue_name"),
            resultSet.getString("city_name")
        );
    }

    private EventsVenues mapRowToEventsVenues(ResultSet resultSet, int rowNum) throws SQLException {
        return new EventsVenues(
            resultSet.getLong("id_event"),
            resultSet.getLong("id_venue"),
            resultSet.getString("event_name"), 
            resultSet.getDate("event_date").toLocalDate(),
            resultSet.getString("venue_name"),
            resultSet.getString("city_name")
        );
    }
    
    @Override
    public Artists findArtistById(Long id) {
        String sql = """
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
            WHERE a.id_artist = ?
            GROUP BY a.id_artist, a.artist_name, a.image_filename, a.image_original_filename, a.image_url, a.is_deleted
        """;
        
        List<Artists> artists = jdbcTemplate.query(sql, this::mapRowToArtists, id);
        return artists.isEmpty() ? null : artists.get(0);
    }

    @Override
    public void updateArtist(Long id, String artistName, String imageFilename, String imageOriginalFilename) {
        String sql = """
            UPDATE artists 
            SET artist_name = ?,
                image_filename = COALESCE(?, image_filename),
                image_original_filename = COALESCE(?, image_original_filename),
                image_url = COALESCE(?, image_url)
            WHERE id_artist = ?
        """;
        
        String imageUrl = imageFilename != null ? "/images/artists/" + imageFilename : null;
        jdbcTemplate.update(sql, artistName, imageFilename, imageOriginalFilename, imageUrl, id);
    }

    @Override
    public List<Events> findEventsByArtist(Long artistId) {
        String sql = """
            SELECT DISTINCT e.* 
            FROM events e
            JOIN setlists s ON e.id_event = s.id_event
            WHERE s.id_artist = ?
            AND NOT e.is_deleted
            ORDER BY e.event_date DESC
        """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Events(
            rs.getLong("id_event"),
            rs.getLong("id_venue"),
            rs.getString("event_name"),
            rs.getDate("event_date").toLocalDate(),
            rs.getBoolean("is_deleted")
        ), artistId);
    }

    @Override
    public List<Comment> findCommentsBySetlistId(Long idSetlist) {
        String sql = "SELECT * FROM comments WHERE id_setlist = ? ORDER BY comment_date DESC";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Comment(
                        rs.getLong("id_comment"),
                        rs.getLong("id_setlist"),
                        rs.getLong("id_user"),
                        rs.getString("comment_text"),
                        rs.getTimestamp("comment_date").toLocalDateTime()
                ),
                idSetlist
        );
    }

    @Override
    public Comment saveComment(Comment comment) {
        String sql = """
            INSERT INTO comments (id_setlist, id_user, comment_text, comment_date) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP) 
            RETURNING id_comment, comment_date
        """;
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> {
                    comment.setIdComment(rs.getLong("id_comment"));
                    comment.setCommentDate(rs.getTimestamp("comment_date").toLocalDateTime());
                    return comment;
                },
                comment.getIdSetlist(),
                comment.getIdUser(),
                comment.getCommentText()
        );
    }

    @Override
    public void deleteComment(Long idComment) {
        String sql = "DELETE FROM comments WHERE id_comment = ?";
        jdbcTemplate.update(sql, idComment);
    }

    @Override
    public List<Edit> findEditsBySetlistId(Long idSetlist) {
        String sql = "SELECT * FROM edits WHERE id_setlist = ? ORDER BY date_added DESC";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Edit(
                        rs.getLong("id_setlist"),
                        rs.getDate("date_added").toLocalDate(),
                        rs.getLong("id_user"),
                        rs.getString("edit_description"),
                        rs.getString("status")
                ),
                idSetlist
        );
    }

    @Override
    public Edit saveEdit(Edit edit) {
        String sql = """
            INSERT INTO edits (id_setlist, id_user, edit_description, date_added, status) 
            VALUES (?, ?, ?, CURRENT_DATE, 'pending')
            RETURNING date_added
        """;
        LocalDate dateAdded = jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> rs.getDate("date_added").toLocalDate(),
                edit.getIdSetlist(),
                edit.getIdUser(),
                edit.getEditDescription()
        );
        edit.setDateAdded(dateAdded);
        edit.setStatus("pending");
        return edit;
    }

    @Override
    public void updateEditStatus(Long idSetlist, LocalDate dateAdded, String status) {
        String sql = "UPDATE edits SET status = ? WHERE id_setlist = ? AND date_added = ?";
        jdbcTemplate.update(sql, status, idSetlist, dateAdded);
    }

}
