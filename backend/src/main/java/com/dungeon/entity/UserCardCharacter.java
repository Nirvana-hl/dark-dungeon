package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户持有的卡牌角色实体
 * 对应数据库表：user_card_characters
 */
@Data
@TableName("user_card_characters")
public class UserCardCharacter {
    /**
     * 卡牌角色实例ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 卡牌角色模板ID
     */
    private Long cardCharacterId;
    
    /**
     * 当前血量（关卡结束重置）
     */
    private Integer currentHp;
    
    /**
     * 当前护甲（关卡结束重置）
     */
    private Integer currentArmor;
    
    /**
     * 是否已上阵（消耗行动点）
     */
    private Boolean isDeployed;
    
    /**
     * 上阵轮次（用于轮次重置）
     */
    private Integer deployedRound;
    
    /**
     * 当前星级
     */
    private Integer currentStarLevel;
}

