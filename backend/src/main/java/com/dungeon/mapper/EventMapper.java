package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Event;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 随机事件 Mapper
 */
@Mapper
public interface EventMapper extends BaseMapper<Event> {
    
    /**
     * 根据事件类型查询
     * @param locationType 事件类型：camp-营地, dungeon-地牢
     * @return 事件列表
     */
    List<Event> selectByLocationType(@Param("locationType") String locationType);
    
    /**
     * 根据关卡编号查询可触发的地牢事件
     * @param stageNumber 关卡编号
     * @param chapterNumber 章节编号
     * @return 事件列表
     */
    List<Event> selectAvailableDungeonEvents(@Param("stageNumber") Integer stageNumber, 
                                             @Param("chapterNumber") Integer chapterNumber);
}

