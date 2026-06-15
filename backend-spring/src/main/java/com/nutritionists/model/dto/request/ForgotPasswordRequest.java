package com.nutritionists.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {
    
    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Inserisci un'email valida")
    private String email;
}