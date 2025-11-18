package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.UserStageProgressDTO;
import com.dungeon.entity.UserStageProgress;
import com.dungeon.service.UserStageProgressService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关卡进度控制器
 */
@RestController
@RequestMapping("/user-stage-progress")
public class UserStageProgressController {
    
    @Autowired
    private UserStageProgressService userStageProgressService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取当前用户的所有关卡进度
     * GET /user-stage-progress
     */
    @GetMapping
    public Result<List<UserStageProgressDTO>> getUserStageProgress(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<UserStageProgress> progressList = userStageProgressService.getUserStageProgress(userId);
            List<UserStageProgressDTO> dtos = progressList.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success(dtos);
        } catch (Exception e) {
            return Result.error("获取关卡进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的关卡进度（单个）
     * GET /user-stage-progress/{stageNumber}
     */
    @GetMapping("/{stageNumber}")
    public Result<UserStageProgressDTO> getUserStageProgress(@PathVariable Integer stageNumber,
                                                             HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            UserStageProgress progress = userStageProgressService.getUserStageProgress(userId, stageNumber);
            if (progress == null) {
                return Result.error(404, "关卡进度不存在");
            }
            return Result.success(toDTO(progress));
        } catch (Exception e) {
            return Result.error("获取关卡进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户指定章节的关卡进度
     * GET /user-stage-progress/chapter/{chapterNumber}
     */
    @GetMapping("/chapter/{chapterNumber}")
    public Result<List<UserStageProgressDTO>> getUserStageProgressByChapter(@PathVariable Integer chapterNumber,
                                                                           HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<UserStageProgress> progressList = userStageProgressService.getUserStageProgressByChapter(userId, chapterNumber);
            List<UserStageProgressDTO> dtos = progressList.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success(dtos);
        } catch (Exception e) {
            return Result.error("获取章节进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过关卡
     * POST /user-stage-progress/{stageNumber}/pass
     */
    @PostMapping("/{stageNumber}/pass")
    public Result<Void> passStage(@PathVariable Integer stageNumber,
                                 @RequestParam(required = false) Integer chapterNumber,
                                 HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 如果没有提供章节编号，自动计算
            if (chapterNumber == null) {
                chapterNumber = (int) Math.ceil(stageNumber / 5.0);
            }
            
            userStageProgressService.passStage(userId, stageNumber, chapterNumber);
            return Result.success("关卡通过成功", null);
        } catch (Exception e) {
            return Result.error("通过关卡失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录关卡尝试（失败时调用）
     * POST /user-stage-progress/{stageNumber}/attempt
     */
    @PostMapping("/{stageNumber}/attempt")
    public Result<Void> recordStageAttempt(@PathVariable Integer stageNumber,
                                           @RequestParam(required = false) Integer chapterNumber,
                                           @RequestParam String result,
                                           HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 如果没有提供章节编号，自动计算
            if (chapterNumber == null) {
                chapterNumber = (int) Math.ceil(stageNumber / 5.0);
            }
            
            userStageProgressService.recordStageAttempt(userId, stageNumber, chapterNumber, result);
            return Result.success("记录尝试成功", null);
        } catch (Exception e) {
            return Result.error("记录尝试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     * GET /user-stage-progress/stats
     */
    @GetMapping("/stats")
    public Result<UserStageStatsDTO> getUserStageStats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer passedCount = userStageProgressService.getPassedStageCount(userId);
            Integer unlockedCount = userStageProgressService.getUnlockedStageCount(userId);
            Integer maxStage = userStageProgressService.getMaxStageNumber(userId);
            
            UserStageStatsDTO stats = new UserStageStatsDTO();
            stats.setPassedCount(passedCount);
            stats.setUnlockedCount(unlockedCount);
            stats.setMaxStageNumber(maxStage);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求头获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权，请先登录");
        }
        token = token.substring(7);
        String userIdStr = jwtUtil.getUserIdFromToken(token);
        if (userIdStr == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("用户ID格式错误");
        }
    }
    
    /**
     * 转换为DTO
     */
    private UserStageProgressDTO toDTO(UserStageProgress progress) {
        UserStageProgressDTO dto = new UserStageProgressDTO();
        BeanUtils.copyProperties(progress, dto);
        // TODO: 如果需要，可以关联查询关卡信息
        return dto;
    }
    
    /**
     * 用户关卡统计DTO
     */
    public static class UserStageStatsDTO {
        private Integer passedCount;
        private Integer unlockedCount;
        private Integer maxStageNumber;
        
        // Getters and Setters
        public Integer getPassedCount() { return passedCount; }
        public void setPassedCount(Integer passedCount) { this.passedCount = passedCount; }
        public Integer getUnlockedCount() { return unlockedCount; }
        public void setUnlockedCount(Integer unlockedCount) { this.unlockedCount = unlockedCount; }
        public Integer getMaxStageNumber() { return maxStageNumber; }
        public void setMaxStageNumber(Integer maxStageNumber) { this.maxStageNumber = maxStageNumber; }
    }
}

