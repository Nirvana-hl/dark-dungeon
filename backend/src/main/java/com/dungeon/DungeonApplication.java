package com.dungeon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 暗黑地牢肉鸽游戏后端主启动类
 */
@SpringBootApplication
@MapperScan("com.dungeon.mapper")
public class DungeonApplication {
    public static void main(String[] args) {
        SpringApplication.run(DungeonApplication.class, args);
    }
}

