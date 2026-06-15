package com.nutritionists.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.WorkSchedule;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    List<WorkSchedule> findByNutritionist_NutritionistId(Long nutritionistId);
    
    List<WorkSchedule> findByNutritionist_NutritionistIdAndDayOfWeek(Long nutritionistId, DayOfWeek dayOfWeek);

    Optional<WorkSchedule> findByNutritionist_NutritionistIdAndDayOfWeekAndStartTimeAndEndTime(Long nutritionistId, DayOfWeek dayOfWeek, java.time.LocalTime startTime, java.time.LocalTime endTime);
}