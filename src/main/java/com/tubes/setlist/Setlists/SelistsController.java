package com.tubes.setlist.Setlists;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SelistsController {

    @Autowired
    private SetlistsRepository repo;

    private int idSetlist = 0;

    @GetMapping ("/setlists")
    public String showAll (Model model){
        //set model attribute
        model = setSetlistModel(model);

        return "setlists/ShowSetlists.html";
    }

    @PostMapping("/setlists")
    public String searchSetlist (@RequestParam(value = "idSetlist", required = false) Integer idSetlist, Model model){
        if (idSetlist == null) this.idSetlist = 0;
        else this.idSetlist = idSetlist;
        //set model attribute
        model = setSetlistModel(model);

        return "setlists/ShowSetlists.html";
    }

    private Model setSetlistModel (Model model){
        // lihat semua setlist 
        List<DataSetlists> setlists = repo.showAllSetlists();
        model.addAttribute("setlists", setlists);

        // lihat edits dari setlist specific (param = id setlist)
        // id = 0 -> semua edit
        // id = n -> edit setlist id-n
        List<DataSetlistEdit> edits = repo.showSetlistEdits(this.idSetlist);
        model.addAttribute("edits", edits);

        // lihat playlist dati setlist specific (param = id setlist)
        // id = 0 -> semua playlist
        // id = n -> playlist setlist id-n
        List<DataSetlistSong> songs = repo.showSetlistSongs(this.idSetlist);
        model.addAttribute("songs", songs);

        return model;
    }

    @GetMapping ("/insertSetlist")
    public String insertSetlist (Model model){
        List<DataArtists> artists = repo.showAllArtists();
        model.addAttribute("artists", artists);

        List<DataEvents> events = repo.showAllEvents();
        model.addAttribute("events", events);
        return "setlists/InsertSetlist.html";
    }

    @PostMapping ("/insertSetlist")
    public String getInsertSetlist (@ModelAttribute DataInsertSetlist data, Model model){
        return "redirect:setlists";
    }

    @GetMapping ("/editSetlist")
    public String editSetlist (Model model){
        List<DataSetlists> setlists = repo.showAllSetlists();
        model.addAttribute("setlists", setlists);
        return "setlists/EditSetlist.html";
    }

    
}