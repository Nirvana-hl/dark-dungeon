package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户手牌 Mapper
 */
@Mapper
public interface UserCardMapper extends BaseMapper<UserCard> {
    
    /**
     * 根据用户ID查询所有手牌
     * @param userId 用户ID
     * @return 手牌列表
     */
    List<UserCard> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询已装备的卡牌
     * @param userId 用户ID
     * @return 已装备的卡牌列表
     */
    List<UserCard> selectEquippedByUserId(@Param("userId") Long userId);
}

