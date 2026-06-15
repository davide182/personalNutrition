package com.nutritionists.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.ProposalQueue;
import com.nutritionists.model.entity.ProposalQueue.ProposalStatus;
import com.nutritionists.repository.ProposalQueueRepository;
import com.nutritionists.service.AppointmentProposalService;
import com.nutritionists.service.FakeEmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ProposalTimeoutScheduler {
    
    private final ProposalQueueRepository proposalQueueRepository;
    private final AppointmentProposalService proposalService;
    private final FakeEmailService fakeEmailService;
    
    private static final long TIMEOUT_MINUTES = 60; // 1 ora
    
    
    @Scheduled(fixedDelay = 300000) 
    @Transactional
    public void checkExpiredProposals() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        
        List<ProposalQueue> activeQueues = proposalQueueRepository.findByStatusAndProposalSentAtBefore(
            ProposalStatus.ACTIVE, timeoutThreshold);
        
        for (ProposalQueue queue : activeQueues) {
            Appointment appointment = queue.getAppointment();
            
            if (appointment.getStatus() == AppointmentStatus.PROPOSED) {
                log.info("⏰ Proposta scaduta per appuntamento {} (inviata alle {})",
                    appointment.getAppointmentId(), queue.getProposalSentAt());
                
                
                int currentIndex = queue.getCurrentIndex();
                queue.setCurrentIndex(currentIndex + 1);
                proposalQueueRepository.save(queue);
                
                fakeEmailService.sendNoResponseTimeoutEmail(
                    appointment.getUser().getEmail(),
                    appointment.getUser().getUserProfile().getFirstName(),
                    appointment.getStartTime()
                );
                
                proposalService.sendProposalToNutritionist(appointment, queue);
            }
        }
    }
}