package com.nutritionists.repository;

import com.nutritionists.model.entity.SystemLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends MongoRepository<SystemLog, String> {
    List<SystemLog> findByServiceAndTimestampBetween(String service, LocalDateTime from, LocalDateTime to);
    List<SystemLog> findByLevel(String level);
    List<SystemLog> findByEmail(String email);
    List<SystemLog> findByAction(String action);
}