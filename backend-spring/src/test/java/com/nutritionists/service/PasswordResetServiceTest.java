package com.nutritionists.service;

import com.nutritionists.model.dto.request.ForgotPasswordRequest;
import com.nutritionists.model.dto.request.ResetPasswordRequest;
import com.nutritionists.model.dto.response.ResetPasswordResponse;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private LoggingService loggingService;
    
    @InjectMocks
    private PasswordResetService passwordResetService;
    
    private User user;
    private UserProfile profile;
    
    @BeforeEach
    void setUp() {
        profile = UserProfile.builder()
                .profileId(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .build();
        
        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .userProfile(profile)
                .build();
        profile.setUser(user);
    }
    
    @Test
    void forgotPassword_Success() {

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        

        passwordResetService.forgotPassword(request);
        

        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenExpiry());
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString(), anyString());
        verify(loggingService).info(eq("AUTH"), eq("FORGOT_PASSWORD"), anyString(), anyString(), anyString());
    }
    
    @Test
    void forgotPassword_DoesNothing_WhenEmailNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@example.com");
        
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        
     
        passwordResetService.forgotPassword(request);
        

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        verify(loggingService).warn(eq("AUTH"), eq("FORGOT_PASSWORD"), isNull(), eq("notfound@example.com"), anyString(), isNull());
    }
    
    @Test
    void validateToken_ReturnsTrue_WhenTokenValid() {
      
        String token = "valid-token";
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(user));
        
    
        boolean result = passwordResetService.validateToken(token);
        
      
        assertTrue(result);
    }
    
    @Test
    void validateToken_ReturnsFalse_WhenTokenNotFound() {
     
        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());
        
        boolean result = passwordResetService.validateToken("invalid-token");
        
    
        assertFalse(result);
    }
    
    @Test
    void validateToken_ReturnsFalse_WhenTokenExpired() {
      
        String token = "expired-token";
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().minusHours(1));
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(user));
        
     
        boolean result = passwordResetService.validateToken(token);
        
        
        assertFalse(result);
    }
    
    @Test
    void resetPassword_Success() {
       
        String token = "valid-token";
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("newPassword123");
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
      
        ResetPasswordResponse response = passwordResetService.resetPassword(request);
       
        assertTrue(response.isSuccess());
        assertEquals("Password resettata con successo. Ora puoi accedere con la nuova password.", response.getMessage());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());
        assertEquals("encodedNewPassword", user.getPasswordHash());
        verify(emailService).sendPasswordResetConfirmationEmail(eq("test@example.com"), anyString());
        verify(loggingService).info(eq("AUTH"), eq("RESET_PASSWORD"), anyString(), anyString(), anyString());
    }
    
    @Test
    void resetPassword_Fails_WhenTokenNotFound() {
    
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("newPassword123");
        
        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());
        
    
        ResetPasswordResponse response = passwordResetService.resetPassword(request);
        
        
        assertFalse(response.isSuccess());
        assertEquals("Token non valido", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void resetPassword_Fails_WhenTokenExpired() {
        
        String token = "expired-token";
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().minusHours(1));
        
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("newPassword123");
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(user));

        ResetPasswordResponse response = passwordResetService.resetPassword(request);
        
  
        assertFalse(response.isSuccess());
        assertEquals("Token scaduto. Richiedi un nuovo reset password", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}