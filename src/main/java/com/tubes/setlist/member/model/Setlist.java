package com.tubes.setlist.member.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Setlist {
    private Long idSetlist;
    private Long idArtist;
    private Long idEvent;
    private String setlistName;
    private String artistName;
    private String eventName;
    private LocalDate eventDate;
    private String proofFilename;
    private String proofOriginalFilename;
    private String proofUrl;
    private LocalDateTime proofUploadTime;
    private LocalDateTime createdAt;
    private List<String> songs;
    private boolean isDeleted;
}
