package com.nutritionists.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${geocoding.nominatim.useragent:NutritionistsApp/1.0 (contact: admin@nutritionists.it)}")
    private String userAgent;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Converte un indirizzo in coordinate (latitudine, longitudine)
     * @param address Indirizzo completo (es. "Via Roma 1, Milano")
     * @return array [latitudine, longitudine] o null se non trovato
     */
    public double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("Indirizzo vuoto per geocoding");
            return null;
        }

        try {
            // Costruzione URL
            URI uri = UriComponentsBuilder
                    .fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("addressdetails", 0)
                    .build()
                    .toUri();

            log.info("Geocoding indirizzo: {}", address);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.set("Accept", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            String response = responseEntity.getBody();
            log.debug("Risposta Nominatim: {}", response);
            
            JsonNode results = objectMapper.readTree(response);

            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();
                String displayName = firstResult.has("display_name") ? firstResult.get("display_name").asText() : address;
                log.info("✅ Indirizzo geocodificato: {} -> lat={}, lon={}", displayName, lat, lon);
                return new double[]{lat, lon};
            } else {
                log.warn("❌ Nessun risultato per indirizzo: {}", address);
                return null;
            }

        } catch (Exception e) {
            log.error("Errore durante geocoding per '{}': {}", address, e.getMessage());
            return null;
        }
    }

    /**
     * Converte coordinate in indirizzo (reverse geocoding)
     */
    public String reverseGeocode(double lat, double lon) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.set("Accept", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            String response = responseEntity.getBody();
            JsonNode result = objectMapper.readTree(response);

            if (result.has("display_name")) {
                String address = result.get("display_name").asText();
                log.info("✅ Reverse geocoding: ({}, {}) -> {}", lat, lon, address);
                return address;
            }
        } catch (Exception e) {
            log.error("Errore durante reverse geocoding: {}", e.getMessage());
        }
        return null;
    }
}