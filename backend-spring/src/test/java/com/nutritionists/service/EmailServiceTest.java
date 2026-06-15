package com.nutritionists.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    
    @Mock
    private FakeEmailService fakeEmailService;
    
    @InjectMocks
    private EmailService emailService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fakeEmail", false);
    }
    
    @Test
    void sendWelcomeEmailToPatient_WhenEmailEnabled_SendsEmail() {
      
        String email = "test@example.com";
        String firstName = "Mario";
        
    
        emailService.sendWelcomeEmailToPatient(email, firstName);
        
      
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(fakeEmailService, never()).sendRawEmail(anyString(), anyString(), anyString());
    }
    
    @Test
    void sendWelcomeEmailToPatient_WhenFakeEmailEnabled_UsesFakeService() {
       
        ReflectionTestUtils.setField(emailService, "fakeEmail", true);
        String email = "test@example.com";
        String firstName = "Mario";
        
       
        emailService.sendWelcomeEmailToPatient(email, firstName);
        
       
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(fakeEmailService).sendRawEmail(eq(email), anyString(), anyString());
    }
    
    @Test
    void sendApprovalEmailToNutritionist_SendsEmail() {
       
        String email = "nutritionist@example.com";
        String firstName = "Giuseppe";
        String lastName = "Verdi";
        
        
        emailService.sendApprovalEmailToNutritionist(email, firstName, lastName);
        
       
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    void sendProposalEmailToNutritionist_SendsEmail() {
        
        String email = "nutritionist@example.com";
        String firstName = "Giuseppe";
        String lastName = "Verdi";
        Long appointmentId = 1L;
        String patientName = "Mario Rossi";
        String date = "2026-06-08";
        String time = "10:00 - 11:00";
        
  
        emailService.sendProposalEmailToNutritionist(email, firstName, lastName, appointmentId, patientName, date, time);
        
 
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    void sendAppointmentConfirmedEmailToPatient_SendsEmail() {
 
        String email = "patient@example.com";
        String patientName = "Mario";
        String nutritionistName = "Dott. Verdi";
        String date = "2026-06-08";
        String time = "10:00 - 11:00";
        
   
        emailService.sendAppointmentConfirmedEmailToPatient(email, patientName, nutritionistName, date, time);
        
    
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    void sendNoAvailabilityEmailToPatient_SendsEmail() {
        
        String email = "patient@example.com";
        String firstName = "Mario";
        
        emailService.sendNoAvailabilityEmailToPatient(email, firstName);
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}