package com.tubes.setlist.Admin;

import java.util.List;

import com.tubes.setlist.Admin.Model.DateNum;
import com.tubes.setlist.Admin.Model.StringNum;

public interface AdminRepository {
    public Integer getTotalEdit();
    public List<StringNum> getUserMostEdit();
    public List<StringNum> getArtistMostPopular();
    public List<DateNum> getMonthlySetlistInsert();
}