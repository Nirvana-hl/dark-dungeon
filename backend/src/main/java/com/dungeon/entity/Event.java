package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 随机事件实体
 * 对应数据库表：events
 */
@Data
@TableName("events")
public class Event {
    /**
     * 事件ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 事件名称
     */
    private String name;
    
    /**
     * 事件类型：camp-营地, dungeon-地牢
     */
    private String locationType;
    
    /**
     * 触发条件（JSON格式：关卡编号范围、章节、房间类型等）
     */
    private String triggerCondition;
    
    /**
     * 触发概率（0.00-1.00，地牢事件使用）
     */
    private java.math.BigDecimal triggerChance;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 效果参数（JSON格式：奖励、惩罚、选择项等）
     */
    private String effectPayload;
    
    /**
     * 选择项配置（JSON格式：如果有多个选择）
     */
    private String choices;
}

