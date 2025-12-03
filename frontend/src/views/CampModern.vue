<template>
  <div class="camp-container">
    <!-- 主角面板 -->
    <section class="hero-panel">
      <!-- 如果没有角色，显示创建角色提示 -->
      <div v-if="!playerCharacter" class="create-character-prompt">
        <div class="prompt-content">
          <div class="prompt-icon">
            <i class="fas fa-user-plus"></i>
          </div>
          <h2>您还没有创建角色</h2>
          <p>创建您的第一个角色，开始冒险之旅！</p>
          <button class="create-character-btn" @click="showClassModal = true">
            <i class="fas fa-plus"></i>
            创建角色
          </button>
        </div>
      </div>
      
      <!-- 如果有角色，显示角色信息 -->
      <div v-else class="hero-card">
        <div class="hero-avatar">
          <img 
            :src="`/api/character/avatar/${playerCharacter?.playerCharacterId || 'default'}`" 
            :alt="getPlayerCharacterName()"
            @error="handleImageError"
          />
          <div class="level-badge">{{ getPlayerCharacterLevel() }}</div>
        </div>
        <div class="hero-info">
          <h2 class="hero-name">{{ getPlayerCharacterName() }}</h2>
          <div class="hero-stats">
            <div class="stat-row">
              <span class="stat-label">生命值</span>
              <div class="health-bar-container">
                <div class="health-bar">
                  <div 
                    class="health-fill" 
                    :style="{ width: `${healthPercentage}%` }"
                  ></div>
                </div>
                <span class="health-text">{{ playerCharacter?.currentHp }}/{{ playerCharacter?.maxHp }}</span>
              </div>
            </div>
            
            <div class="stat-row">
              <span class="stat-label">行动点</span>
              <div class="action-points">
                <div class="ap-bar">
                  <div 
                    class="ap-fill" 
                    :style="{ width: `${actionPointsPercentage}%` }"
                  ></div>
                </div>
                <span class="ap-text">{{ playerCharacter?.currentActionPoints }}/{{ playerCharacter?.maxActionPoints }}</span>
              </div>
            </div>
            
            <div class="stat-row">
              <span class="stat-label">压力</span>
              <StressIndicator :stress-level="playerCharacter?.stressLevel || 1" />
            </div>
          </div>
        </div>
        
        <!-- 技能树入口 -->
        <div class="skills-section">
          <h3>技能树</h3>
          <button class="skill-tree-btn" @click="openSkillTree">
            <i class="fas fa-tree"></i>
            查看技能
          </button>
        </div>
      </div>
    </section>
    
    <!-- 职业选择弹窗 -->
    <ClassSelectionModal 
      :show="showClassModal" 
      @close="showClassModal = false"
      @complete="handleCharacterCreated"
    />

    <!-- 部署区域 -->
    <section class="deployment-section">
      <div class="section-header">
        <h3>角色部署</h3>
        <div class="deployment-info">
          <span>已部署: {{ deployedCount }}/{{ maxDeployed }}</span>
          <span>剩余行动点: {{ remainingActionPoints }}</span>
        </div>
      </div>
      
      <div class="deployment-grid">
        <div 
          v-for="slot in deploymentSlots" 
          :key="slot.id"
          class="deployment-slot"
          :class="{ 'occupied': slot.character, 'empty': !slot.character }"
          @drop="handleDrop($event, slot.id)"
          @dragover="handleDragOver"
          @dragleave="handleDragLeave"
        >
          <CharacterCard 
            v-if="slot.character"
            :character="slot.character"
            :deployed="true"
            @undeploy="undeployCharacter"
          />
          <div v-else class="empty-slot">
            <i class="fas fa-plus"></i>
            <span>拖拽角色到此处</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 卡组管理 -->
    <section class="deck-management">
      <div class="section-header">
        <h3>卡组管理</h3>
        <div class="filter-controls">
          <select v-model="selectedFilter">
            <option value="all">全部</option>
            <option value="character">角色卡</option>
            <option value="spell">法术卡</option>
            <option value="equipment">装备卡</option>
          </select>
        </div>
      </div>
      
      <div class="cards-grid">
        <CharacterCard
          v-for="card in filteredCards"
          :key="card.id"
          :character="card"
          :deployed="isCardDeployed(card.id)"
          @deploy="deployCharacter"
          @undeploy="undeployCharacter"
          draggable="true"
          @dragstart="handleDragStart($event, card)"
        />
      </div>
    </section>

    <!-- 营地设施 -->
    <section class="camp-facilities">
      <h3>营地设施</h3>
      <div class="facilities-grid">
        <div 
          v-for="facility in facilities" 
          :key="facility.id"
          class="facility-card"
          @click="useFacility(facility)"
        >
          <div class="facility-icon">
            <i :class="facility.icon"></i>
          </div>
          <h4>{{ facility.name }}</h4>
          <p>{{ facility.description }}</p>
          <div class="facility-cost">
            <i class="fas fa-coins"></i>
            {{ facility.cost }}
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { campApi, stressApi } from '@/lib/api'
import { useAuthStore } from '@/stores/auth'
import { useWalletStore } from '@/stores/wallet'
import { useCampStore } from '@/stores/camp'
import type { UserCardCharacter, CurrencyType } from '@/types'
import CharacterCard from '@/components/CharacterCard.vue'
import StressIndicator from '@/components/StressIndicator.vue'
import ClassSelectionModal from '@/components/ClassSelectionModal.vue'

const router = useRouter()
const auth = useAuthStore()
const wallet = useWalletStore()
const campStore = useCampStore()
const { playerCharacter, userCardCharacters } = storeToRefs(campStore)

// 响应式数据
const selectedFilter = ref('all')
const loading = ref(false)
const showClassModal = ref(false)

// 营地设施
const facilities = ref([
  {
    id: 'tavern',
    name: '酒馆',
    description: '缓解压力，恢复心情',
    icon: 'fas fa-beer',
    cost: 50,
    type: 'tavern'
  },
  {
    id: 'chapel',
    name: '教堂',
    description: '祈祷净化，降低压力',
    icon: 'fas fa-church',
    cost: 30,
    type: 'chapel'
  },
  {
    id: 'forge',
    name: '锻造铺',
    description: '升级装备，强化角色',
    icon: 'fas fa-hammer',
    cost: 100,
    type: 'forge'
  },
  {
    id: 'training',
    name: '训练场',
    description: '提升等级，增强属性',
    icon: 'fas fa-dumbbell',
    cost: 80,
    type: 'training'
  }
])

// 计算属性
const healthPercentage = computed(() => {
  if (!playerCharacter.value) return 0
  return (playerCharacter.value.currentHp / playerCharacter.value.maxHp) * 100
})

const actionPointsPercentage = computed(() => {
  if (!playerCharacter.value) return 0
  return (playerCharacter.value.currentActionPoints / playerCharacter.value.maxActionPoints) * 100
})

const availableCharacters = computed(() => userCardCharacters.value)
const deployedCharacters = computed(() =>
  availableCharacters.value.filter((char: UserCardCharacter) => Boolean(char.isDeployed))
)
const deploymentSlots = computed(() =>
  Array(6)
    .fill(null)
    .map((_, i) => ({
      id: `slot-${i}`,
      character: deployedCharacters.value[i] || null
    }))
)

const deployedCount = computed(() => deployedCharacters.value.length)
const maxDeployed = computed(() => 6)
const remainingActionPoints = computed(() => {
  if (!playerCharacter.value) return 0
  const usedAP = deployedCharacters.value.reduce((sum: number, char: UserCardCharacter) => {
    // 这里需要从字符模板获取AP消耗，暂时使用默认值1
    return sum + 1
  }, 0)
  return playerCharacter.value.currentActionPoints - usedAP
})

const filteredCards = computed(() => {
  // 由于 UserCardCharacter 没有 type 属性，暂时返回所有卡片
  // 如果需要过滤，需要从关联的 CardCharacter 获取类型信息
  return availableCharacters.value
})

// 方法
async function loadCampData() {
  if (!auth.isAuthenticated) {
    return
  }
  
  loading.value = true
  try {
    await campStore.fetchCampData()
    
    // 加载钱包数据
    await wallet.loadWallets().catch(() => {
      console.log('Failed to load wallet, using cached data')
    })
  } catch (error: any) {
    console.error('Failed to load camp data:', error)
  } finally {
    loading.value = false
  }
}

function isCardDeployed(cardId: string): boolean {
  return deployedCharacters.value.some(char => char.id === cardId)
}

async function deployCharacter(character: UserCardCharacter) {
  // 暂时使用默认值1作为AP消耗
  const apCost = 1
  if (remainingActionPoints.value < apCost) {
    showToast('行动点不足！', 'error')
    return
  }
  
  try {
    const response = await campApi.deployCardCharacter(character.id, true)
    if (response.data.code === 200) {
      showToast('角色部署成功！', 'success')
      await loadCampData() // 重新加载数据
    }
  } catch (error: any) {
    console.error('Deploy character error:', error)
    showToast('部署失败', 'error')
  }
}

async function undeployCharacter(character: UserCardCharacter) {
  try {
    const response = await campApi.deployCardCharacter(character.id, false)
    if (response.data.code === 200) {
      showToast('角色已撤下', 'success')
      await loadCampData()
    }
  } catch (error: any) {
    console.error('Undeploy character error:', error)
    showToast('撤下失败', 'error')
  }
}

// 拖拽功能
function handleDragStart(event: DragEvent, character: UserCardCharacter) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/json', JSON.stringify(character))
    event.dataTransfer.effectAllowed = 'move'
  }
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
  event.dataTransfer!.dropEffect = 'move'
}

function handleDragLeave(event: DragEvent) {
  // 可以添加视觉反馈
}

function handleDrop(event: DragEvent, slotId: string) {
  event.preventDefault()
  const characterData = event.dataTransfer?.getData('application/json')
  if (characterData) {
    const character = JSON.parse(characterData) as UserCardCharacter
    deployCharacterToSlot(character, slotId)
  }
}

function deployCharacterToSlot(character: UserCardCharacter, slotId: string) {
  const slot = deploymentSlots.value.find(s => s.id === slotId)
  if (slot && !slot.character) {
    deployCharacter(character)
  }
}

function openSkillTree() {
  // 导航到技能树页面
  router.push('/skills')
}

async function handleCharacterCreated() {
  showClassModal.value = false
  // 重新加载营地数据
  await loadCampData()
  showToast('角色创建成功！', 'success')
}

async function useFacility(facility: any) {
  const goldBalance = wallet.getBalance('gold' as CurrencyType)
  if (goldBalance < BigInt(facility.cost)) {
    showToast('金币不足！', 'error')
    return
  }
  
  try {
    if (facility.type === 'tavern' || facility.type === 'chapel') {
      const response = await stressApi.relieveStress(facility.type)
      if (response.data.code === 200) {
        showToast(`${facility.name}使用成功！压力已缓解`, 'success')
        await loadCampData()
      }
    } else {
      showToast(`${facility.name}功能开发中...`, 'info')
    }
  } catch (error: any) {
    console.error('Use facility error:', error)
    // 如果 API 失败，使用本地模拟
    if (facility.type === 'tavern' || facility.type === 'chapel') {
      if (playerCharacter.value) {
        playerCharacter.value.currentStress = Math.max(0, playerCharacter.value.currentStress - 10)
        playerCharacter.value.stressLevel = Math.max(1, Math.ceil(playerCharacter.value.currentStress / 25))
        showToast(`${facility.name}使用成功（本地模式）！压力已缓解`, 'success')
      }
    } else {
      showToast('使用设施失败', 'error')
    }
  }
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-avatar.png'
}

// 获取玩家角色名称
function getPlayerCharacterName(): string {
  return playerCharacter.value?.playerCharacterName || '未知英雄'
}

// 获取玩家角色等级
function getPlayerCharacterLevel(): number {
  return playerCharacter.value?.level ?? 1
}

function showToast(message: string, type: 'success' | 'error' | 'info' = 'info') {
  // 实现toast提示
  console.log(message)
}

// 生命周期
onMounted(() => {
  loadCampData()
})
</script>

<style scoped>
.camp-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #ffffff;
}

.hero-panel {
  margin-bottom: 30px;
}

.create-character-prompt {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  margin-bottom: 30px;
}

.prompt-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.prompt-icon {
  font-size: 4rem;
  color: var(--text-accent);
  margin-bottom: 10px;
}

.create-character-prompt h2 {
  font-size: 1.8rem;
  color: var(--text-primary);
  margin: 0;
}

.create-character-prompt p {
  font-size: 1rem;
  color: var(--text-secondary);
  margin: 0;
}

.create-character-btn {
  margin-top: 10px;
  padding: 14px 32px;
  background: linear-gradient(135deg, var(--text-accent), #ff8c42);
  color: #1a1a2e;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 10px;
}

.create-character-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(229, 62, 62, 0.4);
}

.create-character-btn i {
  font-size: 1.2rem;
}

.hero-card {
  background: var(--secondary-bg);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-medium);
  display: flex;
  gap: 20px;
  border: 1px solid var(--border-color);
}

.hero-avatar {
  position: relative;
  width: 80px;
  height: 80px;
}

.hero-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid var(--text-accent);
  object-fit: cover;
}

.level-badge {
  position: absolute;
  bottom: -5px;
  right: -5px;
  background: var(--text-accent);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.hero-info {
  flex: 1;
}

.hero-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 15px;
  color: var(--text-primary);
}

.hero-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-secondary);
}

.health-bar-container,
.action-points {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.health-bar,
.ap-bar {
  flex: 1;
  height: 20px;
  background: var(--tertiary-bg);
  border-radius: 10px;
  overflow: hidden;
}

.health-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--hp-low), var(--hp-full));
  transition: width 0.3s ease;
}

.ap-fill {
  height: 100%;
  background: linear-gradient(90deg, #4a90e2, #2c5aa0);
  transition: width 0.3s ease;
}

.health-text,
.ap-text {
  font-size: 12px;
  font-weight: bold;
  min-width: 60px;
  text-align: right;
}

.skills-section {
  margin-left: auto;
}

.skills-section h3 {
  font-size: 14px;
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.skill-tree-btn {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.skill-tree-btn:hover {
  background: var(--text-accent);
  border-color: var(--text-accent);
}

.deployment-section,
.deck-management,
.camp-facilities {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
}

.deployment-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--text-secondary);
}

.deployment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.deployment-slot {
  aspect-ratio: 1;
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  background: var(--secondary-bg);
}

.deployment-slot.occupied {
  border-style: solid;
  border-color: var(--success);
}

.deployment-slot.empty:hover {
  border-color: var(--text-accent);
  background: var(--tertiary-bg);
}

.empty-slot {
  text-align: center;
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.filter-controls select {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 8px 12px;
  border-radius: 4px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.facilities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.facility-card {
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.facility-card:hover {
  border-color: var(--text-accent);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.facility-icon {
  font-size: 32px;
  color: var(--text-accent);
  margin-bottom: 10px;
}

.facility-card h4 {
  font-size: 16px;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.facility-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.facility-cost {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-weight: bold;
  color: var(--warning);
}

@media (max-width: 768px) {
  .camp-container {
    padding: 12px;
  }
  
  .hero-card {
    flex-direction: column;
    text-align: center;
  }
  
  .deployment-grid {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
  
  .cards-grid,
  .facilities-grid {
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  }
}
</style>