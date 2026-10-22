package com.xiaosuda.controller;

import com.xiaosuda.common.PageResult;
import com.xiaosuda.common.Result;
import com.xiaosuda.dto.CreateOrderRequest;
import com.xiaosuda.dto.ImageUploadRequest;
import com.xiaosuda.dto.OrderVO;
import com.xiaosuda.dto.StatsVO;
import com.xiaosuda.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单", description = "订单CRUD及跑腿操作")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ==================== Order CRUD ====================

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> create(HttpServletRequest request,
                                   @RequestBody CreateOrderRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.create(userId, req));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean mine) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.list(userId, status, type, page, size, mine));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.detail(id, userId));
    }

    @Operation(summary = "编辑订单")
    @PutMapping("/{id}")
    public Result<OrderVO> update(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody CreateOrderRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.update(userId, id, req));
    }

    @Operation(summary = "取消订单")
    @DeleteMapping("/{id}")
    public Result<?> cancel(HttpServletRequest request,
                             @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancel(userId, id);
        return Result.ok();
    }

    // ==================== Runner Actions ====================

    @Operation(summary = "接单")
    @PostMapping("/{id}/accept")
    public Result<OrderVO> accept(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.accept(userId, id));
    }

    @Operation(summary = "上传取件照")
    @PostMapping("/{id}/pickup")
    public Result<OrderVO> pickup(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody ImageUploadRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.pickup(userId, id,
                req != null ? req.getImages() : null));
    }

    @Operation(summary = "开始配送")
    @PostMapping("/{id}/deliver")
    public Result<OrderVO> deliver(HttpServletRequest request,
                                    @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.deliver(userId, id));
    }

    @Operation(summary = "上传送达照，完成订单")
    @PostMapping("/{id}/complete")
    public Result<OrderVO> complete(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestBody ImageUploadRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.complete(userId, id,
                req != null ? req.getImages() : null));
    }

    // ==================== Customer Confirm ====================

    @Operation(summary = "下单人确认订单完成，报酬到账跑腿员")
    @PostMapping("/{id}/confirm")
    public Result<OrderVO> confirm(HttpServletRequest request,
                                    @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.confirm(userId, id));
    }
}
