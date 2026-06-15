package com.nutritionists.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    private GeocodingService geocodingService;
    
    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingService();
        ReflectionTestUtils.setField(geocodingService, "userAgent", 
            "NutritionistsApp/1.0 (contact: admin@nutritionists.it)");
    }
    
    @Test
    void geocodeAddress_ValidAddress_ReturnsCoordinates() {
      
        String address = "Piazza del Duomo, Milano";
    
        double[] coordinates = geocodingService.geocodeAddress(address);
        
        if (coordinates != null) {
            assertEquals(2, coordinates.length);
            assertTrue(coordinates[0] >= 45.0 && coordinates[0] <= 46.0);
            assertTrue(coordinates[1] >= 9.0 && coordinates[1] <= 10.0);
        }
    }
    
    @Test
    void geocodeAddress_NullAddress_ReturnsNull() {
      
        double[] coordinates = geocodingService.geocodeAddress(null);

        assertNull(coordinates);
    }
    
    @Test
    void geocodeAddress_EmptyAddress_ReturnsNull() {
    
        double[] coordinates = geocodingService.geocodeAddress("");
        
  
        assertNull(coordinates);
    }
    
    @Test
    void geocodeAddress_InvalidAddress_ReturnsNull() {
    
        String address = "IndirizzoCheNonEsiste12345";
        
    
        double[] coordinates = geocodingService.geocodeAddress(address);
        
        assertNull(coordinates);
    }
}