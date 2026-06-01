package com.securus.cyberbullet.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/** Configuracao do Spring Security: senha (fator 1) + MFA/TOTP (fator 2). */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final MfaSuccessHandler mfaSuccessHandler;
    private final MfaFilter mfaFilter;

    public SecurityConfig(MfaSuccessHandler mfaSuccessHandler, MfaFilter mfaFilter) {
        this.mfaSuccessHandler = mfaSuccessHandler;
        this.mfaFilter = mfaFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/h2-console/**").permitAll()
                        .requestMatchers("/mfa/**").authenticated()
                        .requestMatchers("/operators/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(mfaSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                // O MfaFilter roda antes da autorizacao para barrar acesso ate o 2o fator.
                .addFilterBefore(mfaFilter, AuthorizationFilter.class)
                // CSRF desabilitado apenas para o console do H2 (ferramenta de dev).
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
                // Permite que o console do H2 seja exibido em frame da mesma origem.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
