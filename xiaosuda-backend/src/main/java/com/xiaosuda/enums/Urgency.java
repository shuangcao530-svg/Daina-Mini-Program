package com.xiaosuda.enums;

import lombok.Getter;

@Getter
public enum Urgency {
    NORMAL("普通"),
    URGENT("加急"),
    EXPRESS("极速");

    private final String label;

    Urgency(String label) {
        this.label = label;
    }
}
