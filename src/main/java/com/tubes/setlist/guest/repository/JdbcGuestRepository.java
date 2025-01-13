package com.tubes.setlist.guest.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.guest.model.EventView;
import com.tubes.setlist.guest.model.SetlistView;
import com.tubes.setlist.guest.model.SongView;
import com.tubes.setlist.guest.model.VenueView;

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
        
        return jdbcTemplate.query(
            sql, 
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getString("image_filename"),
                rs.getString("image_original_filename"),
                rs.getString("image_url"),
                rs.getString("categories") != null ? List.of(rs.getString("categories").split(", ")) : List.of(),
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
            )
            SELECT * FROM artist_categories
        """;
        
        List<ArtistView> artists = jdbcTemplate.query(
            sql, 
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getString("image_filename"),
                rs.getString("image_original_filename"),
                rs.getString("image_url"),
                rs.getString("categories") != null ? List.of(rs.getString("categories").split(", ")) : List.of(),
                rs.getBoolean("is_deleted")
            ),
            id
        );
        
        return artists.isEmpty() ? null : artists.get(0);
    }

    @Override
    public List<EventView> findEventsByArtist(Long artistId) {
        String sql = """
            SELECT DISTINCT e.id_event, e.event_name, e.event_date, 
                   v.venue_name, v.city_name, e.is_deleted,
                   string_agg(DISTINCT a.artist_name, ', ') as artists
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            JOIN setlists s ON e.id_event = s.id_event
            LEFT JOIN setlists s2 ON e.id_event = s2.id_event
            LEFT JOIN artists a ON s2.id_artist = a.id_artist
            WHERE s.id_artist = ?
            GROUP BY e.id_event, e.event_name, e.event_date, 
                     v.venue_name, v.city_name, e.is_deleted
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
                   v.venue_name, v.city_name, e.is_deleted,
                   string_agg(DISTINCT a.artist_name, ', ') as artists
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            LEFT JOIN setlists s ON e.id_event = s.id_event
            LEFT JOIN artists a ON s.id_artist = a.id_artist
            WHERE e.id_event = ?
            GROUP BY e.id_event, e.event_name, e.event_date,
                     v.venue_name, v.city_name, e.is_deleted
        """;
        
        return jdbcTemplate.queryForObject(sql, new EventRowMapper(), id);
    }

    @Override
    public List<SetlistView> findSetlistsByEvent(Long eventId) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, 
                   a.id_artist, a.artist_name, 
                   e.id_event, e.event_name, e.event_date,
                   s.proof_url, s.created_at, s.is_deleted,
                   string_agg(sg.song_name, ', ') as songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlists_songs sl ON s.id_setlist = sl.id_setlist
            LEFT JOIN songs sg ON sl.id_song = sg.id_song
            WHERE s.id_event = ?
            GROUP BY s.id_setlist, s.setlist_name, 
                     a.id_artist, a.artist_name, 
                     e.id_event, e.event_name, e.event_date,
                     s.proof_url, s.created_at, s.is_deleted
            ORDER BY s.created_at DESC
        """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new SetlistView(
                rs.getLong("id_setlist"),
                rs.getString("setlist_name"),
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("proof_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("songs") != null ? List.of(rs.getString("songs").split(", ")) : List.of(),
                rs.getBoolean("is_deleted")
            ),
            eventId
        );
    }

    @Override
    public SetlistView findSetlistById(Long id) {
        String sql = """
            SELECT s.id_setlist, s.setlist_name, 
                   a.id_artist, a.artist_name, 
                   e.id_event, e.event_name, e.event_date,
                   s.proof_url, s.created_at, s.is_deleted,
                   string_agg(sg.song_name, ', ') as songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            LEFT JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlists_songs sl ON s.id_setlist = sl.id_setlist
            LEFT JOIN songs sg ON sl.id_song = sg.id_song
            WHERE s.id_setlist = ?
            GROUP BY s.id_setlist, s.setlist_name, 
                     a.id_artist, a.artist_name, 
                     e.id_event, e.event_name, e.event_date,
                     s.proof_url, s.created_at, s.is_deleted
        """;
        
        return jdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> new SetlistView(
                rs.getLong("id_setlist"),
                rs.getString("setlist_name"),
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("proof_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("songs") != null ? List.of(rs.getString("songs").split(", ")) : List.of(),
                rs.getBoolean("is_deleted")
            ),
            id
        );
    }

    @Override
    public List<ArtistView> findArtistsByNameAndGenre(String name, String genre) {
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
                      ? IS NULL 
                      OR EXISTS (
                          SELECT 1 FROM artists_categories ac2
                          JOIN categories c2 ON ac2.id_category = c2.id_category
                          WHERE ac2.id_artist = a.id_artist
                          AND LOWER(c2.category_name) = LOWER(?)
                      )
                  )
                  AND NOT a.is_deleted
                GROUP BY a.id_artist, a.artist_name, a.image_filename, a.image_original_filename, a.image_url, a.is_deleted
            )
            SELECT * FROM artist_categories
            ORDER BY artist_name
        """;
        
        return jdbcTemplate.query(
            sql, 
            (rs, rowNum) -> new ArtistView(
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getString("image_filename"),
                rs.getString("image_original_filename"),
                rs.getString("image_url"),
                rs.getString("categories") != null ? List.of(rs.getString("categories").split(", ")) : List.of(),
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
            SELECT s.id_setlist, s.setlist_name, 
                   a.id_artist, a.artist_name, 
                   e.id_event, e.event_name, e.event_date,
                   s.proof_url, s.created_at, s.is_deleted,
                   string_agg(sg.song_name, ', ') as songs
            FROM setlists s
            JOIN artists a ON s.id_artist = a.id_artist
            LEFT JOIN events e ON s.id_event = e.id_event
            LEFT JOIN setlists_songs sl ON s.id_setlist = sl.id_setlist
            LEFT JOIN songs sg ON sl.id_song = sg.id_song
            WHERE s.id_artist = ?
            GROUP BY s.id_setlist, s.setlist_name, 
                     a.id_artist, a.artist_name, 
                     e.id_event, e.event_name, e.event_date,
                     s.proof_url, s.created_at, s.is_deleted
            ORDER BY s.created_at DESC
        """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new SetlistView(
                rs.getLong("id_setlist"),
                rs.getString("setlist_name"),
                rs.getLong("id_artist"),
                rs.getString("artist_name"),
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("proof_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("songs") != null ? List.of(rs.getString("songs").split(", ")) : List.of(),
                rs.getBoolean("is_deleted")
            ),
            artistId
        );
    }

    @Override
    public List<EventView> findAllEvents() {
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name,
                   e.is_deleted,
                   string_agg(DISTINCT a.artist_name, ', ') as artists
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            LEFT JOIN setlists s ON e.id_event = s.id_event
            LEFT JOIN artists a ON s.id_artist = a.id_artist
            WHERE e.is_deleted = false
            GROUP BY e.id_event, e.event_name, e.event_date, v.venue_name, 
                     v.city_name, e.is_deleted
            ORDER BY e.event_date DESC
        """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new EventView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getBoolean("is_deleted"),
                rs.getString("artists") != null ? 
                    List.of(rs.getString("artists").split(", ")) : 
                    List.of()
            )
        );
    }

    @Override
    public List<EventView> searchEvents(String query, LocalDate startDate, LocalDate endDate, String location) {
        String searchQuery = query != null ? "%" + query.toLowerCase() + "%" : "%%";
        
        String sql = """
            SELECT e.id_event, e.event_name, e.event_date, v.venue_name, v.city_name,
                   e.is_deleted,
                   string_agg(DISTINCT a.artist_name, ', ') as artists
            FROM events e
            JOIN venues v ON e.id_venue = v.id_venue
            LEFT JOIN setlists s ON e.id_event = s.id_event
            LEFT JOIN artists a ON s.id_artist = a.id_artist
            WHERE e.is_deleted = false
            AND (
                LOWER(e.event_name) LIKE ? 
                OR EXISTS (
                    SELECT 1 FROM setlists s2 
                    JOIN artists a2 ON s2.id_artist = a2.id_artist 
                    WHERE s2.id_event = e.id_event 
                    AND LOWER(a2.artist_name) LIKE ?
                )
            )
            AND (e.event_date >= COALESCE(?, '1900-01-01'::date))
            AND (e.event_date <= COALESCE(?, '2100-12-31'::date))
            AND (LOWER(v.city_name) = LOWER(COALESCE(?, v.city_name)))
            GROUP BY e.id_event, e.event_name, e.event_date, v.venue_name,
                     v.city_name, e.is_deleted
            ORDER BY e.event_date DESC
            """;

        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new EventView(
                rs.getLong("id_event"),
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name"),
                rs.getBoolean("is_deleted"),
                rs.getString("artists") != null ? 
                    List.of(rs.getString("artists").split(", ")) : 
                    List.of()
            ),
            searchQuery,
            searchQuery,
            startDate,
            endDate,
            location);
    }

    @Override
    public Map<String, ArtistView> getRandomArtistsByCategory(int page, int size) {
        String sql = """
            WITH RankedArtists AS (
                SELECT 
                    a.id_artist,
                    a.artist_name,
                    a.image_filename,
                    a.image_original_filename,
                    a.image_url,
                    a.is_deleted,
                    c.category_name,
                    ROW_NUMBER() OVER (PARTITION BY c.category_name ORDER BY RANDOM()) as rn
                FROM artists a
                JOIN artists_categories ac ON a.id_artist = ac.id_artist
                JOIN categories c ON ac.id_category = c.id_category
                WHERE NOT a.is_deleted
            )
            SELECT * FROM RankedArtists 
            WHERE rn = 1
            ORDER BY category_name
        """;
        
        return jdbcTemplate.query(sql, (rs) -> {
            Map<String, ArtistView> artistsByCategory = new HashMap<>();
            while (rs.next()) {
                ArtistView artist = new ArtistView(
                    rs.getLong("id_artist"),
                    rs.getString("artist_name"),
                    rs.getString("image_filename"),
                    rs.getString("image_original_filename"),
                    rs.getString("image_url"),
                    List.of(rs.getString("category_name")),
                    rs.getBoolean("is_deleted")
                );
                artistsByCategory.put(rs.getString("category_name"), artist);
            }
            return artistsByCategory;
        });
    }

    @Override
    public List<String> getRandomArtistImages(int limit) {
        String sql = """
            SELECT image_url 
            FROM artists 
            WHERE image_url IS NOT NULL 
            AND NOT is_deleted 
            ORDER BY RANDOM() 
            LIMIT ?
        """;
        
        return jdbcTemplate.queryForList(sql, String.class, limit);
    }

    @Override
    public List<VenueView> findAllVenues() {
        String sql = """
            SELECT v.id_venue, v.venue_name, v.city_name,
                   COUNT(DISTINCT e.id_event) as event_count
            FROM venues v
            LEFT JOIN events e ON v.id_venue = e.id_venue
            WHERE e.is_deleted = false OR e.is_deleted IS NULL
            GROUP BY v.id_venue, v.venue_name, v.city_name
            ORDER BY event_count DESC, v.city_name, v.venue_name
        """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            VenueView venue = new VenueView(
                rs.getLong("id_venue"),
                rs.getString("venue_name"),
                rs.getString("city_name")
            );
            return venue;
        });
    }

    @Override
    public List<VenueView> searchVenues(String query) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        
        String sql = """
            SELECT v.id_venue, v.venue_name, v.city_name,
                   COUNT(DISTINCT e.id_event) as event_count
            FROM venues v
            LEFT JOIN events e ON v.id_venue = e.id_venue
            WHERE (LOWER(v.venue_name) LIKE ? OR LOWER(v.city_name) LIKE ?)
            AND (e.is_deleted = false OR e.is_deleted IS NULL)
            GROUP BY v.id_venue, v.venue_name, v.city_name
            ORDER BY event_count DESC, v.city_name, v.venue_name
        """;
        
        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> new VenueView(
                rs.getLong("id_venue"),
                rs.getString("venue_name"),
                rs.getString("city_name")
            ),
            searchQuery, searchQuery
        );
    }

    @Override
    public VenueView findVenueById(Long id) {
        String sql = """
            SELECT v.id_venue, v.venue_name, v.city_name
            FROM venues v
            WHERE v.id_venue = ?
        """;
        
        VenueView venue = jdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> new VenueView(
                rs.getLong("id_venue"),
                rs.getString("venue_name"),
                rs.getString("city_name")
            ),
            id
        );

        if (venue != null) {
            // Get upcoming events
            String upcomingEventsSql = """
                SELECT e.id_event, e.event_name, e.event_date,
                       v.venue_name, v.city_name, e.is_deleted,
                       string_agg(DISTINCT a.artist_name, ', ') as artists
                FROM events e
                JOIN venues v ON e.id_venue = v.id_venue
                LEFT JOIN setlists s ON e.id_event = s.id_event
                LEFT JOIN artists a ON s.id_artist = a.id_artist
                WHERE v.id_venue = ? AND e.event_date >= CURRENT_DATE
                AND NOT e.is_deleted
                GROUP BY e.id_event, e.event_name, e.event_date,
                         v.venue_name, v.city_name, e.is_deleted
                ORDER BY e.event_date ASC
            """;
            
            List<EventView> upcomingEvents = jdbcTemplate.query(upcomingEventsSql, 
                new EventRowMapper(), id);
            venue.setUpcomingEvents(upcomingEvents);

            // Get past events
            String pastEventsSql = """
                SELECT e.id_event, e.event_name, e.event_date,
                       v.venue_name, v.city_name, e.is_deleted,
                       string_agg(DISTINCT a.artist_name, ', ') as artists
                FROM events e
                JOIN venues v ON e.id_venue = v.id_venue
                LEFT JOIN setlists s ON e.id_event = s.id_event
                LEFT JOIN artists a ON s.id_artist = a.id_artist
                WHERE v.id_venue = ? AND e.event_date < CURRENT_DATE
                AND NOT e.is_deleted
                GROUP BY e.id_event, e.event_name, e.event_date,
                         v.venue_name, v.city_name, e.is_deleted
                ORDER BY e.event_date DESC
            """;
            
            List<EventView> pastEvents = jdbcTemplate.query(pastEventsSql, 
                new EventRowMapper(), id);
            venue.setPastEvents(pastEvents);
        }
        
        return venue;
    }

    @Override
    public Map<Long, String> getRandomArtistImagesForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashMap<>();
        }

        String sql = """
            WITH event_artists AS (
                SELECT DISTINCT s.id_event, a.image_url
                FROM setlists s
                JOIN artists a ON s.id_artist = a.id_artist
                WHERE s.id_event = ANY(?)
                AND a.image_url IS NOT NULL
            )
            SELECT id_event, 
                   (array_agg(image_url ORDER BY random()))[1] as random_image
            FROM event_artists
            GROUP BY id_event
        """;

        Long[] eventIdArray = eventIds.toArray(new Long[0]);
        return jdbcTemplate.query(
            sql,
            ps -> ps.setArray(1, ps.getConnection().createArrayOf("bigint", eventIdArray)),
            (rs, rowNum) -> Map.entry(rs.getLong("id_event"), rs.getString("random_image"))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
                rs.getBoolean("is_deleted"),
                rs.getString("artists") != null ? 
                    List.of(rs.getString("artists").split(", ")) : 
                    List.of()
            );
        }
    }

    @Override
    public List<SongView> findSongsByArtist(Long idArtist) {
        String sql = """
            SELECT s.id_song, s.id_artist, a.artist_name, s.song_name
            FROM songs s
            JOIN artists a ON s.id_artist = a.id_artist
            WHERE s.id_artist = ?
            ORDER BY s.song_name
        """;
        return jdbcTemplate.query(sql, this::mapToSongView, idArtist);
    }

    @Override
    public List<SongView> searchSongs(String query) {
        String sql = """
            SELECT s.id_song, s.id_artist, a.artist_name, s.song_name
            FROM songs s
            JOIN artists a ON s.id_artist = a.id_artist
            WHERE LOWER(s.song_name) LIKE LOWER(?) OR LOWER(a.artist_name) LIKE LOWER(?)
            ORDER BY s.song_name
        """;
        String searchPattern = "%" + query + "%";
        return jdbcTemplate.query(sql, this::mapToSongView, searchPattern, searchPattern);
    }

    @Override
    public SongView findSongById(Long idSong) {
        String sql = """
            SELECT s.id_song, s.id_artist, a.artist_name, s.song_name
            FROM songs s
            JOIN artists a ON s.id_artist = a.id_artist
            WHERE s.id_song = ?
        """;
        return jdbcTemplate.queryForObject(sql, this::mapToSongView, idSong);
    }

    private SongView mapToSongView(ResultSet rs, int rowNum) throws SQLException {
        return new SongView(
            rs.getLong("id_song"),
            rs.getLong("id_artist"),
            rs.getString("artist_name"),
            rs.getString("song_name")
        );
    }
}
