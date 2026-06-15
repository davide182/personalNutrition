// Crea un nuovo controller o aggiungi a UserController.java
package com.nutritionists.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.dto.request.DisableNutritionistRequest;
import com.nutritionists.model.dto.response.AdminUserResponse;
import com.nutritionists.model.entity.User;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.service.DisableProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final DisableProfileService disableProfileService;
    private final UserRepository userRepository;


    @PostMapping("/disable")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> disablePatientProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        disableProfileService.disablePatientProfile(user.getUserId());
        return ResponseEntity.ok("Profilo disabilitato con successo. Contatta l'amministratore per riattivarlo.");
    }


    @PostMapping("/disable/request")
    @PreAuthorize("hasRole('NUTRITIONIST')")
    public ResponseEntity<String> requestNutritionistDisable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DisableNutritionistRequest request) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        disableProfileService.requestNutritionistDisable(user.getUserId(), request);
        return ResponseEntity.ok("Richiesta di disabilitazione inviata all'amministratore. Riceverai una email di conferma.");
    }

 
    @PutMapping("/admin/disable-nutritionist/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveNutritionistDisable(@PathVariable Long userId) {
        disableProfileService.approveNutritionistDisable(userId);
        return ResponseEntity.ok("Disabilitazione del nutrizionista approvata");
    }


    @PutMapping("/admin/disable-nutritionist/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejectNutritionistDisable(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        disableProfileService.rejectNutritionistDisable(userId, reason != null ? reason : "Nessun motivo specificato");
        return ResponseEntity.ok("Richiesta di disabilitazione negata");
    }


    @PutMapping("/admin/reenable/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reenableProfile(@PathVariable Long userId) {
        disableProfileService.reenableProfile(userId);
        return ResponseEntity.ok("Profilo riabilitato con successo");
    }

    @GetMapping("/admin/pending-disable-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getPendingDisableRequests() {
        List<User> pendingRequests = disableProfileService.getPendingDisableRequests();
        
        List<AdminUserResponse> response = pendingRequests.stream()
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

    @GetMapping("/admin/self-disabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getSelfDisabledProfiles() {
        List<User> disabledProfiles = disableProfileService.getSelfDisabledProfiles();
        
        List<AdminUserResponse> response = disabledProfiles.stream()
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
}