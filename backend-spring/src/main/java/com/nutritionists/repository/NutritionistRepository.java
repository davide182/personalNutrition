package com.nutritionists.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.Nutritionist;

@Repository
public interface NutritionistRepository extends JpaRepository<Nutritionist, Long> {
    Optional<Nutritionist> findByUser_UserId(Long userId);

    @Query("SELECT n FROM Nutritionist n JOIN n.specializations s WHERE s.specializationId = :specializationId")
    List<Nutritionist> findBySpecializationId(@Param("specializationId") Long specializationId);
}