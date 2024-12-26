package com.tubes.setlist.Setlists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSetlistsRepository implements SetlistsRepository{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<DataSetlists> showAllSetlists() {
        String sql = 
                "SELECT id_setlist, setlist_name, artists.id_artist, artists.artist_name, events.id_event, events.event_name, venue_name, city_name " + 
                "FROM setlists " +
                "INNER JOIN artists ON setlists.id_artist = artists.id_artist " +
                "INNER JOIN events ON setlists.id_event = events.id_event " +
                "INNER JOIN venues ON events.id_venue = venues.id_venue;";
        List<DataSetlists> setlists = jdbcTemplate.query(sql, this::mapRowToSetlists);
        return setlists;
    }

    private DataSetlists mapRowToSetlists(ResultSet resultSet, int rowNum) throws SQLException {
        return new DataSetlists (
            resultSet.getInt("id_setlist"),
            resultSet.getString("setlist_name"),
            resultSet.getInt("id_artist"),
            resultSet.getString("artist_name"),
            resultSet.getInt("id_event"),
            resultSet.getString("event_name"),
            resultSet.getString("venue_name"),
            resultSet.getString("city_name")
        );
    }

    @Override
    public List<DataSetlistEdit> showSetlistEdits(int idSetlist){
        String sql = 
                "SELECT setlists.id_setlist, setlist_name, edits.date_added, users.id_user, users.username " +
                "FROM setlists " +
                "INNER JOIN edits ON setlists.id_setlist = edits.id_setlist " +
                "INNER JOIN users ON edits.id_user = users.id_user ";
        if(idSetlist > 0){
            sql = sql + "WHERE setlists.id_setlist = " + idSetlist + " ";
        }
        sql = sql + "ORDER BY edits.date_added DESC;";
        List<DataSetlistEdit> edits = jdbcTemplate.query(sql, this::mapRowToEdits);
        return edits;
    }

    private DataSetlistEdit mapRowToEdits(ResultSet resultSet, int rowNum) throws SQLException {
        return new DataSetlistEdit (
            resultSet.getInt("id_setlist"),
            resultSet.getString("setlist_name"),
            resultSet.getString("date_added"),
            resultSet.getInt("id_user"),
            resultSet.getString("username")
        );
    }

    @Override
    public List<DataSetlistSong> showSetlistSongs(int idSetlist){
        String sql = 
                "SELECT setlists.id_setlist, setlist_name, songs.id_song, song_name, artist_name " +
                "FROM setlists_songs " +
                "INNER JOIN setlists ON setlists_songs.id_setlist = setlists.id_setlist " +
                "INNER JOIN songs ON setlists_songs.id_song = songs.id_song " +
                "INNER JOIN artists ON songs.id_artist = artists.id_artist";
        if(idSetlist > 0){
            sql = sql + " WHERE setlists.id_setlist = " + idSetlist + ";";
        }
        else{
            sql = sql + " ;";
        }
        List<DataSetlistSong> songs = jdbcTemplate.query(sql, this::mapRowToSongs);
        return songs;
    }

    private DataSetlistSong mapRowToSongs(ResultSet resultSet, int rowNum) throws SQLException {
        return new DataSetlistSong(
            resultSet.getInt("id_setlist"),
            resultSet.getString("setlist_name"),
            resultSet.getInt("id_song"),
            resultSet.getString("song_name"),
            resultSet.getString("artist_name")
        );
    }

    @Override
    public List<DataArtists> showAllArtists() {
        String sql = 
                "SELECT id_artist, artist_name " + 
                "FROM artists;";
        List<DataArtists> artists = jdbcTemplate.query(sql, this::mapRowToArtists);
        return artists;
    }

    private DataArtists mapRowToArtists(ResultSet resultSet, int rowNum) throws SQLException {
        return new DataArtists (
            resultSet.getInt("id_artist"),
            resultSet.getString("artist_name")
        );
    }

    @Override
    public List<DataEvents> showAllEvents() {
        String sql = 
                "SELECT id_event, event_name " + 
                "FROM events;";
        List<DataEvents> events = jdbcTemplate.query(sql, this::mapRowToEvents);
        return events;
    }

    private DataEvents mapRowToEvents(ResultSet resultSet, int rowNum) throws SQLException {
        return new DataEvents (
            resultSet.getInt("id_event"),
            resultSet.getString("event_name")
        );
    }

    @Override
    public boolean insertSetlist(DataInsertSetlist data) throws Exception{
        if (!data.getUsername().equals("")){
            String sql ="WITH inserted_setlist AS ( " +
                        "INSERT INTO setlists (id_artist, id_event, setlist_name) VALUES (?, ?, ?) "+
                        "RETURNING id_setlist ) " +
                        "INSERT INTO edits (id_setlist, date_added, id_user) " +
                        "SELECT id_setlist, '2024-12-26', (SELECT id_user FROM users WHERE username = ?) "+
                        "FROM inserted_setlist;";
            jdbcTemplate.update(sql, data.getIdArtist(), data.getIdEvent(), data.getSetlistName(), data.getUsername());
            return true;
        }
        return false;
    }

}
