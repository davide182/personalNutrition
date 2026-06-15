package com.nutritionists.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.nutritionists.model.dto.request.PatientHealthDataRequest;
import com.nutritionists.model.dto.response.PatientHealthDataResponse;
import com.nutritionists.service.PatientHealthDataService;
import com.nutritionists.service.AppointmentProposalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientHealthDataController {
    
    private final PatientHealthDataService healthDataService;
    private final AppointmentProposalService appointmentProposalService;

    @PostMapping("/health-data")
    public ResponseEntity<PatientHealthDataResponse> createHealthData(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatientHealthDataRequest request) {
        
        String email = userDetails.getUsername();
        log.info("Tentativo creazione dati salute per: {}", email);
        
        PatientHealthDataResponse response = healthDataService.createHealthData(email, request);
        return ResponseEntity.ok(response);
    }
    

    @PutMapping("/health-data")
    public ResponseEntity<PatientHealthDataResponse> updateHealthData(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatientHealthDataRequest request) {
        
        String email = userDetails.getUsername();
        log.info("Tentativo aggiornamento dati salute per: {}", email);
        
        PatientHealthDataResponse response = healthDataService.updateHealthData(email, request);
        return ResponseEntity.ok(response);
    }
    

    @GetMapping("/health-data")
    public ResponseEntity<PatientHealthDataResponse> getHealthData(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        log.info("Tentativo recupero dati salute per: {}", email);
        
        PatientHealthDataResponse response = healthDataService.getHealthData(email);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/bmi")
    public ResponseEntity<?> calculateBMI(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("Tentativo calcolo BMI per: {}", email);
        
        PatientHealthDataResponse healthData = healthDataService.getHealthData(email);
        
        if (healthData.getWeight() == null || healthData.getHeight() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Inserisci prima peso e altezza"));
        }
        
        Map<String, Object> bmiData = appointmentProposalService.calculateBMI(
            healthData.getWeight(),
            healthData.getHeight(),
            30,
            "M",
            "moderate"
        );
        
        return ResponseEntity.ok(bmiData);
    }
}