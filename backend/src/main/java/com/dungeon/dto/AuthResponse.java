package com.dungeon.dto;

import lombok.Data;

/**
 * 认证响应 DTO
 */
@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String accountName;
    private String email;
}

