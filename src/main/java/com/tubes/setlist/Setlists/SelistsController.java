package com.tubes.setlist.Setlists;

import java.util.ArrayList;
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
            if (res) {
                return "redirect:setlists";
            }
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

    // attribut, class, method yang dibutuhkan
    private List<DataSetlistSong> currSS;
    private List<DataSetlistSong> newSS;
    private List<EditedDataSetlist> tempEdits;
    private class EditedDataSetlist{
        // -1 = delete, 1 = insert
        private int type;
        private int idSong;

        private EditedDataSetlist(int type, int idSong){
            this.type = type;
            this.idSong = idSong;
        }
    }
    private void updateTempEdits(EditedDataSetlist update){
        boolean updated = false;
        for (EditedDataSetlist i : tempEdits){
            if (i.idSong == update.idSong){
                i.type += update.type;
                updated = true;
            }
        }
        if (!updated){
            tempEdits.add(update);
        }
    }
    private void cancelTempEdits(int idSetlist) throws Exception{
        for (EditedDataSetlist i : tempEdits){
            if (i.type == -1){
                repo.insertSong(idSetlist, i.idSong);
                System.out.println("reinsert");
            }
            else if(i.type == 1){
                repo.deleteSong(idSetlist, i.idSong);
                System.out.println("redelete");
            }
        }
    }

    @GetMapping ("/editSetlist/{idSetlist}")
    public String editSetlist (@PathVariable("idSetlist") int idSetlist, Model model){
        tempEdits = new ArrayList<EditedDataSetlist>();
        
        currSS = repo.showSetlistSongs(idSetlist);
        model.addAttribute("songs", currSS);
        List<DataArtistsSongs> unselected = repo.showUnaddedArtistsSong(idSetlist);
        model.addAttribute("unselected", unselected);
        model.addAttribute("id", idSetlist);
        
        return "setlists/EditSetlist.html";
    }

    @PostMapping ("/editSetlist/{idSetlist}")
    public String editedSetlist (@PathVariable("idSetlist") int idSetlist, @RequestParam int type, @RequestParam int idSong, Model model){
        try {
            if (type > 0){
                repo.insertSong(idSetlist, idSong);
                //mark as insert
                tempEdits.add(new EditedDataSetlist(1, idSong));
            }
            else{
                repo.deleteSong(idSetlist, idSong);
                //mark as delete
                tempEdits.add(new EditedDataSetlist(-1, idSong));
            }
            newSS = repo.showSetlistSongs(idSetlist);
            model.addAttribute("songs", newSS);
        }
        catch(Exception e){
            model.addAttribute("songs", currSS);
            System.out.println(e.getMessage());
        }
        List<DataArtistsSongs> unselected = repo.showUnaddedArtistsSong(idSetlist);
        model.addAttribute("unselected", unselected);
        model.addAttribute("id", idSetlist);
        
        return "setlists/EditSetlist.html";
    }

    @PostMapping("/editSetlist/saveChanges")
    public String saveEditedSetlist(@RequestParam int idSetlist, @RequestParam int save){
        if (save == 1){
            //save edit
        }
        else{
            try{
                cancelTempEdits(idSetlist);
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return "redirect:/detailSetlist/" + idSetlist;
    }

    
}

//EDIT NOTE:
// - penambahan sistem "update setlist edit" untuk insert dan edit setlist
// - penamabahn sistem edit setlist: add song & remove song