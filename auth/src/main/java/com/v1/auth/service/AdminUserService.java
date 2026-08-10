package com.v1.auth.service;

import com.v1.auth.dto.PendingUserResponse;
import com.v1.auth.dto.UserApprovalActionResponse;

import java.util.List;

public interface AdminUserService {
    List<PendingUserResponse> getPendingUsers();

    UserApprovalActionResponse approveUser(Long userId, String adminUsername);

    UserApprovalActionResponse rejectUser(Long userId, String adminUsername, String reason);
}
