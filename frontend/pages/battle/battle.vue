<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import BattleField from '@/components/BattleField.vue'
import CardItem from '@/components/CardItem.vue'
import { useGameStore } from '@/stores/game'
import type { Card } from '@/stores/game'
import { storeToRefs } from 'pinia'
import { useWalletStore } from '@/stores/wallet'
import { useCampStore } from '@/stores/camp'
import { useAuthStore } from '@/stores/auth'
import apiClient from '@/api/request'
import { CurrencyType } from '@/types'
import { stageProgressApi, stageApi, skillApi } from '@/api/request'
import { soundManager } from '@/utils/soundManager'

// uni-app 类型声明（覆盖全局声明以确保识别）
declare const uni: {
  navigateTo: (options: { url: string }) => void
  redirectTo: (options: { url: string }) => void
  reLaunch: (options: { url: string }) => void
  navigateBack: (options?: { delta?: number }) => void
  switchTab: (options: { url: string }) => void
  showToast: (options: { title: string; icon?: 'success' | 'error' | 'loading' | 'none'; duration?: number }) => void
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
  clearStorageSync: () => void
  [key: string]: any
}

// 页面加载
onLoad((options) => {
  routeQuery = options || {}
})

// 页面显示时可按需刷新
onShow(() => {
  // 检查登录状态
  if (!auth.isAuthenticated) {
    console.warn('[BattlePage] 用户未登录，跳转到登录页')
    uni.reLaunch({
      url: '/pages/login/login'
    })
    return
  }

  // 可根据需要刷新数据
})

// 页面卸载
onUnload(() => {
  // 清理代码（如果需要）
})

// 页面参数（替代 useRoute）
let routeQuery: Record<string, any> = {}

const level = computed(() => Number(routeQuery.level ?? 0))
const chapter = computed(() => (level.value ? Math.floor((level.value - 1) / 5) + 1 : 0))

const game = useGameStore()
const auth = useAuthStore()
const gameStore = game as any
// Access store properties directly to avoid TypeScript issues
const hand = computed(() => gameStore.hand)
const canPlay = computed(() => gameStore.canPlay)
const winner = computed(() => gameStore.winner)
const mana = computed(() => gameStore.mana)
const manaMax = computed(() => gameStore.manaMax)
const deckExhausted = computed(() => gameStore.deckExhausted)
const deck = computed(() => gameStore.deck)
const enemyDifficulty = computed(() => gameStore.enemyDifficulty)
// Remove handLength computed property - will cast directly in template

const isEndingTurn = ref(false)
const showExitConfirm = ref(false)
const draggingEquipCard = ref<Card | null>(null)
const draggingSpellCard = ref<Card | null>(null)
const draggingCharacterCard = ref<Card | null>(null)
const remainingDeck = computed(() => deck.value.length)

// 技能使用相关状态
const showSkillModal = ref(false)
const skillsLoading = ref(false)
const battleSkills = ref<any[]>([])
const usingSkill = ref(false)

// ref to BattleField component (used to resolve touch drops inside component)
const battleFieldRef = ref<any>(null)

// BattleField 实例引用监控（用于小程序环境）
watch(battleFieldRef, (newRef, oldRef) => {
  console.log('[Battle] BattleField 实例变化:', { newRef: !!newRef, oldRef: !!oldRef })
}, { immediate: true })

// drag clone state for miniapp fallback
const cloneVisible = ref(false)
const cloneCard = ref<Card | null>(null)
const cloneX = ref(0)
const cloneY = ref(0)
let localTouchStartX = 0
let localTouchStartY = 0
let localTouchMoved = false

function onHandTouchStart(e: any, card: Card) {
  try { battleFieldRef?.value?.onCardTouchStart && battleFieldRef.value.onCardTouchStart(card.id, card.type, e, e.currentTarget) } catch (err) {}
  if (!e.touches || e.touches.length === 0) return
  localTouchStartX = e.touches[0].clientX
  localTouchStartY = e.touches[0].clientY
  localTouchMoved = false
  cloneVisible.value = false
  cloneCard.value = null
}

function onHandTouchMove(e: any, card: Card) {
  try { battleFieldRef?.value?.onCardTouchMove && battleFieldRef.value.onCardTouchMove(card.id, e) } catch (err) {}
  if (!e.touches || e.touches.length === 0) return
  const dx = Math.abs(e.touches[0].clientX - localTouchStartX)
  const dy = Math.abs(e.touches[0].clientY - localTouchStartY)
  if (dx > 8 || dy > 8) localTouchMoved = true
  if (localTouchMoved) {
    cloneVisible.value = true
    cloneCard.value = card
    cloneX.value = e.touches[0].clientX
    cloneY.value = e.touches[0].clientY
  }
}

function onHandTouchEnd(e: any, card: Card) {
  try { battleFieldRef?.value?.onCardTouchEnd && battleFieldRef.value.onCardTouchEnd(card.id, card.type, e) } catch (err) {}
  cloneVisible.value = false
  cloneCard.value = null
  localTouchMoved = false
}

function handleTouchDragEnd(payload: { cardId: string; cardType: string; x: number; y: number; canAfford: boolean }) {
  console.log('[Battle] handleTouchDragEnd called with payload:', payload)
  if (battleFieldRef?.value && typeof battleFieldRef.value.resolveTouchDrop === 'function') {
    console.log('[Battle] Calling resolveTouchDrop on battleFieldRef.value')
    battleFieldRef.value.resolveTouchDrop(payload)
  } else {
    // fallback: try calling directly if ref is function-style
    try {
      // @ts-ignore
      battleFieldRef?.resolveTouchDrop && battleFieldRef.resolveTouchDrop(payload)
    } catch (e) {
      console.warn('[Battle] resolveTouchDrop not available on battleFieldRef', e)
    }
  }
}


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
  gameStore.playCard(id)
}

function startEquipDrag(card: Card) {
  // 仅记录装备卡的拖拽状态
  if (card.type !== 'equipment') return
  console.log('[Battle] startEquipDrag called with card:', card.id, card.name)
  draggingEquipCard.value = card
  console.log('[Battle] draggingEquipCard.value set to:', draggingEquipCard.value)
}

function endEquipDrag() {
  draggingEquipCard.value = null
}

function handleEquipToMinion(payload: { minionId: string; cardId?: string }) {
  console.log('[Battle] handleEquipToMinion called with payload:', payload)

  let equipCardId = payload.cardId

  // 如果payload中没有cardId，尝试使用draggingEquipCard
  if (!equipCardId && draggingEquipCard.value) {
    equipCardId = draggingEquipCard.value.id
  }

  if (!equipCardId) {
    console.log('[Battle] No equipment card ID found, cannot equip')
    return
  }

  console.log('[Battle] Equipping card', equipCardId, 'to minion', payload.minionId)
  gameStore.equipCardToMinion(equipCardId, payload.minionId)
  draggingEquipCard.value = null
}

/* Removed legacy detail panel handlers — CardItem now shows modal internally */

function startCharacterDrag(card: Card) {
  if (card.type !== 'character') return
  draggingCharacterCard.value = card
}

function endCharacterDrag() {
  draggingCharacterCard.value = null
}

function startSpellDrag(card: Card) {
  if (card.type !== 'spell') return
  draggingSpellCard.value = card
}

function endSpellDrag(event?: DragEvent) {
  // 如果法术卡被拖拽离开手牌区，视为打出
  if (draggingSpellCard.value) {
    const card = draggingSpellCard.value
    
    // 检查拖拽结束时鼠标位置是否在手牌区域外
    if (event) {
      const handCardsElement = (event.currentTarget as Element)?.closest('.hand-cards') as HTMLElement | null
      if (handCardsElement) {
        const rect = handCardsElement.getBoundingClientRect()
        const x = event.clientX
        const y = event.clientY
        
        // 如果鼠标位置在手牌区域外，视为打出
        if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
          // 检查是否有足够的法力值
          if (mana.value >= card.cost) {
            gameStore.playCard(card.id)
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

// 计算扇形手牌每张卡片的绝对样式
function fanCardStyle(index: number, total: unknown) {
  const totalNum = Number(total) || 0
  // 最大展开角度（度数）
  const maxSpread = Math.min(70, totalNum * 10)
  const start = -maxSpread / 2
  const step = totalNum > 1 ? maxSpread / (totalNum - 1) : 0
  const deg = start + step * index

  // 横向偏移（像素），让卡片在水平方向分散
  const spacing = 38 // 每张卡的基础间距
  const centerOffset = (index - (totalNum - 1) / 2) * spacing

  // 手牌容器底部定位（距离容器底部的偏移）
  const bottomOffset = 24

  return {
    position: 'absolute',
    left: '50vw', // 以视口居中，避免 footer 内部侧边占位导致偏移
    bottom: `${bottomOffset}px`,
    transform: `translateX(calc(-50% + ${centerOffset}px)) rotate(${deg}deg)`,
    zIndex: 100 + index
  } as any
}

// 获取HP颜色类（页面级别副本，用于底部显示）
function getHPColorClass(percent: number) {
  if (percent > 60) return 'hp-healthy'
  if (percent > 30) return 'hp-warning'
  return 'hp-danger'
}

// 确保返回数字类型的包装函数
function safeHandLength(): number {
  const h = hand.value
  return Array.isArray(h) ? h.length : 0
}

// 包装函数确保正确的类型转换
function getHandLength(): number {
  const arr = hand.value as any[]
  return arr.length
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
        gameStore.playCard(card.id)
        draggingSpellCard.value = null
      }
    }
  }
}

function handleDeployCard(payload: { cardId: string; position: number }) {
  // 调用 playCard 并传入位置参数
  gameStore.playCard(payload.cardId, payload.position)
}


// 退出战斗
function exitBattle() {
  showExitConfirm.value = true
}

function confirmExit() {
  showExitConfirm.value = false
  uni.switchTab({ url: '/pages/explore/explore' })
}

function cancelExit() {
  showExitConfirm.value = false
}

// 结束回合 - 带视觉反馈
async function endTurn() {
  if (!canPlay.value || isEndingTurn.value) return
  isEndingTurn.value = true
  gameStore.endTurn()
  // 添加延迟以提供视觉反馈
  await new Promise(resolve => setTimeout(resolve, 500))
  isEndingTurn.value = false
}

// 打开技能选择弹窗（异步加载技能）
async function openUseSkillModal() {
  try {
    if (!canPlay.value || usingSkill.value) return
    skillsLoading.value = true
    // 从营地store获取当前职业code
    const camp = useCampStore()
    const pcCode = camp.playerCharacter?.playerCharacterCode || camp.playerCharacter?.code || ''
    let resp: any = null
    try {
      resp = await skillApi.getBattleSkills(pcCode)
    } catch (e) {
      // 回退到已解锁列表
      resp = await skillApi.getUnlockedSkills()
    }
    console.log('[Battle] skill API response:', resp)
    if (resp && resp.data && resp.data.code === 200) {
      battleSkills.value = Array.isArray(resp.data.data) ? resp.data.data : []
    } else {
      battleSkills.value = []
    }
    showSkillModal.value = true
  } catch (e: any) {
    try { uni.showToast({ title: e?.message || '加载技能失败', icon: 'none' }) } catch (e) {}
  } finally {
    skillsLoading.value = false
  }
}

function closeSkillModal() {
  showSkillModal.value = false
}

// only active skills for the use-skill modal
const activeBattleSkills = computed(() => {
  return (battleSkills.value || []).filter((s: any) => {
    return Boolean(s.isActive || s.active || s.type === 'active' || s.category === 'active')
  })
})

function getFirstSentence(text?: string) {
  if (!text) return ''
  // match until first sentence-ending punctuation (Chinese/Japanese/English)
  const m = String(text).trim().match(/.*?[。！？.!?](?=\s|$)/)
  if (m && m[0]) return m[0]
  // fallback to first line
  return String(text).split(/\r?\n/)[0]
}

// displayed skills: prefer active list but fall back to all skills if none found
const displayedSkills = computed(() => {
  const act = activeBattleSkills.value || []
  return act.length > 0 ? act : (battleSkills.value || [])
})

// 使用技能：调用后端验证并解析效果，前端尝试应用简单效果（伤害/治疗）
async function useSkill(skill: any) {
  if (!canPlay.value || usingSkill.value) return
  usingSkill.value = true
  try {
    const resp = await skillApi.useSkill(String(skill.id), { currentMana: gameStore.mana })
    if (!resp || !resp.data) {
      throw new Error('技能使用失败')
    }
    if (resp.data.code !== 200) {
      throw new Error(resp.data.message || '技能无法使用')
    }
    const result = resp.data.data || {}
    const manaCost = Number(result.manaCost ?? result.mana_cost ?? skill.manaCost ?? skill.mana_cost ?? 1)
    // 扣除法力
    try { gameStore.mana = Math.max(0, Number(gameStore.mana) - manaCost) } catch (e) {}

    // 解析效果
    let effect: any = result.effectPayload ?? result.effect_payload ?? skill.effectPayload ?? skill.effect_payload
    if (typeof effect === 'string') {
      try { effect = JSON.parse(effect) } catch (e) {}
    }
    // 简单效果处理：damage / heal
    if (effect) {
      const dmg = Number(effect.damage ?? effect.damage_amount ?? effect.damage_to_enemy ?? 0)
      const heal = Number(effect.heal ?? effect.heal_amount ?? effect.heal_self ?? 0)
      if (dmg > 0) {
        try {
          gameStore.enemyHP = Math.max(0, Number(gameStore.enemyHP) - dmg)
          gameStore.log(`技能 ${skill.name} 对敌方造成 ${dmg} 点伤害（剩余 ${gameStore.enemyHP}）`)
        } catch (e) {}
      }
      if (heal > 0) {
        try {
          const camp = useCampStore()
          const maxHp = camp.playerCharacter?.maxHp || 100
          gameStore.heroHP = Math.min(maxHp, Number(gameStore.heroHP) + heal)
          gameStore.log(`技能 ${skill.name} 为我方回复 ${heal} 点生命（当前 ${gameStore.heroHP}/${maxHp}）`)
        } catch (e) {}
      }
    } else {
      gameStore.log(`技能 ${skill.name} 使用成功（效果需前端扩展）`)
    }

    try { uni.showToast({ title: '技能已使用', icon: 'success' }) } catch (e) {}
    showSkillModal.value = false
  } catch (e: any) {
    try { uni.showToast({ title: e?.message || '技能使用失败', icon: 'none' }) } catch (e) {}
  } finally {
    usingSkill.value = false
  }
}

// 根据关卡参数配置敌方难度并开局（从数据库加载玩家/敌方手牌）
onMounted(async () => {
  // 预加载音效（非阻塞）
  soundManager.preloadSounds().catch(err => {
    console.warn('[Game] 音效预加载失败:', err)
  })

  const lv = level.value || 1
  const diff = lv <= 10 ? '普通' : lv <= 20 ? '困难' : '噩梦'
  gameStore.configureEncounter(diff as any)

  // 确保营地数据已加载（用于获取玩家血量）
  const campStore = useCampStore()
  if (!campStore.playerCharacter) {
    await campStore.fetchCampData()
  }

  gameStore.reset() // 先重置状态
  // 从上阵区加载卡牌作为战斗牌库（reset 已清空内存，故在 reset 之后加载）
  console.log('[Battle] 从上阵区加载战斗牌库（reset 之后）')
  await gameStore.loadEquippedCardsAsDeck()
  await gameStore.loadEnemyDeck(lv) // 再加载敌人数据（包括敌人面板）
  // 开始玩家回合并抽牌（从已上阵的牌库中抽取）
  try {
    gameStore.startPlayerTurn()
    console.log('[Battle] 已启动玩家回合并抽牌（首抽）')
  } catch (e) {
    console.warn('[Battle] 启动玩家回合失败:', e)
  }
})

// 若关卡参数变化，重新配置并重新加载敌方手牌
watch(level, async (lv) => {
  const diff = lv && lv <= 10 ? '普通' : lv && lv <= 20 ? '困难' : '噩梦'
  gameStore.configureEncounter(diff as any)

  // 确保营地数据已加载（用于获取玩家血量）
  const campStore = useCampStore()
  if (!campStore.playerCharacter) {
    await campStore.fetchCampData()
  }

  gameStore.reset() // 先重置状态
  // 重新从上阵区加载牌库（如果有）
  await gameStore.loadEquippedCardsAsDeck()
  await gameStore.loadEnemyDeck(lv || 1) // 再加载敌人数据（包括敌人面板）
  // 开始玩家回合并抽牌（从已上阵的牌库中抽取）
  try {
    gameStore.startPlayerTurn()
    console.log('[Battle] 已启动玩家回合并抽牌（首抽）')
  } catch (e) {
    console.warn('[Battle] 启动玩家回合失败:', e)
  }
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
      const battleHp = gameStore.heroHP // 使用战斗中的血量
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
  const lv = Number(routeQuery.level ?? 1)
  try {
    await stageProgressApi.passStage(lv)
    console.log(`[Game] 关卡 ${lv} 已标记为通过`)
  } catch (error) {
    console.error('[Game] 保存关卡进度失败:', error)
  }
  
  showVictoryModal.value = false
  uni.switchTab({ url: '/pages/explore/explore' })
}

// 确认失败结算，返回探索界面
function confirmDefeat() {
  showDefeatModal.value = false
  uni.switchTab({ url: '/pages/explore/explore' })
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
  <view class="battle-container">
    
    <!-- 顶部信息栏（已移除，仅保留系统/设备状态区域） -->

    <!-- 战斗场地 -->
    <view class="battle-main">
        <BattleField
          ref="battleFieldRef"
          :dragging-equip-card="draggingEquipCard"
          @equip-to-minion="handleEquipToMinion"
          @deploy-card="handleDeployCard"
          @play-card="onPlay"
        />
    </view>

    <!-- 暂时隐藏战斗日志 -->
    <!-- <view class="battle-log">
      <view class="log-header">
        <text class="log-icon">📜</text>
        <text class="log-title">战斗日志</text>
        <text class="log-count">({{ logs.length }})</text>
      </view>
      <view class="log-content">
        <view
          v-for="(l, i) in logs"
          :key="i"
          class="log-entry"
          :style="{ animationDelay: `${i * 0.05}s` }"
        >
          {{ l }}
        </view>
        <view v-if="logs.length === 0" class="log-empty">
          <text class="empty-icon">📝</text>
          <text class="empty-text">尚无消息</text>
        </view>
      </view>
    </view> -->

    <!-- 底部操作区 -->
    <view class="battle-footer">
      <!-- 中间：手牌区域（保留唯一底部区域） -->
      <view class="hand-section">
        <!-- 左侧玩家信息：名字 + 血条 -->
        <view class="hand-player-info" aria-hidden="false">
          <view class="player-name-small">
            <text class="name-text-small">{{ playerCharacter?.playerCharacterName || '冒险者' }}</text>
          </view>
          <view class="hp-display small">
            <view class="hp-bar-container horizontal small">
              <view class="hp-bar-bg horizontal small">
                <view
                  class="hp-bar-fill"
                  :class="getHPColorClass((gameStore.heroHP / (playerCharacter?.maxHp || 100)) * 100)"
                  :style="{ width: ((gameStore.heroHP / (playerCharacter?.maxHp || 100)) * 100) + '%' }"
                ></view>
              </view>
            </view>
            <view class="hp-text horizontal small">
              <text class="hp-value">{{ gameStore.heroHP }}</text>
              <text class="hp-separator">/</text>
              <text class="hp-max">{{ playerCharacter?.maxHp || 100 }}</text>
            </view>
          </view>
        </view>
        <view class="hand-header">
          <view class="hand-title">
            <!-- 手牌图标与数量已移除 -->
          </view>
          <!-- (法力与操作按钮已移至右侧侧栏以保持一致布局) -->
          <view class="hand-helpers">
            <view v-if="deckExhausted" class="deck-empty-badge" title="本场战斗无法再抽牌">
              <text class="badge-icon">⚠️</text>
              <text class="badge-text">牌库已耗尽</text>
            </view>
          </view>
        </view>
        <view 
          class="hand-cards"
          @dragleave="handleHandDragLeave"
        >
          <!-- 扇形手牌布局：每张牌包裹在 .fan-card，中点为容器中心 -->
          <view
            v-for="(c, idx) in hand"
            :key="c.id"
            class="fan-card"
            :data-card-id="c.id"
            :style="(fanCardStyle as any)(idx, safeHandLength())"
            @touchstart.passive="(e) => onHandTouchStart(e, c)"
            @touchmove.passive="(e) => onHandTouchMove(e, c)"
            @touchend.passive="(e) => onHandTouchEnd(e, c)"
            @touch-drag-end="handleTouchDragEnd"
          >
            <CardItem
              :card="c"
              @play="onPlay"
              @start-equip-drag="startEquipDrag"
              @end-equip-drag="endEquipDrag"
              @start-character-drag="startCharacterDrag"
              @end-character-drag="endCharacterDrag"
              @start-spell-drag="startSpellDrag"
              @end-spell-drag="(e) => endSpellDrag(e)"
              @deploy-card="handleDeployCard"
              @equip-to-minion="handleEquipToMinion"
              :can-afford="mana >= c.cost"
            />
          </view>
          <view v-if="hand.length === 0" class="empty-hand">
            <text class="empty-icon">📭</text>
            <text class="empty-text">手牌为空</text>
          </view>
        </view>
      <!-- Clone preview for touch drag fallback (miniapp) -->
      <view v-if="cloneVisible && cloneCard" class="drag-clone" :style="{
        position: 'fixed',
        left: cloneX + 'px',
        top: cloneY + 'px',
        transform: 'translate(-50%,-50%)',
        zIndex: 9999
      }">
        <CardItem :card="cloneCard" :can-afford="mana >= (cloneCard.cost)" />
      </view>

      <!-- 右侧操作侧栏：法力（顶部）、使用技能（中间）、结束回合（底部） - 放入手牌区内，顶部不超出对战区 -->
      <view class="right-action-column">
        <view class="mana-display right-mana" pointer-events="none">
          <text class="mana-icon">💎</text>
          <text class="mana-value">{{ mana }}/{{ manaMax }}</text>
        </view>

        <button
          class="use-skill-btn"
          :class="{ 'disabled': !canPlay || isEndingTurn || usingSkill }"
          :disabled="!canPlay || isEndingTurn || usingSkill"
          @click="openUseSkillModal"
        >
          <text class="btn-content">
            <text class="btn-icon-large">✨</text>
            <text class="btn-label">{{ usingSkill ? '使用中...' : '使用技能' }}</text>
          </text>
        </button>

        <view class="action-buttons right-action" pointer-events="none">
          <button 
            class="end-turn-btn"
            :class="{ 
              'disabled': !canPlay || isEndingTurn,
              'processing': isEndingTurn
            }"
            :disabled="!canPlay || isEndingTurn"
            @click="endTurn"
          >
            <text class="btn-content">
              <text class="btn-icon-large">⏭️</text>
              <text class="btn-label">{{ isEndingTurn ? '处理中...' : '结束回合' }}</text>
            </text>
            <view v-if="isEndingTurn" class="btn-loading"></view>
          </button>
        </view>
      </view>
        </view>
    </view>

    
    <!-- 退出确认弹窗 -->
    <view v-if="showExitConfirm" class="modal-overlay" @click="cancelExit">
      <view class="exit-modal" @click.stop>
        <view class="modal-header">
          <h3>退出战斗</h3>
        </view>
        <view class="modal-content">
          <p>确定要退出当前战斗吗？</p>
          <p class="modal-warning">退出后将返回闯关界面，当前战斗进度将丢失。</p>
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel-btn" @click="cancelExit">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmExit">确认退出</button>
        </view>
      </view>
    </view>

  <!-- 技能选择弹窗 -->
  <view v-if="showSkillModal" class="modal-overlay" @click="closeSkillModal">
    <view class="skill-modal" @click.stop>
      <view class="modal-header">
        <h3>选择技能</h3>
      </view>
      <view class="modal-content">
        <view v-if="skillsLoading" class="loading-text">正在加载技能...</view>
        <view v-else>
          <view v-if="battleSkills.length === 0" class="empty-text">
            <text>暂无可用技能</text>
          </view>
          <view v-else class="skill-list">
            <view v-for="s in displayedSkills" :key="s.id" class="skill-item">
              <view class="skill-left">
                <text class="skill-name">{{ s.name }}</text>
                <text class="skill-desc">{{ getFirstSentence(s.description || '') }}</text>
              </view>
              <view class="skill-right">
                <text class="skill-cost">消耗: {{ s.manaCost ?? s.mana_cost ?? 1 }}</text>
                <button class="confirm-btn" @click="useSkill(s)" :disabled="usingSkill || (s.manaCost ?? s.mana_cost ?? 1) > mana">使用</button>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="modal-actions">
        <button class="modal-btn cancel-btn" @click="closeSkillModal">取消</button>
      </view>
    </view>
  </view>

    <!-- 战斗胜利结算弹窗 -->
    <view v-if="showVictoryModal" class="modal-overlay victory-overlay">
      <view class="victory-modal" @click.stop>
        <view class="victory-header">
          <view class="victory-icon">🏆</view>
          <h2 class="victory-title">战斗胜利！</h2>
          <p class="victory-subtitle">第 {{ level }} 关 · {{ enemyDifficulty }}</p>
        </view>
        
        <view class="victory-content">
          <!-- 奖励展示 -->
          <view class="reward-section">
            <h3 class="reward-title">获得奖励</h3>
            <view class="reward-list">
              <view class="reward-item gold-reward">
                <view class="reward-icon">🪙</view>
                <view class="reward-info">
                  <view class="reward-label">金币</view>
                  <view class="reward-value">+{{ victoryReward.gold }}</view>
                </view>
              </view>
            </view>
          </view>

          <!-- 角色提升 -->
          <view class="character-progress-section">
            <h3 class="progress-title">角色提升</h3>
            <view class="progress-list">
              <view class="progress-item exp-progress">
                <view class="progress-icon">⭐</view>
                <view class="progress-info">
                  <view class="progress-label">经验值</view>
                  <view class="progress-value">+{{ victoryReward.exp }}</view>
                </view>
              </view>
              <view class="progress-item stress-progress">
                <view class="progress-icon">😰</view>
                <view class="progress-info">
                  <view class="progress-label">压力值</view>
                  <view class="progress-value">+{{ victoryReward.stress }}</view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="victory-actions">
          <button class="victory-btn" @click="confirmVictory">
            <text class="btn-icon">✓</text>
            <text class="btn-text">确认</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 战斗失败结算弹窗 -->
    <view v-if="showDefeatModal" class="modal-overlay defeat-overlay">
      <view class="defeat-modal" @click.stop>
        <view class="defeat-header">
          <view class="defeat-icon">💀</view>
          <h2 class="defeat-title">战斗失败</h2>
          <p class="defeat-subtitle">第 {{ level }} 关 · {{ enemyDifficulty }}</p>
        </view>
        
        <view class="defeat-content">
          <!-- 濒死机制说明 -->
          <view class="near-death-section">
            <h3 class="near-death-title">⚠️ 濒死触发</h3>
            <p class="near-death-description">你的生命值归零，触发了濒死机制</p>
          </view>

          <!-- 状态变化 -->
          <view class="status-change-section">
            <h3 class="status-title">状态变化</h3>
            <view class="status-list">
              <view class="status-item stress-change">
                <view class="status-icon">😰</view>
                <view class="status-info">
                  <view class="status-label">压力值增加</view>
                  <view class="status-value negative">+{{ defeatInfo.stressIncrease }}</view>
                </view>
              </view>
              <view class="status-item hp-restore">
                <view class="status-icon">❤️</view>
                <view class="status-info">
                  <view class="status-label">生命值恢复</view>
                  <view class="status-value positive">{{ defeatInfo.hpRestored }}/{{ defeatInfo.maxHp }}</view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="defeat-actions">
          <button class="defeat-btn" @click="confirmDefeat">
            <text class="btn-icon">✓</text>
            <text class="btn-text">确认</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.battle-container {
  min-height: 100vh;
  padding-top: 0; /* reduce reserved top space so content can sit closer to native title */
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  position: relative;
  overflow: hidden; /* lock to single screen - prevent vertical scroll */
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
  padding: 0;
  height: 0;
  min-height: 0;
  overflow: hidden;
  background: transparent;
  border-bottom: none;
  position: relative;
  z-index: 10;
}

.battle-header .header-center {
  display: none; /* 已将法力/手牌统计移动到底部，顶部统计隐藏 */
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
  padding: 0 12px; /* 缩减左右与上内边距，让对战区更靠近顶部 */
  margin-top: -6px; /* 上移主内容以贴近原生导航栏 */
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

.battle-main :deep(.battle-field) {
  min-height: 100%;
  box-sizing: border-box;
}

/* 完全隐藏战斗日志（不占位） */
.battle-log {
  display: none !important;
  height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
}

/* 底部操作区 */
.battle-footer {
  padding: 8px 16px 12px;
  background: transparent;
  backdrop-filter: none;
  border-top: none;
  position: relative;
  z-index: 10;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
}

.hand-section {
  flex: 1 1 auto;
  background: transparent;
  padding: 0 12px;
  position: relative;
}

/* 左侧手牌区的玩家名字与血条（紧凑小版） */
.hand-player-info {
  position: absolute;
  left: 12px;
  top: 88px; /* 再向下移动 40px */
  width: 140px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  z-index: 25;
}
.player-name-small .name-text-small {
  font-size: 18px;
  font-weight: 700;
  color: #e2e8f0;
}
.player-name-small {
  padding-left: 30px; /* 向右移动名字 30px */
}
.hp-display.small .hp-bar-container.horizontal.small {
  width: 100px; /* 缩短血条长度为 100px */
}
.hp-bar-bg.horizontal.small {
  height: 10px;
  border-radius: 6px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.hp-bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.4s ease;
}
.hp-bar-fill.hp-healthy {
  background: linear-gradient(90deg, #10b981, #059669);
}
.hp-bar-fill.hp-warning {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}
.hp-bar-fill.hp-danger {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}
.hp-text.horizontal.small {
  display: flex;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
  justify-content: flex-start;
  align-items: center;
}

.hand-header {
  /* 手牌统计信息放在手牌区上方，适配底部横向布局 */
  position: relative;
  left: auto;
  bottom: auto;
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 0;
  background: transparent;
  z-index: 20;
}

.hand-title {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #e2e8f0;
}

.hand-icon {
  font-size: 34rpx;
}

.mana-display {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  background: rgba(59, 130, 246, 0.15);
  border: 1rpx solid rgba(59, 130, 246, 0.3);
  border-radius: 16rpx;
  margin-left: 24rpx;
}

/* 手牌区右侧的法力显示（固定到手牌区右下边） */
.hand-section .hand-mana-right {
  position: absolute;
  right: 12px;
  top: 8px;
  margin-left: 0;
  padding: 8px 14px;
}

.mana-icon {
  font-size: 28rpx;
}

.mana-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #60a5fa;
}

.hand-hint {
  font-size: 24rpx;
  color: #94a3b8;
}

.hand-helpers {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.deck-empty-badge {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  border-radius: 9999rpx;
  background: rgba(248, 113, 113, 0.15);
  border: 1rpx solid rgba(248, 113, 113, 0.4);
  color: #fecaca;
  font-size: 24rpx;
  font-weight: 600;
}

.deck-empty-badge .badge-icon {
  font-size: 28rpx;
}

.hand-cards {
  position: relative;
  width: 100vw;
  margin: 0 auto;
  height: 160px; /* 缩短手牌高度，避免超出屏幕 */
  overflow: visible;
  padding: 8rpx 0;
  background: transparent !important;
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
  margin-top: 24rpx;
  padding: 20rpx 28rpx;
  border-radius: 24rpx;
  background: rgba(15, 23, 42, 0.9);
  border: 1rpx solid rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
  max-width: 840rpx;
}

.equip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.equip-title {
  font-size: 30rpx;
  font-weight: 600;
}

.equip-close {
  background: transparent;
  border: none;
  color: #9ca3af;
  font-size: 28rpx;
}

.equip-body {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  font-size: 26rpx;
}

.equip-name {
  font-weight: 700;
  font-size: 30rpx;
}

.equip-effects {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 8rpx;
}

.equip-effects span {
  padding: 8rpx 20rpx;
  border-radius: 9999rpx;
  background: rgba(34, 197, 94, 0.15);
  border: 1rpx solid rgba(34, 197, 94, 0.4);
  color: #bbf7d0;
}

.equip-hint {
  margin-top: 8rpx;
  font-size: 24rpx;
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
  margin-top: 24rpx;
  display: flex;
  gap: 16rpx;
}

.use-spell-btn {
  flex: 1;
  padding: 20rpx 32rpx;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border: none;
  border-radius: 16rpx;
  color: white;
  font-size: 28rpx;
  font-weight: 600;
}

.use-spell-btn:disabled {
  background: rgba(71, 85, 105, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
}

.empty-hand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  width: 100%;
  padding: 64rpx;
  color: #64748b;
}

.empty-icon {
  font-size: 64rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
}

/* 扇形手牌卡片包装器 */
.fan-card {
  position: absolute;
  top: 0;
  left: 50%;
  transform-origin: bottom center;
  transition: transform 0.12s ease, z-index 0.12s;
}
.fan-card:hover {
  transform: translateX(-50%) scale(1.06);
  z-index: 10000 !important;
}
/* 确保扇形在容器中水平居中 */
.hand-cards {
  display: block;
  text-align: center;
}

.hand-header {
  position: relative; /* 允许在头部定位技能按钮 */
}

.header-use-skill-wrapper {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 40;
  pointer-events: none; /* wrapper 不拦截事件，按钮本身可交互 */
}
.header-use-skill-wrapper .use-skill-btn {
  pointer-events: auto;
}

/* 右侧操作侧栏（覆盖：法力、使用技能、结束回合） */
.right-action-column {
  position: absolute;
  right: 12px;
  top: 12px;
  bottom: calc(env(safe-area-inset-bottom, 0px) + 12px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  z-index: 60;
  pointer-events: none;
  width: auto;
  max-width: 220px;
}
.right-action-column > .use-skill-btn,
.right-action-column > .action-buttons,
.right-action-column > .mana-display {
  pointer-events: auto;
}
.right-action-column .right-mana {
  background: rgba(59,130,246,0.12);
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(59,130,246,0.18);
}
.action-buttons.right-action {
  position: static;
  right: auto;
  bottom: auto;
  width: 100%;
  display: flex;
  justify-content: center;
}

/* Enlarge the use-skill button when placed in the right action column */
.right-action-column .use-skill-btn {
  min-width: 200rpx;
  padding: 22rpx 28rpx; /* increase vertical padding */
  min-height: 92rpx;
  font-size: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 14rpx 36rpx rgba(124, 58, 237, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
}
.right-action-column .use-skill-btn .btn-icon-large {
  font-size: 32rpx;
  line-height: 1;
}

/* 操作按钮 */
  .action-buttons {
  position: absolute;
  right: 12px;
  bottom: calc(env(safe-area-inset-bottom, 0px) + 40px); /* 向下 30px（之前为70px -> 40px） */
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
  width: auto;
  z-index: 30;
}

@media (max-width: 480px) {
  .action-buttons {
    bottom: calc(env(safe-area-inset-bottom, 0px) + 20px);
  }
}

.end-turn-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 180rpx; /* 进一步缩小宽度 */
  padding: 14rpx 28rpx; /* 进一步缩小内边距 */
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 24rpx;
  color: white;
  font-size: 22rpx; /* 进一步缩小文字尺寸 */
  font-weight: 700;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.3);
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

/* 使用技能按钮，样式与结束回合相似但更紧凑 */
.use-skill-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 140rpx;
  padding: 10rpx 20rpx;
  background: linear-gradient(135deg, #9f7aea 0%, #7c3aed 100%);
  border: none;
  border-radius: 20rpx;
  color: white;
  font-size: 18rpx;
  font-weight: 700;
  overflow: hidden;
  box-shadow: 0 8rpx 20rpx rgba(124, 58, 237, 0.25);
}
.use-skill-btn.disabled {
  background: rgba(71, 85, 105, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
}

/* 技能弹窗样式复用已有 modal 类 */
.skill-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 16px;
  max-width: 520px;
  width: 90%;
}
.skill-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.skill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(255,255,255,0.02);
  border-radius: 8px;
}
.skill-left {
  flex: 1;
  padding-right: 12px;
}
.skill-name {
  font-weight: 700;
  color: #e2e8f0;
  display: block;
  margin-bottom: 6px;
}
.skill-desc {
  font-size: 12px;
  color: #94a3b8;
  display: block;
  max-width: 100%;
  white-space: normal;
  overflow: hidden;
  text-overflow: ellipsis;
}
.skill-right {
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}
.skill-cost {
  color: #fbbf24;
  margin-bottom: 6px;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  position: relative;
  z-index: 2;
}

.btn-icon-large {
  font-size: 24rpx;
}

.btn-label {
  font-size: 0.85rem;
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
    min-width: 110px;
    padding: 8px 16px;
    font-size: 18px;
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
    min-width: 100px;
    padding: 8px 16px;
    font-size: 18px;
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