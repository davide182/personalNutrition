package com.nutritionists.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void approveNutritionist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        if (user.getRole() != Role.NUTRITIONIST) {
            throw new RuntimeException("Solo i nutrizionisti possono essere approvati.");
        }
        
        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("Questo nutrizionista non è in attesa di approvazione. Stato attuale: " + user.getStatus());
        }
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("✅ Nutrizionista approvato: {} (ID: {})", user.getEmail(), userId);
        
       
        emailService.sendApprovalEmailToNutritionist(
            user.getEmail(), 
            user.getUserProfile().getFirstName(), 
            user.getUserProfile().getLastName()
        );
    }
    
    @Transactional
    public void rejectNutritionist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        if (user.getRole() != Role.NUTRITIONIST) {
            throw new RuntimeException("Solo i nutrizionisti possono essere respinti.");
        }
        
        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("Questo nutrizionista non è in attesa di approvazione.");
        }
        
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
        
        log.info("❌ Nutrizionista rifiutato e disabilitato: {} (ID: {})", user.getEmail(), userId);
        
    
        emailService.sendRejectionEmailToNutritionist(
            user.getEmail(),
            user.getUserProfile().getFirstName(),
            user.getUserProfile().getLastName()
        );
    }
    
    @Transactional
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Non puoi sospendere l'amministratore");
        }
        
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        
        log.info("⏸️ Utente sospeso: {} (ID: {})", user.getEmail(), userId);
    }
    
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        if (user.getStatus() == UserStatus.SUSPENDED) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            log.info("▶️ Utente riattivato: {} (ID: {})", user.getEmail(), userId);
        } else if (user.getStatus() == UserStatus.DISABLED) {
            throw new RuntimeException("Questo nutrizionista è stato disabilitato permanentemente. Non può essere riattivato.");
        } else {
            throw new RuntimeException("Questo utente non è sospeso. Stato attuale: " + user.getStatus());
        }
    }
    
    @Transactional
    public void reenableDisabledNutritionist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));
        
        if (user.getRole() != Role.NUTRITIONIST) {
            throw new RuntimeException("Solo i nutrizionisti possono essere riabilitati.");
        }
        
        if (user.getStatus() != UserStatus.DISABLED) {
            throw new RuntimeException("Questo nutrizionista non è disabilitato. Stato attuale: " + user.getStatus());
        }
        
        user.setStatus(UserStatus.PENDING); 
        userRepository.save(user);
        
        log.info("🔄 Nutrizionista disabilitato riportato in attesa: {} (ID: {})", user.getEmail(), userId);
    }
}