package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.SkillDTO;
import com.dungeon.dto.UnlockSkillRequest;
import com.dungeon.dto.UseSkillRequest;
import com.dungeon.dto.UserSkillDTO;
import com.dungeon.service.SkillService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 获取战斗可用技能列表
     * GET /user-skills/battle/{playerCharacterCode}
     * 
     * @param playerCharacterCode 职业代码
     * @param request HTTP请求（用于获取当前用户）
     * @return 已解锁技能列表（用于战斗）
     */
    @GetMapping("/user-skills/battle/{playerCharacterCode}")
    public Result<List<SkillDTO>> getBattleSkills(@PathVariable String playerCharacterCode,
                                                   HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<SkillDTO> skills = skillService.getUnlockedSkillsForBattle(userId, playerCharacterCode);
            return Result.success(skills);
        } catch (Exception e) {
            return Result.error("获取战斗技能失败: " + e.getMessage());
        }
    }

    /**
     * 获取战斗可用主动技能列表
     * GET /user-skills/battle/{playerCharacterCode}/active
     * 
     * @param playerCharacterCode 职业代码
     * @param request HTTP请求（用于获取当前用户）
     * @return 已解锁的主动技能列表
     */
    @GetMapping("/user-skills/battle/{playerCharacterCode}/active")
    public Result<List<SkillDTO>> getActiveSkillsForBattle(@PathVariable String playerCharacterCode,
                                                             HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<SkillDTO> skills = skillService.getActiveSkillsForBattle(userId, playerCharacterCode);
            return Result.success(skills);
        } catch (Exception e) {
            return Result.error("获取主动技能失败: " + e.getMessage());
        }
    }

    /**
     * 获取技能详细信息
     * GET /user-skills/{skillId}/info
     * 
     * @param skillId 技能ID
     * @param request HTTP请求（用于获取当前用户）
     * @return 技能详细信息
     */
    @GetMapping("/user-skills/{skillId}/info")
    public Result<SkillDTO> getSkillInfo(@PathVariable Long skillId,
                                          HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            SkillDTO skill = skillService.getSkillById(skillId, userId);
            if (skill == null) {
                return Result.error(404, "技能不存在或未解锁");
            }
            return Result.success(skill);
        } catch (Exception e) {
            return Result.error("获取技能信息失败: " + e.getMessage());
        }
    }

    /**
     * 验证并获取技能使用信息
     * POST /user-skills/{skillId}/use
     * 
     * 验证技能是否可以使用（已解锁、法力值足够），返回技能效果信息
     * 前端根据返回的信息自行应用技能效果
     * 
     * @param skillId 技能ID
     * @param useRequest 使用请求（包含当前法力值）
     * @param request HTTP请求（用于获取当前用户）
     * @return 技能使用信息（包含消耗的法力值、效果描述等）
     */
    @PostMapping("/user-skills/{skillId}/use")
    public Result<Map<String, Object>> useSkill(@PathVariable Long skillId,
                                                 @RequestBody(required = false) UseSkillRequest useRequest,
                                                 HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 获取技能信息
            SkillDTO skill = skillService.getSkillById(skillId, userId);
            if (skill == null) {
                return Result.error(404, "技能不存在或未解锁");
            }
            
            // 验证是否为主动技能
            if (!"active".equals(skill.getSkillType())) {
                return Result.error(400, "该技能不是主动技能，无法主动使用");
            }
            
            // 解析技能效果，获取法力值消耗
            com.alibaba.fastjson2.JSONObject effect = com.alibaba.fastjson2.JSON.parseObject(skill.getEffectPayload());
            int manaCost = 0;
            if (effect.containsKey("mana_cost")) {
                manaCost = effect.getIntValue("mana_cost");
            } else if (effect.containsKey("action_cost")) {
                // 兼容旧的action_cost字段
                manaCost = effect.getIntValue("action_cost");
            } else {
                manaCost = 1; // 默认消耗1点
            }
            
            // 验证法力值
            int currentMana = (useRequest != null && useRequest.getCurrentMana() != null) 
                    ? useRequest.getCurrentMana() 
                    : 0;
            if (currentMana < manaCost) {
                return Result.error(400, "法力值不足，需要 " + manaCost + " 点，当前只有 " + currentMana + " 点");
            }
            
            // 返回技能使用信息
            Map<String, Object> result = new HashMap<>();
            result.put("skillId", skillId);
            result.put("skillName", skill.getName());
            result.put("manaCost", manaCost);
            result.put("effectPayload", skill.getEffectPayload());
            result.put("target", effect.getString("target"));
            
            return Result.success("技能可以使用", result);
        } catch (Exception e) {
            return Result.error("使用技能失败: " + e.getMessage());
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

