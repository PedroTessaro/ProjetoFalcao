package com.securus.cyberbullet.controller;

import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.service.DroneService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Gerenciamento da frota: lista de drones e envio de comandos remotos. */
@Controller
public class FleetController {

    private final DroneService droneService;

    public FleetController(DroneService droneService) {
        this.droneService = droneService;
    }

    @GetMapping("/fleet")
    public String fleet(Model model) {
        model.addAttribute("drones", droneService.findAll());
        model.addAttribute("stats", droneService.stats());
        model.addAttribute("active", "fleet");
        return "fleet";
    }

    @PostMapping("/fleet/{id}/command")
    public String command(@PathVariable Long id, @RequestParam String command, Principal principal) {
        droneService.sendCommand(id, command, principal.getName());
        return "redirect:/fleet";
    }
}
