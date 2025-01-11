package com.tubes.setlist.member.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comments {
    private long idComment;
    private int idSetlist;
    private int idUser;
    private String commentText;
    private LocalDateTime commentDate;
}
