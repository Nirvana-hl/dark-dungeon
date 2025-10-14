<script setup lang="ts">
import { useCharactersStore } from '@/stores/characters'
import AIChat from '@/components/AIChat.vue'
const chars = useCharactersStore()

function addChar() {
  const rand = Math.random()
  chars.create({
    name: rand > 0.5 ? '流萤' : '海盐',
    cls: rand > 0.66 ? '战士' : rand > 0.33 ? '法师' : '游侠'
  })
}

function removeChar(id: string) {
  if (confirm('确定删除该角色？')) chars.remove(id)
}
</script>

<template>
  <div class="min-h-screen p-6 bg-gradient-to-b from-slate-900 to-slate-950">
    <div class="text-2xl font-bold mb-4">营地</div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 角色卡片列表 -->
      <div class="lg:col-span-2 space-y-4">
        <div class="flex justify-between items-center">
          <div class="text-lg font-semibold">角色选择</div>
          <div class="flex gap-2">
            <button class="action-button rounded-button px-4 py-2" @click="addChar">创建角色</button>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="c in chars.list" :key="c.id" class="card rounded-2xl p-4 border border-slate-700">
            <div class="flex justify-between items-center">
              <div class="font-semibold flex items-center gap-2">
                {{ c.name }} · {{ c.cls }}
                <span class="text-yellow-400">
                  {{ '★'.repeat(c.stars) }}{{ '☆'.repeat(Math.max(0, 5 - c.stars)) }}
                </span>
              </div>
              <div class="flex gap-2">
                <button class="action-button rounded-button px-3 py-1" @click="chars.select(c.id)">选择</button>
                <button class="action-button rounded-button px-3 py-1" @click="chars.starUp(c.id)">升星</button>
                <button class="action-button rounded-button px-3 py-1" @click="removeChar(c.id)">删除</button>
              </div>
            </div>
            <div class="mt-2 text-sm text-gray-300">生命: {{ c.hp }} | 法力: {{ c.mp }}</div>
          </div>
        </div>
      </div>

      <!-- 营地互动 + 任务板 -->
      <div class="space-y-4">
        <div class="card rounded-2xl p-4">
          <div class="font-semibold mb-2">营地互动</div>
          <ul class="text-sm list-disc ml-5 space-y-1">
            <li>装备强化（占位）</li>
            <li>技能学习（占位）</li>
            <li>药水制作（占位）</li>
          </ul>
        </div>

        <div class="card rounded-2xl p-4">
          <div class="font-semibold mb-2">任务板</div>
          <ul class="text-sm space-y-1">
            <li>· 探索遗忘神庙（奖励：金币 120 / 经验 150）</li>
            <li>· 击败洞窟巨蜥（奖励：稀有武器图纸）</li>
            <li>· 收集药草样本（奖励：治疗药水 x2）</li>
          </ul>
        </div>
      </div>
    </div>

    <AIChat />
  </div>
</template>