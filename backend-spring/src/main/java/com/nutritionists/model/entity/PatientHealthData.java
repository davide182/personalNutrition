package com.nutritionists.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "patient_health_data")
@Builder
public class PatientHealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PatientDataId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    @NotNull
    private Double weight;

    @Column(nullable = false)
    @NotNull
    private Double height;

    @Column(columnDefinition = "TEXT") // usato per testo
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String goals;
    
}
