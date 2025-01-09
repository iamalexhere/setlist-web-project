package com.tubes.setlist.member.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Events {
    private Long idEvent;
    private Long idVenue;
    private String eventName;
    private LocalDate eventDate;
    private boolean isDeleted;
}