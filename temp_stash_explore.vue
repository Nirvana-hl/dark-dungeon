<template>
  <view class="explore-container" :style="exploreBgStyle">
    <!-- 顶部标题栏 -->
    <view class="explore-header">
      <view class="header-content">
        <view class="header-title">
          <h1 class="title-text">⚔️ 地牢探索</h1>
          <p class="title-subtitle">挑战暗黑地牢，征服未知领域</p>
        </view>
      </view>
    </view>

    <view class="explore-content">
      <!-- 遮罩层 - 点击上半部分空白区域可关闭面板 -->
      <view 
        v-if="selectedStageLevel" 
        class="panel-overlay" 
        @click="closeStageDetail"
      ></view>

      <!-- 主地图区域 -->
      <view class="main-section">
        <!-- 关卡地图 -->
        <view class="map-card">
          <scroll-view 
            class="map-scroll-area" 
            v-if="routeNodes && routeNodes.length > 0"
            scroll-y
            :scroll-top="autoScrollInitialized ? undefined : scrollTop"
            @touchstart="handleMapTouchStart"
            @touchmove="handleMapTouchMove"
            @touchend="handleMapTouchEnd"
            @mousedown="handleMapMouseDown"
            @mousemove="handleMapMouseMove"
            @mouseup="handleMapMouseUp"
            @scroll="handleMapScroll"
          >
            <view class="map-content vertical-route" :style="mapContentStyle">
            <!-- Canvas-only route rendering (兼容小程序) -->
            <canvas
              canvas-id="routeCanvas"
              :style="`position:absolute;left:0;top:0;width:100%;height:${mapHeight}px;`"
              :height="mapHeight"
              :width="mapWidth"
            />
            
            <!-- SVG 路线层（用于 H5 / DOM 环境，保证线条随 map-content 一起滚动） -->
            <svg
              class="route-lines"
              width="100%"
              :viewBox="`0 0 ${mapWidth} ${(mapHeight as any).value}`"
              preserveAspectRatio="none"
              :style="`position:absolute;left:0;top:0;width:100%;height:${(mapHeight as any).value}px;pointer-events:none;`"
              xmlns="http://www.w3.org/2000/svg"
              aria-hidden="true"
            >
              <g>
                <path
                  v-for="(d, idx) in svgPaths"
                  :key="`svg-path-${idx}`"
                  :d="d"
                  class="route-path"
                  fill="none"
                />
              </g>
            </svg>
            
            <!-- 节点层 -->
              <view class="route-nodes">
                <!-- 门动画（基于第5关位置） -->
                <view :style="doorWrapperStyle" class="door-wrapper">
                  <DoorAnimation :open="doorOpen" :width="doorWidth" :height="doorHeight" />
                </view>
              <view
                v-for="(node, index) in routeNodes"
                :key="`node-${index}`"
                class="route-node-wrapper"
                  :style="getNodePosition(node, index)"
            >
                <!-- 主线关卡节点 -->
                <view v-if="node.type === 'main'" class="main-node">
                  <view
                    class="route-node"
                    :class="getLevelNodeClass(node.level as number)"
                    @click="selectLevel(node.level as number)"
                  >
                    <view class="node-icon">⚔️</view>
                    <view class="node-number">{{ node.level }}</view>
                    <view class="node-status">
                      <text v-if="levelStatus(node.level as number).claimed" class="status-badge claimed">✓</text>
                      <text v-else-if="levelStatus(node.level as number).passed" class="status-badge passed">✓</text>
                      <text v-else-if="levelStatus(node.level as number).isCurr" class="status-badge current">●</text>
                    </view>
                    <view class="node-difficulty">{{ stageInfo(node.level as number).difficulty }}</view>
                  </view>
                </view>

                <!-- 未知/分支节点 -->
                <view v-else class="main-node">
                  <view
                    class="route-node unknown-node"
                    @click="handleUnknownNodeClick"
                  >
                    <view class="node-icon unknown-icon">❓</view>
                    <view class="node-label">奇遇</view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          </scroll-view>
        </view>
    </view>

      <!-- 右侧关卡详情面板 -->
      <aside class="stage-detail-panel" :class="{ 'panel-visible': selectedStageLevel }">
        <view class="panel-content" v-if="selectedStageLevel">
          <!-- 关卡标题 -->
          <view class="stage-title-section">
            <view class="stage-combat-icon">⚔️</view>
            <view class="stage-title-info">
              <view class="stage-combat-label">作战</view>
              <view class="stage-name-label">{{ getStageInfo(selectedStageLevel!).name }}</view>
          </view>
            </view>

          <!-- INFO 描述框 -->
          <view class="stage-info-box">
            <view class="info-box-label">INFO</view>
            <view class="info-box-content">
              <p>{{ getStageInfo(selectedStageLevel!).desc || '暂无描述' }}</p>
              </view>
            </view>

          <!-- 主要内容区域 -->
          <view class="stage-main-content">
            <!-- 左侧：敌人情报和奖励 -->
            <view class="stage-left-section">
            <!-- 敌方情报 -->
              <view class="enemy-intel-card">
                <view class="enemy-intel-icon-large">🔍</view>
                <view class="enemy-intel-header" @click="showEnemyIntelModal = true">
                  <view class="enemy-intel-label-wrapper">
                    <text class="enemy-intel-icon">🔍</text>
                    <text class="enemy-intel-label">敌方情报</text>
        </view>
              </view>
    </view>

            <!-- 通关奖励 -->
              <view class="reward-card" v-if="selectedStageLevel">
                <view class="reward-label-header">🎁 通关奖励</view>
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

            <!-- 右侧：开战按钮 -->
            <view class="stage-right-section">
            <button 
              class="stage-start-btn" 
                @click="startStage()" 
              :disabled="inBattle"
            >
                <text class="start-btn-icon">⚔️</text>
                <text class="start-btn-text">開戰</text>
                <text class="start-btn-subtext">×作战</text>
              </button>
            </view>
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

    <!-- 敌人情报弹窗 -->
    <view v-if="showEnemyIntelModal" class="event-modal-backdrop" @click.self="showEnemyIntelModal = false">
      <view class="event-modal">
        <view class="event-modal-header">
          <text class="event-modal-icon">🔍</text>
          <view class="event-modal-title">敌方情报</view>
          <button class="event-modal-close" @click="showEnemyIntelModal = false">✕</button>
        </view>
        <view class="event-modal-body">
          <view class="enemy-intel-modal-content">
            <view class="preview-placeholder">
              <view class="preview-icon-large">👹</view>
              <view class="preview-text">敌人信息</view>
            </view>
          </view>
        </view>
        <view class="event-modal-footer">
          <button class="event-modal-btn" @click="showEnemyIntelModal = false">知道了</button>
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
import DoorAnimation from '@/components/DoorAnimation.vue'

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
const showEnemyIntelModal = ref(false) // 控制敌人情报弹窗的显示
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
const dragStartY = ref(0)

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
  showEnemyIntelModal.value = false
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
  showEnemyIntelModal.value = false // 重置敌人情报弹窗状态
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

// 地图拖拽功能 - 鼠标事件（小程序使用 scroll-view 自带滚动）
function handleMapMouseDown(event: any) {
  // 记录起始 Y，用于判断是否为拖拽以避免误触点击
  try {
    dragStartY.value = event?.clientY || (event?.pageY ?? 0)
    isDragging.value = false
  } catch (e) {}
}

function handleMapMouseMove(event: any) {
  try {
    const y = event?.clientY || (event?.pageY ?? 0)
    if (Math.abs(y - (dragStartY.value || 0)) > 8) {
      isDragging.value = true
    }
  } catch (e) {}
}

function handleMapMouseUp() {
  // 小幅延迟，避免与 click 事件冲突
  setTimeout(() => { isDragging.value = false }, 50)
}

// 地图拖拽功能 - 触摸事件（小程序使用 scroll-view 自带滚动）
function handleMapTouchStart(event: any) {
  try {
    const t = event?.touches?.[0] || event?.changedTouches?.[0]
    dragStartY.value = t ? (t.clientY || t.pageY || 0) : 0
    isDragging.value = false
  } catch (e) {}
}

function handleMapTouchMove(event: any) {
  try {
    const t = event?.touches?.[0]
    const y = t ? (t.clientY || t.pageY || 0) : 0
    if (Math.abs(y - (dragStartY.value || 0)) > 8) {
      isDragging.value = true
    }
  } catch (e) {}
}

function handleMapTouchEnd() {
  // 小幅延迟后复位 isDragging，避免阻断 click 判定
  setTimeout(() => { isDragging.value = false }, 100)
}

function handleMapScroll(event: any) {
  try {
    const st = event?.detail?.scrollTop
    if (typeof st === 'number') {
      scrollTop.value = st
    }
  } catch (e) {}
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

// 获取节点位置（重新设计：简单的垂直布局，交叉分布）
function getNodePosition(node: RouteNode, index: number): any {
  // 使用基于层与层内索引的像素位置：垂直使用像素（保证均匀分布），水平使用百分比（适配容器宽度）
  const pos = getNodePixelPosition(node, index)
  const topPx = pos ? pos.y : (mapHeight as any).value / 2

  // 计算层内横向比例（更稳健地使用百分比定位，避免因容器缩放导致像素定位错位）
  const nodesInThisLayer = routeNodes.value.filter(n => n.layer === node.layer)
  const count = Math.max(1, nodesInThisLayer.length)
  const idx = node.indexInLayer
  // 如果该层包含三个节点，则将它们在更宽的区间内分布以增加彼此间距（避开左右边缘）
  let leftPercent: number
  if (count === 3) {
    const ratioIdx = count > 1 ? idx / (count - 1) : 0 // 0, 0.5, 1
    const min = 12 // 最左边百分比
    const max = 88 // 最右边百分比
    leftPercent = Math.round((min + ratioIdx * (max - min)) * 100) / 100
  } else {
    const ratio = (idx + 1) / (count + 1) // (1..count) -> evenly spaced between 0..1
    leftPercent = Math.round(ratio * 10000) / 100 // 保留两位小数的百分比
  }

  return {
    top: `${topPx}px`,
    left: `${leftPercent}%`,
    transform: 'translateX(-50%)'
  }
}

// 路线节点数据结构（支持分层分支与未知节点）
interface RouteNode {
  // 类型：'main' 表示有关卡的主线节点，'unknown' 表示未知/奇遇分支节点
  type: 'main' | 'unknown'
  // 仅当 type === 'main' 时存在关卡编号
  level?: number
  // 层级索引（从 0 底部到 4 顶部）
  layer: number
  // 在该层内的位置索引（用于水平均匀分布）
  indexInLayer: number
}

// 生成路线节点（按层生成，支持分支/未知节点）
// 从下往上 5 层：第1层（本章首关）、第2层（2个未知）、第3层（1个未知）、第4层（2个未知）、第5层（本章末关）
const chapterStartLevel = computed(() => (chapter.value - 1) * 5 + 1)
const chapterEndLevel = computed(() => Math.min(chapterStartLevel.value + 4, maxLevel))
const routeNodes = computed<RouteNode[]>(() => {
  try {
    // 构建层数组：在每一层中放置未知节点与必要的主线节点（保证原有首关/末关保持不变）
    const layers: Array<Array<Partial<RouteNode>>> = [
      [{ type: 'main', level: chapterStartLevel.value }],                 // layer 0 (bottom) - 本章首关
      // layer1: 在两个未知分支之间加入本章第2关主线节点（居中）
      [{ type: 'unknown' }, { type: 'main', level: chapterStartLevel.value + 1 }, { type: 'unknown' }],
      // layer2: 一个未知 + 本章第3关主线节点（右侧）
      [{ type: 'unknown' }, { type: 'main', level: chapterStartLevel.value + 2 }],
      // layer3: 在两个未知分支之间加入本章第4关主线节点（居中）
      [{ type: 'unknown' }, { type: 'main', level: chapterStartLevel.value + 3 }, { type: 'unknown' }],
      [{ type: 'main', level: chapterEndLevel.value }]                     // layer 4 (top) - 本章末关
    ]

    const nodes: RouteNode[] = []
    for (let layer = 0; layer < layers.length; layer++) {
      const row = layers[layer]
      for (let idx = 0; idx < row.length; idx++) {
        const base = row[idx]
        nodes.push({
          type: (base.type as 'main' | 'unknown'),
          level: (base as any).level,
          layer,
          indexInLayer: idx
        })
      }
    }
    
    return nodes
  } catch (error) {
    console.error('生成路线节点失败:', error)
    return []
  }
})

// 节点间距与地图高度配置（底部向上）
// 缩小节点间距以使 1-5 关无须滑动即可全部显示
const nodeSpacing = 80 // px，每关之间的垂直间距（缩小）
const nodeOffset = 12 // px，地图顶部/底部留白（较小）
// 统一地图宽高，保证 SVG 和节点层对齐
const mapWidth = 400
const mapHeight = computed(() => {
  const calc = routeNodes.value.length * nodeSpacing + nodeOffset * 2
  try {
    const sysInfo = typeof (uni as any).getSystemInfoSync === 'function'
      ? (uni as any).getSystemInfoSync()
      : { windowHeight: 800 }
    const visible = (sysInfo.windowHeight || 800) * 0.68 // match .map-scroll-area height (68vh)
    // Ensure mapHeight is larger than visible area so user can scroll to reveal upper nodes.
    // 增加 buffer 使得第四/第五关更容易显现。
    return Math.max(calc, Math.round(visible + 400))
  } catch (e) {
    return Math.max(calc, 800)
  }
})

// 滚动位置（初始位置），默认滚动到地图底部附近以展示第1关
const scrollTop = ref(0)
// 是否已完成自动初始化滚动（避免在用户滚动时被重置）
const autoScrollInitialized = ref(false)
// 地图内容样式（包含背景图，当显示 1-5 区块时用 tansuo1.png）
const mapContentStyle = computed(() => {
  const h = (mapHeight as any).value
  const styleObj: Record<string, any> = {
    height: `${h}px`,
  }
  // 不再在 map-content 内设置背景（背景改为占据整个探索界面）
  return styleObj as any
})

// 探索页面整体背景（当显示本章区块时使用 tansuo1.png，占据整个 explore 界面）
const exploreBgStyle = computed(() => {
  if (routeNodes.value.length > 0 && routeNodes.value[0].level === chapterStartLevel.value) {
    return {
      backgroundImage: "url('/static/tansuo1.png')",
      backgroundSize: 'cover',
      backgroundRepeat: 'no-repeat',
      backgroundPosition: 'center top'
    } as any
  }
  return {} as any
})

// 门动画控制
const doorOpen = ref(false)
const doorWidth = 360
const doorHeight = 320

// 找到本章末关的像素位置，用于把门定位在其上方居中
const doorNodeIndex = computed(() => routeNodes.value.findIndex(n => n.level === chapterEndLevel.value))
const doorPixel = computed(() => {
  if (doorNodeIndex.value < 0) return { x: mapWidth / 2, y: (mapHeight as any).value / 2 }
  const p = getNodePixelPosition(routeNodes.value[doorNodeIndex.value], doorNodeIndex.value)
  return p ?? { x: mapWidth / 2, y: (mapHeight as any).value / 2 }
})

const doorWrapperStyle = computed(() => {
  const p = doorPixel.value
  const left = (p.x || mapWidth / 2) - doorWidth / 2
  const top = (p.y || ((mapHeight as any).value / 2)) - doorHeight - 24
  return {
    position: 'absolute',
    left: `${left}px`,
    top: `${top}px`,
    width: `${doorWidth}px`,
    height: `${doorHeight}px`,
    pointerEvents: 'none'
  } as any
})

// 当 mapHeight 变化时，将 scrollTop 滚动到接近底部（确保第1关可见）
watch(mapHeight, (h) => {
  try {
    // 只在首次初始化时自动滚到接近底部，避免覆盖用户手动滚动
    if (autoScrollInitialized.value) return
    // 使用设备窗口高度与 CSS 的 .map-scroll-area 高度比例（60vh）计算可视高度
    const sysInfo = typeof (uni as any).getSystemInfoSync === 'function'
      ? (uni as any).getSystemInfoSync()
      : { windowHeight: 800 }
    const visibleEstimate = (sysInfo.windowHeight || 800) * 0.6
    scrollTop.value = Math.max(0, (h as number) - visibleEstimate)
    autoScrollInitialized.value = true
  } catch (e) {
    // 兜底：使用默认估算
    const visibleEstimate = 500
    scrollTop.value = Math.max(0, (h as number) - visibleEstimate)
    autoScrollInitialized.value = true
  }
})

// 监听 passedLevels，当检测到本章末关通关时打开门动画
watch(passedLevels, (newArr) => {
  try {
    if (newArr.includes(chapterEndLevel.value) && !doorOpen.value && !inBattle.value && !isDragging.value) {
      doorOpen.value = true
      // 动画后可在此执行后续逻辑（如自动切换区块）
      setTimeout(() => {
        // 目前仅展示动画，后续可在此加入跳转逻辑
      }, 1200)
    }
  } catch (e) {
    console.error('door watch error', e)
  }
})


// 计算节点布局：将关卡区间延展到门口上方（topGap），并在该范围内均匀分布节点
const nodesTopGap = computed(() => {
  // 以门高度为参考，确保节点延伸到门口附近
  // 如果门高度较大，减去一部分以让节点不覆盖门体
  const extra = Math.max(0, doorHeight - 240)
  return nodeOffset + 40 + extra
})

// 获取节点的像素坐标（根据节点类型和关卡编号），均匀分布在 [topGap, mapHeight - nodeOffset] 区间
function getNodePixelPosition(node: RouteNode, index: number): { x: number, y: number } | null {
  // 所有节点都有 layer 与 indexInLayer
  if (node.layer === undefined || node.indexInLayer === undefined) return null

  const numLayers = Math.max(1, Math.max(...routeNodes.value.map(n => n.layer)) + 1)
  const topGap = (nodesTopGap as any).value
  const bottomY = (mapHeight as any).value - nodeOffset
  const span = Math.max(1, bottomY - topGap)

  // 层间距按层数均匀分布（layer 0 对应底部）
  const layerSpacing = numLayers > 1 ? span / (numLayers - 1) : 0
  const y = Math.round(bottomY - node.layer * layerSpacing)

  // 水平分布：在该层内按 (idx+1)/(count+1) 的比例均匀分布（居中）
  const nodesInThisLayer = routeNodes.value.filter(n => n.layer === node.layer)
  const count = Math.max(1, nodesInThisLayer.length)
  const idx = node.indexInLayer
  const ratio = (idx + 1) / (count + 1)
  const x = Math.round(mapWidth * ratio)

  return { x, y }
}

// 获取从当前节点到后续节点的路径（纵向布局：从下往上），返回多条线段
// 连接算法：为相邻两层生成非交叉的随机连接，保证每个中间层节点有入有出
function connectLayerPairs(lowerIndices: number[], upperIndices: number[], rng: () => number): Array<{ from: number; to: number }> {
  const edges: Array<{ from: number; to: number }> = []
  const n = lowerIndices.length
  const m = upperIndices.length

  if (n === 0 || m === 0) return edges

  if (n <= m) {
    // 把上层 m 个节点划分为 n 个连续分组，每组至少 1 个
    const sizes = Array.from({ length: n }, () => 1)
    let extra = m - n
    while (extra > 0) {
      const r = Math.floor(rng() * n)
      sizes[r]++
      extra--
    }

    let cursor = 0
    for (let i = 0; i < n; i++) {
      const groupSize = sizes[i]
      for (let k = 0; k < groupSize; k++) {
        const upIdx = upperIndices[cursor + k]
        edges.push({ from: lowerIndices[i], to: upIdx })
      }
      cursor += groupSize
    }
  } else {
    // n > m: 把下层 n 个节点划分为 m 个连续分组，每组至少 1 个，组内所有下层节点指向同一个上层节点
    const sizes = Array.from({ length: m }, () => 1)
    let extra = n - m
    while (extra > 0) {
      const r = Math.floor(rng() * m)
      sizes[r]++
      extra--
    }

    let cursor = 0
    for (let j = 0; j < m; j++) {
      const groupSize = sizes[j]
      for (let k = 0; k < groupSize; k++) {
        const lowIdx = lowerIndices[cursor + k]
        edges.push({ from: lowIdx, to: upperIndices[j] })
      }
      cursor += groupSize
    }
  }

  return edges
}

// 预计算所有层之间的边（非交叉、随机分配），并且按来源索引分组
// 创建一个可复现的伪随机生成器（LCG）
function createLCGRandom(seed: number) {
  let s = seed >>> 0
  return () => {
    s = (s * 1664525 + 1013904223) >>> 0
    return s / 4294967296
  }
}

function getOrCreateSeedForChapter(): number {
  const key = `explore_route_seed_chapter_${chapterStartLevel.value}`
  try {
    const stored = uni.getStorageSync(key)
    if (stored) return Number(stored)
  } catch (e) {}
  // 生成新 seed 并保存
  const newSeed = Date.now() ^ Math.floor(Math.random() * 1e9)
  try { uni.setStorageSync(key, String(newSeed)) } catch (e) {}
  return newSeed
}

// 持久化生成的边集合，避免在某些环境下 computed 未按预期触发
const routeEdgesRef = ref<Array<{ from: number; to: number }>>([])

function buildRouteEdgesOnce() {
  const nodes = routeNodes.value
  try { console.log('[Explore] buildRouteEdgesOnce - nodes.length', nodes.length) } catch (e) {}
  if (!nodes || nodes.length === 0) {
    routeEdgesRef.value = []
    return
  }

  const byLayer: Record<number, number[]> = {}
  for (let i = 0; i < nodes.length; i++) {
    const layer = nodes[i].layer
    if (!byLayer[layer]) byLayer[layer] = []
    byLayer[layer].push(i)
  }

  // 使用章节固定 seed 生成可复现的随机连线
  const seed = getOrCreateSeedForChapter()
  const rng = createLCGRandom(seed)

  // 如果已经持久化了 edges（JSON），优先读取并使用，保证刷新不变
  try {
    const edgesKey = `explore_route_edges_chapter_${chapterStartLevel.value}`
    const stored = (uni as any).getStorageSync(edgesKey)
    if (stored) {
      const parsed = JSON.parse(stored)
      if (Array.isArray(parsed) && parsed.length > 0) {
        routeEdgesRef.value = parsed
        try { console.log('[Explore] loaded persisted routeEdges', parsed) } catch (e) {}
        return
      }
    }
  } catch (e) {
    // ignore parse errors and fallback to generate
  }

  const layerKeys = Object.keys(byLayer).map(k => Number(k))
  if (layerKeys.length === 0) {
    routeEdgesRef.value = []
    try { console.log('[Explore] buildRouteEdgesOnce - no layers') } catch (e) {}
    return
  }

  const maxLayer = Math.max(...layerKeys)
  const allEdges: Array<{ from: number; to: number }> = []
  for (let L = 0; L < maxLayer; L++) {
    const lower = byLayer[L] || []
    const upper = byLayer[L + 1] || []
    const edges = connectLayerPairs(lower, upper, rng)
    allEdges.push(...edges)
  }

  routeEdgesRef.value = allEdges
  try { console.log('[Explore] built routeEdges', allEdges) } catch (e) {}
  // persist edges for this chapter so refresh keeps the same route
  try {
    const edgesKey = `explore_route_edges_chapter_${chapterStartLevel.value}`;
    (uni as any).setStorageSync(edgesKey, JSON.stringify(allEdges));
  } catch (e) {
    console.warn('[Explore] persist routeEdges failed', e)
  }
  // trigger immediate draw after edges built (ensure canvas shows lines)
  setTimeout(() => {
    try { drawRoutesOnCanvas() } catch (e) { console.error('[Explore] draw trigger error', e) }
  }, 60)
}

// 如果 routeNodes 已存在，则立即构建；并在 routeNodes 变化时尝试一次构建
watch(routeNodes, (newNodes) => {
  if (newNodes && newNodes.length > 0 && routeEdgesRef.value.length === 0) {
    buildRouteEdgesOnce()
  }
}, { immediate: true })

// Canvas drawing for mini program compatibility
async function drawRoutesOnCanvas() {
  try {
    // ensure edges built
    if (!routeEdgesRef.value || routeEdgesRef.value.length === 0) return
    // prepare positions array
    const positions = routeNodes.value.map((n, i) => getNodePixelPosition(n, i) || { x: 0, y: 0 })

    // Draw using precomputed map-space positions to avoid DOM query race conditions
    // To align canvas with DOM nodes we need the displayed container size (may be scaled).
    const query: any = (uni as any).createSelectorQuery()
    query.select('.map-content').boundingClientRect((mapRect: any) => {
      try {
        const cw = mapRect?.width || mapWidth
        const ch = mapRect?.height || (mapHeight as any).value
        const scaleX = cw / mapWidth
        const scaleY = ch / (mapHeight as any).value

        const ctx: any = (uni as any).createCanvasContext('routeCanvas')
        if (!ctx) return

        ctx.clearRect && ctx.clearRect(0, 0, cw, ch)

        // approximate node radius (route-node is 72px wide in CSS), scaled
        const nodeRadius = Math.round(36 * Math.max(scaleX, scaleY))

        // scale positions to displayed pixels
        const scaledPositions = positions.map(p => ({ x: Math.round(p.x * scaleX), y: Math.round(p.y * scaleY) }))

        for (const e of routeEdgesRef.value) {
          const fromPos = scaledPositions[e.from] || { x: 0, y: 0 }
          const toPos = scaledPositions[e.to] || { x: 0, y: 0 }

          let sx = fromPos.x, sy = fromPos.y, ex = toPos.x, ey = toPos.y
          if (fromPos.y > toPos.y) {
            sy = fromPos.y - nodeRadius
            ey = toPos.y + nodeRadius
          } else {
            sy = fromPos.y + nodeRadius
            ey = toPos.y - nodeRadius
          }

          const midY = Math.round((sy + ey) / 2)
          const c1x = sx, c1y = midY
          const c2x = ex, c2y = midY

          ctx.beginPath && ctx.beginPath()
          ctx.moveTo && ctx.moveTo(sx, sy)
          ctx.bezierCurveTo && ctx.bezierCurveTo(c1x, c1y, c2x, c2y, ex, ey)
          ctx.setStrokeStyle ? ctx.setStrokeStyle('#ffffff') : (ctx.strokeStyle = '#ffffff')
          ctx.setLineWidth ? ctx.setLineWidth(4 * Math.max(scaleX, scaleY)) : (ctx.lineWidth = 4)
          ctx.stroke && ctx.stroke()

          // draw arrow head
          const t1 = 0.98
          const bezierPoint = (t: number) => {
            const u = 1 - t
            const x = Math.round(u * u * u * sx + 3 * u * u * t * c1x + 3 * u * t * t * c2x + t * t * t * ex)
            const y = Math.round(u * u * u * sy + 3 * u * u * t * c1y + 3 * u * t * t * c2y + t * t * t * ey)
            return { x, y }
          }
          const pt1 = bezierPoint(t1)
          const pt2 = { x: ex, y: ey }
          const dx = pt2.x - pt1.x
          const dy = pt2.y - pt1.y
          const ang = Math.atan2(dy, dx)

          ctx.save && ctx.save()
          ctx.translate && ctx.translate(ex, ey)
          ctx.rotate && ctx.rotate(ang)
          ctx.beginPath && ctx.beginPath()
          ctx.moveTo && ctx.moveTo(0, 0)
          ctx.lineTo && ctx.lineTo(-8 * Math.max(scaleX, scaleY), -12 * Math.max(scaleX, scaleY))
          ctx.lineTo && ctx.lineTo(8 * Math.max(scaleX, scaleY), -12 * Math.max(scaleX, scaleY))
          ctx.closePath && ctx.closePath()
          ctx.setFillStyle ? ctx.setFillStyle('#ffffff') : (ctx.fillStyle = '#ffffff')
          ctx.fill && ctx.fill()
          ctx.restore && ctx.restore()
        }

        ctx.draw && ctx.draw(true)
      } catch (e) {
        console.error('[Explore] drawRoutesOnCanvas mapRect callback error', e)
      }
    }).exec && query.exec()
  } catch (e) {
    console.error('[Explore] drawRoutesOnCanvas error', e)
  }
}

// Redraw when edges or positions change
watch([routeEdgesRef, routeNodes, mapHeight], () => {
  // delay until DOM updates
  setTimeout(() => {
    drawRoutesOnCanvas()
    updateSVGPaths()
  }, 50)
})

// 当滚动位置变化时也重绘，保证线条跟随节点（固定相对位置）
watch(scrollTop, () => {
  // 小幅延迟，合并频繁滚动事件
  setTimeout(() => {
    drawRoutesOnCanvas()
    updateSVGPaths()
  }, 30)
})

// 预计算每个节点的像素位置以供模板使用（避免在模板中多次调用函数）
const nodePositions = computed(() => {
  return routeNodes.value.map((n, i) => {
    return getNodePixelPosition(n, i) || { x: 0, y: 0 }
  })
})

// SVG 路径字符串缓存（以 DOM 像素为单位，保证与节点像素位置严格对齐）
const svgPaths = ref<string[]>([])

// 使用 DOM 测量来生成 SVG path D（以 displayed centers 为基准）
function updateSVGPaths() {
  try {
    if (!routeEdgesRef.value || routeEdgesRef.value.length === 0) {
      svgPaths.value = []
      return
    }
    const query: any = (uni as any).createSelectorQuery()
    query.select('.map-content').boundingClientRect((mapRect: any) => {
      try {
        query.selectAll('.route-node').boundingClientRect((nodeRects: any[]) => {
          try {
            const displayedCenters = nodeRects.map((r) => ({
              x: Math.round((r.left - mapRect.left) + r.width / 2),
              y: Math.round((r.top - mapRect.top) + r.height / 2),
              radius: Math.round(Math.max(r.width, r.height) / 2)
            }))
            const paths: string[] = []
            for (const e of routeEdgesRef.value) {
              const from = displayedCenters[e.from] || { x: 0, y: 0, radius: 0 }
              const to = displayedCenters[e.to] || { x: 0, y: 0, radius: 0 }
              let sx = from.x, sy = from.y, ex = to.x, ey = to.y
              if (from.y > to.y) {
                sy = from.y - from.radius
                ey = to.y + to.radius
              } else {
                sy = from.y + from.radius
                ey = to.y - to.radius
              }
              const midY = Math.round((sy + ey) / 2)
              const d = `M ${sx} ${sy} C ${sx} ${midY} ${ex} ${midY} ${ex} ${ey}`
              paths.push(d)
            }
            svgPaths.value = paths
          } catch (e) {
            console.error('[Explore] updateSVGPaths nodeRects error', e)
          }
        }).exec && query.exec()
      } catch (e) {
        console.error('[Explore] updateSVGPaths mapRect callback error', e)
      }
    }).exec && query.exec()
  } catch (e) {
    console.error('[Explore] updateSVGPaths error', e)
  }
}

// 允许手动重新随机化（覆盖持久化的 edges），供开发或用户触发
function regenerateRouteEdgesForChapter() {
  try {
    const key = `explore_route_edges_chapter_${chapterStartLevel.value}`;
    (uni as any).removeStorageSync && (uni as any).removeStorageSync(key);
  } catch (e) {}
  // force rebuild
  routeEdgesRef.value = []
  setTimeout(() => buildRouteEdgesOnce(), 50)
}

// debug points removed (compute inline in template to avoid template property warnings)

// 辅助：按来源节点分组
function getEdgesByFrom(): Record<number, number[]> {
  const edges = routeEdgesRef.value
  const m: Record<number, number[]> = {}
  for (const e of edges) {
    if (!m[e.from]) m[e.from] = []
    m[e.from].push(e.to)
  }
  return m
}

function getPathsFromNode(index: number): string[] {
  const nodes = routeNodes.value
  if (!nodes || index >= nodes.length) return []
  const node = nodes[index]
  const paths: string[] = []

  const currentPos = getNodePixelPosition(node, index)
  if (!currentPos) {
    try { console.log('[Explore] getPathsFromNode - no currentPos for index', index, node) } catch (e) {}
    return []
  }

  const edgesByFrom = getEdgesByFrom()
  const tos = edgesByFrom[index] || []
  try { console.log('[Explore] getPathsFromNode', index, 'tos', tos) } catch (e) {}
  for (const toIndex of tos) {
    const nextPos = getNodePixelPosition(nodes[toIndex], toIndex)
    if (!nextPos) continue
    const currentX = currentPos.x
    const currentY = currentPos.y
    const nextX = nextPos.x
    const nextY = nextPos.y
    const midY = (currentY + nextY) / 2
    const d = `M ${currentX} ${currentY} C ${currentX} ${midY} ${nextX} ${midY} ${nextX} ${nextY}`
    try { console.log('[Explore] pathD', index, '->', toIndex, d) } catch (e) {}
    paths.push(d)
  }

  return paths
}

// 获取路径样式类（只有主线路径）
function getPathClass(node: RouteNode): string {
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
  height: 100vh;
  background: #0a0a0f;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 地图背景图片 - 随滚动移动 */

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
  background: transparent;
  /* 缩小顶部标题高度以腾出更多地图空间 */
  padding: 12rpx 16rpx;
  position: relative;
  z-index: 10;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24rpx;
}

.header-title {
  flex: 1;
}

.title-text {
  font-size: 48rpx; /* 缩小主标题 */
  font-weight: 700;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8rpx 0;
  letter-spacing: 2rpx;
}

.title-subtitle {
  font-size: 20rpx; /* 缩小副标题 */
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
  flex: 1;
  margin: 0 auto;
  padding: 0 32rpx;
  display: flex;
  gap: 0;
  position: relative;
  z-index: 10;
  overflow: hidden;
}

/* 遮罩层 - 只覆盖上半部分，透明，点击可关闭面板 */
.panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 50vh;
  background: transparent;
  z-index: 999;
  cursor: pointer;
}

/* 主区域 */
.main-section {
  flex: 1;
  width: 100%;
  height: 100%;
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
  height: 100%;
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

.map-scroll-area {
  flex: 1;
  width: 100%;
  /* 恢复为可滑动区域，保留上方留白以容纳门动画 */
  height: 74vh; /* 略微加大可滑动区域 */
  max-height: 84vh;
  /* 将关卡区域上移，紧贴标题下方显示 */
  padding-top: 16px;
  overflow-y: auto;
}

.map-content {
  position: relative;
  width: 100%;
  z-index: 2;
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

/* 纵向路线地图 */
.vertical-route {
  position: relative;
  width: 100%;
  z-index: 2;
}

.route-lines {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 500;
  overflow: visible;
}

.route-path {
  opacity: 1 !important;
  transition: opacity 0.3s ease;
  /* 强制白色线条，增粗以便在暗背景上更清晰 */
  stroke: #ffffff !important;
  stroke-width: 4 !important;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.route-nodes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000; /* 提高 z-index，确保节点始终显示在遮罩层（z-index: 999）之上 */
}

.route-node-wrapper {
  position: absolute;
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
  flex-direction: row;
  justify-content: center;
  align-items: center;
  width: 400px; /* 固定宽度，与地图宽度一致 */
  position: relative;
}

.branch-node {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
}

.branch-node.branch-left {
  /* 左分支在左侧房间中心：x = 0.18 * 400 = 72px（向左微调使其更居中） */
  left: 72px;
  transform: translateX(-50%);
}

.branch-node.branch-right {
  /* 右分支在右侧房间中心：x = 0.68 * 400 = 272px（向左微调使其更居中） */
  left: 272px;
  transform: translateX(-50%);
}

.route-node {
  position: relative;
  width: 72px;
  height: 72px;
  background: rgba(0, 0, 0, 0.65);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.4);
}

.route-node:hover {
  transform: scale(1.06);
  box-shadow: 0 5px 16px rgba(212, 175, 55, 0.45);
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
  font-size: 1.2rem;
  margin-bottom: 2px;
}

.node-number {
  font-size: 0.9rem;
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
  bottom: 0;
  left: 0;
  width: 100%;
  height: 50vh;
  background: rgba(15, 23, 42, 0.98);
  backdrop-filter: blur(15px);
  border-top: 2rpx solid rgba(139, 92, 246, 0.4);
  box-shadow: 0 -4rpx 30rpx rgba(0, 0, 0, 0.6);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  overflow-y: auto;
  overflow-x: hidden;
  transform: translateY(100%);
}

.stage-detail-panel.panel-visible {
  transform: translateY(0);
}

.panel-content {
  padding: 20rpx 32rpx;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}

.panel-close {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: #ffffff;
  font-size: 32rpx;
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
  gap: 16rpx;
  margin-bottom: 16rpx;
  padding: 16rpx 20rpx;
  background: rgba(139, 92, 246, 0.15);
  border: 2rpx solid rgba(139, 92, 246, 0.3);
  border-radius: 12rpx;
}

.stage-combat-icon {
  font-size: 48rpx;
  filter: drop-shadow(0 0 10px rgba(139, 92, 246, 0.6));
}

.stage-title-info {
  flex: 1;
}

.stage-combat-label {
  font-size: 28rpx;
  font-weight: bold;
  color: #a78bfa;
  margin-bottom: 4rpx;
  text-shadow: 0 0 10px rgba(139, 92, 246, 0.5);
}

.stage-name-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

/* INFO 描述框 */
.stage-info-box {
  margin-bottom: 8rpx;
  background: rgba(0, 0, 0, 0.4);
  border: 1rpx solid rgba(139, 92, 246, 0.3);
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.info-box-label {
  padding: 0;
  background: rgba(139, 92, 246, 0.2);
  border: none;
  border-radius: 4rpx;
  padding: 4rpx 12rpx;
  font-size: 24rpx;
  font-weight: bold;
  color: #a78bfa;
  letter-spacing: 2rpx;
  white-space: nowrap;
}

.info-box-content {
  padding: 0;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  font-size: 24rpx;
  text-align: left;
  flex: 1;
}

.info-box-content p {
  margin: 0;
}

/* 主要内容区域 */
.stage-main-content {
  display: flex;
  align-items: stretch;
  gap: 24rpx;
  flex: 1;
  margin-top: 8rpx;
  min-height: 0;
}

/* 左侧区域 */
.stage-left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  min-height: 0;
}

/* 敌方情报卡片 */
.enemy-intel-card {
  background: rgba(0, 0, 0, 0.5);
  border: 1rpx solid rgba(139, 92, 246, 0.3);
  border-radius: 12rpx;
  padding: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  flex: 0 0 auto;
  justify-content: center;
  min-height: 0;
  max-height: 40%;
}

.enemy-intel-icon-large {
  font-size: 40rpx;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
  margin-bottom: 2rpx;
}

.enemy-intel-header {
  padding: 6rpx 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease;
  border-radius: 8rpx;
  width: 100%;
}

.enemy-intel-header:active {
  background: rgba(139, 92, 246, 0.1);
}

.enemy-intel-label-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.enemy-intel-icon {
  font-size: 24rpx;
}

.enemy-intel-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

.enemy-intel-modal-content {
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200rpx;
}

/* 奖励卡片 */
.reward-card {
  background: rgba(0, 0, 0, 0.5);
  border: 1rpx solid rgba(139, 92, 246, 0.3);
  border-radius: 12rpx;
  padding: 16rpx;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow-y: auto;
}

.reward-label-header {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  margin-bottom: 12rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

/* 右侧区域 */
.stage-right-section {
  display: flex;
  align-items: stretch;
  justify-content: center;
  min-width: 200rpx;
}

.stage-right-section .stage-start-btn {
  width: 100%;
  min-width: 180rpx;
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.8), rgba(124, 58, 237, 0.9));
  border: 2rpx solid rgba(139, 92, 246, 0.6);
  border-radius: 12rpx;
  color: #ffffff;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  box-shadow: 0 4rpx 20rpx rgba(139, 92, 246, 0.4);
  position: relative;
  overflow: hidden;
}

.preview-item {
  display: flex;
  flex-direction: column;
}

.preview-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.preview-icon {
  font-size: 28rpx;
}

.preview-content {
  flex: 1;
  min-height: 80rpx;
  background: rgba(0, 0, 0, 0.5);
  border: 1rpx solid rgba(139, 92, 246, 0.3);
  border-radius: 8rpx;
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
  font-size: 48rpx;
  margin-bottom: 4rpx;
  opacity: 0.6;
}

.preview-text {
  font-size: 20rpx;
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
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 10rpx;
  background: rgba(245, 158, 11, 0.1);
  border: 1rpx solid rgba(245, 158, 11, 0.3);
  border-radius: 8rpx;
  transition: all 0.2s ease;
}

.reward-item:hover {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.5);
  transform: translateX(4px);
}

.reward-icon {
  font-size: 28rpx;
  width: 36rpx;
  height: 36rpx;
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
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 2rpx;
}

.reward-value {
  font-size: 24rpx;
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
  padding-top: 16rpx;
}

.stage-start-btn {
  width: 100%;
  padding: 16rpx 32rpx;
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
  font-size: 36rpx;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.5));
}

.start-btn-text {
  font-size: 28rpx;
  letter-spacing: 2rpx;
}

.start-btn-subtext {
  font-size: 20rpx;
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
