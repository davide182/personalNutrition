package com.nutritionists.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.model.dto.response.UserProfileResponse;
import com.nutritionists.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        log.info("Recupero profilo per: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        UserProfile profile = user.getUserProfile();
        
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .role(user.getRole())
                .build();
        
        return ResponseEntity.ok(response);
    }

        @GetMapping("/status")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getUserStatus(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        boolean isActive = user.getStatus() == UserStatus.ACTIVE;
        
        return ResponseEntity.ok(Map.of(
                "active", isActive,
                "status", user.getStatus().name(),
                "message", isActive ? "Account attivo" : "Account non attivo"
        ));
        }
}