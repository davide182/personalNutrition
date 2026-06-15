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
public class AppointmentResponse {
    private Long appointmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String patientFirstName;
    private String patientLastName;
    private String location; 
    private Double price;     
}