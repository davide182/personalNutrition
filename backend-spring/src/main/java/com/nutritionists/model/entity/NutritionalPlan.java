package com.nutritionists.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "nutritional_plans")
@Builder
public class NutritionalPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nutritionistPlanId;

    @JoinColumn(name = "appointmentId", nullable = false)
    @OneToOne
    private Appointment appointment;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(nullable = false)
    private LocalDateTime createdAtPlan;

    @PrePersist
    protected void onCreate() {
        createdAtPlan = LocalDateTime.now();
    }
}
