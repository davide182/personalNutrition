package com.nutritionists.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.dto.request.NutritionalPlanRequest;
import com.nutritionists.model.dto.response.NutritionalPlanResponse;
import com.nutritionists.service.NutritionalPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/nutritionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NUTRITIONIST')")
public class NutritionalPlanController {
    
    private final NutritionalPlanService nutritionalPlanService;
    
    @PostMapping("/nutritional-plans")
    public ResponseEntity<NutritionalPlanResponse> createPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NutritionalPlanRequest request) {
        
        String email = userDetails.getUsername();
        NutritionalPlanResponse response = nutritionalPlanService.createPlan(email, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/nutritional-plans/{planId}")
    public ResponseEntity<NutritionalPlanResponse> updatePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long planId,
            @Valid @RequestBody NutritionalPlanRequest request) {
        
        String email = userDetails.getUsername();
        NutritionalPlanResponse response = nutritionalPlanService.updatePlan(email, planId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/appointments/{appointmentId}/nutritional-plan")
    public ResponseEntity<NutritionalPlanResponse> getPlanByAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        
        String email = userDetails.getUsername();
        NutritionalPlanResponse response = nutritionalPlanService.getPlanByAppointment(email, appointmentId);
        return ResponseEntity.ok(response);
    }
}