package com.xiaosuda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosuda.common.Result;
import com.xiaosuda.entity.ChatMessage;
import com.xiaosuda.entity.Order;
import com.xiaosuda.entity.User;
import com.xiaosuda.mapper.ChatMessageMapper;
import com.xiaosuda.mapper.OrderMapper;
import com.xiaosuda.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "聊天", description = "订单内下单人与跑腿员聊天")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 查找用户参与的所有订单（作为下单人或跑腿员，且订单不是待接单/已取消）
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .and(w -> w.eq(Order::getUserId, userId).or().eq(Order::getRunnerId, userId))
                        .notIn(Order::getStatus, "待接单", "已取消")
                        .orderByDesc(Order::getUpdatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (Order order : orders) {
            // 获取最后一条消息
            LambdaQueryWrapper<ChatMessage> lastMsgWrapper = new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getOrderId, order.getId())
                    .orderByDesc(ChatMessage::getCreatedAt)
                    .last("LIMIT 1");
            ChatMessage lastMsg = chatMessageMapper.selectOne(lastMsgWrapper);

            // 未读消息数
            Long unread = chatMessageMapper.selectCount(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getOrderId, order.getId())
                            .eq(ChatMessage::getReceiverId, userId)
                            .eq(ChatMessage::getIsRead, false));

            // 对方用户信息
            Long peerId = order.getUserId().equals(userId) ? order.getRunnerId() : order.getUserId();
            User peer = peerId != null ? userMapper.selectById(peerId) : null;

            Map<String, Object> conv = new LinkedHashMap<>();
            conv.put("orderId", order.getId());
            conv.put("orderNo", order.getOrderNo());
            conv.put("orderType", order.getType());
            conv.put("orderStatus", order.getStatus());
            conv.put("peerId", peerId);
            conv.put("peerName", peer != null ? peer.getNickname() : "未知用户");
            conv.put("peerAvatar", peer != null ? peer.getAvatarUrl() : null);
            conv.put("lastMsg", lastMsg != null ? lastMsg.getContent() : "暂无消息");
            conv.put("lastTime", lastMsg != null && lastMsg.getCreatedAt() != null
                    ? lastMsg.getCreatedAt().format(fmt) : "");
            conv.put("unread", unread);

            result.add(conv);
        }

        return Result.ok(result);
    }

    @Operation(summary = "聊天记录")
    @GetMapping("/{orderId}")
    public Result<Map<String, Object>> messages(@PathVariable Long orderId,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.ok(Collections.emptyMap());
        }

        // 权限检查
        if (!userId.equals(order.getUserId())
                && (order.getRunnerId() == null || !userId.equals(order.getRunnerId()))) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "无权查看");
            return Result.ok(err);
        }

        // 对方信息
        Long peerId = order.getUserId().equals(userId) ? order.getRunnerId() : order.getUserId();
        User peer = peerId != null ? userMapper.selectById(peerId) : null;

        // 聊天记录
        List<ChatMessage> msgs = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getOrderId, orderId)
                        .orderByAsc(ChatMessage::getCreatedAt));

        // 标记已读
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        List<Map<String, Object>> msgList = new ArrayList<>();
        for (ChatMessage m : msgs) {
            if (m.getReceiverId().equals(userId) && (m.getIsRead() == null || !m.getIsRead())) {
                m.setIsRead(true);
                chatMessageMapper.updateById(m);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("content", m.getContent());
            item.put("senderId", m.getSenderId());
            item.put("isMe", m.getSenderId().equals(userId));
            item.put("time", m.getCreatedAt() != null ? m.getCreatedAt().format(fmt) : "");
            msgList.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", order.getOrderNo());
        result.put("peerId", peerId);
        result.put("peerName", peer != null ? peer.getNickname() : "未知用户");
        result.put("peerAvatar", peer != null ? peer.getAvatarUrl() : null);
        result.put("messages", msgList);

        return Result.ok(result);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/{orderId}")
    public Result<Map<String, Object>> send(@PathVariable Long orderId,
                                             @RequestBody Map<String, String> body,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.ok(Collections.emptyMap());
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.ok(Collections.emptyMap());
        }

        // 确定接收方
        Long receiverId;
        if (userId.equals(order.getUserId())) {
            receiverId = order.getRunnerId();
        } else if (userId.equals(order.getRunnerId())) {
            receiverId = order.getUserId();
        } else {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "无权发送");
            return Result.ok(err);
        }

        ChatMessage msg = new ChatMessage();
        msg.setOrderId(orderId);
        msg.setSenderId(userId);
        msg.setReceiverId(receiverId);
        msg.setContent(content.trim());
        msg.setIsRead(false);
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(msg);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", msg.getId());
        result.put("content", msg.getContent());
        result.put("senderId", msg.getSenderId());
        result.put("isMe", true);
        result.put("time", "刚刚");
        return Result.ok(result);
    }
}
