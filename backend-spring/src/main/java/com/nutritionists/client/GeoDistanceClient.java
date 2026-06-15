package com.nutritionists.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritionists.model.dto.request.GeosortRequest;
import com.nutritionists.model.dto.response.GeosortResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeoDistanceClient {
    
    private final RestTemplate restTemplate;
    private final String flaskUrl;
    
    public GeoDistanceClient(@Value("${flask.api.url:http://localhost:5000}") String flaskUrl) {
        this.restTemplate = new RestTemplate();
        this.flaskUrl = flaskUrl;
    }
    
    public GeosortResponse sortNutritionistsByDistance(GeosortRequest request) {
        try {
            log.info("Chiamata Flask per ordinamento nutrizionisti: patient({}, {})", 
                request.getPatientLat(), request.getPatientLon());
            
            String url = flaskUrl + "/api/geosort";
            
            String jsonResponse = restTemplate.postForObject(url, request, String.class);
            log.info("Risposta JSON grezza da Flask: {}", jsonResponse);
            
            ObjectMapper mapper = new ObjectMapper();
            GeosortResponse response = mapper.readValue(jsonResponse, GeosortResponse.class);
            
            log.info("SortedIds dopo deserializzazione: {}", response.getSortedIds());
            
            return response;
            
        } catch (Exception e) {
            log.error("Errore chiamata Flask: {}", e.getMessage(), e);
            throw new RuntimeException("Servizio di geolocalizzazione non disponibile", e);
        }
    }
}