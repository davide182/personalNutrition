package com.nutritionists.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.dto.request.CreateAppointmentRequest;
import com.nutritionists.model.dto.response.AppointmentDetailResponse;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.ProposalQueue;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.ProposalQueueRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.service.AppointmentProposalService;
import com.nutritionists.model.entity.User;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientAppointmentController {
    
    private final AppointmentProposalService proposalService;
    private final AppointmentRepository appointmentRepository;
    private final ProposalQueueRepository proposalQueueRepository;
    private final UserRepository userRepository;
    
    @PostMapping("/appointments")
    public ResponseEntity<Appointment> createAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAppointmentRequest request) {
        
        Long patientId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        Appointment appointment = proposalService.createAppointmentProposal(patientId, request);
        
        return ResponseEntity.ok(appointment);
    }


    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long patientId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        List<Appointment> appointments = appointmentRepository.findByUser_UserId(patientId);
        
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<AppointmentDetailResponse> getAppointmentDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        
        User patient = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        
        if (!appointment.getUser().getUserId().equals(patient.getUserId())) {
            throw new RuntimeException("Non hai accesso a questo appuntamento");
        }
        
        User nutritionist = appointment.getNutritionist().getUser();
        
        // Calcola distanza
        double distance = calculateDistance(
            nutritionist.getUserProfile().getLatitude(),
            nutritionist.getUserProfile().getLongitude(),
            patient.getUserProfile().getLatitude(),
            patient.getUserProfile().getLongitude()
        );
        
        AppointmentDetailResponse response = AppointmentDetailResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .nutritionistName(nutritionist.getUserProfile().getFirstName() + " " + nutritionist.getUserProfile().getLastName())
                .nutritionistLatitude(nutritionist.getUserProfile().getLatitude())
                .nutritionistLongitude(nutritionist.getUserProfile().getLongitude())
                .patientLatitude(patient.getUserProfile().getLatitude())
                .patientLongitude(patient.getUserProfile().getLongitude())
                .distance(distance)
                .price(appointment.getPrice())
                .location(appointment.getLocation())
                .build();
        
        return ResponseEntity.ok(response);
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0;
        }
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

@DeleteMapping("/appointments/{appointmentId}")
public ResponseEntity<String> cancelAppointment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long appointmentId) {
    
    Long patientId = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Utente non trovato"))
            .getUserId();
    
    Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
    
    if (!appointment.getUser().getUserId().equals(patientId)) {
        throw new RuntimeException("Non hai accesso a questo appuntamento");
    }
    
    if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok("✅ Appuntamento cancellato con successo");
    } else if (appointment.getStatus() == AppointmentStatus.PROPOSED ||
               appointment.getStatus() == AppointmentStatus.PENDING_PROPOSAL) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        
        // Chiudi anche la coda proposta
        ProposalQueue queue = proposalQueueRepository.findByAppointment_AppointmentId(appointmentId).orElse(null);
        if (queue != null) {
            queue.setStatus(ProposalQueue.ProposalStatus.FAILED);
            proposalQueueRepository.save(queue);
        }
        
        return ResponseEntity.ok("✅ Richiesta di appuntamento cancellata");
    } else {
        throw new RuntimeException("Non puoi cancellare un appuntamento in stato: " + appointment.getStatus());
    }
    }
}