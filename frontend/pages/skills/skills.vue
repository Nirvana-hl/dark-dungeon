<template>
  <view class="skills-page">
    <!-- 页面头部 -->
    <view class="skills-header">
      <view class="header-content">
        <text class="header-title">技能树</text>
        <text class="header-subtitle">解锁你的力量</text>
      </view>
      <view class="close-btn" @click="goBack">
        <text class="close-icon">✕</text>
      </view>
    </view>

    <!-- 角色信息卡片 -->
    <view v-if="playerCharacter" class="character-info-card">
      <view class="character-avatar-section">
        <image 
          class="character-avatar"
          src="/static/touxiang.png"
          mode="aspectFill"
          @error="handleAvatarError"
        ></image>
        <view class="level-badge">Lv.{{ playerCharacter.level || 1 }}</view>
      </view>
      <view class="character-info-section">
        <text class="character-name">{{ playerCharacter.playerCharacterName || '冒险者' }}</text>
        <text class="character-class">{{ getCharacterClassName(playerCharacter.playerCharacterCode) }}</text>
      </view>
    </view>

    <!-- 技能树容器 -->
    <scroll-view class="skills-container" scroll-y :scroll-top="scrollTop">
      <view class="skill-tree-wrapper">
        <!-- 技能节点 -->
        <view 
          v-for="skill in sortedSkills" 
          :key="skill.id"
          class="skill-node"
          :class="{
            'skill-unlocked': skill.isUnlocked,
            'skill-available': skill.canUnlock && !skill.isUnlocked,
            'skill-locked': !skill.canUnlock && !skill.isUnlocked
          }"
          :style="getSkillNodeStyle(skill)"
          @click="handleSkillClick(skill)"
        >
          <!-- 技能图标 -->
          <view class="skill-icon-wrapper">
            <view class="skill-icon" :class="getSkillIconClass(skill)"></view>
            <view v-if="skill.isUnlocked" class="unlock-badge">✓</view>
          </view>
          
          <!-- 技能名称 -->
          <text class="skill-name">{{ skill.name }}</text>
          
          <!-- 技能等级要求 -->
          <text v-if="skill.requiredLevel" class="skill-level-req">Lv.{{ skill.requiredLevel }}</text>
        </view>

        <!-- 连接线 -->
        <view 
          v-for="(line, index) in connectionLines" 
          :key="`line-${index}`"
          class="connection-line"
          :class="{ 'line-unlocked': line.fromUnlocked && line.toUnlocked }"
          :style="getLineStyle(line)"
        ></view>
      </view>
    </scroll-view>

    <!-- 技能详情弹窗 -->
    <view v-if="selectedSkill" class="skill-modal" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ selectedSkill.name }}</text>
          <view class="modal-close" @click="closeModal">✕</view>
        </view>
        <view class="modal-body">
          <text class="skill-description">{{ selectedSkill.description }}</text>
          <view v-if="selectedSkill.unlockPath && selectedSkill.unlockPath.length > 0" class="prerequisites">
            <text class="prerequisites-title">前置技能:</text>
            <text 
              v-for="(prereq, idx) in getPrerequisiteNames(selectedSkill)" 
              :key="idx"
              class="prerequisite-item"
            >
              {{ prereq }}{{ idx < getPrerequisiteNames(selectedSkill).length - 1 ? '、' : '' }}
            </text>
          </view>
          <view v-if="selectedSkill.requiredLevel" class="level-requirement">
            <text class="requirement-label">等级要求:</text>
            <text class="requirement-value">Lv.{{ selectedSkill.requiredLevel }}</text>
          </view>
          <view v-if="!selectedSkill.isUnlocked && selectedSkill.canUnlock" class="unlock-action">
            <button class="unlock-btn" @click="unlockSkill(selectedSkill)">解锁技能</button>
          </view>
          <view v-else-if="selectedSkill.isUnlocked" class="unlocked-status">
            <text class="unlocked-text">✓ 已解锁</text>
          </view>
          <view v-else class="locked-status">
            <text class="locked-text">条件未满足</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { skillApi } from '@/api/request'
import { useCampStore } from '@/stores/camp'
import type { Skill } from '@/types'

// uni-app 全局对象类型声明
declare const uni: {
  navigateBack: () => void
  showToast: (options: { title: string; icon?: string; duration?: number }) => void
}

// 状态
const loading = ref(false)
const skills = ref<Skill[]>([])
const selectedSkill = ref<Skill | null>(null)
const scrollTop = ref(0)

// Store
const campStore = useCampStore()

// 角色信息
const playerCharacter = computed(() => campStore.playerCharacter)

// 计算属性：按行和列排序技能（从下到上）
const sortedSkills = computed(() => {
  return [...skills.value].sort((a, b) => {
    // 先按行排序（row越大越靠下，所以从下到上显示，row=1在底部，row=5在顶部）
    if (b.positionInTree.row !== a.positionInTree.row) {
      return b.positionInTree.row - a.positionInTree.row
    }
    // 同行的按列排序（从左到右）
    return a.positionInTree.column - b.positionInTree.column
  })
})

// 计算属性：连接线
const connectionLines = computed(() => {
  const lines: Array<{
    from: { row: number; column: number }
    to: { row: number; column: number }
    fromUnlocked: boolean
    toUnlocked: boolean
  }> = []

  skills.value.forEach(skill => {
    if (skill.unlockPath && skill.unlockPath.length > 0) {
      // 查找前置技能
      skill.unlockPath.forEach(prereqCode => {
        // 尝试通过code匹配（可能是技能代码或技能ID）
        let prereqSkill = skills.value.find(s => s.code === prereqCode || String(s.id) === String(prereqCode))
        
        // 如果找不到，尝试通过名称匹配（处理可能的命名不一致）
        if (!prereqSkill && typeof prereqCode === 'string') {
          // 尝试匹配技能名称的一部分
          prereqSkill = skills.value.find(s => 
            s.name && prereqCode.toLowerCase().includes(s.name.toLowerCase().replace(/\s+/g, '_'))
          )
        }
        
        if (prereqSkill) {
          lines.push({
            from: prereqSkill.positionInTree,
            to: skill.positionInTree,
            fromUnlocked: prereqSkill.isUnlocked || false,
            toUnlocked: skill.isUnlocked || false
          })
        }
      })
    }
  })

  return lines
})

// 获取技能节点样式
function getSkillNodeStyle(skill: Skill) {
  const baseSize = 70 // 基础节点大小（rpx）- 缩小
  const spacing = 100 // 节点间距（rpx）- 缩小
  const startX = 30 // 起始X位置（rpx）- 缩小边距
  const startY = 50 // 起始Y位置（rpx）- 缩小边距

  // 计算位置（从下到上，row越大越靠下）
  // row=1在底部，row=5在顶部，所以需要反转
  const maxRow = Math.max(...skills.value.map(s => s.positionInTree.row))
  const minRow = Math.min(...skills.value.map(s => s.positionInTree.row))
  // 计算相对位置：row越大，top值越大（越靠下）
  // 但我们要从底部开始，所以需要反转
  const rowIndex = skill.positionInTree.row - minRow // 0-based row index
  const column = skill.positionInTree.column

  const left = startX + column * spacing
  // 从底部开始，row=1在最底部，row=5在最顶部
  const top = startY + rowIndex * spacing

  return {
    left: `${left}rpx`,
    top: `${top}rpx`,
    width: `${baseSize}rpx`,
    height: `${baseSize}rpx`
  }
}

// 获取连接线样式
function getLineStyle(line: {
  from: { row: number; column: number }
  to: { row: number; column: number }
  fromUnlocked: boolean
  toUnlocked: boolean
}) {
  const baseSize = 70
  const spacing = 100
  const startX = 30
  const startY = 50

  const maxRow = Math.max(...skills.value.map(s => s.positionInTree.row))
  const minRow = Math.min(...skills.value.map(s => s.positionInTree.row))

  // 计算起始和结束位置（从底部开始）
  const fromRowIndex = line.from.row - minRow
  const toRowIndex = line.to.row - minRow
  const fromCol = line.from.column
  const toCol = line.to.column

  const fromX = startX + fromCol * spacing + baseSize / 2
  const fromY = startY + fromRowIndex * spacing + baseSize / 2
  const toX = startX + toCol * spacing + baseSize / 2
  const toY = startY + toRowIndex * spacing + baseSize / 2

  // 计算线的长度和角度
  const dx = toX - fromX
  const dy = toY - fromY
  const length = Math.sqrt(dx * dx + dy * dy)
  const angle = Math.atan2(dy, dx) * (180 / Math.PI)

  // 判断是否已解锁（两端都解锁才显示亮色）
  const isUnlocked = line.fromUnlocked && line.toUnlocked

  return {
    left: `${fromX}rpx`,
    top: `${fromY}rpx`,
    width: `${length}rpx`,
    transform: `rotate(${angle}deg)`,
    transformOrigin: '0 50%'
  }
}

// 获取技能图标类名
function getSkillIconClass(skill: Skill) {
  if (skill.isUnlocked) {
    return 'icon-unlocked'
  } else if (skill.canUnlock) {
    return 'icon-available'
  } else {
    return 'icon-locked'
  }
}

// 获取前置技能名称
function getPrerequisiteNames(skill: Skill): string[] {
  if (!skill.unlockPath || skill.unlockPath.length === 0) {
    return []
  }
  return skill.unlockPath
    .map(code => {
      // 尝试通过code或id匹配
      let prereq = skills.value.find(s => s.code === code || String(s.id) === String(code))
      
      // 如果找不到，尝试通过名称匹配
      if (!prereq && typeof code === 'string') {
        prereq = skills.value.find(s => 
          s.name && code.toLowerCase().includes(s.name.toLowerCase().replace(/\s+/g, '_'))
        )
      }
      
      return prereq ? prereq.name : code
    })
    .filter(Boolean)
}

// 处理技能点击
function handleSkillClick(skill: Skill) {
  selectedSkill.value = skill
}

// 关闭弹窗
function closeModal() {
  selectedSkill.value = null
}

// 解锁技能
async function unlockSkill(skill: Skill) {
  if (!skill.canUnlock || skill.isUnlocked) {
    return
  }

  try {
    loading.value = true
    const response = await skillApi.unlockSkill(String(skill.id))
    
    if (response.data.code === 200) {
      // 更新技能状态
      const index = skills.value.findIndex(s => s.id === skill.id)
      if (index !== -1) {
        skills.value[index].isUnlocked = true
        skills.value[index].canUnlock = true
      }
      
      uni.showToast({
        title: '技能解锁成功',
        icon: 'success',
        duration: 2000
      })
      
      closeModal()
    } else {
      throw new Error(response.data.message || '解锁失败')
    }
  } catch (error: any) {
    console.error('解锁技能失败:', error)
    uni.showToast({
      title: error.message || '解锁失败',
      icon: 'none',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

// 返回
function goBack() {
  uni.navigateBack()
}

// 获取角色职业名称
function getCharacterClassName(code: string): string {
  const classMap: Record<string, string> = {
    'warden': '守望者',
    'occultist': '秘术师',
    'ranger': '游侠',
    'warrior': '战士'
  }
  return classMap[code] || '未知职业'
}

// 处理头像加载错误
function handleAvatarError() {
  // 可以设置默认头像
  console.log('头像加载失败')
}

// 加载技能树
async function loadSkillTree() {
  try {
    loading.value = true
    
    // 获取当前用户的主角职业代码
    const playerCharacter = campStore.playerCharacter
    if (!playerCharacter || !playerCharacter.playerCharacterCode) {
      uni.showToast({
        title: '未找到角色信息',
        icon: 'none',
        duration: 2000
      })
      return
    }

    const playerCharacterCode = playerCharacter.playerCharacterCode
    
    // 调用API获取技能树
    const response = await skillApi.getSkillTree(playerCharacterCode)
    
    if (response.data.code === 200 && response.data.data) {
      skills.value = response.data.data.map((skill: any) => ({
        ...skill,
        positionInTree: typeof skill.positionInTree === 'string' 
          ? JSON.parse(skill.positionInTree) 
          : skill.positionInTree,
        unlockPath: skill.unlockPath 
          ? (typeof skill.unlockPath === 'string' ? JSON.parse(skill.unlockPath) : skill.unlockPath)
          : []
      }))
    } else {
      throw new Error(response.data.message || '获取技能树失败')
    }
  } catch (error: any) {
    console.error('加载技能树失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  // 确保营地数据已加载
  if (!campStore.playerCharacter) {
    campStore.fetchCampData().then(() => {
      loadSkillTree()
    })
  } else {
    loadSkillTree()
  }
})
</script>

<style scoped lang="scss">
.skills-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(180deg, #0a0a0f 0%, #1a1a2e 50%, #0a0a0f 100%);
  position: relative;
  overflow: hidden;
}

// 页面头部
.skills-header {
  position: relative;
  padding: 40rpx 30rpx 30rpx;
  background: linear-gradient(180deg, rgba(139, 0, 0, 0.3) 0%, transparent 100%);
  border-bottom: 2rpx solid rgba(200, 0, 0, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}

.header-content {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 20rpx rgba(255, 215, 0, 0.8), 0 0 40rpx rgba(255, 215, 0, 0.4);
  letter-spacing: 4rpx;
}

.header-subtitle {
  font-size: 24rpx;
  color: #cc9966;
  margin-top: 8rpx;
  text-shadow: 0 0 10rpx rgba(204, 153, 102, 0.5);
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(139, 0, 0, 0.5);
  border: 2rpx solid rgba(200, 0, 0, 0.6);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
}

.close-btn:active {
  background: rgba(200, 0, 0, 0.7);
  transform: scale(0.9);
}

.close-icon {
  font-size: 36rpx;
  color: #ffd700;
  font-weight: bold;
}

// 角色信息卡片
.character-info-card {
  margin: 20rpx 30rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, rgba(139, 0, 0, 0.2) 0%, rgba(26, 26, 46, 0.8) 100%);
  border: 2rpx solid rgba(200, 0, 0, 0.4);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 0 20rpx rgba(255, 215, 0, 0.2);
}

.character-avatar-section {
  position: relative;
  margin-right: 20rpx;
}

.character-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 3rpx solid #ffd700;
  box-shadow: 0 0 15rpx rgba(255, 215, 0, 0.6);
  background: rgba(0, 0, 0, 0.5);
}

.level-badge {
  position: absolute;
  bottom: -8rpx;
  right: -8rpx;
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #8b0000 0%, #cc0000 100%);
  border: 2rpx solid #ffd700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #ffd700;
  font-weight: bold;
  box-shadow: 0 0 10rpx rgba(255, 215, 0, 0.5);
}

.character-info-section {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.character-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 10rpx rgba(255, 215, 0, 0.6);
  margin-bottom: 8rpx;
}

.character-class {
  font-size: 24rpx;
  color: #cc9966;
  text-shadow: 0 0 8rpx rgba(204, 153, 102, 0.4);
}

// 技能树容器
.skills-container {
  width: 100%;
  height: calc(100vh - 320rpx);
  position: relative;
  overflow-y: auto;
}

.skill-tree-wrapper {
  position: relative;
  width: 100%;
  min-height: 100%;
  padding: 30rpx 0;
  transform: scale(0.85);
  transform-origin: top center;
}

// 技能节点
.skill-node {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 5;
  flex-shrink: 0; // 防止被压缩
}

.skill-icon-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skill-icon {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  }
}

// 已解锁技能 - 亮色
.skill-unlocked .skill-icon {
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border: 4rpx solid #ffd700;
  box-shadow: 
    0 0 30rpx rgba(255, 215, 0, 0.8),
    0 0 60rpx rgba(255, 215, 0, 0.4),
    inset 0 0 20rpx rgba(255, 255, 255, 0.3);
  color: #1a1a2e;
  animation: glow-pulse 2s ease-in-out infinite;
}

// 可解锁技能 - 中等亮度
.skill-available .skill-icon {
  background: linear-gradient(135deg, #8b4513 0%, #654321 100%);
  border: 4rpx solid #cc9966;
  box-shadow: 
    0 0 20rpx rgba(204, 153, 102, 0.6),
    0 0 40rpx rgba(204, 153, 102, 0.3);
  color: #ffd700;
  animation: pulse 2s ease-in-out infinite;
}

// 锁定技能 - 暗色
.skill-locked .skill-icon {
  background: linear-gradient(135deg, #2a2a3e 0%, #1a1a2e 100%);
  border: 4rpx solid #4a4a5e;
  box-shadow: inset 0 0 20rpx rgba(0, 0, 0, 0.5);
  color: #666;
  opacity: 0.5;
}

.skill-node:active {
  transform: scale(0.95);
}

.unlock-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 32rpx;
  height: 32rpx;
  background: #00ff00;
  border: 2rpx solid #00cc00;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #1a1a2e;
  font-weight: bold;
  box-shadow: 0 0 15rpx rgba(0, 255, 0, 0.8);
}

.skill-name {
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #ffd700;
  text-align: center;
  font-weight: bold;
  text-shadow: 0 0 10rpx rgba(255, 215, 0, 0.6);
  white-space: nowrap;
  max-width: 100rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}

.skill-locked .skill-name {
  color: #666;
  text-shadow: none;
}

.skill-level-req {
  margin-top: 4rpx;
  font-size: 18rpx;
  color: #cc9966;
  text-align: center;
  line-height: 1.2;
}

// 连接线
.connection-line {
  position: absolute;
  height: 4rpx;
  background: linear-gradient(90deg, 
    rgba(204, 153, 102, 0.3) 0%, 
    rgba(204, 153, 102, 0.6) 50%, 
    rgba(204, 153, 102, 0.3) 100%);
  z-index: 1;
  transform-origin: 0 50%;
  box-shadow: 0 0 10rpx rgba(204, 153, 102, 0.3);
  
  // 已解锁的连接线更亮
  &.line-unlocked {
    background: linear-gradient(90deg, 
      rgba(255, 215, 0, 0.4) 0%, 
      rgba(255, 215, 0, 0.8) 50%, 
      rgba(255, 215, 0, 0.4) 100%);
    box-shadow: 0 0 15rpx rgba(255, 215, 0, 0.5);
  }
}

// 技能详情弹窗
.skill-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s;
}

.modal-content {
  width: 80%;
  max-width: 600rpx;
  background: linear-gradient(180deg, #1a1a2e 0%, #0a0a0f 100%);
  border: 4rpx solid #8b0000;
  border-radius: 20rpx;
  box-shadow: 
    0 0 40rpx rgba(139, 0, 0, 0.6),
    inset 0 0 30rpx rgba(255, 215, 0, 0.1);
  overflow: hidden;
  animation: slideUp 0.3s;
}

.modal-header {
  padding: 30rpx;
  background: linear-gradient(180deg, rgba(139, 0, 0, 0.5) 0%, transparent 100%);
  border-bottom: 2rpx solid rgba(200, 0, 0, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 15rpx rgba(255, 215, 0, 0.8);
}

.modal-close {
  width: 50rpx;
  height: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffd700;
  font-size: 32rpx;
  font-weight: bold;
  cursor: pointer;
}

.modal-body {
  padding: 30rpx;
}

.skill-description {
  font-size: 28rpx;
  color: #cc9966;
  line-height: 1.6;
  margin-bottom: 30rpx;
  text-shadow: 0 0 10rpx rgba(204, 153, 102, 0.3);
}

.prerequisites {
  margin-bottom: 20rpx;
}

.prerequisites-title {
  font-size: 24rpx;
  color: #ffd700;
  margin-right: 10rpx;
}

.prerequisite-item {
  font-size: 24rpx;
  color: #cc9966;
}

.level-requirement {
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
}

.requirement-label {
  font-size: 24rpx;
  color: #ffd700;
  margin-right: 10rpx;
}

.requirement-value {
  font-size: 24rpx;
  color: #cc9966;
}

.unlock-action {
  margin-top: 30rpx;
}

.unlock-btn {
  width: 100%;
  padding: 24rpx;
  background: linear-gradient(135deg, #8b0000 0%, #cc0000 100%);
  border: 2rpx solid #ffd700;
  border-radius: 10rpx;
  color: #ffd700;
  font-size: 28rpx;
  font-weight: bold;
  box-shadow: 0 0 20rpx rgba(255, 215, 0, 0.4);
  transition: all 0.3s;
}

.unlock-btn:active {
  transform: scale(0.95);
  box-shadow: 0 0 30rpx rgba(255, 215, 0, 0.6);
}

.unlocked-status {
  margin-top: 30rpx;
  text-align: center;
}

.unlocked-text {
  font-size: 28rpx;
  color: #00ff00;
  text-shadow: 0 0 15rpx rgba(0, 255, 0, 0.8);
}

.locked-status {
  margin-top: 30rpx;
  text-align: center;
}

.locked-text {
  font-size: 28rpx;
  color: #666;
}

// 加载提示
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.loading-text {
  font-size: 32rpx;
  color: #ffd700;
  text-shadow: 0 0 15rpx rgba(255, 215, 0, 0.8);
}

// 动画
@keyframes glow-pulse {
  0%, 100% {
    box-shadow: 
      0 0 30rpx rgba(255, 215, 0, 0.8),
      0 0 60rpx rgba(255, 215, 0, 0.4),
      inset 0 0 20rpx rgba(255, 255, 255, 0.3);
  }
  50% {
    box-shadow: 
      0 0 40rpx rgba(255, 215, 0, 1),
      0 0 80rpx rgba(255, 215, 0, 0.6),
      inset 0 0 30rpx rgba(255, 255, 255, 0.4);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 
      0 0 20rpx rgba(204, 153, 102, 0.6),
      0 0 40rpx rgba(204, 153, 102, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 
      0 0 30rpx rgba(204, 153, 102, 0.8),
      0 0 60rpx rgba(204, 153, 102, 0.5);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>

