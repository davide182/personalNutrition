package com.nutritionists.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByUser_UserId(Long userId);

    List<Appointment> findByNutritionist_NutritionistId(Long nutritionistId);

    List<Appointment> findByStatus(String status);

    List<Appointment> findByUser_UserIdAndStartTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.nutritionist.nutritionistId = :nutritionistId AND a.startTime >= :rangeStart AND a.endTime <= :rangeEnd")
    List<Appointment> findByNutritionistAndDateRange(@Param("nutritionistId") Long nutritionistId, @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);

    @Query("SELECT a FROM Appointment a WHERE a.nutritionist.nutritionistId = :nutritionistId AND a.startTime < :newEnd AND a.endTime > :newStart")
    List<Appointment> findConflictingAppointments(@Param("nutritionistId") Long nutritionistId, @Param("newStart") LocalDateTime newStart, @Param("newEnd") LocalDateTime newEnd);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user.userId = :userId AND a.status IN ('PENDING_PROPOSAL', 'PROPOSED', 'CONFIRMED')")
    int countActiveAppointmentsByPatient(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.nutritionist.user.userId = :nutritionistUserId AND a.status IN ('PENDING_PROPOSAL', 'PROPOSED', 'CONFIRMED')")
    int countActiveAppointmentsByNutritionist(@Param("nutritionistUserId") Long nutritionistUserId);
}