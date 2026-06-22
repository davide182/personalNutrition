package com.nutritionists.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FakeEmailService {
    
    public void sendRawEmail(String to, String subject, String body) {
        log.info("📧 ===== FAKE EMAIL =====");
        log.info("📧 A: {}", to);
        log.info("📧 Oggetto: {}", subject);
        log.info("📧 Corpo:\n{}", body);
        log.info("📧 ===================================");
    }
    
    public void sendApprovalEmail(String email, String firstName, String lastName) {
        log.info("📧 ===== FAKE EMAIL INVIATA ===== ");
        log.info("📧 A: {}", email);
        log.info("📧 Oggetto: Account Nutrizionista Approvato");
        log.info("📧 Messaggio: Caro/a Dott./Dott.ssa {} {},", firstName, lastName);
        log.info("📧 Il tuo account è stato APPROVATO dall'amministratore.");
        log.info("📧 =============================");
    }
    
    public void sendRejectionEmail(String email, String firstName, String lastName) {
        log.info("📧 ===== FAKE EMAIL INVIATA ===== ");
        log.info("📧 A: {}", email);
        log.info("📧 Oggetto: Account Nutrizionista Non Approvato");
        log.info("📧 Messaggio: Caro/a Dott./Dott.ssa {} {},", firstName, lastName);
        log.info("📧 =============================");
    }
    
    public void sendProposalEmail(String email, String firstName, String lastName, Long appointmentId, LocalDateTime startTime, LocalDateTime endTime,String patientFirstName, String patientLastName) {
        log.info("📧 ===== NUOVA PROPOSTA APPUNTAMENTO =====");
        log.info("📧 A: {} {}", firstName, lastName);
        log.info("📧 Oggetto: Nuova richiesta di appuntamento");
        log.info("📧 Paziente: {} {}", patientFirstName, patientLastName);
        log.info("📧 Data: {}", startTime.toLocalDate());
        log.info("📧 Ora: {} - {}", startTime.toLocalTime(), endTime.toLocalTime());
        log.info("📧 ======================================");
    }
    
    public void sendNoAvailabilityEmail(String email, String firstName) {
        log.info("📧 ===== NESSUN NUTRIZIONISTA DISPONIBILE =====");
        log.info("📧 A: {}", email);
        log.info("📧 =============================================");
    }
    
    public void sendAppointmentConfirmedEmail(String email, String firstName, String nutritionistName, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("📧 ===== APPUNTAMENTO CONFERMATO =====");
        log.info("📧 A: {}", email);
        log.info("📧 Nutrizionista: {}", nutritionistName);
        log.info("📧 Data: {}", startTime.toLocalDate());
        log.info("📧 ==================================");
    }
    
    public void sendAppointmentRejectedEmail(String email, String firstName, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("📧 ===== APPUNTAMENTO RIFIUTATO =====");
        log.info("📧 A: {}", email);
        log.info("📧 =================================");
    }
    
    public void sendNoResponseTimeoutEmail(String email, String firstName, LocalDateTime startTime) {
        log.info("📧 ===== TIMEOUT PROPOSTA =====");
        log.info("📧 A: {}", email);
        log.info("📧 ===========================");
    }
    
    public void sendAllNutritionistsRejectedEmail(String email, String firstName) {
        log.info("📧 ===== TUTTI I NUTRIZIONISTA HANNO RIFIUTATO =====");
        log.info("📧 ===============================================");
    }
}