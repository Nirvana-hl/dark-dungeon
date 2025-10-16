<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCharactersStore } from '@/stores/characters'
import { supabase } from '@/lib/supabase'
import AIChat from '@/components/AIChat.vue'
import { useWalletStore } from '@/stores/wallet'

const chars = useCharactersStore()
const wallet = useWalletStore()

// 数据库手牌（用户的角色/法术/装备）
import { useGameStore } from '@/stores/game'
const game = useGameStore()
const userCards = ref<Array<{ name: string; type: 'character' | 'spell' | 'equipment'; attack?: number; health?: number; effect?: string; stars?: number; quantity?: number }>>([])

// 按类型分组的手牌
const cardsCharacters = computed(() => userCards.value.filter(c => c.type === 'character'))
const cardsSpells = computed(() => userCards.value.filter(c => c.type === 'spell'))
const cardsEquipment = computed(() => userCards.value.filter(c => c.type === 'equipment'))

async function loadUserCards() {
  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (!uid) return
  const { data, error } = await supabase.from('user_cards').select('*').eq('user_id', uid)
  if (error) return
  if (!data || data.length === 0) {
    // 首次为空则插入默认手牌（角色+法术）
    const defaults = [
      { name: '新兵', type: 'character', attack: 1, health: 1, stars: 1, quantity: 2 },
      { name: '盾卫', type: 'character', attack: 2, health: 3, stars: 1, quantity: 1 },
      { name: '祭司', type: 'character', attack: 2, health: 4, stars: 1, quantity: 1 },
      { name: '火球术', type: 'spell', effect: 'fireball3', quantity: 2 },
      { name: '战旗', type: 'equipment', effect: 'teamBuffAtk1', quantity: 1 }
    ]
    await supabase.from('user_cards').insert(defaults.map(d => ({ user_id: uid, ...d })))
    const { data: after } = await supabase.from('user_cards').select('*').eq('user_id', uid)
    userCards.value = after ?? []
  } else {
    userCards.value = data
  }
}

function syncToBattle() {
  const charsForBattle = userCards.value
    .filter(c => c.type === 'character')
    .map(c => ({ name: c.name, attack: Number(c.attack ?? 1), health: Number(c.health ?? 1) }))
  const itemsForBattle = userCards.value
    .filter(c => c.type !== 'character')
    .map(c => ({
      name: c.name,
      effect: (c.effect as any) ?? (c.type === 'spell' ? 'fireball3' : 'teamBuffAtk1'),
      cost: c.type === 'spell' ? 2 : 3
    }))
  game.setDeckFromCamp(charsForBattle, itemsForBattle)
}

/**
 * 根据角色名称与星级，计算属性与特性
 * 可按需扩展不同角色的成长与特性描述
 */
function calcCharStats(name: string, stars: number): { attack: number; health: number; trait: string } {
  const base: Record<string, { attack: number; health: number; trait: string }> = {
    '游侠·莉雅': { attack: 3, health: 4, trait: '敏捷射术' },
    '法师·应龙': { attack: 4, health: 3, trait: '元素亲和' },
    '战士·江陵': { attack: 3, health: 6, trait: '钢铁意志' },
    '新兵': { attack: 1, health: 1, trait: '基础训练' },
    '盾卫': { attack: 2, health: 3, trait: '重盾防御' },
    '祭司': { attack: 2, health: 4, trait: '祈福治疗' }
  }
  const b = base[name] ?? { attack: 2, health: 3, trait: '通用适应' }
  // 简单成长曲线：每升一星，攻防固定比例成长
  const atk = Math.round(b.attack * (1 + (stars - 1) * 0.25))
  const hp = Math.round(b.health * (1 + (stars - 1) * 0.3))
  const traitTier = stars >= 5 ? 'MAX' : stars >= 4 ? 'IV' : stars >= 3 ? 'III' : stars >= 2 ? 'II' : 'I'
  const trait = `${b.trait}·${traitTier}`
  return { attack: atk, health: hp, trait }
}
const traitsMap = ref<Record<string, { trait_key: string; base_power: number; power_per_star: number; description: string }>>({})
async function loadTraits() {
  const { data, error } = await supabase
    .from('character_traits')
    .select('name, trait_key, base_power, power_per_star, description')
  if (error) return
  traitsMap.value = Object.fromEntries((data ?? []).map((r: any) => [r.name, {
    trait_key: r.trait_key,
    base_power: Number(r.base_power ?? 1),
    power_per_star: Number(r.power_per_star ?? 0),
    description: r.description ?? ''
  }]))
}
function charTrait(name: string, stars: number): string {
  const t = traitsMap.value[name]
  if (!t) {
    // 回退到本地计算
    return calcCharStats(name, stars).trait
  }
  const tier = stars >= 5 ? 'MAX' : stars >= 4 ? 'IV' : stars >= 3 ? 'III' : stars >= 2 ? 'II' : 'I'
  return `${t.description}·${tier}`
}

// 合成：当数量 ≥ 3 时，可消耗 3 张进行合成（角色升星+1；法术升级效果；装备维持默认）
async function combineItem(uc: { id?: string; name: string; type: 'character' | 'spell' | 'equipment'; quantity?: number; stars?: number; effect?: string }) {
  // 查找该手牌记录的 id（按 name+type）
  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (!uid) return

  if (!uc.id) {
    const { data: found } = await supabase
      .from('user_cards')
      .select('id, quantity, stars, effect, attack, health')
      .eq('user_id', uid)
      .eq('name', uc.name)
      .eq('type', uc.type)
      .limit(1)
      .maybeSingle()
    if (!found?.id) return
    uc.id = found.id
    uc.quantity = found.quantity ?? uc.quantity
    uc.stars = found.stars ?? uc.stars
    uc.effect = found.effect ?? uc.effect
  }

  const qty = Number(uc.quantity ?? 0)
  if (qty < 3) return

  let newStars = uc.stars ?? 1
  let newEffect = uc.effect

  if (uc.type === 'character') {
    newStars = Math.min(5, (uc.stars ?? 1) + 1)
  } else if (uc.type === 'spell') {
    // 简单升级效果映射
    const upgradeMap: Record<string, string> = {
      fireball3: 'fireball5',
      icebolt2: 'icebolt4',
      heal3: 'heal5'
    }
    if (uc.effect) newEffect = upgradeMap[uc.effect] ?? uc.effect
  }
  // 装备合成不改变 effect

  // 数量减 2（3 合 1）；角色升星后重算 attack/health
  const updateFields: any = {
    quantity: qty - 2,
    stars: newStars,
    effect: newEffect
  }
  if (uc.type === 'character') {
    const stats = calcCharStats(uc.name, newStars)
    updateFields.attack = stats.attack
    updateFields.health = stats.health
  }
  const { error } = await supabase
    .from('user_cards')
    .update(updateFields)
    .eq('id', uc.id)
  if (error) return

  await loadUserCards()
}

// 商店数据
type ShopItem = { offerId: string; itemId: string; name: string; type: string; price: number; description?: string }
const shop = ref<ShopItem[]>([])
const loadingShop = ref(false)
const buyMsg = ref<string | null>(null)

// 商店类型徽章着色
function badgeClass(t: string) {
  switch ((t || '').toLowerCase()) {
    case 'weapon':
      return 'bg-red-900/50 border-red-700/60 text-red-200'
    case 'armor':
      return 'bg-blue-900/50 border-blue-700/60 text-blue-200'
    case 'consumable':
      return 'bg-emerald-900/50 border-emerald-700/60 text-emerald-200'
    case 'spell':
      return 'bg-purple-900/50 border-purple-700/60 text-purple-200'
    case 'character':
      return 'bg-amber-900/50 border-amber-700/60 text-amber-200'
    default:
      return 'bg-slate-800 border-slate-600 text-slate-300'
  }
}

// 背包与道具使用
type BagItem = { invId: string; name: string; type: string; description?: string; quantity: number; attrs: any }
const bag = ref<BagItem[]>([])
const loadingBag = ref(false)

async function loadBag() {
  loadingBag.value = true
  bag.value = []
  if (!selectedChar.value) {
    loadingBag.value = false
    return
  }
  const { data, error } = await supabase
    .from('inventory')
    .select('id, quantity, items(name,type,description,attrs)')
    .eq('character_id', selectedChar.value.id)
  loadingBag.value = false
  if (error) return
  bag.value = (data ?? []).map((r: any) => ({
    invId: r.id,
    quantity: r.quantity,
    name: r.items?.name ?? '',
    type: r.items?.type ?? '',
    description: r.items?.description ?? '',
    attrs: r.items?.attrs ?? {}
  }))
}

// 使用道具：根据 attrs.heal/mpRestore/starShard 应用效果
async function useItem(b: BagItem) {
  if (!selectedChar.value) return
  const heal = Number(b.attrs?.heal ?? 0)
  const mpRestore = Number(b.attrs?.mpRestore ?? 0)
  const starShard = Boolean(b.attrs?.starShard ?? false)

  // 更新角色
  const newHp = selectedChar.value.hp + heal
  const newMp = selectedChar.value.mp + mpRestore
  let newStars = selectedChar.value.stars
  if (starShard) newStars = Math.min(5, newStars + 1)

  const { error: upErr } = await supabase
    .from('characters')
    .update({ hp: newHp, mp: newMp, stars: newStars })
    .eq('id', selectedChar.value.id)
  if (upErr) {
    buyMsg.value = '使用失败：' + upErr.message
    return
  }

  // 扣除库存数量
  const { error: invErr } = await supabase
    .from('inventory')
    .update({ quantity: b.quantity - 1 })
    .eq('id', b.invId)
  if (invErr) {
    buyMsg.value = '扣库存失败：' + invErr.message
    return
  }

  // 刷新本地视图
  await chars.load()
  await loadBag()
  buyMsg.value = '已使用道具'
}

const selectedChar = computed(() => chars.selected)
import { watch } from 'vue'
watch(selectedChar, async () => { await loadBag() })



async function loadShop() {
  loadingShop.value = true
  buyMsg.value = null
  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (!uid) {
    loadingShop.value = false
    buyMsg.value = '未登录，无法加载商店'
    return
  }
  // 从 shop_offers 关联 items 读取
  const { data, error } = await supabase
    .from('shop_offers')
    .select('id, price, items:item_id(id, name, type, description)')
    .eq('user_id', uid)
    .order('created_at', { ascending: false })
  loadingShop.value = false
  if (error) {
    buyMsg.value = '商店加载失败：' + error.message
    return
  }
  const fetched = (data ?? []).map((x: any) => ({
    offerId: x.id,
    itemId: x.items?.id ?? '',
    name: x.items?.name ?? '',
    type: x.items?.type ?? '',
    description: x.items?.description ?? '',
    price: Number(x.price ?? 0)
  }))
  // 仅使用数据库供给
  shop.value = fetched
}

async function buy(item: ShopItem) {
  buyMsg.value = null
  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (!uid) {
    buyMsg.value = '请先登录'
    return
  }
  // 扣金币
  if (!(await wallet.spend(item.price))) {
    buyMsg.value = wallet.errorMsg ?? '金币不足或未登录'
    return
  }

  // 购买：角色/法术/装备 -> 加入手牌；仅消耗品进入背包
  if (item.type === 'character' || item.type === 'spell' || item.type === 'weapon' || item.type === 'armor') {
    if (item.type === 'character') {
      const name = item.name
      const defaults: Record<string, { attack: number; health: number; stars: number }> = {
        '游侠·莉雅': { attack: 3, health: 4, stars: 2 },
        '法师·应龙': { attack: 4, health: 3, stars: 3 },
        '战士·江陵': { attack: 3, health: 6, stars: 3 }
      }
      const base = defaults[name] ?? { attack: 2, health: 3, stars: 1 }
      // 先查是否已有同名角色
      const { data: existChar } = await supabase
        .from('user_cards')
        .select('id, quantity')
        .eq('user_id', uid)
        .eq('name', name)
        .eq('type', 'character')
        .maybeSingle()
      if (existChar?.id) {
        const { error: upErr } = await supabase
          .from('user_cards')
          .update({ quantity: Number(existChar.quantity ?? 0) + 1 })
          .eq('id', existChar.id)
        if (upErr) { buyMsg.value = '购买失败：' + upErr.message; return }
      } else {
        const { error: insErr } = await supabase.from('user_cards').insert({
          user_id: uid,
          name,
          type: 'character',
          attack: base.attack,
          health: base.health,
          stars: base.stars,
          quantity: 1
        })
        if (insErr) { buyMsg.value = '购买失败：' + insErr.message; return }
      }
      await loadUserCards()
      buyMsg.value = `已购买角色 ${item.name}，金币 -${item.price}`
    } else if (item.type === 'spell') {
      const effectMap: Record<string, string> = {
        '火球术': 'fireball3',
        '冰箭术': 'icebolt2',
        '治疗术': 'heal3'
      }
      // 先查是否已有同名法术
      const { data: existSpell } = await supabase
        .from('user_cards')
        .select('id, quantity')
        .eq('user_id', uid)
        .eq('name', item.name)
        .eq('type', 'spell')
        .maybeSingle()
      if (existSpell?.id) {
        const { error: upErr } = await supabase
          .from('user_cards')
          .update({ quantity: Number(existSpell.quantity ?? 0) + 1 })
          .eq('id', existSpell.id)
        if (upErr) { buyMsg.value = '购买失败：' + upErr.message; return }
      } else {
        const { error: insErr } = await supabase.from('user_cards').insert({
          user_id: uid,
          name: item.name,
          type: 'spell',
          effect: effectMap[item.name] ?? 'fireball3',
          quantity: 1
        })
        if (insErr) { buyMsg.value = '购买失败：' + insErr.message; return }
      }
      await loadUserCards()
      buyMsg.value = `已购买法术 ${item.name}，金币 -${item.price}`
    } else {
      // weapon/armor 作为 equipment 加入手牌（例如：皮甲、长剑）
      const { data: existEquip } = await supabase
        .from('user_cards')
        .select('id, quantity')
        .eq('user_id', uid)
        .eq('name', item.name)
        .eq('type', 'equipment')
        .maybeSingle()
      if (existEquip?.id) {
        const { error: upErr } = await supabase
          .from('user_cards')
          .update({ quantity: Number(existEquip.quantity ?? 0) + 1 })
          .eq('id', existEquip.id)
        if (upErr) { buyMsg.value = '购买失败：' + upErr.message; return }
      } else {
        const { error: insErr } = await supabase.from('user_cards').insert({
          user_id: uid,
          name: item.name,
          type: 'equipment',
          quantity: 1
        })
        if (insErr) { buyMsg.value = '购买失败：' + insErr.message; return }
      }
      await loadUserCards()
      buyMsg.value = `已购买装备 ${item.name}，金币 -${item.price}`
    }
    return
  }

  // 购买物品（需要选中角色）
  if (!selectedChar.value) {
    buyMsg.value = '请先选择一个角色再购买物品'
    return
  }

  await chars.load()
  let cloudCharId: string | null = null
  const { data: chById } = await supabase
    .from('characters')
    .select('id')
    .eq('id', selectedChar.value.id)
    .maybeSingle()
  cloudCharId = chById?.id ?? null
  if (!cloudCharId) {
    const { data: chByName } = await supabase
      .from('characters')
      .select('id')
      .eq('name', selectedChar.value.name)
      .eq('class', selectedChar.value.cls)
      .order('created_at', { ascending: true })
      .limit(1)
      .maybeSingle()
    cloudCharId = chByName?.id ?? null
  }
  if (!cloudCharId) {
    const sc = selectedChar.value
    const { error: insCharErr, data: insChar } = await supabase
      .from('characters')
      .insert({
        user_id: uid,
        name: sc.name,
        class: sc.cls,
        level: Math.max(1, Number(sc.stars ?? 1)),
        hp: sc.hp,
        mp: sc.mp,
        stars: sc.stars,
        stress: 0,
        attrs: { mp: sc.mp, stars: sc.stars },
        status: 'idle'
      })
      .select('id')
      .single()
    if (insCharErr) { buyMsg.value = '购买失败：角色同步失败 ' + insCharErr.message; return }
    cloudCharId = insChar?.id ?? null
    if (!cloudCharId) { buyMsg.value = '购买失败：角色同步后仍未获得云端ID'; return }
  }

  const { data: existInv } = await supabase
    .from('inventory')
    .select('id, quantity')
    .eq('character_id', cloudCharId)
    .eq('item_id', item.itemId)
    .maybeSingle()

  if (existInv?.id) {
    const { error: upErr } = await supabase
      .from('inventory')
      .update({ quantity: Number(existInv.quantity ?? 0) + 1 })
      .eq('id', existInv.id)
    if (upErr) { buyMsg.value = '购买失败：' + upErr.message; return }
  } else {
    const { error: insErr } = await supabase
      .from('inventory')
      .insert({
        character_id: cloudCharId,
        item_id: item.itemId,
        quantity: 1,
        equipped: false
      })
    if (insErr) { buyMsg.value = '购买失败：' + insErr.message; return }
  }

  await loadBag()
  buyMsg.value = `已购买物品 ${item.name}，金币 -${item.price}`
}

async function refreshShop() {
  buyMsg.value = null
  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (!uid) {
    buyMsg.value = '请先登录'
    return
  }
  // 扣刷新费用
  if (!(await wallet.spend(50))) {
    buyMsg.value = wallet.errorMsg ?? '金币不足'
    return
  }
  // 随机挑选 items 作为新报价
  const { data: items, error: itemsErr } = await supabase
    .from('items')
    .select('id, type, name, description')
    .order('id', { ascending: true }) // 无 random()，前端简单打乱
    .limit(20)
  if (itemsErr) {
    buyMsg.value = '刷新失败：' + itemsErr.message
    return
  }
  // 简单打乱并取前6
  const arr = (items ?? []).slice()
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  const pick = arr.slice(0, 6)
  const priceOf = (type: string) => (type === 'weapon' ? 150 : type === 'armor' ? 130 : 80)
  // 清空旧报价
  await supabase.from('shop_offers').delete().eq('user_id', uid)
  // 插入新报价
  const payload = pick.map((it: any) => ({
    user_id: uid,
    item_id: it.id,
    price: priceOf(it.type)
  }))
  const { error: insErr } = await supabase.from('shop_offers').insert(payload)
  if (insErr) {
    buyMsg.value = '刷新失败：' + insErr.message
    return
  }
  // 重新加载商店
  await loadShop()
  buyMsg.value = '商店已刷新'
}


function removeChar(id: string) {
  if (confirm('确定删除该角色？')) chars.remove(id)
}

onMounted(async () => {
  await wallet.init()
  await loadShop()
  await loadBag()
  await loadUserCards()
  await loadTraits()
})
</script>

<template>
  <div class="min-h-screen p-6 bg-gradient-to-b from-slate-900 to-slate-950 text-white">
    <div class="flex justify-between items-center mb-4">
      <div class="text-2xl font-bold">营地</div>
      <div class="text-sm text-gray-300">金币：{{ wallet.gold }}</div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧主区域：数据库手牌（占主要部分） -->
      <div class="lg:col-span-2 space-y-4">
        <div class="card rounded-2xl p-4 border border-slate-700/60 bg-slate-900/50">
          <div class="flex justify-between items-center mb-2">
            <div class="font-semibold">手牌</div>
            <button class="action-button rounded-button px-3 py-1" @click="syncToBattle">同步到战斗牌库</button>
          </div>
          <div v-if="userCards.length === 0" class="text-sm text-gray-400">尚无手牌，首次已为你写入默认卡组。</div>
          <div v-else class="space-y-3">
            <!-- 角色 -->
            <div v-if="cardsCharacters.length">
              <div class="text-xs text-slate-400 mb-2">角色</div>
              <div class="grid grid-cols-1 gap-2">
                <div
                  v-for="uc in cardsCharacters"
                  :key="'char-' + uc.name"
                  class="rounded-xl border border-slate-700/60 bg-slate-900/60 p-3"
                >
                  <div class="flex justify-between items-center">
                    <div class="font-semibold">
                      {{ uc.name }}
                      <span class="ml-2 text-[10px] px-2 py-[2px] rounded-full border" :class="badgeClass('character')">character</span>
                      <span v-if="uc.quantity" class="ml-2 text-xs px-2 py-[1px] rounded bg-slate-700 text-gray-200">×{{ uc.quantity }}</span>
                    </div>
                    <div class="text-xs text-gray-400">
                      ATK {{ uc.attack ?? 0 }} / HP {{ uc.health ?? 1 }}
                      <span class="ml-2 text-[11px] text-slate-300">特性：{{ charTrait(uc.name, (uc.stars ?? 1)) }}</span>
                    </div>
                  </div>
                  <div class="mt-2 flex justify-end">
                    <button
                      v-if="(uc.quantity ?? 0) >= 3"
                      class="action-button rounded-button px-3 py-1"
                      @click="combineItem(uc)"
                    >
                      合成
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <!-- 法术 -->
            <div v-if="cardsSpells.length">
              <div class="text-xs text-slate-400 mb-2">法术</div>
              <div class="grid grid-cols-1 gap-2">
                <div
                  v-for="uc in cardsSpells"
                  :key="'spell-' + uc.name + (uc.effect || '')"
                  class="rounded-xl border border-slate-700/60 bg-slate-900/60 p-3"
                >
                  <div class="flex justify-between items-center">
                    <div class="font-semibold">
                      {{ uc.name }}
                      <span class="ml-2 text-[10px] px-2 py-[2px] rounded-full border" :class="badgeClass('spell')">spell</span>
                      <span v-if="uc.quantity" class="ml-2 text-xs px-2 py-[1px] rounded bg-slate-700 text-gray-200">×{{ uc.quantity }}</span>
                    </div>
                    <div class="text-xs text-gray-400">
                      法术：{{ uc.effect || '未知' }}
                    </div>
                  </div>
                  <div class="mt-2 flex justify-end">
                    <button
                      v-if="(uc.quantity ?? 0) >= 3"
                      class="action-button rounded-button px-3 py-1"
                      @click="combineItem(uc)"
                    >
                      合成
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <!-- 装备 -->
            <div v-if="cardsEquipment.length">
              <div class="text-xs text-slate-400 mb-2">装备</div>
              <div class="grid grid-cols-1 gap-2">
                <div
                  v-for="uc in cardsEquipment"
                  :key="'equip-' + uc.name + (uc.effect || '')"
                  class="rounded-xl border border-slate-700/60 bg-slate-900/60 p-3"
                >
                  <div class="flex justify-between items-center">
                    <div class="font-semibold">
                      {{ uc.name }}
                      <span class="ml-2 text-[10px] px-2 py-[2px] rounded-full border bg-indigo-900/50 border-indigo-700/60 text-indigo-200">equipment</span>
                      <span v-if="uc.quantity" class="ml-2 text-xs px-2 py-[1px] rounded bg-slate-700 text-gray-200">×{{ uc.quantity }}</span>
                    </div>
                    <div class="text-xs text-gray-400">
                      装备：{{ uc.effect || '未知' }}
                    </div>
                  </div>
                  <div class="mt-2 flex justify-end">
                    <button
                      v-if="(uc.quantity ?? 0) >= 3"
                      class="action-button rounded-button px-3 py-1"
                      @click="combineItem(uc)"
                    >
                      合成
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：商店 + 背包使用 -->
      <div class="space-y-4">
        <!-- 营地商店 -->
        <div class="card rounded-2xl p-4 border border-slate-700/60 bg-slate-900/50 shadow-lg shadow-black/20">
          <div class="flex justify-between items-center mb-3">
            <div class="text-lg font-semibold">营地商店</div>
            <div class="flex items-center gap-3">
              <div class="text-xs text-gray-400">
                购买角色/法术会加入数据库手牌；物品将进入所选角色背包。
              </div>
              <button class="action-button rounded-button px-3 py-1" @click="refreshShop">刷新（¥50）</button>
            </div>
          </div>

          <div v-if="loadingShop" class="text-sm text-gray-400">正在加载商店...</div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div v-for="it in shop" :key="it.offerId" class="rounded-xl border border-slate-700/60 bg-slate-900/60 p-4 transition hover:border-slate-500 hover:bg-slate-800/60 hover:translate-y-[1px]">
              <div class="flex justify-between items-center">
                <div class="font-semibold">
                  {{ it.name }}
                  <span class="ml-2 px-2 py-[2px] text-[10px] rounded-full border" :class="badgeClass(it.type)">
                    {{ it.type }}
                  </span>
                </div>
                <div class="text-yellow-400">¥{{ it.price }}</div>
              </div>
              <div class="text-sm text-gray-300 mt-2 line-clamp-2">{{ it.description || '暂无描述' }}</div>
              <div class="mt-3 flex justify-end">
                <button class="action-button rounded-button px-3 py-1" @click="buy(it)">购买</button>
              </div>
            </div>
          </div>
          <div v-if="buyMsg" class="mt-2 text-sm" :class="buyMsg.includes('失败') ? 'text-red-400' : 'text-emerald-400'">{{ buyMsg }}</div>
        </div>

        <!-- 背包与道具使用 -->
        <div class="card rounded-2xl p-4 border border-slate-700/60 bg-slate-900/50">
          <div class="font-semibold mb-2">
            背包与道具使用
            <span v-if="selectedChar" class="ml-2 text-xs text-slate-400">当前：{{ selectedChar.name }}（★{{ selectedChar.stars }}）</span>
          </div>
          <div v-if="!selectedChar" class="text-sm text-gray-400">未选择角色，无法使用物品。请在其他界面选择角色后返回。</div>
          <div v-else>
            <div v-if="loadingBag" class="text-sm text-gray-400">背包加载中...</div>
            <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3">
              <div v-for="b in bag" :key="b.invId" class="rounded-xl border border-slate-700/60 bg-slate-900/60 p-3 transition hover:border-slate-500 hover:bg-slate-800/60 hover:translate-y-[1px]">
                <div class="flex justify-between items-center">
                  <div class="font-semibold">
                    {{ b.name }}
                    <span class="ml-2 text-xs px-2 py-[2px] rounded-full border" :class="badgeClass(b.type)">{{ b.type }}</span>
                    <span class="ml-2 text-xs px-2 py-[1px] rounded bg-slate-700 text-gray-200">×{{ b.quantity }}</span>
                  </div>
                  <div class="flex gap-2">
                    <button class="action-button rounded-button px-2 py-1" @click="useItem(b)" :disabled="b.quantity <= 0">使用</button>
                  </div>
                </div>
                <div class="mt-1 text-xs text-gray-300">{{ b.description || '无描述' }}</div>
                <div class="mt-1 text-xs text-gray-500">效果：回血 {{ b.attrs?.heal ?? 0 }}，回蓝 {{ b.attrs?.mpRestore ?? 0 }}，升星碎片 {{ b.attrs?.starShard ? '是' : '否' }}</div>
              </div>
              <div v-if="bag.length===0" class="text-xs text-gray-500">暂无道具。可在左侧商店购买后刷新背包。</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <AIChat />
  </div>
</template>