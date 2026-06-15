package com.nutritionists.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @InjectMocks
    private HealthController healthController;

    @Test
    void health_ReturnsUp() {
        
        ResponseEntity<Map<String, String>> response = healthController.health();

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("nutritionists-backend", response.getBody().get("service"));
    }
}