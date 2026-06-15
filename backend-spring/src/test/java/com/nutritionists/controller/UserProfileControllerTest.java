package com.nutritionists.controller;

import com.nutritionists.model.dto.response.UserProfileResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserProfileController userProfileController;

    private User user;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = UserProfile.builder()
                .profileId(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .latitude(45.4642)
                .longitude(9.1900)
                .build();

        user = User.builder()
                .userId(1L)
                .email("mario.rossi@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .userProfile(profile)
                .build();
        profile.setUser(user);
    }

    @Test
    void getUserProfile_ReturnsProfile() {
   
        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(user));

      
        ResponseEntity<UserProfileResponse> response = userProfileController.getUserProfile(userDetails);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("mario.rossi@example.com", response.getBody().getEmail());
        assertEquals("Mario", response.getBody().getFirstName());
        assertEquals("Rossi", response.getBody().getLastName());
    }

    @Test
    void getUserStatus_ReturnsActive() {
      
        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(user));

      
        ResponseEntity<Map<String, Object>> response = userProfileController.getUserStatus(userDetails);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("active"));
        assertEquals("ACTIVE", response.getBody().get("status"));
        assertEquals("Account attivo", response.getBody().get("message"));
    }

    @Test
    void getUserStatus_ReturnsInactive_WhenSuspended() {
     
        user.setStatus(UserStatus.SUSPENDED);
        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(user));

      
        ResponseEntity<Map<String, Object>> response = userProfileController.getUserStatus(userDetails);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("active"));
        assertEquals("SUSPENDED", response.getBody().get("status"));
        assertEquals("Account non attivo", response.getBody().get("message"));
    }
}