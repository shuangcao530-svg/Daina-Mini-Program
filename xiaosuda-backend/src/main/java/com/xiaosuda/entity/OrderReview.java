package com.xiaosuda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单评价 —— 完成订单后双方互评
 */
@Data
@TableName("order_review")
public class OrderReview {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long reviewerId;     // 评价人
    private Long targetUserId;   // 被评价人
    private Integer rating;      // 1-5 星
    private String content;
    private String tags;         // JSON数组: ["快速","友好"]

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
