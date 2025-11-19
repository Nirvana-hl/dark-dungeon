package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户钱包 DTO
 */
@Data
public class UserWalletDTO {
    private Long id;
    private Long userId;
    private String currencyType;
    private Long balance;
    private LocalDateTime updatedAt;
}

