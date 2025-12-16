<template>
  <view class="achievements-page">
    <!-- 返回首页按钮 -->
    <view class="back-to-home" @click="goHome">
      <text class="home-icon">🏠</text>
      <text class="home-text">返回首页</text>
    </view>
    
    <!-- 页面头部 -->
    <header class="page-header">
      <view class="header-content">
        <view class="title-section">
          <h1 class="page-title">
            <i class="fas fa-trophy"></i>
            成就系统
          </h1>
          <p class="page-subtitle">追踪你的游戏成就和里程碑</p>
        </view>
        <view class="stats-summary">
          <view class="summary-item">
            <text class="summary-number">{{ unlockedCount }}</text>
            <text class="summary-label">已解锁</text>
          </view>
          <view class="summary-item">
            <text class="summary-number">{{ totalAchievements }}</text>
            <text class="summary-label">总成就</text>
          </view>
          <view class="summary-item">
            <text class="summary-number">{{ completionPercentage }}%</text>
            <text class="summary-label">完成度</text>
          </view>
        </view>
      </view>
    </header>

    <!-- 成就分类筛选 -->
    <nav class="achievement-categories">
      <button 
        v-for="category in categories" 
        :key="category.id"
        :class="['category-btn', { active: selectedCategory === category.id }]"
        @click="selectedCategory = category.id"
      >
        <i :class="category.icon"></i>
        <text>{{ category.name }}</text>
        <text class="category-count">{{ getCategoryCount(category.id) }}</text>
      </button>
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
      <view v-else class="achievements-grid">
        <view 
          v-for="(achievement, index) in filteredAchievements" 
          :key="achievement.id"
          class="achievement-card"
          :class="{ 
            unlocked: achievement.unlocked,
            featured: achievement.featured,
            [achievement.rarity]: true
          }"
          :style="{ '--delay': (index * 0.05) + 's' }"
          @click="openModal(achievement)"
        >
          <view class="achievement-icon">
            <view class="icon-bg" :class="achievement.rarity">
              <i :class="achievement.icon"></i>
            </view>
            <view v-if="achievement.unlocked" class="unlock-indicator">
              <i class="fas fa-check"></i>
            </view>
            <view v-if="!achievement.unlocked && achievement.progress" class="progress-ring">
              <svg class="progress-svg" viewBox="0 0 36 36">
                <circle
                  class="progress-bg"
                  cx="18" cy="18" r="16"
                  fill="none"
                  stroke="rgba(255,255,255,0.1)"
                  stroke-width="2"
                />
                <circle
                  class="progress-fill"
                  cx="18" cy="18" r="16"
                  fill="none"
                  :stroke-dasharray="achievementProgress(achievement)"
                  stroke-dashoffset="0"
                  stroke-linecap="round"
                  transform="rotate(-90 18 18)"
                />
              </svg>
              <text class="progress-text">{{ Math.round(achievement.progress) }}%</text>
            </view>
          </view>
          
          <view class="achievement-content">
            <view class="achievement-header">
              <h3 class="achievement-title">{{ achievement.name }}</h3>
              <view class="achievement-rarity" :class="achievement.rarity">
                {{ getRarityLabel(achievement.rarity) }}
              </view>
            </view>
            
            <p class="achievement-description">{{ achievement.description }}</p>
            
            <view class="achievement-requirements">
              <h4 v-if="achievement.requirements">解锁条件</h4>
              <ul v-if="achievement.requirements">
                <li v-for="req in achievement.requirements" :key="req">
                  {{ req }}
                </li>
              </ul>
            </view>
            
            <view class="achievement-rewards" v-if="achievement.rewards && achievement.rewards.length > 0">
              <h4>奖励</h4>
              <view class="reward-items">
                <view 
                  v-for="reward in achievement.rewards" 
                  :key="reward.type"
                  class="reward-item"
                  :class="reward.type"
                >
                  <i :class="getRewardIcon(reward.type)"></i>
                  <text>{{ reward.value }} {{ getRewardName(reward.type) }}</text>
                </view>
              </view>
            </view>
            
            <view class="achievement-footer">
              <view class="achievement-date" v-if="achievement.unlocked && achievement.unlockedAt">
                <i class="fas fa-calendar"></i>
                {{ formatDate(achievement.unlockedAt) }}
              </view>
              <view class="achievement-status" :class="{ unlocked: achievement.unlocked }">
                <i :class="achievement.unlocked ? 'fas fa-trophy' : 'fas fa-lock'"></i>
                <text>{{ achievement.unlocked ? '已解锁' : '未解锁' }}</text>
              </view>
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

// 分类数据（匹配后端分类）
const categories = ref([
  { id: 'all', name: '全部', icon: 'fas fa-th' },
  { id: 'progression', name: '进度', icon: 'fas fa-chart-line' },
  { id: 'mastery', name: '精通', icon: 'fas fa-star' },
  { id: 'collection', name: '收集', icon: 'fas fa-layer-group' },
  { id: 'social', name: '社交', icon: 'fas fa-users' }
])

// 状态
const selectedCategory = ref('all')
const selectedAchievement = ref<Achievement | null>(null)

// 计算属性
const filteredAchievements = computed(() => {
  if (selectedCategory.value === 'all') {
    return achievements.value
  }
  return achievements.value.filter(achievement => 
    achievement.category === selectedCategory.value
  )
})

const unlockedCount = computed(() => {
  return achievements.value.filter(a => a.unlocked).length
})

const totalAchievements = computed(() => {
  return achievements.value.length
})

const completionPercentage = computed(() => {
  if (totalAchievements.value === 0) return 0
  return Math.round((unlockedCount.value / totalAchievements.value) * 100)
})

// 方法
const getCategoryCount = (categoryId: string) => {
  if (categoryId === 'all') return totalAchievements.value
  return achievements.value.filter(a => a.category === categoryId).length
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: #e8e8e8;
  padding: 40rpx;
  position: relative;
}

/* 返回首页按钮 */
.back-to-home {
  position: fixed;
  top: 40rpx;
  left: 40rpx;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 40rpx;
  background: rgba(255, 255, 255, 0.08);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  border-radius: 50rpx;
  color: white;
  text-decoration: none;
  font-size: 28rpx;
  font-weight: 500;
}

.back-to-home i {
  font-size: 32rpx;
}

.page-header {
  margin-bottom: 64rpx;
}

.header-content {
  max-width: 2400rpx;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 64rpx;
}

.title-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.page-title {
  font-size: 64rpx;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.page-subtitle {
  color: #9ca3af;
  font-size: 32rpx;
  margin: 0;
}

.stats-summary {
  display: flex;
  gap: 64rpx;
}

.summary-item {
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 32rpx 48rpx;
  border-radius: 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.summary-number {
  display: block;
  font-size: 58rpx;
  font-weight: bold;
  color: #ffd700;
  margin-bottom: 8rpx;
}

.summary-label {
  font-size: 26rpx;
  color: #9ca3af;
}

.achievement-categories {
  display: flex;
  gap: 24rpx;
  margin-bottom: 64rpx;
  flex-wrap: wrap;
}

.category-btn {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: rgba(255, 255, 255, 0.05);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 50rpx;
  color: #9ca3af;
  font-size: 28rpx;
}

.category-btn.active {
  background: rgba(76, 175, 80, 0.2);
  border-color: rgba(76, 175, 80, 0.4);
  color: #4ade80;
}

.category-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
  min-width: 40rpx;
  text-align: center;
}

.achievements-main {
  max-width: 2400rpx;
  margin: 0 auto;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(600rpx, 1fr));
  gap: 48rpx;
}

.achievement-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 32rpx;
  padding: 48rpx;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.achievement-card.unlocked {
  border-color: rgba(76, 175, 80, 0.4);
  background: linear-gradient(145deg, rgba(76, 175, 80, 0.1), rgba(255, 255, 255, 0.05));
}

.achievement-card.featured {
  border-color: rgba(255, 152, 0, 0.4);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.1), rgba(255, 255, 255, 0.05));
}

.achievement-icon {
  position: relative;
  margin-bottom: 32rpx;
  display: flex;
  justify-content: center;
}

.icon-bg {
  width: 160rpx;
  height: 160rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.1);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
  position: relative;
}

.icon-bg.common { border-color: #9e9e9e; }
.icon-bg.rare { border-color: #2196f3; }
.icon-bg.epic { border-color: #9c27b0; }
.icon-bg.legendary { border-color: #ff9800; }

.unlock-indicator {
  position: absolute;
  top: -16rpx;
  right: -16rpx;
  width: 48rpx;
  height: 48rpx;
  background: #4caf50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 26rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.progress-ring {
  position: absolute;
  top: -16rpx;
  right: -16rpx;
  width: 72rpx;
  height: 72rpx;
}

.progress-svg {
  width: 100%;
  height: 100%;
}

.progress-bg {
  stroke-dasharray: 100;
  stroke-dashoffset: 0;
}

.progress-fill {
  stroke: #4caf50;
  stroke-width: 3;
  transition: stroke-dashoffset 0.5s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.5rem;
  font-weight: bold;
  color: white;
}

.achievement-content {
  text-align: center;
}

.achievement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  gap: 0.5rem;
}

.achievement-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
}

.achievement-rarity {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
}

.achievement-rarity.common { background: rgba(158, 158, 158, 0.2); color: #9e9e9e; }
.achievement-rarity.rare { background: rgba(33, 150, 243, 0.2); color: #2196f3; }
.achievement-rarity.epic { background: rgba(156, 39, 176, 0.2); color: #9c27b0; }
.achievement-rarity.legendary { background: rgba(255, 152, 0, 0.2); color: #ff9800; }

.achievement-description {
  color: #9ca3af;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1rem;
}

.achievement-requirements {
  margin-bottom: 1rem;
}

.achievement-requirements h4 {
  font-size: 0.8rem;
  font-weight: 600;
  color: #e8e8e8;
  margin: 0 0 0.5rem 0;
}

.achievement-requirements ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.achievement-requirements li {
  font-size: 0.8rem;
  color: #9ca3af;
  padding: 0.25rem 0;
  position: relative;
  padding-left: 1rem;
}

.achievement-requirements li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #4caf50;
}

.achievement-rewards h4 {
  font-size: 0.8rem;
  font-weight: 600;
  color: #ffd700;
  margin: 0 0 0.5rem 0;
}

.reward-items {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.7rem;
  background: rgba(255, 215, 0, 0.1);
}

.reward-item.gold { color: #ffd700; }
.reward-item.exp { color: #2196f3; }
.reward-item.soulstone { color: #9c27b0; }
.reward-item.crystal { color: #00bcd4; }

.achievement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  gap: 0.5rem;
}

.achievement-date {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #9ca3af;
}

.achievement-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  background: rgba(244, 67, 54, 0.2);
  color: #f87171;
}

.achievement-status.unlocked {
  background: rgba(76, 175, 80, 0.2);
  color: #4ade80;
}

.gold { color: #ffd700; }
.blue { color: #2196f3; }
.purple { color: #9c27b0; }
.cyan { color: #00bcd4; }

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
  .achievements-page {
    padding: 1rem;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .stats-summary {
    justify-content: center;
    gap: 1rem;
  }
  
  .summary-item {
    padding: 0.75rem 1rem;
  }
  
  .achievement-categories {
    justify-content: center;
  }
  
  .achievements-grid {
    grid-template-columns: 1fr;
  }
  
  .achievement-footer {
    flex-direction: column;
    gap: 0.75rem;
  }
}
</style>