package com.xiaosuda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaosuda.common.BusinessException;
import com.xiaosuda.common.ErrorCode;
import com.xiaosuda.config.WechatConfig;
import com.xiaosuda.dto.LoginRequest;
import com.xiaosuda.dto.LoginResponse;
import com.xiaosuda.dto.UserVO;
import com.xiaosuda.entity.User;
import com.xiaosuda.mapper.UserMapper;
import com.xiaosuda.service.AuthService;
import com.xiaosuda.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WechatConfig wechatConfig;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String openId;

        if (wechatConfig.isMockLoginEnabled()) {
            // Dev mode: 使用固定 openId，切换用户不会创建新号
            String role = request.getRole() != null ? request.getRole() : "user";
            openId = "mock_" + role + "_001";
            log.info("Mock login: role={}, openId={}", role, openId);
        } else {
            // Call WeChat jscode2session API
            if (request.getCode() == null || request.getCode().isEmpty()) {
                throw new BusinessException(ErrorCode.LOGIN_FAILED, "缺少微信登录code");
            }
            openId = getOpenIdFromWechat(request.getCode());
        }

        // Upsert user
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname("新用户");
            user.setSchool("校园跑腿用户");
            user.setRole(request.getRole() != null ? request.getRole() : "user");
            user.setBalance(BigDecimal.ZERO);
            user.setTotalEarned(BigDecimal.ZERO);
            user.setTotalOrders(0);
            userMapper.insert(user);
        }

        String token = jwtUtil.generateToken(user.getId(), openId, user.getRole());

        UserVO vo = toUserVO(user);
        return LoginResponse.builder()
                .token(token)
                .user(vo)
                .build();
    }

    @Override
    public LoginResponse check(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getOpenId(), user.getRole());
        return LoginResponse.builder()
                .token(token)
                .user(toUserVO(user))
                .build();
    }

    private String getOpenIdFromWechat(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatConfig.getAppid(), wechatConfig.getSecret(), code);

        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BusinessException(ErrorCode.LOGIN_FAILED, "微信接口调用失败");
            }
            String body = response.body().string();
            JsonNode json = objectMapper.readTree(body);

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                String errmsg = json.has("errmsg") ? json.get("errmsg").asText() : "未知错误";
                throw new BusinessException(ErrorCode.LOGIN_FAILED, "微信登录失败: " + errmsg);
            }

            return json.get("openid").asText();
        } catch (java.io.IOException e) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "微信接口网络异常");
        }
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .school(user.getSchool())
                .role(user.getRole())
                .balance(user.getBalance())
                .totalEarned(user.getTotalEarned())
                .totalOrders(user.getTotalOrders())
                .build();
    }
}
