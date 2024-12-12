package com.tubes.setlist.Events;

import lombok.Data;

@Data
public class Events {
    private int idEvent;
    private int idVenue;
    private String eventName;
    private String eventDate;
    
    public Events(int idEvent, int idVenue, String eventName, String eventDate) {
        this.idEvent = idEvent;
        this.idVenue = idVenue;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }

    
}
