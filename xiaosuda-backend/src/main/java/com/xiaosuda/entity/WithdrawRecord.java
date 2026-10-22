package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录 —— 每一笔提现都有申请、处理、完成的状态流转
 */
@Data
@TableName("withdraw_record")
public class WithdrawRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private BigDecimal amount;
    private String status;      // pending / processing / completed / rejected
    private String accountType; // wechat / alipay / bank
    private String accountInfo;
    private String remark;
    private LocalDateTime processedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
