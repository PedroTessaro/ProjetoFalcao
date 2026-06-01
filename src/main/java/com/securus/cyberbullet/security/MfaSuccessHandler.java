package com.securus.cyberbullet.security;

import com.securus.cyberbullet.domain.Operator;
import com.securus.cyberbullet.repository.OperatorRepository;
import com.securus.cyberbullet.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Handler executado APOS o sucesso do fator 1 (senha).
 *
 * <p>Simula a verificacao biometrica (fator 3) e decide se exige o fator 2
 * (TOTP/MFA). Se o operador tem MFA habilitado, marca a sessao como pendente
 * e redireciona para /mfa; caso contrario, libera o acesso.
 */
@Component
public class MfaSuccessHandler implements AuthenticationSuccessHandler {

    /** Atributo de sessao que indica se o MFA ja foi concluido. */
    public static final String MFA_PASSED = "MFA_PASSED";
    public static final String PENDING_USER = "MFA_PENDING_USER";

    private static final Logger log = LoggerFactory.getLogger(MfaSuccessHandler.class);

    private final OperatorRepository operatorRepository;
    private final AuditService audit;

    public MfaSuccessHandler(OperatorRepository operatorRepository, AuditService audit) {
        this.operatorRepository = operatorRepository;
        this.audit = audit;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        HttpSession session = request.getSession(true);

        // [SIMULACAO BIOMETRIA] >>> Aqui ficaria a checagem do leitor biometrico
        // (digital/iris) do terminal do operador. Simulamos como aprovada.
        log.info("[AUTH] Fator 1 (senha) OK para '{}'. [SIMULACAO BIOMETRIA] leitor de digital: APROVADO.", username);

        Operator op = operatorRepository.findByUsername(username).orElse(null);
        boolean mfaEnabled = op != null && op.isMfaEnabled() && op.getTotpSecret() != null;

        if (mfaEnabled) {
            session.setAttribute(MFA_PASSED, Boolean.FALSE);
            session.setAttribute(PENDING_USER, username);
            log.info("[AUTH] '{}' requer 2o fator (MFA/TOTP). Redirecionando para /mfa.", username);
            audit.record(username, "AUTH", "PASSWORD_OK", "Senha valida; aguardando 2o fator (MFA).");
            response.sendRedirect(request.getContextPath() + "/mfa");
        } else {
            session.setAttribute(MFA_PASSED, Boolean.TRUE);
            audit.record(username, "AUTH", "LOGIN", "Login concluido (sem MFA).");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}
