package com.nutritionists.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nutritionists.model.dto.AuthResponse;
import com.nutritionists.model.dto.request.LoginRequest;
import com.nutritionists.model.dto.request.RegisterRequest;
import com.nutritionists.model.entity.Nutritionist;
import com.nutritionists.model.entity.Specialization;
import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.NutritionistRepository;
import com.nutritionists.repository.SpecializationRepository;
import com.nutritionists.repository.UserProfileRepository;
import com.nutritionists.repository.UserRepository;
import com.nutritionists.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final NutritionistRepository nutritionistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final LoggingService loggingService;
    private final GeocodingService geocodingService;
    private final SpecializationRepository specializationRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentativo di registrazione per email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            loggingService.warn("AUTH", "REGISTER", null, request.getEmail(), 
                "Tentativo registrazione con email già esistente", request.getEmail());
            throw new RuntimeException("Email già in uso");
        }
        if (request.getRole() == Role.ADMIN) {
            loggingService.warn("AUTH", "REGISTER", null, request.getEmail(), 
                "Tentativo registrazione come admin non consentito", null);
            throw new RuntimeException("Registrazione come admin non consentita");
        }

        double[] coordinates = geocodingService.geocodeAddress(request.getAddress());
        if (coordinates == null) {
            throw new RuntimeException("Impossibile trovare le coordinate per l'indirizzo fornito. Verifica l'indirizzo.");
        }
        request.setLatitude(coordinates[0]);
        request.setLongitude(coordinates[1]);

        UserStatus status;
        String message;
        
        if (request.getRole() == Role.PATIENT) {
            status = UserStatus.ACTIVE;
            message = "Registrazione completata con successo. Puoi effettuare il login.";
        } else if (request.getRole() == Role.NUTRITIONIST) {
            status = UserStatus.PENDING;
            message = "Registrazione completata. In attesa di approvazione da parte dell'amministratore.";
        } else {
            throw new RuntimeException("Ruolo non valido");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(status)
                .build();
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        userProfileRepository.save(profile);

        if (request.getRole() == Role.NUTRITIONIST) {
            Nutritionist nutritionist = Nutritionist.builder()
                    .user(user)
                    .bio(request.getBio())
                    .serviceRadiusKm(request.getServiceRadiusKm() != null ? request.getServiceRadiusKm() : 10.0)
                    .build();

            if (request.getSpecializationId() != null) {
                Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                        .orElseThrow(() -> new RuntimeException("Specializzazione non trovata"));
                nutritionist.getSpecializations().add(specialization);
            }
            nutritionistRepository.save(nutritionist);
        }
        
        // Invio email
        if (request.getRole() == Role.PATIENT) {
            emailService.sendWelcomeEmailToPatient(user.getEmail(), request.getFirstName());
        } else if (request.getRole() == Role.NUTRITIONIST) {
            emailService.sendPendingEmailToNutritionist(user.getEmail(), request.getFirstName(), request.getLastName());
        }
        
        loggingService.info("AUTH", "REGISTER", String.valueOf(user.getUserId()), user.getEmail(), 
            "Utente registrato con ruolo: " + request.getRole() + " presso: " + request.getAddress());

        return AuthResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .message(message)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativo di login per email: {}", request.getEmail());
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email obbligatoria");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password obbligatoria");
        }
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            String errorMessage = e.getMessage();
            loggingService.error("AUTH", "LOGIN", null, request.getEmail(), 
                "Tentativo di login fallito", errorMessage);
            
            if (errorMessage != null && errorMessage.contains("disabilitato")) {
                throw new RuntimeException(errorMessage);
            } else if (errorMessage != null && errorMessage.contains("attesa di revisione")) {
                throw new RuntimeException(errorMessage);
            } else if (errorMessage != null && errorMessage.contains("attesa di approvazione")) {
                throw new RuntimeException(errorMessage);
            } else if (errorMessage != null && errorMessage.contains("disabilitato permanentemente")) {
                throw new RuntimeException(errorMessage);
            } else {
                throw new RuntimeException("Email o password non validi");
            }
        } catch (Exception e) {
            log.error("Errore imprevisto durante il login: {}", e.getMessage());
            throw new RuntimeException("Errore durante il login. Riprova più tardi.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email o password non validi"));

        UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Profilo utente non trovato"));

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .message("Login effettuato con successo")
                .build();
    }
}