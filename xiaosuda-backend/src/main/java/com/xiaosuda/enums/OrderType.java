package com.xiaosuda.enums;

import lombok.Getter;

@Getter
public enum OrderType {
    EXPRESS("快递", "📦"),
    FOOD("外卖", "🍱"),
    FILE("文件", "📄"),
    KEY("钥匙", "🔑"),
    SHOPPING("代购", "🛒"),
    OTHER("其他", "📋");

    private final String label;
    private final String icon;

    OrderType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }
}
