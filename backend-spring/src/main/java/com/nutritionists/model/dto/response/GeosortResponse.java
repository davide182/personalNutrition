package com.nutritionists.model.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeosortResponse {
    
    @JsonProperty("sorted_ids")
    @JsonAlias({"sortedIds", "sorted_ids"})
    private List<Long> sortedIds;
    
    @JsonProperty("distances")
    @JsonAlias({"distances"})
    private List<Double> distances;
    
    @JsonProperty("durations")
    @JsonAlias({"durations"})
    private List<Double> durations;
}