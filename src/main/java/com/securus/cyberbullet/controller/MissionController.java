package com.securus.cyberbullet.controller;

import com.securus.cyberbullet.domain.Mission;
import com.securus.cyberbullet.domain.MissionPriority;
import com.securus.cyberbullet.domain.MissionStatus;
import com.securus.cyberbullet.service.DroneService;
import com.securus.cyberbullet.service.MissionService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Planejamento e execucao de missoes. */
@Controller
public class MissionController {

    private final MissionService missionService;
    private final DroneService droneService;

    public MissionController(MissionService missionService, DroneService droneService) {
        this.missionService = missionService;
        this.droneService = droneService;
    }

    @GetMapping("/missions")
    public String missions(Model model) {
        model.addAttribute("missions", missionService.findAll());
        model.addAttribute("drones", droneService.findAll());
        model.addAttribute("priorities", MissionPriority.values());
        model.addAttribute("active", "missions");
        return "missions";
    }

    @PostMapping("/missions")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String objective,
                         @RequestParam MissionPriority priority,
                         @RequestParam(required = false) Long droneId,
                         Principal principal) {
        Mission mission = new Mission();
        mission.setName(name);
        mission.setObjective(objective);
        mission.setPriority(priority);
        if (droneId != null) {
            droneService.findById(droneId).ifPresent(mission::setDrone);
        }
        missionService.create(mission, principal.getName());
        return "redirect:/missions";
    }

    @PostMapping("/missions/{id}/start")
    public String start(@PathVariable Long id, Principal principal) {
        missionService.start(id, principal.getName());
        return "redirect:/missions";
    }

    @PostMapping("/missions/{id}/finish")
    public String finish(@PathVariable Long id, @RequestParam MissionStatus outcome, Principal principal) {
        missionService.finish(id, outcome, principal.getName());
        return "redirect:/missions";
    }
}
