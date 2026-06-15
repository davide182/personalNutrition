package com.nutritionists.service;

import com.nutritionists.model.dto.request.DisableNutritionistRequest;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisableProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private LoggingService loggingService;
    
    @InjectMocks
    private DisableProfileService disableProfileService;
    
    private User patient;
    private User nutritionist;
    private UserProfile profile;
    
    @BeforeEach
    void setUp() {
        profile = UserProfile.builder()
                .profileId(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .build();
        
        patient = User.builder()
                .userId(1L)
                .email("patient@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .userProfile(profile)
                .build();
        profile.setUser(patient);
        
        nutritionist = User.builder()
                .userId(2L)
                .email("nutritionist@example.com")
                .role(Role.NUTRITIONIST)
                .status(UserStatus.ACTIVE)
                .userProfile(profile)
                .build();
    }
    
    @Test
    void disablePatientProfile_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.countActiveAppointmentsByPatient(1L)).thenReturn(0);
        when(userRepository.save(any(User.class))).thenReturn(patient);
        
   
        disableProfileService.disablePatientProfile(1L);
        

        assertEquals(UserStatus.SELF_DISABLED, patient.getStatus());
        verify(emailService).sendProfileDisabledEmail(anyString(), anyString());
        verify(loggingService).info(eq("PROFILE"), eq("SELF_DISABLE"), anyString(), anyString(), anyString());
    }
    
    @Test
    void disablePatientProfile_Fails_WhenPatientHasActiveAppointments() {
    
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.countActiveAppointmentsByPatient(1L)).thenReturn(2);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            disableProfileService.disablePatientProfile(1L);
        });
        
        assertTrue(exception.getMessage().contains("appuntamenti attivi"));
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void disablePatientProfile_Fails_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
    
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            disableProfileService.disablePatientProfile(99L);
        });
        
        assertEquals("Paziente non trovato", exception.getMessage());
    }
    
    @Test
    void requestNutritionistDisable_Success() {
       
        DisableNutritionistRequest request = new DisableNutritionistRequest();
        request.setReason("Voglio disabilitare temporaneamente");
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionist));
        when(appointmentRepository.countActiveAppointmentsByNutritionist(2L)).thenReturn(0);
        when(userRepository.save(any(User.class))).thenReturn(nutritionist);
        
       
        disableProfileService.requestNutritionistDisable(2L, request);
        

        assertEquals(UserStatus.SUSPENDED, nutritionist.getStatus());
        verify(emailService).notifyAdminNutritionistDisableRequest(any(User.class), anyString());
        verify(loggingService).info(eq("PROFILE"), eq("DISABLE_REQUEST"), anyString(), anyString(), anyString());
    }
    
    @Test
    void approveNutritionistDisable_Success() {
    
        nutritionist.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionist));
        when(userRepository.save(any(User.class))).thenReturn(nutritionist);
        

        disableProfileService.approveNutritionistDisable(2L);
        

        assertEquals(UserStatus.SELF_DISABLED, nutritionist.getStatus());
        verify(emailService).sendProfileDisabledEmail(anyString(), anyString());
        verify(loggingService).info(eq("PROFILE"), eq("DISABLE_APPROVED"), anyString(), anyString(), anyString());
    }
    
    @Test
    void rejectNutritionistDisable_Success() {
      
        nutritionist.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionist));
        when(userRepository.save(any(User.class))).thenReturn(nutritionist);
      
        disableProfileService.rejectNutritionistDisable(2L, "Motivo del rifiuto");
        
        assertEquals(UserStatus.ACTIVE, nutritionist.getStatus());
        verify(emailService).sendProfileDisableRejectedEmail(anyString(), anyString(), anyString());
        verify(loggingService).info(eq("PROFILE"), eq("DISABLE_REJECTED"), anyString(), anyString(), anyString());
    }
    
    @Test
    void reenableProfile_Success() {

        patient.setStatus(UserStatus.SELF_DISABLED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.save(any(User.class))).thenReturn(patient);
        

        disableProfileService.reenableProfile(1L);
        
        assertEquals(UserStatus.ACTIVE, patient.getStatus());
        verify(emailService).sendProfileReenabledEmail(anyString(), anyString());
        verify(loggingService).info(eq("PROFILE"), eq("REENABLE"), anyString(), anyString(), anyString());
    }
    
    @Test
    void reenableProfile_Fails_WhenUserNotSelfDisabled() {

        patient.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            disableProfileService.reenableProfile(1L);
        });
        
        assertTrue(exception.getMessage().contains("non è disabilitato volontariamente"));
    }
}