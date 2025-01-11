package com.tubes.setlist.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class Comment {
    private Long idComment;
    private Long idSetlist;
    private Long idUser;
    private String username;
    private String commentText;
    private LocalDateTime commentDate;
}