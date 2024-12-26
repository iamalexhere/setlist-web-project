package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataEvents {
    private int idEvent;
    private String eventName;
    
    public DataEvents(int idEvent, String eventName) {
        this.idEvent = idEvent;
        this.eventName = eventName;
    }
}
