package com.dungeon.service;

import com.dungeon.entity.UserWallet;
import com.dungeon.mapper.UserWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 钱包服务
 */
@Service
public class WalletService {

    @Autowired
    private UserWalletMapper walletMapper;

    /**
     * 获取用户金币
     */
    public Integer getGold(Long userId) {
        UserWallet wallet = walletMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserWallet>()
                .eq("user_id", userId)
        );
        if (wallet == null) {
            // 初始化钱包
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setGold(500);
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(wallet);
            return 500;
        }
        return wallet.getGold();
    }

    /**
     * 消费金币
     */
    @Transactional
    public boolean spend(Long userId, Integer amount) {
        if (amount <= 0) {
            return true;
        }
        UserWallet wallet = walletMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserWallet>()
                .eq("user_id", userId)
        );
        if (wallet == null) {
            throw new RuntimeException("钱包不存在");
        }
        if (wallet.getGold() < amount) {
            throw new RuntimeException("金币不足");
        }
        wallet.setGold(wallet.getGold() - amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);
        return true;
    }

    /**
     * 增加金币
     */
    @Transactional
    public Integer add(Long userId, Integer amount) {
        if (amount <= 0) {
            return getGold(userId);
        }
        UserWallet wallet = walletMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserWallet>()
                .eq("user_id", userId)
        );
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setGold(amount);
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(wallet);
            return amount;
        }
        wallet.setGold(wallet.getGold() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);
        return wallet.getGold();
    }
}

