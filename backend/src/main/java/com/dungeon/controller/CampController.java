package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.CampDashboardDTO;
import com.dungeon.service.CampService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 营地数据聚合接口
 */
@RestController
@RequestMapping("/camp")
public class CampController {

    @Autowired
    private CampService campService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取营地仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<CampDashboardDTO> getDashboard(HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success(campService.getDashboard(userId));
        } catch (Exception e) {
            return Result.error("获取营地数据失败: " + e.getMessage());
        }
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权");
        }
        return jwtUtil.getUserIdFromTokenAsLong(token.substring(7));
    }
}

