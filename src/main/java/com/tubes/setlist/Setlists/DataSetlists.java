package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataSetlists {
    private int idSetlist;
    private String setlistName;
    private int idArtist;
    private String artistName;
    private int idEvent;
    private String eventName;
    private String venueName;
    private String cityName;
    
    public DataSetlists(int idSetlist, String setlistName, int idArtist, String artistName, int idEvent, String eventName, String venueName, String cityName) {
        this.idSetlist = idSetlist;
        this.setlistName = setlistName;
        this.idArtist = idArtist;
        this.artistName = artistName;
        this.idEvent = idEvent;
        this.eventName = eventName;
        this.venueName = venueName;
        this.cityName = cityName;
    }
}
