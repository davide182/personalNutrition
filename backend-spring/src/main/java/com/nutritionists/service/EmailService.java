package com.nutritionists.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.nutritionists.model.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final FakeEmailService fakeEmailService;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.email.fake:true}")
    private boolean fakeEmail;
    
    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled || fakeEmail) {
            fakeEmailService.sendRawEmail(to, subject, body);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
          
            message.setFrom("davidepalese24@gmail.com");
            mailSender.send(message);
            log.info("✅ Email reale inviata a: {}", to);
        } catch (Exception e) {
            log.error("❌ Errore invio email a {}: {}", to, e.getMessage());
            fakeEmailService.sendRawEmail(to, subject, body);
        }
    }
    
    public void sendWelcomeEmailToPatient(String email, String firstName) {
        String subject = "Benvenuto su Nutritionists!";
        String body = String.format("""
            Ciao %s,
            
            Benvenuto su Nutritionists!
            
            Il tuo account è stato creato con successo.
            Ora puoi cercare nutrizionisti e prenotare visite.
            
            A presto!
            Il team di Nutritionists
            """, firstName);
        sendEmail(email, subject, body);
    }
    
    public void sendPendingEmailToNutritionist(String email, String firstName, String lastName) {
        String subject = "Registrazione Nutrizionista - In attesa di approvazione";
        String body = String.format("""
            Caro/a Dott./Dott.ssa %s %s,
            
            Grazie per esserti registrato su Nutritionists!
            
            Il tuo account è in attesa di approvazione da parte dell'amministratore.
            Riceverai una notifica non appena il tuo account sarà attivato.
            
            A presto!
            Il team di Nutritionists
            """, firstName, lastName);
        sendEmail(email, subject, body);
    }
    
    public void sendApprovalEmailToNutritionist(String email, String firstName, String lastName) {
        String subject = "Account Nutrizionista Approvato!";
        String body = String.format("""
            Caro/a Dott./Dott.ssa %s %s,
            
            Il tuo account è stato APPROVATO dall'amministratore!
            
            Ora puoi accedere al portale e iniziare a gestire:
            - Il tuo profilo professionale
            - Gli orari di lavoro
            - Le richieste di appuntamento
            
            Buon lavoro!
            Il team di Nutritionists
            """, firstName, lastName);
        sendEmail(email, subject, body);
    }
    
    public void sendRejectionEmailToNutritionist(String email, String firstName, String lastName) {
        String subject = "Account Nutrizionista - Non Approvato";
        String body = String.format("""
            Caro/a Dott./Dott.ssa %s %s,
            
            Ci dispiace informarti che la tua registrazione come nutrizionista
            non è stata approvata in questo momento.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName, lastName);
        sendEmail(email, subject, body);
    }
    
    public void sendProposalEmailToNutritionist(String email, String firstName, String lastName,Long appointmentId, String patientName,String date, String time) {
        String subject = "Nuova richiesta di appuntamento";
        String body = String.format("""
            Caro/a Dott./Dott.ssa %s %s,
            
            Hai ricevuto una nuova richiesta di appuntamento!
            
            Paziente: %s
            Data: %s
            Ora: %s
            
            Per accettare o rifiutare, accedi al portale.
            
            A presto!
            Il team di Nutritionists
            """, firstName, lastName, patientName, date, time);
        sendEmail(email, subject, body);
    }
    
    public void sendAppointmentConfirmedEmailToPatient(String email, String patientName,String nutritionistName, String date, String time) {
        String subject = "Appuntamento Confermato!";
        String body = String.format("""
            Ciao %s,
            
            Il tuo appuntamento con %s è stato CONFERMATO!
            
            Data: %s
            Ora: %s
            
            A presto!
            Il team di Nutritionists
            """, patientName, nutritionistName, date, time);
        sendEmail(email, subject, body);
    }
    
    public void sendNoAvailabilityEmailToPatient(String email, String firstName) {
        String subject = "Nessuna disponibilità per l'appuntamento";
        String body = String.format("""
            Ciao %s,
            
            Ci dispiace informarti che al momento non ci sono nutrizionisti
            disponibili per la fascia oraria richiesta.
            
            Ti invitiamo a riprovare con un altro orario.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName);
        sendEmail(email, subject, body);
    }

    public void sendProfileDisabledEmail(String email, String firstName) {
        String subject = "Profilo disabilitato - Nutritionists";
        String body = String.format("""
            Ciao %s,
            
            Il tuo profilo è stato disabilitato con successo.
            
            Se desideri riattivarlo, contatta l'amministratore all'indirizzo admin@nutritionists.it.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName);
        sendEmail(email, subject, body);
    }

    public void sendProfileReenabledEmail(String email, String firstName) {
        String subject = "Profilo riattivato - Nutritionists";
        String body = String.format("""
            Ciao %s,
            
            Il tuo profilo è stato riattivato dall'amministratore.
            
            Ora puoi tornare ad utilizzare la piattaforma.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName);
        sendEmail(email, subject, body);
    }

    public void sendProfileDisableRejectedEmail(String email, String firstName, String reason) {
        String subject = "Richiesta disabilitazione negata - Nutritionists";
        String body = String.format("""
            Ciao %s,
            
            La tua richiesta di disabilitazione del profilo è stata negata dall'amministratore.
            
            Motivo: %s
            
            Per maggiori informazioni, contatta l'amministratore.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName, reason);
        sendEmail(email, subject, body);
    }

    public void notifyAdminNutritionistDisableRequest(User nutritionist, String reason) {
        String adminEmail = "admin@nutritionists.it";
        String subject = "Nuova richiesta di disabilitazione - Nutritionists";
        String body = String.format("""
            Richiesta di disabilitazione dal nutrizionista:
            
            Nome: %s %s
            Email: %s
            Motivo: %s
            
            Per gestire la richiesta, accedi al pannello admin.
            """,
            nutritionist.getUserProfile().getFirstName(),
            nutritionist.getUserProfile().getLastName(),
            nutritionist.getEmail(),
            reason != null ? reason : "Nessun motivo specificato"
        );
        sendEmail(adminEmail, subject, body);
    }

    public void sendPasswordResetEmail(String email, String firstName, String resetLink) {
        String subject = "Reset Password - Nutritionists";
        String body = String.format("""
            Ciao %s,
            
            Abbiamo ricevuto una richiesta di reset della password per il tuo account.
            
            Clicca sul link sottostante per reimpostare la password:
            %s
            
            Il link è valido per 24 ore.
            
            Se non hai richiesto tu il reset, ignora questo messaggio.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName, resetLink);
        sendEmail(email, subject, body);
    }

    public void sendPasswordResetConfirmationEmail(String email, String firstName) {
        String subject = "Password Resettata - Nutritionists";
        String body = String.format("""
            Ciao %s,
            
            La tua password è stata resettata con successo.
            
            Se non sei stato tu a effettuare questa operazione, contatta immediatamente l'assistenza.
            
            Cordiali saluti,
            Il team di Nutritionists
            """, firstName);
        sendEmail(email, subject, body);
    }
}