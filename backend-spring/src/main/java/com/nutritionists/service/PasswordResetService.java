// com.nutritionists.service.PasswordResetService.java
package com.nutritionists.service;

import com.nutritionists.model.dto.request.ForgotPasswordRequest;
import com.nutritionists.model.dto.request.ResetPasswordRequest;
import com.nutritionists.model.dto.response.ResetPasswordResponse;
import com.nutritionists.model.entity.User;
import com.nutritionists.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final LoggingService loggingService;

    private static final int TOKEN_EXPIRY_HOURS = 24;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
    
        if (user == null) {
            log.info("Tentativo reset password per email non esistente: {}", request.getEmail());
            loggingService.warn("AUTH", "FORGOT_PASSWORD", null, request.getEmail(), 
                "Tentativo reset per email non registrata", null);
            return;
        }
        
       
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        userRepository.save(user);
        
        
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(
            user.getEmail(),
            user.getUserProfile().getFirstName(),
            resetLink
        );
        
        log.info("Email reset password inviata a: {}", user.getEmail());
        loggingService.info("AUTH", "FORGOT_PASSWORD", String.valueOf(user.getUserId()), user.getEmail(), 
            "Email reset password inviata. Token scade tra " + TOKEN_EXPIRY_HOURS + " ore");
    }


    public boolean validateToken(String token) {
        User user = userRepository.findByResetToken(token).orElse(null);
        
        if (user == null) {
            return false;
        }
        
        if (user.getResetTokenExpiry() == null || 
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }


    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken()).orElse(null);
        
        if (user == null) {
            log.warn("Tentativo reset con token non valido: {}", request.getToken());
            loggingService.warn("AUTH", "RESET_PASSWORD", null, null, 
                "Tentativo reset con token non valido: " + request.getToken(), null);
            return ResetPasswordResponse.builder()
                    .success(false)
                    .message("Token non valido")
                    .build();
        }
        
        if (user.getResetTokenExpiry() == null || 
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Token scaduto per utente: {}", user.getEmail());
            loggingService.warn("AUTH", "RESET_PASSWORD", String.valueOf(user.getUserId()), user.getEmail(), 
                "Token scaduto", null);
            return ResetPasswordResponse.builder()
                    .success(false)
                    .message("Token scaduto. Richiedi un nuovo reset password")
                    .build();
        }
        

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        
        log.info("Password resettata con successo per: {}", user.getEmail());
        loggingService.info("AUTH", "RESET_PASSWORD", String.valueOf(user.getUserId()), user.getEmail(), 
            "Password resettata con successo");
        
       
        emailService.sendPasswordResetConfirmationEmail(
            user.getEmail(),
            user.getUserProfile().getFirstName()
        );
        
        return ResetPasswordResponse.builder()
                .success(true)
                .message("Password resettata con successo. Ora puoi accedere con la nuova password.")
                .build();
    }
}