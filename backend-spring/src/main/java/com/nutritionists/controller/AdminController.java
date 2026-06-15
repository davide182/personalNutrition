package com.nutritionists.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.dto.response.AdminAppointmentResponse;
import com.nutritionists.model.dto.response.AdminUserResponse;
import com.nutritionists.model.dto.request.SpecializationRequest;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.Specialization;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.SpecializationRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/pending-nutritionists")
    public ResponseEntity<List<AdminUserResponse>> getPendingNutritionists() {
        List<User> pendingUsers = userRepository.findByRoleAndStatus(Role.NUTRITIONIST, UserStatus.PENDING);
        
        List<AdminUserResponse> response = pendingUsers.stream()
                .map(user -> AdminUserResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .firstName(user.getUserProfile().getFirstName())
                        .lastName(user.getUserProfile().getLastName())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/disabled-nutritionists")
    public ResponseEntity<List<AdminUserResponse>> getDisabledNutritionists() {
        List<User> disabledUsers = userRepository.findByRoleAndStatus(Role.NUTRITIONIST, UserStatus.DISABLED);
        
        List<AdminUserResponse> response = disabledUsers.stream()
                .map(user -> AdminUserResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .firstName(user.getUserProfile().getFirstName())
                        .lastName(user.getUserProfile().getLastName())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/approve-nutritionist/{userId}")
    public ResponseEntity<String> approveNutritionist(@PathVariable Long userId) {
        adminService.approveNutritionist(userId);
        return ResponseEntity.ok("✅ Nutrizionista approvato con successo. Email di notifica inviata.");
    }

    @PutMapping("/reject-nutritionist/{userId}")
    public ResponseEntity<String> rejectNutritionist(@PathVariable Long userId) {
        adminService.rejectNutritionist(userId);
        return ResponseEntity.ok("❌ Nutrizionista respinto e disabilitato. Email di notifica inviata.");
    }
    
    @PutMapping("/reenable-nutritionist/{userId}")
    public ResponseEntity<String> reenableDisabledNutritionist(@PathVariable Long userId) {
        adminService.reenableDisabledNutritionist(userId);
        return ResponseEntity.ok("🔄 Nutrizionista disabilitato riportato in stato PENDING. Ora può essere riapprovato.");
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<User> allUsers = userRepository.findAll();
        
        List<AdminUserResponse> response = allUsers.stream()
                .map(user -> {
                    String firstName = user.getUserProfile() != null ? user.getUserProfile().getFirstName() : "N/A";
                    String lastName = user.getUserProfile() != null ? user.getUserProfile().getLastName() : "N/A";
                    return AdminUserResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .firstName(firstName)
                        .lastName(lastName)
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build();
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/suspend-user/{userId}")
    public ResponseEntity<String> suspendUser(@PathVariable Long userId) {
        adminService.suspendUser(userId);
        return ResponseEntity.ok("⏸️ Utente sospeso con successo.");
    }

    @PutMapping("/activate-user/{userId}")
    public ResponseEntity<String> activateUser(@PathVariable Long userId) {
        adminService.activateUser(userId);
        return ResponseEntity.ok("▶️ Utente riattivato con successo.");
    }
    
    @PostMapping("/specializations")
    public ResponseEntity<Specialization> addSpecialization(@RequestBody SpecializationRequest request) {
        if (specializationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Specializzazione già esistente: " + request.getName());
        }
        
        Specialization spec = Specialization.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        Specialization saved = specializationRepository.save(spec);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok(specializationRepository.findAll());
    }


    @GetMapping("/appointments")
    public ResponseEntity<List<AdminAppointmentResponse>> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        
        List<AdminAppointmentResponse> response = appointments.stream()
                .map(appointment -> {
                    // Dati paziente
                    String patientFirstName = "N/A";
                    String patientLastName = "N/A";
                    if (appointment.getUser() != null && appointment.getUser().getUserProfile() != null) {
                        patientFirstName = appointment.getUser().getUserProfile().getFirstName();
                        patientLastName = appointment.getUser().getUserProfile().getLastName();
                    }
                    
                    // Dati nutrizionista
                    String nutritionistFirstName = "";
                    String nutritionistLastName = "";
                    if (appointment.getNutritionist() != null 
                            && appointment.getNutritionist().getUser() != null
                            && appointment.getNutritionist().getUser().getUserProfile() != null) {
                        nutritionistFirstName = appointment.getNutritionist().getUser().getUserProfile().getFirstName();
                        nutritionistLastName = appointment.getNutritionist().getUser().getUserProfile().getLastName();
                    }
                    
                    //Traduzione dello stato in italiano
                    String statusItalian = translateStatus(appointment.getStatus());
                    
                    return AdminAppointmentResponse.builder()
                            .appointmentId(appointment.getAppointmentId())
                            .startTime(appointment.getStartTime())
                            .endTime(appointment.getEndTime())
                            .status(statusItalian) 
                            .patientFirstName(patientFirstName)
                            .patientLastName(patientLastName)
                            .nutritionistFirstName(nutritionistFirstName)
                            .nutritionistLastName(nutritionistLastName)
                            .location(appointment.getLocation())
                            .price(appointment.getPrice())
                            .build();
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    private String translateStatus(AppointmentStatus status) {
        if (status == null) return "SCONOSCIUTO";
        
        switch (status) {
            case PENDING_PROPOSAL:
                return "IN ATTESA";
            case PROPOSED:
                return "PROPOSTO";
            case CONFIRMED:
                return "CONFERMATO";
            case COMPLETED:
                return "COMPLETATO";
            case CANCELLED:
                return "CANCELLATO";
            case FAILED:
                return "FALLITO";
            default:
                return status.name();
        }
    }
}