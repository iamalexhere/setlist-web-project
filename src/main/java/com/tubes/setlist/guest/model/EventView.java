package com.tubes.setlist.guest.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventView {
    private Long idEvent;
    private String eventName;
    private LocalDate eventDate;
    private String venueName;
    private String cityName;
    private boolean isDeleted;
}
