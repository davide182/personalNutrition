package com.nutritionists.controller;

import com.nutritionists.model.dto.request.ForgotPasswordRequest;
import com.nutritionists.model.dto.request.ResetPasswordRequest;
import com.nutritionists.model.dto.response.ResetPasswordResponse;
import com.nutritionists.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private ForgotPasswordRequest forgotRequest;
    private ResetPasswordRequest resetRequest;

    @BeforeEach
    void setUp() {
        forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("test@example.com");

        resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("valid-token");
        resetRequest.setNewPassword("newPassword123");
    }

    @Test
    void forgotPassword_ReturnsSuccessMessage() {

        doNothing().when(passwordResetService).forgotPassword(any(ForgotPasswordRequest.class));

    
        ResponseEntity<Map<String, String>> response = passwordResetController.forgotPassword(forgotRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("Se l'email è registrata"));
        verify(passwordResetService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void verifyResetToken_ReturnsValidTrue() {
 
        when(passwordResetService.validateToken("valid-token")).thenReturn(true);

      
        ResponseEntity<Map<String, Boolean>> response = passwordResetController.verifyResetToken("valid-token");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("valid"));
    }

    @Test
    void verifyResetToken_ReturnsValidFalse() {
    
        when(passwordResetService.validateToken("invalid-token")).thenReturn(false);


        ResponseEntity<Map<String, Boolean>> response = passwordResetController.verifyResetToken("invalid-token");


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().get("valid"));
    }

    @Test
    void resetPassword_ReturnsSuccess() {
        ResetPasswordResponse resetResponse = ResetPasswordResponse.builder()
                .success(true)
                .message("Password resettata con successo")
                .build();
        when(passwordResetService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(resetResponse);


        ResponseEntity<ResetPasswordResponse> response = passwordResetController.resetPassword(resetRequest);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password resettata con successo", response.getBody().getMessage());
    }
}