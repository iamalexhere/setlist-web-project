package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataSetlistSong {
    private int idSetlist;
    private String setlistName;
    private int idSong;
    private String songName;
    private String artistName;
    
    public DataSetlistSong(int idSetlist, String setlistName, int idSong, String songName, String artistName) {
        this.idSetlist = idSetlist;
        this.setlistName = setlistName;
        this.idSong = idSong;
        this.songName = songName;
        this.artistName = artistName;
    }
}
