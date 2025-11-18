package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.PlayerAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 玩家操作记录 Mapper
 */
@Mapper
public interface PlayerActionMapper extends BaseMapper<PlayerAction> {
    
    /**
     * 根据用户ID查询操作记录
     * @param userId 用户ID
     * @return 操作记录列表
     */
    List<PlayerAction> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据场景来源查询操作记录
     * @param userId 用户ID
     * @param sourceScene 场景来源：camp-营地, dungeon-地牢, battle-战斗
     * @return 操作记录列表
     */
    List<PlayerAction> selectBySourceScene(@Param("userId") String userId, @Param("sourceScene") String sourceScene);
}

