package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Skill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 职业技能模板 Mapper
 */
@Mapper
public interface SkillMapper extends BaseMapper<Skill> {
    
    /**
     * 根据职业短码查询技能树
     * @param playerCharacterCode 职业短码
     * @return 技能列表
     */
    List<Skill> selectByPlayerCharacterCode(@Param("playerCharacterCode") String playerCharacterCode);
}

