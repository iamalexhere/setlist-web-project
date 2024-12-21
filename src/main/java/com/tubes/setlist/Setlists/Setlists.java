package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class Setlists {
    private int idSetlist;
    private String setlistName;
    private int idArtist;
    private String artistName;
    private int idEvent;
    private String eventName;
    
    public Setlists(int idSetlist, String setlistName, int idArtist, String artistName, int idEvent, String eventName) {
        this.idSetlist = idSetlist;
        this.setlistName = setlistName;
        this.idArtist = idArtist;
        this.artistName = artistName;
        this.idEvent = idEvent;
        this.eventName = eventName;
    }
}
