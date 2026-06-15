package com.nutritionists.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nutritionists.model.entity.SystemLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    private final MongoTemplate mongoTemplate;

    /**
     * Recupera i log di sistema da MongoDB
     * @param level  Filtro per livello (INFO, WARN, ERROR)
     * @param service  Filtro per servizio (AUTH, APPOINTMENT, ADMIN, FLASK)
     * @param limit  Numero massimo di log da restituire (default 100)
     */
    @GetMapping("/logs")
    public ResponseEntity<List<SystemLog>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service,
            @RequestParam(defaultValue = "100") int limit) {
        
        log.info("Admin richiede log - level={}, service={}, limit={}", level, service, limit);
        
        Query query = new Query();
        
        if (level != null && !level.isEmpty()) {
            query.addCriteria(Criteria.where("level").is(level.toUpperCase()));
        }
        
        if (service != null && !service.isEmpty()) {
            query.addCriteria(Criteria.where("service").is(service.toUpperCase()));
        }
        
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.limit(Math.min(limit, 500)); 
        
        List<SystemLog> logs = mongoTemplate.find(query, SystemLog.class);
        
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/stats")
    public ResponseEntity<?> getLogStats() {
        long totalLogs = mongoTemplate.count(new Query(), SystemLog.class);
        long errorLogs = mongoTemplate.count(Query.query(Criteria.where("level").is("ERROR")), SystemLog.class);
        long warnLogs = mongoTemplate.count(Query.query(Criteria.where("level").is("WARN")), SystemLog.class);
        
        return ResponseEntity.ok(Map.of(
            "totalLogs", totalLogs,
            "errorLogs", errorLogs,
            "warnLogs", warnLogs,
            "infoLogs", totalLogs - errorLogs - warnLogs
        ));
    }
}