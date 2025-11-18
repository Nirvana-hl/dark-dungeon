package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户手牌 DTO
 */
@Data
public class UserCardDTO {
    private Long id;
    private Long userId;
    private Long cardId;
    private Integer quantity;
    private Integer level;
    private Long loadoutId;
    private Long equippedToUserCardCharacterId;
    private LocalDateTime acquiredAt;
    private String acquiredSource;
    private LocalDateTime lastUsedAt;
}


