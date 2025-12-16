<template>
  <div class="skills-simple">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🌟 技能树</h1>
      <p>解锁和升级你的角色技能</p>
    </div>

    <!-- 技能点显示 -->
    <div class="skill-points">
      <div class="points-info">
        <div class="points-display">
          <span class="points-label">可用技能点:</span>
          <span class="points-value">{{ availableSkillPoints }}</span>
        </div>
        <div class="level-info">
          <span class="level-label">角色等级:</span>
          <span class="level-value">1</span>
        </div>
      </div>
    </div>

    <!-- 技能树分类 -->
    <div class="skill-categories">
      <div class="category-tabs">
        <button 
          v-for="category in skillCategories" 
          :key="category.id"
          :class="['tab-btn', { active: activeCategory === category.id }]"
          @click="activeCategory = category.id"
        >
          <i :class="category.icon"></i>
          {{ category.name }}
        </button>
      </div>
    </div>

    <!-- 技能树网格 -->
    <div class="skill-tree-container">
      <div class="skill-tree">
        <div 
          v-for="skill in filteredSkills" 
          :key="skill.id"
          :class="['skill-node', 
            { 
              'unlocked': skill.unlocked, 
              'available': canUnlock(skill),
              'locked': !canUnlock(skill) && !skill.unlocked
            }]"
          @click="toggleSkill(skill)"
          :title="skill.description"
        >
          <div class="skill-icon">
            <i :class="skill.icon"></i>
          </div>
          <div class="skill-level" v-if="skill.level">{{ skill.level }}</div>
          <div class="skill-name">{{ skill.name }}</div>
          <div class="skill-cost">{{ skill.cost }} 点</div>
          
          <!-- 技能连接线 (简化版) -->
          <div 
            v-if="skill.requires" 
            class="connection-line"
            :class="{ active: isSkillUnlocked(skill.requires) }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 技能详情面板 -->
    <div v-if="selectedSkill" class="skill-details">
      <div class="details-header">
        <h3>{{ selectedSkill.name }}</h3>
        <button @click="selectedSkill = null" class="close-btn">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="details-content">
        <div class="skill-description">
          {{ selectedSkill.description }}
        </div>
        <div class="skill-effects">
          <h4>技能效果:</h4>
          <ul>
            <li v-for="effect in selectedSkill.effects" :key="effect">
              {{ effect }}
            </li>
          </ul>
        </div>
        <div class="skill-requirements" v-if="selectedSkill.requires">
          <h4>前置技能:</h4>
          <span>{{ getSkillName(selectedSkill.requires) }}</span>
        </div>
      </div>
    </div>

    <!-- 底部操作区 -->
    <div class="skill-actions">
      <button @click="resetSkills" class="reset-btn">
        <i class="fas fa-undo"></i>
        重置技能点
      </button>
      <div class="skill-summary">
        <span>已解锁技能: {{ unlockedSkillsCount }} / {{ totalSkillsCount }}</span>
      </div>
    </div>

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
import { ref, computed } from 'vue'

// 技能分类
const skillCategories = ref([
  { id: 'combat', name: '战斗', icon: 'fas fa-sword' },
  { id: 'defense', name: '防御', icon: 'fas fa-shield' },
  { id: 'magic', name: '魔法', icon: 'fas fa-magic' },
  { id: 'support', name: '辅助', icon: 'fas fa-hands-helping' }
])

const activeCategory = ref('combat')
const selectedSkill = ref<any>(null)
const availableSkillPoints = ref(3)
const notification = ref<any>(null)

// 技能数据
const skills = ref([
  // 战斗技能
  { id: '1', name: '强力攻击', category: 'combat', cost: 1, unlocked: false, level: null, icon: 'fas fa-sword', description: '增加攻击力', effects: ['攻击力 +10%'], requires: null },
  { id: '2', name: '连击', category: 'combat', cost: 2, unlocked: false, level: null, icon: 'fas fa-bolt', description: '连续攻击敌人', effects: ['50% 概率攻击两次'], requires: '1' },
  { id: '3', name: '暴击强化', category: 'combat', cost: 2, unlocked: false, level: null, icon: 'fas fa-crosshairs', description: '提升暴击率和暴击伤害', effects: ['暴击率 +15%', '暴击伤害 +50%'], requires: '2' },
  
  // 防御技能
  { id: '4', name: '铁壁', category: 'defense', cost: 1, unlocked: false, level: null, icon: 'fas fa-shield-alt', description: '增加防御力', effects: ['防御力 +15%'], requires: null },
  { id: '5', name: '反击', category: 'defense', cost: 2, unlocked: false, level: null, icon: 'fas fa-fist-raised', description: '受到攻击时反击', effects: ['反弹 30% 伤害'], requires: '4' },
  
  // 魔法技能
  { id: '6', name: '火球术', category: 'magic', cost: 1, unlocked: false, level: null, icon: 'fas fa-fire', description: '发射火球攻击敌人', effects: ['魔法伤害 +20'], requires: null },
  { id: '7', name: '冰霜护盾', category: 'magic', cost: 2, unlocked: false, level: null, icon: 'fas fa-snowflake', description: '创建冰霜护盾', effects: ['每回合反弹 10 伤害'], requires: '6' },
  
  // 辅助技能
  { id: '8', name: '治疗术', category: 'support', cost: 1, unlocked: false, level: null, icon: 'fas fa-heart', description: '恢复生命值', effects: ['恢复 30 生命值'], requires: null },
  { id: '9', name: '群体治疗', category: 'support', cost: 3, unlocked: false, level: null, icon: 'fas fa-hands', description: '治疗所有队友', effects: ['所有角色恢复 20 生命值'], requires: '8' }
])

// 计算属性
const filteredSkills = computed(() => {
  return skills.value.filter(skill => skill.category === activeCategory.value)
})

const unlockedSkillsCount = computed(() => {
  return skills.value.filter(skill => skill.unlocked).length
})

const totalSkillsCount = computed(() => {
  return skills.value.length
})

// 方法
function canUnlock(skill: any) {
  if (skill.unlocked) return false
  if (skill.cost > availableSkillPoints.value) return false
  if (skill.requires && !isSkillUnlocked(skill.requires)) return false
  return true
}

function isSkillUnlocked(skillId: string) {
  const skill = skills.value.find(s => s.id === skillId)
  return skill ? skill.unlocked : false
}

function getSkillName(skillId: string) {
  const skill = skills.value.find(s => s.id === skillId)
  return skill ? skill.name : '未知技能'
}

function toggleSkill(skill: any) {
  if (skill.unlocked) {
    // 已解锁，显示详情
    selectedSkill.value = skill
  } else if (canUnlock(skill)) {
    // 解锁技能
    skill.unlocked = true
    availableSkillPoints.value -= skill.cost
    showNotification('success', `成功解锁技能: ${skill.name}`, 'fas fa-check-circle')
  } else {
    // 无法解锁，显示原因
    if (skill.cost > availableSkillPoints.value) {
      showNotification('error', '技能点不足', 'fas fa-exclamation-circle')
    } else if (skill.requires && !isSkillUnlocked(skill.requires)) {
      showNotification('error', '需要先解锁前置技能', 'fas fa-exclamation-circle')
    }
  }
}

function resetSkills() {
  const refundPoints = skills.value
    .filter(skill => skill.unlocked)
    .reduce((total, skill) => total + skill.cost, 0)
  
  skills.value.forEach(skill => {
    skill.unlocked = false
  })
  
  availableSkillPoints.value += refundPoints
  showNotification('info', `技能点已重置，返还 ${refundPoints} 点`, 'fas fa-info-circle')
}

function showNotification(type: string, message: string, icon: string) {
  notification.value = { type, message, icon }
  setTimeout(() => {
    notification.value = null
  }, 3000)
}
</script>

<style scoped>
.skills-simple {
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
  background: linear-gradient(135deg, #4caf50, #8bc34a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

.skill-points {
  background: linear-gradient(135deg, var(--secondary-bg), rgba(76, 175, 80, 0.1));
  border: 1px solid #4caf50;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.points-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.points-display, .level-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.points-label, .level-label {
  color: var(--text-secondary);
  font-weight: bold;
}

.points-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #4caf50;
}

.level-value {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
}

.skill-categories {
  margin-bottom: 2rem;
}

.category-tabs {
  display: flex;
  gap: 0.5rem;
  background: var(--secondary-bg);
  padding: 0.5rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.tab-btn {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
}

.tab-btn:hover {
  background: var(--tertiary-bg);
  color: var(--text-primary);
}

.tab-btn.active {
  background: #4caf50;
  color: white;
}

.skill-tree-container {
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  min-height: 400px;
}

.skill-tree {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1.5rem;
  position: relative;
}

.skill-node {
  background: var(--tertiary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  padding: 1rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100px;
}

.skill-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.skill-node.unlocked {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.2), var(--tertiary-bg));
  border-color: #4caf50;
}

.skill-node.available {
  border-color: #8bc34a;
  background: linear-gradient(135deg, rgba(139, 195, 74, 0.1), var(--tertiary-bg));
  animation: pulse-green 2s infinite;
}

.skill-node.locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.skill-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.skill-level {
  position: absolute;
  top: 5px;
  right: 5px;
  background: #4caf50;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: bold;
}

.skill-name {
  font-weight: bold;
  font-size: 0.9rem;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.skill-cost {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.skill-details {
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.details-header h3 {
  font-size: 1.3rem;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0.5rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: var(--tertiary-bg);
  color: var(--text-primary);
}

.details-content h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.skill-description {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.skill-effects ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.skill-effects li {
  padding: 0.25rem 0;
  color: var(--text-secondary);
}

.skill-effects li::before {
  content: "✓ ";
  color: #4caf50;
  font-weight: bold;
}

.skill-requirements {
  margin-top: 1rem;
}

.skill-requirements span {
  color: #ff9800;
  font-weight: bold;
}

.skill-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1rem 1.5rem;
}

.reset-btn {
  background: #f44336;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.reset-btn:hover {
  background: #d32f2f;
}

.skill-summary {
  color: var(--text-secondary);
  font-weight: bold;
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

@keyframes pulse-green {
  0% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(76, 175, 80, 0); }
  100% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0); }
}

@media (max-width: 768px) {
  .skills-simple {
    padding: 1rem;
  }
  
  .points-info {
    flex-direction: column;
    text-align: center;
  }
  
  .category-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .tab-btn {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
  
  .skill-tree {
    grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
    gap: 1rem;
  }
  
  .skill-node {
    min-height: 80px;
    padding: 0.75rem;
  }
  
  .skill-icon {
    font-size: 1.5rem;
  }
  
  .skill-name {
    font-size: 0.8rem;
  }
}
</style>