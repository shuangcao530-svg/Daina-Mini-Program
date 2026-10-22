package com.xiaosuda.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("user"),
    RUNNER("runner"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}
