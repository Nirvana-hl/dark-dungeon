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
     * 根据用户ID和职业模板ID查询已解锁技能
     * @param userId 用户ID
     * @param playerCharacterId 职业模板ID
     * @return 已解锁技能列表
     */
    List<UserPlayerCharacterSkill> selectByUserIdAndPlayerCharacterId(
            @Param("userId") Long userId, 
            @Param("playerCharacterId") Long playerCharacterId);
}

