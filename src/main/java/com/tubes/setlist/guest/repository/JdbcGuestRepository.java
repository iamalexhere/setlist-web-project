package com.tubes.setlist.guest.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.guest.model.*;

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
