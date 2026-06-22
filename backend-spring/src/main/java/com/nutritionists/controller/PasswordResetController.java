package com.nutritionists.controller;

import com.nutritionists.model.dto.request.ForgotPasswordRequest;
import com.nutritionists.model.dto.request.ResetPasswordRequest;
import com.nutritionists.model.dto.response.ResetPasswordResponse;
import com.nutritionists.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        
        return ResponseEntity.ok(Map.of(
            "message", "Se l'email è registrata, riceverai un link per il reset della password"
        ));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<Map<String, Boolean>> verifyResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordResponse response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}