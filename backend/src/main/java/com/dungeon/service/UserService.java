package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.entity.User;
import com.dungeon.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

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
     */
    public User createUser(String accountName, String email, String encodedPassword) {
        User user = new User();
        user.setAccountName(accountName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setPlayerLevel(1);
        user.setPlayerExp(0L);
        user.setStatus("active");
        userMapper.insert(user);
        return user;
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

