package com.nutritionists.controller;

import com.nutritionists.model.dto.AuthResponse;
import com.nutritionists.model.dto.request.LoginRequest;
import com.nutritionists.model.dto.request.RegisterRequest;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .role(Role.PATIENT)
                .address("Piazza del Duomo, Milano")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        authResponse = AuthResponse.builder()
                .email("test@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .firstName("Test")
                .lastName("User")
                .message("Registrazione completata")
                .build();
    }

    @Test
    void register_ReturnsOk() {
      
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

      
        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

      
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals(Role.PATIENT, response.getBody().getRole());
        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_ReturnsOk() {
      
        AuthResponse loginResponse = AuthResponse.builder()
                .token("jwt-token-123")
                .email("test@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .firstName("Test")
                .lastName("User")
                .message("Login effettuato con successo")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

       
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token-123", response.getBody().getToken());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(authService).login(any(LoginRequest.class));
    }
}