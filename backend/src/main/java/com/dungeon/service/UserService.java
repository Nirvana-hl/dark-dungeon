package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.entity.User;
import com.dungeon.entity.UserWallet;
import com.dungeon.mapper.UserMapper;
import com.dungeon.mapper.UserWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserWalletMapper userWalletMapper;

    /**
     * 根据ID查询用户
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 根据邮箱查询用户
     */
    public User getUserByEmail(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据账户名查询用户
     */
    public User getUserByAccountName(String accountName) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account_name", accountName);
        return userMapper.selectOne(wrapper);
    }
    
    /**
     * 创建新用户（已加密密码）
     * 同时初始化用户钱包，给予1000初始金币
     */
    @Transactional
    public User createUser(String accountName, String email, String encodedPassword) {
        // 创建用户
        User user = new User();
        user.setAccountName(accountName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setPlayerLevel(1);
        user.setPlayerExp(0L);
        user.setStatus("active");
        userMapper.insert(user);
        
        // 初始化用户钱包，给予1000初始金币
        initializeUserWallet(user.getId(), "gold", 1000L);
        
        System.out.println("[UserService] 新用户注册成功: userId=" + user.getId() + 
                         ", accountName=" + accountName + 
                         ", 已初始化1000金币");
        
        return user;
    }
    
    /**
     * 初始化用户钱包
     * @param userId 用户ID
     * @param currencyType 货币类型（gold-金币等）
     * @param initialBalance 初始余额
     */
    private void initializeUserWallet(Long userId, String currencyType, Long initialBalance) {
        // 检查是否已存在钱包
        QueryWrapper<UserWallet> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("currency_type", currencyType);
        UserWallet existingWallet = userWalletMapper.selectOne(wrapper);
        
        if (existingWallet == null) {
            // 创建新钱包
            UserWallet wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setCurrencyType(currencyType);
            wallet.setBalance(initialBalance);
            wallet.setUpdatedAt(LocalDateTime.now());
            userWalletMapper.insert(wallet);
            System.out.println("[UserService] 初始化钱包: userId=" + userId + 
                             ", currencyType=" + currencyType + 
                             ", balance=" + initialBalance);
        } else {
            // 如果钱包已存在，更新余额（避免重复初始化）
            if (existingWallet.getBalance() == null || existingWallet.getBalance() == 0) {
                existingWallet.setBalance(initialBalance);
                existingWallet.setUpdatedAt(LocalDateTime.now());
                userWalletMapper.updateById(existingWallet);
                System.out.println("[UserService] 更新钱包余额: userId=" + userId + 
                                 ", currencyType=" + currencyType + 
                                 ", balance=" + initialBalance);
            }
        }
    }
    
    /**
     * 判断账号名称是否已存在
     */
    public boolean existsByAccountName(String accountName) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account_name", accountName);
        return userMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 判断邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 更新用户等级和经验
     */
    public void updateUserLevel(Long userId, Integer level, Long exp) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPlayerLevel(level);
            user.setPlayerExp(exp);
            userMapper.updateById(user);
        }
    }

    /**
     * 增加用户经验
     */
    public void addExp(Long userId, Long exp) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPlayerExp(user.getPlayerExp() + exp);
            // 计算等级提升（每1000经验升1级）
            int newLevel = (int) (user.getPlayerExp() / 1000) + 1;
            if (newLevel > user.getPlayerLevel()) {
                user.setPlayerLevel(newLevel);
            }
            userMapper.updateById(user);
        }
    }
}

