package com.nutritionists.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class NutritionistGeoDto {
        private Long id;
        private Double lat;
        private Double lon;
    }