package com.dungeon.service;

import com.dungeon.dto.AISuggestionDTO;
import com.dungeon.entity.UserPlayerCharacter;
import com.dungeon.mapper.UserPlayerCharacterMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI建议服务
 */
@Service
public class AISuggestionService {

    @Autowired
    private UserPlayerCharacterMapper userPlayerCharacterMapper;

    /**
     * 获取AI建议
     * @param userId 用户ID
     * @return AI建议列表
     */
    public List<AISuggestionDTO> getAISuggestions(Long userId) {
        List<AISuggestionDTO> suggestions = new ArrayList<>();

        // 获取用户角色信息
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .last("LIMIT 1");
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);

        // 根据角色状态生成建议
        if (character != null) {
            // 建议1：压力管理
            if (character.getCurrentStress() != null && character.getCurrentStress() > 50) {
                AISuggestionDTO suggestion1 = new AISuggestionDTO();
                suggestion1.setId(1L);
                suggestion1.setType("strategy");
                suggestion1.setTitle("压力过高");
                suggestion1.setContent("你的角色压力值较高，建议前往酒馆或教堂缓解压力");
                suggestion1.setPriority(5);
                suggestions.add(suggestion1);
            }

            // 建议2：血量管理
            if (character.getCurrentHp() != null && character.getCurrentHp() < character.getMaxHp() * 0.5) {
                AISuggestionDTO suggestion2 = new AISuggestionDTO();
                suggestion2.setId(2L);
                suggestion2.setType("strategy");
                suggestion2.setTitle("血量不足");
                suggestion2.setContent("你的角色血量较低，建议使用治疗道具或休息");
                suggestion2.setPriority(4);
                suggestions.add(suggestion2);
            }

            // 建议3：探索建议
            AISuggestionDTO suggestion3 = new AISuggestionDTO();
            suggestion3.setId(3L);
            suggestion3.setType("explore");
            suggestion3.setTitle("开始探索");
            suggestion3.setContent("准备好后，可以开始新的地牢探索，获取更多奖励");
            suggestion3.setPriority(3);
            suggestions.add(suggestion3);
        } else {
            // 如果没有角色，建议创建角色
            AISuggestionDTO suggestion = new AISuggestionDTO();
            suggestion.setId(1L);
            suggestion.setType("upgrade");
            suggestion.setTitle("创建角色");
            suggestion.setContent("你还没有创建角色，建议先创建一个角色开始游戏");
            suggestion.setPriority(5);
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 刷新AI建议
     * @param userId 用户ID
     * @return 新的AI建议列表
     */
    public List<AISuggestionDTO> refreshAISuggestions(Long userId) {
        // 重新生成建议
        return getAISuggestions(userId);
    }
}

