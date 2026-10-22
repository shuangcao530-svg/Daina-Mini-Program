package com.xiaosuda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaosuda.common.BusinessException;
import com.xiaosuda.common.ErrorCode;
import com.xiaosuda.common.PageResult;
import com.xiaosuda.dto.CreateOrderRequest;
import com.xiaosuda.dto.OrderVO;
import com.xiaosuda.dto.StatsVO;
import com.xiaosuda.dto.TimelineVO;
import com.xiaosuda.entity.Order;
import com.xiaosuda.entity.OrderTimeline;
import com.xiaosuda.entity.User;
import com.xiaosuda.entity.WalletTransaction;
import com.xiaosuda.enums.OrderStatus;
import com.xiaosuda.mapper.OrderMapper;
import com.xiaosuda.mapper.OrderTimelineMapper;
import com.xiaosuda.mapper.UserMapper;
import com.xiaosuda.mapper.WalletTransactionMapper;
import com.xiaosuda.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderTimelineMapper timelineMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private com.xiaosuda.mapper.ChatMessageMapper chatMessageMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Daily order sequence counter (fallback when no Redis)
    private final AtomicInteger dailySeq = new AtomicInteger(0);
    private volatile String lastDate = "";

    // Type-icon mapping
    private static final Map<String, String> TYPE_ICONS = Map.of(
            "快递", "📦", "外卖", "🍱", "文件", "📄", "钥匙", "🔑", "代购", "🛒", "其他", "📋"
    );

    @Override
    @Transactional
    public OrderVO create(Long userId, CreateOrderRequest req) {
        if (req.getPickupLocation() == null || req.getPickupLocation().isEmpty()
                || req.getDeliveryLocation() == null || req.getDeliveryLocation().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写取件和送达地点");
        }
        if (req.getImages() == null || req.getImages().isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_IMAGE_REQUIRED);
        }
        if (req.getFee() == null || req.getFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请输入合理的配送费");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setType(req.getType() != null ? req.getType() : "其他");
        order.setPickupLocation(req.getPickupLocation());
        order.setDeliveryLocation(req.getDeliveryLocation());
        order.setPickupCode(req.getPickupCode());
        order.setUrgency(req.getUrgency() != null ? req.getUrgency() : "普通");
        order.setFee(req.getFee());
        order.setPlatformFeeRate(new BigDecimal("0.10"));  // 平台服务费 10%
        order.setNote(req.getNote());
        order.setContactName(req.getContactName());
        order.setContactPhone(req.getContactPhone());
        order.setStatus(OrderStatus.PENDING.getLabel());
        try {
            order.setImages(objectMapper.writeValueAsString(req.getImages()));
        } catch (JsonProcessingException e) {
            order.setImages("[]");
        }
        orderMapper.insert(order);

        // Timeline
        addTimeline(order.getId(), order.getStatus(), "任务已发布", userId);

        return toOrderVO(order);
    }

    @Override
    public PageResult<OrderVO> list(Long userId, String status, String type, int page, int size, boolean mine) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty() && !"全部".equals(status)) {
            if ("进行中".equals(status)) {
                wrapper.in(Order::getStatus, "已接单", "已取件", "配送中", "待确认");
            } else {
                wrapper.eq(Order::getStatus, status);
            }
        }
        if (type != null && !type.isEmpty() && !"全部".equals(type)) {
            wrapper.eq(Order::getType, type);
        }
        if (userId != null) {
            if (mine) {
                // "我的订单"：只看自己下的单
                wrapper.eq(Order::getUserId, userId);
            } else {
                // 跑腿市场：排除自己的单（不能接自己发的单）
                wrapper.ne(Order::getUserId, userId);
            }
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> mpPage = new Page<>(page, size);
        Page<Order> result = orderMapper.selectPage(mpPage, wrapper);

        List<OrderVO> records = result.getRecords().stream()
                .map(this::toOrderVO)
                .toList();

        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public OrderVO detail(Long orderId, Long currentUserId) {
        Order order = getOrder(orderId);
        OrderVO vo = toOrderVO(order);
        // Load timeline
        vo.setTimeline(getTimelines(orderId));
        // Privacy: only show pickupCode to creator or runner
        if (!currentUserId.equals(order.getUserId())
                && (order.getRunnerId() == null || !currentUserId.equals(order.getRunnerId()))) {
            vo.setPickupCode(null);
        }
        return vo;
    }

    @Override
    @Transactional
    public OrderVO update(Long userId, Long orderId, CreateOrderRequest req) {
        Order order = getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_YOURS);
        }
        if (!OrderStatus.canEdit(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_EDIT);
        }

        if (req.getPickupLocation() != null) order.setPickupLocation(req.getPickupLocation());
        if (req.getDeliveryLocation() != null) order.setDeliveryLocation(req.getDeliveryLocation());
        if (req.getPickupCode() != null) order.setPickupCode(req.getPickupCode());
        if (req.getUrgency() != null) order.setUrgency(req.getUrgency());
        if (req.getFee() != null) order.setFee(req.getFee());
        if (req.getNote() != null) order.setNote(req.getNote());
        if (req.getContactName() != null) order.setContactName(req.getContactName());
        if (req.getContactPhone() != null) order.setContactPhone(req.getContactPhone());
        if (req.getImages() != null) {
            try {
                order.setImages(objectMapper.writeValueAsString(req.getImages()));
            } catch (JsonProcessingException ignored) {}
        }
        orderMapper.updateById(order);
        return toOrderVO(order);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_YOURS);
        }
        if (!OrderStatus.canCancel(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        order.setStatus(OrderStatus.CANCELLED.getLabel());
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelBy(userId);
        order.setCancelReason("用户主动取消");
        orderMapper.updateById(order);
        addTimeline(order.getId(), order.getStatus(), "下单人取消订单", userId);
    }

    // ==================== Runner Actions ====================

    @Override
    @Transactional
    public OrderVO accept(Long runnerId, Long orderId) {
        Order order = getOrder(orderId);
        if (!OrderStatus.canAccept(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (order.getUserId().equals(runnerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能接自己的订单");
        }

        order.setRunnerId(runnerId);
        order.setStatus(OrderStatus.ACCEPTED.getLabel());
        int rows = orderMapper.updateById(order);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_ACCEPTED);
        }

        addTimeline(order.getId(), order.getStatus(), "跑腿员已接单", runnerId);

        // 系统消息：通知下单人
        sendSystemMsg(order, runnerId, order.getUserId(),
                "跑腿员已接单，可以开始聊天沟通配送细节");

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO pickup(Long runnerId, Long orderId, List<String> images) {
        Order order = getOrder(orderId);
        checkRunner(order, runnerId);
        if (!OrderStatus.canPickup(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.PICKED.getLabel());
        if (images != null && !images.isEmpty()) {
            try {
                order.setPickupImages(objectMapper.writeValueAsString(images));
            } catch (JsonProcessingException ignored) {}
        }
        orderMapper.updateById(order);

        addTimeline(order.getId(), order.getStatus(), "跑腿员上传取件凭证", runnerId);

        sendSystemMsg(order, runnerId, order.getUserId(), "跑腿员已取件，正在准备配送");

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO deliver(Long runnerId, Long orderId) {
        Order order = getOrder(orderId);
        checkRunner(order, runnerId);
        if (!OrderStatus.canDeliver(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.DELIVERING.getLabel());
        orderMapper.updateById(order);

        addTimeline(order.getId(), order.getStatus(), "跑腿员开始配送", runnerId);

        sendSystemMsg(order, runnerId, order.getUserId(), "跑腿员已开始配送，请留意收货");

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO complete(Long runnerId, Long orderId, List<String> images) {
        Order order = getOrder(orderId);
        checkRunner(order, runnerId);
        if (!OrderStatus.canComplete(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        // 跑腿员上传送达凭证 → 进入"待确认"状态，等待下单人确认
        order.setStatus(OrderStatus.CONFIRMING.getLabel());
        if (images != null && !images.isEmpty()) {
            try {
                order.setDeliveryImages(objectMapper.writeValueAsString(images));
            } catch (JsonProcessingException ignored) {}
        }
        // Calculate runner earnings with platform fee (default 10%)
        BigDecimal fee = order.getFee() != null ? order.getFee() : BigDecimal.ZERO;
        BigDecimal platformRate = order.getPlatformFeeRate() != null ? order.getPlatformFeeRate() : new BigDecimal("0.10");
        BigDecimal runnerEarned = fee.multiply(BigDecimal.ONE.subtract(platformRate));
        order.setRunnerEarned(runnerEarned);
        orderMapper.updateById(order);

        addTimeline(order.getId(), order.getStatus(), "跑腿员上传送达凭证，等待下单人确认", runnerId);

        // 通知下单人去确认
        sendSystemMsg(order, runnerId, order.getUserId(),
                "跑腿员已上传送达凭证，请确认订单完成（确认后报酬将到账）");

        // 注意：此时钱还没有到跑腿员余额，需要下单人确认后才能到账
        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO confirm(Long userId, Long orderId) {
        Order order = getOrder(orderId);
        // 只有下单人才能确认完成
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CONFIRM);
        }
        if (!OrderStatus.canConfirm(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.COMPLETED.getLabel());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        addTimeline(order.getId(), order.getStatus(), "下单人确认完成，报酬已到账", userId);

        // 下单人确认后，钱才到跑腿员余额
        Long runnerId = order.getRunnerId();

        // 通知跑腿员报酬已到账
        if (runnerId != null) {
            sendSystemMsg(order, userId, runnerId,
                    "下单人已确认完成，报酬已到账 ¥" + order.getRunnerEarned());
        }
        if (runnerId != null) {
            BigDecimal runnerEarned = order.getRunnerEarned() != null ? order.getRunnerEarned() : BigDecimal.ZERO;
            User runner = userMapper.selectById(runnerId);
            if (runner != null) {
                BigDecimal balanceBefore = runner.getBalance();
                runner.setBalance(runner.getBalance().add(runnerEarned));
                runner.setTotalEarned(runner.getTotalEarned().add(runnerEarned));
                runner.setTotalOrders((runner.getTotalOrders() != null ? runner.getTotalOrders() : 0) + 1);
                userMapper.updateById(runner);

                // 记录钱包流水 —— 订单收入
                WalletTransaction tx = new WalletTransaction();
                tx.setUserId(runnerId);
                tx.setOrderId(order.getId());
                tx.setType("earn");
                tx.setAmount(runnerEarned);
                tx.setBalanceBefore(balanceBefore);
                tx.setBalanceAfter(runner.getBalance());
                tx.setDescription("订单 " + order.getOrderNo() + " 配送收入（下单人已确认）");
                walletTransactionMapper.insert(tx);
            }
        }

        return toOrderVO(order);
    }

    // ==================== Stats ====================

    @Override
    public StatsVO getRunnerStats(Long runnerId) {
        // Available orders (status = 待接单)
        Long available = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, "待接单"));

        // Today's completed
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        Long today = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getRunnerId, runnerId)
                        .eq(Order::getStatus, "已完成")
                        .between(Order::getUpdatedAt, todayStart, todayEnd));

        // Today's earnings
        List<Order> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getRunnerId, runnerId)
                        .eq(Order::getStatus, "已完成")
                        .between(Order::getUpdatedAt, todayStart, todayEnd));
        BigDecimal earned = todayOrders.stream()
                .map(o -> o.getFee() != null ? o.getFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StatsVO.builder()
                .available(available.intValue())
                .today(today.intValue())
                .earned(earned)
                .build();
    }

    // ==================== Private Helpers ====================

    private Order getOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void checkRunner(Order order, Long runnerId) {
        if (order.getRunnerId() == null || !order.getRunnerId().equals(runnerId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_YOURS);
        }
    }

    private void addTimeline(Long orderId, String status, String label, Long operatorId) {
        OrderTimeline tl = new OrderTimeline();
        tl.setOrderId(orderId);
        tl.setStatus(status);
        tl.setLabel(label);
        tl.setOperatorId(operatorId);
        timelineMapper.insert(tl);
    }

    /** 发送系统消息到聊天 */
    private void sendSystemMsg(Order order, Long senderId, Long receiverId, String content) {
        if (receiverId == null) return;
        com.xiaosuda.entity.ChatMessage msg = new com.xiaosuda.entity.ChatMessage();
        msg.setOrderId(order.getId());
        msg.setSenderId(senderId != null ? senderId : 0L);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(false);
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(msg);
    }

    private List<TimelineVO> getTimelines(Long orderId) {
        List<OrderTimeline> timelines = timelineMapper.selectList(
                new LambdaQueryWrapper<OrderTimeline>()
                        .eq(OrderTimeline::getOrderId, orderId)
                        .orderByAsc(OrderTimeline::getCreatedAt));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return timelines.stream()
                .map(t -> TimelineVO.builder()
                        .label(t.getLabel())
                        .time(t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "")
                        .build())
                .toList();
    }

    private OrderVO toOrderVO(Order o) {
        List<String> userImgs = parseJsonArray(o.getImages());
        List<String> pickupImgs = parseJsonArray(o.getPickupImages());
        List<String> deliveryImgs = parseJsonArray(o.getDeliveryImages());

        // Combine contact
        String contact = "";
        if (o.getContactName() != null) contact += o.getContactName();
        if (o.getContactPhone() != null) contact += " " + o.getContactPhone();
        contact = contact.trim();

        // Title: type + "取件"
        String title = (o.getType() != null ? o.getType() : "订单") + "取件";

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return OrderVO.builder()
                .id(o.getId())
                .orderNo(o.getOrderNo())
                .userId(o.getUserId())
                .runnerId(o.getRunnerId())
                .type(o.getType())
                .typeIcon(TYPE_ICONS.getOrDefault(o.getType(), "📋"))
                .title(title)
                .from(o.getPickupLocation())
                .to(o.getDeliveryLocation())
                .pickupCode(o.getPickupCode())
                .urgency(o.getUrgency())
                .urgentKey(getUrgentKey(o.getUrgency()))
                .fee(o.getFee())
                .note(o.getNote())
                .contact(contact)
                .status(o.getStatus())
                .statusKey(getStatusKey(o.getStatus()))
                .userImages(userImgs)
                .pickupImages(pickupImgs)
                .deliveryImages(deliveryImgs)
                .earned(o.getRunnerEarned() != null ? o.getRunnerEarned().intValue() : (o.getFee() != null ? o.getFee().intValue() : 0))
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "")
                .timeline(getTimelines(o.getId()))
                .build();
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String getStatusKey(String status) {
        return switch (status) {
            case "待接单" -> "pending";
            case "已接单" -> "accepted";
            case "已取件" -> "picked";
            case "配送中" -> "delivering";
            case "待确认" -> "confirming";
            case "已完成" -> "done";
            case "已取消" -> "cancelled";
            default -> "done";
        };
    }

    private String getUrgentKey(String urgent) {
        if ("加急".equals(urgent) || "紧急".equals(urgent) || "极速".equals(urgent)) {
            return "urgent";
        }
        return "normal";
    }

    private synchronized String generateOrderNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!today.equals(lastDate)) {
            lastDate = today;
            // 从数据库查询今天已有最大序号，避免重启后重复
            String prefix = "XD" + today;
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.likeRight(Order::getOrderNo, prefix)
                   .orderByDesc(Order::getOrderNo)
                   .last("LIMIT 1");
            Order last = orderMapper.selectOne(wrapper);
            if (last != null && last.getOrderNo() != null
                    && last.getOrderNo().length() == prefix.length() + 5) {
                try {
                    int maxSeq = Integer.parseInt(last.getOrderNo().substring(prefix.length()));
                    dailySeq.set(maxSeq);
                } catch (NumberFormatException e) {
                    dailySeq.set(0);
                }
            } else {
                dailySeq.set(0);
            }
        }
        int seq = dailySeq.incrementAndGet();
        return String.format("XD%s%05d", today, seq);
    }
}
