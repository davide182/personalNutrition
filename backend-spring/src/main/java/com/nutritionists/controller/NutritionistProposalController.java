package com.nutritionists.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.nutritionists.model.dto.NutritionistProposalDto;
import com.nutritionists.model.dto.request.AcceptProposalRequest;
import com.nutritionists.model.dto.response.AppointmentResponse;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.Nutritionist;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.service.AppointmentProposalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/nutritionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NUTRITIONIST')")
public class NutritionistProposalController {
    
    private final AppointmentProposalService proposalService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    
    @GetMapping("/proposals")
    public ResponseEntity<List<NutritionistProposalDto>> getMyProposals(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long nutritionistId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        List<NutritionistProposalDto> proposals = proposalService.getProposalsForNutritionist(nutritionistId);
        
        return ResponseEntity.ok(proposals);
    }
    
    @PutMapping("/proposals/{appointmentId}/accept")
    public ResponseEntity<String> acceptProposal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId,
            @RequestBody AcceptProposalRequest request) {
        
        Long nutritionistId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        proposalService.acceptProposal(appointmentId, nutritionistId, request.getPrice(), request.getLocation());
        
        return ResponseEntity.ok("✅ Proposta accettata! L'appuntamento è stato confermato.");
    }
    
    @PutMapping("/proposals/{appointmentId}/reject")
    public ResponseEntity<String> rejectProposal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        
        Long nutritionistId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        proposalService.rejectProposal(appointmentId, nutritionistId);
        
        return ResponseEntity.ok("❌ Proposta rifiutata. La richiesta passerà al prossimo nutrizionista disponibile.");
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyConfirmedAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long nutritionistId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        log.info("🔍 Recupero appuntamenti per nutrizionista ID: {}", nutritionistId);
        
        Nutritionist nutritionist = userRepository.findById(nutritionistId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"))
                .getNutritionist();

        if (nutritionist == null) {
            log.warn("⚠️ Profilo nutrizionista non trovato per ID: {}", nutritionistId);
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        List<Appointment> appointments = appointmentRepository.findByNutritionist_NutritionistId(
            nutritionist.getNutritionistId());
        
        log.info("📅 Trovati {} appuntamenti per nutrizionista", appointments.size());
        
        List<AppointmentResponse> response = appointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.COMPLETED)
            .map(a -> AppointmentResponse.builder()
                .appointmentId(a.getAppointmentId())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .patientFirstName(a.getUser().getUserProfile().getFirstName())
                .patientLastName(a.getUser().getUserProfile().getLastName())
                .location(a.getLocation())
                .price(a.getPrice())
                .build())
            .collect(Collectors.toList());
        
        log.info("✅ Restituiti {} appuntamenti filtrati", response.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<String> markAppointmentAsCompleted(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        
        Long nutritionistId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        
        if (appointment.getNutritionist() == null || 
            !appointment.getNutritionist().getUser().getUserId().equals(nutritionistId)) {
            throw new RuntimeException("Non hai accesso a questo appuntamento");
        }
        
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Puoi completare solo appuntamenti confermati");
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        
        log.info("✅ Appuntamento {} completato dal nutrizionista {}", appointmentId, nutritionistId);
        
        return ResponseEntity.ok("✅ Appuntamento marcato come completato");
    }
}