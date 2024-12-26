package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataInsertSetlist {
    private String setlistName;
    private String username;
    private Integer idArtist;
    private Integer idEvent;
    
    public DataInsertSetlist(String setlistName, String username, int idArtist, int idEvent) {
        this.setlistName = setlistName;
        this.username = username;
        this.idArtist = idArtist;
        this.idEvent = idEvent;
    }
}
