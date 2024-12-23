package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataArtistsSongs {
    private int idArtist;
    private String artistName;
    private int idSong;
    private String songName;
    
    public DataArtistsSongs(int idArtist, String artistName, int idSong, String songName) {
        this.idArtist = idArtist;
        this.artistName = artistName;
        this.idSong = idSong;
        this.songName = songName;
    }
}
