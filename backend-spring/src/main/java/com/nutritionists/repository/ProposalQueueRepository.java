package com.nutritionists.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.ProposalQueue;
import com.nutritionists.model.entity.ProposalQueue.ProposalStatus;

@Repository
public interface ProposalQueueRepository extends JpaRepository<ProposalQueue, Long> {
    
    Optional<ProposalQueue> findByAppointment_AppointmentId(Long appointmentId);
    
    List<ProposalQueue> findByStatusAndProposalSentAtBefore(
        ProposalStatus status, 
        LocalDateTime timeout
    );

    List<ProposalQueue> findByStatus(ProposalStatus status);
}