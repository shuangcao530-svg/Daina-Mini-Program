package com.xiaosuda.controller;

import com.xiaosuda.common.BusinessException;
import com.xiaosuda.common.ErrorCode;
import com.xiaosuda.common.Result;
import com.xiaosuda.dto.LoginRequest;
import com.xiaosuda.dto.LoginResponse;
import com.xiaosuda.service.AuthService;
import com.xiaosuda.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "登录认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "微信登录 / Mock登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "校验Token")
    @GetMapping("/check")
    public Result<LoginResponse> check(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        return Result.ok(authService.check(userId));
    }
}
