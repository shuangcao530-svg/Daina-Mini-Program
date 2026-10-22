package com.xiaosuda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String school;
    private String role;
    private BigDecimal balance;
    private BigDecimal totalEarned;
    private Integer totalOrders;
}
