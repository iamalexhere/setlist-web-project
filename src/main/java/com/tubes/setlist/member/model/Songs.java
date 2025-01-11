package com.tubes.setlist.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Songs {
    private Long idSong;
    private Long idArtist;
    private String songName;
    private String artistName;
}
