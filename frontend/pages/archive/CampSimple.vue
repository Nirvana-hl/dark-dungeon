<template>
  <div class="camp-simple">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🏕️ 营地</h1>
      <p>管理你的角色和准备下一次冒险</p>
    </div>

    <!-- 主角面板 -->
    <section class="hero-panel">
      <h2>🦸 主角信息</h2>
      <div class="hero-info">
        <div class="avatar-placeholder">
          <i class="fas fa-user"></i>
        </div>
        <div class="hero-details">
          <h3>冒险者</h3>
          <div class="stats">
            <div class="stat">
              <span class="label">等级:</span>
              <span class="value">1</span>
            </div>
            <div class="stat">
              <span class="label">生命值:</span>
              <span class="value health">100/100</span>
            </div>
            <div class="stat">
              <span class="label">压力值:</span>
              <span class="value stress">25/100</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 角色管理 -->
    <section class="character-management">
      <h2>👥 可用角色</h2>
      <div class="characters-grid">
        <CharacterCardSimple
          v-for="character in characters"
          :key="character.id"
          :character="character"
          :deployed="character.isDeployed"
          :can-deploy="canDeployCharacter"
          @deploy="deployCharacter"
          @undeploy="undeployCharacter"
          @click="selectCharacter"
        />
      </div>
      
      <div v-if="characters.length === 0" class="empty-state">
        <i class="fas fa-users"></i>
        <p>暂无可用角色</p>
      </div>
    </section>

    <!-- 营地设施 -->
    <section class="camp-facilities">
      <h2>🏛️ 营地设施</h2>
      <div class="facilities-grid">
        <div class="facility-card" @click="useTavern">
          <div class="facility-icon">🍺</div>
          <h3>酒馆</h3>
          <p>缓解压力，恢复士气</p>
          <div class="facility-cost">💰 10金币</div>
        </div>
        
        <div class="facility-card" @click="useChapel">
          <div class="facility-icon">⛪</div>
          <h3>教堂</h3>
          <p>祈祷祝福，净化心灵</p>
          <div class="facility-cost">💰 15金币</div>
        </div>
        
        <div class="facility-card" @click="useSanctum">
          <div class="facility-icon">🏛️</div>
          <h3>圣所</h3>
          <p>冥想修炼，提升境界</p>
          <div class="facility-cost">💰 20金币</div>
        </div>
      </div>
    </section>

    <!-- 快速操作 -->
    <section class="quick-actions">
      <h2>⚡ 快速操作</h2>
      <div class="actions-grid">
        <RouterLink to="/shop" class="action-card shop">
          <div class="action-icon">🛒</div>
          <h3>商城</h3>
          <p>购买道具和装备</p>
        </RouterLink>
        
        <RouterLink to="/skills" class="action-card skills">
          <div class="action-icon">🌟</div>
          <h3>技能</h3>
          <p>升级和解锁技能</p>
        </RouterLink>
        
        <RouterLink to="/explore" class="action-card explore">
          <div class="action-icon">⚡</div>
          <h3>闯关</h3>
          <p>开始新的冒险</p>
        </RouterLink>
      </div>
    </section>

    <!-- 通知提示 -->
    <div v-if="notification" class="notification" :class="notification.type">
      <i :class="notification.icon"></i>
      <span>{{ notification.message }}</span>
      <button @click="notification = null" class="close-btn">
        <i class="fas fa-times"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { UserCardCharacter } from '@/types'

// 模拟数据
const characters = ref<UserCardCharacter[]>([
  {
    id: '1',
    userId: 'user-1',
    cardCharacterId: 'char-1',
    currentHp: 10,
    currentArmor: 0,
    isDeployed: false,
    deployedRound: 0,
    currentStarLevel: 1
  },
  {
    id: '2',
    userId: 'user-1',
    cardCharacterId: 'char-2',
    currentHp: 8,
    currentArmor: 2,
    isDeployed: true,
    deployedRound: 1,
    currentStarLevel: 2
  },
  {
    id: '3',
    userId: 'user-1',
    cardCharacterId: 'char-3',
    currentHp: 12,
    currentArmor: 1,
    isDeployed: false,
    deployedRound: 0,
    currentStarLevel: 1
  }
])

const notification = ref<{
  type: 'success' | 'error' | 'info'
  message: string
  icon: string
} | null>(null)

const canDeployCharacter = ref(true)

// 部署角色
function deployCharacter(character: UserCardCharacter) {
  character.isDeployed = true
  character.deployedRound = 1
  showNotification('success', '角色已成功部署！', 'fas fa-check-circle')
}

// 撤下角色
function undeployCharacter(character: UserCardCharacter) {
  character.isDeployed = false
  character.deployedRound = 0
  showNotification('info', '角色已撤下', 'fas fa-info-circle')
}

// 选择角色
function selectCharacter(character: UserCardCharacter) {
  console.log('Selected character:', character)
}

// 使用酒馆
function useTavern() {
  showNotification('success', '在酒馆休息，压力值减少 20', 'fas fa-beer')
}

// 使用教堂
function useChapel() {
  showNotification('success', '在教堂祈祷，获得祝福效果', 'fas fa-cross')
}

// 使用圣所
function useSanctum() {
  showNotification('success', '在圣所冥想，精神力提升', 'fas fa-om')
}

// 显示通知
function showNotification(type: 'success' | 'error' | 'info', message: string, icon: string) {
  notification.value = { type, message, icon }
  setTimeout(() => {
    notification.value = null
  }, 3000)
}

onMounted(() => {
  // 加载营地数据
  console.log('Camp page loaded')
})
</script>

<style scoped>
.camp-simple {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--text-primary);
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

section {
  background: var(--secondary-bg);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid var(--border-color);
}

section h2 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.hero-panel {
  background: linear-gradient(135deg, var(--secondary-bg), rgba(255, 107, 53, 0.1));
  border-color: #ff6b35;
}

.hero-info {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  background: var(--tertiary-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: var(--text-muted);
  border: 2px solid var(--border-color);
}

.hero-details h3 {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stat {
  display: flex;
  justify-content: space-between;
  min-width: 200px;
  font-size: 0.9rem;
}

.stat .label {
  color: var(--text-secondary);
}

.stat .value {
  font-weight: bold;
  color: var(--text-primary);
}

.stat .value.health {
  color: #4caf50;
}

.stat .value.stress {
  color: #ff9800;
}

.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.facilities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.facility-card {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.facility-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border-color: #ff6b35;
}

.facility-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.facility-card h3 {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.facility-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.facility-cost {
  background: var(--primary-bg);
  color: #ffd700;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: bold;
  display: inline-block;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.action-card {
  display: block;
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  text-decoration: none;
  color: inherit;
}

.action-card.shop:hover {
  border-color: #ffd700;
}

.action-card.skills:hover {
  border-color: #4caf50;
}

.action-card.explore:hover {
  border-color: #ff6b35;
}

.action-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.action-card h3 {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.action-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  z-index: 1000;
  max-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.notification.success {
  background: #4caf50;
  color: white;
}

.notification.error {
  background: #f44336;
  color: white;
}

.notification.info {
  background: #2196f3;
  color: white;
}

.close-btn {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 0;
  margin-left: auto;
}

@media (max-width: 768px) {
  .camp-simple {
    padding: 1rem;
  }
  
  .hero-info {
    flex-direction: column;
    text-align: center;
  }
  
  .characters-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  
  .facilities-grid,
  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>