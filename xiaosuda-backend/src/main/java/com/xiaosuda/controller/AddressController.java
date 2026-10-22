package com.xiaosuda.controller;

import com.xiaosuda.common.Result;
import com.xiaosuda.entity.Address;
import com.xiaosuda.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "地址", description = "收货/取件地址管理")
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Operation(summary = "地址列表")
    @GetMapping
    public Result<List<Address>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(addressService.list(userId));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<Address> create(HttpServletRequest request,
                                   @RequestBody Address address) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(addressService.create(userId, address));
    }

    @Operation(summary = "编辑地址")
    @PutMapping("/{id}")
    public Result<Address> update(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody Address address) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(addressService.update(userId, id, address));
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<?> delete(HttpServletRequest request,
                             @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.delete(userId, id);
        return Result.ok();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public Result<?> setDefault(HttpServletRequest request,
                                 @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.setDefault(userId, id);
        return Result.ok();
    }
}
