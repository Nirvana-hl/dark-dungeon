<template>
  <view class="camp-container">
    <!-- 背景图片 -->
    <image class="camp-background" src="/static/yingdi.png" mode="aspectFill"></image>
    
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

      <!-- 中间空白区域 -->
      <view class="center-space"></view>

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

    <!-- 底部中间开始闯关按钮 -->
    <view class="bottom-action">
      <button class="start-battle-btn" @click="startBattle">
        <view class="btn-glow"></view>
        <view class="btn-content">
          <i class="fas fa-sword btn-icon"></i>
          <text class="btn-text">开始闯关</text>
                </view>
                </button>
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
import { campApi } from '@/api/request'
import { useCampStore } from '@/stores/camp'

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

onLoad(async () => {
  console.log('[Camp] 营地界面加载')
// 加载营地数据
  try {
    await campStore.fetchCampData()
  } catch (error) {
    console.error('加载营地数据失败:', error)
  }
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
  align-items: flex-start;
  justify-content: space-between;
  padding: 40rpx 40rpx 0;
  min-height: 0;
}

/* 中间空白区域 */
.center-space {
  flex: 0 0 200rpx;
  min-width: 200rpx;
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

/* 底部操作区域 */
.bottom-action {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0 60rpx;
}

.start-battle-btn {
  position: relative;
  width: 240rpx;
  height: 240rpx;
  border: none;
  background: radial-gradient(circle at center, rgba(139, 69, 19, 0.4) 0%, rgba(0, 0, 0, 0.8) 100%);
  border: 4rpx solid rgba(139, 69, 19, 0.9);
  border-radius: 50%;
  cursor: pointer;
  overflow: visible;
  box-shadow: 
    0 0 30rpx rgba(139, 69, 19, 0.6),
    0 0 60rpx rgba(139, 69, 19, 0.4),
    inset 0 0 40rpx rgba(0, 0, 0, 0.6);
  transition: all 0.3s ease;
}

.start-battle-btn:active {
  transform: scale(0.95);
  box-shadow: 
    0 0 60rpx rgba(184, 134, 11, 0.8),
    0 0 120rpx rgba(184, 134, 11, 0.6),
    inset 0 0 80rpx rgba(0, 0, 0, 0.8);
}

.btn-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(184, 134, 11, 0.4) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
  pointer-events: none;
}

.start-battle-btn:active .btn-glow {
  width: 400rpx;
  height: 400rpx;
}

.btn-content {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  z-index: 2;
}

.btn-icon {
  font-size: 60rpx;
  color: #daa520;
  text-shadow: 
    0 0 15rpx rgba(218, 165, 32, 0.8),
    0 0 30rpx rgba(218, 165, 32, 0.5),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  transition: all 0.3s ease;
}

.start-battle-btn:active .btn-icon {
  color: #ffd700;
  text-shadow: 
    0 0 20rpx rgba(255, 215, 0, 1),
    0 0 40rpx rgba(255, 215, 0, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  transform: scale(1.1);
}

.btn-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #daa520;
  text-shadow: 
    0 0 12rpx rgba(218, 165, 32, 0.8),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
  letter-spacing: 2rpx;
  transition: all 0.3s ease;
}

.start-battle-btn:active .btn-text {
  color: #ffd700;
  text-shadow: 
    0 0 15rpx rgba(255, 215, 0, 1),
    0 2rpx 4rpx rgba(0, 0, 0, 0.8);
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
</style>