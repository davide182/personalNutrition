package com.nutritionists.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAppointmentResponse {
    private Long appointmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String patientFirstName;
    private String patientLastName;
    private String nutritionistFirstName;
    private String nutritionistLastName;
    private String location;
    private Double price;
}