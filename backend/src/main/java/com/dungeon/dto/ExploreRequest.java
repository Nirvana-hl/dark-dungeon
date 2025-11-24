package com.dungeon.dto;

import lombok.Data;

/**
 * 地牢探索操作请求
 */
@Data
public class ExploreRequest {
    private String action = "explore";
    private String choice;
}

