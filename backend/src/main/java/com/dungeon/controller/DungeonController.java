package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.*;
import com.dungeon.service.DungeonService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 地牢与探索控制器
 */
@RestController
@RequestMapping("/dungeons")
public class DungeonController {

    @Autowired
    private DungeonService dungeonService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取地牢列表
     */
    @GetMapping
    public Result<List<DungeonDTO>> getDungeons() {
        try {
            return Result.success(dungeonService.getAllDungeons());
        } catch (Exception e) {
            return Result.error("获取地牢列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前探索记录
     */
    @GetMapping("/runs/current")
    public Result<RunDTO> getCurrentRun(HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            RunDTO run = dungeonService.getCurrentRun(userId);
            if (run == null) {
                return Result.success("暂无进行中的探索", null);
            }
            return Result.success(run);
        } catch (Exception e) {
            return Result.error("获取探索信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取探索详情
     */
    @GetMapping("/runs/{runId}")
    public Result<RunDTO> getRunDetail(@PathVariable Long runId, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success(dungeonService.getRunDetail(userId, runId));
        } catch (Exception e) {
            return Result.error("获取探索详情失败: " + e.getMessage());
        }
    }

    /**
     * 开启探索
     */
    @PostMapping("/runs/start")
    public Result<RunDTO> startRun(@RequestBody StartRunRequest startRunRequest,
                                   HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success("探索已开启", dungeonService.startRun(userId, startRunRequest));
        } catch (Exception e) {
            return Result.error("开启探索失败: " + e.getMessage());
        }
    }

    /**
     * 探索房间/触发事件
     */
    @PostMapping("/runs/{runId}/explore")
    public Result<RunActionResponse> explore(@PathVariable Long runId,
                                             @RequestBody(required = false) ExploreRequest exploreRequest,
                                             HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success(dungeonService.explore(userId, runId, exploreRequest != null ? exploreRequest : new ExploreRequest()));
        } catch (Exception e) {
            return Result.error("探索操作失败: " + e.getMessage());
        }
    }

    /**
     * 结算战斗
     */
    @PostMapping("/runs/{runId}/battle")
    public Result<RunActionResponse> battle(@PathVariable Long runId,
                                            @RequestBody(required = false) BattleRequest battleRequest,
                                            HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success(dungeonService.resolveBattle(userId, runId, battleRequest != null ? battleRequest : new BattleRequest()));
        } catch (Exception e) {
            return Result.error("战斗操作失败: " + e.getMessage());
        }
    }

    /**
     * 结束探索
     */
    @PostMapping("/runs/{runId}/end")
    public Result<RunDTO> endRun(@PathVariable Long runId,
                                 @RequestBody(required = false) EndRunRequest endRunRequest,
                                 HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success("探索已结束", dungeonService.finishRun(userId, runId, endRunRequest));
        } catch (Exception e) {
            return Result.error("结束探索失败: " + e.getMessage());
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

