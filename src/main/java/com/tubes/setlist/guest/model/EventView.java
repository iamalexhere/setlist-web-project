package com.tubes.setlist.guest.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventView {
    private Long idEvent;
    private String eventName;
    private LocalDate eventDate;
    private String venueName;
    private String cityName;
    private boolean isDeleted;
    private List<String> artists;

    // Constructor without artists for backward compatibility
    public EventView(Long idEvent, String eventName, LocalDate eventDate, 
                    String venueName, String cityName, boolean isDeleted) {
        this.idEvent = idEvent;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.venueName = venueName;
        this.cityName = cityName;
        this.isDeleted = isDeleted;
    }
}
