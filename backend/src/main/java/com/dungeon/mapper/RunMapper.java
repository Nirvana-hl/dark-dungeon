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
    List<Run> selectByUserId(@Param("userId") String userId);
}

