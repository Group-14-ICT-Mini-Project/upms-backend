package com.v1.auth.service;

import com.v1.auth.dto.LoginRequest;
import com.v1.auth.dto.LoginResponse;
import com.v1.auth.dto.RefreshTokenRequest;
import com.v1.auth.dto.SignupRequest;
import com.v1.auth.dto.SignupResponse;

public interface AuthenticationService {

	LoginResponse login(LoginRequest request);

	SignupResponse signup(SignupRequest request);

	LoginResponse refreshToken(RefreshTokenRequest request);

	void verifyToken(String authHeader);

	void logout(String authHeader);
}
