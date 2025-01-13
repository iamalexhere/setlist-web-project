package com.tubes.setlist.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Correction {
    private Long contentId;
    private String contentType;
    private String contentName;
    private String status;
    private String submittedBy;
    private LocalDateTime submittedAt;
}
