package com.xiaosuda.controller;

import com.xiaosuda.common.Result;
import com.xiaosuda.entity.CampusPoint;
import com.xiaosuda.service.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "校园", description = "校园地点")
@RestController
@RequestMapping("/api/campus")
public class CampusController {

    @Autowired
    private CampusService campusService;

    @Operation(summary = "校园地点列表")
    @GetMapping("/points")
    public Result<List<CampusPoint>> points() {
        return Result.ok(campusService.list());
    }
}
