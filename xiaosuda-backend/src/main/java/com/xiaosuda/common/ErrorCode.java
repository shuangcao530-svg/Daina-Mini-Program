package com.xiaosuda.common;

import lombok.Getter;

/**
 * 业务错误码枚举
 */
@Getter
public enum ErrorCode {

    // 通用错误
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 (1000-1099)
    USER_NOT_FOUND(1001, "用户不存在"),
    BALANCE_INSUFFICIENT(1002, "余额不足"),
    WITHDRAW_AMOUNT_INVALID(1003, "提现金额无效"),

    // 订单相关 (2000-2099)
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_INVALID(2002, "订单状态不允许此操作"),
    ORDER_ALREADY_ACCEPTED(2003, "订单已被其他跑腿员接单"),
    ORDER_NOT_YOURS(2004, "您不是该订单的跑腿员"),
    ORDER_CANNOT_CANCEL(2005, "订单当前状态不可取消"),
    ORDER_CANNOT_EDIT(2006, "订单当前状态不可编辑"),
    ORDER_IMAGE_REQUIRED(2007, "请至少上传一张物品照片"),
    ORDER_CANNOT_CONFIRM(2008, "只有下单人才能确认完成"),

    // 文件相关 (3000-3099)
    FILE_UPLOAD_FAILED(3001, "文件上传失败"),
    FILE_TYPE_INVALID(3002, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(3003, "文件大小超出限制"),

    // 认证相关 (4000-4099)
    LOGIN_FAILED(4001, "登录失败"),
    TOKEN_INVALID(4002, "Token无效"),
    TOKEN_EXPIRED(4003, "Token已过期");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
