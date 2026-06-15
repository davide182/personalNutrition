package com.nutritionists.service;

import com.nutritionists.model.dto.request.PatientHealthDataRequest;
import com.nutritionists.model.dto.response.PatientHealthDataResponse;
import com.nutritionists.model.entity.PatientHealthData;
import com.nutritionists.model.entity.User;
import com.nutritionists.repository.PatientHealthDataRepository;
import com.nutritionists.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientHealthDataService {

    private final PatientHealthDataRepository healthDataRepository;
    private final UserRepository userRepository;
    private final LoggingService loggingService;

    @Transactional
    public PatientHealthDataResponse createHealthData(String email, PatientHealthDataRequest request) {
    
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (healthDataRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new RuntimeException("Dati salute già esistenti per questo utente. Usa l'update.");
        }

        PatientHealthData healthData = PatientHealthData.builder()
                .user(user)
                .weight(request.getWeight())
                .height(request.getHeight())
                .allergies(request.getAllergies())
                .goals(request.getGoals())
                .build();

        PatientHealthData saved = healthDataRepository.save(healthData);
        
        double bmi = calculateBmi(request.getWeight(), request.getHeight());
        loggingService.info("HEALTH_DATA", "CREATE", String.valueOf(user.getUserId()), email, 
            "Dati salute creati - BMI: " + bmi);

        return mapToResponse(saved, bmi);
    }

    @Transactional
    public PatientHealthDataResponse updateHealthData(String email, PatientHealthDataRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        PatientHealthData healthData = healthDataRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Dati salute non trovati"));


        healthData.setWeight(request.getWeight());
        healthData.setHeight(request.getHeight());
        healthData.setAllergies(request.getAllergies());
        healthData.setGoals(request.getGoals());

        PatientHealthData saved = healthDataRepository.save(healthData);
        
        double bmi = calculateBmi(request.getWeight(), request.getHeight());
        loggingService.info("HEALTH_DATA", "UPDATE", String.valueOf(user.getUserId()), email, 
            "Dati salute aggiornati - BMI: " + bmi);

        return mapToResponse(saved, bmi);
    }

    @Transactional(readOnly = true)
    public PatientHealthDataResponse getHealthData(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

  
        PatientHealthData healthData = healthDataRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Dati salute non trovati"));

        double bmi = calculateBmi(healthData.getWeight(), healthData.getHeight());
        return mapToResponse(healthData, bmi);
    }

    private double calculateBmi(Double weight, Double height) {
        if (weight == null || height == null || height == 0) {
            return 0;
        }
        double heightM = height / 100;
        return Math.round((weight / (heightM * heightM)) * 100.0) / 100.0;
    }

    private PatientHealthDataResponse mapToResponse(PatientHealthData data, double bmi) {
        return PatientHealthDataResponse.builder()
                .patientDataId(data.getPatientDataId())
                .userId(data.getUser().getUserId())
                .weight(data.getWeight())
                .height(data.getHeight())
                .allergies(data.getAllergies())
                .goals(data.getGoals())
                .bmi(bmi)
                .build();
    }
}