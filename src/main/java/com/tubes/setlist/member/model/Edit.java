package com.tubes.setlist.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@AllArgsConstructor
@Data
public class Edit {
    private Long idSetlist;
    private LocalDate dateAdded;
    private Long idUser;
    private String editDescription;
    private String status; // pending, approved, rejected
}