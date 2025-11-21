package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.SkillDTO;
import com.dungeon.dto.UnlockSkillRequest;
import com.dungeon.dto.UserSkillDTO;
import com.dungeon.service.SkillService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 技能系统控制器
 * 提供技能树查询、解锁等功能
 */
@RestController
@RequestMapping()
public class SkillController {

    @Autowired
    private SkillService skillService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 根据职业短码获取技能树
     * GET /skills/{playerCharacterCode}
     * 
     * @param Code 职业短码（如：warrior, occultist, ranger）
     * @param request HTTP请求（用于获取当前用户）
     * @return 技能树列表，包含每个技能的解锁状态
     */
    @GetMapping("/skills/{Code}")
    public Result<List<SkillDTO>> getSkillTree(@PathVariable String Code,
                                                HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<SkillDTO> skillTree = skillService.getSkillTreeByPlayerCharacterCode(Code, userId);
            return Result.success(skillTree);
        } catch (Exception e) {
            return Result.error("获取技能树失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户已解锁的技能列表
     * GET /user-skills
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 已解锁技能列表
     */
    @GetMapping("/user-skills")
    public Result<List<UserSkillDTO>> getUserUnlockedSkills(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<UserSkillDTO> unlockedSkills = skillService.getUserUnlockedSkills(userId);
            return Result.success(unlockedSkills);
        } catch (Exception e) {
            return Result.error("获取已解锁技能失败: " + e.getMessage());
        }
    }

    /**
     * 解锁技能
     * POST /user-skills/unlock
     * 
     * @param unlockRequest 解锁请求（包含技能ID）
     * @param request HTTP请求（用于获取当前用户）
     * @return 解锁后的技能信息
     * 
     * 注意：技能解锁总是针对当前用户的当前职业，系统会自动从token中获取用户信息
     */
    @PostMapping("/user-skills/unlock")
    public Result<SkillDTO> unlockSkill(@RequestBody UnlockSkillRequest unlockRequest,
                                        HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 验证请求参数
            if (unlockRequest == null || unlockRequest.getSkillId() == null) {
                return Result.error(400, "技能ID不能为空");
            }

            SkillDTO unlockedSkill = skillService.unlockSkill(userId, unlockRequest);
            return Result.success("解锁技能成功", unlockedSkill);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("解锁技能失败: " + e.getMessage());
        }
    }

    /**
     * 从请求头获取用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权，请先登录");
        }
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromTokenAsLong(token);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

