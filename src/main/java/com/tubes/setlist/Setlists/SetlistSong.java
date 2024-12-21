package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class SetlistSong {
    private int idSetlist;
    private String setlistName;
    private int idSong;
    private String songName;
    private String artistName;
    
    public SetlistSong(int idSetlist, String setlistName, int idSong, String songName, String artistName) {
        this.idSetlist = idSetlist;
        this.setlistName = setlistName;
        this.idSong = idSong;
        this.songName = songName;
        this.artistName = artistName;
    }
}
