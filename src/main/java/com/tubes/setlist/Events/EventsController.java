package com.tubes.setlist.Events;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EventsController {

    @Autowired
    private EventsRepository repo;

    @GetMapping("/showEvents")
    public String getAllNama(Model model) {
        List<Events> events = this.repo.showAllEvents();
        model.addAttribute("results", events);
        return "ShowEvents";
    }
    
}
