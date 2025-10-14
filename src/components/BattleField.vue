<script setup lang="ts">
</script>

<template>
  <main class="battle-area flex-1 py-6 px-4">
    <!-- 敌方战场 -->
    <div class="mb-8">
      <div class="flex justify-center mb-2">
        <div class="text-sm text-gray-400">敌方战场</div>
      </div>
      <div class="flex justify-center space-x-4">
        <div class="card card-minion w-24 h-32 flex flex-col items-center p-2">
          <div class="text-center font-bold text-sm">腐化巨龙</div>
          <div class="flex-1 flex items-center justify-center">
            <i class="fas fa-dragon text-2xl text-red-500"></i>
          </div>
          <div class="flex justify-between w-full text-xs">
            <div class="text-red-500">6</div>
            <div class="text-green-500">60</div>
          </div>
        </div>
        <div class="card card-minion w-24 h-32 flex flex-col items-center p-2">
          <div class="text-center font-bold text-sm">暗影猎手</div>
          <div class="flex-1 flex items-center justify-center">
            <i class="fas fa-user-ninja text-2xl text-purple-500"></i>
          </div>
          <div class="flex justify-between w-full text-xs">
            <div class="text-red-500">3</div>
            <div class="text-green-500">25</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分隔线 -->
    <div class="flex justify-center my-4">
      <div class="h-px bg-gray-600 w-3/4"></div>
    </div>

    <!-- 我方战场 -->
    <div class="mt-8">
      <div class="flex justify-center mb-2">
        <div class="text-sm text-gray-400">我方战场</div>
      </div>
      <div class="flex justify-center space-x-4">
        <div class="card card-minion w-24 h-32 flex flex-col items-center p-2">
          <div class="text-center font-bold text-sm">暗影刺客</div>
          <div class="flex-1 flex items-center justify-center">
            <i class="fas fa-user-ninja text-2xl text-blue-500"></i>
          </div>
          <div class="flex justify-between w-full text-xs">
            <div class="text-red-500">4</div>
            <div class="text-green-500">45</div>
          </div>
        </div>
        <div class="card card-minion w-24 h-32 flex flex-col items-center p-2">
          <div class="text-center font-bold text-sm">圣光骑士</div>
          <div class="flex-1 flex items-center justify-center">
            <i class="fas fa-horse-head text-2xl text-yellow-500"></i>
          </div>
          <div class="flex justify-between w-full text-xs">
            <div class="text-red-500">2</div>
            <div class="text-green-500">30</div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>