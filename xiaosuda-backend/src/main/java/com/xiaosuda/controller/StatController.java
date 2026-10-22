package com.xiaosuda.controller;

import com.xiaosuda.common.Result;
import com.xiaosuda.dto.StatsVO;
import com.xiaosuda.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "统计", description = "跑腿员/用户统计数据")
@RestController
@RequestMapping("/api/stats")
public class StatController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "跑腿员统计")
    @GetMapping("/runner")
    public Result<StatsVO> runnerStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.getRunnerStats(userId));
    }
}
