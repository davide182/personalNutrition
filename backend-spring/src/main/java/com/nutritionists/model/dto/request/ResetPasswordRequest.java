package com.nutritionists.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    
    @NotBlank(message = "Il token è obbligatorio")
    private String token;
    
    @NotBlank(message = "La password è obbligatoria")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",message = "La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola, un numero e un carattere speciale")
    private String newPassword;
}