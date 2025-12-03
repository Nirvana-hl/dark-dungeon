<template>
  <div class="explore-container">
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

    <!-- 奖励弹窗 -->
    <Transition name="modal">
      <div v-if="rewardOpen" class="reward-modal-overlay" @click.self="rewardOpen = false">
        <div class="reward-modal">
          <div class="modal-header">
            <div class="modal-icon">🎁</div>
            <h3 class="modal-title">关卡奖励</h3>
          </div>
          <div class="modal-content">
            <p class="reward-message">恭喜通关第 {{ rewardLevel }} 关！</p>
            <div class="reward-list">
              <div class="reward-item">
                <span class="reward-icon">🪙</span>
                <span class="reward-label">金币</span>
                <span class="reward-value">+{{ rewardGold }}</span>
              </div>
              <div class="reward-item">
                <span class="reward-icon">⭐</span>
                <span class="reward-label">经验</span>
                <span class="reward-value">+{{ rewardExp }}</span>
              </div>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn-secondary" @click="rewardOpen = false">稍后领取</button>
            <button class="btn-primary" @click="claimReward">立即领取</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWalletStore } from '@/stores/wallet'
import { useCharactersStore } from '@/stores/characters'
import { useGameStore } from '@/stores/game'

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
    passedLevels.value = [1, 2, 3]
    claimedLevels.value = [1, 2]
  } catch {}
}

async function upsertProgress(level: number, changes: Partial<{ passed: boolean; claimed: boolean }>) {
  try {
    if (changes.passed && !passedLevels.value.includes(level)) {
      passedLevels.value.push(level)
    }
    if (changes.claimed && !claimedLevels.value.includes(level)) {
      claimedLevels.value.push(level)
    }
  } catch (error) {
    console.log('Progress updated locally')
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

function startBattleLog() {
  const msgs = [
    '前锋与守门者短兵相接！',
    '游侠侧袭，造成精准打击。',
    '法师吟唱完成，火球命中！',
    '战士格挡成功，反击造成伤害。',
    '敌方发起冲锋，被阻截！'
  ]
  stopBattleLog()
  battleTimer = setInterval(() => {
    const i = Math.floor(Math.random() * msgs.length)
    log('战报：' + msgs[i])
  }, 1000)
}

function stopBattleLog() {
  if (battleTimer) { clearInterval(battleTimer); battleTimer = null }
}

const rewardOpen = ref(false)
const rewardGold = ref(0)
const rewardExp = ref(0)
const rewardLevel = ref(0)

async function startStage() {
  if (inBattle.value) return
  inBattle.value = true
  log(`进入关卡：${stage.value.name}（难度：${stage.value.difficulty}）`)
  startBattleLog()
  
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
  const { gold, exp } = getRewards(stage.value.difficulty)
  rewardGold.value = gold
  rewardExp.value = exp
  rewardLevel.value = currentLevel.value
  rewardOpen.value = true
}

async function claimReward() {
  const lvl = rewardLevel.value
  rewardOpen.value = false
  
  try {
    if (wallet && wallet.add) {
      await wallet.add(rewardGold.value)
    }
    log(`奖励领取成功：金币 +${rewardGold.value}`)
  } catch (error) {
    log('金币奖励发放失败，使用本地记录')
  }
  
  const selected = chars.selected
  if (selected) {
    const prevExp = Number(((selected as any).attrs?.exp) ?? 0)
    const totalExp = prevExp + rewardExp.value
    const levelUp = Math.floor(totalExp / 100)
    const newExp = totalExp % 100
    const prevLevel = Number(((selected as any).attrs?.level) ?? 1)
    const newLevel = Math.max(1, prevLevel + levelUp)
    try {
      const selectedAttrs = (selected as any).attrs
      if (selectedAttrs) {
        selectedAttrs.exp = newExp
        selectedAttrs.level = newLevel
      }
      log(`角色经验 +${rewardExp.value}${levelUp > 0 ? `，升级 +${levelUp}` : ''}`)
    } catch (error) {
      log(`角色经验发放失败：${error}`)
    }
  } else {
    log('未选择角色，经验未发放。')
  }
  
  try {
    if (!claimedLevels.value.includes(lvl)) {
      claimedLevels.value.push(lvl)
      await upsertProgress(lvl, { claimed: true })
    }
  } catch (error) {
    log('进度更新失败，使用本地存储')
  }
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
  
  const victory = route.query.victory === '1'
  const lvl = Number(route.query.level ?? 0)
  if (victory && lvl > 0) {
    currentLevel.value = lvl
    if (!passedLevels.value.includes(lvl)) {
      passedLevels.value.push(lvl)
      try {
        await upsertProgress(lvl, { passed: true })
      } catch (error) {
        log('进度更新失败，使用本地存储')
      }
    }
    const diff = stageInfo(lvl).difficulty
    const { gold, exp } = getRewards(diff)
    rewardGold.value = gold
    rewardExp.value = exp
    rewardLevel.value = lvl
    rewardOpen.value = true
    log(`战斗胜利返回：第 ${lvl} 关奖励待领取`)
  }
})

onUnmounted(() => stopBattleLog())
</script>

<style scoped>
.explore-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
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
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.05);
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

/* 奖励弹窗 */
.reward-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.reward-modal {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid rgba(212, 175, 55, 0.5);
  border-radius: 20px;
  padding: 32px;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-header {
  text-align: center;
  margin-bottom: 24px;
}

.modal-icon {
  font-size: 4rem;
  margin-bottom: 12px;
}

.modal-title {
  font-size: 1.75rem;
  font-weight: bold;
  color: #d4af37;
  margin: 0;
}

.modal-content {
  margin-bottom: 24px;
}

.reward-message {
  text-align: center;
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 20px;
}

.reward-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
}

.reward-icon {
  font-size: 1.5rem;
}

.reward-label {
  flex: 1;
  color: rgba(255, 255, 255, 0.8);
}

.reward-value {
  font-size: 1.25rem;
  font-weight: bold;
  color: #d4af37;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .reward-modal,
.modal-leave-active .reward-modal {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .reward-modal,
.modal-leave-to .reward-modal {
  transform: scale(0.9) translateY(-20px);
  opacity: 0;
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
