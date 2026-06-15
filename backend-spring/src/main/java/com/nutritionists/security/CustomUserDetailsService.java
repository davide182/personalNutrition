package com.nutritionists.security;

import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        if (user.getStatus() == UserStatus.SELF_DISABLED) {
            log.warn("Tentativo di accesso per utente auto-disabilitato: {}", email);
            throw new UsernameNotFoundException("SELF_DISABLED: Il tuo account è stato disabilitato. Contatta l'amministratore per riattivarlo.");
        }
        
        if (user.getStatus() == UserStatus.PENDING_DISABLE) {
            log.warn("Tentativo di accesso per utente in attesa di disabilitazione: {}", email);
            throw new UsernameNotFoundException("PENDING_DISABLE: Il tuo account è in attesa di disabilitazione. Riceverai una email di conferma.");
        }
        
        if (user.getStatus() == UserStatus.PENDING) {
            log.warn("Tentativo di accesso per utente in attesa di approvazione: {}", email);
            throw new UsernameNotFoundException("PENDING: Il tuo account è in attesa di approvazione. Riceverai una email quando sarà attivato.");
        }
        
        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Tentativo di accesso per utente sospeso: {}", email);
            throw new UsernameNotFoundException("SUSPENDED: Il tuo account è in attesa di revisione. Riceverai una email quando sarà riattivato.");
        }
        
        if (user.getStatus() == UserStatus.DISABLED) {
            log.warn("Tentativo di accesso per utente disabilitato permanentemente: {}", email);
            throw new UsernameNotFoundException("DISABLED: Il tuo account è stato disabilitato permanentemente. Contatta l'amministratore.");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Tentativo di accesso per utente con stato non attivo: {} - {}", email, user.getStatus());
            throw new UsernameNotFoundException("INACTIVE: Il tuo account non è attivo. Contatta l'amministratore.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}