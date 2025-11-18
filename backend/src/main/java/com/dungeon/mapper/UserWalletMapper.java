package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户钱包 Mapper
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {
    
    /**
     * 根据用户ID查询所有钱包
     * @param userId 用户ID
     * @return 钱包列表
     */
    List<UserWallet> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据用户ID和货币类型查询钱包
     * @param userId 用户ID
     * @param currencyType 货币类型
     * @return 钱包对象
     */
    UserWallet selectByUserIdAndCurrencyType(@Param("userId") String userId, @Param("currencyType") String currencyType);
}

