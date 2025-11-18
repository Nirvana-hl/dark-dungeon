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
     * 事件ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 事件名称
     */
    private String name;
    
    /**
     * 事件类型：camp-营地, dungeon-地牢
     */
    private String locationType;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 效果参数（JSON格式）
     */
    private String effectPayload;
}

