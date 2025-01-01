package com.tubes.setlist.guest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArtistView {
    private Long idArtist;
    private String artistName;
    private String categoryName;
    private boolean isDeleted;
}
