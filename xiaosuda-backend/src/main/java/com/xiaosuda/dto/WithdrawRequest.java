package com.xiaosuda.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest {
    private BigDecimal amount;
}
