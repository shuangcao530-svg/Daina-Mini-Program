package com.xiaosuda.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String code;
    private String role;  // for mock login
}
