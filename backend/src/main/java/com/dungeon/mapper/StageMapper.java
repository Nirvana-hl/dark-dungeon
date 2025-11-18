package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Stage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关卡模板 Mapper
 */
@Mapper
public interface StageMapper extends BaseMapper<Stage> {
    
    /**
     * 根据关卡编号查询关卡
     * @param stageNumber 关卡编号
     * @return 关卡实体
     */
    Stage selectByStageNumber(@Param("stageNumber") Integer stageNumber);
    
    /**
     * 根据章节编号查询所有关卡
     * @param chapterNumber 章节编号
     * @return 关卡列表
     */
    List<Stage> selectByChapterNumber(@Param("chapterNumber") Integer chapterNumber);
    
    /**
     * 查询所有Boss关卡
     * @return Boss关卡列表
     */
    List<Stage> selectBossStages();
    
    /**
     * 根据难度查询关卡
     * @param difficulty 难度
     * @return 关卡列表
     */
    List<Stage> selectByDifficulty(@Param("difficulty") String difficulty);
}

