package com.tubes.setlist.Admin;

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

import com.tubes.setlist.Admin.Model.DateNum;
import com.tubes.setlist.Admin.Model.StringNum;

@Controller
public class AdminController {
    @Autowired
    AdminRepository repo;

    @GetMapping ("/laporan")
    public String showLaporan(Model model){
        Integer totalEdit = repo.getTotalEdit();
        model.addAttribute("total", (int) totalEdit);

        List<StringNum> userMostEdit = repo.getUserMostEdit();
        model.addAttribute("userEdit", userMostEdit);

        List<StringNum> artistMostRef = repo.getArtistMostPopular();
        model.addAttribute("artistPop", artistMostRef);

        List<DateNum> setlistInsert = repo.getMonthlySetlistInsert();
        model.addAttribute("setlistInsert", setlistInsert);

        return "admin/laporan.html";
    }
}
