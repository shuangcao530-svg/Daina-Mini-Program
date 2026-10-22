package com.xiaosuda.service.impl;

import com.xiaosuda.common.BusinessException;
import com.xiaosuda.common.ErrorCode;
import com.xiaosuda.dto.StatsVO;
import com.xiaosuda.dto.UpdateProfileRequest;
import com.xiaosuda.dto.UserVO;
import com.xiaosuda.entity.User;
import com.xiaosuda.entity.WalletTransaction;
import com.xiaosuda.entity.WithdrawRecord;
import com.xiaosuda.mapper.UserMapper;
import com.xiaosuda.mapper.WalletTransactionMapper;
import com.xiaosuda.mapper.WithdrawRecordMapper;
import com.xiaosuda.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

    @Override
    public UserVO getProfile(Long userId) {
        User user = getUser(userId);
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getSchool() != null) user.setSchool(request.getSchool());
        userMapper.updateById(user);
        return toUserVO(user);
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        User user = getUser(userId);
        // 可提现余额 = balance - frozen_balance
        BigDecimal frozen = user.getFrozenBalance() != null ? user.getFrozenBalance() : BigDecimal.ZERO;
        return user.getBalance().subtract(frozen);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {
        User user = getUser(userId);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.WITHDRAW_AMOUNT_INVALID);
        }

        // 可提现余额
        BigDecimal frozen = user.getFrozenBalance() != null ? user.getFrozenBalance() : BigDecimal.ZERO;
        BigDecimal available = user.getBalance().subtract(frozen);
        if (available.compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT);
        }

        BigDecimal balanceBefore = user.getBalance();
        // 冻结提现金额
        user.setFrozenBalance(frozen.add(amount));
        userMapper.updateById(user);

        // 记录钱包流水 —— 冻结
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType("freeze");
        tx.setAmount(amount.negate());
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(user.getBalance());
        tx.setDescription("提现冻结: ¥" + amount);
        walletTransactionMapper.insert(tx);

        // 创建提现记录
        WithdrawRecord record = new WithdrawRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setStatus("pending");
        record.setAccountType("wechat");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        withdrawRecordMapper.insert(record);
    }

    @Override
    public StatsVO getStats(Long userId) {
        User user = getUser(userId);
        return StatsVO.builder()
                .available(0)
                .today(user.getTotalOrders())
                .earned(user.getTotalEarned())
                .build();
    }

    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserVO toUserVO(User user) {
        BigDecimal frozen = user.getFrozenBalance() != null ? user.getFrozenBalance() : BigDecimal.ZERO;
        return UserVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .school(user.getSchool())
                .role(user.getRole())
                .balance(user.getBalance().subtract(frozen))  // 返回可用余额
                .totalEarned(user.getTotalEarned())
                .totalOrders(user.getTotalOrders())
                .build();
    }
}
