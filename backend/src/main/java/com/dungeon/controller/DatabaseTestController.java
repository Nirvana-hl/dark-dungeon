package com.dungeon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接测试控制器
 * 用于测试数据库连接是否成功
 */
@RestController
@RequestMapping("/test")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    /**
     * 测试数据库连接
     * 访问地址: http://localhost:8080/api/test/db
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            
            result.put("success", true);
            result.put("message", "数据库连接成功！");
            result.put("databaseProductName", metaData.getDatabaseProductName());
            result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            result.put("url", metaData.getURL());
            result.put("username", metaData.getUserName());
            result.put("driverName", metaData.getDriverName());
            result.put("driverVersion", metaData.getDriverVersion());
            result.put("isClosed", connection.isClosed());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据库连接失败！");
            result.put("error", e.getMessage());
            result.put("errorClass", e.getClass().getName());
            
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 测试简单查询
     * 访问地址: http://localhost:8080/api/test/query
     */
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> testQuery() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 执行简单查询
            java.sql.Statement statement = connection.createStatement();
            java.sql.ResultSet rs = statement.executeQuery("SELECT 1 as test_value, NOW() as current_time");
            
            if (rs.next()) {
                result.put("success", true);
                result.put("message", "数据库查询成功！");
                result.put("testValue", rs.getInt("test_value"));
                result.put("currentTime", rs.getTimestamp("current_time").toString());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据库查询失败！");
            result.put("error", e.getMessage());
            result.put("errorClass", e.getClass().getName());
            
            return ResponseEntity.status(500).body(result);
        }
    }
}

