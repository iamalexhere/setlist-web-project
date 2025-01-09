package com.tubes.setlist.Setlists;

import java.util.List;

public interface SetlistsRepository {
    List<DataSetlists> showAllSetlists();
    DataSetlists showSetlist(int idSetlist);
    List<DataSetlistEdit> showSetlistEdits(int idSetlist);
    List<DataSetlistSong> showSetlistSongs(int idSetlist);
    List<DataArtists> showAllArtists();
    List<DataEvents> showAllEvents();
    boolean insertSetlist(DataInsertSetlist data) throws Exception;
    List<DataArtistsSongs> showUnaddedArtistsSong (int idSetlist);
}
