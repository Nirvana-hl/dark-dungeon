package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户钱包 Mapper
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {
}

