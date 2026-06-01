package com.securus.cyberbullet.controller;

import com.securus.cyberbullet.service.AuditService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trilha de auditoria imutavel e verificacao de integridade da cadeia. */
@Controller
public class AuditController {

    private final AuditService audit;

    public AuditController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("logs", audit.latest());
        model.addAttribute("verification", audit.verifyChain());
        model.addAttribute("active", "audit");
        return "audit";
    }
}
