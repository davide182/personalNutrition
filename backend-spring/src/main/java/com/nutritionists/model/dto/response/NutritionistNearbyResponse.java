package com.nutritionists.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionistNearbyResponse {
    private Long id;
    private String name;
    private String firstName;
    private String lastName;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private String bio;
    private List<String> specializations;
}