package com.nutritionists.model.dto;

import com.nutritionists.model.entity.enums.Role;
import com.nutritionists.model.entity.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class AuthResponse {
    
    private String token;
    private String email;
    private Role role;
    private UserStatus status;
    private String firstName;
    private String lastName;
    private String message; 

    public boolean hasToken(){
        return token != null && !token.isEmpty();
    }
}
