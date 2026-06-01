package com.securus.cyberbullet.controller;

import com.securus.cyberbullet.service.DroneService;
import com.securus.cyberbullet.service.MissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Dashboard em tempo real com telemetria, frota, ameacas e SO embarcado. */
@Controller
public class DashboardController {

    private final DroneService droneService;
    private final MissionService missionService;

    public DashboardController(DroneService droneService, MissionService missionService) {
        this.droneService = droneService;
        this.missionService = missionService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("fleet", droneService.stats());
        model.addAttribute("missions", missionService.stats());
        model.addAttribute("active", "dashboard");
        return "dashboard";
    }
}
