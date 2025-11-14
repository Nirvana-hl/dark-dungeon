<script setup lang="ts">
import StatusBar from '@/components/StatusBar.vue'
import AIChat from '@/components/AIChat.vue'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWalletStore } from '@/stores/wallet'
import { useCharactersStore } from '@/stores/characters'
import { useGameStore } from '@/stores/game'
import { supabase } from '@/lib/supabase'

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
const inBattle = ref(false)
const logs = ref<string[]>([])
const passedLevels = ref<number[]>([])     // 已通关关卡
const claimedLevels = ref<number[]>([])    // 已领取奖励的关卡

async function loadProgress() {
  try {
    const { data: userData } = await supabase.auth.getUser()
    const uid = userData.user?.id
    if (!uid) return
    const { data, error } = await supabase.from('stage_progress').select('level, passed, claimed')
    if (error) return
    passedLevels.value = (data ?? []).filter((r: any) => r.passed).map((r: any) => Number(r.level))
    claimedLevels.value = (data ?? []).filter((r: any) => r.claimed).map((r: any) => Number(r.level))
  } catch {}
}

async function upsertProgress(level: number, changes: Partial<{ passed: boolean; claimed: boolean }>) {
  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (!uid) return
  await supabase.from('stage_progress').upsert({
    user_id: uid,
    level,
    passed: changes.passed ?? passedLevels.value.includes(level),
    claimed: changes.claimed ?? claimedLevels.value.includes(level),
    updated_at: new Date().toISOString()
  })
}

let battleTimer: any = null

function log(msg: string) {
  logs.value.unshift(new Date().toLocaleTimeString() + ' ' + msg)
  if (logs.value.length > 100) logs.value.pop()
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
  const { data, error } = await supabase.from('stages').select('level, name, difficulty, description')
  if (error) return
  stagesMap.value = Object.fromEntries((data ?? []).map((r: any) => [Number(r.level), { name: r.name, difficulty: r.difficulty, desc: r.description }]))
}
const stage = computed(() => {
  const db = stagesMap.value[currentLevel.value]
  if (db) return { level: currentLevel.value, name: db.name, difficulty: db.difficulty, desc: db.desc }
  return stageInfo(currentLevel.value)
})

async function ensureStages() {
  // 若 stages 表为空，则按本地 stageInfo 批量写入 1..maxLevel 关卡
  const { data, error } = await supabase.from('stages').select('id').limit(1)
  if (error) return
  if (data && data.length > 0) return
  const payload = Array.from({ length: maxLevel }, (_, i) => {
    const lvl = i + 1
    const si = stageInfo(lvl)
    return {
      level: lvl,
      name: si.name,
      difficulty: si.difficulty,
      description: si.desc
    }
  })
  await supabase.from('stages').insert(payload)
}

function getRewards(difficulty: Stage['difficulty']) {
  if (difficulty === '普通') return { gold: 50, exp: 50 }
  if (difficulty === '困难') return { gold: 100, exp: 100 }
  return { gold: 150, exp: 150 }
}

// 战斗日志生成（简化模拟）
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

// 奖励弹窗
const rewardOpen = ref(false)
const rewardGold = ref(0)
const rewardExp = ref(0)
const rewardLevel = ref(0)

async function startStage() {
  if (inBattle.value) return
  inBattle.value = true
  log(`进入关卡：${stage.value.name}（难度：${stage.value.difficulty}）`)
  startBattleLog()
  // 确保关卡已写入数据库
  await ensureStages()
  // 记录用户开始该关卡的进度
  await upsertProgress(currentLevel.value, { passed: false })
  // 配置战斗难度并从数据库加载敌人卡组
  game.configureEncounter(stage.value.difficulty)
  await game.loadEnemyDeck(currentLevel.value)
  // 跳转到战斗界面（携带当前关卡参数）
  router.push({ path: '/', query: { level: String(currentLevel.value) } })
}

async function completeStage() {
  if (!inBattle.value) return
  inBattle.value = false
  stopBattleLog()
  if (!passedLevels.value.includes(currentLevel.value)) {
    passedLevels.value.push(currentLevel.value)
    await upsertProgress(currentLevel.value, { passed: true })
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
  // 钱包加金币
  await wallet.add(rewardGold.value)
  log(`奖励领取成功：金币 +${rewardGold.value}`)
  // 角色经验
  const selected = chars.selected
  if (selected) {
    const prevExp = Number((selected as any).attrs?.exp ?? 0)
    const totalExp = prevExp + rewardExp.value
    const levelUp = Math.floor(totalExp / 100)
    const newExp = totalExp % 100
    const prevLevel = Number((selected as any).attrs?.level ?? 1)
    const newLevel = Math.max(1, prevLevel + levelUp)
    const { error } = await supabase
      .from('characters')
      .update({ level: newLevel, attrs: { ...(selected as any).attrs, exp: newExp, level: newLevel } })
      .eq('id', selected.id)
    if (!error) {
      log(`角色经验 +${rewardExp.value}${levelUp > 0 ? `，升级 +${levelUp}` : ''}`)
      await chars.load()
    } else {
      log(`角色经验发放失败：${error.message}`)
    }
  } else {
    log('未选择角色，经验未发放。')
  }
  // 标记已领取
  if (!claimedLevels.value.includes(lvl)) {
    claimedLevels.value.push(lvl)
    await upsertProgress(lvl, { claimed: true })
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

onMounted(async () => {
  log('闯关模式已启用：每5关为一章，逐关推进直至终章。')
  await wallet.init().catch(() => {})
  await ensureStages()
  await loadStages()
  await loadProgress()
  // 若来自战斗胜利，自动弹出奖励并标记通关
  const victory = route.query.victory === '1'
  const lvl = Number(route.query.level ?? 0)
  if (victory && lvl > 0) {
    currentLevel.value = lvl
    if (!passedLevels.value.includes(lvl)) {
      passedLevels.value.push(lvl)
      await upsertProgress(lvl, { passed: true })
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

// 地图总览（简易）：按章节分组，每章5关
const levels = computed(() => Array.from({ length: maxLevel }, (_, i) => i + 1))
function levelStatus(l: number) {
  const passed = passedLevels.value.includes(l)
  const claimed = claimedLevels.value.includes(l)
  const isCurr = currentLevel.value === l
  return { passed, claimed, isCurr }
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gradient-to-b from-slate-900 to-slate-950">
    <StatusBar />

    <div class="flex-1 p-6 grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 主区域：关卡信息与操作 -->
      <div class="lg:col-span-3 space-y-4">
        <div class="card rounded-2xl p-5 border border-slate-700/60 shadow-lg shadow-black/20">
          <div class="mb-4 flex items-center justify-between">
            <div class="font-semibold text-lg">闯关模式</div>
            <div class="text-sm text-slate-300">第 {{ currentLevel }} 关 · 第 {{ chapter }} 章</div>
          </div>

          <div class="rounded-xl border border-slate-700 p-4 bg-slate-900/60">
            <div class="text-xl font-bold">{{ stage.name }}</div>
            <div class="text-sm text-slate-300 mt-1">难度：{{ stage.difficulty }}</div>
            <div class="text-sm text-slate-400 mt-1">{{ stage.desc }}</div>

            <div class="mt-4 flex flex-wrap gap-3">
              <button class="action-button rounded-button px-4 py-2" :disabled="inBattle" @click="startStage">
                {{ inBattle ? '进行中...' : '开始关卡' }}
              </button>
              <button class="action-button rounded-button px-4 py-2" :disabled="!inBattle" @click="completeStage">
                战斗胜利
              </button>
              <button class="action-button rounded-button px-4 py-2" :disabled="inBattle" @click="nextStage">
                下一关
              </button>
              <button class="action-button rounded-button px-4 py-2" :disabled="inBattle" @click="prevStage">
                上一关
              </button>
            </div>

            <div class="mt-6 grid grid-cols-1 md:grid-cols-3 gap-3">
              <div class="rounded-lg border border-slate-700 p-3">
                <div class="font-semibold">关卡状态</div>
                <div class="text-sm text-slate-300 mt-1">
                  {{ inBattle ? '进行中' : '未开始' }}
                </div>
              </div>

              <div class="rounded-lg border border-slate-700 p-3">
                <div class="font-semibold">章节进度</div>
                <div class="text-sm text-slate-300 mt-1">
                  已通关：{{ passedLevels.length }} / {{ maxLevel }}
                </div>
              </div>

              <div class="rounded-lg border border-slate-700 p-3">
                <div class="font-semibold">奖励提示</div>
                <div class="text-sm text-slate-400 mt-1">
                  胜利后领取奖励；领取成功后在地图标记“已领取”。
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 简易地图总览：每章5关 -->
        <div class="card rounded-2xl p-5 border border-slate-700/60">
          <div class="font-semibold mb-3">关卡总览（每5关一章）</div>
          <div class="grid grid-cols-5 gap-2">
            <div
              v-for="l in levels"
              :key="l"
              class="relative rounded-md border text-center py-2 cursor-pointer transition"
              :class="[
                levelStatus(l).isCurr ? 'ring-2 ring-sky-400 border-sky-400' : '',
                levelStatus(l).claimed ? 'bg-emerald-900/40 border-emerald-500 text-emerald-300' :
                levelStatus(l).passed ? 'bg-slate-700/60 border-slate-500 text-slate-200' :
                'bg-slate-900/60 border-slate-700 text-slate-300'
              ]"
              @click="currentLevel = l"
              :title="`第${l}关（${(stagesMap[l]?.difficulty) ?? stageInfo(l).difficulty}）`"
            >
              <div class="text-xs">第 {{ l }} 关</div>
              <div class="text-[10px] opacity-80">{{ stageInfo(l).difficulty }}</div>
              <div v-if="levelStatus(l).claimed" class="absolute top-1 right-1 text-[10px] text-emerald-300">已领</div>
              <div v-else-if="levelStatus(l).passed" class="absolute top-1 right-1 text-[10px] text-slate-300">已通</div>
              <div v-else-if="levelStatus(l).isCurr" class="absolute top-1 right-1 text-[10px] text-sky-300">当前</div>
            </div>
          </div>
          <!-- 章节分隔提示 -->
          <div class="mt-2 text-xs text-slate-400">绿色表示奖励已领取；灰色表示已通关但未领取；蓝圈表示当前关卡。</div>
        </div>
      </div>

      <!-- 右侧：事件日志 -->
      <div class="card rounded-2xl p-4 border border-slate-700/60 shadow-lg shadow-black/20 flex flex-col">
        <div class="font-semibold mb-2">消息日志</div>
        <div class="flex-1 overflow-auto rounded-md bg-slate-900/60 border border-slate-700/60 p-2 text-xs space-y-1">
          <div v-for="(l, i) in logs" :key="i" class="text-slate-300">{{ l }}</div>
          <div v-if="logs.length===0" class="text-slate-500">尚无消息</div>
        </div>
      </div>
    </div>

    <!-- 奖励弹窗 -->
    <div v-if="rewardOpen" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="w-[min(92vw,520px)] rounded-xl border border-slate-700/60 bg-slate-900 p-5 shadow-2xl">
        <div class="text-lg font-semibold mb-2">领取奖励</div>
        <div class="text-slate-300 mb-4">恭喜通关第 {{ rewardLevel }} 关。可领取：金币 {{ rewardGold }}、经验 {{ rewardExp }}。</div>
        <div class="flex gap-3 justify-end">
          <button class="px-4 py-2 rounded-button bg-slate-800 hover:bg-slate-700 border border-slate-600 transition-colors" @click="rewardOpen=false">稍后再领</button>
          <button class="px-4 py-2 rounded-button action-button" @click="claimReward">立即领取</button>
        </div>
      </div>
    </div>

    <AIChat />
  </div>
</template>