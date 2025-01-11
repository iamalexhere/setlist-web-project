package com.tubes.setlist.member.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventsVenues {
    private Long idEvent;
    private Long idVenue;
    private String eventName;
    private LocalDate eventDate;
    private String venueName;
    private String cityName;
    private String artistName;
    private Integer songCount;
    
    // Constructor for backward compatibility
    public EventsVenues(Long idEvent, Long idVenue, String eventName, LocalDate eventDate, String venueName, String cityName) {
        this(idEvent, idVenue, eventName, eventDate, venueName, cityName, null, 0);
    }
}
