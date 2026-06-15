package com.nutritionists.service;

import com.nutritionists.model.dto.request.LoginRequest;
import com.nutritionists.model.dto.request.RegisterRequest;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.NutritionistRepository;
import com.nutritionists.repository.UserProfileRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserProfileRepository userProfileRepository;
    
    @Mock
    private NutritionistRepository nutritionistRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private LoggingService loggingService;
    
    @Mock
    private GeocodingService geocodingService;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserProfile userProfile;
    
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
        
        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .build();
        
        userProfile = UserProfile.builder()
                .profileId(1L)
                .user(user)
                .firstName("Test")
                .lastName("User")
                .latitude(45.4642)
                .longitude(9.1900)
                .build();
    }
    
    @Test
    void register_Success_ForPatient() {
     
        double[] coordinates = {45.4642, 9.1900};
        when(geocodingService.geocodeAddress(anyString())).thenReturn(coordinates);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        var response = authService.register(registerRequest);
    
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.PATIENT, response.getRole());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertNull(response.getToken());
        
        verify(geocodingService).geocodeAddress("Piazza del Duomo, Milano");
        verify(emailService).sendWelcomeEmailToPatient(anyString(), anyString());
    }
    
    @Test
    void register_Fails_WhenEmailAlreadyExists() {
    
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        
        assertEquals("Email già in uso", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_Fails_WhenGeocodingFails() {
      
        when(geocodingService.geocodeAddress(anyString())).thenReturn(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        
       
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        
        assertEquals("Impossibile trovare le coordinate per l'indirizzo fornito. Verifica l'indirizzo.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void login_Success() {
       
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_UserId(anyLong())).thenReturn(Optional.of(userProfile));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        
    
        var response = authService.login(loginRequest);
        
 
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertNotNull(response.getMessage());
    }
    
    @Test
    void login_Fails_WhenUserNotFound() {
     
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
   
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Email o password non validi", exception.getMessage());
    }
    
    @Test
    void login_Fails_WhenAccountIsPending() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        
        when(userRepository.findByEmail(anyString())).thenThrow(
            new BadCredentialsException("Account in attesa di approvazione da parte dell'amministratore")
        );
        
       
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        

        assertTrue(exception.getMessage().contains("attesa di approvazione") || 
                   exception.getMessage().contains("attesa di revisione"));
    }
    
    @Test
    void login_Fails_WhenAccountIsSuspended() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenThrow(
            new BadCredentialsException("Account sospeso. Contatta l'amministratore.")
        );
        

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        

        assertTrue(exception.getMessage().contains("sospeso"));
    }
    
    @Test
    void login_Fails_WhenAccountIsDisabled() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByEmail(anyString())).thenThrow(
            new BadCredentialsException("Account disabilitato permanentemente.")
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertTrue(exception.getMessage().contains("disabilitato"));
    }
    
    @Test
    void login_Fails_WhenAuthenticationThrowsGenericException() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Errore durante il login. Riprova più tardi.", exception.getMessage());
    }
}