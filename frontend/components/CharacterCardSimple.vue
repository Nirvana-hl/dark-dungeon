<template>
  <view 
    class="character-card-simple"
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
      <view class="stat-row">
        <i class="fas fa-heart stat-icon health"></i>
        <text>{{ character.currentHp || 1 }}</text>
      </view>
      <view class="stat-row">
        <i class="fas fa-sword stat-icon attack"></i>
        <text>1</text>
      </view>
      <view class="stat-row" v-if="character.currentArmor">
        <i class="fas fa-shield stat-icon armor"></i>
        <text>{{ character.currentArmor }}</text>
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
import type { UserCardCharacter } from '@/types'

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
</script>

<style scoped>
.character-card-simple {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  aspect-ratio: 2/3;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 200px;
}

.character-card-simple:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
  border-color: #ff6b35;
}

.character-card-simple.deployed {
  border-color: #4caf50;
  background: linear-gradient(135deg, #2a2a2a, rgba(76, 175, 80, 0.1));
}

.character-card-simple.draggable {
  cursor: grab;
}

.character-card-simple.draggable:active {
  cursor: grabbing;
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-cost {
  background: #ff6b35;
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-image {
  font-size: 48px;
  color: var(--text-muted);
  opacity: 0.5;
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
  gap: 4px;
  font-size: 12px;
  font-weight: bold;
  color: var(--text-primary);
}

.stat-icon {
  width: 12px;
  height: 12px;
}

.stat-icon.health {
  color: #4caf50;
}

.stat-icon.attack {
  color: #ff6b35;
}

.stat-icon.armor {
  color: #2196f3;
}

.card-actions {
  padding: 8px 12px;
  border-top: 1px solid var(--border-color);
  background: var(--tertiary-bg);
  margin-top: auto;
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
  background: #4caf50;
  color: white;
}

.action-btn.deploy:hover:not(:disabled) {
  background: #45a049;
}

.action-btn.undeploy {
  background: #ff9800;
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
  background: #4caf50;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .character-card-simple {
    aspect-ratio: 3/4;
    max-width: 150px;
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