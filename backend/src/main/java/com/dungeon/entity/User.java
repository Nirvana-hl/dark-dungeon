package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 * 对应数据库表：users
 */
@Data
@TableName("users")
public class User {
    /**
     * 用户ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 账户名称/显示名称
     */
    private String accountName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码（加密后）
     */
    private String password;
    
    /**
     * 玩家等级
     */
    private Integer playerLevel;
    
    /**
     * 玩家经验值
     */
    private Long playerExp;
    
    /**
     * 账户状态：active-活跃, banned-封禁, dormant-休眠
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

