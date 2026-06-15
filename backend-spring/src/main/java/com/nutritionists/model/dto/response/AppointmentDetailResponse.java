package com.nutritionists.model.dto.response;

import java.time.LocalDateTime;

import com.nutritionists.model.entity.enums.AppointmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDetailResponse {
    private Long appointmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String nutritionistName;
    private Double nutritionistLatitude;
    private Double nutritionistLongitude;
    private Double patientLatitude;
    private Double patientLongitude;
    private Double distance;
    private Double price;
    private String location;
}