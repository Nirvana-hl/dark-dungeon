package com.dungeon.service;

import com.dungeon.dto.AuthResponse;
import com.dungeon.dto.LoginRequest;
import com.dungeon.dto.RegisterRequest;
import com.dungeon.entity.User;
import com.dungeon.entity.UserWallet;
import com.dungeon.mapper.UserMapper;
import com.dungeon.mapper.UserWalletMapper;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserWalletMapper userWalletMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        User existUser = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("email", request.getEmail())
        );
        if (existUser != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查用户名是否已存在
        User existUsername = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("username", request.getUsername())
        );
        if (existUsername != null) {
            throw new RuntimeException("用户名已被使用");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        userMapper.insert(user);

        // 创建钱包（初始金币500）
        UserWallet wallet = new UserWallet();
        wallet.setUserId(user.getId());
        wallet.setGold(500);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletMapper.insert(wallet);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 返回响应
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("email", request.getEmail())
        );
        if (user == null) {
            throw new RuntimeException("邮箱或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("邮箱或密码错误");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 返回响应
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }
}

