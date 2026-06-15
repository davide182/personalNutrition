package com.nutritionists.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nutritionists.client.GeoDistanceClient;
import com.nutritionists.model.dto.request.CreateAppointmentRequest;
import com.nutritionists.model.dto.request.GeosortRequest;
import com.nutritionists.model.dto.response.GeosortResponse;
import com.nutritionists.model.dto.NutritionistGeoDto;
import com.nutritionists.model.dto.NutritionistProposalDto;
import com.nutritionists.model.entity.Appointment;
import com.nutritionists.model.entity.enums.AppointmentStatus;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.model.entity.Nutritionist;
import com.nutritionists.model.entity.ProposalQueue;
import com.nutritionists.model.entity.ProposalQueue.ProposalStatus;
import com.nutritionists.model.entity.Specialization;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.WorkSchedule;
import com.nutritionists.repository.AppointmentRepository;
import com.nutritionists.repository.ProposalQueueRepository;
import com.nutritionists.repository.SpecializationRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.repository.WorkScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentProposalService {

    private final AppointmentRepository appointmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final SpecializationRepository specializationRepository;
    private final UserRepository userRepository;
    private final ProposalQueueRepository proposalQueueRepository;
    private final GeoDistanceClient geoDistanceClient;
    private final EmailService emailService;
    private final LoggingService loggingService;
    private final RestTemplate restTemplate;

    @Transactional
    public Appointment createAppointmentProposal(Long patientId, CreateAppointmentRequest request) {
        
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Paziente non trovato"));
        
        UserProfile patientProfile = patient.getUserProfile();
        if (patientProfile.getLatitude() == null || patientProfile.getLongitude() == null) {
            throw new RuntimeException("Il paziente non ha impostato la posizione. Aggiorna il profilo.");
        }
        
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        

        List<Appointment> patientConflictingAppointments = appointmentRepository
                .findByUser_UserIdAndStartTimeBetween(patientId, startTime, endTime);
        if (!patientConflictingAppointments.isEmpty()) {
            throw new RuntimeException("Hai già un appuntamento in questa fascia oraria");
        }
        
    
        List<User> availableNutritionists = new ArrayList<>();
        
        if (request.getNutritionistId() != null) {
        
            User specificNutritionist = userRepository.findById(request.getNutritionistId())
                    .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
            
           
            if (specificNutritionist.getRole() != Role.NUTRITIONIST || 
                specificNutritionist.getStatus() != UserStatus.ACTIVE) {
                throw new RuntimeException("Il nutrizionista selezionato non è disponibile");
            }
            
           
            log.info("🔍 Controllo disponibilità per nutrizionista ID: {}", specificNutritionist.getUserId());
            log.info("🔍 Orario richiesto: {} - {}", startTime, endTime);
            
            if (!isNutritionistAvailable(specificNutritionist.getNutritionist(), startTime, endTime)) {
                throw new RuntimeException("Il nutrizionista selezionato non è disponibile in questa fascia oraria");
            }
            
     
            List<Appointment> nutritionistConflictingAppointments = appointmentRepository
                    .findConflictingAppointments(specificNutritionist.getNutritionist().getNutritionistId(), startTime, endTime);
            if (!nutritionistConflictingAppointments.isEmpty()) {
                throw new RuntimeException("Il nutrizionista selezionato ha già un appuntamento in questa fascia oraria");
            }
            
            availableNutritionists.add(specificNutritionist);
            log.info("Richiesta diretta al nutrizionista specifico: {}", specificNutritionist.getEmail());
            
        } else {
            List<User> activeNutritionists = userRepository.findByRoleAndStatus(Role.NUTRITIONIST, UserStatus.ACTIVE);
            
            if (activeNutritionists.isEmpty()) {
                throw new RuntimeException("Nessun nutrizionista attualmente disponibile");
            }
            
            if (request.getSpecializationId() != null) {
                Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                        .orElseThrow(() -> new RuntimeException("Specializzazione non trovata"));
                
                activeNutritionists = activeNutritionists.stream()
                        .filter(user -> {
                            Nutritionist nutr = user.getNutritionist();
                            return nutr != null && nutr.getSpecializations().contains(specialization);
                        })
                        .collect(Collectors.toList());
                
                if (activeNutritionists.isEmpty()) {
                    throw new RuntimeException("Nessun nutrizionista trovato con la specializzazione richiesta");
                }
            }
            
            for (User user : activeNutritionists) {
                Nutritionist nutritionist = user.getNutritionist();
                if (isNutritionistAvailable(nutritionist, startTime, endTime)) {
                    List<Appointment> nutritionistConflictingAppointments = appointmentRepository
                            .findConflictingAppointments(nutritionist.getNutritionistId(), startTime, endTime);
                    if (nutritionistConflictingAppointments.isEmpty()) {
                        availableNutritionists.add(user);
                    }
                }
            }
            
            if (availableNutritionists.isEmpty()) {
                throw new RuntimeException("Nessun nutrizionista disponibile nella fascia oraria richiesta");
            }
            

            List<NutritionistGeoDto> nutritionistGeoList = availableNutritionists.stream()
                    .map(user -> {
                        UserProfile profile = user.getUserProfile();
                        return NutritionistGeoDto.builder()
                                .id(user.getUserId())
                                .lat(profile.getLatitude())
                                .lon(profile.getLongitude())
                                .build();
                    })
                    .collect(Collectors.toList());
            
            GeosortRequest geoRequest = GeosortRequest.builder()
                    .patientLat(patientProfile.getLatitude())
                    .patientLon(patientProfile.getLongitude())
                    .nutritionists(nutritionistGeoList)
                    .build();
            
            GeosortResponse geoResponse = geoDistanceClient.sortNutritionistsByDistance(geoRequest);
            
            if (geoResponse == null || geoResponse.getSortedIds() == null || geoResponse.getSortedIds().isEmpty()) {
                throw new RuntimeException("Nessun nutrizionista trovato nelle vicinanze");
            }
            
            log.info("Nutrizionisti ordinati da Flask: {}", geoResponse.getSortedIds());
            
         
            availableNutritionists.clear();
            for (Long id : geoResponse.getSortedIds()) {
                userRepository.findById(id).ifPresent(availableNutritionists::add);
            }
        }
        
    
        Appointment appointment = Appointment.builder()
                .user(patient)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.PENDING_PROPOSAL)
                .build();
        appointment = appointmentRepository.save(appointment);
        
    
        List<Long> candidateIds = availableNutritionists.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        
        ProposalQueue queue = ProposalQueue.builder()
                .appointment(appointment)
                .candidateNutritionistIds(new ArrayList<>(candidateIds))
                .currentIndex(0)
                .proposalSentAt(LocalDateTime.now())
                .status(ProposalQueue.ProposalStatus.ACTIVE)
                .build();
        proposalQueueRepository.save(queue);
        
        log.info("Coda proposta creata con {} candidati", queue.getCandidateNutritionistIds().size());
        
        
        sendProposalToNutritionist(appointment, queue);
        
        log.info("Creata proposta appuntamento {} per paziente {}", 
            appointment.getAppointmentId(), patient.getEmail());
        
        return appointment;
    }
    
    private boolean isNutritionistAvailable(Nutritionist nutritionist, LocalDateTime startTime, LocalDateTime endTime) {
        var dayOfWeek = startTime.getDayOfWeek();
        List<WorkSchedule> schedules = workScheduleRepository.findByNutritionist_NutritionistIdAndDayOfWeek(
            nutritionist.getNutritionistId(), dayOfWeek);
        
        log.info("🔍 Orari di lavoro trovati per {}: {}", nutritionist.getNutritionistId(), schedules.size());
        
        boolean hasSchedule = schedules.stream().anyMatch(schedule -> 
            !startTime.toLocalTime().isBefore(schedule.getStartTime()) &&
            !endTime.toLocalTime().isAfter(schedule.getEndTime())
        );
        
        if (!hasSchedule) {
            log.warn("❌ Nutrizionista {} non lavora in questa fascia oraria (orari disponibili: {})", 
                nutritionist.getNutritionistId(), schedules);
            return false;
        }
        

        List<Appointment> existingAppointments = appointmentRepository.findConflictingAppointments(
            nutritionist.getNutritionistId(), startTime, endTime);
        
        if (!existingAppointments.isEmpty()) {
            log.warn("⚠️ Nutrizionista {} ha già {} appuntamenti conflittuali", 
                nutritionist.getNutritionistId(), existingAppointments.size());
            return false;
        }
        
        log.info("✅ Nutrizionista {} disponibile per l'orario richiesto", nutritionist.getNutritionistId());
        return true;
    }
    
    @Transactional
    public void sendProposalToNutritionist(Appointment appointment, ProposalQueue queue) {
        List<Long> candidates = queue.getCandidateNutritionistIds();
        

        if (candidates == null || candidates.isEmpty()) {
            log.error("Lista candidati è null o vuota per appointment {}", appointment.getAppointmentId());
            appointment.setStatus(AppointmentStatus.FAILED);
            queue.setStatus(ProposalQueue.ProposalStatus.FAILED);
            appointmentRepository.save(appointment);
            proposalQueueRepository.save(queue);
            
            User patient = appointment.getUser();
            emailService.sendNoAvailabilityEmailToPatient(
                patient.getEmail(),
                patient.getUserProfile().getFirstName()
            );
            
            loggingService.error("APPOINTMENT", "PROPOSAL", String.valueOf(patient.getUserId()), patient.getEmail(), 
                "Nessun candidato disponibile per appuntamento", String.valueOf(appointment.getAppointmentId()));
            return;
        }
        
        int currentIndex = queue.getCurrentIndex();
        
        if (currentIndex >= candidates.size()) {
            appointment.setStatus(AppointmentStatus.FAILED);
            queue.setStatus(ProposalQueue.ProposalStatus.FAILED);
            appointmentRepository.save(appointment);
            proposalQueueRepository.save(queue);
            
            User patient = appointment.getUser();
            emailService.sendNoAvailabilityEmailToPatient(
                patient.getEmail(),
                patient.getUserProfile().getFirstName()
            );
            log.info("Proposta fallita - nessun nutrizionista disponibile per appuntamento {}", 
                appointment.getAppointmentId());
            
            loggingService.warn("APPOINTMENT", "PROPOSAL", String.valueOf(patient.getUserId()), patient.getEmail(), 
                "Tutti i candidati hanno rifiutato o non disponibili", String.valueOf(appointment.getAppointmentId()));
            return;
        }
        
        Long nutritionistUserId = candidates.get(currentIndex);
        User nutritionistUser = userRepository.findById(nutritionistUserId).orElse(null);
        
        if (nutritionistUser == null || nutritionistUser.getNutritionist() == null) {
            log.warn("Nutrizionista {} non valido, passo al prossimo", nutritionistUserId);
            queue.setCurrentIndex(currentIndex + 1);
            proposalQueueRepository.save(queue);
            sendProposalToNutritionist(appointment, queue);
            return;
        }
        
        appointment.setStatus(AppointmentStatus.PROPOSED);
        appointmentRepository.save(appointment);
        
        queue.setProposalSentAt(LocalDateTime.now());
        proposalQueueRepository.save(queue);
        
        User patient = appointment.getUser();
        String patientFullName = patient.getUserProfile().getFirstName() + " " + patient.getUserProfile().getLastName();
        String date = appointment.getStartTime().toLocalDate().toString();
        String time = appointment.getStartTime().toLocalTime().toString() + " - " + 
                    appointment.getEndTime().toLocalTime().toString();
        
        emailService.sendProposalEmailToNutritionist(
            nutritionistUser.getEmail(),
            nutritionistUser.getUserProfile().getFirstName(),
            nutritionistUser.getUserProfile().getLastName(),
            appointment.getAppointmentId(),
            patientFullName,
            date,
            time
        );
        
        loggingService.info("APPOINTMENT", "PROPOSAL", String.valueOf(nutritionistUserId), nutritionistUser.getEmail(), 
            "Proposta inviata per appuntamento " + appointment.getAppointmentId());
        
        log.info("Proposta inviata a nutrizionista {} (indice {}) per appuntamento {}", 
            nutritionistUser.getEmail(), currentIndex, appointment.getAppointmentId());
    }


    public List<NutritionistProposalDto> getProposalsForNutritionist(Long nutritionistUserId) {
        
        User nutritionistUser = userRepository.findById(nutritionistUserId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
        
        Nutritionist nutritionist = nutritionistUser.getNutritionist();
        if (nutritionist == null) {
            throw new RuntimeException("Profilo nutrizionista non trovato");
        }
        
        List<ProposalQueue> activeQueues = proposalQueueRepository.findByStatus(ProposalStatus.ACTIVE);
        
        List<NutritionistProposalDto> proposals = new ArrayList<>();
        
        for (ProposalQueue queue : activeQueues) {
            Appointment appointment = queue.getAppointment();
            
            if (appointment.getStatus() == AppointmentStatus.PROPOSED) {
                List<Long> candidates = queue.getCandidateNutritionistIds();
                int currentIndex = queue.getCurrentIndex();
                
                if (currentIndex < candidates.size() && candidates.get(currentIndex).equals(nutritionistUserId)) {
                    
                    User patient = appointment.getUser();
                    UserProfile patientProfile = patient.getUserProfile();
                    UserProfile nutritionistProfile = nutritionistUser.getUserProfile();
                    
                    Double distance = calculateDistance(
                        nutritionistProfile.getLatitude(),
                        nutritionistProfile.getLongitude(),
                        patientProfile.getLatitude(),
                        patientProfile.getLongitude()
                    );
                    
                    NutritionistProposalDto dto = NutritionistProposalDto.builder()
                            .appointmentId(appointment.getAppointmentId())
                            .patientFirstName(patientProfile.getFirstName())
                            .patientLastName(patientProfile.getLastName())
                            .patientEmail(patient.getEmail())
                            .startTime(appointment.getStartTime())
                            .endTime(appointment.getEndTime())
                            .patientLatitude(patientProfile.getLatitude())
                            .patientLongitude(patientProfile.getLongitude())
                            .distanceKm(distance)
                            .positionInQueue(currentIndex + 1)
                            .build();
                    
                    proposals.add(dto);
                }
            }
        }
        
        loggingService.info("APPOINTMENT", "GET_PROPOSALS", String.valueOf(nutritionistUserId), nutritionistUser.getEmail(), 
            "Recuperate " + proposals.size() + " proposte");
        
        return proposals;
    }

    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    @Transactional
    public void acceptProposal(Long appointmentId, Long nutritionistUserId, Double price, String location) {
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        
        if (appointment.getStatus() != AppointmentStatus.PROPOSED) {
            throw new RuntimeException("Questa proposta non è più attiva. Stato attuale: " + appointment.getStatus());
        }
        
        ProposalQueue queue = proposalQueueRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Coda proposta non trovata"));
        
        List<Long> candidates = queue.getCandidateNutritionistIds();
        int currentIndex = queue.getCurrentIndex();
        
        if (currentIndex >= candidates.size() || !candidates.get(currentIndex).equals(nutritionistUserId)) {
            throw new RuntimeException("Non sei il destinatario di questa proposta");
        }
        
        User nutritionistUser = userRepository.findById(nutritionistUserId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setNutritionist(nutritionistUser.getNutritionist());
        appointment.setPrice(price);
        appointment.setLocation(location);
        appointmentRepository.save(appointment);
        
        queue.setStatus(ProposalStatus.ACCEPTED);
        proposalQueueRepository.save(queue);
        
        User patient = appointment.getUser();
        String patientName = patient.getUserProfile().getFirstName();
        String nutritionistName = "Dott./Dott.ssa " + nutritionistUser.getUserProfile().getLastName();
        String date = appointment.getStartTime().toLocalDate().toString();
        String time = appointment.getStartTime().toLocalTime().toString() + " - " + 
                    appointment.getEndTime().toLocalTime().toString();
        
        emailService.sendAppointmentConfirmedEmailToPatient(
            patient.getEmail(),
            patientName,
            nutritionistName,
            date,
            time
        );
        
        log.info("✅ Proposta accettata - Appuntamento {} confermato con nutrizionista {}",
            appointmentId, nutritionistUser.getEmail());
    }

    @Transactional
    public void rejectProposal(Long appointmentId, Long nutritionistUserId) {
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));
        
        if (appointment.getStatus() != AppointmentStatus.PROPOSED) {
            throw new RuntimeException("Questa proposta non è più attiva. Stato attuale: " + appointment.getStatus());
        }
        
        ProposalQueue queue = proposalQueueRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Coda proposta non trovata"));
        
        List<Long> candidates = queue.getCandidateNutritionistIds();
        int currentIndex = queue.getCurrentIndex();
        
        if (currentIndex >= candidates.size() || !candidates.get(currentIndex).equals(nutritionistUserId)) {
            throw new RuntimeException("Non sei il destinatario di questa proposta");
        }
        
        User nutritionistUser = userRepository.findById(nutritionistUserId)
                .orElseThrow(() -> new RuntimeException("Nutrizionista non trovato"));
        
        log.info("❌ Nutrizionista {} ha rifiutato la proposta per appuntamento {}",
            nutritionistUserId, appointmentId);
        
        loggingService.warn("APPOINTMENT", "REJECT", String.valueOf(nutritionistUserId), nutritionistUser.getEmail(), 
            "Rifiutato appuntamento " + appointmentId, "Passo al prossimo candidato");
        
        User patient = appointment.getUser();
        emailService.sendNoAvailabilityEmailToPatient(
            patient.getEmail(),
            patient.getUserProfile().getFirstName()
        );
        
        queue.setCurrentIndex(currentIndex + 1);
        proposalQueueRepository.save(queue);
        
        sendProposalToNutritionist(appointment, queue);
    }

    public Map<String, Object> calculateBMI(Double weight, Double height, Integer age, String gender, String activityLevel) {
        try {
            String url = "http://localhost:5000/api/bmi";
            
            Map<String, Object> request = Map.of(
                "weight", weight,
                "height", height,
                "age", age != null ? age : 30,
                "gender", gender != null ? gender : "M",
                "activity_level", activityLevel != null ? activityLevel : "moderate"
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            loggingService.info("FLASK", "BMI_CALC", null, null, "BMI calcolato: " + response.get("bmi"));
            
            return response;
        } catch (Exception e) {
            loggingService.error("FLASK", "BMI_CALC", null, null, "Errore calcolo BMI", e.getMessage());
            throw new RuntimeException("Servizio BMI non disponibile: " + e.getMessage());
        }
    }
}