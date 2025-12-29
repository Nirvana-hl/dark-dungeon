<template>
  <view class="class-selection-page">
    <!-- 背景装饰 -->
    <view class="background-pattern"></view>
    
    <!-- 顶部标题栏 -->
    <view class="title-banner">
      <view class="title-arrow left-arrow">‹</view>
      <text class="title-text">职业选择</text>
      <view class="title-arrow right-arrow">›</view>
    </view>

    <!-- 主角色展示区域 -->
    <view class="main-character-display" v-if="selectedClass">
      <!-- 角色图片区域 -->
      <view class="character-image-area">
        <image 
          :src="getCharacterImage(selectedClass.code)" 
          mode="aspectFit"
          class="character-image"
        ></image>
        <view class="character-bg-glow"></view>
      </view>
      
      <!-- 职业名称和描述 -->
      <view class="character-info-area">
        <view class="class-name-banner">
          <text class="class-icon">{{ getClassIcon(selectedClass.code) }}</text>
          <text class="class-name-text">{{ selectedClass.name }}</text>
        </view>
        <view class="class-description">
          <text>{{ getClassDescription(selectedClass.code) }}</text>
        </view>
      </view>
    </view>

    <!-- 职业选择网格 -->
    <view class="class-grid-container">
      <view
        v-for="classItem in classes"
        :key="classItem.id"
        class="class-grid-item"
        :class="{ 'selected': selectedClassId === classItem.id, 'new-class': isNewClass(classItem) }"
        @click="selectClass(classItem)"
      >
        <view class="class-item-icon">{{ getClassIcon(classItem.code) }}</view>
        <text class="class-item-name">{{ classItem.name }}</text>
        <view class="new-badge" v-if="isNewClass(classItem)">新职业</view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="action-banner">
      <view class="banner-arrow left-arrow">‹</view>
      <button 
        class="start-adventure-btn"
        :disabled="!selectedClassId || creating"
        @click="handleStartAdventure"
      >
        {{ creating ? '创建中...' : '开启冒险' }}
      </button>
      <view class="banner-arrow right-arrow">›</view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-spinner"></view>
      <text>加载职业信息中...</text>
    </view>

    <!-- 错误提示 -->
    <view v-if="error" class="error-overlay">
      <text>{{ error }}</text>
      <button class="retry-button" @click="loadClasses">重试</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCharactersStore } from '@/stores/characters'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/api/request'
import type { PlayerCharacter } from '@/types'

// uni-app 全局对象类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
  reLaunch: (options: { url: string }) => void
  setStorageSync: (key: string, value: any) => void
}

const classes = ref<PlayerCharacter[]>([])
const selectedClassId = ref<string | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const creating = ref(false)

const charactersStore = useCharactersStore()

// 计算选中的职业
const selectedClass = computed(() => {
  return classes.value.find(c => c.id === selectedClassId.value) || null
})

// 职业图标映射
function getClassIcon(code: string): string {
  const iconMap: Record<string, string> = {
    'warden': '🛡️',
    'warrior': '⚔️',
    'occultist': '🔮',
    'ranger': '🏹',
    'priest': '✨',
    'mage': '🔥',
    'rogue': '🗡️',
    'mechanist': '⚙️',
    'paladin': '🛡️',
    'soulmaster': '👻',
    'druid': '🦌',
    'barbarian': '⚔️'
  }
  return iconMap[code] || '⚔️'
}

// 职业描述映射
function getClassDescription(code: string): string {
  const descMap: Record<string, string> = {
    'warden': '守护者，拥有强大的防御力和生命值，擅长在前线承受伤害',
    'warrior': '近战战士，拥有强大的防御力和生命值，擅长在前线承受伤害',
    'occultist': '神秘学者，掌握黑暗魔法，能够召唤亡灵和施放诅咒',
    'ranger': '远程射手，精准的箭术和陷阱技能，适合远程输出',
    'priest': '神圣牧师，治疗和支援专家，能够恢复生命和驱散负面效果',
    'mage': '元素法师，掌控火焰、冰霜和雷电，强大的范围伤害',
    'rogue': '敏捷盗贼，高爆发伤害和闪避能力，擅长暗杀和偷袭',
    'mechanist': '战争机器,毁灭之源。擅长远距离作战,可瞬间制造多种武装机械,对敌人进行火力覆盖。',
    'paladin': '神圣骑士，拥有强大的防御和治疗能力',
    'soulmaster': '魂灵师，掌控灵魂之力，能够召唤和操控亡灵',
    'druid': '德鲁伊，与自然和谐共存，能够变形和召唤自然力量',
    'barbarian': '野蛮人，狂暴的战士，拥有极高的攻击力和生命值'
  }
  return descMap[code] || '未知职业'
}

// 角色图片映射（可以根据实际图片路径调整）
function getCharacterImage(code: string): string {
  const imageMap: Record<string, string> = {
    'warden': '/static/touxiang.png',
    'mechanist': '/static/touxiang.png',
    'warrior': '/static/touxiang.png',
    'occultist': '/static/touxiang.png',
    'ranger': '/static/touxiang.png',
    'priest': '/static/touxiang.png',
    'mage': '/static/touxiang.png',
    'rogue': '/static/touxiang.png'
  }
  return imageMap[code] || '/static/touxiang.png'
}

// 判断是否为新职业（可以根据实际情况调整）
function isNewClass(classItem: PlayerCharacter): boolean {
  // 这里可以根据实际需求判断，比如根据创建时间等
  return classItem.code === 'mechanist'
}

// 从 API 加载职业列表
async function loadClasses() {
  loading.value = true
  error.value = null

  try {
    const response = await apiClient.get<ApiResponse<PlayerCharacter[]>>(
      '/player-characters'
    )

    if (response.data && response.data.code === 200 && response.data.data) {
      classes.value = response.data.data
      if (classes.value.length > 0 && !selectedClassId.value) {
        // 默认选择第一个职业
        selectedClassId.value = classes.value[0].id
      }
      if (classes.value.length === 0) {
        error.value = '暂无可用职业'
      }
    } else {
      throw new Error(response.data?.message || '加载职业列表失败')
    }
  } catch (err: any) {
    console.error('Load classes error:', err)
    
    if (err.response) {
      const status = err.response.status
      if (status === 401) {
        error.value = '登录已过期，请重新登录'
      } else if (status === 403) {
        error.value = '没有权限访问职业列表'
      } else if (status === 404) {
        error.value = '职业列表接口不存在'
      } else {
        error.value = err.response.data?.message || `加载职业列表失败 (${status})`
      }
    } else if (err.request) {
      error.value = '网络连接失败，请检查后端服务是否正常运行'
    } else {
      error.value = err.message || '加载职业列表失败'
    }
    
    classes.value = []
  } finally {
    loading.value = false
  }
}

// 选择职业
function selectClass(classItem: PlayerCharacter) {
  selectedClassId.value = classItem.id
}

// 确认选择并创建角色
async function handleStartAdventure() {
  if (!selectedClassId.value) return

  creating.value = true
  error.value = null

  try {
    const selectedClass = classes.value.find(c => c.id === selectedClassId.value)
    if (!selectedClass) {
      error.value = '选择的职业不存在'
      creating.value = false
      return
    }

    // 调用后端API创建角色
    try {
      const response = await apiClient.post<ApiResponse<any>>(
        '/user-player-characters',
        { playerCharacterId: Number(selectedClassId.value) }
      )

      if (response.data && response.data.code === 200) {
        console.log('角色创建成功，返回数据:', response.data.data)
        
        // 更新 charactersStore 中的角色数据
        try {
          await charactersStore.loadPlayerCharacter()
          console.log('✅ charactersStore 角色数据已更新')
        } catch (err) {
          console.warn('更新 charactersStore 失败:', err)
        }
        
        // 等待一小段时间确保后端数据已保存
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 跳转到营地页面
        uni.reLaunch({ url: '/pages/camp/camp' })
      } else {
        throw new Error(response.data.message || '创建角色失败')
      }
    } catch (apiError: any) {
      if (apiError.code === 'ECONNREFUSED' || apiError.message?.includes('Network Error')) {
        console.log('Backend not available, using mock data')
        const mockPlayerCharacter = {
          id: `player-${Date.now()}`,
          playerCharacterId: selectedClassId.value,
          name: selectedClass.name,
          code: selectedClass.code,
          baseHp: selectedClass.baseHp,
          hpPerLevel: selectedClass.hpPerLevel,
          maxHp: selectedClass.baseHp,
          currentHp: selectedClass.baseHp,
          level: 1,
          exp: 0
        }
        uni.setStorageSync('mockPlayerCharacter', JSON.stringify(mockPlayerCharacter))
        uni.reLaunch({ url: '/pages/camp/camp' })
      } else {
        const errorMsg = apiError.response?.data?.message || apiError.message || '创建角色失败'
        error.value = errorMsg
        throw apiError
      }
    }
  } catch (err: any) {
    if (!error.value) {
      error.value = '创建角色失败，请稍后重试'
    }
    console.error('Create character error:', err)
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  loadClasses()
})
</script>

<style scoped>
.class-selection-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #2d1b0e 0%, #1a0f08 100%);
  position: relative;
  padding: 40rpx 30rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.background-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(139, 69, 19, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(139, 69, 19, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 标题栏 */
.title-banner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  padding: 20rpx 40rpx;
  background: linear-gradient(135deg, #3d2817 0%, #2d1b0e 100%);
  border: 2rpx solid rgba(212, 175, 55, 0.5);
  border-radius: 12rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.5);
}

.title-arrow {
  font-size: 48rpx;
  color: rgba(212, 175, 55, 0.6);
  font-weight: bold;
}

.title-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
}

/* 主角色展示区域 */
.main-character-display {
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, #3d2817 0%, #2d1b0e 100%);
  border: 2rpx solid rgba(139, 69, 19, 0.5);
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 
    0 0 50rpx rgba(139, 69, 19, 0.3),
    inset 0 0 100rpx rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  min-height: 500rpx;
}

.character-image-area {
  position: relative;
  width: 100%;
  height: 400rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.character-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  z-index: 2;
}

.character-bg-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120%;
  height: 120%;
  background: radial-gradient(circle, rgba(255, 140, 0, 0.3) 0%, transparent 70%);
  z-index: 1;
  pointer-events: none;
}

.character-info-area {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.class-name-banner {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #3d2817 0%, #2d1b0e 100%);
  border: 2rpx solid rgba(212, 175, 55, 0.5);
  border-radius: 8rpx;
  width: fit-content;
}

.class-icon {
  font-size: 32rpx;
}

.class-name-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
}

.class-description {
  padding: 20rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
}

.class-description text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

/* 职业选择网格 */
.class-grid-container {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  padding: 20rpx 0;
}

.class-grid-item {
  position: relative;
  background: linear-gradient(135deg, #3d2817 0%, #2d1b0e 100%);
  border: 2rpx solid rgba(139, 69, 19, 0.5);
  border-radius: 12rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.3);
}

.class-grid-item:active {
  transform: scale(0.95);
}

.class-grid-item.selected {
  border-color: rgba(212, 175, 55, 0.8);
  background: linear-gradient(135deg, #4d3827 0%, #3d2817 100%);
  box-shadow: 
    0 0 20rpx rgba(212, 175, 55, 0.5),
    inset 0 0 20rpx rgba(212, 175, 55, 0.1);
}

.class-item-icon {
  font-size: 64rpx;
  margin-bottom: 8rpx;
}

.class-item-name {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
  text-align: center;
}

.new-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 107, 0.5);
}

/* 底部按钮 */
.action-banner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  padding: 20rpx 40rpx;
}

.banner-arrow {
  font-size: 48rpx;
  color: rgba(212, 175, 55, 0.6);
  font-weight: bold;
}

.start-adventure-btn {
  flex: 1;
  padding: 24rpx 60rpx;
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  border: 2rpx solid rgba(212, 175, 55, 0.8);
  border-radius: 12rpx;
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  box-shadow: 
    0 4rpx 20rpx rgba(76, 175, 80, 0.4),
    inset 0 2rpx 10rpx rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.start-adventure-btn:active:not(:disabled) {
  transform: scale(0.98);
  box-shadow: 
    0 2rpx 10rpx rgba(76, 175, 80, 0.3),
    inset 0 2rpx 10rpx rgba(255, 255, 255, 0.1);
}

.start-adventure-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载和错误状态 */
.loading-overlay,
.error-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 30rpx;
  z-index: 10000;
}

.loading-spinner {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.2);
  border-top-color: #ffd700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay text,
.error-overlay text {
  font-size: 32rpx;
  color: #ffffff;
}

.retry-button {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  color: #1a1a2e;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>

