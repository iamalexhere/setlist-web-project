package com.tubes.setlist.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@AllArgsConstructor
@Data
public class Artists {
    private Long idArtist;
    private String artistName;
    private List<String> categories;
    private boolean isDeleted;
}
