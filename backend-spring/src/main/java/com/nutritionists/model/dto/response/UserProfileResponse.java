package com.nutritionists.model.dto.response;

import com.nutritionists.model.entity.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Double latitude;
    private Double longitude;
    private Role role;
}