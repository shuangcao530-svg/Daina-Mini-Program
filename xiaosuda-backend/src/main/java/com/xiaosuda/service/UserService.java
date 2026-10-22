package com.xiaosuda.service;

import com.xiaosuda.dto.StatsVO;
import com.xiaosuda.dto.UpdateProfileRequest;
import com.xiaosuda.dto.UserVO;

import java.math.BigDecimal;

public interface UserService {
    UserVO getProfile(Long userId);
    UserVO updateProfile(Long userId, UpdateProfileRequest request);
    BigDecimal getBalance(Long userId);
    void withdraw(Long userId, BigDecimal amount);
    StatsVO getStats(Long userId);
}
