<script setup lang="ts">
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- 全局导航（与整体风格一致的暗色条） -->
    <nav class="status-bar py-3 px-6 flex items-center gap-3 flex-wrap">
      <RouterLink to="/" class="action-button px-3 py-2 rounded-button">战斗</RouterLink>
      <RouterLink to="/camp" class="action-button px-3 py-2 rounded-button">营地</RouterLink>
      <RouterLink to="/explore" class="action-button px-3 py-2 rounded-button">探索</RouterLink>
      <RouterLink to="/summary" class="action-button px-3 py-2 rounded-button">结算</RouterLink>
      <RouterLink to="/skills" class="action-button px-3 py-2 rounded-button">技能树</RouterLink>
      <RouterLink to="/settings" class="action-button px-3 py-2 rounded-button">设置</RouterLink>
    </nav>
    <RouterView />
  </div>
</template>