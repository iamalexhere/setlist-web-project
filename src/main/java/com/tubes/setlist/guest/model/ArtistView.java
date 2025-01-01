package com.tubes.setlist.guest.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArtistView {
    private Long idArtist;
    private String artistName;
    private List<String> categories;
    private Boolean isDeleted;
}