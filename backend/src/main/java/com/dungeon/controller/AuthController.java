package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.AuthResponse;
import com.dungeon.dto.LoginRequest;
import com.dungeon.dto.RegisterRequest;
import com.dungeon.entity.User;
import com.dungeon.service.UserService;
import com.dungeon.util.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器：注册 / 登录
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            if (!StringUtils.hasText(request.getAccountName())) {
                return Result.error(400, "账号名称不能为空");
            }
            if (!StringUtils.hasText(request.getEmail())) {
                return Result.error(400, "邮箱不能为空");
            }
            if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
                return Result.error(400, "密码至少6位");
            }

            if (userService.existsByAccountName(request.getAccountName())) {
                return Result.error(400, "账号名称已被占用");
            }
            if (userService.existsByEmail(request.getEmail())) {
                return Result.error(400, "邮箱已被注册");
            }

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User user = userService.createUser(request.getAccountName(), request.getEmail(), encodedPassword);

            AuthResponse response = buildAuthResponse(user);
            response.setToken(jwtUtil.generateToken(user.getId(), user.getAccountName()));

            return Result.success("注册成功", response);
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            if (!StringUtils.hasText(request.getEmail()) && !StringUtils.hasText(request.getAccountName())) {
                return Result.error(400, "请提供邮箱或账号名称");
            }
            if (!StringUtils.hasText(request.getPassword())) {
                return Result.error(400, "密码不能为空");
            }

            User user = null;
            if (StringUtils.hasText(request.getEmail())) {
                user = userService.getUserByEmail(request.getEmail());
            }
            if (user == null && StringUtils.hasText(request.getAccountName())) {
                user = userService.getUserByAccountName(request.getAccountName());
            }
            if (user == null) {
                return Result.error(401, "用户不存在或密码错误");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Result.error(401, "用户不存在或密码错误");
            }
            if (!"active".equalsIgnoreCase(user.getStatus())) {
                return Result.error(403, "账户状态异常，请联系管理员");
            }

            AuthResponse response = buildAuthResponse(user);
            response.setToken(jwtUtil.generateToken(user.getId(), user.getAccountName()));

            return Result.success("登录成功", response);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * 注意：JWT是无状态的，登出主要是前端清除token
     * 这里提供一个接口用于记录登出日志（可选）
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            // JWT是无状态的，登出主要是前端清除token
            // 这里可以记录登出日志（如果需要）
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                // 可以在这里记录登出日志或加入黑名单（如果需要）
                System.out.println("[AuthController] 用户登出");
            }
            return Result.success("登出成功", null);
        } catch (Exception e) {
            // 即使出错也返回成功，因为登出主要是前端清除token
            return Result.success("登出成功", null);
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setAccountName(user.getAccountName());
        response.setEmail(user.getEmail());
        return response;
    }
}


