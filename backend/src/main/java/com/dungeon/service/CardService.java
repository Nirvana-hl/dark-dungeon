package com.dungeon.service;

import com.dungeon.dto.CardDTO;
import com.dungeon.entity.Card;
import com.dungeon.mapper.CardMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 卡牌模板服务（法术/装备）
 */
@Service
public class CardService {

    @Autowired
    private CardMapper cardMapper;

    public List<CardDTO> getAllCards() {
        List<Card> list = cardMapper.selectList(null);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CardDTO> getByType(String cardType) {
        if (cardType == null) {
            return getAllCards();
        }
        return cardMapper.selectByCardType(cardType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getByRarity(String rarity) {
        if (rarity == null) {
            return getAllCards();
        }
        return cardMapper.selectByRarity(rarity).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CardDTO getById(Long id) {
        Card card = cardMapper.selectById(id);
        if (card == null) {
            return null;
        }
        return toDTO(card);
    }

    public CardDTO create(CardDTO dto) {
        Card card = new Card();
        BeanUtils.copyProperties(dto, card);
        cardMapper.insert(card);
        return toDTO(card);
    }

    public CardDTO update(Long id, CardDTO dto) {
        Card card = cardMapper.selectById(id);
        if (card == null) {
            return null;
        }
        BeanUtils.copyProperties(dto, card, "id");
        cardMapper.updateById(card);
        return toDTO(card);
    }

    public void delete(Long id) {
        cardMapper.deleteById(id);
    }

    private CardDTO toDTO(Card card) {
        CardDTO dto = new CardDTO();
        BeanUtils.copyProperties(card, dto);
        return dto;
    }
}


