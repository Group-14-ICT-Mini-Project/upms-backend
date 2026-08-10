package com.v1.auth.service;

import com.v1.auth.dto.AdminUserResponse;
import com.v1.auth.dto.PendingUserResponse;
import com.v1.auth.dto.RoleResponse;
import com.v1.auth.dto.UpdateAdminUserRequest;
import com.v1.auth.dto.UserApprovalActionResponse;

import java.util.List;

public interface AdminUserService {
    List<AdminUserResponse> getAllUsers();

    List<PendingUserResponse> getPendingUsers();

    List<RoleResponse> getRoles();

    UserApprovalActionResponse approveUser(Long userId, String adminUsername);

    UserApprovalActionResponse rejectUser(Long userId, String adminUsername, String reason);

    AdminUserResponse updateUser(Long userId, UpdateAdminUserRequest request, String adminUsername);

    void deleteUser(Long userId, String adminUsername);
}
