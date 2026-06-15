package com.nutritionists.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutritionists.model.dto.request.DisableNutritionistRequest;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisableProfileService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final LoggingService loggingService;

    @Transactional
    public void disablePatientProfile(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));

        if (patient.getRole() != Role.PATIENT) {
            throw new RuntimeException("Solo i pazienti possono disabilitare il proprio profilo");
        }

        if (patient.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Il profilo non è attivo. Stato attuale: " + patient.getStatus());
        }

        int activeAppointments = appointmentRepository.countActiveAppointmentsByPatient(patientId);
        if (activeAppointments > 0) {
            throw new RuntimeException("Non puoi disabilitare il profilo perché hai " + activeAppointments + 
                    " appuntamenti attivi o in sospeso. Annulla o completa gli appuntamenti prima di procedere.");
        }

        patient.setStatus(UserStatus.SELF_DISABLED);
        userRepository.save(patient);

        log.info("🔴 Paziente {} ha disabilitato volontariamente il proprio profilo", patient.getEmail());
        
        loggingService.info("PROFILE", "SELF_DISABLE", String.valueOf(patientId), patient.getEmail(), 
            "Paziente ha disabilitato il proprio profilo");

        emailService.sendProfileDisabledEmail(patient.getEmail(), patient.getUserProfile().getFirstName());

    }


    @Transactional
    public void requestNutritionistDisable(Long nutritionistId, DisableNutritionistRequest request) {
        User nutritionist = userRepository.findById(nutritionistId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));

        if (nutritionist.getRole() != Role.NUTRITIONIST) {
            throw new RuntimeException("Solo i nutrizionisti possono richiedere la disabilitazione");
        }

        if (nutritionist.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Il profilo non è attivo. Stato attuale: " + nutritionist.getStatus());
        }

        int activeAppointments = appointmentRepository.countActiveAppointmentsByNutritionist(nutritionistId);
        if (activeAppointments > 0) {
            log.warn("Nutrizionista {} ha richiesto disabilitazione ma ha {} appuntamenti attivi", 
                nutritionist.getEmail(), activeAppointments);
        }

        nutritionist.setStatus(UserStatus.SUSPENDED);
        userRepository.save(nutritionist);

        log.info("⏳ Nutrizionista {} ha RICHIESTO la disabilitazione del profilo. Motivo: {}", 
            nutritionist.getEmail(), request.getReason());
        
        loggingService.info("PROFILE", "DISABLE_REQUEST", String.valueOf(nutritionistId), nutritionist.getEmail(), 
            "Nutrizionista ha richiesto disabilitazione. Motivo: " + (request.getReason() != null ? request.getReason() : "Nessun motivo specificato"));

        emailService.notifyAdminNutritionistDisableRequest(nutritionist, request.getReason());
    }

    @Transactional
    public void approveNutritionistDisable(Long nutritionistId) {
        User nutritionist = userRepository.findById(nutritionistId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));

        if (nutritionist.getRole() != Role.NUTRITIONIST) {
            throw new RuntimeException("Solo i nutrizionisti possono essere disabilitati");
        }

        if (nutritionist.getStatus() != UserStatus.SUSPENDED) {
            throw new RuntimeException("Questo nutrizionista non ha richiesto la disabilitazione. Stato attuale: " + nutritionist.getStatus());
        }

        nutritionist.setStatus(UserStatus.SELF_DISABLED);
        userRepository.save(nutritionist);

        log.info("✅ Admin ha APPROVATO la disabilitazione del nutrizionista {}", nutritionist.getEmail());
        
        loggingService.info("PROFILE", "DISABLE_APPROVED", String.valueOf(nutritionistId), nutritionist.getEmail(), 
            "Admin ha approvato la disabilitazione del nutrizionista");

        emailService.sendProfileDisabledEmail(nutritionist.getEmail(), nutritionist.getUserProfile().getFirstName());
    }


    @Transactional
    public void rejectNutritionistDisable(Long nutritionistId, String reason) {
        User nutritionist = userRepository.findById(nutritionistId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));

        if (nutritionist.getRole() != Role.NUTRITIONIST) {
            throw new RuntimeException("Solo i nutrizionisti possono essere disabilitati");
        }

        if (nutritionist.getStatus() != UserStatus.SUSPENDED) {
            throw new RuntimeException("Questo nutrizionista non ha richiesto la disabilitazione");
        }


        nutritionist.setStatus(UserStatus.ACTIVE);
        userRepository.save(nutritionist);

        log.info("❌ Admin ha NEGATO la disabilitazione del nutrizionista {}. Motivo: {}", 
            nutritionist.getEmail(), reason);
        
        loggingService.info("PROFILE", "DISABLE_REJECTED", String.valueOf(nutritionistId), nutritionist.getEmail(), 
            "Admin ha negato la disabilitazione. Motivo: " + reason);

  
        emailService.sendProfileDisableRejectedEmail(nutritionist.getEmail(), 
            nutritionist.getUserProfile().getFirstName(), reason);
    }

    @Transactional
    public void reenableProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (user.getStatus() != UserStatus.SELF_DISABLED) {
            throw new RuntimeException("Questo profilo non è disabilitato volontariamente. Stato attuale: " + user.getStatus());
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("🟢 Admin ha RIABILITATO il profilo di {}", user.getEmail());
        
        loggingService.info("PROFILE", "REENABLE", String.valueOf(userId), user.getEmail(), 
            "Admin ha riabilitato il profilo");


        emailService.sendProfileReenabledEmail(user.getEmail(), user.getUserProfile().getFirstName());
    }

    @Transactional(readOnly = true)
    public List<User> getPendingDisableRequests() {
        return userRepository.findByStatus(UserStatus.SUSPENDED);
    }

    @Transactional(readOnly = true)
    public List<User> getSelfDisabledProfiles() {
        return userRepository.findByStatus(UserStatus.SELF_DISABLED);
    }
}