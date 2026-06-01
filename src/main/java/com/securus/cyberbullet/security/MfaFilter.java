package com.securus.cyberbullet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Garante que um operador autenticado por senha so acesse o sistema APOS
 * concluir o segundo fator (MFA/TOTP). Enquanto o atributo de sessao
 * {@code MFA_PASSED} for falso, qualquer requisicao protegida e redirecionada
 * para /mfa.
 */
@Component
public class MfaFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        if (authenticated && !isAllowed(request)) {
            HttpSession session = request.getSession(false);
            boolean mfaPassed = session != null
                    && Boolean.TRUE.equals(session.getAttribute(MfaSuccessHandler.MFA_PASSED));
            if (!mfaPassed) {
                response.sendRedirect(request.getContextPath() + "/mfa");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /** Caminhos liberados mesmo com o MFA pendente. */
    private boolean isAllowed(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/mfa")
                || path.startsWith("/login")
                || path.startsWith("/logout")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/webjars")
                || path.startsWith("/h2-console");
    }
}
