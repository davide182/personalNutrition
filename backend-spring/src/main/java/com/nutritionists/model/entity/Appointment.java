package com.nutritionists.model.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nutritionists.model.entity.enums.AppointmentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "appointments")
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @JoinColumn(name = "userId", nullable = false)
    @ManyToOne
    @JsonIgnore 
    private User user;

    @JoinColumn(name = "nutritionist_id", nullable = true)
    @ManyToOne
    @JsonIgnore  
    private Nutritionist nutritionist;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
    
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonIgnore 
    private NutritionalPlan nutritionalPlan;

    @Column(nullable = true)
    private Double price;

    @Column(nullable = true)
    private String location;
}