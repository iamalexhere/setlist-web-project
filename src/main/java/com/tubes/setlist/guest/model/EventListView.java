package com.tubes.setlist.guest.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EventListView {
    private Long idEvent;
    private String eventName;
    private LocalDate eventDate;
    private String venueName;
    private String cityName;
    private LocalDateTime createdAt;
    private List<String> artists;
    private boolean isDeleted;

    public EventListView(Long idEvent, String eventName, LocalDate eventDate, 
                        String venueName, String cityName, LocalDateTime createdAt,
                        List<String> artists, boolean isDeleted) {
        this.idEvent = idEvent;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.venueName = venueName;
        this.cityName = cityName;
        this.createdAt = createdAt;
        this.artists = artists;
        this.isDeleted = isDeleted;
    }

    // Getters
    public Long getIdEvent() { return idEvent; }
    public String getEventName() { return eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public String getVenueName() { return venueName; }
    public String getCityName() { return cityName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getArtists() { return artists; }
    public boolean isDeleted() { return isDeleted; }
}
