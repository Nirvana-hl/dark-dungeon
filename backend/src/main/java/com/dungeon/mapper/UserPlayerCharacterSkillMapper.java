package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserPlayerCharacterSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 职业技能实例 Mapper
 */
@Mapper
public interface UserPlayerCharacterSkillMapper extends BaseMapper<UserPlayerCharacterSkill> {
    
    /**
     * 根据玩家角色实例ID查询已解锁技能
     * @param userPlayerCharacterId 玩家角色实例ID
     * @return 已解锁技能列表
     */
    List<UserPlayerCharacterSkill> selectByUserPlayerCharacterId(@Param("userPlayerCharacterId") String userPlayerCharacterId);
}

