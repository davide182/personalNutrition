package com.nutritionists.model.dto.response;

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

public class AdminUserResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private UserStatus status;
}