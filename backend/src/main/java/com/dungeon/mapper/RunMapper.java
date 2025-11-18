package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Run;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地牢探索记录 Mapper
 */
@Mapper
public interface RunMapper extends BaseMapper<Run> {
    
    /**
     * 根据用户ID查询探索记录
     * @param userId 用户ID
     * @return 探索记录列表
     */
    List<Run> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和关卡编号查询探索记录
     * @param userId 用户ID
     * @param stageNumber 关卡编号
     * @return 探索记录列表
     */
    List<Run> selectByUserIdAndStageNumber(@Param("userId") Long userId, 
                                           @Param("stageNumber") Integer stageNumber);
    
    /**
     * 查询用户当前正在进行的探索记录
     * @param userId 用户ID
     * @return 探索记录（未完成的）
     */
    Run selectCurrentRun(@Param("userId") Long userId);
}

