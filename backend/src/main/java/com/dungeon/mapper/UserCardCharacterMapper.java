package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserCardCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户卡牌角色实例 Mapper
 */
@Mapper
public interface UserCardCharacterMapper extends BaseMapper<UserCardCharacter> {
    
    /**
     * 根据用户ID查询所有卡牌角色实例
     * @param userId 用户ID
     * @return 卡牌角色实例列表
     */
    List<UserCardCharacter> selectByUserId(@Param("userId") String userId);
    
    /**
     * 查询已上阵的卡牌角色
     * @param userId 用户ID
     * @return 已上阵的卡牌角色列表
     */
    List<UserCardCharacter> selectDeployedByUserId(@Param("userId") String userId);
}

