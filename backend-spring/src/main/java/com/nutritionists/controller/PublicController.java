package com.nutritionists.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.entity.Specialization;
import com.nutritionists.repository.SpecializationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final SpecializationRepository specializationRepository;

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getSpecializations() {
        List<Specialization> specializations = specializationRepository.findAll();
        return ResponseEntity.ok(specializations);
    }
}