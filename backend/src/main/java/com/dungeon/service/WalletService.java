package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.UserWalletDTO;
import com.dungeon.entity.UserWallet;
import com.dungeon.mapper.UserWalletMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 钱包服务
 */
@Service
public class WalletService {

    @Autowired
    private UserWalletMapper userWalletMapper;

    /**
     * 获取用户所有钱包信息
     * @param userId 用户ID
     * @return 钱包列表
     */
    public List<UserWalletDTO> getUserWallets(Long userId) {
        QueryWrapper<UserWallet> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserWallet> wallets = userWalletMapper.selectList(wrapper);
        
        return wallets.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 增加货币
     * @param userId 用户ID
     * @param currencyType 货币类型
     * @param amount 增加数量
     * @return 更新后的钱包信息
     */
    @Transactional
    public UserWalletDTO addCurrency(Long userId, String currencyType, Long amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("增加数量必须大于0");
        }

        // 获取或创建钱包
        UserWallet wallet = getOrCreateWallet(userId, currencyType);
        
        // 增加余额
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletMapper.updateById(wallet);
        
        return toDTO(wallet);
    }

    /**
     * 消费货币
     * @param userId 用户ID
     * @param currencyType 货币类型
     * @param amount 消费数量
     * @return 更新后的钱包信息
     */
    @Transactional
    public UserWalletDTO consumeCurrency(Long userId, String currencyType, Long amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("消费数量必须大于0");
        }

        // 获取钱包
        UserWallet wallet = getOrCreateWallet(userId, currencyType);
        
        // 检查余额
        if (wallet.getBalance() < amount) {
            throw new RuntimeException("余额不足");
        }
        
        // 扣除余额
        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletMapper.updateById(wallet);
        
        return toDTO(wallet);
    }

    /**
     * 获取或创建钱包
     * @param userId 用户ID
     * @param currencyType 货币类型
     * @return 钱包对象
     */
    private UserWallet getOrCreateWallet(Long userId, String currencyType) {
        QueryWrapper<UserWallet> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("currency_type", currencyType);
        UserWallet wallet = userWalletMapper.selectOne(wrapper);
        
        if (wallet == null) {
            // 创建新钱包
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setCurrencyType(currencyType);
            wallet.setBalance(0L);
            wallet.setUpdatedAt(LocalDateTime.now());
            userWalletMapper.insert(wallet);
        }
        
        return wallet;
    }

    /**
     * 实体转DTO
     */
    private UserWalletDTO toDTO(UserWallet wallet) {
        UserWalletDTO dto = new UserWalletDTO();
        BeanUtils.copyProperties(wallet, dto);
        return dto;
    }
}

