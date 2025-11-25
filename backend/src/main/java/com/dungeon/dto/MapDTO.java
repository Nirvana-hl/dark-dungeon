package com.dungeon.dto;

import lombok.Data;
import java.util.List;

/**
 * 地图DTO
 */
@Data
public class MapDTO {
    /**
     * 房间列表
     */
    private List<RoomDTO> rooms;
    
    /**
     * 路径列表
     */
    private List<PathDTO> paths;
    
    /**
     * 布局类型：linear/branching/grid（可选）
     */
    private String layout;
}

