<template>
  <view class="camp-container">
    <!-- 背景图片 - 完整展示 -->
    <image class="camp-background" src="/static/yingdi.png" mode="aspectFill"></image>
    
    <!-- 暗黑遮罩层 -->
    <view class="dark-overlay"></view>
    
    <!-- 顶部角色信息 -->
    <view class="character-info-section">
      <view v-if="playerCharacter" class="character-card">
        <view class="character-avatar">
            <image 
              :src="avatarImageSrc" 
              mode="aspectFill" 
              class="avatar-image"
              @error="handleAvatarError"
            ></image>
          </view>
        <view class="character-details">
          <view class="character-name">{{ playerCharacter.playerCharacterName || '冒险者' }}</view>
          <view class="character-stats">
            <view class="stat-item">
              <text class="stat-label">等级:</text>
              <text class="stat-value">Lv.{{ playerCharacter.level || 1 }}</text>
        </view>
            <view class="stat-item">
            <text class="stat-label">生命:</text>
              <text class="stat-value">{{ (playerCharacter.currentHp ?? playerCharacter.maxHp ?? 0) }}/{{ playerCharacter.maxHp ?? 100 }}</text>
          </view>
            <view class="stat-item">
              <text class="stat-label">经验:</text>
            <text class="stat-value">{{ getCurrentExp() }}/{{ getMaxExp() }}</text>
          </view>
          </view>
          <!-- 生命值进度条 -->
          <view class="hp-bar-container">
            <view class="hp-bar-bg">
              <view class="hp-bar-fill" :style="{ width: hpPercentage + '%' }"></view>
            </view>
          </view>
          <!-- 经验值进度条 -->
          <view class="exp-bar-container">
            <view class="exp-bar-bg">
              <view class="exp-bar-fill" :style="{ width: expPercentage + '%' }"></view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 左侧导航 -->
      <view class="nav-side nav-left">
            <view 
          class="nav-item"
          @click="goToInventory"
        >
          <view class="nav-icon-wrapper">
            <i class="fas fa-shopping-bag nav-icon"></i>
              </view>
          <text class="nav-label">背包</text>
            </view>

              <view 
          class="nav-item"
          @click="goToSkills"
        >
          <view class="nav-icon-wrapper">
            <i class="fas fa-sitemap nav-icon"></i>
                </view>
          <text class="nav-label">技能</text>
            </view>
          </view>

      <!-- 中间区域 - 开始闯关按钮 -->
      <view class="center-action-area">
        <button class="start-battle-btn" @click="startBattle">
          <view class="btn-glow"></view>
          <view class="btn-shadow"></view>
          <view class="btn-content">
            <i class="fas fa-sword btn-icon"></i>
            <text class="btn-text">开始闯关</text>
          </view>
        </button>
      </view>

      <!-- 右侧导航 -->
      <view class="nav-side nav-right">
              <view 
          class="nav-item"
          @click="goToShop"
        >
          <view class="nav-icon-wrapper">
            <i class="fas fa-store nav-icon"></i>
                </view>
          <text class="nav-label">商城</text>
          </view>

          <view 
          class="nav-item"
          @click="goToAchievements"
        >
          <view class="nav-icon-wrapper">
            <i class="fas fa-trophy nav-icon"></i>
              </view>
          <text class="nav-label">成就</text>
              </view>
            </view>
            </view>

    <!-- 底部压力显示区域 -->
    <view class="stress-section">
      <view class="stress-container" @click="toggleStressDetails">
        <!-- 压力标题 -->
        <view class="stress-header">
          <i class="fas fa-skull stress-icon"></i>
          <text class="stress-title">压力状态</text>
          <i class="fas fa-chevron-down stress-arrow" :class="{ 'expanded': showStressDetails }"></i>
        </view>
        
        <!-- 压力条 -->
        <view class="stress-bar-wrapper">
          <view class="stress-bar-bg">
            <view 
              class="stress-bar-fill"
              :class="stressLevelClass"
              :style="{ width: stressPercentage + '%' }"
            >
              <view class="stress-bar-glow"></view>
            </view>
          </view>
          <view class="stress-value">
            <text class="stress-number">{{ currentStress }}</text>
            <text class="stress-max">/100</text>
            <text class="stress-level-text" :class="stressLevelClass">{{ stressLevelText }}</text>
          </view>
        </view>
        
        <!-- 压力等级指示点 -->
        <view class="stress-dots">
          <view 
            v-for="level in 4" 
            :key="level"
            class="stress-dot"
            :class="{ 
              'active': level <= stressLevel,
              [`level-${level}`]: true
            }"
          ></view>
        </view>
      </view>
      
      <!-- Debuff显示区域 -->
      <view v-if="showStressDetails && activeDebuffs.length > 0" class="debuffs-section">
        <view class="debuffs-title">
          <i class="fas fa-exclamation-triangle debuff-title-icon"></i>
          <text>负面效果</text>
        </view>
        <view class="debuffs-list">
          <view 
            v-for="(debuff, index) in activeDebuffs" 
            :key="`${debuff.stressLevel}-${index}`"
            class="debuff-item"
            :class="debuff.debuffType || debuff.type"
          >
            <view class="debuff-icon-wrapper">
              <i :class="getDebuffIcon(debuff.debuffType || debuff.type)"></i>
            </view>
            <view class="debuff-info">
              <view class="debuff-name">{{ debuff.debuffName || debuff.name }}</view>
              <view class="debuff-description">{{ debuff.effectDescription || debuff.description }}</view>
            </view>
            <view class="debuff-badge">
              <text class="debuff-level">Lv.{{ debuff.stressLevel }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 背包模态框 -->
    <view v-if="showInventoryModal" class="modal-overlay" @click="showInventoryModal = false">
      <view class="modal-container" @click.stop>
        <view class="modal-header">
          <text class="modal-title">背包</text>
          <button class="modal-close" @click="showInventoryModal = false">
        <i class="fas fa-times"></i>
      </button>
    </view>
        <view class="modal-body">
          <view v-if="loadingInventory" class="loading-text">加载中...</view>
          <view v-else-if="inventoryItems.length === 0" class="empty-text">
            <i class="fas fa-box-open"></i>
            <text>背包为空</text>
            </view>
          <view v-else class="inventory-list">
            <view 
              v-for="item in inventoryItems" 
              :key="item.id"
              class="inventory-item"
            >
              <view class="item-icon">
                <i :class="getItemIcon(item.itemType)"></i>
              </view>
              <view class="item-info">
                <text class="item-name">{{ item.name }}</text>
                <text class="item-quantity">数量: {{ item.quantity }}</text>
                  </view>
                  </view>
                </view>
              </view>
              </view>
            </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { storeToRefs } from 'pinia'
import { campApi, stressApi } from '@/api/request'
import { useCampStore } from '@/stores/camp'
import type { StressDebuffConfig } from '@/types'

// uni-app 类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
}

// 使用 store
const campStore = useCampStore()
const { playerCharacter } = storeToRefs(campStore)

// 状态
const showInventoryModal = ref(false)
const inventoryItems = ref<any[]>([])
const loadingInventory = ref(false)
const avatarImageSrc = ref('/static/tabbar/touxiang.jpg')

// 压力系统状态
const currentStress = ref(0)
const stressLevel = ref(1)
const activeDebuffs = ref<any[]>([])
const showStressDetails = ref(false)
const loadingStress = ref(false)

// 计算属性
const hpPercentage = computed(() => {
  if (!playerCharacter.value?.maxHp) return 0
  const currentHp = playerCharacter.value.currentHp ?? playerCharacter.value.maxHp ?? 0
  return Math.min(100, Math.max(0, (currentHp / playerCharacter.value.maxHp) * 100))
})

const expPercentage = computed(() => {
  const current = getCurrentExp()
  const max = getMaxExp()
  if (max === 0) return 0
  return Math.min(100, Math.max(0, (current / max) * 100))
})

// 获取当前经验值
function getCurrentExp(): number {
  // 这里可以根据实际数据结构获取经验值
  // 如果playerCharacter有exp字段，使用它
  return playerCharacter.value?.exp || playerCharacter.value?.playerExp || 0
}

// 获取最大经验值（根据等级计算）
function getMaxExp(): number {
  const level = playerCharacter.value?.level || 1
  // 简单的经验值计算：每级需要 100 * level 的经验值
  return level * 100
}

// 处理头像加载错误
function handleAvatarError() {
  avatarImageSrc.value = '/static/tabbar/touxiang.jpg'
}

// 导航函数
async function goToInventory() {
  showInventoryModal.value = true
  await loadInventory()
}

async function loadInventory() {
  try {
    loadingInventory.value = true
    const response = await campApi.getInventory()
    if (response.data.code === 200) {
      inventoryItems.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载背包失败:', error)
    inventoryItems.value = []
  } finally {
    loadingInventory.value = false
  }
}

function getItemIcon(itemType: string) {
  const icons: { [key: string]: string } = {
    consumable: 'fas fa-flask',
    material: 'fas fa-gem',
    equipment: 'fas fa-shield-alt',
    special: 'fas fa-star'
  }
  return icons[itemType] || 'fas fa-box'
}

function goToSkills() {
  uni.navigateTo({ url: '/pages/skills/skills' })
}

function goToShop() {
  uni.navigateTo({ url: '/pages/shop/shop' })
}

function goToAchievements() {
  uni.navigateTo({ url: '/pages/achievements/achievements' })
}

function startBattle() {
  uni.navigateTo({ url: '/pages/explore/explore' })
}

// 压力系统相关函数
async function loadStressData() {
  try {
    loadingStress.value = true
    const response = await stressApi.getStressStatus()
    if (response.data.code === 200 && response.data.data) {
      const stressData = response.data.data
      currentStress.value = stressData.currentStress || 0
      stressLevel.value = stressData.stressLevel || 1
      
      // 处理激活的debuff
      if (stressData.activeDebuffs && stressData.activeDebuffs.length > 0) {
        activeDebuffs.value = stressData.activeDebuffs.map((debuff: any) => ({
          ...debuff,
          name: debuff.debuffName || debuff.name,
          description: debuff.effectDescription || debuff.description,
          type: debuff.debuffType || debuff.type
        }))
      } else {
        // 如果没有激活的debuff，根据压力等级获取可能的debuff
        await loadDebuffsByLevel()
      }
    }
  } catch (error) {
    console.error('加载压力数据失败:', error)
    // 如果API失败，尝试从角色数据中获取压力信息
    if (playerCharacter.value) {
      currentStress.value = playerCharacter.value.currentStress || 0
      stressLevel.value = playerCharacter.value.stressLevel || 1
    }
  } finally {
    loadingStress.value = false
  }
}

async function loadDebuffsByLevel() {
  try {
    const response = await stressApi.getStressDebuffs()
    if (response.data.code === 200 && response.data.data) {
      const allDebuffs = response.data.data
      // 根据当前压力等级筛选debuff
      const levelDebuffs = allDebuffs.filter((debuff: any) => 
        debuff.stressLevel === stressLevel.value
      )
      
      // 如果有多个debuff，随机选择一个显示（模拟触发）
      if (levelDebuffs.length > 0) {
        const randomIndex = Math.floor(Math.random() * levelDebuffs.length)
        const selectedDebuff = levelDebuffs[randomIndex]
        activeDebuffs.value = [{
          ...selectedDebuff,
          name: selectedDebuff.debuffName || selectedDebuff.name,
          description: selectedDebuff.effectDescription || selectedDebuff.description,
          type: selectedDebuff.debuffType || selectedDebuff.type
        }]
      }
    }
  } catch (error) {
    console.error('加载debuff配置失败:', error)
  }
}

function toggleStressDetails() {
  showStressDetails.value = !showStressDetails.value
  // 如果展开且没有debuff数据，尝试加载
  if (showStressDetails.value && activeDebuffs.value.length === 0) {
    loadDebuffsByLevel()
  }
}

// 计算属性
const stressPercentage = computed(() => {
  return Math.min(100, Math.max(0, currentStress.value))
})

const stressLevelClass = computed(() => {
  switch (stressLevel.value) {
    case 1: return 'stress-low'
    case 2: return 'stress-medium'
    case 3: return 'stress-high'
    case 4: return 'stress-max'
    default: return 'stress-none'
  }
})

const stressLevelText = computed(() => {
  switch (stressLevel.value) {
    case 1: return '轻松'
    case 2: return '紧张'
    case 3: return '焦虑'
    case 4: return '崩溃'
    default: return '平静'
  }
})

function getDebuffIcon(type: string): string {
  switch (type) {
    case 'mental': return 'fas fa-brain'
    case 'combat': return 'fas fa-sword'
    case 'behavioral': return 'fas fa-walking'
    default: return 'fas fa-exclamation-triangle'
  }
}

onLoad(async () => {
  console.log('[Camp] 营地界面加载')
  // 加载营地数据
  try {
    await campStore.fetchCampData()
  } catch (error) {
    console.error('加载营地数据失败:', error)
  }
  
  // 加载压力数据
  await loadStressData()
})

onMounted(async () => {
  // 如果store中没有数据，尝试加载
  if (!playerCharacter.value) {
    try {
      await campStore.fetchCampData()
    } catch (error) {
      console.error('加载营地数据失败:', error)
    }
  }
  
  // 如果角色数据中有压力信息，使用它
  if (playerCharacter.value) {
    if (playerCharacter.value.currentStress !== undefined) {
      currentStress.value = playerCharacter.value.currentStress
    }
    if (playerCharacter.value.stressLevel !== undefined) {
      stressLevel.value = playerCharacter.value.stressLevel
    }
  }
  
  // 加载压力数据
  await loadStressData()
})
</script>

<style scoped>
.camp-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
}

.camp-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  object-fit: cover;
  object-position: center;
}

/* 暗黑遮罩层 - 增强恐怖氛围 */
.dark-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.3) 0%,
    rgba(0, 0, 0, 0.5) 50%,
    rgba(0, 0, 0, 0.7) 100%
  );
  z-index: 1;
  pointer-events: none;
}

/* 顶部角色信息 */
.character-info-section {
  position: relative;
  z-index: 10;
  padding: 40rpx 40rpx 30rpx;
}

.character-card {
  display: flex;
  align-items: center;
  gap: 30rpx;
  padding: 30rpx;
  background: rgba(0, 0, 0, 0.6);
  border: 3rpx solid rgba(139, 69, 19, 0.8);
  border-radius: 20rpx;
  box-shadow: 
    0 0 30rpx rgba(139, 69, 19, 0.5),
    inset 0 0 20rpx rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10rpx);
}

.character-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 3rpx solid rgba(139, 69, 19, 0.8);
  box-shadow: 0 0 20rpx rgba(139, 69, 19, 0.5);
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.character-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.character-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #daa520;
  text-shadow: 
    0 0 10rpx rgba(218, 165, 32, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  letter-spacing: 2rpx;
}

.character-stats {
  display: flex;
  gap: 30rpx;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(218, 165, 32, 0.8);
}

.stat-value {
  font-size: 24rpx;
  font-weight: 600;
  color: #daa520;
  text-shadow: 0 0 8rpx rgba(218, 165, 32, 0.6);
}

.hp-bar-container,
.exp-bar-container {
  width: 100%;
}

.hp-bar-bg,
.exp-bar-bg {
  width: 100%;
  height: 12rpx;
  background: rgba(0, 0, 0, 0.6);
  border: 1rpx solid rgba(139, 69, 19, 0.5);
  border-radius: 6rpx;
  overflow: hidden;
}

.hp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f97316);
  border-radius: 6rpx;
  transition: width 0.3s ease;
  box-shadow: 0 0 10rpx rgba(239, 68, 68, 0.5);
}

.exp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  border-radius: 6rpx;
  transition: width 0.3s ease;
  box-shadow: 0 0 10rpx rgba(59, 130, 246, 0.5);
}

/* 主要内容区域 */
.main-content {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx;
  min-height: 0;
}

/* 中间操作区域 - 开始闯关按钮 */
.center-action-area {
  position: relative;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 300rpx;
  height: 300rpx;
}

/* 侧边导航 */
.nav-side {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  flex: 0 0 auto;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-item:active {
  transform: scale(0.95);
}

.nav-icon-wrapper {
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  border: 3rpx solid rgba(139, 69, 19, 0.8);
  border-radius: 50%;
  box-shadow: 
    0 0 20rpx rgba(139, 69, 19, 0.5),
    inset 0 0 30rpx rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.nav-icon-wrapper::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(184, 134, 11, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.3s ease, height 0.3s ease;
}

.nav-item:active .nav-icon-wrapper::before {
  width: 200rpx;
  height: 200rpx;
}

.nav-icon-wrapper::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 215, 0, 0.1) 50%,
    transparent 70%
  );
  transform: rotate(45deg);
  transition: left 0.6s ease;
}

.nav-item:active .nav-icon-wrapper::after {
  left: 100%;
}

.nav-icon {
  font-size: 48rpx;
  color: #daa520;
  text-shadow: 
    0 0 10rpx rgba(218, 165, 32, 0.8),
    0 0 20rpx rgba(218, 165, 32, 0.5),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.nav-item:active .nav-icon {
  color: #ffd700;
  text-shadow: 
    0 0 15rpx rgba(255, 215, 0, 1),
    0 0 30rpx rgba(255, 215, 0, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
}

.nav-label {
  font-size: 28rpx;
  color: #daa520;
  text-shadow: 
    0 0 8rpx rgba(218, 165, 32, 0.6),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  font-weight: 600;
  letter-spacing: 2rpx;
  transition: all 0.3s ease;
}

.nav-item:active .nav-label {
  color: #ffd700;
  text-shadow: 
    0 0 12rpx rgba(255, 215, 0, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
}

/* 开始闯关按钮 - 暗黑地牢风格 */
.start-battle-btn {
  position: relative;
  width: 280rpx;
  height: 280rpx;
  border: none;
  background: radial-gradient(
    circle at center,
    rgba(139, 0, 0, 0.6) 0%,
    rgba(69, 0, 0, 0.8) 50%,
    rgba(0, 0, 0, 0.95) 100%
  );
  border: 5rpx solid rgba(139, 0, 0, 0.9);
  border-radius: 50%;
  cursor: pointer;
  overflow: visible;
  box-shadow: 
    0 0 40rpx rgba(139, 0, 0, 0.8),
    0 0 80rpx rgba(139, 0, 0, 0.5),
    inset 0 0 50rpx rgba(0, 0, 0, 0.8),
    0 0 0 10rpx rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 
      0 0 40rpx rgba(139, 0, 0, 0.8),
      0 0 80rpx rgba(139, 0, 0, 0.5),
      inset 0 0 50rpx rgba(0, 0, 0, 0.8),
      0 0 0 10rpx rgba(0, 0, 0, 0.3);
  }
  50% {
    box-shadow: 
      0 0 60rpx rgba(184, 0, 0, 1),
      0 0 120rpx rgba(184, 0, 0, 0.7),
      inset 0 0 70rpx rgba(0, 0, 0, 0.9),
      0 0 0 15rpx rgba(139, 0, 0, 0.5);
  }
}

.start-battle-btn:active {
  transform: scale(0.95);
  animation: none;
  box-shadow: 
    0 0 80rpx rgba(220, 20, 60, 1),
    0 0 160rpx rgba(220, 20, 60, 0.8),
    inset 0 0 100rpx rgba(0, 0, 0, 1);
}

.btn-shadow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(0, 0, 0, 0.8) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  z-index: 0;
  pointer-events: none;
}

.btn-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(220, 20, 60, 0.5) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
  pointer-events: none;
  z-index: 1;
}

.start-battle-btn:active .btn-glow {
  width: 500rpx;
  height: 500rpx;
}

.btn-content {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  z-index: 2;
}

.btn-icon {
  font-size: 72rpx;
  color: #dc143c;
  text-shadow: 
    0 0 20rpx rgba(220, 20, 60, 1),
    0 0 40rpx rgba(220, 20, 60, 0.8),
    0 0 60rpx rgba(139, 0, 0, 0.6),
    0 4rpx 8rpx rgba(0, 0, 0, 0.9);
  transition: all 0.3s ease;
  filter: drop-shadow(0 0 10rpx rgba(220, 20, 60, 0.8));
}

.start-battle-btn:active .btn-icon {
  color: #ff1744;
  text-shadow: 
    0 0 30rpx rgba(255, 23, 68, 1),
    0 0 60rpx rgba(255, 23, 68, 0.9),
    0 0 90rpx rgba(220, 20, 60, 0.8),
    0 4rpx 8rpx rgba(0, 0, 0, 0.9);
  transform: scale(1.15) rotate(5deg);
}

.btn-text {
  font-size: 36rpx;
  font-weight: 900;
  color: #dc143c;
  text-shadow: 
    0 0 15rpx rgba(220, 20, 60, 1),
    0 0 30rpx rgba(220, 20, 60, 0.8),
    0 4rpx 8rpx rgba(0, 0, 0, 0.9);
  letter-spacing: 4rpx;
  transition: all 0.3s ease;
  font-family: 'Arial Black', sans-serif;
}

.start-battle-btn:active .btn-text {
  color: #ff1744;
  text-shadow: 
    0 0 20rpx rgba(255, 23, 68, 1),
    0 0 40rpx rgba(255, 23, 68, 0.9),
    0 4rpx 8rpx rgba(0, 0, 0, 0.9);
  transform: scale(1.1);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-container {
  width: 80%;
  max-width: 600rpx;
  max-height: 80vh;
  background: linear-gradient(135deg, rgba(30, 30, 30, 0.95) 0%, rgba(15, 15, 15, 0.98) 100%);
  border: 3rpx solid rgba(139, 69, 19, 0.8);
  border-radius: 20rpx;
  box-shadow: 
    0 0 40rpx rgba(139, 69, 19, 0.6),
    inset 0 0 30rpx rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  padding: 30rpx 40rpx;
  border-bottom: 2rpx solid rgba(139, 69, 19, 0.5);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #daa520;
  text-shadow: 0 0 10rpx rgba(218, 165, 32, 0.6);
  letter-spacing: 2rpx;
}

.modal-close {
  width: 60rpx;
  height: 60rpx;
  border: none;
  background: rgba(139, 69, 19, 0.3);
  border: 2rpx solid rgba(139, 69, 19, 0.6);
  border-radius: 50%;
  color: #daa520;
  font-size: 32rpx;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.modal-close:active {
  background: rgba(139, 69, 19, 0.5);
  transform: scale(0.95);
}

.modal-body {
  flex: 1;
  padding: 30rpx;
  overflow-y: auto;
}

.loading-text,
.empty-text {
  text-align: center;
  padding: 80rpx 0;
  color: rgba(218, 165, 32, 0.7);
  font-size: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.empty-text i {
  font-size: 80rpx;
  opacity: 0.5;
}

.inventory-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.inventory-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx;
  background: rgba(0, 0, 0, 0.4);
  border: 2rpx solid rgba(139, 69, 19, 0.4);
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.inventory-item:active {
  background: rgba(139, 69, 19, 0.2);
  border-color: rgba(139, 69, 19, 0.6);
}

.item-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(139, 69, 19, 0.3);
  border: 2rpx solid rgba(139, 69, 19, 0.5);
  border-radius: 12rpx;
  font-size: 40rpx;
  color: #daa520;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #daa520;
  text-shadow: 0 0 8rpx rgba(218, 165, 32, 0.5);
}

.item-quantity {
  font-size: 24rpx;
  color: rgba(218, 165, 32, 0.7);
}

/* 压力显示区域 - 暗黑地牢风格 */
.stress-section {
  position: relative;
  z-index: 10;
  padding: 30rpx 40rpx 40rpx;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.9) 0%,
    rgba(0, 0, 0, 0.7) 50%,
    rgba(0, 0, 0, 0.5) 100%
  );
  border-top: 3rpx solid rgba(139, 0, 0, 0.8);
  box-shadow: 
    0 -10rpx 30rpx rgba(0, 0, 0, 0.8),
    inset 0 10rpx 20rpx rgba(0, 0, 0, 0.5);
}

.stress-container {
  background: rgba(0, 0, 0, 0.6);
  border: 2rpx solid rgba(139, 0, 0, 0.6);
  border-radius: 16rpx;
  padding: 24rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    0 0 20rpx rgba(139, 0, 0, 0.4),
    inset 0 0 20rpx rgba(0, 0, 0, 0.5);
}

.stress-container:active {
  background: rgba(0, 0, 0, 0.7);
  border-color: rgba(184, 0, 0, 0.8);
  box-shadow: 
    0 0 30rpx rgba(184, 0, 0, 0.6),
    inset 0 0 30rpx rgba(0, 0, 0, 0.7);
}

.stress-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.stress-icon {
  font-size: 32rpx;
  color: #dc143c;
  text-shadow: 
    0 0 10rpx rgba(220, 20, 60, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  animation: skull-pulse 2s ease-in-out infinite;
}

@keyframes skull-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.stress-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #dc143c;
  text-shadow: 
    0 0 10rpx rgba(220, 20, 60, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  letter-spacing: 2rpx;
  flex: 1;
}

.stress-arrow {
  font-size: 24rpx;
  color: rgba(220, 20, 60, 0.7);
  transition: transform 0.3s ease;
}

.stress-arrow.expanded {
  transform: rotate(180deg);
}

.stress-bar-wrapper {
  margin-bottom: 16rpx;
}

.stress-bar-bg {
  width: 100%;
  height: 20rpx;
  background: rgba(0, 0, 0, 0.8);
  border: 2rpx solid rgba(139, 0, 0, 0.6);
  border-radius: 10rpx;
  overflow: hidden;
  position: relative;
  box-shadow: inset 0 2rpx 8rpx rgba(0, 0, 0, 0.8);
}

.stress-bar-fill {
  height: 100%;
  transition: width 0.5s ease, background 0.3s ease;
  border-radius: 8rpx;
  position: relative;
  overflow: hidden;
}

.stress-bar-fill.stress-none {
  background: linear-gradient(90deg, #4a5568, #718096);
}

.stress-bar-fill.stress-low {
  background: linear-gradient(90deg, #48bb78, #38a169);
  box-shadow: 0 0 10rpx rgba(72, 187, 120, 0.5);
}

.stress-bar-fill.stress-medium {
  background: linear-gradient(90deg, #ed8936, #dd6b20);
  box-shadow: 0 0 15rpx rgba(237, 137, 54, 0.6);
}

.stress-bar-fill.stress-high {
  background: linear-gradient(90deg, #f56565, #e53e3e);
  box-shadow: 0 0 20rpx rgba(245, 101, 101, 0.7);
  animation: stress-high-pulse 1.5s ease-in-out infinite;
}

.stress-bar-fill.stress-max {
  background: linear-gradient(90deg, #dc143c, #8b0000);
  box-shadow: 0 0 30rpx rgba(220, 20, 60, 0.9);
  animation: stress-max-pulse 1s ease-in-out infinite;
}

@keyframes stress-high-pulse {
  0%, 100% {
    box-shadow: 0 0 20rpx rgba(245, 101, 101, 0.7);
  }
  50% {
    box-shadow: 0 0 40rpx rgba(245, 101, 101, 1);
  }
}

@keyframes stress-max-pulse {
  0%, 100% {
    box-shadow: 0 0 30rpx rgba(220, 20, 60, 0.9);
    opacity: 1;
  }
  50% {
    box-shadow: 0 0 60rpx rgba(220, 20, 60, 1);
    opacity: 0.9;
  }
}

.stress-bar-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 100%
  );
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.stress-value {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 12rpx;
}

.stress-number {
  font-size: 36rpx;
  font-weight: 900;
  color: #dc143c;
  text-shadow: 
    0 0 10rpx rgba(220, 20, 60, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
}

.stress-max {
  font-size: 24rpx;
  color: rgba(220, 20, 60, 0.7);
}

.stress-level-text {
  font-size: 24rpx;
  font-weight: 700;
  margin-left: auto;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.8);
}

.stress-level-text.stress-low {
  color: #48bb78;
  background: rgba(72, 187, 120, 0.2);
}

.stress-level-text.stress-medium {
  color: #ed8936;
  background: rgba(237, 137, 54, 0.2);
}

.stress-level-text.stress-high {
  color: #f56565;
  background: rgba(245, 101, 101, 0.2);
}

.stress-level-text.stress-max {
  color: #dc143c;
  background: rgba(220, 20, 60, 0.3);
  animation: text-pulse 1s ease-in-out infinite;
}

@keyframes text-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.stress-dots {
  display: flex;
  gap: 12rpx;
  justify-content: center;
  margin-top: 16rpx;
}

.stress-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: rgba(139, 0, 0, 0.3);
  border: 2rpx solid rgba(139, 0, 0, 0.5);
  transition: all 0.3s ease;
}

.stress-dot.active.level-1 {
  background: #48bb78;
  border-color: #38a169;
  box-shadow: 0 0 10rpx rgba(72, 187, 120, 0.6);
}

.stress-dot.active.level-2 {
  background: #ed8936;
  border-color: #dd6b20;
  box-shadow: 0 0 10rpx rgba(237, 137, 54, 0.6);
}

.stress-dot.active.level-3 {
  background: #f56565;
  border-color: #e53e3e;
  box-shadow: 0 0 15rpx rgba(245, 101, 101, 0.8);
  animation: dot-pulse 1.5s ease-in-out infinite;
}

.stress-dot.active.level-4 {
  background: #dc143c;
  border-color: #8b0000;
  box-shadow: 0 0 20rpx rgba(220, 20, 60, 1);
  animation: dot-pulse 1s ease-in-out infinite;
}

@keyframes dot-pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.8;
  }
}

/* Debuff显示区域 */
.debuffs-section {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 2rpx solid rgba(139, 0, 0, 0.4);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.debuffs-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #dc143c;
  text-shadow: 
    0 0 10rpx rgba(220, 20, 60, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
}

.debuff-title-icon {
  font-size: 28rpx;
  color: #f56565;
  animation: warning-blink 1.5s ease-in-out infinite;
}

@keyframes warning-blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.debuffs-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.debuff-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: rgba(0, 0, 0, 0.5);
  border-left: 4rpx solid;
  border-radius: 8rpx;
  transition: all 0.3s ease;
  box-shadow: 
    0 2rpx 8rpx rgba(0, 0, 0, 0.5),
    inset 0 0 10rpx rgba(0, 0, 0, 0.3);
}

.debuff-item.mental {
  border-left-color: #9c27b0;
  background: rgba(156, 39, 176, 0.1);
}

.debuff-item.combat {
  border-left-color: #f56565;
  background: rgba(245, 101, 101, 0.1);
}

.debuff-item.behavioral {
  border-left-color: #ed8936;
  background: rgba(237, 137, 54, 0.1);
}

.debuff-item:active {
  background: rgba(0, 0, 0, 0.7);
  transform: translateX(4rpx);
}

.debuff-icon-wrapper {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  border: 2rpx solid;
  flex-shrink: 0;
}

.debuff-item.mental .debuff-icon-wrapper {
  border-color: #9c27b0;
  color: #ba68c8;
  box-shadow: 0 0 10rpx rgba(156, 39, 176, 0.5);
}

.debuff-item.combat .debuff-icon-wrapper {
  border-color: #f56565;
  color: #fc8181;
  box-shadow: 0 0 10rpx rgba(245, 101, 101, 0.5);
}

.debuff-item.behavioral .debuff-icon-wrapper {
  border-color: #ed8936;
  color: #f6ad55;
  box-shadow: 0 0 10rpx rgba(237, 137, 54, 0.5);
}

.debuff-icon-wrapper i {
  font-size: 28rpx;
}

.debuff-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.debuff-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #dc143c;
  text-shadow: 
    0 0 8rpx rgba(220, 20, 60, 0.6),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
}

.debuff-description {
  font-size: 22rpx;
  color: rgba(220, 20, 60, 0.8);
  line-height: 1.4;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.8);
}

.debuff-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 60rpx;
  padding: 6rpx 12rpx;
  background: rgba(139, 0, 0, 0.6);
  border: 2rpx solid rgba(220, 20, 60, 0.6);
  border-radius: 8rpx;
  flex-shrink: 0;
}

.debuff-level {
  font-size: 20rpx;
  font-weight: 700;
  color: #dc143c;
  text-shadow: 
    0 0 6rpx rgba(220, 20, 60, 0.8),
    0 1rpx 2rpx rgba(0, 0, 0, 0.8);
}
</style>