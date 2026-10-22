package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String school;
    private String role;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal totalEarned;
    private Integer totalOrders;
    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
