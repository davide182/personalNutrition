package com.nutritionists.service;

import com.nutritionists.client.GeoDistanceClient;
import com.nutritionists.model.dto.request.CreateAppointmentRequest;
import com.nutritionists.model.dto.response.GeosortResponse;
import com.nutritionists.model.entity.*;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentProposalServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private WorkScheduleRepository workScheduleRepository;
    
    @Mock
    private SpecializationRepository specializationRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProposalQueueRepository proposalQueueRepository;
    
    @Mock
    private GeoDistanceClient geoDistanceClient;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private FakeEmailService fakeEmailService;
    
    @Mock
    private LoggingService loggingService;
    
    @InjectMocks
    private AppointmentProposalService appointmentProposalService;
    
    private User patient;
    private UserProfile patientProfile;
    private User nutritionist1;
    private UserProfile nutritionistProfile1;
    private Nutritionist nutritionistEntity1;
    private CreateAppointmentRequest request;
    private GeosortResponse geosortResponse;
    private WorkSchedule workSchedule;
    private Appointment appointment;
    
    @BeforeEach
    void setUp() {
        // Setup paziente
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
        
      
        nutritionistProfile1 = UserProfile.builder()
                .profileId(2L)
                .firstName("Giuseppe")
                .lastName("Verdi")
                .latitude(45.4610)
                .longitude(9.1850)
                .build();
        
        nutritionist1 = User.builder()
                .userId(2L)
                .email("giuseppe.verdi@example.com")
                .role(Role.NUTRITIONIST)
                .status(UserStatus.ACTIVE)
                .userProfile(nutritionistProfile1)
                .build();
        nutritionistProfile1.setUser(nutritionist1);
        
        nutritionistEntity1 = Nutritionist.builder()
                .nutritionistId(1L)
                .user(nutritionist1)
                .serviceRadiusKm(15.0)
                .build();
        nutritionist1.setNutritionist(nutritionistEntity1);
        

        appointment = Appointment.builder()
                .appointmentId(1L)
                .user(patient)
                .startTime(LocalDateTime.of(2026, 6, 8, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 8, 11, 0))
                .status(AppointmentStatus.PENDING_PROPOSAL)
                .build();
     
        request = CreateAppointmentRequest.builder()
                .startTime(LocalDateTime.of(2026, 6, 8, 10, 0))
                .endTime(LocalDateTime.of(2026, 6, 8, 11, 0))
                .build();
        
   
        geosortResponse = GeosortResponse.builder()
                .sortedIds(List.of(2L))
                .distances(List.of(1.2))
                .build();

        workSchedule = WorkSchedule.builder()
                .workScheduleId(1L)
                .nutritionist(nutritionistEntity1)
                .dayOfWeek(java.time.DayOfWeek.MONDAY)
                .startTime(java.time.LocalTime.of(9, 0))
                .endTime(java.time.LocalTime.of(18, 0))
                .build();
    }
    
    @Test
    void getProposalsForNutritionist_ReturnsList() {
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionist1));
        when(proposalQueueRepository.findByStatus(any()))
                .thenReturn(new ArrayList<>());
        
      
        var result = appointmentProposalService.getProposalsForNutritionist(2L);
        
    
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getProposalsForNutritionist_ThrowsWhenNotFound() {
        
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
       
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentProposalService.getProposalsForNutritionist(99L);
        });
        
        assertEquals("Nutrizionista non trovato", exception.getMessage());
    }
    
    @Test
    void createAppointmentProposal_Fails_WhenPatientHasConflictingAppointment() {
      
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByUser_UserIdAndStartTimeBetween(anyLong(), any(), any()))
                .thenReturn(List.of(new Appointment()));
        
      
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentProposalService.createAppointmentProposal(1L, request);
        });
        
        assertEquals("Hai già un appuntamento in questa fascia oraria", exception.getMessage());
    }
    
    @Test
    void createAppointmentProposal_Success_WithDirectNutritionist() {
        request.setNutritionistId(2L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nutritionist1));
        when(workScheduleRepository.findByNutritionist_NutritionistIdAndDayOfWeek(anyLong(), any()))
                .thenReturn(List.of(workSchedule));
        when(appointmentRepository.findConflictingAppointments(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findByUser_UserIdAndStartTimeBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        
        ProposalQueue queue = ProposalQueue.builder()
                .queueId(1L)
                .appointment(appointment)
                .candidateNutritionistIds(List.of(2L))
                .currentIndex(0)
                .status(ProposalQueue.ProposalStatus.ACTIVE)
                .proposalSentAt(LocalDateTime.now())
                .build();
        when(proposalQueueRepository.save(any(ProposalQueue.class))).thenReturn(queue);
        
       
        Appointment result = appointmentProposalService.createAppointmentProposal(1L, request);
        
       
        assertNotNull(result);
        verify(appointmentRepository, atLeastOnce()).save(any(Appointment.class));
    }
}