package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 * 
 * 注意：此接口继承BaseMapper，可以使用MyBatis Plus的自动CRUD功能
 * 同时，也可以在XML文件中定义自定义SQL方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户（使用XML映射）
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户（使用XML映射）
     * @param email 邮箱
     * @return 用户对象
     */
    User selectByEmail(@Param("email") String email);
    
    /**
     * 统计用户总数（使用XML映射）
     * @return 用户总数
     */
    Long countUsers();
    
    /**
     * 更新用户最后登录时间（使用XML映射）
     * @param id 用户ID
     * @return 更新行数
     */
    int updateLastLoginTime(@Param("id") Long id);
}

