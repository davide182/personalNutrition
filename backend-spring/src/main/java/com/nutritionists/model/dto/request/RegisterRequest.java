package com.nutritionists.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.nutritionists.model.entity.enums.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Email obbligatoria")
    @Email(message = "Formato email non valido")
    private String email;

    @NotBlank(message = "Password obbligatoria")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",message = "La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola, un numero e un carattere speciale")
    private String password;

    @NotBlank(message = "Il nome è obbligatorio")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String lastName;

    @NotNull(message = "Ruolo obbligatorio")
    private Role role;
    
    @NotBlank(message = "L'indirizzo è obbligatorio")
    private String address;
    
    private Double latitude;
    private Double longitude;

    private String bio;
    private Double serviceRadiusKm;
    
    private Long specializationId;
}