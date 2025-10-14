<script setup lang="ts">
import StatusBar from '@/components/StatusBar.vue'
import AIChat from '@/components/AIChat.vue'
import { computed, onMounted, reactive, ref } from 'vue'

type TileType = 'empty' | 'chest' | 'altar' | 'enemy' | 'exit' | 'trap' | 'fountain' | 'lore'
interface Tile {
  x: number
  y: number
  explored: boolean
  type: TileType
}
interface Skill {
  id: string
  name: string
  cd: number
  remain: number
  onUse: () => void
}

const size = 7
const player = reactive({ x: 0, y: 0, hp: 100, stress: 10, buffs: [] as string[] })
const grid = ref<Tile[]>(Array.from({ length: size * size }, (_, i) => {
  const x = i % size, y = Math.floor(i / size)
  return { x, y, explored: false, type: 'empty' as TileType }
}))
const logs = ref<string[]>([])

const modalOpen = ref(false)
const modalTitle = ref('')
const modalDesc = ref('')
const modalActions = ref<{ label: string; action: () => void; variant?: 'primary' | 'ghost' }[]>([])

function idx(x: number, y: number) { return y * size + x }
function get(x: number, y: number) {
  const t = grid.value[idx(x, y)]
  return t ?? { x, y, explored: false, type: 'empty' as TileType }
}
function inBounds(x: number, y: number) { return x >= 0 && y >= 0 && x < size && y < size }
function isNeighbor(x: number, y: number) { return Math.abs(player.x - x) + Math.abs(player.y - y) === 1 }

function log(msg: string) {
  logs.value.unshift(new Date().toLocaleTimeString() + ' ' + msg)
  if (logs.value.length > 50) logs.value.pop()
}

function generateMap() {
  grid.value = Array.from({ length: size * size }, (_, i) => {
    const x = i % size, y = Math.floor(i / size)
    return { x, y, explored: false, type: 'empty' as TileType }
  })
  get(0, 0).explored = true

  // 放置终点“出口”（遗忘神庙的出口），位于右下角，保证可达
  get(size-1, size-1).type = 'exit'

  // 随机放置丰富但简洁的事件（避免起点与出口）
  placeRandom('chest', 2)
  placeRandom('altar', 1)
  placeRandom('enemy', 2)
  placeRandom('trap', 2)
  placeRandom('fountain', 1)
  placeRandom('lore', 1)

  function placeRandom(type: TileType, count: number) {
    let placed = 0
    let guard = 0
    while (placed < count && guard < 1000) {
      guard++
      const rx = Math.floor(Math.random() * size)
      const ry = Math.floor(Math.random() * size)
      if ((rx === 0 && ry === 0) || (rx === size-1 && ry === size-1)) continue
      const t = get(rx, ry)
      if (t.type === 'empty') {
        t.type = type
        placed++
      }
    }
  }
  log('地牢生成完毕，目标：抵达遗忘神庙的出口')
}

function openModal(title: string, desc: string, actions: { label: string; action: () => void; variant?: 'primary' | 'ghost' }[]) {
  modalTitle.value = title
  modalDesc.value = desc
  modalActions.value = actions
  modalOpen.value = true
}
function closeModal() { modalOpen.value = false }

function applyHeal(amount: number) {
  const before = player.hp
  player.hp = Math.min(100, player.hp + amount)
  log(`治疗 +${player.hp - before} HP`)
}
function reduceStress(amount: number) {
  const before = player.stress
  player.stress = Math.max(0, player.stress - amount)
  log(`压力 -${before - player.stress}`)
}
function addBuff(buff: string) {
  if (!player.buffs.includes(buff)) {
    player.buffs.push(buff)
    log(`获得增益：${buff}`)
  }
}

function triggerEvent(tile: Tile) {
  if (tile.type === 'chest') {
    openModal('发现宝箱', '你在角落发现了一个尘封的宝箱。', [
      { label: '打开宝箱', action: () => { addBuff('幸运'); applyHeal(10); tile.type = 'empty'; closeModal(); }, variant: 'primary' },
      { label: '忽略', action: closeModal, variant: 'ghost' }
    ])
  } else if (tile.type === 'altar') {
    openModal('古老祭坛', '心境平和，愿在此祈祷吗？', [
      { label: '祈祷（减压）', action: () => { reduceStress(15); tile.type = 'empty'; closeModal(); }, variant: 'primary' },
      { label: '离开', action: closeModal, variant: 'ghost' }
    ])
  } else if (tile.type === 'enemy') {
    openModal('遭遇敌人', '潜伏的敌人现身。', [
      { label: '进入战斗', action: () => { log('进入战斗（占位示例）'); tile.type = 'empty'; closeModal(); }, variant: 'primary' },
      { label: '撤退', action: closeModal, variant: 'ghost' }
    ])
  } else if (tile.type === 'trap') {
    openModal('暗影陷阱', '你踩中了机关！', [
      { label: '强行挣脱 (-HP)', action: () => { player.hp = Math.max(0, player.hp - 15); log('陷阱伤害 -15 HP'); tile.type = 'empty'; closeModal(); }, variant: 'primary' },
      { label: '冷静应对 (-压力)', action: () => { player.stress = Math.max(0, player.stress - 5); log('冷静处理 -5 压力'); tile.type = 'empty'; closeModal(); }, variant: 'ghost' }
    ])
  } else if (tile.type === 'fountain') {
    openModal('回复喷泉', '清冽的泉水涌动。', [
      { label: '饮用 (+HP)', action: () => { applyHeal(18); tile.type = 'empty'; closeModal(); }, variant: 'primary' },
      { label: '净心 (-压力)', action: () => { reduceStress(12); tile.type = 'empty'; closeModal(); }, variant: 'ghost' }
    ])
  } else if (tile.type === 'lore') {
    openModal('古籍手札', '你读到了古文明的片段。', [
      { label: '记下线索', action: () => { addBuff('洞察'); log('获得线索：遗忘神庙的门扉刻着曙光符号'); tile.type = 'empty'; closeModal(); }, variant: 'primary' },
      { label: '合上手札', action: closeModal, variant: 'ghost' }
    ])
  } else if (tile.type === 'exit') {
    openModal('找到出口', '你抵达了遗忘神庙的出口。是否离开地牢？', [
      { label: '离开地牢', action: () => { log('通关：成功离开地牢！'); closeModal(); }, variant: 'primary' },
      { label: '再探索一下', action: closeModal, variant: 'ghost' }
    ])
  }
}

function moveTo(x: number, y: number) {
  if (!inBounds(x, y) || !isNeighbor(x, y)) return
  player.x = x
  player.y = y
  const t = get(x, y)
  if (!t.explored) {
    t.explored = true
    log(`探索 (${x}, ${y})`)
  } else {
    log(`移动至 (${x}, ${y})`)
  }
  if (t.type !== 'empty') triggerEvent(t)
}

const legend = [
  { label: '未探索', cls: 'bg-slate-900 border-slate-800' },
  { label: '已探索', cls: 'bg-slate-700 border-slate-600' },
  { label: '玩家', cls: 'ring-2 ring-sky-400' },
  { label: '出口', cls: 'text-emerald-300' },
  { label: '宝箱', cls: 'text-amber-300' },
  { label: '祭坛', cls: 'text-fuchsia-300' },
  { label: '敌人', cls: 'text-rose-300' },
  { label: '陷阱', cls: 'text-orange-300' },
  { label: '喷泉', cls: 'text-cyan-300' },
  { label: '手札', cls: 'text-indigo-300' },
]

const skills = ref<Skill[]>([])
function tickCooldown() { skills.value.forEach(s => { if (s.remain > 0) s.remain-- }) }
function useSkill(s: Skill) { if (s.remain > 0) return; s.onUse(); s.remain = s.cd }

onMounted(() => {
  generateMap()
  skills.value = [
    { id: 'slash',  name: '斩击', cd: 3, remain: 0, onUse: () => log('使用技能：斩击') },
    { id: 'block',  name: '格挡', cd: 5, remain: 0, onUse: () => addBuff('格挡') },
    { id: 'heal',   name: '治疗', cd: 4, remain: 0, onUse: () => applyHeal(12) },
    { id: 'sprint', name: '疾跑', cd: 6, remain: 0, onUse: () => reduceStress(8) },
  ]
  setInterval(tickCooldown, 1000)
})

const rightInfo = computed(() => {
  const t = get(player.x, player.y)
  return {
    pos: `(${player.x}, ${player.y})`,
    explored: t.explored ? '是' : '否',
    type: t.type === 'empty' ? '普通区域' : (t.type === 'chest' ? '宝箱' : t.type === 'altar' ? '祭坛' : '敌人')
  }
})
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gradient-to-b from-slate-900 to-slate-950">
    <StatusBar />

    <div class="flex-1 p-6 grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 地图 -->
      <div class="lg:col-span-3 card rounded-2xl p-4 border border-slate-700/60 shadow-lg shadow-black/20">
        <div class="mb-3 flex items-center justify-between">
          <div class="font-semibold">地牢地图</div>
          <div class="flex flex-wrap gap-2 text-xs text-slate-300">
            <span v-for="l in legend" :key="l.label" class="inline-flex items-center gap-1">
              <i :class="['inline-block w-3 h-3 rounded-sm border', l.cls]" /> {{ l.label }}
            </span>
          </div>
        </div>

        <div class="grid" :style="{ gridTemplateColumns: `repeat(${size}, minmax(0, 1fr))` }">
          <template v-for="y in size" :key="'row-'+y">
            <button
              v-for="x in size"
              :key="`${x-1}-${y-1}`"
              class="aspect-square m-1 rounded-md border transition relative overflow-hidden disabled:opacity-50 disabled:cursor-not-allowed"
              :class="[
                get(x-1,y-1).explored ? 'bg-slate-700 border-slate-600' : 'bg-slate-900 border-slate-800',
                isNeighbor(x-1, y-1) ? 'hover:bg-slate-800 hover:border-sky-400' : '',
                (player.x === x-1 && player.y === y-1) ? 'ring-2 ring-sky-400' : ''
              ]"
              :disabled="!isNeighbor(x-1, y-1)"
              @click="isNeighbor(x-1, y-1) && moveTo(x-1, y-1)"
              :title="`(${x-1}, ${y-1})`"
            >
              <!-- 特殊房间图标 -->
              <div v-if="get(x-1,y-1).type !== 'empty' && get(x-1,y-1).explored"
                   class="absolute inset-0 flex items-center justify-center text-xs">
                <span :class="{
                  'text-emerald-300': get(x-1,y-1).type==='exit',
                  'text-amber-300': get(x-1,y-1).type==='chest',
                  'text-fuchsia-300': get(x-1,y-1).type==='altar',
                  'text-rose-300': get(x-1,y-1).type==='enemy',
                  'text-orange-300': get(x-1,y-1).type==='trap',
                  'text-cyan-300': get(x-1,y-1).type==='fountain',
                  'text-indigo-300': get(x-1,y-1).type==='lore'
                }">
                  {{
                    get(x-1,y-1).type === 'exit' ? '出口' :
                    get(x-1,y-1).type === 'chest' ? '宝箱' :
                    get(x-1,y-1).type === 'altar' ? '祭坛' :
                    get(x-1,y-1).type === 'enemy' ? '敌人' :
                    get(x-1,y-1).type === 'trap' ? '陷阱' :
                    get(x-1,y-1).type === 'fountain' ? '喷泉' : '手札'
                  }}
                </span>
              </div>
              <!-- 未探索的特殊房间：不显示图标，保持神秘 -->
              <!-- 玩家位置点 -->
              <div v-if="player.x === x-1 && player.y === y-1"
                   class="absolute bottom-1 right-1 w-2 h-2 rounded-full bg-sky-400"></div>
            </button>
          </template>
        </div>

        <div class="text-xs text-slate-400 mt-2">提示：只能移动到相邻的上下左右格子。</div>
      </div>

      <!-- 右侧：当前格信息 + 日志 -->
      <div class="card rounded-2xl p-4 border border-slate-700/60 shadow-lg shadow-black/20 flex flex-col">
        <div class="font-semibold mb-2">当前位置</div>
        <div class="text-sm text-slate-200 space-y-1">
          <div>坐标：{{ rightInfo.pos }}</div>
          <div>已探索：{{ rightInfo.explored }}</div>
          <div>地块类型：{{ rightInfo.type }}</div>
          <div class="mt-2">
            <div>生命值：{{ player.hp }}</div>
            <div>压力值：{{ player.stress }}</div>
            <div>增益：<span v-if="player.buffs.length===0" class="text-slate-400">无</span><span v-else>{{ player.buffs.join('、') }}</span></div>
          </div>
        </div>

        <div class="font-semibold mt-4 mb-2">事件日志</div>
        <div class="flex-1 overflow-auto rounded-md bg-slate-900/60 border border-slate-700/60 p-2 text-xs space-y-1">
          <div v-for="(l, i) in logs" :key="i" class="text-slate-300">{{ l }}</div>
          <div v-if="logs.length===0" class="text-slate-500">尚无事件</div>
        </div>
      </div>
    </div>

    <!-- 底部技能快捷栏 -->
    <div class="status-bar px-6 py-3 flex gap-3">
      <button
        v-for="s in skills"
        :key="s.id"
        class="action-button rounded-button px-4 py-2 relative disabled:opacity-50"
        :disabled="s.remain>0"
        @click="useSkill(s)"
      >
        <span>{{ s.name }}</span>
        <span v-if="s.remain>0" class="absolute -top-1 -right-1 text-xs bg-slate-800 border border-slate-600 px-1 rounded">
          {{ s.remain }}
        </span>
      </button>
    </div>

    <!-- 事件弹窗 -->
    <div v-if="modalOpen" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="w-[min(92vw,520px)] rounded-xl border border-slate-700/60 bg-slate-900 p-5 shadow-2xl">
        <div class="text-lg font-semibold mb-2">{{ modalTitle }}</div>
        <div class="text-slate-300 mb-4">{{ modalDesc }}</div>
        <div class="flex gap-3 justify-end">
          <button
            v-for="(a,i) in modalActions"
            :key="i"
            class="px-4 py-2 rounded-button"
            :class="a.variant==='primary' ? 'action-button' : 'bg-slate-800 hover:bg-slate-700 border border-slate-600 transition-colors'"
            @click="a.action()"
          >
            {{ a.label }}
          </button>
        </div>
      </div>
    </div>

    <AIChat />
  </div>
</template>