package com.xiaosuda.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    PENDING("待接单"),
    ACCEPTED("已接单"),
    PICKED("已取件"),
    DELIVERING("配送中"),
    CONFIRMING("待确认"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public static boolean canAccept(String status) {
        return PENDING.label.equals(status);
    }

    public static boolean canPickup(String status) {
        return ACCEPTED.label.equals(status);
    }

    public static boolean canDeliver(String status) {
        return PICKED.label.equals(status);
    }

    /**
     * 跑腿员上传送达凭证后，订单进入"待确认"状态
     */
    public static boolean canComplete(String status) {
        return DELIVERING.label.equals(status);
    }

    /**
     * 下单人确认完成后，订单变为"已完成"
     */
    public static boolean canConfirm(String status) {
        return CONFIRMING.label.equals(status);
    }

    public static boolean canEdit(String status) {
        return PENDING.label.equals(status);
    }

    public static boolean canCancel(String status) {
        return PENDING.label.equals(status);
    }

    public static boolean isFinal(String status) {
        return COMPLETED.label.equals(status) || CANCELLED.label.equals(status);
    }
}
