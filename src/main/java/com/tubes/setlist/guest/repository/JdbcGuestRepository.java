package com.tubes.setlist.guest.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.tubes.setlist.guest.model.*;

@Repository
public class JdbcGuestRepository implements GuestRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ArtistView> findArtistsByName(String name) {
        String sql = """
            SELECT a.id_artist, a.artist_name, c.category_name, a.is_deleted 
            FROM artists a 
            LEFT JOIN artists_categories ac ON a.id_artist = ac.id_artist
            LEFT JOIN categories c ON ac.id_category = c.id_category
            WHERE LOWER(a.artist_name) LIKE LOWER(?) AND a.is_deleted = false
        """;
        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getString("category_name"),
                rs.getBoolean("is_deleted")
            ), 
            "%" + name + "%"
        );
    }

    @Override
    public ArtistView findArtistById(Long id) {
        String sql = """
            SELECT a.id_artist, a.artist_name, c.category_name, a.is_deleted 
            FROM artists a 
            LEFT JOIN artists_categories ac ON a.id_artist = ac.id_artist
            LEFT JOIN categories c ON ac.id_category = c.id_category
            WHERE a.id_artist = ? AND a.is_deleted = false
        """;
        return jdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getString("category_name"),
                rs.getBoolean("is_deleted")
            ),
            id
        );
    }

    @Override
    public List<EventView> findEventsByArtist(Long artistId) {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name, e.is_deleted
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            JOIN setlists s ON e.id_event = s.id_event
            WHERE s.id_artist = ? AND e.is_deleted = false
            ORDER BY e.event_date DESC
        """;
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new EventView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getBoolean("is_deleted")
            ),
            artistId
        );
    }

    @Override
    public List<EventView> findEventsByDateAndLocation(LocalDate startDate, LocalDate endDate, String location) {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name, e.is_deleted
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            WHERE e.event_date BETWEEN ? AND ?
            AND LOWER(v.city_name) LIKE LOWER(?)
            AND e.is_deleted = false
            ORDER BY e.event_date DESC
        """;
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new EventView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getBoolean("is_deleted")
            ),
            startDate, endDate, "%" + location + "%"
        );
    }

    @Override
    public EventView findEventById(Long id) {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name, e.is_deleted
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            WHERE e.id_event = ? AND e.is_deleted = false
        """;
        return jdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> new EventView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getBoolean("is_deleted")
            ),
            id
        );
    }

    @Override
    public List<SetlistView> findSetlistsByEvent(Long eventId) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, a.artist_name, e.event_name, 
                   s.proof_url, s.created_at, s.is_deleted
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            WHERE s.id_event = ? AND s.is_deleted = false
        """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> {
                Long setlistId = rs.getLong("id_setlist");
                List<String> songs = findSongsBySetlistId(setlistId);
                
                return new SetlistView(
                    setlistId,
                    rs.getString("setlist_name"),
                    rs.getString("artist_name"),
                    rs.getString("event_name"),
                    rs.getString("proof_url"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    songs,
                    rs.getBoolean("is_deleted")
                );
            },
            eventId
        );
    }

    @Override
    public SetlistView findSetlistById(Long id) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, a.artist_name, e.event_name, 
                   s.proof_url, s.created_at, s.is_deleted
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            WHERE s.id_setlist = ? AND s.is_deleted = false
        """;
        
        return jdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> {
                Long setlistId = rs.getLong("id_setlist");
                List<String> songs = findSongsBySetlistId(setlistId);
                
                return new SetlistView(
                    setlistId,
                    rs.getString("setlist_name"),
                    rs.getString("artist_name"),
                    rs.getString("event_name"),
                    rs.getString("proof_url"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    songs,
                    rs.getBoolean("is_deleted")
                );
            },
            id
        );
    }

    private List<String> findSongsBySetlistId(Long setlistId) {
        String sql = """
            SELECT s.song_name
            FROM songs s
            JOIN setlists_songs ss ON s.id_song = ss.id_song
            WHERE ss.id_setlist = ?
            ORDER BY ss.id_song
        """;
        
        return jdbcTemplate.queryForList(sql, String.class, setlistId);
    }
}
