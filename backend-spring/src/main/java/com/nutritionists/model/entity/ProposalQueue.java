package com.nutritionists.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "proposal_queues")
@Builder
public class ProposalQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long queueId;
    
    @OneToOne
    @JoinColumn(name = "appointmentId", nullable = false)
    private Appointment appointment;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "proposal_candidates", joinColumns = @JoinColumn(name = "queueId"))
    @Column(name = "nutritionistId")
    @Builder.Default
    private List<Long> candidateNutritionistIds = new ArrayList<>();
    
    @Builder.Default
    private int currentIndex = 0;
    
    @Column(nullable = false)
    private LocalDateTime proposalSentAt;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.ACTIVE;
    
    public enum ProposalStatus {
        ACTIVE, ACCEPTED, FAILED
    }
}