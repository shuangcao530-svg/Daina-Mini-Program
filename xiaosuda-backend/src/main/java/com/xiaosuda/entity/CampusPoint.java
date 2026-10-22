package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_point")
public class CampusPoint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String category;
    private String building;
    private Integer sortOrder;
    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
