<template>
  <view class="skills-panel">
    <!-- 面板头部 -->
    <view class="skills-header">
      <!-- 左侧：职业和头像 -->
      <view v-if="playerCharacter" class="header-character-info">
        <image 
          class="character-avatar"
          src="/static/touxiang.png"
          mode="aspectFill"
          @error="handleAvatarError"
        ></image>
        <view class="character-details">
          <text class="character-name">{{ playerCharacter.playerCharacterName || '冒险者' }}</text>
          <text class="character-class">{{ getCharacterClassName(playerCharacter.playerCharacterCode) }}</text>
        </view>
      </view>
      <view v-else class="header-character-info-placeholder"></view>
      
      <!-- 中间：标题（居中） -->
      <view class="header-title-center">
        <text class="header-title">技能树</text>
        <text class="header-subtitle">解锁你的力量</text>
      </view>
      
      <!-- 右侧：关闭按钮 -->
      <view class="close-btn" @click="handleClose">
        <text class="close-icon">✕</text>
      </view>
    </view>

    <!-- 技能树容器 -->
    <scroll-view class="skills-container" scroll-y :scroll-top="scrollTop">
      <!-- 主动技能区域 -->
      <view class="skill-category-section">
        <view class="category-header">
          <text class="category-title">主动技能</text>
        </view>
        <view class="skill-tree-wrapper" :style="{ minHeight: activeSkillTreeMinHeight }">
          <!-- 每行的等级标签（主动技能） -->
          <view 
            v-for="[row, level] in activeSkillRowLevels" 
            :key="`active-level-${row}`"
            class="row-level-label"
            :style="getRowLevelStyle(row, activeSkills)"
          >
            <text class="level-text">Lv.{{ level }}</text>
          </view>
          
          <!-- 主动技能节点 -->
          <view 
            v-for="skill in sortedActiveSkills" 
            :key="skill.id"
            class="skill-node"
            :class="{
              'skill-unlocked': skill.isUnlocked,
              'skill-available': skill.canUnlock && !skill.isUnlocked,
              'skill-locked': !skill.canUnlock && !skill.isUnlocked
            }"
            :style="getSkillNodeStyle(skill, activeSkills)"
            @click="handleSkillClick(skill)"
          >
            <!-- 图标和名称区域 -->
            <view class="skill-content">
              <!-- 技能图标 -->
              <view class="skill-icon-wrapper">
                <view class="skill-icon" :class="getSkillIconClass(skill)"></view>
                <view v-if="skill.isUnlocked" class="unlock-badge">✓</view>
              </view>
              
              <!-- 技能名称 -->
              <text class="skill-name">{{ skill.name }}</text>
            </view>
          </view>

          <!-- 主动技能连接线 -->
          <view 
            v-for="(line, index) in activeConnectionLines" 
            :key="`active-line-${index}`"
            class="connection-line"
            :class="{ 'line-unlocked': line.fromUnlocked && line.toUnlocked }"
            :style="getLineStyle(line, activeSkills)"
          ></view>
        </view>
      </view>

      <!-- 被动技能区域 -->
      <view class="skill-category-section">
        <view class="category-header">
          <text class="category-title">被动技能</text>
        </view>
        <view class="skill-tree-wrapper" :style="{ minHeight: passiveSkillTreeMinHeight }">
          <!-- 每行的等级标签（被动技能） -->
          <view 
            v-for="[row, level] in passiveSkillRowLevels" 
            :key="`passive-level-${row}`"
            class="row-level-label"
            :style="getRowLevelStyle(row, passiveSkills)"
          >
            <text class="level-text">Lv.{{ level }}</text>
          </view>
          
          <!-- 被动技能节点 -->
          <view 
            v-for="skill in sortedPassiveSkills" 
            :key="skill.id"
            class="skill-node"
            :class="{
              'skill-unlocked': skill.isUnlocked,
              'skill-available': skill.canUnlock && !skill.isUnlocked,
              'skill-locked': !skill.canUnlock && !skill.isUnlocked
            }"
            :style="getSkillNodeStyle(skill, passiveSkills)"
            @click="handleSkillClick(skill)"
          >
            <!-- 图标和名称区域 -->
            <view class="skill-content">
              <!-- 技能图标 -->
              <view class="skill-icon-wrapper">
                <view class="skill-icon" :class="getSkillIconClass(skill)"></view>
                <view v-if="skill.isUnlocked" class="unlock-badge">✓</view>
              </view>
              
              <!-- 技能名称 -->
              <text class="skill-name">{{ skill.name }}</text>
            </view>
          </view>

          <!-- 被动技能连接线 -->
          <view 
            v-for="(line, index) in passiveConnectionLines" 
            :key="`passive-line-${index}`"
            class="connection-line"
            :class="{ 'line-unlocked': line.fromUnlocked && line.toUnlocked }"
            :style="getLineStyle(line, passiveSkills)"
          ></view>
        </view>
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
          <!-- 技能类型标签 -->
          <view class="skill-type-badge" :class="isPassiveSkill(selectedSkill) ? 'type-passive' : 'type-active'">
            <text class="skill-type-text">{{ isPassiveSkill(selectedSkill) ? '被动技能' : '主动技能' }}</text>
          </view>
          
          <text class="skill-description">{{ selectedSkill.description }}</text>
          
          <!-- 被动技能属性提升显示 -->
          <view v-if="isPassiveSkill(selectedSkill)" class="passive-effects">
            <text class="effects-title">属性提升:</text>
            <view class="effects-list">
              <view v-if="getPassiveEffect(selectedSkill, 'attack_bonus')" class="effect-item">
                <text class="effect-label">攻击力:</text>
                <text class="effect-value">+{{ formatEffectValue(getPassiveEffect(selectedSkill, 'attack_bonus')) }}</text>
              </view>
              <view v-if="getPassiveEffect(selectedSkill, 'max_mana_bonus')" class="effect-item">
                <text class="effect-label">法力值上限:</text>
                <text class="effect-value">+{{ formatEffectValue(getPassiveEffect(selectedSkill, 'max_mana_bonus')) }}</text>
              </view>
              <view v-if="getPassiveEffect(selectedSkill, 'hp_bonus')" class="effect-item">
                <text class="effect-label">生命值上限:</text>
                <text class="effect-value">+{{ formatEffectValue(getPassiveEffect(selectedSkill, 'hp_bonus')) }}</text>
              </view>
              <view v-if="getPassiveEffect(selectedSkill, 'physical_defense_bonus')" class="effect-item">
                <text class="effect-label">物理防御:</text>
                <text class="effect-value">+{{ formatEffectValue(getPassiveEffect(selectedSkill, 'physical_defense_bonus')) }}</text>
              </view>
              <view v-if="getPassiveEffect(selectedSkill, 'damage_reduction')" class="effect-item">
                <text class="effect-label">伤害减免:</text>
                <text class="effect-value">+{{ formatEffectValue(getPassiveEffect(selectedSkill, 'damage_reduction')) }}</text>
              </view>
            </view>
          </view>
          
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
  showToast: (options: { title: string; icon?: string; duration?: number }) => void
}

// 定义事件
const emit = defineEmits<{
  close: []
}>()

// 状态
const loading = ref(false)
const skills = ref<Skill[]>([])
const selectedSkill = ref<Skill | null>(null)
const scrollTop = ref(0)

// Store
const campStore = useCampStore()

// 角色信息
const playerCharacter = computed(() => campStore.playerCharacter)

// 判断技能是否为被动技能
function isPassiveSkill(skill: Skill): boolean {
  // 优先使用后端返回的 skillType 字段（性能更好，类型更安全）
  if (skill.skillType) {
    return skill.skillType === 'passive'
  }
  
  // 兼容：如果没有 skillType，则解析 effectPayload（向后兼容）
  try {
    if (skill.effectPayload && typeof skill.effectPayload === 'object') {
      return skill.effectPayload.type === 'passive' || skill.effectPayload.type === 'passive_trigger'
    }
  } catch (e) {
    // 如果解析失败，返回false
  }
  return false
}

// 计算属性：主动技能列表
const activeSkills = computed(() => {
  return skills.value.filter(skill => !isPassiveSkill(skill))
})

// 计算属性：被动技能列表
const passiveSkills = computed(() => {
  return skills.value.filter(skill => isPassiveSkill(skill))
})

// 计算属性：按行和列排序主动技能（从下到上）
const sortedActiveSkills = computed(() => {
  return [...activeSkills.value].sort((a, b) => {
    if (b.positionInTree.row !== a.positionInTree.row) {
      return b.positionInTree.row - a.positionInTree.row
    }
    return a.positionInTree.column - b.positionInTree.column
  })
})

// 计算属性：按行和列排序被动技能（从下到上）
const sortedPassiveSkills = computed(() => {
  return [...passiveSkills.value].sort((a, b) => {
    if (b.positionInTree.row !== a.positionInTree.row) {
      return b.positionInTree.row - a.positionInTree.row
    }
    return a.positionInTree.column - b.positionInTree.column
  })
})

// 计算连接线的通用函数
// 注意：这里使用所有技能列表来查找前置技能，确保跨类型连接也能找到
function calculateConnectionLines(skillList: Skill[], allSkills: Skill[] = []) {
  const lines: Array<{
    from: { row: number; column: number }
    to: { row: number; column: number }
    fromUnlocked: boolean
    toUnlocked: boolean
  }> = []

  // 使用所有技能列表来查找前置技能（支持跨类型连接）
  const searchList = allSkills.length > 0 ? allSkills : skillList

  skillList.forEach(skill => {
    if (skill.unlockPath && skill.unlockPath.length > 0) {
      skill.unlockPath.forEach(prereqCode => {
        // 在所有技能中查找前置技能（支持主动和被动之间的连接）
        let prereqSkill = searchList.find(s => s.code === prereqCode || String(s.id) === String(prereqCode))
        
        if (!prereqSkill && typeof prereqCode === 'string') {
          prereqSkill = searchList.find(s => 
            s.name && prereqCode.toLowerCase().includes(s.name.toLowerCase().replace(/\s+/g, '_'))
          )
        }
        
        // 只有当前置技能也在当前技能列表中时，才添加连接线（确保在同一区域显示）
        if (prereqSkill && skillList.includes(prereqSkill)) {
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
}

// 计算属性：主动技能连接线
const activeConnectionLines = computed(() => {
  // 传入所有技能列表，确保能找到前置技能（即使前置技能是被动技能）
  return calculateConnectionLines(activeSkills.value, skills.value)
})

// 计算属性：被动技能连接线
const passiveConnectionLines = computed(() => {
  // 传入所有技能列表，确保能找到前置技能（即使前置技能是主动技能）
  return calculateConnectionLines(passiveSkills.value, skills.value)
})

// 计算属性：获取每行的等级信息（主动技能）
const activeSkillRowLevels = computed(() => {
  const rowLevels: Map<number, number> = new Map()
  activeSkills.value.forEach(skill => {
    if (skill.requiredLevel) {
      const row = skill.positionInTree.row
      // 如果该行还没有等级，或者当前技能的等级更小（更早的等级），则更新
      if (!rowLevels.has(row) || (rowLevels.get(row)! > skill.requiredLevel)) {
        rowLevels.set(row, skill.requiredLevel)
      }
    }
  })
  // 转换为数组形式，以便在模板中遍历
  return Array.from(rowLevels.entries())
})

// 计算属性：获取每行的等级信息（被动技能）
const passiveSkillRowLevels = computed(() => {
  const rowLevels: Map<number, number> = new Map()
  passiveSkills.value.forEach(skill => {
    if (skill.requiredLevel) {
      const row = skill.positionInTree.row
      // 如果该行还没有等级，或者当前技能的等级更小（更早的等级），则更新
      if (!rowLevels.has(row) || (rowLevels.get(row)! > skill.requiredLevel)) {
        rowLevels.set(row, skill.requiredLevel)
      }
    }
  })
  // 转换为数组形式，以便在模板中遍历
  return Array.from(rowLevels.entries())
})

// 获取行等级标签的样式
function getRowLevelStyle(row: number, skillList: Skill[]) {
  const baseSize = 100
  const spacing = 180 // 增加间距
  const startY = 20
  
  const maxRow = Math.max(...skillList.map(s => s.positionInTree.row))
  const minRow = Math.min(...skillList.map(s => s.positionInTree.row))
  const rowIndex = row - minRow
  
  const top = startY + rowIndex * spacing + baseSize / 2
  
  return {
    top: `${top}rpx`,
    left: '20rpx'
  }
}

// 计算技能树最小高度的通用函数
function calculateSkillTreeMinHeight(skillList: Skill[]) {
  if (skillList.length === 0) {
    return 'auto'
  }
  const baseSize = 100 // 缩小后的节点大小
  const spacing = 180 // 增加节点间距
  const startY = 20
  const paddingBottom = 20
  
  const maxRow = Math.max(...skillList.map(s => s.positionInTree.row))
  const minRow = Math.min(...skillList.map(s => s.positionInTree.row))
  const rowCount = maxRow - minRow + 1
  
  const nodeTotalHeight = baseSize + 60 // 节点高度 + 名称高度（增加了名称的高度）
  const totalHeight = startY + (rowCount - 1) * spacing + nodeTotalHeight + paddingBottom
  
  return `${totalHeight}rpx`
}

// 计算属性：主动技能树最小高度
const activeSkillTreeMinHeight = computed(() => {
  return calculateSkillTreeMinHeight(activeSkills.value)
})

// 计算属性：被动技能树最小高度
const passiveSkillTreeMinHeight = computed(() => {
  return calculateSkillTreeMinHeight(passiveSkills.value)
})

// 获取技能节点样式
function getSkillNodeStyle(skill: Skill, skillList: Skill[]) {
  const baseSize = 100 // 缩小后的节点大小（rpx）- 正方形
  const spacing = 180 // 增加节点间距（rpx）
  const startY = 20 // 起始Y位置（rpx）
  const levelLabelWidth = 80 // 左侧等级标签占用的宽度（rpx）

  // 计算位置（从下到上，row越大越靠下）
  const maxRow = Math.max(...skillList.map(s => s.positionInTree.row))
  const minRow = Math.min(...skillList.map(s => s.positionInTree.row))
  const rowIndex = skill.positionInTree.row - minRow // 0-based row index
  const column = skill.positionInTree.column

  // 计算列位置，居中显示
  const maxCol = Math.max(...skillList.map(s => s.positionInTree.column))
  const minCol = Math.min(...skillList.map(s => s.positionInTree.column))
  const colCount = maxCol - minCol + 1
  
  // 计算节点实际宽度
  const nodeWidth = baseSize
  // 计算总宽度：列数 * 间距 + 最后一个节点的大小
  const totalWidth = (colCount - 1) * spacing + nodeWidth
  
  // 获取容器宽度（考虑弹窗的实际宽度，约85%的750rpx = 637.5rpx，取630rpx）
  // 减去左侧等级标签的宽度
  const containerWidth = 630 - levelLabelWidth // 弹窗实际宽度（rpx）
  const offsetX = Math.max(0, (containerWidth - totalWidth) / 2) + levelLabelWidth

  // 计算相对于最小列的偏移
  const left = offsetX + (column - minCol) * spacing
  const top = startY + rowIndex * spacing

  return {
    left: `${left}rpx`,
    top: `${top}rpx`,
    width: `${baseSize}rpx`,
    height: 'auto' // 高度自适应
  }
}

// 获取连接线样式
function getLineStyle(line: {
  from: { row: number; column: number }
  to: { row: number; column: number }
  fromUnlocked: boolean
  toUnlocked: boolean
}, skillList: Skill[]) {
  const baseSize = 100 // 缩小后的节点大小
  const spacing = 180 // 增加节点间距
  const startY = 20
  const levelLabelWidth = 80 // 左侧等级标签占用的宽度（rpx）

  const maxRow = Math.max(...skillList.map(s => s.positionInTree.row))
  const minRow = Math.min(...skillList.map(s => s.positionInTree.row))
  const maxCol = Math.max(...skillList.map(s => s.positionInTree.column))
  const minCol = Math.min(...skillList.map(s => s.positionInTree.column))
  const colCount = maxCol - minCol + 1

  // 计算起始和结束位置
  const fromRowIndex = line.from.row - minRow
  const toRowIndex = line.to.row - minRow
  const fromCol = line.from.column
  const toCol = line.to.column

  // 计算列位置，居中显示
  const nodeWidth = baseSize
  const totalWidth = (colCount - 1) * spacing + nodeWidth
  const containerWidth = 630 - levelLabelWidth // 弹窗实际宽度（rpx）
  const offsetX = Math.max(0, (containerWidth - totalWidth) / 2) + levelLabelWidth

  // 连接线从图标中心开始
  const iconCenterOffset = baseSize / 2 // 图标中心在节点中心
  const fromX = offsetX + (fromCol - minCol) * spacing + iconCenterOffset
  const fromY = startY + fromRowIndex * spacing + baseSize / 2
  const toX = offsetX + (toCol - minCol) * spacing + iconCenterOffset
  const toY = startY + toRowIndex * spacing + baseSize / 2

  // 计算线的长度和角度
  const dx = toX - fromX
  const dy = toY - fromY
  const length = Math.sqrt(dx * dx + dy * dy)
  const angle = Math.atan2(dy, dx) * (180 / Math.PI)

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

// 获取被动技能效果值
function getPassiveEffect(skill: Skill, effectKey: string): number | null {
  try {
    if (skill.effectPayload && typeof skill.effectPayload === 'object') {
      const value = skill.effectPayload[effectKey]
      if (value !== undefined && value !== null) {
        return Number(value)
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
  return null
}

// 格式化效果值（百分比或数值）
function formatEffectValue(value: number | null): string {
  if (value === null) return ''
  if (value < 1) {
    // 小于1的视为百分比
    return `${(value * 100).toFixed(0)}%`
  } else {
    // 大于等于1的视为固定数值
    return `${value}`
  }
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

// 关闭面板
function handleClose() {
  emit('close')
}

// 获取职业名称
function getCharacterClassName(code: string | undefined): string {
  if (!code) return '未知职业'
  const classMap: Record<string, string> = {
    'warden': '守望者',
    'occultist': '秘术师',
    'ranger': '游侠',
    'warrior': '战士',
    'priest': '牧师',
    'mage': '法师',
    'rogue': '盗贼',
    'mechanist': '机械师'
  }
  return classMap[code] || '未知职业'
}

// 处理头像加载错误
function handleAvatarError() {
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
.skills-panel {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, 
    #1a1a3e 0%, 
    #2d1b4e 25%,
    #3d2b5e 50%, 
    #2d1b4e 75%,
    #1a1a3e 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  // 添加魔法光效背景
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 30%, rgba(138, 43, 226, 0.15) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(75, 0, 130, 0.15) 0%, transparent 50%),
      radial-gradient(circle at 50% 50%, rgba(147, 112, 219, 0.1) 0%, transparent 60%);
    pointer-events: none;
    z-index: 0;
    animation: magicGlow 8s ease-in-out infinite;
  }
}

@keyframes magicGlow {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

// 页面头部
.skills-header {
  position: relative;
  padding: 30rpx 20rpx 20rpx;
  background: linear-gradient(180deg, 
    rgba(138, 43, 226, 0.4) 0%, 
    rgba(75, 0, 130, 0.3) 50%,
    transparent 100%);
  border-bottom: 2rpx solid rgba(147, 112, 219, 0.5);
  box-shadow: 0 4rpx 20rpx rgba(138, 43, 226, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
  flex-shrink: 0;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2rpx;
    background: linear-gradient(90deg, 
      transparent 0%,
      rgba(255, 215, 0, 0.6) 50%,
      transparent 100%);
    animation: shimmer 3s ease-in-out infinite;
  }
}

// 左侧角色信息
.header-character-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 0 0 auto;
  min-width: 200rpx;
}

.header-character-info-placeholder {
  flex: 0 0 auto;
  min-width: 200rpx;
}

.character-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(147, 112, 219, 0.8);
  box-shadow: 
    0 0 15rpx rgba(138, 43, 226, 0.6),
    0 0 30rpx rgba(138, 43, 226, 0.3);
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.3) 0%, 
    rgba(75, 0, 130, 0.3) 100%);
}

.character-details {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.character-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 
    0 0 10rpx rgba(255, 215, 0, 0.8),
    0 0 20rpx rgba(138, 43, 226, 0.4);
  white-space: nowrap;
}

.character-class {
  font-size: 20rpx;
  color: rgba(255, 215, 0, 0.8);
  text-shadow: 0 0 8rpx rgba(255, 215, 0, 0.5);
  white-space: nowrap;
}

// 中间标题（居中）
.header-title-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
}

@keyframes shimmer {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}


.header-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 
    0 0 20rpx rgba(255, 215, 0, 0.9),
    0 0 40rpx rgba(255, 215, 0, 0.6),
    0 0 60rpx rgba(138, 43, 226, 0.4);
  letter-spacing: 4rpx;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 50%, #ffd700 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.3);
  }
}

.header-subtitle {
  font-size: 20rpx;
  color: #cc9966;
  margin-top: 6rpx;
  text-shadow: 0 0 10rpx rgba(204, 153, 102, 0.5);
}

.close-btn {
  width: 50rpx;
  height: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.6) 0%, rgba(75, 0, 130, 0.6) 100%);
  border: 2rpx solid rgba(147, 112, 219, 0.8);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 0 15rpx rgba(138, 43, 226, 0.4);
  position: relative;
  z-index: 11;
  flex: 0 0 auto;
  
  &:hover {
    background: linear-gradient(135deg, rgba(138, 43, 226, 0.8) 0%, rgba(75, 0, 130, 0.8) 100%);
    box-shadow: 0 0 25rpx rgba(138, 43, 226, 0.6);
    transform: scale(1.1);
  }
}

.close-btn:active {
  background: rgba(200, 0, 0, 0.7);
  transform: scale(0.9);
}

.close-icon {
  font-size: 32rpx;
  color: #ffd700;
  font-weight: bold;
}

// 技能树容器
.skills-container {
  width: 100%;
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

// 技能分类区域
.skill-category-section {
  width: 100%;
  margin-bottom: 40rpx;
  flex-shrink: 0;
}

.category-header {
  padding: 15rpx 20rpx;
  border-bottom: 2rpx solid rgba(147, 112, 219, 0.4);
  margin-bottom: 20rpx;
  background: linear-gradient(90deg, 
    transparent 0%,
    rgba(138, 43, 226, 0.1) 50%,
    transparent 100%);
  border-radius: 8rpx;
}

.category-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 
    0 0 15rpx rgba(255, 215, 0, 0.9),
    0 0 30rpx rgba(138, 43, 226, 0.5);
  letter-spacing: 2rpx;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.skill-tree-wrapper {
  position: relative;
  width: 100%;
  padding: 20rpx 0;
  padding-bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  min-height: fit-content;
}

// 技能节点
.skill-node {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 5;
  flex-shrink: 0;
}

// 技能内容区域（图标和名称）
.skill-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  flex-shrink: 0;
}

// 行等级标签
.row-level-label {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 4;
  transform: translateY(-50%);
}

.level-text {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.9);
  font-weight: bold;
  text-shadow: 
    0 0 10rpx rgba(255, 215, 0, 0.8),
    0 0 20rpx rgba(138, 43, 226, 0.4);
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.3) 0%, 
    rgba(75, 0, 130, 0.3) 100%);
  border: 2rpx solid rgba(147, 112, 219, 0.6);
  border-radius: 8rpx;
  white-space: nowrap;
}

.skill-icon-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.skill-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 45rpx;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 12rpx;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  }
}

// 已解锁技能 - 金色魔法光效
.skill-unlocked .skill-icon {
  background: linear-gradient(135deg, 
    #ffd700 0%, 
    #ffaa00 50%,
    #ffd700 100%);
  border: 4rpx solid #ffd700;
  box-shadow: 
    0 0 30rpx rgba(255, 215, 0, 0.9),
    0 0 60rpx rgba(255, 215, 0, 0.6),
    0 0 90rpx rgba(138, 43, 226, 0.4),
    inset 0 0 20rpx rgba(255, 255, 255, 0.4);
  color: #1a1a2e;
  animation: magicGlowPulse 2s ease-in-out infinite;
  
  &::after {
    content: '';
    position: absolute;
    top: -2rpx;
    left: -2rpx;
    right: -2rpx;
    bottom: -2rpx;
    border-radius: 12rpx;
    background: linear-gradient(135deg, 
      rgba(255, 215, 0, 0.6) 0%,
      rgba(138, 43, 226, 0.6) 50%,
      rgba(255, 215, 0, 0.6) 100%);
    z-index: -1;
    animation: borderGlow 3s ease-in-out infinite;
  }
}

@keyframes magicGlowPulse {
  0%, 100% {
    box-shadow: 
      0 0 30rpx rgba(255, 215, 0, 0.9),
      0 0 60rpx rgba(255, 215, 0, 0.6),
      0 0 90rpx rgba(138, 43, 226, 0.4);
  }
  50% {
    box-shadow: 
      0 0 40rpx rgba(255, 215, 0, 1),
      0 0 80rpx rgba(255, 215, 0, 0.8),
      0 0 120rpx rgba(138, 43, 226, 0.6);
  }
}

@keyframes borderGlow {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

// 可解锁技能 - 紫色魔法光效
.skill-available .skill-icon {
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.8) 0%, 
    rgba(75, 0, 130, 0.8) 50%,
    rgba(138, 43, 226, 0.8) 100%);
  border: 4rpx solid rgba(147, 112, 219, 0.9);
  box-shadow: 
    0 0 20rpx rgba(138, 43, 226, 0.7),
    0 0 40rpx rgba(138, 43, 226, 0.4),
    0 0 60rpx rgba(75, 0, 130, 0.3);
  color: #ffd700;
  animation: purplePulse 2s ease-in-out infinite;
}

@keyframes purplePulse {
  0%, 100% {
    box-shadow: 
      0 0 20rpx rgba(138, 43, 226, 0.7),
      0 0 40rpx rgba(138, 43, 226, 0.4),
      0 0 60rpx rgba(75, 0, 130, 0.3);
  }
  50% {
    box-shadow: 
      0 0 30rpx rgba(138, 43, 226, 0.9),
      0 0 60rpx rgba(138, 43, 226, 0.6),
      0 0 90rpx rgba(75, 0, 130, 0.5);
  }
}

// 锁定技能 - 暗紫色，提高亮度
.skill-locked .skill-icon {
  background: linear-gradient(135deg, 
    rgba(45, 27, 78, 0.8) 0%, 
    rgba(29, 26, 62, 0.8) 100%);
  border: 4rpx solid rgba(75, 0, 130, 0.6);
  box-shadow: 
    inset 0 0 20rpx rgba(0, 0, 0, 0.3),
    0 0 10rpx rgba(75, 0, 130, 0.2);
  color: rgba(147, 112, 219, 0.6);
  opacity: 0.7;
}

.skill-node:active {
  transform: scale(0.95);
}

.unlock-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 28rpx;
  height: 28rpx;
  background: linear-gradient(135deg, #00ff88 0%, #00cc66 100%);
  border: 2rpx solid #00ff88;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: #1a1a2e;
  font-weight: bold;
  box-shadow: 
    0 0 15rpx rgba(0, 255, 136, 0.9),
    0 0 30rpx rgba(0, 255, 136, 0.5);
  z-index: 10;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 
      0 0 15rpx rgba(0, 255, 136, 0.9),
      0 0 30rpx rgba(0, 255, 136, 0.5);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 
      0 0 20rpx rgba(0, 255, 136, 1),
      0 0 40rpx rgba(0, 255, 136, 0.7);
  }
}

.skill-name {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #ffffff;
  text-align: center;
  font-weight: bold;
  white-space: nowrap;
  max-width: 120rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  padding: 4rpx 8rpx;
  background: linear-gradient(135deg, 
    rgba(26, 26, 46, 0.9) 0%, 
    rgba(45, 27, 78, 0.9) 100%);
  border: 2rpx solid rgba(147, 112, 219, 0.6);
  border-radius: 8rpx;
  box-shadow: 
    0 0 10rpx rgba(0, 0, 0, 0.5),
    0 0 20rpx rgba(138, 43, 226, 0.3);
  text-shadow: 
    1rpx 1rpx 2rpx rgba(0, 0, 0, 0.8),
    0 0 8rpx rgba(255, 215, 0, 0.6);
}

.skill-locked .skill-name {
  color: rgba(200, 200, 200, 0.7);
  background: linear-gradient(135deg, 
    rgba(26, 26, 46, 0.7) 0%, 
    rgba(45, 27, 78, 0.7) 100%);
  border-color: rgba(75, 0, 130, 0.4);
  text-shadow: 
    1rpx 1rpx 2rpx rgba(0, 0, 0, 0.6),
    0 0 6rpx rgba(147, 112, 219, 0.4);
}


// 连接线 - 魔法光效
.connection-line {
  position: absolute;
  height: 6rpx;
  background: linear-gradient(90deg, 
    rgba(147, 112, 219, 0.4) 0%, 
    rgba(138, 43, 226, 0.6) 50%, 
    rgba(147, 112, 219, 0.4) 100%);
  z-index: 1;
  transform-origin: 0 50%;
  box-shadow: 
    0 0 10rpx rgba(138, 43, 226, 0.4),
    0 0 20rpx rgba(75, 0, 130, 0.2);
  border-radius: 3rpx;
  animation: lineFlow 3s ease-in-out infinite;
  
  // 已解锁的连接线 - 金色魔法光效
  &.line-unlocked {
    background: linear-gradient(90deg, 
      rgba(255, 215, 0, 0.6) 0%, 
      rgba(255, 215, 0, 1) 50%, 
      rgba(255, 215, 0, 0.6) 100%);
    box-shadow: 
      0 0 20rpx rgba(255, 215, 0, 0.8),
      0 0 40rpx rgba(255, 215, 0, 0.4),
      0 0 60rpx rgba(138, 43, 226, 0.3);
    height: 8rpx;
    animation: lineFlowGold 2s ease-in-out infinite;
  }
}

@keyframes lineFlow {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

@keyframes lineFlowGold {
  0%, 100% {
    box-shadow: 
      0 0 20rpx rgba(255, 215, 0, 0.8),
      0 0 40rpx rgba(255, 215, 0, 0.4),
      0 0 60rpx rgba(138, 43, 226, 0.3);
  }
  50% {
    box-shadow: 
      0 0 30rpx rgba(255, 215, 0, 1),
      0 0 60rpx rgba(255, 215, 0, 0.6),
      0 0 90rpx rgba(138, 43, 226, 0.5);
  }
}


// 技能详情弹窗
.skill-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s;
}

.modal-content {
  width: 80%;
  max-width: 600rpx;
  background: linear-gradient(180deg, 
    rgba(45, 27, 78, 0.95) 0%, 
    rgba(29, 26, 62, 0.95) 50%,
    rgba(45, 27, 78, 0.95) 100%);
  border: 4rpx solid rgba(147, 112, 219, 0.8);
  border-radius: 20rpx;
  box-shadow: 
    0 0 40rpx rgba(138, 43, 226, 0.6),
    0 0 80rpx rgba(75, 0, 130, 0.4),
    inset 0 0 30rpx rgba(255, 215, 0, 0.1);
  overflow: hidden;
  animation: slideUp 0.3s;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 30% 20%, rgba(138, 43, 226, 0.2) 0%, transparent 50%),
      radial-gradient(circle at 70% 80%, rgba(75, 0, 130, 0.2) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }
}

.modal-header {
  padding: 30rpx;
  background: linear-gradient(180deg, 
    rgba(138, 43, 226, 0.5) 0%, 
    rgba(75, 0, 130, 0.3) 50%,
    transparent 100%);
  border-bottom: 2rpx solid rgba(147, 112, 219, 0.5);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2rpx;
    background: linear-gradient(90deg, 
      transparent 0%,
      rgba(255, 215, 0, 0.6) 50%,
      transparent 100%);
  }
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 
    0 0 15rpx rgba(255, 215, 0, 0.9),
    0 0 30rpx rgba(255, 215, 0, 0.6),
    0 0 45rpx rgba(138, 43, 226, 0.4);
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
  position: relative;
  z-index: 1;
}

.skill-type-badge {
  display: inline-block;
  padding: 10rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
  box-shadow: 0 0 15rpx rgba(138, 43, 226, 0.3);
}

.type-active {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.4) 0%, 
    rgba(37, 99, 235, 0.6) 100%);
  border: 2rpx solid rgba(59, 130, 246, 0.8);
  color: #93c5fd;
  text-shadow: 0 0 10rpx rgba(59, 130, 246, 0.8);
  box-shadow: 
    0 0 15rpx rgba(59, 130, 246, 0.4),
    0 0 30rpx rgba(37, 99, 235, 0.2);
}

.type-passive {
  background: linear-gradient(135deg, 
    rgba(168, 85, 247, 0.4) 0%, 
    rgba(147, 51, 234, 0.6) 100%);
  border: 2rpx solid rgba(168, 85, 247, 0.8);
  color: #c4b5fd;
  text-shadow: 0 0 10rpx rgba(168, 85, 247, 0.8);
  box-shadow: 
    0 0 15rpx rgba(168, 85, 247, 0.4),
    0 0 30rpx rgba(147, 51, 234, 0.2);
}

.skill-type-text {
  font-size: 22rpx;
  font-weight: bold;
}

.skill-description {
  font-size: 28rpx;
  color: rgba(255, 215, 0, 0.9);
  line-height: 1.8;
  margin-bottom: 30rpx;
  text-shadow: 
    0 0 10rpx rgba(255, 215, 0, 0.5),
    0 0 20rpx rgba(138, 43, 226, 0.3);
}

.passive-effects {
  margin-bottom: 30rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.15) 0%, 
    rgba(75, 0, 130, 0.15) 100%);
  border-radius: 12rpx;
  border: 2rpx solid rgba(147, 112, 219, 0.4);
  box-shadow: 
    inset 0 0 20rpx rgba(138, 43, 226, 0.1),
    0 0 15rpx rgba(138, 43, 226, 0.2);
}

.effects-title {
  font-size: 26rpx;
  color: #ffd700;
  font-weight: bold;
  margin-bottom: 15rpx;
  display: block;
  text-shadow: 
    0 0 10rpx rgba(255, 215, 0, 0.8),
    0 0 20rpx rgba(138, 43, 226, 0.4);
}

.effects-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.effect-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 8rpx;
  border-radius: 8rpx;
  background: rgba(138, 43, 226, 0.05);
  transition: all 0.3s;
  
  &:hover {
    background: rgba(138, 43, 226, 0.1);
  }
}

.effect-label {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.9);
  min-width: 120rpx;
  text-shadow: 0 0 8rpx rgba(255, 215, 0, 0.5);
}

.effect-value {
  font-size: 24rpx;
  color: #00ff88;
  font-weight: bold;
  text-shadow: 
    0 0 10rpx rgba(0, 255, 136, 0.8),
    0 0 20rpx rgba(0, 255, 136, 0.4);
}

.prerequisites {
  margin-bottom: 20rpx;
  padding: 15rpx;
  background: rgba(138, 43, 226, 0.05);
  border-radius: 8rpx;
  border-left: 4rpx solid rgba(147, 112, 219, 0.6);
}

.prerequisites-title {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.9);
  margin-right: 10rpx;
  text-shadow: 0 0 8rpx rgba(255, 215, 0, 0.5);
  font-weight: bold;
}

.prerequisite-item {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.8);
  text-shadow: 0 0 6rpx rgba(255, 215, 0, 0.4);
}

.level-requirement {
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  padding: 15rpx;
  background: rgba(138, 43, 226, 0.05);
  border-radius: 8rpx;
  border-left: 4rpx solid rgba(147, 112, 219, 0.6);
}

.requirement-label {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.9);
  margin-right: 10rpx;
  text-shadow: 0 0 8rpx rgba(255, 215, 0, 0.5);
  font-weight: bold;
}

.requirement-value {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.9);
  text-shadow: 0 0 8rpx rgba(255, 215, 0, 0.5);
  font-weight: bold;
}

.unlock-action {
  margin-top: 30rpx;
}

.unlock-btn {
  width: 100%;
  padding: 24rpx;
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.8) 0%, 
    rgba(75, 0, 130, 0.8) 100%);
  border: 2rpx solid rgba(147, 112, 219, 0.9);
  border-radius: 12rpx;
  color: #ffd700;
  font-size: 28rpx;
  font-weight: bold;
  text-shadow: 
    0 0 10rpx rgba(255, 215, 0, 0.8),
    0 0 20rpx rgba(138, 43, 226, 0.4);
  box-shadow: 
    0 0 20rpx rgba(138, 43, 226, 0.6),
    0 0 40rpx rgba(75, 0, 130, 0.3);
  transition: all 0.3s;
  
  &:hover {
    background: linear-gradient(135deg, 
      rgba(138, 43, 226, 1) 0%, 
      rgba(75, 0, 130, 1) 100%);
    box-shadow: 
      0 0 30rpx rgba(138, 43, 226, 0.8),
      0 0 60rpx rgba(75, 0, 130, 0.5);
  }
}

.unlock-btn:active {
  transform: scale(0.95);
  box-shadow: 
    0 0 40rpx rgba(138, 43, 226, 1),
    0 0 80rpx rgba(75, 0, 130, 0.7);
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

