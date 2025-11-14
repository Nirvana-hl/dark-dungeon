package com.dungeon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库连接测试类
 */
@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试数据库连接是否成功
     */
    @Test
    public void testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为null");
            
            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            String url = metaData.getURL();
            String username = metaData.getUserName();
            
            System.out.println("==========================================");
            System.out.println("数据库连接测试成功！");
            System.out.println("==========================================");
            System.out.println("数据库类型: " + databaseProductName);
            System.out.println("数据库版本: " + databaseProductVersion);
            System.out.println("连接URL: " + url);
            System.out.println("用户名: " + username);
            System.out.println("连接状态: " + (connection.isClosed() ? "已关闭" : "已连接"));
            System.out.println("==========================================");
            
            assertFalse(connection.isClosed(), "数据库连接应该是打开的");
            assertNotNull(databaseProductName, "数据库产品名称不应为null");
            
        } catch (Exception e) {
            System.err.println("==========================================");
            System.err.println("数据库连接测试失败！");
            System.err.println("==========================================");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("==========================================");
            e.printStackTrace();
            fail("数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据库查询功能
     */
    @Test
    public void testDatabaseQuery() {
        if (jdbcTemplate == null) {
            System.out.println("JdbcTemplate未配置，跳过查询测试");
            return;
        }
        
        try {
            // 测试简单查询
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, result, "查询结果应该为1");
            
            System.out.println("==========================================");
            System.out.println("数据库查询测试成功！");
            System.out.println("查询结果: " + result);
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("==========================================");
            System.err.println("数据库查询测试失败！");
            System.err.println("==========================================");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("==========================================");
            e.printStackTrace();
            fail("数据库查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据源配置
     */
    @Test
    public void testDataSourceConfiguration() {
        assertNotNull(dataSource, "数据源不应为null");
        System.out.println("数据源配置正确: " + dataSource.getClass().getName());
    }
}

