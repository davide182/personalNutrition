package com.nutritionists.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.dto.response.NutritionalPlanResponse;
import com.nutritionists.service.NutritionalPlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientNutritionalPlanController {
    
    private final NutritionalPlanService nutritionalPlanService;
    
    @GetMapping("/appointments/{appointmentId}/nutritional-plan")
    public ResponseEntity<NutritionalPlanResponse> getMyPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        
        String email = userDetails.getUsername();
        NutritionalPlanResponse response = nutritionalPlanService.getPlanByAppointmentForPatient(email, appointmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nutritional-plans")
    public ResponseEntity<List<NutritionalPlanResponse>> getPatientNutritionalPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<NutritionalPlanResponse> plans = nutritionalPlanService.getPlansByPatientEmail(email);
        return ResponseEntity.ok(plans);
    }
}