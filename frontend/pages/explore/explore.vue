<template>
  <view class="explore-container">
    <!-- 返回首页按钮 -->
    <view class="back-to-home" @click="goToHome">
      <text class="back-icon">🏠</text>
      <text class="back-text">返回首页</text>
    </view>
    
    <!-- 顶部标题栏 -->
    <view class="explore-header">
      <view class="header-content">
        <view class="header-title">
          <h1 class="title-text">⚔️ 地牢探索</h1>
          <p class="title-subtitle">挑战暗黑地牢，征服未知领域</p>
        </view>
        <view class="header-stats">
          <view class="stat-card">
            <view class="stat-label">当前章节</view>
            <view class="stat-value">第 {{ chapter }} 章</view>
          </view>
          <view class="stat-card">
            <view class="stat-label">关卡进度</view>
            <view class="stat-value">{{ passedLevels.length }} / {{ maxLevel }}</view>
          </view>
        </view>
      </view>
    </view>

    <view class="explore-content">
      <!-- 遮罩层 - 点击左侧区域可关闭面板 -->
      <view 
        v-if="selectedStageLevel || isUnknownNode" 
        class="panel-overlay" 
        @click="closeStageDetail"
      ></view>

      <!-- 主地图区域 -->
      <view class="main-section">
        <!-- 关卡地图 -->
        <view class="map-card">
          <view class="map-header">
            <h3 class="map-title">关卡地图</h3>
            <view class="map-legend">
              <view class="legend-item">
                <view class="legend-color legend-current"></view>
                <text>当前</text>
              </view>
              <view class="legend-item">
                <view class="legend-color legend-passed"></view>
                <text>已通关</text>
              </view>
              <view class="legend-item">
                <view class="legend-color legend-claimed"></view>
                <text>已领取</text>
              </view>
            </view>
          </view>

          <view 
            class="map-content horizontal-route" 
            v-if="routeNodes && routeNodes.length > 0"
            ref="mapContentRef"
            @mousedown="handleMapMouseDown"
            @mousemove="handleMapMouseMove"
            @mouseup="handleMapMouseUp"
            @mouseleave="handleMapMouseUp"
            @touchstart="handleMapTouchStart"
            @touchmove="handleMapTouchMove"
            @touchend="handleMapTouchEnd"
            :style="{ height: `${mapHeight}px` }"
          >
            <!-- SVG路径层 -->
            <svg 
              class="route-lines" 
              :viewBox="`0 0 ${mapWidth} ${mapHeight}`" 
              :width="mapWidth" 
              :height="mapHeight"
            >
              <template v-for="(node, index) in routeNodes" :key="`path-group-${index}`">
                <path
                  v-for="(pathD, pIdx) in getPathsFromNode(index)"
                  :key="`path-${index}-${pIdx}`"
                  :d="pathD"
                  class="route-path"
                  fill="none"
                />
              </template>
            </svg>
            
            <!-- 节点层 -->
            <view class="route-nodes" :style="{ width: `${mapWidth}px`, height: `${mapHeight}px` }">
              <view
                v-for="(node, index) in routeNodes"
                :key="`node-${index}`"
                class="route-node-wrapper"
                :style="{ left: `${index * 160 + 20}px` }"
            >
                <!-- 分支节点容器 -->
                <view v-if="node.type === 'branch'" class="branch-container">
                  <!-- 左分支 -->
                  <view class="branch-node branch-left">
                    <!-- 战斗节点 -->
                    <view
                      v-if="node.leftType === 'battle' && node.leftLevel"
                      class="route-node"
                      :class="getLevelNodeClass(node.leftLevel)"
                      @click="selectLevel(node.leftLevel)"
                    >
                      <view class="node-icon">⚔️</view>
                      <view class="node-number">{{ node.leftLevel }}</view>
                      <view class="node-status">
                        <text v-if="levelStatus(node.leftLevel).claimed" class="status-badge claimed">✓</text>
                        <text v-else-if="levelStatus(node.leftLevel).passed" class="status-badge passed">✓</text>
                        <text v-else-if="levelStatus(node.leftLevel).isCurr" class="status-badge current">●</text>
              </view>
                      <view class="node-label">战斗</view>
                    </view>
                    <!-- 未知节点 -->
                <view
                      v-else-if="node.leftType === 'unknown'"
                      class="route-node unknown-node"
                      @click="handleUnknownNodeClick()"
                >
                      <view class="node-icon unknown-icon">❓</view>
                      <view class="node-label">未知</view>
                      <view class="unknown-glow"></view>
                  </view>
                </view>
                  <!-- 右分支 -->
                  <view class="branch-node branch-right">
                    <!-- 战斗节点 -->
                    <view
                      v-if="node.rightType === 'battle' && node.rightLevel"
                      class="route-node"
                      :class="getLevelNodeClass(node.rightLevel)"
                      @click="selectLevel(node.rightLevel)"
                    >
                      <view class="node-icon">⚔️</view>
                      <view class="node-number">{{ node.rightLevel }}</view>
                      <view class="node-status">
                        <text v-if="levelStatus(node.rightLevel).claimed" class="status-badge claimed">✓</text>
                        <text v-else-if="levelStatus(node.rightLevel).passed" class="status-badge passed">✓</text>
                        <text v-else-if="levelStatus(node.rightLevel).isCurr" class="status-badge current">●</text>
              </view>
                      <view class="node-label">战斗</view>
                    </view>
                    <!-- 未知节点 -->
                    <view
                      v-else-if="node.rightType === 'unknown'"
                      class="route-node unknown-node"
                      @click="handleUnknownNodeClick()"
                    >
                      <view class="node-icon unknown-icon">❓</view>
                      <view class="node-label">未知</view>
                      <view class="unknown-glow"></view>
                    </view>
                  </view>
                </view>
                <!-- 普通节点 -->
                <view v-else-if="node.level" class="main-node">
                  <view
                    class="route-node"
                    :class="getLevelNodeClass(node.level)"
                    @click="selectLevel(node.level)"
                  >
                    <view class="node-icon">⚔️</view>
                    <view class="node-number">{{ node.level }}</view>
                    <view class="node-status">
                      <text v-if="levelStatus(node.level).claimed" class="status-badge claimed">✓</text>
                      <text v-else-if="levelStatus(node.level).passed" class="status-badge passed">✓</text>
                      <text v-else-if="levelStatus(node.level).isCurr" class="status-badge current">●</text>
                    </view>
                    <view class="node-difficulty">{{ stageInfo(node.level).difficulty }}</view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
    </view>

      <!-- 右侧关卡详情面板 -->
      <aside class="stage-detail-panel" :class="{ 'panel-visible': selectedStageLevel || isUnknownNode }">
        <view class="panel-content" v-if="selectedStageLevel || isUnknownNode">
          <button class="panel-close" @click="closeStageDetail">✕</button>
          
          <!-- 关卡标题 -->
          <view class="stage-title-section">
            <view class="stage-combat-icon">{{ isUnknownNode ? '❓' : '⚔️' }}</view>
            <view class="stage-title-info">
              <view class="stage-combat-label">{{ isUnknownNode ? '奇遇' : '作战' }}</view>
              <view class="stage-name-label">{{ isUnknownNode ? '未知事件' : getStageInfo(selectedStageLevel!).name }}</view>
          </view>
            </view>

          <!-- INFO 描述框 -->
          <view class="stage-info-box">
            <view class="info-box-label">INFO</view>
            <view class="info-box-content">
              <p>{{ isUnknownNode ? '会遇到特殊的事件或敌人。' : (getStageInfo(selectedStageLevel!).desc || '暂无描述') }}</p>
              </view>
            </view>

          <!-- 预览区域 - 奇遇节点时不显示 -->
          <view class="stage-preview-section" v-if="!isUnknownNode">
            <!-- 敌方情报 -->
            <view class="preview-item">
              <view class="preview-label">
                <text class="preview-icon">🔍</text>
                <text>敌方情报</text>
          </view>
              <view class="preview-content enemy-preview">
                <view class="preview-placeholder">
                  <view class="preview-icon-large">👹</view>
                  <view class="preview-text">敌人信息</view>
        </view>
              </view>
    </view>

            <!-- 通关奖励 -->
            <view class="preview-item" v-if="selectedStageLevel">
              <view class="preview-label">
                <text class="preview-icon">🎁</text>
                <text>通关奖励</text>
          </view>
              <view class="preview-content reward-preview">
            <view class="reward-list">
              <view class="reward-item">
                    <view class="reward-icon">🪙</view>
                    <view class="reward-info">
                      <view class="reward-label">金币</view>
                      <view class="reward-value">{{ getRewards(selectedStageLevel, getStageInfo(selectedStageLevel).difficulty).gold }}</view>
                    </view>
              </view>
              <view class="reward-item">
                    <view class="reward-icon">⭐</view>
                    <view class="reward-info">
                      <view class="reward-label">经验</view>
                      <view class="reward-value">{{ getRewards(selectedStageLevel, getStageInfo(selectedStageLevel).difficulty).exp }}</view>
              </view>
            </view>
          </view>
          </view>
        </view>
      </view>

          <!-- 底部操作按钮 -->
          <view class="stage-action-section">
            <button 
              class="stage-start-btn" 
              :class="{ 'unknown-btn': isUnknownNode }"
              @click="isUnknownNode ? handleGoToUnknown() : startStage()" 
              :disabled="inBattle"
            >
              <text class="start-btn-icon">{{ isUnknownNode ? '❓' : '⚔️' }}</text>
              <text class="start-btn-text">{{ isUnknownNode ? '前往' : '開戰' }}</text>
              <text class="start-btn-subtext">{{ isUnknownNode ? '×奇遇' : '×作战' }}</text>
            </button>
          </view>
        </view>
      </aside>
    </view>

    <!-- 奇遇事件弹窗 -->
    <view v-if="showEventModal && currentEvent" class="event-modal-backdrop" @click.self="closeEventModal">
      <view class="event-modal">
        <view class="event-modal-header">
          <text class="event-modal-icon">❓</text>
          <view class="event-modal-title">{{ currentEvent.title }}</view>
          <button class="event-modal-close" @click="closeEventModal">✕</button>
        </view>
        <view class="event-modal-body">
          <p class="event-modal-desc">{{ currentEvent.desc }}</p>
        </view>
        <view class="event-modal-footer">
          <button class="event-modal-btn" @click="closeEventModal">知道了</button>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { useWalletStore } from '@/stores/wallet'
import { useCharactersStore } from '@/stores/characters'
import { useGameStore } from '@/stores/game'

// uni-app 全局对象类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
  navigateBack: (options?: { delta?: number }) => void
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
}
import { campApi, stageProgressApi, stageApi } from '@/api/request'

type Stage = {
  level: number
  name: string
  difficulty: '普通' | '困难' | '噩梦'
  desc?: string
}

const wallet = useWalletStore()
const chars = useCharactersStore()
const game = useGameStore()

// 获取页面参数（替代 useRoute）
let routeQuery: Record<string, any> = {}

const currentLevel = ref(1)
const maxLevel = 30
const chapter = computed(() => Math.floor((currentLevel.value - 1) / 5) + 1)
const totalChapters = computed(() => Math.ceil(maxLevel / 5))
const inBattle = ref(false)
const passedLevels = ref<number[]>([])
const claimedLevels = ref<number[]>([])
const selectedStageLevel = ref<number | null>(null)
const isUnknownNode = ref(false) // 标识是否是未知节点（奇遇）
const currentEvent = ref<{ title: string; desc: string } | null>(null)
const showEventModal = ref(false)
const dungeonEvents = ref<Array<{ id: number; name: string; description: string; locationType?: string }>>([])
const eventsLoading = ref(false)

// 关卡奖励数据缓存（从后端获取）
const stageRewards = ref<Record<number, { gold: number; exp: number; stress?: number }>>({})

// 地图拖拽相关
const mapContentRef = ref<HTMLElement | null>(null)
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartScrollLeft = ref(0)

async function loadProgress() {
  try {
    // 从后端加载关卡进度
    const response = await stageProgressApi.getAllProgress()
    if (response.data.code === 200 && response.data.data) {
      const progressList = response.data.data as Array<{ stageNumber: number; isPassed: boolean }>
      // 提取已通过的关卡编号
      passedLevels.value = progressList
        .filter(p => p.isPassed)
        .map(p => p.stageNumber)
        .sort((a, b) => a - b)
      
      // 如果没有进度记录，确保第1关是解锁的（但未通过）
      if (passedLevels.value.length === 0) {
        currentLevel.value = 1
      } else {
        // 设置当前关卡为最后一个已通过的关卡的下一个，或第1关
        const maxPassed = Math.max(...passedLevels.value)
        currentLevel.value = Math.min(maxPassed + 1, maxLevel)
      }
    } else {
      currentLevel.value = 1
    }
  } catch (error) {
    console.error('加载关卡进度失败:', error)
    // 如果加载失败，默认从第1关开始
    currentLevel.value = 1
  }
}

async function loadDungeonEvents() {
  try {
    eventsLoading.value = true
    const res = await campApi.getEvents()
    const list = res.data?.data ?? []
    // 不再区分营地/地牢，全部作为奇遇事件使用
    dungeonEvents.value = list.map((e: any) => ({
      id: Number(e.id),
      name: e.name,
      description: e.description || '神秘事件',
      locationType: e.locationType
    }))
  } catch (error) {
    console.error('加载奇遇事件失败:', error)
    dungeonEvents.value = []
  } finally {
    eventsLoading.value = false
  }
}

async function upsertProgress(level: number, changes: Partial<{ passed: boolean; claimed: boolean }>) {
  try {
    if (changes.passed && !passedLevels.value.includes(level)) {
      // 调用后端API保存关卡进度
      try {
        await stageProgressApi.passStage(level)
      passedLevels.value.push(level)
        passedLevels.value.sort((a, b) => a - b)
      } catch (error) {
        console.error('保存关卡进度失败:', error)
        // 即使后端保存失败，也在本地标记，避免用户重复通关
        passedLevels.value.push(level)
        passedLevels.value.sort((a, b) => a - b)
      }
    }
    if (changes.claimed && !claimedLevels.value.includes(level)) {
      claimedLevels.value.push(level)
    }
  } catch (error) {
    console.error('更新进度失败:', error)
  }
}

let battleTimer: any = null

function getStageInfo(level: number): Stage {
  const db = stagesMap.value[level]
  if (db) return { level, name: db.name, difficulty: db.difficulty, desc: db.desc }
  return stageInfo(level)
}

function closeStageDetail() {
  selectedStageLevel.value = null
  isUnknownNode.value = false
}

function stageInfo(level: number): Stage {
  const chapterIdx = Math.floor((level - 1) / 5) + 1
  const idxInChapter = ((level - 1) % 5) + 1
  const chapterNames = [
    '遗忘之径', '深影洞窟', '血色牢城', '黑曜古塔', '虚空裂隙',
    '霜寒墓园', '荒蛮旷野', '腐朽神殿', '雷鸣断崖', '终焉祭坛'
  ]
  const baseName = chapterNames[chapterIdx - 1] ?? `第${chapterIdx}章`
  const namesPerIdx = ['哨岗', '窄道', '密室', '前庭', '守门者']
  const name = `${baseName}·${namesPerIdx[idxInChapter - 1] ?? '关卡'}`
  const diff: Stage['difficulty'] =
    level <= 10 ? '普通' : level <= 20 ? '困难' : '噩梦'
  const desc = `每5关为一章。当前为第${chapterIdx}章·第${idxInChapter}关。`
  return { level, name, difficulty: diff, desc }
}

const stagesMap = ref<Record<number, { name: string; difficulty: Stage['difficulty']; desc?: string }>>({})
async function loadStages() {
  try {
    const mockStages = Array.from({ length: maxLevel }, (_, i) => {
      const lvl = i + 1
      const si = stageInfo(lvl)
      return { level: lvl, name: si.name, difficulty: si.difficulty, description: si.desc }
    })
    stagesMap.value = Object.fromEntries(mockStages.map((r: any) => [r.level, { name: r.name, difficulty: r.difficulty, desc: r.description }]))
  } catch (error) {
    console.log('Mock stages loaded')
  }
}

const stage = computed(() => {
  const db = stagesMap.value[currentLevel.value]
  if (db) return { level: currentLevel.value, name: db.name, difficulty: db.difficulty, desc: db.desc }
  return stageInfo(currentLevel.value)
})

async function ensureStages() {
  console.log('Mock stages ensured')
}

// 从后端加载关卡奖励数据
async function loadStageReward(level: number) {
  // 如果已经加载过，直接返回
  if (stageRewards.value[level]) {
    return stageRewards.value[level]
  }
  
  try {
    const response = await stageApi.getStageByNumber(level)
    if (response.data.code === 200 && response.data.data) {
      const stage = response.data.data
      
      // 解析奖励池配置（JSON格式）
      let rewardPool: any = {}
      if (stage.rewardPool) {
        try {
          rewardPool = typeof stage.rewardPool === 'string' 
            ? JSON.parse(stage.rewardPool) 
            : stage.rewardPool
        } catch (e) {
          console.warn(`[Explore] 解析关卡 ${level} 奖励池配置失败，使用默认值:`, e)
        }
      }
      
      // 从奖励池获取奖励数据
      const gold = rewardPool.gold || 50
      const exp = rewardPool.exp || 50
      const stress = rewardPool.stress
      
      const reward = { gold, exp, ...(stress !== undefined && { stress }) }
      stageRewards.value[level] = reward
      
      console.log(`[Explore] 已加载关卡 ${level} 的奖励数据:`, reward)
      return reward
    }
  } catch (error) {
    console.error(`[Explore] 获取关卡 ${level} 奖励失败:`, error)
  }
  
  // 降级方案：使用默认值
  return getDefaultRewards(level)
}

// 获取默认奖励（降级方案）
function getDefaultRewards(level: number): { gold: number; exp: number } {
  // 根据关卡数计算基础奖励
  const baseGold = 50 + (level - 1) * 10
  const baseExp = 50 + (level - 1) * 10
  return { gold: baseGold, exp: baseExp }
}

// 获取奖励（优先从缓存获取，如果没有则使用默认值）
function getRewards(level: number | null, difficulty?: Stage['difficulty']): { gold: number; exp: number; stress?: number } {
  if (level && stageRewards.value[level]) {
    return stageRewards.value[level]
  }
  
  // 降级方案：根据难度返回默认值
  if (difficulty) {
    if (difficulty === '普通') return { gold: 50, exp: 50 }
    if (difficulty === '困难') return { gold: 100, exp: 100 }
    return { gold: 150, exp: 150 }
  }
  
  // 如果有关卡编号，使用默认计算
  if (level) {
    return getDefaultRewards(level)
  }
  
  return { gold: 50, exp: 50 }
}

function getDifficultyIcon(difficulty: Stage['difficulty']): string {
  if (difficulty === '普通') return '⚪'
  if (difficulty === '困难') return '🟠'
  return '🔴'
}

function stopBattleLog() {
  if (battleTimer) { clearInterval(battleTimer); battleTimer = null }
}


async function startStage() {
  if (inBattle.value) return
  inBattle.value = true
  
  try {
    await ensureStages()
    await upsertProgress(currentLevel.value, { passed: false })
    if (game && game.configureEncounter) {
      game.configureEncounter(stage.value.difficulty)
    }
    if (game && game.loadEnemyDeck) {
      await game.loadEnemyDeck(currentLevel.value)
    }
    uni.navigateTo({ url: `/pages/battle/battle?level=${currentLevel.value}` })
  } catch (error) {
    uni.navigateTo({ url: `/pages/battle/battle?level=${currentLevel.value}` })
  }
}

async function completeStage() {
  if (!inBattle.value) return
  inBattle.value = false
  stopBattleLog()
  
  try {
    if (!passedLevels.value.includes(currentLevel.value)) {
      passedLevels.value.push(currentLevel.value)
      await upsertProgress(currentLevel.value, { passed: true })
    }
  } catch (error) {
    console.error('进度更新失败:', error)
  }
}

function nextStage() {
  if (inBattle.value) return
  if (currentLevel.value >= maxLevel) return
  currentLevel.value++
  selectedStageLevel.value = currentLevel.value
}

function prevStage() {
  if (inBattle.value) return
  if (currentLevel.value <= 1) return
  currentLevel.value--
  selectedStageLevel.value = currentLevel.value
}

function selectLevel(level: number) {
  if (inBattle.value || isDragging.value) return
  currentLevel.value = level
  selectedStageLevel.value = level
  isUnknownNode.value = false
  // 自动加载该关卡的奖励数据
  loadStageReward(level)
}

// 处理主区域点击（关闭面板，但不关闭节点点击）
function handleMainSectionClick(event: MouseEvent) {
  // 如果点击的不是节点，则关闭面板
  const target = event.target as HTMLElement
  if (!target.closest('.route-node') && !target.closest('.route-lines')) {
    closeStageDetail()
  }
}

// 地图拖拽功能 - 鼠标事件
function handleMapMouseDown(event: MouseEvent) {
  const target = event.target as HTMLElement
  // 仅当点击节点时不启动拖拽，其余空白/线条区域都可拖动
  if (target.closest('.route-node')) return
  if (!mapContentRef.value) return
  
  isDragging.value = true
  dragStartX.value = event.clientX
  dragStartScrollLeft.value = mapContentRef.value.scrollLeft
  mapContentRef.value.classList.add('dragging')
  event.preventDefault()
}

function handleMapMouseMove(event: MouseEvent) {
  if (!isDragging.value || !mapContentRef.value) return
  
  const deltaX = event.clientX - dragStartX.value
  mapContentRef.value.scrollLeft = dragStartScrollLeft.value - deltaX
  event.preventDefault()
}

function handleMapMouseUp() {
  if (mapContentRef.value) {
    mapContentRef.value.classList.remove('dragging')
  }
  isDragging.value = false
}

// 地图拖拽功能 - 触摸事件
function handleMapTouchStart(event: TouchEvent) {
  const target = event.target as HTMLElement
  // 仅当点击节点时不启动拖拽
  if (target.closest('.route-node')) return
  if (!mapContentRef.value || event.touches.length !== 1) return
  
  isDragging.value = true
  dragStartX.value = event.touches[0].clientX
  dragStartScrollLeft.value = mapContentRef.value.scrollLeft
  event.preventDefault()
}

function handleMapTouchMove(event: TouchEvent) {
  if (!isDragging.value || !mapContentRef.value || event.touches.length !== 1) return
  
  const deltaX = event.touches[0].clientX - dragStartX.value
  mapContentRef.value.scrollLeft = dragStartScrollLeft.value - deltaX
  event.preventDefault()
    }

function handleMapTouchEnd() {
  isDragging.value = false
  }

// 处理未知节点点击
function handleUnknownNodeClick() {
  if (inBattle.value || isDragging.value) return
  isUnknownNode.value = true
  selectedStageLevel.value = null // 清空关卡选择，显示奇遇内容
  currentEvent.value = null
  showEventModal.value = false
  if (!eventsLoading.value && dungeonEvents.value.length === 0) {
    loadDungeonEvents()
}
}

// 处理前往未知节点
async function handleGoToUnknown() {
  if (inBattle.value) return
  if (dungeonEvents.value.length === 0 && !eventsLoading.value) {
    await loadDungeonEvents()
  }
  const pool = dungeonEvents.value
  if (pool.length === 0) {
    currentEvent.value = { title: '暂无事件', desc: '事件数据获取失败，请稍后重试。' }
  } else {
    const idx = Math.floor(Math.random() * pool.length)
    const ev = pool[idx]
    currentEvent.value = { title: ev.name, desc: ev.description || '神秘事件' }
  }
  isUnknownNode.value = true
  showEventModal.value = true
}

function closeEventModal() {
  showEventModal.value = false
}

function getChapterLevels(chapterNum: number): number[] {
  const start = (chapterNum - 1) * 5 + 1
  const end = Math.min(chapterNum * 5, maxLevel)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
}

function getChapterProgress(chapterNum: number): number {
  const levels = getChapterLevels(chapterNum)
  return levels.filter(l => passedLevels.value.includes(l)).length
}

function levelStatus(l: number) {
  const passed = passedLevels.value.includes(l)
  const claimed = claimedLevels.value.includes(l)
  const isCurr = currentLevel.value === l
  return { passed, claimed, isCurr }
}

function getLevelNodeClass(level: number): string {
  const status = levelStatus(level)
  if (status.claimed) return 'level-claimed'
  if (status.passed) return 'level-passed'
  if (status.isCurr) return 'level-current'
  return 'level-locked'
}

// 路线节点数据结构
interface RouteNode {
  type: 'main' | 'branch'
  level?: number
  leftLevel?: number
  rightLevel?: number
  leftType?: 'battle' | 'unknown'  // 左分支类型：战斗或未知
  rightType?: 'battle' | 'unknown' // 右分支类型：战斗或未知
}

// 生成路线节点（前五关在第3关添加分支）
const routeNodes = computed<RouteNode[]>(() => {
  try {
    const nodes: RouteNode[] = []
    
    // 第1关
    nodes.push({ type: 'main', level: 1 })
    // 第2关
    nodes.push({ type: 'main', level: 2 })
    // 第3关：分支节点（二选一）- 左分支是战斗，右分支是未知节点
    nodes.push({ 
      type: 'branch', 
      leftLevel: 3, 
      rightLevel: undefined,
      leftType: 'battle',
      rightType: 'unknown'
    })
    // 第4关（分支后合并）
    nodes.push({ type: 'main', level: 4 })
    // 第5关
    nodes.push({ type: 'main', level: 5 })
    
    // 第6关到第11关
    for (let i = 6; i <= 11; i++) {
      nodes.push({ type: 'main', level: i })
    }
    
    // 第12关：分支节点（二选一）- 左分支是战斗，右分支是未知节点
    nodes.push({ 
      type: 'branch', 
      leftLevel: 12, 
      rightLevel: undefined,
      leftType: 'battle',
      rightType: 'unknown'
    })
    // 第13关（分支后合并）
    nodes.push({ type: 'main', level: 13 })
    
    // 第14关到第30关
    for (let i = 14; i <= maxLevel; i++) {
      nodes.push({ type: 'main', level: i })
    }
    
    return nodes
  } catch (error) {
    console.error('生成路线节点失败:', error)
    return []
  }
})

// 统一地图宽高，保证 SVG 和节点层对齐
const mapWidth = computed(() => routeNodes.value.length * 160 + 100)
const mapHeight = 400

// 获取从上一个节点到当前节点的路径（横向布局）
function getPathToNode(node: RouteNode, index: number): string {
  const nodes = routeNodes.value
  if (!nodes || index === 0) return ''
  
  const prevNode = nodes[index - 1]
  // 以节点的右侧作为起点（prev），左侧作为终点（current）
  const x1 = (index - 1) * 160 + 100 // 上一节点右侧
  const x2 = index * 160 + 10       // 当前节点左侧，稍靠左以便箭头贴边
  const centerY = 200
  const midX = (x1 + x2) / 2
  const curveOffset = 50
  
  if (prevNode.type === 'branch') {
    // 从前一个分支节点合并到当前节点
    const topY = 120
    const bottomY = 280
    if (node.type === 'main') {
      // 分支合并到主线
      const c1x = x1 + 60
      const c2x = x2 - 40
      return `M ${x1 + 40} ${topY} C ${c1x} ${topY} ${c2x} ${centerY - curveOffset} ${x2} ${centerY} M ${x1 + 40} ${bottomY} C ${c1x} ${bottomY} ${c2x} ${centerY + curveOffset} ${x2} ${centerY}`
    }
  } else if (node.type === 'branch') {
    // 从主线分叉到当前分支节点
    const topY = 120
    const bottomY = 280
    const c1x = x1 + 40
    const c2x = x2 - 60
    return `M ${x1} ${centerY} C ${c1x} ${centerY - curveOffset} ${c2x} ${topY} ${x2 - 40} ${topY} M ${x1} ${centerY} C ${c1x} ${centerY + curveOffset} ${c2x} ${bottomY} ${x2 - 40} ${bottomY}`
  } else {
    // 主线到主线：直线
    return `M ${x1} ${centerY} C ${midX} ${centerY - curveOffset} ${midX} ${centerY + curveOffset} ${x2} ${centerY}`
  }
  return ''
}

// 获取从当前节点到后续节点的路径（横向布局），返回多条线段
function getPathsFromNode(index: number): string[] {
  const nodes = routeNodes.value
  if (!nodes || index >= nodes.length - 1) return []
  const node = nodes[index]
  const nextNode = nodes[index + 1]
  const paths: string[] = []

  // 统一的水平间距与控制点偏移
  const nodeWidth = 90
  const nodeLeft = index * 160 + 20
  const nextNodeLeft = (index + 1) * 160 + 20
  const startXRight = nodeLeft + nodeWidth // 当前节点右侧贴边
  const endXLeft = nextNodeLeft // 下一节点左侧贴边
  const centerY = 200
  const curveOffset = 40
  const topY = 140
  const bottomY = 260

  // 主线到主线
  const addMainToMain = () => {
    const midX = (startXRight + endXLeft) / 2
    paths.push(`M ${startXRight} ${centerY} C ${midX} ${centerY - curveOffset} ${midX} ${centerY + curveOffset} ${endXLeft} ${centerY}`)
  }

  // 从主线到分支（两条）
  const addMainToBranch = () => {
    const midX = (startXRight + endXLeft) / 2
    paths.push(`M ${startXRight} ${centerY} C ${midX} ${centerY - curveOffset} ${midX} ${topY} ${endXLeft} ${topY}`)
    paths.push(`M ${startXRight} ${centerY} C ${midX} ${centerY + curveOffset} ${midX} ${bottomY} ${endXLeft} ${bottomY}`)
  }

  // 从分支到主线（两条）
  const addBranchToMain = () => {
    const midXTop = (startXRight + endXLeft) / 2
    const midXBottom = midXTop
    // 从分支上下连接到后续节点，起点稍微偏回 20 像素以贴合节点边缘
    paths.push(`M ${startXRight - 20} ${topY} C ${midXTop} ${topY} ${midXTop} ${centerY - curveOffset} ${endXLeft} ${centerY}`)
    paths.push(`M ${startXRight - 20} ${bottomY} C ${midXBottom} ${bottomY} ${midXBottom} ${centerY + curveOffset} ${endXLeft} ${centerY}`)
  }

  if (nextNode.type === 'branch') {
    // 进入分支：从当前节点生成两条线到分支的上下
    addMainToBranch()
  } else if (node.type === 'branch') {
    // 退出分支：从分支的上下分别连回主线
    addBranchToMain()
  } else {
    // 普通节点：单条线
    addMainToMain()
  }

  return paths
}

// 获取路径样式类
function getPathClass(node: RouteNode): string {
  if (node.type === 'branch') {
    return 'branch-path'
  }
  return 'main-path'
}

// 监听 selectedStageLevel 变化，自动加载奖励数据
watch(selectedStageLevel, (newLevel) => {
  if (newLevel) {
    loadStageReward(newLevel)
  }
})

onLoad((options?: Record<string, any>) => {
  (async () => {
    // 保存页面参数
    routeQuery = options || {}
  try {
    await wallet.loadWallets().catch(() => {})
  } catch (error) {
    console.error('钱包初始化失败:', error)
  }
  
  try {
    await ensureStages()
    await loadStages()
    await loadProgress()
  } catch (error) {
    console.error('数据加载失败:', error)
  }
  
  // 处理战斗胜利返回的情况（进度已在 Game.vue 中保存，这里只需要刷新显示）
    const victory = routeQuery.victory === '1'
    const lvl = Number(routeQuery.level ?? 0)
  if (victory && lvl > 0) {
    currentLevel.value = lvl
    selectedStageLevel.value = lvl
    // 重新加载进度以确保显示最新状态
    await loadProgress()
    // 加载该关卡的奖励数据
    loadStageReward(lvl)
  } else {
    // 预加载当前关卡的奖励数据
    if (currentLevel.value) {
      loadStageReward(currentLevel.value)
    }
  }

  // 预加载地牢事件（奇遇）
  loadDungeonEvents()
  })()
})

// 页面显示时可按需刷新
onShow(() => {
  // 可根据需要刷新数据
})

onUnload(() => stopBattleLog())

// 跳转到首页
function goToHome() {
  uni.navigateTo({ url: '/pages/home/home' })
}
</script>

<style scoped>
.explore-container {
  min-height: 100vh;
  background: url('/static/tansuo.png') center center / cover no-repeat;
  color: #ffffff;
  position: relative;
}

/* 微信小程序 WXSS 不支持 * 选择器，改为具体选择器 */
.explore-container > view {
  position: relative;
  z-index: 1;
}

/* 返回首页按钮（改为相对定位，rpx 单位） */
.back-to-home {
  position: absolute;
  top: 40rpx;
  left: 40rpx;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 40rpx;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50rpx;
  color: white;
  font-size: 28rpx;
  font-weight: 600;
}

/* 顶部标题栏（rpx & 移除 heavy blur/shadow） */
.explore-header {
  background: rgba(15, 23, 42, 0.92);
  border-bottom: 2rpx solid rgba(212, 175, 55, 0.3);
  padding: 48rpx 40rpx;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 48rpx;
}

.header-title {
  flex: 1;
}

.title-text {
  font-size: 60rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 12rpx 0;
  letter-spacing: 2rpx;
}

.title-subtitle {
  font-size: 30rpx;
  color: rgba(255, 255, 255, 0.72);
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 24rpx;
}

.stat-card {
  background: rgba(212, 175, 55, 0.12);
  border: 1rpx solid rgba(212, 175, 55, 0.3);
  border-radius: 24rpx;
  padding: 24rpx 40rpx;
  text-align: center;
  min-width: 240rpx;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 8rpx;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #d4af37;
}

/* 主内容区域 */
.explore-content {
  width: 100%;
  margin: 0 auto;
  padding: 32px;
  display: flex;
  gap: 0;
  position: relative;
}

/* 遮罩层 */
.panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: calc(100% - 33.333%);
  height: 100vh;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
  z-index: 999;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

/* 主区域 */
.main-section {
  flex: 1;
  width: 100%;
  min-width: 0;
}

.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.stage-title-group {
  flex: 1;
}

.stage-number {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.stage-name {
  font-size: 2rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
  line-height: 1.2;
}

.stage-difficulty {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.875rem;
}

/* 微信小程序 WXSS 不支持类名中包含中文字符，改为英文 */
.difficulty-normal {
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
  color: #4caf50;
}

.difficulty-hard {
  background: rgba(255, 152, 0, 0.2);
  border: 1px solid rgba(255, 152, 0, 0.4);
  color: #ff9800;
}

.difficulty-nightmare {
  background: rgba(244, 67, 54, 0.2);
  border: 1px solid rgba(244, 67, 54, 0.4);
  color: #f44336;
}

.difficulty-icon {
  font-size: 1rem;
}

.stage-description {
  margin-bottom: 24px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  font-size: 0.95rem;
}

/* 操作按钮组 */
.action-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.btn-primary {
  background: linear-gradient(135deg, #d4af37, #ffd700);
  color: #1a1a2e;
  border: none;
  border-radius: 12px;
  padding: 14px 28px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-large {
  padding: 16px 32px;
  font-size: 1.1rem;
}

.btn-secondary {
  background: rgba(212, 175, 55, 0.1);
  color: #d4af37;
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 14px 28px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.5);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-nav {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #ffffff;
  font-size: 1.2rem;
}

.btn-nav:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(212, 175, 55, 0.5);
}

.btn-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 1.1rem;
}

/* 关卡状态信息 */
.stage-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-item {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
}

.info-value {
  font-size: 1rem;
  font-weight: bold;
  color: #ffffff;
}

.status-active {
  color: #4caf50;
}

.status-idle {
  color: rgba(255, 255, 255, 0.7);
}

/* 地图卡片 */
.map-card {
  /* 删除所有背景、边框、阴影样式，让背景图直接显示 */
  width: 100%;
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  /* 删除边框，让背景图直接显示 */
}

.map-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
}

.map-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.7);
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-current {
  background: #3b82f6;
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.6);
}

.legend-passed {
  background: rgba(255, 255, 255, 0.3);
}

.legend-claimed {
  background: #4caf50;
}

.map-content {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  position: relative;
  scrollbar-width: thin;
  scrollbar-color: rgba(212, 175, 55, 0.5) transparent;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  touch-action: pan-x;
}

.map-content:active {
  cursor: grabbing;
}

.map-content.dragging {
  cursor: grabbing;
}

/* 拖拽时禁用节点点击 */
.map-content.dragging .route-node {
  pointer-events: none;
}

.map-content::-webkit-scrollbar {
  height: 8px;
}

.map-content::-webkit-scrollbar-track {
  background: transparent;
}

.map-content::-webkit-scrollbar-thumb {
  background: rgba(212, 175, 55, 0.5);
  border-radius: 4px;
}

.map-content::-webkit-scrollbar-thumb:hover {
  background: rgba(212, 175, 55, 0.7);
}

/* 横向路线地图 */
.horizontal-route {
  position: relative;
  padding: 0; /* 取消内边距，避免节点与 SVG 坐标偏移 */
  min-width: 100%;
  min-height: 100%;
}

.route-lines {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 1;
  overflow: visible;
}

.route-path {
  opacity: 0.8;
  transition: opacity 0.3s ease;
  stroke: #ffffff;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.route-nodes {
  position: relative;
  z-index: 2;
}

.route-node-wrapper {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.main-node {
  display: flex;
  justify-content: center;
}

.branch-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 280px; /* 与路径的上下 Y 值（140/260）贴合 */
  gap: 80px;
}

.branch-node {
  display: flex;
  justify-content: center;
  align-items: center;
}

.branch-node.branch-left {
  align-self: flex-start;
}

.branch-node.branch-right {
  align-self: flex-end;
}

.route-node {
  position: relative;
  width: 90px;
  height: 90px;
  background: rgba(0, 0, 0, 0.6);
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.route-node:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4);
}

.route-node.level-current {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.3);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.6);
}

.route-node.level-passed {
  border-color: rgba(34, 197, 94, 0.8);
  background: rgba(34, 197, 94, 0.2);
  box-shadow: 0 0 12px rgba(34, 197, 94, 0.4);
}

.route-node.level-claimed {
  border-color: #4caf50;
  background: rgba(76, 175, 80, 0.3);
}

.route-node.level-locked {
  opacity: 0.4;
  cursor: not-allowed;
}

.event-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1500;
  padding: 24px;
}

.event-modal {
  background: rgba(17, 24, 39, 0.92);
  border: 1px solid rgba(139, 92, 246, 0.4);
  border-radius: 16px;
  width: min(520px, 90vw);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 20px 24px;
  position: relative;
}

.event-modal-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.event-modal-icon {
  font-size: 1.5rem;
}

.event-modal-title {
  flex: 1;
  font-size: 1.2rem;
  font-weight: 700;
}

.event-modal-close {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
}

.event-modal-body {
  padding: 8px 0 16px;
}

.event-modal-desc {
  margin: 0;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
}

.event-modal-footer {
  display: flex;
  justify-content: flex-end;
}

.event-modal-btn {
  padding: 10px 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.9), rgba(124, 58, 237, 1));
  color: #fff;
  border: 1px solid rgba(139, 92, 246, 0.8);
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.2s ease;
}

.event-modal-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.5);
}
.node-icon {
  font-size: 1.5rem;
  margin-bottom: 4px;
}

.node-number {
  font-size: 1rem;
  font-weight: bold;
  color: #ffffff;
}

.node-status {
  position: absolute;
  top: -4px;
  right: -4px;
}

.node-difficulty {
  position: absolute;
  bottom: -20px;
  font-size: 0.625rem;
  color: rgba(255, 255, 255, 0.6);
  white-space: nowrap;
}

.node-label {
  position: absolute;
  bottom: -20px;
  font-size: 0.625rem;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
  font-weight: 600;
}

/* 未知节点样式 */
.unknown-node {
  border-color: rgba(139, 92, 246, 0.6) !important;
  background: rgba(139, 92, 246, 0.2) !important;
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.5) !important;
  animation: unknown-pulse 2s ease-in-out infinite;
}

.unknown-node:hover {
  border-color: rgba(139, 92, 246, 0.9) !important;
  background: rgba(139, 92, 246, 0.3) !important;
  box-shadow: 0 0 30px rgba(139, 92, 246, 0.7) !important;
  transform: scale(1.15) !important;
}

.unknown-icon {
  font-size: 2rem !important;
  animation: unknown-rotate 3s linear infinite;
  filter: drop-shadow(0 0 8px rgba(139, 92, 246, 0.8));
}

.unknown-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.4) 0%, transparent 70%);
  animation: unknown-glow-pulse 2s ease-in-out infinite;
  pointer-events: none;
  z-index: -1;
}

@keyframes unknown-pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(139, 92, 246, 0.5);
  }
  50% {
    box-shadow: 0 0 30px rgba(139, 92, 246, 0.8);
  }
}

@keyframes unknown-rotate {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-10deg);
  }
  50% {
    transform: rotate(0deg);
  }
  75% {
    transform: rotate(10deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

@keyframes unknown-glow-pulse {
  0%, 100% {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.8;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.chapter-group {
  margin-bottom: 32px;
}

.chapter-group:last-child {
  margin-bottom: 0;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 8px;
}

.chapter-label {
  font-weight: bold;
  color: #d4af37;
  font-size: 1rem;
}

.chapter-progress {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.levels-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.level-node {
  aspect-ratio: 1;
  background: rgba(0, 0, 0, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  padding: 8px;
}

.level-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.level-current {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.2);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.4);
}

.level-passed {
  border-color: rgba(34, 197, 94, 0.8);
  border-width: 2px;
  background: rgba(34, 197, 94, 0.15);
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.3);
}

.level-claimed {
  border-color: #4caf50;
  background: rgba(76, 175, 80, 0.2);
}

.level-locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.level-number {
  font-size: 1.25rem;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 4px;
}

.level-status {
  position: absolute;
  top: 4px;
  right: 4px;
}

.status-badge {
  display: inline-block;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
}

.status-badge.current {
  background: #3b82f6;
  color: white;
}

.status-badge.passed {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.status-badge.claimed {
  background: #4caf50;
  color: white;
}

.level-difficulty {
  font-size: 0.625rem;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

/* 右侧关卡详情面板 - 占界面1/3 */
.stage-detail-panel {
  position: fixed;
  top: 0;
  right: -33.333%;
  width: 33.333%;
  min-width: 400px;
  height: 100vh;
  background: rgba(15, 23, 42, 0.98);
  backdrop-filter: blur(15px);
  border-left: 2px solid rgba(139, 92, 246, 0.4);
  box-shadow: -4px 0 30px rgba(0, 0, 0, 0.6);
  transition: right 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  overflow-y: auto;
  overflow-x: hidden;
}

.stage-detail-panel.panel-visible {
  right: 0;
}

.panel-content {
  padding: 40px 32px;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}

.panel-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: #ffffff;
  font-size: 1.3rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}

.panel-close:hover {
  background: rgba(239, 68, 68, 0.4);
  border-color: rgba(239, 68, 68, 0.6);
  color: #f87171;
  transform: rotate(90deg);
}

/* 关卡标题区域 */
.stage-title-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px;
  background: rgba(139, 92, 246, 0.15);
  border: 2px solid rgba(139, 92, 246, 0.3);
  border-radius: 12px;
}

.stage-combat-icon {
  font-size: 3rem;
  filter: drop-shadow(0 0 10px rgba(139, 92, 246, 0.6));
}

.stage-title-info {
  flex: 1;
}

.stage-combat-label {
  font-size: 1.5rem;
  font-weight: bold;
  color: #a78bfa;
  margin-bottom: 8px;
  text-shadow: 0 0 10px rgba(139, 92, 246, 0.5);
}

.stage-name-label {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

/* INFO 描述框 */
.stage-info-box {
  margin-bottom: 32px;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.info-box-label {
  padding: 12px 16px;
  background: rgba(139, 92, 246, 0.2);
  border-bottom: 1px solid rgba(139, 92, 246, 0.3);
  font-size: 0.875rem;
  font-weight: bold;
  color: #a78bfa;
  letter-spacing: 2px;
}

.info-box-content {
  padding: 20px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.8;
  font-size: 0.95rem;
  text-align: justify;
}

.info-box-content p {
  margin: 0;
}

/* 预览区域 */
.stage-preview-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 32px;
}

.preview-item {
  display: flex;
  flex-direction: column;
}

.preview-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.preview-icon {
  font-size: 1rem;
}

.preview-content {
  flex: 1;
  min-height: 120px;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: rgba(255, 255, 255, 0.5);
}

.preview-icon-large {
  font-size: 2.5rem;
  margin-bottom: 8px;
  opacity: 0.6;
}

.preview-text {
  font-size: 0.75rem;
  text-align: center;
}

.enemy-preview {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(0, 0, 0, 0.5));
}

/* 奖励预览 */
.reward-preview {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.15), rgba(0, 0, 0, 0.5));
}

.reward-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.reward-item:hover {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.5);
  transform: translateX(4px);
}

.reward-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(245, 158, 11, 0.2);
  border-radius: 8px;
}

.reward-info {
  flex: 1;
}

.reward-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
}

.reward-value {
  font-size: 1.25rem;
  font-weight: bold;
  color: #fbbf24;
  text-shadow: 0 0 8px rgba(245, 158, 11, 0.5);
}

/* 未知节点预览 */
.unknown-preview {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(0, 0, 0, 0.5));
  border-color: rgba(139, 92, 246, 0.4);
}

/* 底部操作按钮 */
.stage-action-section {
  margin-top: auto;
  padding-top: 24px;
}

.stage-start-btn {
  width: 100%;
  padding: 20px 32px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.8), rgba(124, 58, 237, 0.9));
  border: 2px solid rgba(139, 92, 246, 0.6);
  border-radius: 12px;
  color: #ffffff;
  font-size: 1.25rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.4);
  position: relative;
  overflow: hidden;
}

.stage-start-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.stage-start-btn:hover:not(:disabled)::before {
  left: 100%;
}

.stage-start-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.9), rgba(124, 58, 237, 1));
  border-color: rgba(139, 92, 246, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 6px 30px rgba(139, 92, 246, 0.6);
}

.stage-start-btn:active:not(:disabled) {
  transform: translateY(0);
}

.stage-start-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.stage-start-btn.unknown-btn {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.9), rgba(124, 58, 237, 1));
  border-color: rgba(139, 92, 246, 0.8);
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.6);
}

.stage-start-btn.unknown-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(139, 92, 246, 1), rgba(124, 58, 237, 1));
  box-shadow: 0 6px 30px rgba(139, 92, 246, 0.8);
}

.start-btn-icon {
  font-size: 2rem;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.5));
}

.start-btn-text {
  font-size: 1.5rem;
  letter-spacing: 2px;
}

.start-btn-subtext {
  font-size: 0.875rem;
  opacity: 0.8;
  font-weight: normal;
}


/* 响应式设计 */
@media (max-width: 1200px) {
  .explore-content {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    order: -1;
  }
  
  .log-card {
    max-height: 300px;
  }
}

@media (max-width: 768px) {
  .explore-header {
    padding: 16px 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-stats {
    width: 100%;
    justify-content: space-between;
  }
  
  .explore-content {
    padding: 20px;
  }
  
  .stage-card,
  .map-card {
    padding: 20px;
  }
  
  .stage-info-grid {
    grid-template-columns: 1fr;
  }
  
  .levels-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .action-group {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }
}
</style>
