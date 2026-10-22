package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_timeline")
public class OrderTimeline {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String status;
    private String label;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
