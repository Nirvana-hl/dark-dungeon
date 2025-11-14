package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户卡牌 Mapper
 */
@Mapper
public interface UserCardMapper extends BaseMapper<UserCard> {
}

