package com.tubes.setlist.Admin.Model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DateNum {
    private Timestamp date;
    private Long num;
}
