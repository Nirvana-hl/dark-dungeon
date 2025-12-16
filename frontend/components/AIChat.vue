<script setup lang="ts">
import { ref, nextTick } from 'vue'

interface Msg { role: 'user' | 'assistant'; text: string }
const messages = ref<Msg[]>([
  { role: 'assistant', text: '你好，我是你的战术顾问。需要我为本次探索提供建议吗？' }
])
const input = ref('')
const open = ref(true)
const listEl = ref<HTMLDivElement | null>(null)

function reply(user: string) {
  const u = user.toLowerCase()
  let text = '已记录你的问题，我会在后续版本中给出更详细建议。'
  if (u.includes('boss') || u.includes('首领')) text = '建议先清理小怪，保留控制与位移技能，BOSS 期间优先规避高伤技能再输出。'
  else if (u.includes('加点') || u.includes('构筑') || u.includes('build')) text = '当前版本战士推荐力量优先，其次体质；法师走高法强与回蓝；游侠敏捷优先。'
  else if (u.includes('地图') || u.includes('路线')) text = '优先探索含宝箱/祭坛的分支，再进入精英或 BOSS 区域，注意保留逃生道具。'
  messages.value.push({ role: 'assistant', text })
  nextTick(() => { listEl.value?.scrollTo({ top: listEl.value.scrollHeight, behavior: 'smooth' }) })
}

function send() {
  const txt = input.value.trim()
  if (!txt) return
  messages.value.push({ role: 'user', text: txt })
  input.value = ''
  nextTick(() => { listEl.value?.scrollTo({ top: listEl.value.scrollHeight, behavior: 'smooth' }) })
  setTimeout(() => reply(txt), 300)
}
</script>

<template>
  <view class="fixed right-4 bottom-4 z-40">
    <view class="card rounded-2xl w-80 overflow-hidden shadow-lg" v-show="open">
      <view class="status-bar px-4 py-2 flex items-center justify-between">
        <view class="font-semibold">AI 助手</view>
        <button class="action-button rounded-button px-2 py-1" @click="open = false">收起</button>
      </view>
      <view class="p-3 h-64 overflow-y-auto space-y-2" ref="listEl">
        <view v-for="(m,i) in messages" :key="i" class="text-sm" :class="m.role === 'user' ? 'text-right' : 'text-left'">
          <text class="inline-block px-3 py-2 rounded-lg" :class="m.role === 'user' ? 'bg-indigo-600' : 'bg-slate-700'">{{ m.text }}</text>
        </view>
      </view>
      <view class="p-3 flex gap-2">
        <input class="flex-1 rounded-md px-3 py-2 bg-slate-800 border border-slate-600 outline-none" v-model="input" placeholder="问我战术/构筑/路线…" @keydown.enter="send" />
        <button class="action-button rounded-button px-3 py-2" @click="send">发送</button>
      </view>
    </view>
    <button v-show="!open" class="action-button rounded-button px-4 py-2" @click="open = true">AI</button>
  </view>
</template>