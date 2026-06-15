package com.nutritionists.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionistProposalDto {
    private Long appointmentId;
    private String patientFirstName;
    private String patientLastName;
    private String patientEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double patientLatitude;
    private Double patientLongitude;
    private Double distanceKm;
    private Integer positionInQueue;  
}