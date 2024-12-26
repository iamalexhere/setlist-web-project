package com.tubes.setlist.Setlists;

import java.util.List;

public interface SetlistsRepository {
    List<DataSetlists> showAllSetlists();
    List<DataSetlistEdit> showSetlistEdits(int idSetlist);
    List<DataSetlistSong> showSetlistSongs(int idSetlist);
    List<DataArtists> showAllArtists();
    List<DataEvents> showAllEvents();
    void insertSetlist(DataInsertSetlist data) throws Exception;
}
