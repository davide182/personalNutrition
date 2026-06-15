package com.nutritionists.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.User;
import com.nutritionists.model.entity.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail (String email);
    boolean existsByEmail(String email);
    List<User> findByRole (Role role);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    List<User> findByStatus(UserStatus status);
    Optional<User> findByResetToken(String token);
    
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND (u.status = 'ACTIVE' OR u.status = 'SELF_DISABLED')")
    Optional<User> findActiveOrSelfDisabledById(@Param("userId") Long userId);

}
