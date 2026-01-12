<template>
  <view 
    class="character-card"
    :class="{ 
      'deployed': deployed, 
      'draggable': !deployed
    }"
    :draggable="!deployed"
    @click="$emit('click', character)"
  >
    <!-- 卡片头部 -->
    <view class="card-header">
      <view class="card-name">{{ `角色 #${character.id.slice(-6)}` }}</view>
      <view class="card-cost">1</view>
    </view>
    
    <!-- 卡片图片 -->
    <view class="card-image">
      <view class="placeholder-image">
        <i class="fas fa-user"></i>
      </view>
      <view class="level-indicator" v-if="character.currentStarLevel">
        <i v-for="i in character.currentStarLevel" :key="i" class="fas fa-star"></i>
      </view>
    </view>
    
    <!-- 卡片属性 -->
    <view class="card-stats">
      <view class="stat-row stat-row-health">
        <view class="stat-icon-wrapper health">
          <i class="fas fa-heart stat-icon"></i>
          <text class="stat-value">{{ character.currentHp || 1 }}</text>
        </view>
      </view>
      <view class="stat-row stat-row-attack">
        <view class="stat-icon-wrapper attack">
          <i class="fas fa-sword stat-icon"></i>
          <text class="stat-value">1</text>
        </view>
      </view>
      <view class="stat-row stat-row-armor" v-if="character.currentArmor">
        <view class="stat-icon-wrapper armor">
          <i class="fas fa-shield stat-icon"></i>
          <text class="stat-value">{{ character.currentArmor }}</text>
        </view>
      </view>
    </view>
    
    <!-- 特性标签 -->
    <view class="card-traits" v-if="traits.length > 0">
      <view 
        v-for="trait in traits.slice(0, 2)" 
        :key="trait.name"
        class="trait-tag"
        :class="trait.type"
        :title="trait.description"
      >
        {{ trait.name }}
      </view>
    </view>
    
    <!-- 操作按钮 -->
    <view class="card-actions">
      <button 
        v-if="!deployed" 
        class="action-btn deploy"
        @click.stop="$emit('deploy', character)"
        :disabled="!canDeploy"
      >
        <i class="fas fa-plus"></i>
        部署
      </button>
      <button 
        v-else 
        class="action-btn undeploy"
        @click.stop="$emit('undeploy', character)"
      >
        <i class="fas fa-minus"></i>
        撤下
      </button>
    </view>
    
    <!-- 部署状态指示器 -->
    <view v-if="deployed" class="deployed-indicator">
      <i class="fas fa-check-circle"></i>
      已部署
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserCardCharacter, CardCharacterTrait } from '@/types'
import { TraitType } from '@/types'

interface Props {
  character: UserCardCharacter
  deployed?: boolean
  canDeploy?: boolean
}

interface Emits {
  (e: 'deploy', character: UserCardCharacter): void
  (e: 'undeploy', character: UserCardCharacter): void
  (e: 'click', character: UserCardCharacter): void
}

const props = withDefaults(defineProps<Props>(), {
  deployed: false,
  canDeploy: true
})

const emit = defineEmits<Emits>()

// 计算特性（这里需要从API获取，暂时使用模拟数据）
const traits = computed<CardCharacterTrait[]>(() => [
  {
    id: 'trait-1',
    cardCharacterId: props.character.cardCharacterId,
    name: '勇敢',
    type: TraitType.POSITIVE,
    effectPayload: { damageBonus: 1 },
    scalingPayload: {},
    description: '攻击力 +1'
  }
])

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-character.png'
}
</script>

<style scoped>
.character-card {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  box-shadow: var(--shadow-light);
  aspect-ratio: 2/3;
  display: flex;
  flex-direction: column;
}

.character-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-medium);
  border-color: var(--text-accent);
}

.character-card.deployed {
  border-color: var(--success);
  background: linear-gradient(135deg, var(--secondary-bg), rgba(76, 175, 80, 0.1));
}

.character-card.draggable {
  cursor: grab;
}

.character-card.draggable:active {
  cursor: grabbing;
}

/* 稀有度样式 */
.character-card.rarity-common {
  border-color: var(--common);
}

.character-card.rarity-rare {
  border-color: var(--rare);
  box-shadow: 0 0 10px rgba(33, 150, 243, 0.3);
}

.character-card.rarity-epic {
  border-color: var(--epic);
  box-shadow: 0 0 15px rgba(156, 39, 176, 0.4);
}

.character-card.rarity-legendary {
  border-color: var(--legendary);
  box-shadow: 0 0 20px rgba(255, 152, 0, 0.5);
  animation: legendary-glow 2s ease-in-out infinite alternate;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--tertiary-bg);
  border-bottom: 1px solid var(--border-color);
}

.card-name {
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
  flex: 1;
  /* allow full names to wrap instead of truncating with ellipsis */
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  word-break: break-word;
}

.card-cost {
  background: var(--text-accent);
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.card-image {
  position: relative;
  height: 120px;
  overflow: hidden;
  background: var(--tertiary-bg);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.level-indicator {
  position: absolute;
  top: 5px;
  right: 5px;
  display: flex;
  gap: 2px;
}

.level-indicator i {
  color: var(--warning);
  font-size: 12px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
}

.card-stats {
  padding: 8px 12px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  background: rgba(0, 0, 0, 0.2);
}

.stat-row {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.stat-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
}

.stat-icon-wrapper.health {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.9) 0%, rgba(185, 28, 28, 0.9) 100%);
  border-color: rgba(255, 200, 200, 0.5);
}

.stat-icon-wrapper.attack {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.9) 0%, rgba(220, 38, 38, 0.9) 100%);
  border-color: rgba(255, 200, 150, 0.5);
}

.stat-icon-wrapper.armor {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.9) 0%, rgba(13, 71, 161, 0.9) 100%);
  border-color: rgba(200, 220, 255, 0.5);
}

.stat-icon {
  position: absolute;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  opacity: 0.7;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.stat-value {
  position: relative;
  z-index: 2;
  font-size: 13px;
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.9);
  line-height: 1;
}

.card-traits {
  padding: 4px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  flex: 1;
}

.trait-tag {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}

.trait-tag.positive {
  background: rgba(76, 175, 80, 0.2);
  color: var(--success);
  border: 1px solid var(--success);
}

.trait-tag.negative {
  background: rgba(244, 67, 54, 0.2);
  color: var(--error);
  border: 1px solid var(--error);
}

.trait-tag.neutral {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.card-actions {
  padding: 8px 12px;
  border-top: 1px solid var(--border-color);
  background: var(--tertiary-bg);
}

.action-btn {
  width: 100%;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-btn.deploy {
  background: var(--success);
  color: white;
}

.action-btn.deploy:hover:not(:disabled) {
  background: #45a049;
}

.action-btn.undeploy {
  background: var(--warning);
  color: white;
}

.action-btn.undeploy:hover {
  background: #e68900;
}

.action-btn:disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.6;
}

.deployed-indicator {
  position: absolute;
  top: 5px;
  left: 5px;
  background: var(--success);
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
}

@keyframes legendary-glow {
  0% { box-shadow: 0 0 10px rgba(255, 152, 0, 0.5); }
  100% { box-shadow: 0 0 20px rgba(255, 152, 0, 0.8); }
}

@media (max-width: 768px) {
  .character-card {
    aspect-ratio: 3/4;
  }
  
  .card-image {
    height: 100px;
  }
  
  .card-name {
    font-size: 12px;
  }
  
  .stat-row {
    font-size: 11px;
  }
}
</style>