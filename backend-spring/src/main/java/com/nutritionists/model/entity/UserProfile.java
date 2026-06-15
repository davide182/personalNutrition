package com.nutritionists.model.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "user_profiles")
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    @NotNull
    private String firstName; 
    
    @Column(nullable = false)
    @NotNull
    private String lastName;  
    
    @Column(nullable = false)
    @NotNull
    private LocalDateTime createdAtProfile;

    private Double latitude;
    private Double longitude;

    @PrePersist
    protected void onCreate() {
        createdAtProfile = LocalDateTime.now();
    }
}