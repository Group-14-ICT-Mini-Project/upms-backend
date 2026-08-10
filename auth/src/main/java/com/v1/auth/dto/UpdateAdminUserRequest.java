package com.v1.auth.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateAdminUserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String faculty;
    private String department;
    private Boolean isActive;
    private String approvalStatus;
    private Set<String> roles;
}
