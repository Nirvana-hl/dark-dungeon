package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.*;
import com.dungeon.service.AISuggestionService;
import com.dungeon.service.CampService;
import com.dungeon.service.EventService;
import com.dungeon.service.ItemService;
import com.dungeon.service.UserCardCharacterService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 营地数据聚合接口
 */
@RestController
@RequestMapping("/camp")
public class CampController {

    @Autowired
    private CampService campService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserCardCharacterService userCardCharacterService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AISuggestionService aiSuggestionService;

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


    /**
     * 获取用户背包
     * GET /camp/inventory
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 背包道具列表
     */
    @GetMapping("/inventory")
    public Result<List<InventoryItemDTO>> getUserInventory(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<InventoryItemDTO> inventory = itemService.getUserInventory(userId);
            return Result.success(inventory);
        } catch (Exception e) {
            return Result.error("获取背包失败: " + e.getMessage());
        }
    }

    /**
     * 使用道具
     * POST /camp/use-item
     * 
     * @param useRequest 使用请求（包含背包记录ID和数量）
     * @param request HTTP请求（用于获取当前用户）
     * @return 使用结果信息
     */
    @PostMapping("/use-item")
    public Result<String> useItem(@RequestBody UseItemRequest useRequest,
                                   HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (useRequest == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            // 前端传递的是 inventoryId（字符串），需要转换为 Long
            // 如果前端传递的是字符串格式的 inventoryId，需要处理
            if (useRequest.getInventoryId() == null) {
                return Result.error(400, "背包记录ID不能为空");
            }

            String message = itemService.useItem(userId, useRequest);
            return Result.success(message, null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("使用道具失败: " + e.getMessage());
        }
    }

    /**
     * 部署/撤下卡牌角色
     * POST /camp/deploy-card-character
     * 
     * @param deployRequest 部署请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 更新后的卡牌角色
     */
    @PostMapping("/deploy-card-character")
    public Result<UserCardCharacterDTO> deployCardCharacter(@RequestBody DeployCardCharacterRequest deployRequest,
                                                             HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (deployRequest == null || deployRequest.getUserCardCharacterId() == null || deployRequest.getDeploy() == null) {
                return Result.error(400, "卡牌角色ID和部署状态不能为空");
            }

            Long userCardCharacterId = deployRequest.getUserCardCharacterIdAsLong();
            if (userCardCharacterId == null) {
                return Result.error(400, "卡牌角色ID格式错误");
            }

            UserCardCharacterDTO character = userCardCharacterService.deployCardCharacter(
                userId, 
                userCardCharacterId, 
                deployRequest.getDeploy()
            );
            return Result.success(character);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("部署卡牌角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务事件
     * GET /camp/events
     * 
     * @return 事件列表
     */
    @GetMapping("/events")
    public Result<List<EventDTO>> getEvents() {
        try {
            List<EventDTO> events = eventService.getCampEvents();
            return Result.success(events);
        } catch (Exception e) {
            return Result.error("获取任务事件失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务事件
     * POST /camp/complete-event
     * 
     * @param completeRequest 完成事件请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 完成结果信息
     */
    @PostMapping("/complete-event")
    public Result<String> completeEvent(@RequestBody CompleteEventRequest completeRequest,
                                        HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (completeRequest == null || completeRequest.getEventId() == null) {
                return Result.error(400, "事件ID不能为空");
            }

            Long eventId = completeRequest.getEventIdAsLong();
            if (eventId == null) {
                return Result.error(400, "事件ID格式错误");
            }

            String message = eventService.completeEvent(userId, eventId);
            return Result.success(message, null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("完成任务事件失败: " + e.getMessage());
        }
    }


    /**
     * 刷新AI建议
     * POST /camp/refresh-ai-suggestions
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 新的AI建议列表
     */
    @PostMapping("/refresh-ai-suggestions")
    public Result<List<AISuggestionDTO>> refreshAISuggestions(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<AISuggestionDTO> suggestions = aiSuggestionService.refreshAISuggestions(userId);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("刷新AI建议失败: " + e.getMessage());
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

