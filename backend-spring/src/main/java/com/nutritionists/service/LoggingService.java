package com.nutritionists.service;

import com.nutritionists.model.entity.SystemLog;
import com.nutritionists.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingService {

    private final SystemLogRepository systemLogRepository;

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public void info(String service, String action, String userId, String email, String message) {
        saveLog("INFO", service, action, userId, email, message, null);
    }

    public void warn(String service, String action, String userId, String email, String message, String details) {
        saveLog("WARN", service, action, userId, email, message, details);
    }

    public void error(String service, String action, String userId, String email, String message, String details) {
        saveLog("ERROR", service, action, userId, email, message, details);
        log.error("[{}] {} - {}: {}", service, action, email, message);
    }

    public void infoWithCorrelation(String correlationId, String service, String action, String userId, String email, String message) {
        SystemLog systemLog = SystemLog.builder()
                .level("INFO")
                .service(service)
                .action(action)
                .userId(userId)
                .email(email)
                .message(message)
                .correlationId(correlationId)
                .build();
        systemLogRepository.save(systemLog);
    }

    private void saveLog(String level, String service, String action, String userId, String email, String message, String details) {
        SystemLog systemLog = SystemLog.builder()
                .level(level)
                .service(service)
                .action(action)
                .userId(userId)
                .email(email)
                .message(message)
                .details(details)
                .build();
        systemLogRepository.save(systemLog);
        
        if ("ERROR".equals(level)) {
            log.error("📝 [MONGO] {} - {}: {}", service, action, message);
        } else if ("WARN".equals(level)) {
            log.warn("📝 [MONGO] {} - {}: {}", service, action, message);
        } else {
            log.info("📝 [MONGO] {} - {}: {}", service, action, message);
        }
    }
}