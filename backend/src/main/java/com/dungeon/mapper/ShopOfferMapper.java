package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.ShopOffer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商城上架配置 Mapper
 */
@Mapper
public interface ShopOfferMapper extends BaseMapper<ShopOffer> {
    
    /**
     * 根据商品类型查询
     * @param offerType 商品类型：card-卡牌, item-道具, bundle-礼包
     * @return 商品列表
     */
    List<ShopOffer> selectByOfferType(@Param("offerType") String offerType);
    
    /**
     * 按显示顺序查询所有商品
     * @return 商品列表（按displayOrder排序）
     */
    List<ShopOffer> selectAllOrderByDisplayOrder();
}

