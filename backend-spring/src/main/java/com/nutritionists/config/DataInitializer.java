package com.nutritionists.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.Specialization;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.UserProfile;
import com.nutritionists.model.entity.enums.UserStatus;
import com.nutritionists.repository.SpecializationRepository;
import com.nutritionists.repository.UserProfileRepository;
import com.nutritionists.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner{

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args){
        createAdminIfNotExists();
        createDefaultSpecializations();
    }

    private void createAdminIfNotExists(){
        String adminEmail = "admin@nutritionists.it";
        if(userRepository.existsByEmail(adminEmail)){
            log.info("Email già esistente: {}", adminEmail);
            return;
        }
        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);

        UserProfile profile = UserProfile.builder()
                .user(admin)
                .firstName("Admin")
                .lastName("Sistema")
                .build();
        userProfileRepository.save(profile);
        log.info("Admin creato: {} / admin123" , adminEmail);
    }
    
    private void createDefaultSpecializations() {
        String[][] specializations = {
            {"Nutrizione clinica", "Specializzata nella gestione di patologie metaboliche e malattie croniche"},
            {"Nutrizione sportiva", "Supporto nutrizionale per atleti e sportivi"},
            {"Disturbi alimentari", "Supporto per disturbi del comportamento alimentare (DCA)"},
            {"Pediatria nutrizionale", "Nutrizione per bambini e adolescenti"},
            {"Nutrizione vegetariana/vegana", "Piani alimentari per diete vegetariane e vegane"},
            {"Metabolismo e dimagrimento", "Supporto per perdita di peso e metabolismo"},
            {"Nutrizione in gravidanza", "Supporto nutrizionale durante gravidanza e allattamento"},
            {"Gastroenterologia nutrizionale", "Supporto per patologie gastrointestinali"},
            {"Nutrizione geriatrica", "Nutrizione per anziani e terza età"},
            {"Nutrizione estetica", "Supporto per pelle, capelli e benessere estetico"}
        };
        
        for (String[] spec : specializations) {
            if (!specializationRepository.existsByName(spec[0])) {
                Specialization specialization = Specialization.builder()
                        .name(spec[0])
                        .description(spec[1])
                        .build();
                specializationRepository.save(specialization);
                log.info("Specializzazione creata: {}", spec[0]);
            }
        }
    }
}