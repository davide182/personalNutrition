package com.nutritionists.model.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Document(collection = "system_logs")
public class SystemLog {
    @Id
    private String id;
    private String level;
    private String service;
    private String action;
    private String userId;
    private String email;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String correlationId;

    public SystemLog() {}

    public SystemLog(String level, String service, String action, String userId, String email, String message, String details, String correlationId) {
        this.level = level;
        this.service = service;
        this.action = action;
        this.userId = userId;
        this.email = email;
        this.message = message;
        this.details = details;
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
    }

    public static SystemLogBuilder builder() {
        return new SystemLogBuilder();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public static class SystemLogBuilder {
        private String level;
        private String service;
        private String action;
        private String userId;
        private String email;
        private String message;
        private String details;
        private String correlationId;

        public SystemLogBuilder level(String level) { this.level = level; return this; }
        public SystemLogBuilder service(String service) { this.service = service; return this; }
        public SystemLogBuilder action(String action) { this.action = action; return this; }
        public SystemLogBuilder userId(String userId) { this.userId = userId; return this; }
        public SystemLogBuilder email(String email) { this.email = email; return this; }
        public SystemLogBuilder message(String message) { this.message = message; return this; }
        public SystemLogBuilder details(String details) { this.details = details; return this; }
        public SystemLogBuilder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

        public SystemLog build() {
            return new SystemLog(level, service, action, userId, email, message, details, correlationId);
        }
    }
}