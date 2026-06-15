package com.nutritionists.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutritionists.model.dto.request.NutritionalPlanRequest;
import com.nutritionists.model.dto.response.NutritionalPlanResponse;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.NutritionalPlan;
import com.nutritionists.model.entity.User;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.NutritionalPlanRepository;
import com.nutritionists.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionalPlanService {
    
    private final NutritionalPlanRepository nutritionalPlanRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public NutritionalPlanResponse createPlan(String nutritionistEmail, NutritionalPlanRequest request) {
        
        User nutritionist = userRepository.findByEmail(nutritionistEmail)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
        
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        

        if (appointment.getNutritionist() == null || 
            !appointment.getNutritionist().getUser().getUserId().equals(nutritionist.getUserId())) {
            throw new RuntimeException("Non hai accesso a questo appuntamento");
        }
        
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Puoi creare un piano nutrizionale solo dopo aver completato la visita");
        }
        
        if (nutritionalPlanRepository.findByAppointment_AppointmentId(request.getAppointmentId()).isPresent()) {
            throw new RuntimeException("Piano nutrizionale già esistente per questo appuntamento");
        }
        
        NutritionalPlan plan = NutritionalPlan.builder()
                .appointment(appointment)
                .diagnosis(request.getDiagnosis())
                .recommendations(request.getRecommendations())
                .build();
        
        NutritionalPlan saved = nutritionalPlanRepository.save(plan);
        
        log.info("Piano nutrizionale creato per appuntamento {} dal nutrizionista {}", 
            request.getAppointmentId(), nutritionistEmail);
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public NutritionalPlanResponse updatePlan(String nutritionistEmail, Long planId, NutritionalPlanRequest request) {
        
        User nutritionist = userRepository.findByEmail(nutritionistEmail)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
        
        NutritionalPlan plan = nutritionalPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Piano nutrizionale non trovato"));
        
        Appointment appointment = plan.getAppointment();
        
        if (appointment.getNutritionist() == null || 
            !appointment.getNutritionist().getUser().getUserId().equals(nutritionist.getUserId())) {
            throw new RuntimeException("Non hai accesso a questo piano nutrizionale");
        }
        
        plan.setDiagnosis(request.getDiagnosis());
        plan.setRecommendations(request.getRecommendations());
        
        NutritionalPlan saved = nutritionalPlanRepository.save(plan);
        
        log.info("Piano nutrizionale {} aggiornato per appuntamento {}", 
            planId, appointment.getAppointmentId());
        
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public NutritionalPlanResponse getPlanByAppointment(String nutritionistEmail, Long appointmentId) {
        
        User nutritionist = userRepository.findByEmail(nutritionistEmail)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        
        if (appointment.getNutritionist() == null || 
            !appointment.getNutritionist().getUser().getUserId().equals(nutritionist.getUserId())) {
            throw new RuntimeException("Non hai accesso a questo appuntamento");
        }
        
        NutritionalPlan plan = nutritionalPlanRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Piano nutrizionale non trovato per questo appuntamento"));
        
        return mapToResponse(plan);
    }
    
    @Transactional(readOnly = true)
    public NutritionalPlanResponse getPlanByAppointmentForPatient(String patientEmail, Long appointmentId) {
        
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        
        if (!appointment.getUser().getUserId().equals(patient.getUserId())) {
            throw new RuntimeException("Non hai accesso a questo appuntamento");
        }
        
        NutritionalPlan plan = nutritionalPlanRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Piano nutrizionale non ancora disponibile"));
        
        return mapToResponse(plan);
    }
    
    private NutritionalPlanResponse mapToResponse(NutritionalPlan plan) {
        return NutritionalPlanResponse.builder()
                .planId(plan.getNutritionistPlanId())
                .appointmentId(plan.getAppointment().getAppointmentId())
                .diagnosis(plan.getDiagnosis())
                .recommendations(plan.getRecommendations())
                .createdAt(plan.getCreatedAtPlan().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public List<NutritionalPlanResponse> getPlansByPatientEmail(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));
        
        List<NutritionalPlan> plans = nutritionalPlanRepository.findAllByAppointment_User_UserId(patient.getUserId());
        
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}