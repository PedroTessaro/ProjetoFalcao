package com.securus.cyberbullet.controller;

import com.securus.cyberbullet.domain.Operator;
import com.securus.cyberbullet.repository.OperatorRepository;
import com.securus.cyberbullet.security.MfaSuccessHandler;
import com.securus.cyberbullet.service.AuditService;
import com.securus.cyberbullet.service.TotpService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Telas de login e de segundo fator (MFA/TOTP). */
@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final OperatorRepository operatorRepository;
    private final TotpService totp;
    private final AuditService audit;

    @Value("${cyberbullet.security.demo-print-totp:false}")
    private boolean demoPrintTotp;

    public AuthController(OperatorRepository operatorRepository, TotpService totp, AuditService audit) {
        this.operatorRepository = operatorRepository;
        this.totp = totp;
        this.audit = audit;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/mfa")
    public String mfa(HttpSession session, Model model) {
        String username = (String) session.getAttribute(MfaSuccessHandler.PENDING_USER);
        if (username == null) {
            return "redirect:/login";
        }
        Operator op = operatorRepository.findByUsername(username).orElse(null);
        if (op == null || op.getTotpSecret() == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("qr", totp.qrCodeDataUri(username, op.getTotpSecret()));

        if (demoPrintTotp) {
            String code = totp.currentCodeForDemo(op.getTotpSecret());
            // DEMONSTRACAO: codigo valido impresso no log e exibido na tela.
            log.warn("[MFA-DEMO] Codigo TOTP valido agora para '{}': {} (apenas demonstracao!)", username, code);
            model.addAttribute("demoCode", code);
        }
        return "mfa";
    }

    @PostMapping("/mfa/verify")
    public String verify(@RequestParam("code") String code, HttpSession session, Model model) {
        String username = (String) session.getAttribute(MfaSuccessHandler.PENDING_USER);
        if (username == null) {
            return "redirect:/login";
        }
        Operator op = operatorRepository.findByUsername(username).orElse(null);
        if (op != null && totp.isValid(op.getTotpSecret(), code)) {
            session.setAttribute(MfaSuccessHandler.MFA_PASSED, Boolean.TRUE);
            audit.record(username, "AUTH", "MFA_OK", "Segundo fator (TOTP) validado com sucesso.");
            log.info("[AUTH] MFA validado para '{}'. Acesso liberado.", username);
            return "redirect:/dashboard";
        }
        audit.record(username, "AUTH", "MFA_FAIL", "Codigo TOTP invalido.");
        log.warn("[AUTH] Codigo MFA invalido para '{}'.", username);
        model.addAttribute("error", true);
        model.addAttribute("username", username);
        model.addAttribute("qr", totp.qrCodeDataUri(username, op != null ? op.getTotpSecret() : ""));
        if (demoPrintTotp && op != null) {
            model.addAttribute("demoCode", totp.currentCodeForDemo(op.getTotpSecret()));
        }
        return "mfa";
    }
}
