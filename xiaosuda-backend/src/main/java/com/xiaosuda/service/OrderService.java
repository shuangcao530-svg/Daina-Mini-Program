package com.xiaosuda.service;

import com.xiaosuda.common.PageResult;
import com.xiaosuda.dto.CreateOrderRequest;
import com.xiaosuda.dto.OrderVO;
import com.xiaosuda.dto.StatsVO;

import java.util.List;

public interface OrderService {
    OrderVO create(Long userId, CreateOrderRequest request);
    PageResult<OrderVO> list(Long userId, String status, String type, int page, int size, boolean mine);
    OrderVO detail(Long orderId, Long currentUserId);
    OrderVO update(Long userId, Long orderId, CreateOrderRequest request);
    void cancel(Long userId, Long orderId);

    // Runner actions
    OrderVO accept(Long runnerId, Long orderId);
    OrderVO pickup(Long runnerId, Long orderId, List<String> images);
    OrderVO deliver(Long runnerId, Long orderId);
    OrderVO complete(Long runnerId, Long orderId, List<String> images);

    // Customer confirms order completion → money transfers to runner
    OrderVO confirm(Long userId, Long orderId);

    // Stats
    StatsVO getRunnerStats(Long runnerId);
}
