package com.nutritionists.model.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
@Table(name = "nutritionists")
@Builder
public class Nutritionist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nutritionistId;

    @JoinColumn(name = "userId", nullable = false)
    @OneToOne
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private Double serviceRadiusKm;
    
    @ManyToMany
    @JoinTable(
        name = "nutritionist_specializations",
        joinColumns = @JoinColumn(name = "nutritionistId"),
        inverseJoinColumns = @JoinColumn(name = "specializationId")
    )
    @Builder.Default
    private Set<Specialization> specializations = new HashSet<>();

    @OneToMany(mappedBy = "nutritionist", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkSchedule> workSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "nutritionist", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}