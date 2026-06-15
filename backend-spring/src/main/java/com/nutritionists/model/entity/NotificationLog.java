package com.nutritionists.model.entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notification_logs")
public class NotificationLog {
    private String id;
    private String type;  // PROPOSAL_TO_NUTRITIONIST, CONFIRMATION_TO_PATIENT, REJECTION_TO_PATIENT
    private String to;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private Long appointmentId;
    private Long nutritionistId;
    private Long patientId;
}