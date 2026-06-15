package com.nutritionists.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.client.GeoDistanceClient;
import com.nutritionists.model.dto.NutritionistGeoDto;
import com.nutritionists.model.dto.request.GeosortRequest;
import com.nutritionists.model.dto.response.GeosortResponse;
import com.nutritionists.model.dto.response.NutritionistNearbyResponse;
import com.nutritionists.model.entity.Specialization;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class NutritionistController {

    private final UserRepository userRepository;
    private final GeoDistanceClient geoDistanceClient;

    @GetMapping("/nutritionists/nearby")
    @PreAuthorize("hasRole('PATIENT')") 
    public ResponseEntity<List<NutritionistNearbyResponse>> getNearbyNutritionists(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "999999") double radius) {

        log.info("========================================");
        log.info("🔍🔍🔍 CHIAMATA RICEVUTA a /nutritionists/nearby");
        log.info("🔍 lat={}, lon={}", lat, lon);
        log.info("========================================");
        
        // Verifica che l'utente sia autenticato
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🔍 Authentication: {}", auth);
        log.info("🔍 Is Authenticated: {}", auth != null ? auth.isAuthenticated() : false);
        
        List<User> activeNutritionists = userRepository.findByRoleAndStatus(Role.NUTRITIONIST, UserStatus.ACTIVE);
        
        log.info("🔍 Nutrizionisti attivi trovati: {}", activeNutritionists.size());
        
        if (activeNutritionists.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        List<NutritionistGeoDto> nutritionistGeoList = activeNutritionists.stream()
                .map(user -> {
                    UserProfile profile = user.getUserProfile();
                    return NutritionistGeoDto.builder()
                            .id(user.getUserId())
                            .lat(profile.getLatitude())
                            .lon(profile.getLongitude())
                            .build();
                })
                .collect(Collectors.toList());
        
        GeosortRequest geoRequest = GeosortRequest.builder()
                .patientLat(lat)
                .patientLon(lon)
                .nutritionists(nutritionistGeoList)
                .build();
        
        GeosortResponse geoResponse = geoDistanceClient.sortNutritionistsByDistance(geoRequest);
        
        if (geoResponse == null || geoResponse.getSortedIds() == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        List<NutritionistNearbyResponse> response = new ArrayList<>();
        
        for (int i = 0; i < geoResponse.getSortedIds().size(); i++) {
            Long nutritionistId = geoResponse.getSortedIds().get(i);
            Double distance = geoResponse.getDistances().get(i);
            
            User nutritionist = userRepository.findById(nutritionistId).orElse(null);
            if (nutritionist != null && nutritionist.getUserProfile() != null) {
                
                List<String> specializations = new ArrayList<>();
                if (nutritionist.getNutritionist() != null && nutritionist.getNutritionist().getSpecializations() != null) {
                    specializations = nutritionist.getNutritionist().getSpecializations().stream()
                            .map(Specialization::getName)
                            .collect(Collectors.toList());
                }
                
                response.add(NutritionistNearbyResponse.builder()
                        .id(nutritionistId)
                        .name("Dott./Dott.ssa " + nutritionist.getUserProfile().getLastName())
                        .firstName(nutritionist.getUserProfile().getFirstName())
                        .lastName(nutritionist.getUserProfile().getLastName())
                        .latitude(nutritionist.getUserProfile().getLatitude())
                        .longitude(nutritionist.getUserProfile().getLongitude())
                        .distance(distance)
                        .bio(nutritionist.getNutritionist() != null ? nutritionist.getNutritionist().getBio() : null)
                        .specializations(specializations)
                        .build());
            }
        }
        
        log.info("Trovati {} nutrizionisti", response.size());
        return ResponseEntity.ok(response);
    }
}