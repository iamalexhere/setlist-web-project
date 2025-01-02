package com.tubes.setlist.guest.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.guest.model.EventListView;
import com.tubes.setlist.guest.model.EventView;
import com.tubes.setlist.guest.model.SetlistView;

@Repository
public class JdbcGuestRepository implements GuestRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ArtistView> findArtistsByName(String name) {
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
            "%" + name + "%"
        );
    }

    @Override
    public ArtistView findArtistById(Long id) {
        String sql = """
            WITH artist_categories AS (
                SELECT a.id_artist, 
                       a.artist_name,
                       a.is_deleted,
                       string_agg(c.category_name, ', ') as categories
                FROM artists a
                LEFT JOIN artists_categories ac ON a.id_artist = ac.id_artist
                LEFT JOIN categories c ON ac.id_category = c.id_category
                WHERE a.id_artist = ?
                GROUP BY a.id_artist, a.artist_name, a.is_deleted
            )
            SELECT * FROM artist_categories
        """;
        
        return jdbcTemplate.queryForObject(
            sql,
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                List.of(rs.getString("categories").split(", ")),
                rs.getBoolean("is_deleted")
            ),
            id
        );
    }

    @Override
    public List<EventView> findEventsByArtist(Long artistId) {
        String sql = """
            SELECT DISTINCT e.id_event, e.event_name, e.event_date, 
                   v.venue_name, v.city_name, e.is_deleted
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            JOIN setlists s ON e.id_event = s.id_event
            WHERE s.id_artist = ?
            ORDER BY e.event_date DESC
        """;
        
        return jdbcTemplate.query(sql, new EventRowMapper(), artistId);
    }

    @Override
    public List<EventView> findEventsByDateAndLocation(LocalDate startDate, LocalDate endDate, String location) {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, 
                   v.venue_name, v.city_name, e.is_deleted
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            WHERE e.event_date BETWEEN ? AND ?
            AND LOWER(v.city_name) LIKE LOWER(?)
            ORDER BY e.event_date DESC
        """;
        
        return jdbcTemplate.query(
            sql, 
            new EventRowMapper(),
            startDate, endDate, "%" + location + "%"
        );
    }

    @Override
    public EventView findEventById(Long id) {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, 
                   v.venue_name, v.city_name, e.is_deleted
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            WHERE e.id_event = ?
        """;
        
        return jdbcTemplate.queryForObject(sql, new EventRowMapper(), id);
    }

    @Override
    public List<SetlistView> findSetlistsByEvent(Long eventId) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, a.artist_name, e.event_name,
                   s.proof_url, s.created_at, s.is_deleted,
                   string_agg(sg.song_name, ', ') as songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlists_songs ss ON s.id_setlist = ss.id_setlist
            LEFT JOIN songs sg ON ss.id_song = sg.id_song
            WHERE s.id_event = ?
            GROUP BY s.id_setlist, s.setlist_name, a.artist_name, e.event_name,
                     s.proof_url, s.created_at, s.is_deleted
            ORDER BY s.created_at DESC
        """;
        
        return jdbcTemplate.query(sql, new SetlistRowMapper(), eventId);
    }

    @Override
    public SetlistView findSetlistById(Long id) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, a.artist_name, e.event_name,
                   s.proof_url, s.created_at, s.is_deleted,
                   string_agg(sg.song_name, ', ') as songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlists_songs ss ON s.id_setlist = ss.id_setlist
            LEFT JOIN songs sg ON ss.id_song = sg.id_song
            WHERE s.id_setlist = ?
            GROUP BY s.id_setlist, s.setlist_name, a.artist_name, e.event_name,
                     s.proof_url, s.created_at, s.is_deleted
        """;
        
        return jdbcTemplate.queryForObject(sql, new SetlistRowMapper(), id);
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
    public Map<String, Integer> getGenreCounts() {
        String sql = """
            SELECT c.category_name, COUNT(DISTINCT a.id_artist) as artist_count
            FROM categories c
            LEFT JOIN artists_categories ac ON c.id_category = ac.id_category
            LEFT JOIN artists a ON ac.id_artist = a.id_artist
            GROUP BY c.category_name
            ORDER BY c.category_name
        """;
        
        return jdbcTemplate.query(sql, (rs) -> {
            Map<String, Integer> counts = new HashMap<>();
            while (rs.next()) {
                counts.put(rs.getString("category_name"), rs.getInt("artist_count"));
            }
            return counts;
        });
    }

    @Override
    public List<SetlistView> findSetlistsByArtist(Long artistId) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, a.artist_name, e.event_name,
                   s.proof_url, s.created_at, s.is_deleted,
                   string_agg(sg.song_name, ', ') as songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            LEFT JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlists_songs sl ON s.id_setlist = sl.id_setlist
            LEFT JOIN songs sg ON sl.id_song = sg.id_song
            WHERE s.id_artist = ?
            GROUP BY s.id_setlist, s.setlist_name, a.artist_name, e.event_name,
                     s.proof_url, s.created_at, s.is_deleted
            ORDER BY s.created_at DESC
        """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new SetlistView(
                rs.getLong("id_setlist"),
                rs.getString("setlist_name"),
                rs.getString("artist_name"),
                rs.getString("event_name"),
                rs.getString("proof_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                List.of(rs.getString("songs").split(", ")),
                rs.getBoolean("is_deleted")
            ),
            artistId
        );
    }

    public List<EventListView> findAllEvents() {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name,
                   e.created_at, e.is_deleted,
                   string_agg(DISTINCT a.artist_name, ', ') as performing_artists
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            LEFT JOIN setlists s ON e.id_event = s.id_event
            LEFT JOIN artists a ON s.id_artist = a.id_artist
            WHERE e.is_deleted = false
            GROUP BY e.id_event, e.event_name, e.event_date, v.venue_name, 
                     v.city_name, e.created_at, e.is_deleted
            ORDER BY e.event_date DESC
        """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new EventListView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("performing_artists") != null ? 
                    List.of(rs.getString("performing_artists").split(", ")) : 
                    List.of(),
                rs.getBoolean("is_deleted")
            )
        );
    }

    public List<EventListView> searchEvents(String query, LocalDate startDate, LocalDate endDate, String location) {
        String searchQuery = query != null ? "%" + query.toLowerCase() + "%" : "%%";
        
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name,
                   e.created_at, e.is_deleted,
                   string_agg(DISTINCT a.artist_name, ', ') as performing_artists
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            LEFT JOIN setlists s ON e.id_event = s.id_event
            LEFT JOIN artists a ON s.id_artist = a.id_artist
            WHERE e.is_deleted = false
            AND LOWER(e.event_name) LIKE ?
            AND (e.event_date >= COALESCE(?, '1900-01-01'::date))
            AND (e.event_date <= COALESCE(?, '2100-12-31'::date))
            AND (LOWER(v.city_name) = LOWER(COALESCE(?, v.city_name)))
            GROUP BY e.id_event, e.event_name, e.event_date, v.venue_name,
                     v.city_name, e.created_at, e.is_deleted
            ORDER BY e.event_date DESC
            """;

        return jdbcTemplate.query(sql, new EventListRowMapper(),
            searchQuery,
            startDate,
            endDate,
            location);
    }

    private class EventRowMapper implements RowMapper<EventView> {
        @Override
        public EventView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EventView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getBoolean("is_deleted")
            );
        }
    }

    private class EventListRowMapper implements RowMapper<EventListView> {
        @Override
        public EventListView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EventListView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("performing_artists") != null ? 
                    List.of(rs.getString("performing_artists").split(", ")) : 
                    List.of(),
                rs.getBoolean("is_deleted")
            );
        }
    }

    private class SetlistRowMapper implements RowMapper<SetlistView> {
        @Override
        public SetlistView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SetlistView(
                rs.getLong("id_setlist"),
                rs.getString("setlist_name"),
                rs.getString("artist_name"),
                rs.getString("event_name"),
                rs.getString("proof_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                List.of(rs.getString("songs").split(", ")),
                rs.getBoolean("is_deleted")
            );
        }
    }
}
