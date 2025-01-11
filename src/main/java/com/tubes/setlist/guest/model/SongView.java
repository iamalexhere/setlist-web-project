package com.tubes.setlist.guest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongView {
    private Long idSong;
    private Long idArtist;
    private String artistName;
    private String songName;
}