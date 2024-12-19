package com.tubes.setlist.Setlists;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SelistsController {

    @Autowired
    private SetlistsRepository repo;

    private int idSetlist = 0;

    @GetMapping ("/setlists")
    public String showAll (Model model){
        // lihat semua setlist 
        List<Setlists> setlists = repo.showAllSetlists();
        model.addAttribute("setlists", setlists);

        // lihat edits dari setlist specific (param = id setlist)
        // id = 0 -> semua edit
        // id = n -> edit setlist id-n
        List<SetlistEdit> edits = repo.showSetlistEdits(this.idSetlist);
        model.addAttribute("edits", edits);

        // lihat playlist dati setlist specific (param = id setlist)
        // id = 0 -> semua playlist
        // id = n -> playlist setlist id-n
        List<SetlistSong> songs = repo.showSetlistSongs(this.idSetlist);
        model.addAttribute("songs", songs);

        return "TempSetlists.html";
    }

    @PostMapping("/setlists")
    public String searchSetlist (@RequestParam(value = "idSetlist", required = false) Integer idSetlist, Model model){
        if (idSetlist == null) this.idSetlist = 0;
        else this.idSetlist = idSetlist;

        // lihat semua setlist 
        List<Setlists> setlists = repo.showAllSetlists();
        model.addAttribute("setlists", setlists);

        // lihat edits dari setlist specific (param = id setlist)
        // id = 0 -> semua edit
        // id = n -> edit setlist id-n
        List<SetlistEdit> edits = repo.showSetlistEdits(this.idSetlist);
        model.addAttribute("edits", edits);

        // lihat playlist dati setlist specific (param = id setlist)
        // id = 0 -> semua playlist
        // id = n -> playlist setlist id-n
        List<SetlistSong> songs = repo.showSetlistSongs(this.idSetlist);
        model.addAttribute("songs", songs);

        return "TempSetlists.html";
    }

}