<template>
  <view class="achievements-page">
    <!-- 进度条 -->
    <view class="progress-section">
      <view class="progress-bar-container">
        <view class="progress-bar" :style="{ width: progressPercentage + '%' }"></view>
      </view>
      <text class="progress-text">{{ unlockedCount }}/{{ totalAchievements }}</text>
    </view>

    <!-- 分类标签页 -->
    <nav class="tabs-nav">
      <view 
        v-for="category in categories" 
        :key="category.id"
        :class="['tab-item', { active: selectedCategory === category.id }]"
        @click="selectedCategory = category.id"
      >
        <text>{{ category.name }}</text>
      </view>
    </nav>

    <!-- 成就列表 -->
    <main class="achievements-main">
      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <view class="loading-spinner">
          <i class="fas fa-spinner fa-spin"></i>
          <p>加载成就数据中...</p>
        </view>
      </view>
      
      <!-- 错误提示 -->
      <view v-else-if="error" class="error-container">
        <view class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          <p>{{ error }}</p>
          <button @click="loadAchievements" class="retry-btn">
            <i class="fas fa-redo"></i> 重试
          </button>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="!loading && filteredAchievements.length === 0" class="empty-container">
        <view class="empty-message">
          <i class="fas fa-inbox"></i>
          <p>暂无成就数据</p>
        </view>
      </view>
      
      <!-- 成就列表 -->
      <view v-else class="achievements-list">
        <view 
          v-for="(achievement, index) in filteredAchievements" 
          :key="achievement.id"
          class="achievement-item"
          :class="{ unlocked: achievement.unlocked }"
        >
          <!-- 左侧角色图标 -->
          <view class="achievement-character-icon">
            <view class="character-icon-bg">
              <i :class="achievement.icon"></i>
            </view>
          </view>
          
          <!-- 中间内容 -->
          <view class="achievement-content">
            <text class="achievement-title">{{ achievement.name }}</text>
            <text class="achievement-description">{{ formatDescription(achievement.description) }}</text>
          </view>
          
          <!-- 右侧完成状态 -->
          <view class="achievement-status-badge">
            <!-- 已完成：显示文字 -->
            <view v-if="achievement.unlocked" class="status-circle completed">
              <text class="status-text">已完成</text>
            </view>
            <!-- 未完成：显示进度圆圈 -->
            <view v-else class="progress-circle-container">
              <svg class="progress-circle" viewBox="0 0 36 36">
                <defs>
                  <linearGradient id="progressGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#d32f2f;stop-opacity:1" />
                    <stop offset="50%" style="stop-color:#ff5252;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#d32f2f;stop-opacity:1" />
                  </linearGradient>
                </defs>
                <circle
                  class="progress-bg"
                  cx="18"
                  cy="18"
                  r="16"
                  fill="none"
                  stroke="rgba(255,255,255,0.15)"
                  stroke-width="3"
                />
                <circle
                  class="progress-fill"
                  cx="18"
                  cy="18"
                  r="16"
                  fill="none"
                  :stroke-dasharray="achievementProgress(achievement)"
                  stroke-dashoffset="0"
                  stroke-linecap="round"
                  stroke="url(#progressGradient)"
                  transform="rotate(-90 18 18)"
                />
              </svg>
              <text class="progress-percent">{{ Math.round(achievement.progress || 0) }}%</text>
            </view>
          </view>
        </view>
      </view>
    </main>

    <!-- 成就详情模态框 -->
    <view v-if="selectedAchievement" class="modal-overlay" @click="closeModal">
      <view class="achievement-modal" @click.stop>
        <view class="modal-header">
          <h2>成就详情</h2>
          <button class="close-modal" @click="closeModal">
            <i class="fas fa-times"></i>
          </button>
        </view>
        <view class="modal-content">
          <view class="modal-icon">
            <view class="icon-bg large" :class="selectedAchievement.rarity">
              <i :class="selectedAchievement.icon"></i>
            </view>
          </view>
          <view class="modal-info">
            <h3>{{ selectedAchievement.name }}</h3>
            <p>{{ selectedAchievement.description }}</p>
            <view class="modal-stats" v-if="selectedAchievement.stats">
              <view v-for="(stat, key) in selectedAchievement.stats" :key="key" class="stat-row">
                <text class="stat-label">{{ getStatLabel(key) }}</text>
                <text class="stat-value">{{ stat }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { achievementApi } from '@/api/request'

interface Achievement {
  id: string | number
  name: string
  description: string
  icon: string
  rarity: 'common' | 'rare' | 'epic' | 'legendary'
  category: string
  requirements?: string[]
  rewards: Array<{ type: string; value: string | number }>
  unlocked: boolean
  unlockedAt?: string
  progress?: number
  featured?: boolean
  stats?: Record<string, any>
}

// 成就数据（从后端获取）
const achievements = ref<Achievement[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// uni-app 全局对象类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
}

// 分类数据
const categories = ref([
  { id: 'progression', name: '进度' },
  { id: 'mastery', name: '精通' },
  { id: 'collection', name: '收集' },
  { id: 'social', name: '社交' }
])

// 状态
const selectedCategory = ref('progression')
const selectedAchievement = ref<Achievement | null>(null)

// 计算属性
const filteredAchievements = computed(() => {
  // 根据选中的分类过滤成就
  return achievements.value.filter(a => a.category === selectedCategory.value)
})

const progressPercentage = computed(() => {
  if (totalAchievements.value === 0) return 0
  return Math.round((unlockedCount.value / totalAchievements.value) * 100)
})

const unlockedCount = computed(() => {
  return achievements.value.filter(a => a.unlocked).length
})

const totalAchievements = computed(() => {
  return achievements.value.length
})

// 方法
const formatDescription = (description: string) => {
  // 删除"普通难度"前缀
  return description.replace(/^普通难度[,，]\s*/, '').replace(/^困难难度[,，]\s*/, '').replace(/^噩梦难度[,，]\s*/, '')
}

const getRarityLabel = (rarity: string) => {
  const labels = {
    common: '普通',
    rare: '稀有',
    epic: '史诗',
    legendary: '传说'
  }
  return labels[rarity as keyof typeof labels] || '未知'
}

const getRewardIcon = (type: string) => {
  const icons: Record<string, string> = {
    gold: 'fas fa-coins gold',
    exp: 'fas fa-star blue',
    soulstone: 'fas fa-gem purple',
    crystal: 'fas fa-crystal cyan',
    card_pack: 'fas fa-layer-group green',
    title: 'fas fa-crown orange',
    special_item: 'fas fa-scroll red'
  }
  return icons[type] || 'fas fa-gift'
}

const getRewardName = (type: string) => {
  const names: Record<string, string> = {
    gold: '金币',
    exp: '经验值',
    soulstone: '魂晶',
    crystal: '水晶',
    card_pack: '卡包',
    title: '称号',
    special_item: '特殊道具'
  }
  return names[type] || '奖励'
}

const getStatLabel = (key: string) => {
  const labels: Record<string, string> = {
    currentCards: '当前卡牌数',
    targetCards: '目标卡牌数'
  }
  return labels[key] || key
}

const achievementProgress = (achievement: Achievement) => {
  try {
    if (!achievement || achievement.progress === undefined) {
      const circumference = 2 * Math.PI * 16
      return `0 ${circumference}`
    }
    const circumference = 2 * Math.PI * 16
    const progress = (achievement.progress || 0) / 100
    return `${progress * circumference} ${circumference}`
  } catch (error) {
    console.error('计算成就进度时出错:', error)
    return '0 100.48'
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const openModal = (achievement: Achievement) => {
  try {
    selectedAchievement.value = achievement
  } catch (error) {
    console.error('打开成就详情模态框时出错:', error)
  }
}

const closeModal = () => {
  try {
    selectedAchievement.value = null
  } catch (error) {
    console.error('关闭成就详情模态框时出错:', error)
  }
}

// 根据分类获取图标
const getCategoryIcon = (category: string): string => {
  const iconMap: Record<string, string> = {
    progression: 'fas fa-chart-line',
    mastery: 'fas fa-star',
    collection: 'fas fa-layer-group',
    social: 'fas fa-users',
    combat: 'fas fa-sword',
    exploration: 'fas fa-dungeon',
    character: 'fas fa-user'
  }
  return iconMap[category] || 'fas fa-trophy'
}

// 根据分类和名称推断稀有度
const inferRarity = (category: string, name: string): 'common' | 'rare' | 'epic' | 'legendary' => {
  // 根据分类和名称关键词推断稀有度
  if (name.includes('传奇') || name.includes('传说') || name.includes('终极')) {
    return 'legendary'
  }
  if (name.includes('史诗') || name.includes('大师') || name.includes('完美')) {
    return 'epic'
  }
  if (name.includes('稀有') || name.includes('高级') || name.includes('专家')) {
    return 'rare'
  }
  // 根据分类推断
  if (category === 'mastery') {
    return 'epic'
  }
  if (category === 'collection') {
    return 'rare'
  }
  return 'common'
}

// 解析 requirements JSON 字符串
const parseRequirements = (requirementsStr: string | null | undefined): string[] => {
  if (!requirementsStr) return []
  try {
    const parsed = JSON.parse(requirementsStr)
    if (Array.isArray(parsed)) {
      return parsed
    }
    if (typeof parsed === 'object') {
      // 如果是对象，转换为数组
      return Object.values(parsed) as string[]
    }
    return [String(parsed)]
  } catch {
    // 如果不是 JSON，直接作为字符串返回
    return [requirementsStr]
  }
}

// 将后端数据转换为前端格式
const mapAchievementFromDTO = (dto: any): Achievement => {
  const requirements = parseRequirements(dto.requirements)
  const category = dto.category || 'progression'
  
  return {
    id: dto.id,
    name: dto.name,
    description: dto.description || '',
    icon: getCategoryIcon(category),
    rarity: inferRarity(category, dto.name),
    category: category,
    requirements: requirements,
    rewards: [], // 后端暂无奖励数据，留空
    unlocked: dto.isCompleted || false,
    progress: dto.progress || 0,
    featured: false
  }
}

// 加载成就数据
const loadAchievements = async () => {
  loading.value = true
  error.value = null
  try {
    console.log('[Achievements] 加载成就数据...')
    const response = await achievementApi.getAchievementProgress()
    
    if (response.data.code === 200) {
      const data = response.data.data || []
      achievements.value = data.map(mapAchievementFromDTO)
      console.log('[Achievements] 加载成功，共', achievements.value.length, '个成就')
    } else {
      throw new Error(response.data.message || '获取成就数据失败')
    }
  } catch (err) {
    console.error('[Achievements] 加载失败:', err)
    error.value = err instanceof Error ? err.message : '获取成就数据失败'
    // 如果获取用户成就失败，尝试获取所有成就列表
    try {
      const response = await achievementApi.getAchievements()
      if (response.data.code === 200) {
        const data = response.data.data || []
        achievements.value = data.map(mapAchievementFromDTO)
        console.log('[Achievements] 使用成就列表数据，共', achievements.value.length, '个成就')
      }
    } catch (fallbackErr) {
      console.error('[Achievements] 备用加载也失败:', fallbackErr)
    }
  } finally {
    loading.value = false
  }
}

onLoad(() => {
  loadAchievements()
})

onShow(() => {
  // 可按需刷新
})

function goHome() {
  uni.navigateTo({ url: '/pages/home/home' })
}
</script>

<style scoped>
.achievements-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0f 0%, #1a1a2e 50%, #0f1419 100%);
  color: #ffffff;
  padding: 0;
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 */
.achievements-page::before {
  content: '';
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(211, 47, 47, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 进度条区域 */
.progress-section {
  padding: 60rpx 60rpx 40rpx;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.4) 0%, transparent 100%);
}

.progress-bar-container {
  width: 100%;
  height: 20rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 10rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
  position: relative;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #d32f2f 0%, #ff5252 50%, #d32f2f 100%);
  background-size: 200% 100%;
  border-radius: 10rpx;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 20rpx rgba(211, 47, 47, 0.5);
  animation: progressShine 2s ease-in-out infinite;
}

@keyframes progressShine {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.progress-text {
  font-size: 32rpx;
  color: #ffffff;
  text-align: center;
  display: block;
  font-weight: 600;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
}

/* 标签页 - 固定定位 */
.tabs-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  gap: 0;
  padding: 0 60rpx;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.95) 0%, rgba(0, 0, 0, 0.85) 100%);
  border-bottom: 2rpx solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20rpx);
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.3);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 36rpx 0;
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.5);
  border-bottom: 4rpx solid transparent;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.tab-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, #d32f2f, transparent);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item.active {
  color: #ffffff;
  font-weight: 600;
  text-shadow: 0 0 20rpx rgba(211, 47, 47, 0.5);
}

.tab-item.active::after {
  width: 80%;
}

.tab-item:active {
  transform: scale(0.95);
}

.achievements-main {
  padding: 40rpx 60rpx;
  height: calc(100vh - 240rpx);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
  z-index: 1;
}

/* 成就列表 */
.achievements-list {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding: 36rpx;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10rpx);
}

.achievement-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.6s;
}

.achievement-item:active::before {
  left: 100%;
}

.achievement-item:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.2);
}

.achievement-item.unlocked {
  background: linear-gradient(135deg, rgba(211, 47, 47, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
  border-color: rgba(211, 47, 47, 0.3);
  box-shadow: 0 4rpx 20rpx rgba(211, 47, 47, 0.2), inset 0 0 40rpx rgba(211, 47, 47, 0.05);
}

.achievement-item.unlocked::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(211, 47, 47, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

/* 左侧角色图标 */
.achievement-character-icon {
  flex-shrink: 0;
  width: 140rpx;
  height: 140rpx;
  position: relative;
}

.character-icon-bg {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 72rpx;
  color: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.3), inset 0 0 20rpx rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.achievement-item.unlocked .character-icon-bg {
  background: linear-gradient(135deg, rgba(211, 47, 47, 0.3) 0%, rgba(255, 255, 255, 0.1) 100%);
  box-shadow: 0 4rpx 20rpx rgba(211, 47, 47, 0.4), inset 0 0 30rpx rgba(211, 47, 47, 0.2);
}

.achievement-item:hover .character-icon-bg {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 0 6rpx 30rpx rgba(0, 0, 0, 0.4);
}

/* 中间内容 */
.achievement-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.achievement-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.4;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
  letter-spacing: 0.5rpx;
  transition: all 0.3s;
}

.achievement-item.unlocked .achievement-title {
  color: #ffeb3b;
  text-shadow: 0 0 20rpx rgba(255, 235, 59, 0.5);
}

.achievement-description {
  font-size: 30rpx;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.6;
  letter-spacing: 0.5rpx;
}

/* 右侧完成状态 */
.achievement-status-badge {
  flex-shrink: 0;
}

.status-circle {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  border: 3rpx solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.3), inset 0 0 20rpx rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-circle.completed {
  background: linear-gradient(135deg, rgba(211, 47, 47, 0.4) 0%, rgba(255, 82, 82, 0.3) 100%);
  border-color: rgba(211, 47, 47, 0.6);
  box-shadow: 0 4rpx 30rpx rgba(211, 47, 47, 0.5), inset 0 0 40rpx rgba(211, 47, 47, 0.2);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4rpx 30rpx rgba(211, 47, 47, 0.5), inset 0 0 40rpx rgba(211, 47, 47, 0.2);
  }
  50% {
    box-shadow: 0 6rpx 40rpx rgba(211, 47, 47, 0.7), inset 0 0 50rpx rgba(211, 47, 47, 0.3);
  }
}

.status-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.6);
  text-align: center;
  line-height: 1.2;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.status-circle.completed .status-text {
  color: #ffffff;
  font-weight: 700;
  text-shadow: 0 0 20rpx rgba(255, 255, 255, 0.8);
}

/* 进度圆圈 */
.progress-circle-container {
  width: 140rpx;
  height: 140rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-circle {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  filter: drop-shadow(0 0 10rpx rgba(211, 47, 47, 0.5));
}

.progress-bg {
  stroke: rgba(255, 255, 255, 0.15);
  stroke-width: 3;
}

.progress-fill {
  stroke-width: 4;
  transition: stroke-dasharray 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  stroke-linecap: round;
  filter: drop-shadow(0 0 8rpx rgba(211, 47, 47, 0.8));
}

.progress-percent {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 700;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
  letter-spacing: 1rpx;
}

/* 为SVG添加渐变 */
.progress-circle-container {
  background: radial-gradient(circle, rgba(211, 47, 47, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.achievement-modal {
  background: linear-gradient(145deg, rgba(26, 26, 46, 0.95), rgba(15, 52, 96, 0.95));
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2rem;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  color: #ffffff;
  margin: 0;
  font-size: 1.5rem;
}

.close-modal {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.5rem;
  transition: all 0.3s;
}

.close-modal:hover {
  color: #ffffff;
}

.modal-content {
  display: flex;
  gap: 1.5rem;
}

.modal-icon {
  flex-shrink: 0;
}

.icon-bg.large {
  width: 100px;
  height: 100px;
  font-size: 2.5rem;
}

.modal-info {
  flex: 1;
}

.modal-info h3 {
  color: #ffffff;
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
}

.modal-info p {
  color: #9ca3af;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.modal-stats {
  background: rgba(255, 255, 255, 0.05);
  padding: 1rem;
  border-radius: 8px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-label {
  color: #9ca3af;
  font-size: 0.9rem;
}

.stat-value {
  color: #ffffff;
  font-weight: 600;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 加载状态 */
.loading-container,
.error-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 2rem;
}

.loading-spinner,
.error-message,
.empty-message {
  text-align: center;
  color: #9ca3af;
}

.loading-spinner i,
.error-message i,
.empty-message i {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #4caf50;
}

.error-message i {
  color: #f44336;
}

.empty-message i {
  color: #9ca3af;
}

.loading-spinner p,
.error-message p,
.empty-message p {
  font-size: 1.1rem;
  margin: 0.5rem 0;
}

.retry-btn {
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
  border-radius: 8px;
  color: #4ade80;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.retry-btn:hover {
  background: rgba(76, 175, 80, 0.3);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .progress-section {
    padding: 50rpx 40rpx 30rpx;
  }
  
  .tabs-nav {
    padding: 0 40rpx;
  }
  
  .tab-item {
    font-size: 28rpx;
    padding: 24rpx 0;
  }
  
  .achievements-main {
    padding: 30rpx 40rpx;
  }
  
  .achievement-item {
    padding: 24rpx;
    gap: 24rpx;
  }
  
  .achievement-character-icon {
    width: 100rpx;
    height: 100rpx;
  }
  
  .character-icon-bg {
    font-size: 52rpx;
  }
  
  .achievement-title {
    font-size: 36rpx;
  }
  
  .achievement-description {
    font-size: 26rpx;
  }
  
  .status-circle {
    width: 100rpx;
    height: 100rpx;
  }
  
  .status-text {
    font-size: 22rpx;
  }
  
  .progress-circle-container {
    width: 100rpx;
    height: 100rpx;
  }
  
  .progress-percent {
    font-size: 20rpx;
  }
  
  .achievements-main {
    height: calc(100vh - 280rpx);
  }
}
</style>
