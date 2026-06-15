package com.nutritionists.model.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nutritionists.model.dto.NutritionistGeoDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeosortRequest {

    @JsonProperty("patient_lat")//json property per mappare il campo lat del paziente
    private Double patientLat;

    @JsonProperty("patient_lon")
    private Double patientLon;

    @JsonProperty("nutritionists")
    private List<NutritionistGeoDto> nutritionists;
    

}