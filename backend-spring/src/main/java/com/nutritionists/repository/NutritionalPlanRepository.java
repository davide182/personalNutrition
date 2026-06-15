package com.nutritionists.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.NutritionalPlan;

@Repository
public interface NutritionalPlanRepository extends JpaRepository<NutritionalPlan, Long> {
    Optional<NutritionalPlan> findByAppointment_AppointmentId(Long appointmentId);

    List<NutritionalPlan> findAllByAppointment_User_UserId(Long userId);
}