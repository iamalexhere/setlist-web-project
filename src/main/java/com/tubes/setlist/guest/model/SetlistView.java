package com.tubes.setlist.guest.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SetlistView {
    private Long idSetlist;
    private String setlistName;
    private Long idArtist;
    private String artistName;
    private Long idEvent;
    private String eventName;
    private LocalDate eventDate;
    private String proofUrl;
    private LocalDateTime createdAt;
    private List<String> songs;
    private boolean isDeleted;
}
