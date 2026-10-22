package com.xiaosuda.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String type;
    private String pickupLocation;
    private String deliveryLocation;
    private String pickupCode;
    private String urgency;
    private BigDecimal fee;
    private String note;
    private String contactName;
    private String contactPhone;
    private List<String> images;  // photo URLs
}
