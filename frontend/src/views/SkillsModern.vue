<template>
  <div class="skills-container">
    <!-- 返回首页按钮 -->
    <RouterLink to="/" class="back-to-home">
      <i class="fas fa-home"></i>
      返回首页
    </RouterLink>
    
    <!-- 头部信息 -->
    <header class="skills-header">
      <div class="character-info">
        <div class="character-avatar">
          <img 
            src="/touxiang.png" 
            :alt="getPlayerClassName()"
            @error="handleImageError"
          />
        </div>
        <div class="character-details">
          <h2>{{ getPlayerClassName() || '未知角色' }}</h2>
          <div class="level-info">
            <span class="level">等级 {{ getPlayerLevel() }}</span>
            <div class="exp-bar">
              <div 
                class="exp-fill" 
                :style="{ width: `${expPercentage}%` }"
              ></div>
              <span class="exp-text">{{ currentExp }}/{{ maxExp }}</span>
            </div>
          </div>
        </div>
      </div>
      
    </header>

    <div v-if="errorMessage" class="error-banner">
      <i class="fas fa-exclamation-triangle"></i>
      <span>{{ errorMessage }}</span>
      <button v-if="errorMessage.includes('创建角色')" class="retry-btn" @click="goToCamp">
        前往营地创建角色
      </button>
      <button v-else class="retry-btn" @click="initializeSkills">
        重试
      </button>
    </div>

    <!-- 技能树主体 -->
    <main class="skill-tree-main">
      <div class="tree-container" v-if="skillTree">
        <!-- 线性技能列表 -->
        <div class="linear-skill-list">
          <div 
            v-for="(skill, index) in allSkills" 
            :key="skill.id"
            class="linear-skill-item"
            :class="{
              'unlocked': isSkillUnlocked(skill.id),
              'available': canUnlockSkill(skill),
              'locked': !canUnlockSkill(skill) && !isSkillUnlocked(skill.id),
              'active': selectedSkill?.id === skill.id
            }"
            @click="selectSkill(skill)"
            @mouseenter="hoveredSkill = skill"
            @mouseleave="hoveredSkill = null"
          >
            <div class="skill-item-content">
              <div class="skill-item-icon">
                <div class="skill-icon-placeholder">{{ getSkillIcon(skill.code) }}</div>
                <div v-if="isSkillUnlocked(skill.id)" class="skill-unlocked-badge">✓</div>
              </div>
              <div class="skill-item-info">
                <div class="skill-item-name">{{ skill.name }}</div>
                <div class="skill-item-desc">{{ skill.description }}</div>
                <div class="skill-item-level">需要等级: {{ skill.requiredLevel }}</div>
              </div>
              <div class="skill-item-arrow" v-if="index < allSkills.length - 1">→</div>
            </div>
          </div>
        </div>

        <!-- 技能详情面板 -->
        <div class="skill-details-panel" v-if="selectedSkill || hoveredSkill">
          <div class="skill-detail-content">
            <h3>{{ (selectedSkill || hoveredSkill)?.name }}</h3>
            <p class="skill-description">{{ (selectedSkill || hoveredSkill)?.description }}</p>
            
            <div class="skill-requirements">
              <h4>解锁条件:</h4>
              <ul>
                <li v-for="req in getSkillRequirements(selectedSkill || hoveredSkill)" :key="req">
                  {{ req }}
                </li>
              </ul>
            </div>
            
            <div class="skill-effects">
              <h4>技能效果:</h4>
              <div class="effect-item">
                <span>基础效果:</span>
                <span>{{ getSkillEffect(selectedSkill || hoveredSkill, 1) }}</span>
              </div>
              <div v-if="(selectedSkill || hoveredSkill) && canUpgradeSkill(selectedSkill || hoveredSkill || undefined)" class="effect-item">
                <span>升级效果:</span>
                <span>{{ getSkillEffect(selectedSkill || hoveredSkill, 2) }}</span>
              </div>
            </div>
            
            <div class="skill-actions">
              <button
                v-if="selectedSkill && canUnlockSkill(selectedSkill)"
                class="unlock-btn"
                :disabled="unlockingSkillId === selectedSkill.id"
                @click="unlockSelectedSkill(selectedSkill)"
              >
                {{ unlockingSkillId === selectedSkill.id ? '解锁中...' : '解锁技能' }}
              </button>
              <div v-else-if="selectedSkill" class="skill-status">
                <span class="status-label">
                  {{ isSkillUnlocked(selectedSkill.id) ? '已解锁' : '尚未满足解锁条件' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-else-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载技能树中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <i class="fas fa-tree"></i>
        <p>暂无可用的技能树</p>
      </div>
    </main>

    <!-- 底部统计 -->
    <footer class="skills-footer">
      <div class="skill-stats">
        <div class="stat-item">
          <span>已解锁技能:</span>
          <span>{{ unlockedSkillsCount }}/{{ totalSkillsCount }}</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { skillApi } from '@/lib/api'
import { useAuthStore } from '@/stores/auth'
import { useCampStore } from '@/stores/camp'
import type { Skill } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const campStore = useCampStore()

const selectedCharacter = ref('')
const skillTree = ref<Skill[]>([])
const selectedSkill = ref<Skill | null>(null)
const hoveredSkill = ref<Skill | null>(null)
const loading = ref(false)
const errorMessage = ref<string | null>(null)
const unlockingSkillId = ref<string | null>(null)

const playerCharacter = computed(() => campStore.playerCharacter)
const playerLevel = computed(() => {
  // 如果没有角色，返回1；如果有角色但没有level字段，尝试从其他字段推断
  if (!playerCharacter.value) return 1
  // 可以根据经验值或其他字段计算等级，这里先返回1
  return 1
})
const playerClassName = computed(() => playerCharacter.value?.playerCharacterName || '未知角色')
const currentExp = computed(() => playerCharacter.value?.currentActionPoints ?? 0)
const maxExp = computed(() => playerCharacter.value?.maxActionPoints ?? 1)
const expPercentage = computed(() => {
  if (!maxExp.value || maxExp.value <= 0) return 0
  return Math.min(100, (currentExp.value / maxExp.value) * 100)
})

const allSkills = computed(() => {
  return [...skillTree.value].sort((a, b) => {
    if (a.positionInTree.row !== b.positionInTree.row) {
      return a.positionInTree.row - b.positionInTree.row
    }
    return a.positionInTree.column - b.positionInTree.column
  })
})

const gridStyle = computed(() => {
  if (!allSkills.value.length) return {}
  const maxRow = Math.max(...allSkills.value.map(s => s.positionInTree.row))
  const maxCol = Math.max(...allSkills.value.map(s => s.positionInTree.column))
  return {
    gridTemplateColumns: `repeat(${maxCol + 1}, 80px)`,
    gridTemplateRows: `repeat(${maxRow + 1}, 100px)`,
    gap: '20px'
  }
})

const unlockedSkillsCount = computed(() => skillTree.value.filter(skill => skill.isUnlocked).length)
const totalSkillsCount = computed(() => skillTree.value.length)
const skillMapByCode = computed(() => new Map(skillTree.value.map(skill => [skill.code, skill])))

function safeJsonParse(value: unknown, fallback: any) {
  if (value == null) return fallback
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch {
      return fallback
    }
  }
  return value ?? fallback
}

function normalizeSkill(dto: any, index: number): Skill {
  const positionRaw = safeJsonParse(dto.positionInTree, dto.positionInTree)
  const position = {
    row: Number(positionRaw?.row ?? index),
    column: Number(positionRaw?.column ?? 0)
  }
  const unlockPathRaw = safeJsonParse(dto.unlockPath, dto.unlockPath)
  const unlockPath = Array.isArray(unlockPathRaw) ? unlockPathRaw : []
  const effectPayloadRaw = safeJsonParse(dto.effectPayload, dto.effectPayload)
  const effectPayload =
    effectPayloadRaw && typeof effectPayloadRaw === 'object'
      ? effectPayloadRaw
      : { description: dto.description || effectPayloadRaw || '' }

  return {
    id: String(dto.id),
    code: dto.code,
    playerCharacterCode: dto.playerCharacterCode || selectedCharacter.value,
    name: dto.name,
    description: dto.description,
    effectPayload,
    requiredLevel: dto.requiredLevel ?? 1,
    positionInTree: position,
    unlockPath,
    isUnlocked: Boolean(dto.isUnlocked),
    canUnlock: Boolean(dto.canUnlock)
  }
}

async function ensureCampData() {
  if (!playerCharacter.value && auth.isAuthenticated) {
    try {
      await campStore.fetchCampData()
    } catch (error) {
      console.error('Failed to load camp data:', error)
    }
  }
}

async function initializeSkills() {
  await ensureCampData()
  if (!auth.isAuthenticated) {
    errorMessage.value = '请先登录后查看技能树'
    return
  }
  
  // 检查是否有角色
  if (!playerCharacter.value) {
    errorMessage.value = '您还没有创建角色，请先在营地创建角色后再查看技能树'
    return
  }
  
  // 获取角色代码
  const characterCode = playerCharacter.value?.playerCharacterCode
  if (!characterCode) {
    errorMessage.value = '无法获取角色信息，请刷新页面重试'
    return
  }
  
  selectedCharacter.value = characterCode
  await loadSkillTree()
}

async function loadSkillTree() {
  if (!selectedCharacter.value || !auth.isAuthenticated) {
    return
  }
  
  loading.value = true
  errorMessage.value = null
  
  try {
    const response = await skillApi.getSkillTree(selectedCharacter.value)
    
    if (response.data && response.data.code === 200) {
      const list = Array.isArray(response.data.data) ? response.data.data : []
      skillTree.value = list.map((item: any, index: number) => normalizeSkill(item, index))
      selectedSkill.value = skillTree.value[0] || null
      
      if (list.length === 0) {
        errorMessage.value = '该职业暂无可用技能'
      }
    } else {
      const errorMsg = response.data?.message || '获取技能树失败'
      throw new Error(errorMsg)
    }
  } catch (error: any) {
    console.error('Failed to load skill tree:', error)
    
    // 处理不同类型的错误
    if (error.response) {
      // HTTP错误响应
      const status = error.response.status
      if (status === 401) {
        errorMessage.value = '登录已过期，请重新登录'
      } else if (status === 404) {
        errorMessage.value = '该职业的技能树不存在'
      } else if (status === 403) {
        errorMessage.value = '没有权限访问技能树'
      } else {
        errorMessage.value = error.response.data?.message || `获取技能树失败 (${status})`
      }
    } else if (error.request) {
      // 网络错误
      errorMessage.value = '网络连接失败，请检查后端服务是否正常运行'
    } else {
      // 其他错误
    errorMessage.value = error instanceof Error ? error.message : '获取技能树失败'
    }
    
    skillTree.value = []
    selectedSkill.value = null
  } finally {
    loading.value = false
  }
}

function isSkillUnlocked(skillId: string): boolean {
  return skillTree.value.some(skill => skill.id === skillId && skill.isUnlocked)
}

function canUnlockSkill(skill?: Skill | null): boolean {
  if (!skill) return false
  return Boolean(skill.canUnlock) && !skill.isUnlocked
}

function canUpgradeSkill(skill?: Skill | null): boolean {
  if (!skill) return false
  return Boolean(skill.isUnlocked) && Boolean(skill.canUnlock)
}

function getSkillRequirements(skill?: Skill | null): string[] {
  if (!skill) return []
  const requirements: string[] = []
  if (skill.requiredLevel > 1) {
    requirements.push(`需要等级 ${skill.requiredLevel}`)
  }
  if (skill.unlockPath && skill.unlockPath.length > 0) {
    const names = skill.unlockPath.map(code => skillMapByCode.value.get(code)?.name || code)
    requirements.push(`前置技能：${names.join('、')}`)
  }
  if (!skill.canUnlock && !skill.isUnlocked) {
    requirements.push('等待等级或前置技能满足')
  }
  return requirements
}

function formatEffectPayload(payload: Record<string, any>, multiplier = 1): string {
  const entries = Object.entries(payload || {})
  if (!entries.length) {
    return '效果描述待补充'
  }
  return entries
    .map(([key, value]) => {
      const label = key
        .replace(/([A-Z])/g, ' $1')
        .replace(/_/g, ' ')
        .trim()
      if (typeof value === 'number') {
        const display = Number.isInteger(value) ? value * multiplier : Number((value * multiplier).toFixed(2))
        return `${label} +${display}`
      }
      if (typeof value === 'boolean') {
        return `${label} ${value ? '激活' : '关闭'}`
      }
      return `${label} ${value}`
    })
    .join('，')
}

function getSkillEffect(skill?: Skill | null, level = 1): string {
  if (!skill) return ''
  return formatEffectPayload(skill.effectPayload || {}, level)
}

function selectSkill(skill: Skill) {
  selectedSkill.value = skill
}

async function unlockSelectedSkill(skill?: Skill | null) {
  const targetSkill = skill || selectedSkill.value
  if (!targetSkill || !canUnlockSkill(targetSkill)) {
    return
  }
  unlockingSkillId.value = targetSkill.id
  try {
    const response = await skillApi.unlockSkill(targetSkill.id)
    if (response.data.code === 200) {
      showToast('技能解锁成功', 'success')
      await loadSkillTree()
    } else {
      throw new Error(response.data.message || '解锁技能失败')
    }
  } catch (error) {
    console.error('Failed to unlock skill:', error)
    showToast(error instanceof Error ? error.message : '解锁技能失败', 'error')
  } finally {
    unlockingSkillId.value = null
  }
}

function getPlayerClassName(): string {
  return playerClassName.value
}

function getPlayerLevel(): number {
  return playerLevel.value
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-avatar.png'
}

function getSkillIcon(code: string): string {
  const iconMap: Record<string, string> = {
    basic_attack: '⚔️',
    defense_stance: '🛡️',
    power_strike: '💥',
    iron_will: '🔒',
    berserker: '🔥',
    war_master: '👑',
    dark_bolt: '🌑',
    curse: '☠️',
    summon_undead: '💀',
    dark_ritual: '🔮',
    necromancy: '⚰️',
    void_master: '🌌',
    precise_shot: '🏹',
    trap_mastery: '🪤',
    eagle_eye: '👁️',
    nature_bond: '🌿',
    multi_shot: '🎯',
    master_hunter: '🦅',
    heal: '✨',
    blessing: '🌟',
    purify: '💧',
    divine_protection: '🛡️',
    resurrection: '⚡',
    divine_light: '☀️',
    fireball: '🔥',
    ice_shard: '❄️',
    lightning_bolt: '⚡',
    arcane_power: '💫',
    meteor: '☄️',
    archmage: '🧙',
    backstab: '🗡️',
    dodge: '💨',
    poison_blade: '☠️',
    shadow_step: '👻',
    assassinate: '⚔️',
    shadow_master: '🌑'
  }
  return iconMap[code] || '⭐'
}

function showToast(message: string, type: 'success' | 'error' | 'info' = 'info') {
  const tag = type === 'error' ? '[技能系统][错误]' : '[技能系统]'
  console.log(tag, message)
}

function goToCamp() {
  router.push('/camp')
}

onMounted(async () => {
  if (!auth.isAuthenticated) {
    errorMessage.value = '请先登录后查看技能树'
    return
  }
  await initializeSkills()
})

watch(
  () => playerCharacter.value?.playerCharacterCode,
  async (code, oldCode) => {
    if (code && code !== oldCode) {
      selectedCharacter.value = code
      await loadSkillTree()
    }
  }
)

// 监听角色变化，如果角色被创建，重新加载技能树
watch(
  () => playerCharacter.value,
  async (newChar, oldChar) => {
    if (newChar && !oldChar && newChar.playerCharacterCode) {
      selectedCharacter.value = newChar.playerCharacterCode
      await loadSkillTree()
    }
  }
)
</script>



<style scoped>
.skills-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #ffffff;
  padding: 20px;
  position: relative;
}

/* 返回首页按钮 */
.back-to-home {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
}

.back-to-home:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.back-to-home i {
  font-size: 1rem;
}

.skills-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: var(--secondary-bg);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.error-banner {
  margin-bottom: 20px;
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(244, 67, 54, 0.4);
  background: rgba(244, 67, 54, 0.15);
  color: #ff8a80;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.retry-btn {
  margin-left: auto;
  padding: 6px 12px;
  background: rgba(244, 67, 54, 0.3);
  border: 1px solid rgba(244, 67, 54, 0.6);
  border-radius: 6px;
  color: #ff8a80;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: rgba(244, 67, 54, 0.5);
  border-color: rgba(244, 67, 54, 0.8);
}

.character-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.character-avatar {
  width: 60px;
  height: 60px;
}

.character-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid var(--text-accent);
  object-fit: cover;
}

.character-details h2 {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
}

.level-info {
  margin-bottom: 8px;
}

.level {
  font-weight: bold;
  color: var(--text-accent);
  margin-bottom: 4px;
  display: block;
}

.exp-bar {
  width: 200px;
  height: 6px;
  background: var(--tertiary-bg);
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.exp-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--text-accent), #ff5722);
  transition: width 0.3s ease;
}

.exp-text {
  position: absolute;
  top: -20px;
  right: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.skill-points {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  color: var(--warning);
}

.tree-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tree-selector label {
  color: var(--text-secondary);
}

.tree-selector select {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 8px 12px;
  border-radius: 6px;
  min-width: 150px;
}

.skill-tree-main {
  margin-bottom: 20px;
}

.tree-container {
  background: var(--secondary-bg);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  padding: 30px;
  position: relative;
  overflow: auto;
}

.skill-grid {
  position: relative;
  display: grid;
  min-width: 400px;
  min-height: 400px;
  margin: 0 auto;
}

/* 线性技能列表样式 */
.linear-skill-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.linear-skill-item {
  background: var(--tertiary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.linear-skill-item:hover {
  transform: translateX(5px);
  border-color: var(--text-accent);
}

.linear-skill-item.unlocked {
  background: rgba(76, 175, 80, 0.1);
  border-color: var(--success);
}

.linear-skill-item.available {
  background: rgba(255, 152, 0, 0.1);
  border-color: var(--warning);
  animation: pulse-available 2s infinite;
}

.linear-skill-item.locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.linear-skill-item.active {
  border-color: var(--text-accent);
  box-shadow: 0 0 15px rgba(229, 62, 62, 0.5);
}

.skill-item-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.skill-item-icon {
  position: relative;
  flex-shrink: 0;
}

.skill-icon-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.skill-unlocked-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--success);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.skill-item-info {
  flex: 1;
}

.skill-item-name {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.skill-item-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.skill-item-level {
  font-size: 12px;
  color: var(--text-muted);
}

.skill-item-arrow {
  font-size: 24px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.skill-node {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--tertiary-bg);
  border: 2px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skill-node:hover {
  transform: scale(1.1);
  z-index: 10;
}

.skill-node.available {
  border-color: var(--warning);
  background: rgba(255, 152, 0, 0.1);
  animation: pulse-available 2s infinite;
}

.skill-node.unlocked {
  border-color: var(--success);
  background: rgba(76, 175, 80, 0.1);
}

.skill-node.locked {
  border-color: var(--text-muted);
  background: var(--tertiary-bg);
  cursor: not-allowed;
  opacity: 0.6;
}

.skill-node.active {
  border-color: var(--text-accent);
  box-shadow: 0 0 15px rgba(229, 62, 62, 0.5);
}

.skill-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: var(--secondary-bg);
}

.skill-icon img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.skill-level {
  position: absolute;
  bottom: -5px;
  right: -5px;
  background: var(--text-accent);
  color: white;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
}

.skill-connection {
  position: absolute;
  height: 2px;
  background: var(--border-color);
  transform-origin: 0 50%;
  z-index: 1;
  transition: all 0.3s;
}

.skill-connection.available {
  background: var(--warning);
  height: 3px;
}

.skill-connection.active {
  background: var(--success);
  height: 3px;
}

.skill-details-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 300px;
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-heavy);
  z-index: 100;
}

.skill-detail-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
}

.skill-description {
  margin: 0 0 15px 0;
  color: var(--text-secondary);
  line-height: 1.4;
}

.skill-requirements,
.skill-effects {
  margin-bottom: 15px;
}

.skill-requirements h4,
.skill-effects h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
}

.skill-requirements ul {
  margin: 0;
  padding-left: 20px;
}

.skill-requirements li {
  margin-bottom: 4px;
  color: var(--text-secondary);
  font-size: 14px;
}

.effect-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 14px;
}

.effect-item span:first-child {
  color: var(--text-secondary);
}

.effect-item span:last-child {
  color: var(--text-primary);
  font-weight: bold;
}

.skill-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.unlock-btn,
.upgrade-btn {
  background: linear-gradient(135deg, var(--text-accent), #ff8c42);
  color: #1a1a2e;
  border: none;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.unlock-btn:disabled,
.upgrade-btn:disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  color: #1a1a2e;
}

.upgrade-btn {
  background: var(--text-accent);
}

.upgrade-btn:hover:not(:disabled) {
  background: #d84315;
}

.skill-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.status-label {
  color: var(--text-secondary);
}

.status-value {
  color: var(--text-primary);
  font-weight: bold;
}

.loading-container,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: var(--secondary-bg);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-container p,
.empty-state p {
  color: var(--text-secondary);
  font-size: 16px;
}

.empty-state i {
  font-size: 48px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.skills-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.skill-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.stat-item span:first-child {
  color: var(--text-secondary);
}

.stat-item span:last-child {
  color: var(--text-primary);
  font-weight: bold;
}

.reset-btn {
  background: var(--error);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reset-btn:hover {
  background: #d32f2f;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse-available {
  0%, 100% { 
    box-shadow: 0 0 5px rgba(255, 152, 0, 0.5); 
  }
  50% { 
    box-shadow: 0 0 15px rgba(255, 152, 0, 0.8); 
  }
}

@media (max-width: 768px) {
  .skills-container {
    padding: 12px;
  }
  
  .skills-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .character-info {
    flex-direction: column;
    text-align: center;
  }
  
  .exp-bar {
    width: 150px;
  }
  
  .tree-container {
    padding: 20px;
  }
  
  .skill-grid {
    min-width: 300px;
    transform: scale(0.8);
    transform-origin: top center;
  }
  
  .skill-details-panel {
    position: static;
    width: 100%;
    margin-top: 20px;
  }
  
  .skills-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>