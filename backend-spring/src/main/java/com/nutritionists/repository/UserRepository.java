package com.nutritionists.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.Role;
import com.nutritionists.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail (String email);
    boolean existsByEmail(String email);
    List<User> findByRole (Role role);
    
}
