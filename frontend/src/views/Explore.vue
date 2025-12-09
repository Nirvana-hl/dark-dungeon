<template>
  <div class="explore-container">
    <!-- 返回首页按钮 -->
    <RouterLink to="/" class="back-to-home">
      <i class="fas fa-home"></i>
      返回首页
    </RouterLink>
    
    <!-- 顶部标题栏 -->
    <header class="explore-header">
      <div class="header-content">
        <div class="header-title">
          <h1 class="title-text">⚔️ 地牢探索</h1>
          <p class="title-subtitle">挑战暗黑地牢，征服未知领域</p>
        </div>
        <div class="header-stats">
          <div class="stat-card">
            <div class="stat-label">当前章节</div>
            <div class="stat-value">第 {{ chapter }} 章</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">关卡进度</div>
            <div class="stat-value">{{ passedLevels.length }} / {{ maxLevel }}</div>
          </div>
        </div>
      </div>
    </header>

    <div class="explore-content">
      <!-- 左侧主区域 -->
      <main class="main-section">
        <!-- 当前关卡信息卡片 -->
        <div class="stage-card">
          <div class="stage-header">
            <div class="stage-title-group">
              <div class="stage-number">第 {{ currentLevel }} 关</div>
              <h2 class="stage-name">{{ stage.name }}</h2>
            </div>
            <div class="stage-difficulty" :class="`difficulty-${stage.difficulty}`">
              <span class="difficulty-icon">{{ getDifficultyIcon(stage.difficulty) }}</span>
              <span class="difficulty-text">{{ stage.difficulty }}</span>
            </div>
          </div>

          <div class="stage-description">
            <p>{{ stage.desc }}</p>
          </div>

          <!-- 操作按钮组 -->
          <div class="action-group">
            <button 
              class="btn-primary btn-large"
              :disabled="inBattle" 
              @click="startStage"
            >
              <span class="btn-icon">⚡</span>
              <span>{{ inBattle ? '战斗中...' : '开始挑战' }}</span>
            </button>
            <button 
              class="btn-secondary"
              :disabled="!inBattle" 
              @click="completeStage"
            >
              <span class="btn-icon">✓</span>
              <span>完成关卡</span>
            </button>
            <div class="nav-buttons">
              <button 
                class="btn-nav"
                :disabled="inBattle || currentLevel <= 1" 
                @click="prevStage"
              >
                <span>←</span>
              </button>
              <button 
                class="btn-nav"
                :disabled="inBattle || currentLevel >= maxLevel" 
                @click="nextStage"
              >
                <span>→</span>
              </button>
            </div>
          </div>

          <!-- 关卡状态信息 -->
          <div class="stage-info-grid">
            <div class="info-item">
              <div class="info-icon">📊</div>
              <div class="info-content">
                <div class="info-label">关卡状态</div>
                <div class="info-value" :class="inBattle ? 'status-active' : 'status-idle'">
                  {{ inBattle ? '进行中' : '未开始' }}
                </div>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">📈</div>
              <div class="info-content">
                <div class="info-label">通关进度</div>
                <div class="info-value">{{ passedLevels.length }} / {{ maxLevel }}</div>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">🎁</div>
              <div class="info-content">
                <div class="info-label">预计奖励</div>
                <div class="info-value">{{ getRewards(stage.difficulty).gold }} 金币</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 关卡地图 -->
        <div class="map-card">
          <div class="map-header">
            <h3 class="map-title">关卡地图</h3>
            <div class="map-legend">
              <div class="legend-item">
                <div class="legend-color legend-current"></div>
                <span>当前</span>
              </div>
              <div class="legend-item">
                <div class="legend-color legend-passed"></div>
                <span>已通关</span>
              </div>
              <div class="legend-item">
                <div class="legend-color legend-claimed"></div>
                <span>已领取</span>
              </div>
            </div>
          </div>

          <div class="map-content">
            <!-- 按章节分组显示 -->
            <div 
              v-for="chapterNum in totalChapters" 
              :key="chapterNum"
              class="chapter-group"
            >
              <div class="chapter-header">
                <span class="chapter-label">第 {{ chapterNum }} 章</span>
                <span class="chapter-progress">
                  {{ getChapterProgress(chapterNum) }} / 5
                </span>
              </div>
              <div class="levels-grid">
                <div
                  v-for="level in getChapterLevels(chapterNum)"
                  :key="level"
                  class="level-node"
                  :class="getLevelNodeClass(level)"
                  @click="selectLevel(level)"
                >
                  <div class="level-number">{{ level }}</div>
                  <div class="level-status">
                    <span v-if="levelStatus(level).claimed" class="status-badge claimed">✓</span>
                    <span v-else-if="levelStatus(level).passed" class="status-badge passed">✓</span>
                    <span v-else-if="levelStatus(level).isCurr" class="status-badge current">●</span>
                  </div>
                  <div class="level-difficulty">{{ stageInfo(level).difficulty }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 右侧辅助信息 -->
      <aside class="sidebar">
        <!-- 消息日志 -->
        <div class="log-card">
          <div class="log-header">
            <h3 class="log-title">📜 战斗日志</h3>
            <button class="btn-clear" @click="clearLogs">清空</button>
          </div>
          <div class="log-content">
            <div v-if="logs.length === 0" class="log-empty">
              <div class="empty-icon">📝</div>
              <p>暂无消息</p>
            </div>
            <div v-else class="log-list">
              <div 
                v-for="(log, index) in logs" 
                :key="index"
                class="log-item"
              >
                <span class="log-time">{{ log.split(' ')[0] }}</span>
                <span class="log-message">{{ log.substring(log.indexOf(' ') + 1) }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useWalletStore } from '@/stores/wallet'
import { useCharactersStore } from '@/stores/characters'
import { useGameStore } from '@/stores/game'
import { stageProgressApi } from '@/lib/api'

type Stage = {
  level: number
  name: string
  difficulty: '普通' | '困难' | '噩梦'
  desc?: string
}

const router = useRouter()
const route = useRoute()
const wallet = useWalletStore()
const chars = useCharactersStore()
const game = useGameStore()

const currentLevel = ref(1)
const maxLevel = 30
const chapter = computed(() => Math.floor((currentLevel.value - 1) / 5) + 1)
const totalChapters = computed(() => Math.ceil(maxLevel / 5))
const inBattle = ref(false)
const logs = ref<string[]>([])
const passedLevels = ref<number[]>([])
const claimedLevels = ref<number[]>([])

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
      
      log(`已加载关卡进度：${passedLevels.value.length} 个关卡已通过`)
      
      // 如果没有进度记录，确保第1关是解锁的（但未通过）
      if (passedLevels.value.length === 0) {
        log('首次进入，初始化第1关')
        currentLevel.value = 1
      } else {
        // 设置当前关卡为最后一个已通过的关卡的下一个，或第1关
        const maxPassed = Math.max(...passedLevels.value)
        currentLevel.value = Math.min(maxPassed + 1, maxLevel)
      }
    } else {
      log('加载关卡进度失败，使用默认值')
      currentLevel.value = 1
    }
  } catch (error) {
    console.error('加载关卡进度失败:', error)
    log('加载关卡进度失败，使用默认值')
    // 如果加载失败，默认从第1关开始
    currentLevel.value = 1
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
        log(`关卡 ${level} 已标记为通过`)
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

function log(msg: string) {
  logs.value.unshift(new Date().toLocaleTimeString() + ' ' + msg)
  if (logs.value.length > 50) logs.value.pop()
}

function clearLogs() {
  logs.value = []
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

function getRewards(difficulty: Stage['difficulty']) {
  if (difficulty === '普通') return { gold: 50, exp: 50 }
  if (difficulty === '困难') return { gold: 100, exp: 100 }
  return { gold: 150, exp: 150 }
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
  log(`进入关卡：${stage.value.name}（难度：${stage.value.difficulty}）`)
  
  try {
    await ensureStages()
    await upsertProgress(currentLevel.value, { passed: false })
    if (game && game.configureEncounter) {
      game.configureEncounter(stage.value.difficulty)
    }
    if (game && game.loadEnemyDeck) {
      await game.loadEnemyDeck(currentLevel.value)
    }
    router.push({ path: '/battle', query: { level: String(currentLevel.value) } })
  } catch (error) {
    log('启动关卡失败，使用模拟模式')
    router.push({ path: '/battle', query: { level: String(currentLevel.value) } })
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
    log('进度更新失败，使用本地存储')
  }
  
  log(`胜利：第 ${currentLevel.value} 关 - ${stage.value.name}`)
}

function nextStage() {
  if (inBattle.value) return
  if (currentLevel.value >= maxLevel) {
    log('已到达最终关卡，无法继续前进。')
    return
  }
  currentLevel.value++
  log(`进入下一关：第 ${currentLevel.value} 关`)
}

function prevStage() {
  if (inBattle.value) return
  if (currentLevel.value <= 1) return
  currentLevel.value--
  log(`返回上一关：第 ${currentLevel.value} 关`)
}

function selectLevel(level: number) {
  if (inBattle.value) return
  currentLevel.value = level
  log(`切换到第 ${level} 关`)
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

onMounted(async () => {
  log('闯关模式已启用：每5关为一章，逐关推进直至终章。')
  try {
    await wallet.loadWallets().catch(() => {})
  } catch (error) {
    log('钱包初始化失败，使用模拟模式')
  }
  
  try {
    await ensureStages()
    await loadStages()
    await loadProgress()
  } catch (error) {
    log('数据加载失败，使用本地模拟数据')
  }
  
  // 处理战斗胜利返回的情况（进度已在 Game.vue 中保存，这里只需要刷新显示）
  const victory = route.query.victory === '1'
  const lvl = Number(route.query.level ?? 0)
  if (victory && lvl > 0) {
    currentLevel.value = lvl
    // 重新加载进度以确保显示最新状态
    await loadProgress()
    log(`战斗胜利返回：第 ${lvl} 关`)
  }
})

onUnmounted(() => stopBattleLog())
</script>

<style scoped>
.explore-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
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

/* 顶部标题栏 */
.explore-header {
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid rgba(212, 175, 55, 0.3);
  padding: 24px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.header-title {
  flex: 1;
}

.title-text {
  font-size: 2rem;
  font-weight: bold;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.title-subtitle {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-card {
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 12px 20px;
  text-align: center;
  min-width: 120px;
}

.stat-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: bold;
  color: #d4af37;
}

/* 主内容区域 */
.explore-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
}

/* 关卡信息卡片 */
.stage-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  margin-bottom: 24px;
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

.difficulty-普通 {
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
  color: #4caf50;
}

.difficulty-困难 {
  background: rgba(255, 152, 0, 0.2);
  border: 1px solid rgba(255, 152, 0, 0.4);
  color: #ff9800;
}

.difficulty-噩梦 {
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
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
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
  max-height: 600px;
  overflow-y: auto;
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

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.log-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  height: fit-content;
  max-height: calc(100vh - 200px);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.log-title {
  font-size: 1.25rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
}

.btn-clear {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  padding: 6px 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-clear:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.log-content {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

.log-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.5);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 12px;
  opacity: 0.5;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-item {
  padding: 10px 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  border-left: 3px solid rgba(212, 175, 55, 0.5);
  font-size: 0.875rem;
  line-height: 1.5;
  transition: all 0.2s ease;
}

.log-item:hover {
  background: rgba(0, 0, 0, 0.4);
  border-left-color: rgba(212, 175, 55, 0.8);
}

.log-time {
  color: rgba(255, 255, 255, 0.5);
  margin-right: 8px;
  font-size: 0.75rem;
}

.log-message {
  color: rgba(255, 255, 255, 0.9);
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
