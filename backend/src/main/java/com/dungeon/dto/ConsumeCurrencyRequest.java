package com.dungeon.dto;

import lombok.Data;

/**
 * 消费货币请求DTO
 */
@Data
public class ConsumeCurrencyRequest {
    /**
     * 货币类型：gold-金币, soulstone-魂晶等
     */
    private String currencyType;
    
    /**
     * 消费数量（字符串格式，前端传递）
     */
    private String amount;
    
    /**
     * 原因（可选）
     */
    private String reason;
}

