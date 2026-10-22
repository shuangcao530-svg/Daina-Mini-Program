package com.xiaosuda.service;

import com.xiaosuda.dto.LoginRequest;
import com.xiaosuda.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse check(Long userId);
}
