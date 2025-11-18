package com.dungeon.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {
    /**
     * 可以使用邮箱或账号名称登录（至少提供一个）
     */
    private String email;
    private String accountName;
    
    @javax.validation.constraints.NotBlank(message = "密码不能为空")
    private String password;
}

