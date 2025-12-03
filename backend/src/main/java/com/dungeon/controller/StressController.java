package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.RelieveStressRequest;
import com.dungeon.dto.StressDebuffDTO;
import com.dungeon.dto.StressStatusDTO;
import com.dungeon.service.StressService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 压力系统控制器
 */
@RestController
@RequestMapping("/stress")
public class StressController {

    @Autowired
    private StressService stressService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前压力状态
     * GET /stress/status
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 压力状态
     */
    @GetMapping("/status")
    public Result<StressStatusDTO> getStressStatus(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            StressStatusDTO status = stressService.getStressStatus(userId);
            return Result.success(status);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("获取压力状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取压力debuff配置
     * GET /stress/debuffs
     * 
     * @return debuff配置列表
     */
    @GetMapping("/debuffs")
    public Result<List<StressDebuffDTO>> getStressDebuffs() {
        try {
            List<StressDebuffDTO> debuffs = stressService.getAllDebuffConfigs();
            return Result.success(debuffs);
        } catch (Exception e) {
            return Result.error("获取压力debuff配置失败: " + e.getMessage());
        }
    }

    /**
     * 缓解压力（使用营地设施）
     * POST /stress/relieve
     * 
     * @param relieveRequest 缓解压力请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 缓解后的压力状态
     */
    @PostMapping("/relieve")
    public Result<StressStatusDTO> relieveStress(@RequestBody RelieveStressRequest relieveRequest,
                                                  HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (relieveRequest == null || relieveRequest.getFacilityType() == null) {
                return Result.error(400, "设施类型不能为空");
            }

            StressStatusDTO status = stressService.relieveStress(userId, relieveRequest.getFacilityType());
            return Result.success(status);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("缓解压力失败: " + e.getMessage());
        }
    }

    /**
     * 从请求头获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
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

