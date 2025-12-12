<script setup lang="ts">
import { computed, onMounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BattleField from '@/components/BattleField.vue'
import CardItem from '@/components/CardItem.vue'
import { useGameStore } from '@/stores/game'
import type { Card } from '@/stores/game'
import { storeToRefs } from 'pinia'
import { useWalletStore } from '@/stores/wallet'
import { useCampStore } from '@/stores/camp'
import apiClient from '@/lib/api'
import { CurrencyType } from '@/types'
import { stageProgressApi, stageApi } from '@/lib/api'
import { soundManager } from '@/utils/soundManager'

const route = useRoute()
const router = useRouter()

const level = computed(() => Number(route.query.level ?? 0))
const chapter = computed(() => (level.value ? Math.floor((level.value - 1) / 5) + 1 : 0))

const game = useGameStore()
const { hand, canPlay, winner, mana, manaMax, deckExhausted, deck } = storeToRefs(game)
const enemyDifficulty = computed(() => game.enemyDifficulty)

const isEndingTurn = ref(false)
const showExitConfirm = ref(false)
const draggingEquipCard = ref<Card | null>(null)
const draggingSpellCard = ref<Card | null>(null)
const selectedEquipCard = ref<Card | null>(null)
const selectedCharacterCard = ref<Card | null>(null)
const selectedSpellCard = ref<Card | null>(null)
const draggingCharacterCard = ref<Card | null>(null)
const remainingDeck = computed(() => deck.value.length)

// 结算画面相关
const showVictoryModal = ref(false)
const showDefeatModal = ref(false)
const victoryReward = ref({
  gold: 0,
  exp: 0,
  stress: 0
})
const defeatInfo = ref({
  stressIncrease: 0,
  hpRestored: 0,
  maxHp: 0
})
const playerCharacter = computed(() => useCampStore().playerCharacter)
const wallet = useWalletStore()

function onPlay(id: string) {
  game.playCard(id)
}

function startEquipDrag(card: Card) {
  // 仅记录装备卡的拖拽状态
  if (card.type !== 'equipment') return
  draggingEquipCard.value = card
  // 开始拖拽时关闭详情面板，避免遮挡
  selectedEquipCard.value = null
}

function endEquipDrag() {
  draggingEquipCard.value = null
}

function handleEquipToMinion(payload: { minionId: string }) {
  if (!draggingEquipCard.value) return
  game.equipCardToMinion(draggingEquipCard.value.id, payload.minionId)
  draggingEquipCard.value = null
}

function showEquipDetails(card: Card) {
  if (card.type !== 'equipment') return
  selectedEquipCard.value = card
}

function closeEquipDetails() {
  selectedEquipCard.value = null
}

function showCharacterDetails(card: Card) {
  if (card.type !== 'character') return
  selectedCharacterCard.value = card
}

function closeCharacterDetails() {
  selectedCharacterCard.value = null
}

function showSpellDetails(card: Card) {
  if (card.type !== 'spell') return
  selectedSpellCard.value = card
}

function closeSpellDetails() {
  selectedSpellCard.value = null
}

function startCharacterDrag(card: Card) {
  if (card.type !== 'character') return
  draggingCharacterCard.value = card
  // 开始拖拽时关闭详情面板，避免遮挡
  selectedCharacterCard.value = null
}

function endCharacterDrag() {
  draggingCharacterCard.value = null
}

function startSpellDrag(card: Card) {
  if (card.type !== 'spell') return
  draggingSpellCard.value = card
  // 开始拖拽时关闭详情面板，避免遮挡
  selectedSpellCard.value = null
}

function endSpellDrag(event?: DragEvent) {
  // 如果法术卡被拖拽离开手牌区，视为打出
  if (draggingSpellCard.value) {
    const card = draggingSpellCard.value
    
    // 检查拖拽结束时鼠标位置是否在手牌区域外
    if (event) {
      const handCardsElement = event.currentTarget?.closest('.hand-cards') as HTMLElement | null
      if (handCardsElement) {
        const rect = handCardsElement.getBoundingClientRect()
        const x = event.clientX
        const y = event.clientY
        
        // 如果鼠标位置在手牌区域外，视为打出
        if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
          // 检查是否有足够的法力值
          if (mana.value >= card.cost) {
            game.playCard(card.id)
            draggingSpellCard.value = null
            return
          }
        }
      }
    }
    
    // 如果拖拽结束但还在手牌区域内，清除状态
    draggingSpellCard.value = null
  }
}

// 处理手牌区域拖拽离开事件（作为备用检测）
function handleHandDragLeave(event: DragEvent) {
  // 检查是否有正在拖拽的法术卡
  if (draggingSpellCard.value) {
    const relatedTarget = event.relatedTarget as HTMLElement | null
    const handCardsElement = event.currentTarget as HTMLElement
    
    // 如果 relatedTarget 不在手牌区域内，视为离开
    if (relatedTarget && !handCardsElement.contains(relatedTarget)) {
      const card = draggingSpellCard.value
      // 检查是否有足够的法力值
      if (mana.value >= card.cost) {
        game.playCard(card.id)
        draggingSpellCard.value = null
      }
    }
  }
}

function handleDeployCard(payload: { cardId: string; position: number }) {
  // 调用 playCard 并传入位置参数
  game.playCard(payload.cardId, payload.position)
}

// 退出战斗
function exitBattle() {
  showExitConfirm.value = true
}

function confirmExit() {
  showExitConfirm.value = false
  const lv = level.value || 1
  router.push({ path: '/explore', query: { level: String(lv) } })
}

function cancelExit() {
  showExitConfirm.value = false
}

// 结束回合 - 带视觉反馈
async function endTurn() {
  if (!canPlay.value || isEndingTurn.value) return
  isEndingTurn.value = true
  game.endTurn()
  // 添加延迟以提供视觉反馈
  await new Promise(resolve => setTimeout(resolve, 500))
  isEndingTurn.value = false
}

// 根据关卡参数配置敌方难度并开局（从数据库加载玩家/敌方手牌）
onMounted(async () => {
  // 预加载音效（非阻塞）
  soundManager.preloadSounds().catch(err => {
    console.warn('[Game] 音效预加载失败:', err)
  })

  const lv = level.value || 1
  const diff = lv <= 10 ? '普通' : lv <= 20 ? '困难' : '噩梦'
  game.configureEncounter(diff as any)

  // 确保营地数据已加载（用于获取玩家血量）
  const campStore = useCampStore()
  if (!campStore.playerCharacter) {
    await campStore.fetchCampData()
  }

  // 从 Spring Boot API 加载用户数据
  await game.loadUserDeckFromDB()
  game.reset() // 先重置状态
  await game.loadEnemyDeck(lv) // 再加载敌人数据（包括敌人面板）
})

// 若关卡参数变化，重新配置并重新加载敌方手牌
watch(level, async (lv) => {
  const diff = lv && lv <= 10 ? '普通' : lv && lv <= 20 ? '困难' : '噩梦'
  game.configureEncounter(diff as any)
  
  // 确保营地数据已加载（用于获取玩家血量）
  const campStore = useCampStore()
  if (!campStore.playerCharacter) {
    await campStore.fetchCampData()
  }
  
  game.reset() // 先重置状态
  await game.loadEnemyDeck(lv || 1) // 再加载敌人数据（包括敌人面板）
})

// 计算奖励（从后端数据库获取）
async function calculateReward() {
  const lv = level.value || 1
  
  try {
    // 从后端获取关卡信息
    const response = await stageApi.getStageByNumber(lv)
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
          console.warn('[Game] 解析奖励池配置失败，使用默认值:', e)
        }
      }
      
      // 从奖励池获取奖励数据，如果没有则使用默认值
      const gold = rewardPool.gold || 50
      const exp = rewardPool.exp || 50
      // 压力值通常不在奖励池中，使用默认值或根据难度计算
      const diff = enemyDifficulty.value || '普通'
      let baseStress = 5
      if (diff === '困难') {
        baseStress = 8
      } else if (diff === '噩梦') {
        baseStress = 12
      }
      const stress = rewardPool.stress || baseStress
      
      console.log('[Game] 从后端获取奖励数据:', { gold, exp, stress, stageNumber: lv })
      return { gold, exp, stress }
    } else {
      console.warn('[Game] 获取关卡信息失败，使用默认奖励')
    }
  } catch (error) {
    console.error('[Game] 获取关卡奖励失败，使用默认奖励:', error)
  }
  
  // 降级方案：如果后端获取失败，使用默认计算逻辑
  const diff = enemyDifficulty.value || '普通'
  let baseGold = 50
  let baseExp = 50
  let baseStress = 5
  
  if (diff === '困难') {
    baseGold = 100
    baseExp = 100
    baseStress = 8
  } else if (diff === '噩梦') {
    baseGold = 150
    baseExp = 150
    baseStress = 12
  }
  
  const levelMultiplier = 1 + (lv - 1) * 0.1
  const gold = Math.floor(baseGold * levelMultiplier)
  const exp = Math.floor(baseExp * levelMultiplier)
  const stress = Math.floor(baseStress * levelMultiplier)
  
  return { gold, exp, stress }
}

// 应用奖励到角色
async function applyRewards() {
  const reward = victoryReward.value
  
  try {
    // 1. 增加金币
    if (reward.gold > 0) {
      await wallet.addCurrency(CurrencyType.GOLD, BigInt(reward.gold), 'battle_victory')
    }
    
    // 2. 更新角色血量、行动点和压力值
    // 重要：使用战斗中的 heroHP，而不是营地中的 currentHp，确保战斗扣除的血量不返回
    if (playerCharacter.value?.id) {
      const battleHp = game.heroHP // 使用战斗中的血量
      const currentActionPoints = playerCharacter.value.currentActionPoints
      const currentStress = Math.min(100, (playerCharacter.value.currentStress || 0) + reward.stress)
      
      console.log('[Game] 保存战斗后的血量:', {
        battleHp,
        previousHp: playerCharacter.value.currentHp,
        maxHp: playerCharacter.value.maxHp
      })
      
      // 保存战斗后的血量（战斗扣除的血量不返回）
      await apiClient.put(`/user-player-characters/${playerCharacter.value.id}`, {
        currentHp: battleHp, // 使用战斗中的血量
        currentActionPoints,
        currentStress
      })
      
      // 更新营地store中的血量，确保营地界面显示最新血量
      const campStore = useCampStore()
      campStore.updatePlayerCharacter({
        currentHp: battleHp
      })
    }
  } catch (error) {
    console.error('应用奖励失败:', error)
  }
}

// 确认结算，返回探索界面
async function confirmVictory() {
  await applyRewards()
  
  // 保存关卡进度
  const lv = Number(route.query.level ?? 1)
  try {
    await stageProgressApi.passStage(lv)
    console.log(`[Game] 关卡 ${lv} 已标记为通过`)
  } catch (error) {
    console.error('[Game] 保存关卡进度失败:', error)
  }
  
  showVictoryModal.value = false
  router.push({ path: '/explore', query: { level: String(lv), victory: '1' } })
}

// 确认失败结算，返回探索界面
function confirmDefeat() {
  showDefeatModal.value = false
  const lv = String(route.query.level ?? '1')
  router.push({ path: '/explore', query: { level: lv } })
}

// 监听胜负，显示结算画面
watch(winner, async (w) => {
  if (w === 'player') {
    // 从后端获取奖励数据
    victoryReward.value = await calculateReward()
    // 显示结算画面
    showVictoryModal.value = true
  } else if (w === 'enemy') {
    // 失败时计算失败信息（濒死机制已更新血量和压力值）
    const campStore = useCampStore()
    const playerChar = campStore.playerCharacter
    
    if (playerChar) {
      const maxHp = playerChar.maxHp || 100
      const halfHp = Math.floor(maxHp / 2)
      const currentStress = playerChar.currentStress || 0
      const previousStress = Math.max(0, currentStress - 50) // 估算之前的压力值
      
      defeatInfo.value = {
        stressIncrease: 50,
        hpRestored: halfHp,
        maxHp: maxHp
      }
    }
    
    // 显示失败结算画面
    showDefeatModal.value = true
  }
})
</script>

<template>
  <div class="battle-container">
    
    <!-- 顶部信息栏 -->
    <div v-if="level" class="battle-header">
      <div class="header-left">
        <div class="level-badge">
          <span class="level-icon">⚔️</span>
          <div class="level-info">
            <span class="level-text">第 {{ level }} 关</span>
            <span class="chapter-text">第 {{ chapter }} 章</span>
          </div>
        </div>
      </div>
      
      <div class="header-center">
        <div class="info-chip">
          <span class="chip-label">法力</span>
          <span class="chip-value">{{ mana }}/{{ manaMax }}</span>
        </div>
        <div class="info-chip">
          <span class="chip-label">手牌</span>
          <span class="chip-value">{{ hand.length }}/10</span>
        </div>
        <div class="info-chip">
          <span class="chip-label">牌库</span>
          <span class="chip-value">{{ remainingDeck }}</span>
        </div>
      </div>
      
      <div class="header-right">
        <button 
          class="exit-battle-btn"
          @click="exitBattle"
          title="退出战斗"
        >
          <span class="btn-icon">←</span>
          <span class="btn-text">退出</span>
        </button>
      </div>
    </div>

    <!-- 战斗场地 -->
    <div class="battle-main">
        <BattleField 
          :dragging-equip-card="draggingEquipCard"
          @equip-to-minion="handleEquipToMinion"
          @deploy-card="handleDeployCard"
        />
    </div>

    <!-- 底部操作区 -->
    <div class="battle-footer">
      <!-- 手牌区域 -->
      <div class="hand-section">
        <div class="hand-header">
          <div class="hand-title">
            <span class="hand-icon">🃏</span>
            <span>手牌 ({{ hand.length }}/10)</span>
            <div class="mana-display">
              <span class="mana-icon">💎</span>
              <span class="mana-value">{{ mana }}/{{ manaMax }}</span>
            </div>
          </div>
          <div class="hand-helpers">
          <div class="hand-hint">点击查看详情，拖拽部署角色</div>
            <div v-if="deckExhausted" class="deck-empty-badge" title="本场战斗无法再抽牌">
              <span class="badge-icon">⚠️</span>
              <span class="badge-text">牌库已耗尽</span>
            </div>
          </div>
        </div>
        <div 
          class="hand-cards"
          @dragleave="handleHandDragLeave"
        >
          <CardItem 
            v-for="c in hand" 
            :key="c.id" 
            :card="c" 
            @play="onPlay"
            @start-equip-drag="startEquipDrag"
            @end-equip-drag="endEquipDrag"
            @start-character-drag="startCharacterDrag"
            @end-character-drag="endCharacterDrag"
            @start-spell-drag="startSpellDrag"
            @end-spell-drag="(e) => endSpellDrag(e)"
            @show-equipment="showEquipDetails"
            @show-character="showCharacterDetails"
            @show-spell="showSpellDetails"
            :can-afford="mana >= c.cost"
          />
          <div v-if="hand.length === 0" class="empty-hand">
            <span class="empty-icon">📭</span>
            <span class="empty-text">手牌为空</span>
          </div>
        </div>

        <!-- 装备详情面板（点击装备卡时展示） -->
        <div v-if="selectedEquipCard" class="equip-details">
          <div class="equip-header">
            <span class="equip-title">装备详情</span>
            <button class="equip-close" @click="closeEquipDetails">✕</button>
          </div>
          <div class="equip-body">
            <div class="equip-name">{{ selectedEquipCard.name }}</div>
            <div class="equip-type">类型：装备 · 费用 {{ selectedEquipCard.cost }}</div>
            <div class="equip-effects">
              <span v-if="(selectedEquipCard as any).bonusAttack">
                攻击 +{{ (selectedEquipCard as any).bonusAttack }}
              </span>
              <span v-if="(selectedEquipCard as any).bonusHp">
                生命 +{{ (selectedEquipCard as any).bonusHp }}
              </span>
              <span v-if="(selectedEquipCard as any).bonusDefense">
                防御 +{{ (selectedEquipCard as any).bonusDefense }}
              </span>
              <span 
                v-if="!(selectedEquipCard as any).bonusAttack 
                      && !(selectedEquipCard as any).bonusHp 
                      && !(selectedEquipCard as any).bonusDefense"
              >
                暂无数值加成，可能为特殊效果装备
              </span>
            </div>
            <div class="equip-hint">
              提示：按住此装备拖到己方角色卡牌上，即可为该角色穿戴装备。
            </div>
          </div>
        </div>

        <!-- 角色卡详情面板（点击角色卡时展示） -->
        <div v-if="selectedCharacterCard" class="equip-details character-details">
          <div class="equip-header">
            <span class="equip-title">角色详情</span>
            <button class="equip-close" @click="closeCharacterDetails">✕</button>
          </div>
          <div class="equip-body">
            <div class="equip-name">{{ selectedCharacterCard.name }}</div>
            <div class="equip-type">类型：角色 · 费用 {{ selectedCharacterCard.cost }}</div>
            <div class="equip-effects">
              <span>攻击力：{{ selectedCharacterCard.attack ?? 0 }}</span>
              <span>生命值：{{ selectedCharacterCard.health ?? 0 }}</span>
            </div>
            <div class="equip-hint">
              提示：按住此角色卡拖到战场位置槽上，即可部署该角色。
            </div>
          </div>
        </div>

        <!-- 法术卡详情面板（点击法术卡时展示） -->
        <div v-if="selectedSpellCard" class="equip-details spell-details">
          <div class="equip-header">
            <span class="equip-title">法术详情</span>
            <button class="equip-close" @click="closeSpellDetails">✕</button>
          </div>
          <div class="equip-body">
            <div class="equip-name">{{ selectedSpellCard.name }}</div>
            <div class="equip-type">类型：法术 · 费用 {{ selectedSpellCard.cost }}</div>
            <div class="equip-effects">
              <span v-if="selectedSpellCard.effect === 'fireball3'">效果：造成3点伤害</span>
              <span v-else-if="selectedSpellCard.effect">效果：{{ selectedSpellCard.effect }}</span>
              <span v-else>效果：法术效果</span>
            </div>
            <div class="equip-hint">
              提示：点击"使用"按钮或直接点击卡牌即可使用此法术。
            </div>
            <div class="spell-actions">
              <button 
                class="use-spell-btn" 
                @click="onPlay(selectedSpellCard.id); closeSpellDetails()"
                :disabled="mana < selectedSpellCard.cost"
              >
                使用法术
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <button 
          class="end-turn-btn"
          :class="{ 
            'disabled': !canPlay || isEndingTurn,
            'processing': isEndingTurn
          }"
          :disabled="!canPlay || isEndingTurn"
          @click="endTurn"
        >
          <span class="btn-content">
            <span class="btn-icon-large">⏭️</span>
            <span class="btn-label">{{ isEndingTurn ? '处理中...' : '结束回合' }}</span>
          </span>
          <div v-if="isEndingTurn" class="btn-loading"></div>
        </button>
      </div>
    </div>

    <!-- 退出确认弹窗 -->
    <div v-if="showExitConfirm" class="modal-overlay" @click="cancelExit">
      <div class="exit-modal" @click.stop>
        <div class="modal-header">
          <h3>退出战斗</h3>
        </div>
        <div class="modal-content">
          <p>确定要退出当前战斗吗？</p>
          <p class="modal-warning">退出后将返回闯关界面，当前战斗进度将丢失。</p>
        </div>
        <div class="modal-actions">
          <button class="modal-btn cancel-btn" @click="cancelExit">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmExit">确认退出</button>
        </div>
      </div>
    </div>

    <!-- 战斗胜利结算弹窗 -->
    <div v-if="showVictoryModal" class="modal-overlay victory-overlay">
      <div class="victory-modal" @click.stop>
        <div class="victory-header">
          <div class="victory-icon">🏆</div>
          <h2 class="victory-title">战斗胜利！</h2>
          <p class="victory-subtitle">第 {{ level }} 关 · {{ enemyDifficulty }}</p>
        </div>
        
        <div class="victory-content">
          <!-- 奖励展示 -->
          <div class="reward-section">
            <h3 class="reward-title">获得奖励</h3>
            <div class="reward-list">
              <div class="reward-item gold-reward">
                <div class="reward-icon">🪙</div>
                <div class="reward-info">
                  <div class="reward-label">金币</div>
                  <div class="reward-value">+{{ victoryReward.gold }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 角色提升 -->
          <div class="character-progress-section">
            <h3 class="progress-title">角色提升</h3>
            <div class="progress-list">
              <div class="progress-item exp-progress">
                <div class="progress-icon">⭐</div>
                <div class="progress-info">
                  <div class="progress-label">经验值</div>
                  <div class="progress-value">+{{ victoryReward.exp }}</div>
                </div>
              </div>
              <div class="progress-item stress-progress">
                <div class="progress-icon">😰</div>
                <div class="progress-info">
                  <div class="progress-label">压力值</div>
                  <div class="progress-value">+{{ victoryReward.stress }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="victory-actions">
          <button class="victory-btn" @click="confirmVictory">
            <span class="btn-icon">✓</span>
            <span class="btn-text">确认</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 战斗失败结算弹窗 -->
    <div v-if="showDefeatModal" class="modal-overlay defeat-overlay">
      <div class="defeat-modal" @click.stop>
        <div class="defeat-header">
          <div class="defeat-icon">💀</div>
          <h2 class="defeat-title">战斗失败</h2>
          <p class="defeat-subtitle">第 {{ level }} 关 · {{ enemyDifficulty }}</p>
        </div>
        
        <div class="defeat-content">
          <!-- 濒死机制说明 -->
          <div class="near-death-section">
            <h3 class="near-death-title">⚠️ 濒死触发</h3>
            <p class="near-death-description">你的生命值归零，触发了濒死机制</p>
          </div>

          <!-- 状态变化 -->
          <div class="status-change-section">
            <h3 class="status-title">状态变化</h3>
            <div class="status-list">
              <div class="status-item stress-change">
                <div class="status-icon">😰</div>
                <div class="status-info">
                  <div class="status-label">压力值增加</div>
                  <div class="status-value negative">+{{ defeatInfo.stressIncrease }}</div>
                </div>
              </div>
              <div class="status-item hp-restore">
                <div class="status-icon">❤️</div>
                <div class="status-info">
                  <div class="status-label">生命值恢复</div>
                  <div class="status-value positive">{{ defeatInfo.hpRestored }}/{{ defeatInfo.maxHp }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="defeat-actions">
          <button class="defeat-btn" @click="confirmDefeat">
            <span class="btn-icon">✓</span>
            <span class="btn-text">确认</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.battle-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  position: relative;
  overflow-y: auto;
}

.battle-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 顶部信息栏 */
.battle-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  position: relative;
  z-index: 10;
}

.header-left, .header-center, .header-right {
  flex: 1;
  display: flex;
  align-items: center;
}

.header-center {
  justify-content: center;
  gap: 12px;
}

.header-right {
  justify-content: flex-end;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 12px;
}

.level-icon {
  font-size: 1.5rem;
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.level-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: #e2e8f0;
}

.chapter-text {
  font-size: 0.75rem;
  color: #94a3b8;
}

.info-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 14px;
  background: rgba(148, 163, 184, 0.12);
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 12px;
  min-width: 90px;
}

.chip-label {
  font-size: 0.75rem;
  color: #94a3b8;
  letter-spacing: 0.05em;
}

.chip-value {
  font-size: 1rem;
  font-weight: 700;
  color: #f4f4f5;
}

.exit-battle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #f87171;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.exit-battle-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.exit-battle-btn:active {
  transform: translateY(0);
}

.exit-battle-btn .btn-icon {
  font-size: 1rem;
  font-weight: bold;
}

.battle-main {
  flex: 1;
  min-height: 0;
  padding: 12px 24px;
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

.battle-main :deep(.battle-field) {
  min-height: 100%;
  box-sizing: border-box;
}

/* 底部操作区 */
.battle-footer {
  padding: 8px 24px 12px;
  background: rgba(15, 23, 42, 0.92);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  position: relative;
  z-index: 10;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.hand-section {
  flex: 1;
}

.hand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.hand-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  color: #e2e8f0;
}

.hand-icon {
  font-size: 1.125rem;
}

.mana-display {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  margin-left: 12px;
}

.mana-icon {
  font-size: 0.875rem;
}

.mana-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: #60a5fa;
}

.hand-hint {
  font-size: 0.75rem;
  color: #94a3b8;
}

.hand-helpers {
  display: flex;
  align-items: center;
  gap: 8px;
}

.deck-empty-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(248, 113, 113, 0.15);
  border: 1px solid rgba(248, 113, 113, 0.4);
  color: #fecaca;
  font-size: 0.75rem;
  font-weight: 600;
}

.deck-empty-badge .badge-icon {
  font-size: 0.9rem;
}

.hand-cards {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.3) transparent;
}

.hand-cards::-webkit-scrollbar {
  height: 6px;
}

.hand-cards::-webkit-scrollbar-track {
  background: transparent;
}

.hand-cards::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}

.hand-cards::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}

.equip-details {
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
  max-width: 420px;
}

.equip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.equip-title {
  font-size: 0.9rem;
  font-weight: 600;
}

.equip-close {
  background: transparent;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  font-size: 0.9rem;
}

.equip-close:hover {
  color: #e5e7eb;
}

.equip-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.8rem;
}

.equip-name {
  font-weight: 700;
  font-size: 0.95rem;
}

.equip-effects {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}

.equip-effects span {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.15);
  border: 1px solid rgba(34, 197, 94, 0.4);
  color: #bbf7d0;
}

.equip-hint {
  margin-top: 2px;
  font-size: 0.75rem;
  color: #9ca3af;
}

/* 法术卡详情面板 */
.spell-details {
  border-color: rgba(139, 92, 246, 0.4);
}

.spell-details .equip-effects span {
  background: rgba(139, 92, 246, 0.15);
  border-color: rgba(139, 92, 246, 0.4);
  color: #c4b5fd;
}

.spell-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.use-spell-btn {
  flex: 1;
  padding: 10px 16px;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}

.use-spell-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.use-spell-btn:active:not(:disabled) {
  transform: translateY(0);
}

.use-spell-btn:disabled {
  background: rgba(71, 85, 105, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
}

.empty-hand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 32px;
  color: #64748b;
}

.empty-icon {
  font-size: 2rem;
  opacity: 0.5;
}

.empty-text {
  font-size: 0.875rem;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;
  width: 220px;
}

.end-turn-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 160px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.end-turn-btn:hover:not(.disabled) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.end-turn-btn:active:not(.disabled) {
  transform: translateY(0);
}

.end-turn-btn.disabled {
  background: rgba(71, 85, 105, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
}

.end-turn-btn.processing {
  pointer-events: none;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 2;
}

.btn-icon-large {
  font-size: 1.25rem;
}

.btn-label {
  font-size: 1rem;
}

.btn-loading {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: loading-slide 1.5s infinite;
}

@keyframes loading-slide {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* 退出确认弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fade-in 0.2s ease;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.exit-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  padding: 24px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  animation: slide-up 0.3s ease;
}

@keyframes slide-up {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  margin-bottom: 16px;
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #e2e8f0;
  margin: 0;
}

.modal-content {
  margin-bottom: 24px;
}

.modal-content p {
  font-size: 0.875rem;
  color: #cbd5e1;
  margin: 0 0 8px 0;
  line-height: 1.6;
}

.modal-warning {
  color: #fbbf24 !important;
  font-size: 0.8125rem !important;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.modal-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: rgba(71, 85, 105, 0.5);
  color: #cbd5e1;
}

.cancel-btn:hover {
  background: rgba(71, 85, 105, 0.7);
}

.confirm-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .battle-header {
    padding: 10px 16px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .header-left, .header-center, .header-right {
    flex: none;
  }

  .header-center {
    order: 3;
    width: 100%;
    justify-content: center;
  }

  .level-badge {
    padding: 6px 12px;
  }

  .exit-battle-btn .btn-text {
    display: none;
  }

  .battle-footer {
    padding: 12px 16px;
  }

  .hand-cards {
    gap: 8px;
  }

  .end-turn-btn {
    min-width: 140px;
    padding: 12px 24px;
  }
}

@media (max-width: 480px) {
  .level-badge {
    padding: 6px 10px;
  }

  .level-icon {
    font-size: 1.25rem;
  }

  .level-text {
    font-size: 0.8125rem;
  }

  .chapter-text {
    font-size: 0.6875rem;
  }

  .mana-display {
    padding: 6px 12px;
  }

  .mana-text {
    font-size: 0.875rem;
  }

  .end-turn-btn {
    min-width: 120px;
    padding: 10px 20px;
  }

  .btn-label {
    font-size: 0.875rem;
  }
}

/* 战斗胜利结算弹窗 */
.victory-overlay {
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.victory-modal {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.95));
  border: 2px solid rgba(251, 191, 36, 0.5);
  border-radius: 24px;
  padding: 32px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), 0 0 40px rgba(251, 191, 36, 0.3);
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.victory-modal::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.1) 0%, transparent 70%);
  animation: rotate 10s linear infinite;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.victory-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.victory-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  animation: bounce 1s ease infinite;
  display: inline-block;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.victory-title {
  font-size: 2rem;
  font-weight: 700;
  color: #fbbf24;
  margin: 0 0 8px 0;
  text-shadow: 0 0 20px rgba(251, 191, 36, 0.5);
}

.victory-subtitle {
  font-size: 0.875rem;
  color: #94a3b8;
  margin: 0;
}

.victory-content {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.reward-section,
.character-progress-section {
  margin-bottom: 24px;
}

.reward-title,
.progress-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #e2e8f0;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reward-title::before,
.progress-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #fbbf24, #f59e0b);
  border-radius: 2px;
}

.reward-list,
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reward-item,
.progress-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.reward-item:hover,
.progress-item:hover {
  background: rgba(15, 23, 42, 0.8);
  border-color: rgba(148, 163, 184, 0.4);
  transform: translateX(4px);
}

.reward-icon,
.progress-icon {
  font-size: 2rem;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(251, 191, 36, 0.1);
  border-radius: 12px;
  flex-shrink: 0;
}

.gold-reward .reward-icon {
  background: rgba(251, 191, 36, 0.2);
}

.exp-progress .progress-icon {
  background: rgba(59, 130, 246, 0.2);
}

.stress-progress .progress-icon {
  background: rgba(239, 68, 68, 0.2);
}

.progress-info,
.reward-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-label,
.reward-label {
  font-size: 0.875rem;
  color: #94a3b8;
  font-weight: 500;
}

.progress-value,
.reward-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #e2e8f0;
}

.gold-reward .reward-value {
  color: #fbbf24;
}

.exp-progress .progress-value {
  color: #60a5fa;
}

.stress-progress .progress-value {
  color: #f87171;
}

.victory-actions {
  display: flex;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.victory-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.3);
}

.victory-btn:hover {
  background: linear-gradient(135deg, #fcd34d, #fbbf24);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(251, 191, 36, 0.4);
}

.victory-btn:active {
  transform: translateY(0);
}

.btn-icon {
  font-size: 1.25rem;
}

.btn-text {
  font-size: 1rem;
}

@media (max-width: 768px) {
  .victory-modal {
    padding: 24px;
    max-width: 90%;
  }

  .victory-icon {
    font-size: 3rem;
  }

  .victory-title {
    font-size: 1.5rem;
  }

  .reward-item,
  .progress-item {
    padding: 12px;
  }

  .reward-icon,
  .progress-icon {
    width: 40px;
    height: 40px;
    font-size: 1.5rem;
  }
}

/* 战斗失败结算弹窗 */
.defeat-overlay {
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

.defeat-modal {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.95));
  border: 2px solid rgba(239, 68, 68, 0.5);
  border-radius: 24px;
  padding: 32px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), 0 0 40px rgba(239, 68, 68, 0.3);
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.defeat-modal::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(239, 68, 68, 0.1) 0%, transparent 70%);
  animation: rotate 10s linear infinite;
}

.defeat-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.defeat-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  animation: pulse 2s ease-in-out infinite;
}

.defeat-title {
  font-size: 2rem;
  font-weight: 700;
  color: #f87171;
  margin-bottom: 8px;
  text-shadow: 0 0 20px rgba(239, 68, 68, 0.5);
}

.defeat-subtitle {
  font-size: 0.875rem;
  color: #94a3b8;
  font-weight: 500;
}

.defeat-content {
  position: relative;
  z-index: 1;
  margin-bottom: 32px;
}

.near-death-section {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.near-death-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #f87171;
  margin-bottom: 8px;
}

.near-death-description {
  font-size: 0.875rem;
  color: #cbd5e1;
  line-height: 1.5;
}

.status-change-section {
  margin-top: 24px;
}

.status-title {
  font-size: 1rem;
  font-weight: 600;
  color: #e2e8f0;
  margin-bottom: 16px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(30, 41, 59, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.status-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 41, 59, 0.8);
  border-radius: 8px;
}

.status-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-label {
  font-size: 0.8125rem;
  color: #94a3b8;
  font-weight: 500;
}

.status-value {
  font-size: 1.125rem;
  font-weight: 700;
}

.status-value.negative {
  color: #f87171;
}

.status-value.positive {
  color: #60a5fa;
}

.defeat-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.defeat-btn {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.8), rgba(220, 38, 38, 0.8));
  border: 1px solid rgba(239, 68, 68, 0.5);
  border-radius: 12px;
  padding: 12px 32px;
  font-size: 1rem;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.defeat-btn:hover {
  background: linear-gradient(135deg, rgba(239, 68, 68, 1), rgba(220, 38, 38, 1));
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
}

.defeat-btn:active {
  transform: translateY(0);
}

.defeat-btn .btn-icon {
  font-size: 1.125rem;
}

@media (max-width: 768px) {
  .defeat-modal {
    padding: 24px;
    max-width: 90%;
  }

  .defeat-icon {
    font-size: 3rem;
  }

  .defeat-title {
    font-size: 1.5rem;
  }
}
</style>