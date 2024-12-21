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
    public List<Setlists> showAllSetlists() {
        String sql = 
                "SELECT id_setlist, setlist_name, artists.id_artist, artists.artist_name, events.id_event, events.event_name, venue_name, city_name " + 
                "FROM setlists " +
                "INNER JOIN artists ON setlists.id_artist = artists.id_artist " +
                "INNER JOIN events ON setlists.id_event = events.id_event " +
                "INNER JOIN venues ON events.id_venue = venues.id_venue;";
        List<Setlists> setlists = jdbcTemplate.query(sql, this::mapRowToSetlists);
        return setlists;
    }

    private Setlists mapRowToSetlists(ResultSet resultSet, int rowNum) throws SQLException {
        return new Setlists (
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
    public List<SetlistEdit> showSetlistEdits(int idSetlist){
        String sql = 
                "SELECT setlists.id_setlist, setlist_name, edits.date_added, users.id_user, users.username " +
                "FROM setlists " +
                "INNER JOIN edits ON setlists.id_setlist = edits.id_setlist " +
                "INNER JOIN users ON edits.id_user = users.id_user ";
        if(idSetlist > 0){
            sql = sql + "WHERE setlists.id_setlist = " + idSetlist + " ";
        }
        sql = sql + "ORDER BY edits.date_added DESC;";
        List<SetlistEdit> edits = jdbcTemplate.query(sql, this::mapRowToEdits);
        return edits;
    }

    private SetlistEdit mapRowToEdits(ResultSet resultSet, int rowNum) throws SQLException {
        return new SetlistEdit (
            resultSet.getInt("id_setlist"),
            resultSet.getString("setlist_name"),
            resultSet.getString("date_added"),
            resultSet.getInt("id_user"),
            resultSet.getString("username")
        );
    }

    @Override
    public List<SetlistSong> showSetlistSongs(int idSetlist){
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
        List<SetlistSong> songs = jdbcTemplate.query(sql, this::mapRowToSongs);
        return songs;
    }

    private SetlistSong mapRowToSongs(ResultSet resultSet, int rowNum) throws SQLException {
        return new SetlistSong(
            resultSet.getInt("id_setlist"),
            resultSet.getString("setlist_name"),
            resultSet.getInt("id_song"),
            resultSet.getString("song_name"),
            resultSet.getString("artist_name")
        );
    }

}
