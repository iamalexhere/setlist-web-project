package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataArtists {
    private int idArtist;
    private String artistName;
    
    public DataArtists(int idArtist, String artistName) {
        this.idArtist = idArtist;
        this.artistName = artistName;
    }
}
