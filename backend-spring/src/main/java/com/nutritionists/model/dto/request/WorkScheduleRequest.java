package com.nutritionists.model.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleRequest {
    
    @NotNull(message = "Il giorno della settimana è obbligatorio")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "L'ora di inizio è obbligatoria")
    private LocalTime startTime;
    
    @NotNull(message = "L'ora di fine è obbligatoria")
    private LocalTime endTime;
}
