package com.nutritionists.controller;

import com.nutritionists.model.dto.response.AdminUserResponse;
import com.nutritionists.model.entity.Specialization;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.SpecializationRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AdminController adminController;

    private User pendingNutritionist;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = UserProfile.builder()
                .profileId(1L)
                .firstName("Dott")
                .lastName("Rossi")
                .build();

        pendingNutritionist = User.builder()
                .userId(1L)
                .email("dott.rossi@example.com")
                .role(Role.NUTRITIONIST)
                .status(UserStatus.PENDING)
                .userProfile(profile)
                .build();
        profile.setUser(pendingNutritionist);
    }

    @Test
    void getPendingNutritionists_ReturnsList() {
   
        when(userRepository.findByRoleAndStatus(Role.NUTRITIONIST, UserStatus.PENDING))
                .thenReturn(List.of(pendingNutritionist));

        
        ResponseEntity<List<AdminUserResponse>> response = adminController.getPendingNutritionists();

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("dott.rossi@example.com", response.getBody().get(0).getEmail());
    }

    @Test
    void approveNutritionist_ReturnsOk() {
        
        doNothing().when(adminService).approveNutritionist(1L);

       
        ResponseEntity<String> response = adminController.approveNutritionist(1L);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("approvato"));
        verify(adminService).approveNutritionist(1L);
    }

    @Test
    void rejectNutritionist_ReturnsOk() {
        
        doNothing().when(adminService).rejectNutritionist(1L);

        
        ResponseEntity<String> response = adminController.rejectNutritionist(1L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("respinto"));
        verify(adminService).rejectNutritionist(1L);
    }

    @Test
    void suspendUser_ReturnsOk() {
        
        doNothing().when(adminService).suspendUser(2L);

       
        ResponseEntity<String> response = adminController.suspendUser(2L);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("sospeso"));
        verify(adminService).suspendUser(2L);
    }

    @Test
    void activateUser_ReturnsOk() {
        
        doNothing().when(adminService).activateUser(2L);

        
        ResponseEntity<String> response = adminController.activateUser(2L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("riattivato"));
        verify(adminService).activateUser(2L);
    }

    @Test
    void getAllSpecializations_ReturnsList() {
      
        Specialization spec = Specialization.builder()
                .specializationId(1L)
                .name("Nutrizione Clinica")
                .build();
        when(specializationRepository.findAll()).thenReturn(List.of(spec));

       
        ResponseEntity<List<Specialization>> response = adminController.getAllSpecializations();

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Nutrizione Clinica", response.getBody().get(0).getName());
    }
}