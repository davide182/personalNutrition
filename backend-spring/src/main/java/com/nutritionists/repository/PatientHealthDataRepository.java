package com.nutritionists.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.PatientHealthData;

@Repository
public interface PatientHealthDataRepository extends JpaRepository<PatientHealthData, Long> {
    Optional<PatientHealthData> findByUser_UserId(Long userId);
}