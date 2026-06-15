package com.nutritionists.service;

import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.UserStatus;
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
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private LoggingService loggingService;
    
    @InjectMocks
    private AdminService adminService;
    
    private User nutritionist;
    private UserProfile profile;
    
    @BeforeEach
    void setUp() {
        profile = UserProfile.builder()
                .profileId(1L)
                .firstName("Dott")
                .lastName("Rossi")
                .build();
        
        nutritionist = User.builder()
                .userId(1L)
                .email("dott.rossi@example.com")
                .role(Role.NUTRITIONIST)
                .status(UserStatus.PENDING)
                .userProfile(profile)
                .build();
        profile.setUser(nutritionist);
    }
    
    @Test
    void approveNutritionist_Success() {
     
        when(userRepository.findById(1L)).thenReturn(Optional.of(nutritionist));
        when(userRepository.save(any(User.class))).thenReturn(nutritionist);
        
   
        adminService.approveNutritionist(1L);
        

        assertEquals(UserStatus.ACTIVE, nutritionist.getStatus());
        verify(emailService).sendApprovalEmailToNutritionist(anyString(), anyString(), anyString());
    }
    
    @Test
    void approveNutritionist_Fails_WhenUserNotFound() {
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
       
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.approveNutritionist(1L);
        });
        
        assertTrue(exception.getMessage().contains("non trovato"));
    }
    
    @Test
    void approveNutritionist_Fails_WhenUserIsNotNutritionist() {
       
        nutritionist.setRole(Role.PATIENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(nutritionist));
        
       
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.approveNutritionist(1L);
        });
        
        assertEquals("Solo i nutrizionisti possono essere approvati.", exception.getMessage());
    }
    
    @Test
    void approveNutritionist_Fails_WhenUserIsNotPending() {
      
        nutritionist.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(nutritionist));
        
       
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.approveNutritionist(1L);
        });
        
        assertTrue(exception.getMessage().contains("non è in attesa di approvazione"));
    }
    
    @Test
    void rejectNutritionist_Success() {
     
        when(userRepository.findById(1L)).thenReturn(Optional.of(nutritionist));
        when(userRepository.save(any(User.class))).thenReturn(nutritionist);
        
      
        adminService.rejectNutritionist(1L);
        
      
        assertEquals(UserStatus.DISABLED, nutritionist.getStatus());
        verify(emailService).sendRejectionEmailToNutritionist(anyString(), anyString(), anyString());
    }
    
    @Test
    void suspendUser_Success() {
        
        User patient = User.builder()
                .userId(2L)
                .email("patient@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(userRepository.save(any(User.class))).thenReturn(patient);
        
       
        adminService.suspendUser(2L);
        
        
        assertEquals(UserStatus.SUSPENDED, patient.getStatus());
    }
    
    @Test
    void suspendUser_Fails_WhenTryingToSuspendAdmin() {
      
        User admin = User.builder()
                .userId(3L)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        
    
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.suspendUser(3L);
        });
        
        assertEquals("Non puoi sospendere l'amministratore", exception.getMessage());
    }
    
    @Test
    void activateUser_Success() {
       
        User suspendedUser = User.builder()
                .userId(2L)
                .email("patient@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.SUSPENDED)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(suspendedUser));
        when(userRepository.save(any(User.class))).thenReturn(suspendedUser);
        
      
        adminService.activateUser(2L);
        
    
        assertEquals(UserStatus.ACTIVE, suspendedUser.getStatus());
    }
    
    @Test
    void activateUser_Fails_WhenUserNotSuspended() {

        User activeUser = User.builder()
                .userId(2L)
                .email("patient@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(activeUser));
        
    
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.activateUser(2L);
        });
        
        assertTrue(exception.getMessage().contains("non è sospeso"));
    }
    
    @Test
    void reenableDisabledNutritionist_Success() {
      
        User disabledNutritionist = User.builder()
                .userId(2L)
                .email("disabled@example.com")
                .role(Role.NUTRITIONIST)
                .status(UserStatus.DISABLED)
                .userProfile(profile)
                .build();
        profile.setUser(disabledNutritionist);
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(disabledNutritionist));
        when(userRepository.save(any(User.class))).thenReturn(disabledNutritionist);
        

        adminService.reenableDisabledNutritionist(2L);
        
        
        assertEquals(UserStatus.PENDING, disabledNutritionist.getStatus());

    }
}