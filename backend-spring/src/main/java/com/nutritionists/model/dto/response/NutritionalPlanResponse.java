package com.nutritionists.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionalPlanResponse {
    private Long planId;
    private Long appointmentId;
    private String diagnosis;
    private String recommendations;
    private String createdAt;
}
