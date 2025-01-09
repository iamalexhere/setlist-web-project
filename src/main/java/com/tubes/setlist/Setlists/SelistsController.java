package com.tubes.setlist.Setlists;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SelistsController {
    //temporary error message handler
    private String error = "";

    @Autowired
    private SetlistsRepository repo;

    @GetMapping ("/setlists")
    public String showAll (Model model){
        //set model attribute
        List<DataSetlists> setlists = repo.showAllSetlists();
        model.addAttribute("setlists", setlists);

        return "setlists/ShowSetlists.html";
    }

    @GetMapping("/detailSetlist/{idSetlist}")
    public String detailSetlist (@PathVariable("idSetlist") int idSetlist, Model model){
        DataSetlists detail = repo.showSetlist(idSetlist);
        model.addAttribute("detail", detail);
        
        List<DataSetlistEdit> edits = repo.showSetlistEdits(idSetlist);
        model.addAttribute("edits", edits);

        List<DataSetlistSong> songs = repo.showSetlistSongs(idSetlist);
        model.addAttribute("songs", songs);

        return "setlists/DetailSetlist.html";
    }


    @GetMapping ("/insertSetlist")
    public String insertSetlist (Model model){
        List<DataArtists> artists = repo.showAllArtists();
        model.addAttribute("artists", artists);

        List<DataEvents> events = repo.showAllEvents();
        model.addAttribute("events", events);

        if(!error.equals("")){
            model.addAttribute("error", error);
        }
        return "setlists/InsertSetlist.html";
    }

    @PostMapping ("/insertSetlist")
    public String getInsertSetlist (@ModelAttribute DataInsertSetlist data, Model model){
        try{
            boolean res = repo.insertSetlist(data);
            if (res) return "redirect:setlists";
            else{
                error = "null";
                return "redirect:insertSetlist";
            }
        }
        catch (Exception e){
            System.out.println("Exception message: " + e.getMessage());
            error = "failed";
            return "redirect:insertSetlist";
        }
    }

    @GetMapping ("/editSetlist/{idSetlist}")
    public String editSetlist (@PathVariable("idSetlist") int idSetlist, Model model){
        List<DataSetlistSong> songs = repo.showSetlistSongs(idSetlist);
        model.addAttribute("songs", songs);
        List<DataArtistsSongs> unselected = repo.showUnaddedArtistsSong(idSetlist);
        model.addAttribute("unselected", unselected);
        
        return "setlists/EditSetlist.html";
    }

    
}