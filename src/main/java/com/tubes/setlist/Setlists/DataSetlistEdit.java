package com.tubes.setlist.Setlists;

import lombok.Data;

@Data
public class DataSetlistEdit {
    private int idSetlist;
    private String setlistName;
    private String editDate;
    private int idUser;
    private String username;
    
    public DataSetlistEdit(int idSetlist, String setlistName, String editDate, int idUser, String username) {
        this.idSetlist = idSetlist;
        this.setlistName = setlistName;
        this.editDate = editDate;
        this.idUser = idUser;
        this.username = username;
    }
}
