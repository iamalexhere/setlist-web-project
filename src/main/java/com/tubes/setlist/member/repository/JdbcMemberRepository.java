package com.tubes.setlist.member.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.guest.model.ArtistView;
import com.tubes.setlist.member.model.Artists;
import com.tubes.setlist.member.model.Categories;
import com.tubes.setlist.member.model.Events;
import com.tubes.setlist.member.model.EventsVenues;
import com.tubes.setlist.member.model.GenreView;
import com.tubes.setlist.member.model.Venues;

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
    public List<Artists> findAllArtists() {
        String sql = "SELECT id_artist, artist_name FROM artists";
        return jdbcTemplate.query(sql, this::mapRowToArtists);
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
        String sql = "SELECT * FROM events WHERE id_venue = ? AND event_name = ? AND event_date = ?";
        try {
            return jdbcTemplate.queryForObject(
                sql,
                new Object[]{venueId, showName, date},
                (resultSet, rowNum) -> Optional.of(new Events(
                    resultSet.getLong("id_event"),
                    resultSet.getLong("id_venue"),
                    resultSet.getString("event_name"),
                    resultSet.getDate("event_date").toLocalDate(),
                    resultSet.getBoolean("is_deleted")
                ))
            );
        } catch (EmptyResultDataAccessException e) {
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
        String sql = "SELECT s.id_event, v.id_venue, s.event_name, s.event_date, v.venue_name, v.city_name " +
                     "FROM events s " +
                     "JOIN venues v ON s.id_venue = v.id_venue " +
                     "WHERE LOWER(s.event_name) LIKE LOWER(?) AND s.is_deleted = 'false' " +
                     "OR LOWER(v.venue_name) LIKE LOWER(?) " +
                     "OR CAST(s.event_date AS TEXT) LIKE ? ";
    
        String searchKeyword = "%" + keyword + "%";
    
        return jdbcTemplate.query(
            sql,
            new Object[]{searchKeyword, searchKeyword, searchKeyword},
            (rs, rowNum) -> new EventsVenues(
                rs.getLong("id_event"),
                rs.getLong("id_venue"), 
                rs.getString("event_name"),
                rs.getDate("event_date").toLocalDate(),
                rs.getString("venue_name"),
                rs.getString("city_name")
            )
        );
    }    

    @Override
    public List<EventsVenues> findAllEvents() {
        String sql = """
            SELECT id_event, events.id_venue, event_name, event_date, venue_name, city_name 
            FROM events 
            INNER JOIN venues ON events.id_venue = venues.id_venue
            WHERE is_deleted = false
        """;
        return jdbcTemplate.query(sql, this::mapRowToEventsVenues);
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
        String sql = """
            UPDATE events 
            SET event_name = ?, event_date = ?, id_venue = ?
            WHERE id_event = ?
        """;
        jdbcTemplate.update(sql, eventName, eventDate, idVenue, eventId);
    }

    @Override
    public void deleteEvent(Long eventId) {
        String sql = "UPDATE events SET is_deleted = true WHERE id_event = ?";
        jdbcTemplate.update(sql, eventId);
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
    
    
    
}
