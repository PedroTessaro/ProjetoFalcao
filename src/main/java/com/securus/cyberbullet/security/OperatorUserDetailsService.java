package com.securus.cyberbullet.security;

import com.securus.cyberbullet.domain.Operator;
import com.securus.cyberbullet.repository.OperatorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Carrega operadores do banco relacional para o Spring Security. */
@Service
public class OperatorUserDetailsService implements UserDetailsService {

    private final OperatorRepository operatorRepository;

    public OperatorUserDetailsService(OperatorRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Operator op = operatorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Operador nao encontrado: " + username));
        return User.withUsername(op.getUsername())
                .password(op.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + op.getRole().name()))
                .build();
    }
}
