package com.tubes.setlist.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Venues {
    private Long idVenue;
    private String venueName;
    private String venueCity;
}

