package com.xiaosuda.controller;

import com.xiaosuda.common.Result;
import com.xiaosuda.dto.StatsVO;
import com.xiaosuda.dto.UpdateProfileRequest;
import com.xiaosuda.dto.UserVO;
import com.xiaosuda.dto.WithdrawRequest;
import com.xiaosuda.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户", description = "用户信息相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<UserVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getProfile(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(HttpServletRequest request,
                                         @RequestBody UpdateProfileRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.updateProfile(userId, req));
    }

    @Operation(summary = "获取余额")
    @GetMapping("/balance")
    public Result<java.math.BigDecimal> getBalance(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getBalance(userId));
    }

    @Operation(summary = "提现")
    @PostMapping("/withdraw")
    public Result<?> withdraw(HttpServletRequest request,
                               @RequestBody WithdrawRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        userService.withdraw(userId, req.getAmount());
        return Result.ok("提现成功");
    }

    @Operation(summary = "用户统计")
    @GetMapping("/stats")
    public Result<StatsVO> getStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getStats(userId));
    }
}
