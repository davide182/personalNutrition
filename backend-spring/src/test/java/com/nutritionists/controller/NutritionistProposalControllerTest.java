package com.nutritionists.controller;

import com.nutritionists.model.dto.NutritionistProposalDto;
import com.nutritionists.model.dto.request.AcceptProposalRequest;
import com.nutritionists.model.dto.response.AppointmentResponse;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.Nutritionist;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.service.AppointmentProposalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionistProposalControllerTest {

    @Mock
    private AppointmentProposalService proposalService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private NutritionistProposalController nutritionistProposalController;

    private User nutritionistUser;
    private Nutritionist nutritionist;
    private UserProfile profile;
    private Appointment appointment;
    private User patient;

    @BeforeEach
    void setUp() {
        profile = UserProfile.builder()
                .profileId(1L)
                .firstName("Giuseppe")
                .lastName("Verdi")
                .build();

        nutritionist = Nutritionist.builder()
                .nutritionistId(1L)
                .build();

        nutritionistUser = User.builder()
                .userId(2L)
                .email("giuseppe.verdi@example.com")
                .role(Role.NUTRITIONIST)
                .status(UserStatus.ACTIVE)
                .userProfile(profile)
                .nutritionist(nutritionist) 
                .build();
        profile.setUser(nutritionistUser);
        nutritionist.setUser(nutritionistUser);

        // Paziente per l'appuntamento
        UserProfile patientProfile = UserProfile.builder()
                .profileId(2L)
                .firstName("Mario")
                .lastName("Rossi")
                .build();
        patient = User.builder()
                .userId(3L)
                .email("mario.rossi@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .userProfile(patientProfile)
                .build();
        patientProfile.setUser(patient);

        appointment = Appointment.builder()
                .appointmentId(1L)
                .nutritionist(nutritionist)
                .user(patient)
                .status(AppointmentStatus.PROPOSED)
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .build();
    }

    @Test
    void getMyProposals_ReturnsList() {
        
        NutritionistProposalDto proposal = NutritionistProposalDto.builder()
                .appointmentId(1L)
                .patientFirstName("Mario")
                .patientLastName("Rossi")
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .distanceKm(2.5)
                .positionInQueue(1)
                .build();

        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        when(proposalService.getProposalsForNutritionist(2L)).thenReturn(List.of(proposal));

       
        ResponseEntity<List<NutritionistProposalDto>> response = nutritionistProposalController.getMyProposals(userDetails);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getAppointmentId());
    }

    @Test
    void acceptProposal_ReturnsOk() {
       
        AcceptProposalRequest request = new AcceptProposalRequest();
        request.setPrice(50.0);
        request.setLocation("Studio Milano");

        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        doNothing().when(proposalService).acceptProposal(eq(1L), eq(2L), eq(50.0), eq("Studio Milano"));

        
        ResponseEntity<String> response = nutritionistProposalController.acceptProposal(userDetails, 1L, request);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("accettata"));
        verify(proposalService).acceptProposal(eq(1L), eq(2L), eq(50.0), eq("Studio Milano"));
    }

    @Test
    void rejectProposal_ReturnsOk() {
       
        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        doNothing().when(proposalService).rejectProposal(1L, 2L);

        
        ResponseEntity<String> response = nutritionistProposalController.rejectProposal(userDetails, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("rifiutata"));
        verify(proposalService).rejectProposal(1L, 2L);
    }

    @Test
    void getMyConfirmedAppointments_ReturnsList() {
        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionistUser));

        Appointment confirmedAppointment = Appointment.builder()
                .appointmentId(2L)
                .nutritionist(nutritionist)
                .user(patient)
                .status(AppointmentStatus.CONFIRMED)
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .location("Studio Milano")
                .price(50.0)
                .build();

        when(appointmentRepository.findByNutritionist_NutritionistId(1L)).thenReturn(List.of(confirmedAppointment));

       
        ResponseEntity<List<AppointmentResponse>> response = 
                nutritionistProposalController.getMyConfirmedAppointments(userDetails);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getAppointmentId());
    }

    @Test
    void getMyConfirmedAppointments_WhenNoAppointments_ReturnsEmptyList() {
    
        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionistUser));
        when(appointmentRepository.findByNutritionist_NutritionistId(1L)).thenReturn(List.of());

       
        ResponseEntity<List<AppointmentResponse>> response = 
                nutritionistProposalController.getMyConfirmedAppointments(userDetails);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getMyConfirmedAppointments_FiltersOnlyConfirmedAndCompleted() {
       
        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionistUser));

        Appointment confirmedAppointment = Appointment.builder()
                .appointmentId(2L)
                .nutritionist(nutritionist)
                .user(patient)
                .status(AppointmentStatus.CONFIRMED)
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .build();

        Appointment proposedAppointment = Appointment.builder()
                .appointmentId(3L)
                .nutritionist(nutritionist)
                .user(patient)
                .status(AppointmentStatus.PROPOSED)
                .startTime(LocalDateTime.of(2026, 6, 16, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 16, 11, 0))
                .build();

        when(appointmentRepository.findByNutritionist_NutritionistId(1L))
                .thenReturn(List.of(confirmedAppointment, proposedAppointment));

      
        ResponseEntity<List<AppointmentResponse>> response = 
                nutritionistProposalController.getMyConfirmedAppointments(userDetails);

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size()); // Solo CONFIRMED, non PROPOSED
        assertEquals(2L, response.getBody().get(0).getAppointmentId());
    }

    @Test
    void markAppointmentAsCompleted_ReturnsOk() {
        
        Appointment confirmedAppointment = Appointment.builder()
                .appointmentId(2L)
                .nutritionist(nutritionist)
                .user(patient)
                .status(AppointmentStatus.CONFIRMED)
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .build();

        when(userDetails.getUsername()).thenReturn("giuseppe.verdi@example.com");
        when(userRepository.findByEmail("giuseppe.verdi@example.com")).thenReturn(Optional.of(nutritionistUser));
        when(appointmentRepository.findById(2L)).thenReturn(Optional.of(confirmedAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(confirmedAppointment);

       
        ResponseEntity<String> response = nutritionistProposalController.markAppointmentAsCompleted(userDetails, 2L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("completato"));
        verify(appointmentRepository).save(any(Appointment.class));
    }
}