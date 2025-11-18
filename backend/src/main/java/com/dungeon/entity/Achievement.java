package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 成就实体
 * 对应数据库表：achievements
 */
@Data
@TableName("achievements")
public class Achievement {
    /**
     * 成就ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 成就名称
     */
    private String name;
    
    /**
     * 分类：progression-进度, mastery-精通, collection-收集, social-社交
     */
    private String category;
    
    /**
     * 成就描述
     */
    private String description;
    
    /**
     * 完成条件（JSON格式）
     */
    private String requirements;
}

