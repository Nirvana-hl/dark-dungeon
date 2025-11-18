package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户钱包实体
 * 对应数据库表：user_wallets
 */
@Data
@TableName("user_wallets")
public class UserWallet {
    /**
     * 钱包ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 货币类型：gold-金币, soulstone-魂晶等
     */
    private String currencyType;
    
    /**
     * 余额
     */
    private Long balance;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

