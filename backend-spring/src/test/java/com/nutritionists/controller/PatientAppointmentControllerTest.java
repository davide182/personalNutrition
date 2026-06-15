package com.nutritionists.controller;

import com.nutritionists.model.dto.request.CreateAppointmentRequest;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.ProposalQueueRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientAppointmentControllerTest {

    @Mock
    private AppointmentProposalService proposalService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ProposalQueueRepository proposalQueueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private PatientAppointmentController patientAppointmentController;

    private User patient;
    private UserProfile patientProfile;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patientProfile = UserProfile.builder()
                .profileId(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .latitude(45.4642)
                .longitude(9.1900)
                .build();

        patient = User.builder()
                .userId(1L)
                .email("mario.rossi@example.com")
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .userProfile(patientProfile)
                .build();
        patientProfile.setUser(patient);

        appointment = Appointment.builder()
                .appointmentId(1L)
                .user(patient)
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .status(AppointmentStatus.PENDING_PROPOSAL)
                .build();
    }

    @Test
    void createAppointment_ReturnsOk() {
    
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .startTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .build();

        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(patient));
        when(proposalService.createAppointmentProposal(eq(1L), any(CreateAppointmentRequest.class)))
                .thenReturn(appointment);


        ResponseEntity<Appointment> response = patientAppointmentController.createAppointment(userDetails, request);

      
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getAppointmentId());
        verify(proposalService).createAppointmentProposal(eq(1L), any(CreateAppointmentRequest.class));
    }

    @Test
    void getMyAppointments_ReturnsList() {
       
        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByUser_UserId(1L)).thenReturn(List.of(appointment));

       
        ResponseEntity<List<Appointment>> response = patientAppointmentController.getMyAppointments(userDetails);

    
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getAppointmentId());
    }

    @Test
    void cancelAppointment_ForConfirmedAppointment_ReturnsOk() {
    
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

    
        ResponseEntity<String> response = patientAppointmentController.cancelAppointment(userDetails, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("cancellato"));
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void cancelAppointment_WhenNotOwner_ThrowsException() {

        User otherPatient = User.builder().userId(99L).build();
        appointment.setUser(otherPatient);

        when(userDetails.getUsername()).thenReturn("mario.rossi@example.com");
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientAppointmentController.cancelAppointment(userDetails, 1L);
        });

        assertEquals("Non hai accesso a questo appuntamento", exception.getMessage());
    }
}