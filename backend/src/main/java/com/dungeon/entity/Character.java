package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("characters")
@Alias("GameCharacter")  // 避免与 java.lang.Character 冲突
public class Character {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String classType; // 职业：战士/法师/游侠
    private Integer level;
    private Integer stars; // 星级 1-5
    private Integer hp;
    private Integer mp;
    private Integer stress; // 压力值
    private String attrs; // JSON 格式的额外属性
    private String status; // idle/exploring/battling
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}

