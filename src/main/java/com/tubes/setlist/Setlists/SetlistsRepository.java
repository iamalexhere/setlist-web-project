package com.tubes.setlist.Setlists;

import java.util.List;

public interface SetlistsRepository {
    List<Setlists> showAllSetlists();
    List<SetlistEdit> showSetlistEdits(int idSetlist);
    List<SetlistSong> showSetlistSongs(int idSetlist);
}
