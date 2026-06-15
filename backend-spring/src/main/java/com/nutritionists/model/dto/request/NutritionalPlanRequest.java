package com.nutritionists.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionalPlanRequest {
    
    @NotNull(message = "ID appuntamento obbligatorio")
    private Long appointmentId;
    
    private String diagnosis;
    
    @NotBlank(message = "Raccomandazioni obbligatorie")
    private String recommendations;
}