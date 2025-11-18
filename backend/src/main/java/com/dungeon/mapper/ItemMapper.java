package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 道具模板 Mapper
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    
    /**
     * 根据道具类型查询
     * @param itemType 道具类型
     * @return 道具列表
     */
    List<Item> selectByItemType(@Param("itemType") String itemType);
}

