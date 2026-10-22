package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("delivery_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long runnerId;
    private String type;
    private String pickupLocation;
    private String deliveryLocation;
    private String pickupCode;
    private String urgency;
    private BigDecimal fee;
    private BigDecimal platformFeeRate;
    private BigDecimal runnerEarned;
    private String note;
    private String contactName;
    private String contactPhone;
    private String status;
    private String images;         // JSON array string
    private String pickupImages;   // JSON array string
    private String deliveryImages; // JSON array string
    private String cancelReason;
    private Long cancelBy;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
