package com.nutritionists.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHealthDataResponse {
    private Long patientDataId;
    private Long userId;
    private Double weight;
    private Double height;
    private String allergies;
    private String goals;
    private Double bmi; 
}