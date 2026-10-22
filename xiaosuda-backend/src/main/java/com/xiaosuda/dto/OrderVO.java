package com.xiaosuda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long runnerId;
    private String type;
    private String typeIcon;
    private String title;        // type + "取件" --> "快递取件"
    private String from;         // = pickupLocation (frontend names)
    private String to;            // = deliveryLocation
    private String pickupCode;
    private String urgency;
    private String urgentKey;    // "normal" / "urgent" for CSS
    private BigDecimal fee;
    private String note;
    private String contact;      // contactName + " " + contactPhone
    private String status;
    private String statusKey;    // "pending" / "accepted" / etc for CSS
    private List<String> userImages;
    private List<String> pickupImages;
    private List<String> deliveryImages;
    private Integer earned;      // frontend uses int
    private String createdAt;
    private List<TimelineVO> timeline;
}
