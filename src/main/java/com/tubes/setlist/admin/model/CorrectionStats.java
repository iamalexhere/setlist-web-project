package com.tubes.setlist.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorrectionStats {
    private Long totalCorrections;
    private Long pendingCorrections;
    private Long approvedToday;
}
