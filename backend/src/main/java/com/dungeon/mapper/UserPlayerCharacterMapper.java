package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserPlayerCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 玩家角色实例 Mapper
 */
@Mapper
public interface UserPlayerCharacterMapper extends BaseMapper<UserPlayerCharacter> {
    
    /**
     * 根据用户ID查询所有角色实例
     * @param userId 用户ID
     * @return 角色实例列表
     */
    List<UserPlayerCharacter> selectByUserId(@Param("userId") String userId);
}

