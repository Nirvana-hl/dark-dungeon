package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户背包实体
 * 对应数据库表：inventory
 */
@Data
@TableName("inventory")
public class Inventory {
    /**
     * 背包记录ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 道具模板ID
     */
    private String itemId;
    
    /**
     * 持有数量
     */
    private Integer quantity;
    
    /**
     * 是否绑定：unbound-未绑定, bound-已绑定
     */
    private String bindStatus;
    
    /**
     * 更新时间
     */
    private LocalDateTime lastUpdatedAt;
}

