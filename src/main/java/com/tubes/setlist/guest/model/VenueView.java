package com.tubes.setlist.guest.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VenueView {
    private Long idVenue;
    private String venueName;
    private String cityName;
    private List<EventView> upcomingEvents;
    private List<EventView> pastEvents;

    public VenueView(long idVenue, String venueName, String cityName) {
        this.idVenue = idVenue;
        this.venueName = venueName;
        this.cityName = cityName;
        this.upcomingEvents = new ArrayList<>();
        this.pastEvents = new ArrayList<>();
    }

    // Getters
    public Long getIdVenue() {
        return idVenue;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getCityName() {
        return cityName;
    }

    public List<EventView> getUpcomingEvents() {
        return upcomingEvents;
    }

    public List<EventView> getPastEvents() {
        return pastEvents;
    }

    // Setters
    public void setUpcomingEvents(List<EventView> upcomingEvents) {
        this.upcomingEvents = upcomingEvents != null ? upcomingEvents : new ArrayList<>();
    }

    public void setPastEvents(List<EventView> pastEvents) {
        this.pastEvents = pastEvents != null ? pastEvents : new ArrayList<>();
    }
}