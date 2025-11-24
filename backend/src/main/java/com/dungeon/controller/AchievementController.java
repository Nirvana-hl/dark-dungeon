package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.AchievementDTO;
import com.dungeon.entity.Achievement;
import com.dungeon.entity.UserAchievement;
import com.dungeon.service.AchievementService;
import com.dungeon.service.UserAchievementService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成就控制器
 * 提供成就相关的API接口
 */
@RestController
@RequestMapping("/achievements")
public class AchievementController {
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserAchievementService userAchievementService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取所有成就列表
     * GET /achievements
     */
    @GetMapping
    public Result<List<AchievementDTO>> getAllAchievements() {
        try {
            List<Achievement> achievements = achievementService.getAllAchievements();
            List<AchievementDTO> achievementDTOs = achievements.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success("获取成就列表成功", achievementDTOs);
        } catch (Exception e) {
            return Result.error("获取成就列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类获取成就列表
     * GET /achievements/category/{category}
     * 
     * @param category 分类：progression-进度, mastery-精通, collection-收集, social-社交
     */
    @GetMapping("/category/{category}")
    public Result<List<AchievementDTO>> getAchievementsByCategory(@PathVariable String category) {
        try {
            List<Achievement> achievements = achievementService.getAchievementsByCategory(category);
            List<AchievementDTO> achievementDTOs = achievements.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success("获取成就列表成功", achievementDTOs);
        } catch (Exception e) {
            return Result.error("获取成就列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取成就详情
     * GET /achievements/{id}
     * 
     * @param id 成就ID
     */
    @GetMapping("/{id}")
    public Result<AchievementDTO> getAchievementById(@PathVariable Long id) {
        try {
            Achievement achievement = achievementService.getAchievementById(id);
            if (achievement == null) {
                return Result.error(404, "成就不存在");
            }
            return Result.success("获取成就详情成功", toDTO(achievement));
        } catch (Exception e) {
            return Result.error("获取成就详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据名称搜索成就
     * GET /achievements/search?name={name}
     * 
     * @param name 成就名称（支持模糊查询）
     */
    @GetMapping("/search")
    public Result<List<AchievementDTO>> searchAchievements(@RequestParam String name) {
        try {
            List<Achievement> achievements = achievementService.searchAchievementsByName(name);
            List<AchievementDTO> achievementDTOs = achievements.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success("搜索成就成功", achievementDTOs);
        } catch (Exception e) {
            return Result.error("搜索成就失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户成就列表（带完成状态）
     * GET /achievements/user
     * 
     * @param request HttpServletRequest
     */
    @GetMapping("/user")
    public Result<List<AchievementDTO>> getUserAchievements(HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            // 获取所有成就模板
            List<Achievement> achievements = achievementService.getAllAchievements();
            // 获取用户成就实例
            List<UserAchievement> userAchievements = userAchievementService.getUserAchievements(userId);
            
            // 创建用户成就映射（achievementId -> UserAchievement）
            Map<Long, UserAchievement> userAchievementMap = userAchievements.stream()
                    .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));
            
            // 合并数据，生成DTO列表
            List<AchievementDTO> achievementDTOs = achievements.stream()
                    .map(achievement -> toDTO(achievement, userAchievementMap.get(achievement.getId())))
                    .collect(Collectors.toList());
            
            return Result.success("获取用户成就列表成功", achievementDTOs);
        } catch (Exception e) {
            return Result.error("获取用户成就列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类获取用户成就列表
     * GET /achievements/user/category/{category}
     * 
     * @param request HttpServletRequest
     * @param category 分类
     */
    @GetMapping("/user/category/{category}")
    public Result<List<AchievementDTO>> getUserAchievementsByCategory(
            HttpServletRequest request,
            @PathVariable String category) {
        Long userId = getUserId(request);
        try {
            // 获取指定分类的成就模板
            List<Achievement> achievements = achievementService.getAchievementsByCategory(category);
            // 获取用户成就实例
            List<UserAchievement> userAchievements = userAchievementService.getUserAchievementsByCategory(userId, category);
            
            // 创建用户成就映射
            Map<Long, UserAchievement> userAchievementMap = userAchievements.stream()
                    .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));
            
            // 合并数据
            List<AchievementDTO> achievementDTOs = achievements.stream()
                    .map(achievement -> toDTO(achievement, userAchievementMap.get(achievement.getId())))
                    .collect(Collectors.toList());
            
            return Result.success("获取用户成就列表成功", achievementDTOs);
        } catch (Exception e) {
            return Result.error("获取用户成就列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户成就统计
     * GET /achievements/user/stats
     * 
     * @param request HttpServletRequest
     */
    @GetMapping("/user/stats")
    public Result<UserAchievementService.AchievementStats> getUserAchievementStats(HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            UserAchievementService.AchievementStats stats = userAchievementService.getAchievementStats(userId);
            return Result.success("获取成就统计成功", stats);
        } catch (Exception e) {
            return Result.error("获取成就统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 转换为DTO（无用户数据）
     */
    private AchievementDTO toDTO(Achievement achievement) {
        return toDTO(achievement, null);
    }
    
    /**
     * 转换为DTO（带用户数据）
     * @param achievement 成就模板
     * @param userAchievement 用户成就实例（可为null）
     */
    private AchievementDTO toDTO(Achievement achievement, UserAchievement userAchievement) {
        AchievementDTO dto = new AchievementDTO();
        BeanUtils.copyProperties(achievement, dto);
        
        // 如果有用户成就数据，设置完成状态和进度
        if (userAchievement != null) {
            dto.setIsCompleted(userAchievement.getIsCompleted());
            dto.setProgress(userAchievement.getProgress());
        } else {
            // 没有用户成就数据，表示未开始
            dto.setIsCompleted(false);
            dto.setProgress(0);
        }
        
        return dto;
    }
    
    /**
     * 从请求头解析用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权");
        }
        return jwtUtil.getUserIdFromTokenAsLong(token.substring(7));
    }
}

