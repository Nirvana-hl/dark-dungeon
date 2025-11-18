package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.StageDTO;
import com.dungeon.entity.Stage;
import com.dungeon.service.StageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关卡控制器
 */
@RestController
@RequestMapping("/stages")
public class StageController {
    
    @Autowired
    private StageService stageService;
    
    /**
     * 获取所有关卡列表
     * GET /stages
     */
    @GetMapping
    public Result<List<StageDTO>> getAllStages() {
        try {
            List<Stage> stages = stageService.getAllStages();
            List<StageDTO> stageDTOs = stages.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success(stageDTOs);
        } catch (Exception e) {
            return Result.error("获取关卡列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据关卡编号获取关卡详情
     * GET /stages/number/{stageNumber}
     */
    @GetMapping("/number/{stageNumber}")
    public Result<StageDTO> getStageByNumber(@PathVariable Integer stageNumber) {
        try {
            Stage stage = stageService.getStageByNumber(stageNumber);
            if (stage == null) {
                return Result.error(404, "关卡不存在");
            }
            return Result.success(toDTO(stage));
        } catch (Exception e) {
            return Result.error("获取关卡详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据章节编号获取关卡列表
     * GET /stages/chapter/{chapterNumber}
     */
    @GetMapping("/chapter/{chapterNumber}")
    public Result<List<StageDTO>> getStagesByChapter(@PathVariable Integer chapterNumber) {
        try {
            List<Stage> stages = stageService.getStagesByChapter(chapterNumber);
            List<StageDTO> stageDTOs = stages.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success(stageDTOs);
        } catch (Exception e) {
            return Result.error("获取章节关卡失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有Boss关卡
     * GET /stages/boss
     */
    @GetMapping("/boss")
    public Result<List<StageDTO>> getBossStages() {
        try {
            List<Stage> stages = stageService.getBossStages();
            List<StageDTO> stageDTOs = stages.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Result.success(stageDTOs);
        } catch (Exception e) {
            return Result.error("获取Boss关卡失败: " + e.getMessage());
        }
    }
    
    /**
     * 转换为DTO
     */
    private StageDTO toDTO(Stage stage) {
        StageDTO dto = new StageDTO();
        BeanUtils.copyProperties(stage, dto);
        // TODO: 如果需要，可以关联查询地牢信息
        return dto;
    }
}

