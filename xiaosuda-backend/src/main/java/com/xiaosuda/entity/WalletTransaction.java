package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录 —— 每一笔余额变动都有迹可循
 */
@Data
@TableName("wallet_transaction")
public class WalletTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long orderId;
    private String type;        // earn / withdraw / freeze / unfreeze / adjust
    private BigDecimal amount;  // 正=收入, 负=支出
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
