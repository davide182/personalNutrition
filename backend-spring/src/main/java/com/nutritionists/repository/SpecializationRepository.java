package com.nutritionists.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.Specialization;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long>{
    Optional<Specialization> findByName (String name);  
    
    boolean existsByName (String name);
}
