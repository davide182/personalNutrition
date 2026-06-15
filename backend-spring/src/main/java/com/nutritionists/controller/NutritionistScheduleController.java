package com.nutritionists.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.nutritionists.model.dto.request.WorkScheduleRequest;
import com.nutritionists.model.dto.response.WorkScheduleResponse;
import com.nutritionists.model.entity.Nutritionist;
import com.nutritionists.model.entity.WorkSchedule;
import com.nutritionists.repository.NutritionistRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.repository.WorkScheduleRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/nutritionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NUTRITIONIST')")
public class NutritionistScheduleController {
    
    private final UserRepository userRepository;
    private final NutritionistRepository nutritionistRepository;
    private final WorkScheduleRepository workScheduleRepository;

    private String getDayName(String day) {
        Map<String, String> days = Map.of(
            "MONDAY", "Lunedì",
            "TUESDAY", "Martedì",
            "WEDNESDAY", "Mercoledì",
            "THURSDAY", "Giovedì",
            "FRIDAY", "Venerdì",
            "SATURDAY", "Sabato",
            "SUNDAY", "Domenica"
        );
        return days.getOrDefault(day, day);
    }
    
    @PostMapping("/schedule")
    public ResponseEntity<WorkScheduleResponse> addWorkSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkScheduleRequest request) {
        
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        Nutritionist nutritionist = nutritionistRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Profilo nutrizionista non trovato"));
        
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("L'ora di fine deve essere dopo l'ora di inizio");
        }
        
        boolean exists = workScheduleRepository
                .findByNutritionist_NutritionistIdAndDayOfWeek(
                    nutritionist.getNutritionistId(), 
                    request.getDayOfWeek())
                .stream()
                .anyMatch(schedule -> 
                    schedule.getStartTime().equals(request.getStartTime()) &&
                    schedule.getEndTime().equals(request.getEndTime()));
        
        if (exists) {
            throw new RuntimeException("Orario già esistente per " + getDayName(request.getDayOfWeek().toString()));
        }
        
        WorkSchedule schedule = WorkSchedule.builder()
                .nutritionist(nutritionist)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
        
        WorkSchedule saved = workScheduleRepository.save(schedule);
        
        return ResponseEntity.ok(WorkScheduleResponse.builder()
                .workScheduleId(saved.getWorkScheduleId())
                .dayOfWeek(saved.getDayOfWeek())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .build());
    }
    
    @GetMapping("/schedule")
    public ResponseEntity<List<WorkScheduleResponse>> getMySchedule(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        Nutritionist nutritionist = nutritionistRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Profilo nutrizionista non trovato"));
        
        List<WorkSchedule> schedules = workScheduleRepository
                .findByNutritionist_NutritionistId(nutritionist.getNutritionistId());
        
        List<WorkScheduleResponse> response = schedules.stream()
                .map(schedule -> WorkScheduleResponse.builder()
                        .workScheduleId(schedule.getWorkScheduleId())
                        .dayOfWeek(schedule.getDayOfWeek())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    

    @DeleteMapping("/schedule/{scheduleId}")
    public ResponseEntity<String> deleteWorkSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long scheduleId) {
        
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"))
                .getUserId();
        
        Nutritionist nutritionist = nutritionistRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Profilo nutrizionista non trovato"));
        
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Orario non trovato"));
        
        if (!schedule.getNutritionist().getNutritionistId().equals(nutritionist.getNutritionistId())) {
            throw new RuntimeException("Non hai accesso a questo orario");
        }
        
        workScheduleRepository.delete(schedule);
        
        return ResponseEntity.ok("Orario eliminato con successo");
    }
}