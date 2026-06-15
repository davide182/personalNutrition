package com.nutritionists.controller;

import com.nutritionists.model.entity.Specialization;
import com.nutritionists.repository.SpecializationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicControllerTest {

    @Mock
    private SpecializationRepository specializationRepository;

    @InjectMocks
    private PublicController publicController;

    private Specialization specialization;

    @BeforeEach
    void setUp() {
        specialization = Specialization.builder()
                .specializationId(1L)
                .name("Nutrizione Clinica")
                .description("Specializzazione in nutrizione clinica")
                .build();
    }

    @Test
    void getSpecializations_ReturnsList() {
    
        when(specializationRepository.findAll()).thenReturn(List.of(specialization));

     
        ResponseEntity<List<Specialization>> response = publicController.getSpecializations();

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Nutrizione Clinica", response.getBody().get(0).getName());
        verify(specializationRepository).findAll();
    }

    @Test
    void getSpecializations_ReturnsEmptyList() {
        
        when(specializationRepository.findAll()).thenReturn(List.of());

    
        ResponseEntity<List<Specialization>> response = publicController.getSpecializations();

       
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}