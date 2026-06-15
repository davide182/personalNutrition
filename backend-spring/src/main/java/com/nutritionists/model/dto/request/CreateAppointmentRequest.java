package com.nutritionists.model.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppointmentRequest {
    
    @NotNull(message = "Data e ora inizio obbligatoria")
    @Future(message = "La data deve essere futura")
    private LocalDateTime startTime;
    
    @NotNull(message = "Data e ora fine obbligatoria")
    @Future(message = "La data deve essere futura")
    private LocalDateTime endTime;
    
    private Long specializationId;
    
    private Long nutritionistId;
}