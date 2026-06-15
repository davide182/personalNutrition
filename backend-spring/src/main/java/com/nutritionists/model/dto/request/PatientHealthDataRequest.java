package com.nutritionists.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHealthDataRequest {
    
    @NotNull(message = "Il peso è obbligatorio")
    @Positive(message = "Il peso deve essere maggiore di 0")
    @Min(value = 1, message = "Il peso deve essere almeno 1 kg")
    private Double weight;
    
    @NotNull(message = "L'altezza è obbligatoria")
    @Positive(message = "L'altezza deve essere maggiore di 0")
    @Min(value = 50, message = "L'altezza deve essere almeno 50 cm")
    private Double height;
    
    private String allergies;
    private String goals;
}