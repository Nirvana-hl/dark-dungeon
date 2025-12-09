<template>
  <div class="camp-official">
    <!-- 返回首页按钮 -->
    <RouterLink to="/" class="back-to-home">
      <i class="fas fa-home"></i>
      返回首页
    </RouterLink>
    
    <!-- 顶部导航栏 -->
    <header class="main-header">
      <div class="header-content">
        <div class="logo-section">
          <h1 class="camp-title">⚔️ 黑暗地城营地</h1>
          <p class="camp-subtitle">战略指挥中心</p>
        </div>
        
        <!-- 货币和状态栏 -->
        <div class="status-bar">
          <div class="currencies-compact">
            <div class="currency-compact" v-for="wallet in userWallets" :key="wallet.currencyType">
              <i :class="getCurrencyIcon(wallet.currencyType)"></i>
              <span class="amount">{{ formatNumber(Number(wallet.balance)) }}</span>
            </div>
          </div>
          
          <div class="quick-stats">
            <div class="stat-compact" :class="stressLevelClass">
              <i class="fas fa-brain"></i>
              <span>{{ playerCharacter?.currentStress || 0 }}%</span>
            </div>
          </div>
          
          <div class="notifications-bell" :class="{ active: hasUnclaimedRewards }">
            <i class="fas fa-bell"></i>
            <span class="notification-dot"></span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 左侧角色信息面板 -->
      <aside class="character-sidebar">
        <!-- 调试信息（开发时可见） -->
        <div v-if="false" style="background: rgba(255,0,0,0.1); padding: 10px; margin-bottom: 10px; font-size: 12px;">
          <div>playerCharacter: {{ playerCharacter ? '存在' : 'null' }}</div>
          <div v-if="playerCharacter">ID: {{ playerCharacter.id }}</div>
          <div v-if="playerCharacter">Name: {{ playerCharacter.playerCharacterName }}</div>
          <div>campStore.playerCharacter: {{ campStore.playerCharacter ? '存在' : 'null' }}</div>
        </div>
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
        
        <!-- 如果有角色，显示角色面板 -->
        <CharacterPanel 
          v-else
          :player-character="playerCharacter"
          :unlocked-skills="unlockedSkills"
          :equipped-cards="equippedCards"
          :total-skills="totalSkills"
          @manage-cards="activeTab = 'cards'"
          @show-stress-details="openStressDetails"
        />
      </aside>

      <!-- 右侧功能区 -->
      <main class="function-area">
      <!-- 导航选项卡 -->
      <nav class="modern-tabs">
        <button 
          v-for="tab in functionTabs" 
          :key="tab.id"
          :class="['modern-tab-btn', { active: activeTab === tab.id }]"
          @click="switchTab(tab.id)"
        >
          <div class="tab-icon">
            <i :class="tab.icon"></i>
            <div class="tab-indicator"></div>
          </div>
          <span class="tab-label">{{ tab.name }}</span>
          <div v-if="tab.count" class="tab-badge">{{ tab.count }}</div>
        </button>
      </nav>

      <!-- 功能内容区 -->
      <div class="content-area">
        <!-- 卡组管理 -->
        <div v-if="activeTab === 'cards'" class="module-card">
          <div class="filter-group">
            <button 
              v-for="filter in cardTypes" 
              :key="filter.value"
              :class="['filter-btn', { active: cardFilter.type === filter.value }]"
              @click="cardFilter.type = filter.value"
            >
              <i :class="filter.icon"></i>
              {{ filter.label }}
            </button>
          </div>
          
          <!-- 稀有度筛选 -->
          <div class="rarity-filters">
            <button 
              v-for="rarity in rarities" 
              :key="rarity.value"
              :class="['rarity-filter', { active: cardFilter.rarity === rarity.value }]"
              @click="cardFilter.rarity = rarity.value"
            >
              <div class="rarity-dot" :class="rarity.value"></div>
              {{ rarity.label }}
            </button>
          </div>
          
          <!-- 角色卡牌列表：仅在“全部”或“角色”筛选下展示 -->
            <div 
            v-if="cardFilter.type === 'all' || cardFilter.type === 'character'"
            class="card-characters-section"
          >
            <div class="section-header compact">
              <div class="header-left">
                <h4>
                  <i class="fas fa-users"></i>
                  角色卡牌
                </h4>
                <p class="section-description">
                  管理你拥有的角色卡，购买后会自动同步到此列表
                </p>
              </div>
              <div class="count-badge">
                共 {{ cardCharacters.length }} 张
              </div>
            </div>

            <div v-if="cardCharacters.length === 0" class="card-characters-empty">
              <i class="fas fa-user-slash"></i>
              <p>尚未拥有角色卡牌，前往商城购买即可解锁。</p>
            </div>

            <div v-else class="card-characters-grid">
              <div 
                v-for="character in cardCharacters" 
                :key="character.id" 
                class="character-card-mini"
                :class="getCharacterRarityClass(character)"
                @click="toggleCharacterDeploy(character)"
              >
                <div class="character-card-header">
                  <span class="rarity-tag" :class="getCharacterRarityClass(character)">
                    {{ getCharacterRarityLabel(character.characterRarity) }}
                  </span>
                  <span class="quantity-tag">×{{ character.quantity ?? 0 }}</span>
                  <button
                    v-if="(character.quantity ?? 0) >= 3"
                    class="star-up-btn"
                    :disabled="isUpgradingStar[character.id] || (character.currentStarLevel || 1) >= 5"
                    @click.stop="upgradeCharacterStar(character)"
                    :title="(character.currentStarLevel || 1) >= 5 ? '已达到最大星级' : '消耗3张卡牌升星'"
                  >
                    <i class="fas fa-star"></i>
                    升星
                  </button>
                </div>
                <div class="character-card-body">
                  <div class="character-name">
                    {{ character.characterName || '未知角色' }}
                  </div>
                  <div class="character-meta">
                    <span>
                      <i class="fas fa-hat-wizard"></i>
                      {{ formatCharacterClass(character.characterClassType) }}
                    </span>
                    <span>
                      <i class="fas fa-star"></i>
                      {{ character.currentStarLevel || 1 }}★
                    </span>
                  </div>
                  <div class="character-stats">
                    <span>
                      <i class="fas fa-heart"></i>
                      {{ character.baseHp ?? character.currentHp ?? '-' }}
                    </span>
                    <span>
                      <i class="fas fa-sword"></i>
                      {{ character.baseAttack ?? '-' }}
                    </span>
                    <span>
                      <i class="fas fa-shield-alt"></i>
                      {{ character.baseArmor ?? character.currentArmor ?? 0 }}
                    </span>
                  </div>
                  <p class="character-description">
                    {{ character.characterDescription || '暂无描述' }}
                  </p>
                </div>
                <div 
                  class="character-status"
                  :class="character.isDeployed ? 'deployed' : 'standby'"
                >
                  <i :class="character.isDeployed ? 'fas fa-flag' : 'fas fa-bed'"></i>
                  {{ character.isDeployed ? '已上阵' : '待命' }}
                </div>
              </div>
            </div>
          </div>

          <!-- 法术卡牌列表：仅在“全部”或“法术”筛选下展示 -->
          <div 
            v-if="cardFilter.type === 'all' || cardFilter.type === 'spell'"
            class="card-characters-section"
          >
            <div class="section-header compact">
              <div class="header-left">
                <h4>
                  <i class="fas fa-magic"></i>
                  法术卡牌
                </h4>
                <p class="section-description">
                  查看你拥有的所有法术卡，主要用于造成伤害或提供功能性效果
                </p>
              </div>
              <div class="count-badge">
                共 {{ spellCards.length }} 张
              </div>
            </div>

            <div v-if="spellCards.length === 0" class="card-characters-empty">
              <i class="fas fa-scroll"></i>
              <p>当前还没有法术卡牌，可以前往法术装备商店购买。</p>
            </div>

            <div v-else class="card-characters-grid">
              <div 
                v-for="card in spellCards" 
                :key="card.id" 
                class="character-card-mini"
                :class="[normalizeRarity(card.rarity || 'common'), isCardEquipped(card.id) ? 'deployed' : 'standby']"
                @click="toggleEquipCard(card)"
              >
                <div class="character-card-header">
                  <span class="rarity-tag" :class="normalizeRarity(card.rarity || 'common')">
                    {{ getRarityName(normalizeRarity(card.rarity || 'common')) }}
                  </span>
                  <span class="quantity-tag">×{{ card.quantity || 1 }}</span>
                </div>
                <div class="character-card-body">
                  <div class="character-name">
                    {{ card.name || card.cardName || '未知法术' }}
                  </div>
                  <div class="character-meta">
                    <span>
                      <i class="fas fa-burn"></i>
                      消耗 {{ card.manaCost || card.actionPointCost || 2 }} 点行动值
                    </span>
                  </div>
                  <p class="character-description">
                    {{ card.cardDescription || card.description || '暂无描述' }}
                  </p>
                </div>
                <div 
                  class="character-status"
                  :class="isCardEquipped(card.id) ? 'deployed' : 'standby'"
                >
                  <i :class="isCardEquipped(card.id) ? 'fas fa-flag' : 'fas fa-bed'"></i>
                  {{ isCardEquipped(card.id) ? '已上阵' : '待命' }}
                </div>
              </div>
            </div>
          </div>

          <!-- 装备卡牌列表：仅在“全部”或“装备”筛选下展示 -->
          <div 
            v-if="cardFilter.type === 'all' || cardFilter.type === 'equipment'"
            class="card-characters-section"
          >
            <div class="section-header compact">
              <div class="header-left">
                <h4>
                  <i class="fas fa-shield-alt"></i>
                  装备卡牌
                </h4>
                <p class="section-description">
                  查看你拥有的装备卡，为队伍提供持续增益与防御能力
                </p>
              </div>
              <div class="count-badge">
                共 {{ equipmentCards.length }} 张
              </div>
            </div>

            <div v-if="equipmentCards.length === 0" class="card-characters-empty">
              <i class="fas fa-box-open"></i>
              <p>当前还没有装备卡牌，可以前往法术装备商店购买。</p>
            </div>

            <div v-else class="card-characters-grid">
              <div 
                v-for="card in equipmentCards" 
                :key="card.id" 
                class="character-card-mini"
                :class="[normalizeRarity(card.rarity || 'common'), isCardEquipped(card.id) ? 'deployed' : 'standby']"
                @click="toggleEquipCard(card)"
              >
                <div class="character-card-header">
                  <span class="rarity-tag" :class="normalizeRarity(card.rarity || 'common')">
                    {{ getRarityName(normalizeRarity(card.rarity || 'common')) }}
                  </span>
                  <span class="quantity-tag">×{{ card.quantity || 1 }}</span>
                </div>
                <div class="character-card-body">
                  <div class="character-name">
                    {{ card.name || card.cardName || '未知装备' }}
                  </div>
                  <div class="character-meta">
                    <span>
                      <i class="fas fa-tools"></i>
                      类型：{{ card.slotType || '通用装备' }}
                    </span>
                  </div>
                  <p class="character-description">
                    {{ card.cardDescription || card.description || '暂无描述' }}
                  </p>
                </div>
                <div 
                  class="character-status"
                  :class="isCardEquipped(card.id) ? 'deployed' : 'standby'"
                >
                  <i :class="isCardEquipped(card.id) ? 'fas fa-flag' : 'fas fa-bed'"></i>
                  {{ isCardEquipped(card.id) ? '已上阵' : '待命' }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 背包与道具 -->
        <div v-if="activeTab === 'inventory'" class="module-card">
          <!-- 物品分类 -->
          <div class="inventory-nav">
            <div 
              v-for="category in inventoryCategories" 
              :key="category.type"
              :class="['inventory-nav-item', { active: selectedCategory === category.type }]"
              @click="selectedCategory = category.type"
            >
              <div class="nav-icon">
                <i :class="category.icon"></i>
              </div>
              <div class="nav-content">
                <span class="nav-label">{{ category.name }}</span>
                <span class="nav-count">{{ getCategoryItemCount(category.type) }} 件</span>
              </div>
            </div>
          </div>
          
          <!-- 物品列表 -->
          <div class="items-enhanced-list">
            <div 
              v-for="(item, index) in filteredInventory" 
              :key="item.id"
              class="item-card"
              @click="useItem(item)"
              :style="{ '--delay': index * 0.03 + 's' }"
            >
              <div class="item-visual">
                <div class="item-icon-bg">
                  <i :class="getItemIcon(item.itemType)"></i>
                </div>
                <div class="item-quantity-indicator">{{ item.quantity }}</div>
              </div>
              <div class="item-details">
                <h4 class="item-name">{{ item.name }}</h4>
                <p class="item-desc">{{ item.description }}</p>
                <div class="item-meta">
                  <div class="item-tags-enhanced">
                    <span 
                      v-for="tag in item.tags" 
                      :key="tag"
                      class="tag-enhanced"
                    >
                      {{ tag }}
                    </span>
                  </div>
                  <div class="item-bind-status" :class="item.bindStatus">
                    <i :class="item.bindStatus === 'bound' ? 'fas fa-lock' : 'fas fa-unlock'"></i>
                    {{ item.bindStatus === 'bound' ? '已绑定' : '可交易' }}
                  </div>
                </div>
              </div>
              <div class="item-action">
                <button class="use-btn">
                  <i class="fas fa-hand-pointer"></i>
                  使用
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 商城 -->
        <div v-if="activeTab === 'shop'" class="module-card">
          <ShopPanel 
            :user-balance="userWallets"
            @purchase-item="handlePurchaseItem"
          />
        </div>

        <!-- 任务与事件 -->
        <div v-if="activeTab === 'events'" class="module-card">
          <div class="module-header">
            <div class="header-left">
              <h3>
                <i class="fas fa-scroll"></i>
                任务中心
              </h3>
              <p class="module-description">完成挑战获取丰厚奖励</p>
            </div>
            <!-- 已完成计数已隐藏，不显示在界面中
            <div class="event-stats">
              <span class="completed-count">{{ completedEventsCount }}/{{ availableEvents.length }} 已完成</span>
            </div>
            -->
          </div>
          
          <!-- 任务进度条 -->
          <div class="quest-progress">
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: eventProgressPercentage + '%' }"></div>
            </div>
            <span class="progress-label">完成度: {{ eventProgressPercentage }}%</span>
          </div>
          
          <!-- 任务列表 -->
          <div class="quests-enhanced-list">
            <div 
              v-for="(event, index) in availableEvents" 
              :key="event.id"
              class="quest-card"
              :class="{ 
                completed: event.completed,
                featured: (event as any).featured
              }"
              @click="handleEvent(event)"
              :style="{ '--delay': index * 0.06 + 's' }"
            >
              <div class="quest-visual">
                <div class="quest-icon-bg" :class="(event as any).type || 'default'">
                  <i :class="getEventIcon((event as any).type || 'default')"></i>
                </div>
                <div v-if="(event as any).featured" class="featured-badge">
                  <i class="fas fa-crown"></i>
                </div>
              </div>
              
              <div class="quest-content">
                <div class="quest-header">
                  <h4 class="quest-title">{{ event.name || (event as any).title || '任务' }}</h4>
                  <div class="quest-difficulty" :class="(event as any).difficulty || 'normal'">
                    {{ getDifficultyLabel((event as any).difficulty || 'normal') }}
                  </div>
                </div>
                
                <p class="quest-description">{{ event.description }}</p>
                
                <!-- 任务目标 -->
                <div class="quest-objectives">
                  <h5>任务目标</h5>
                  <div class="objectives-list">
                    <div 
                      v-for="objective in ((event as any).objectives || [])" 
                      :key="objective.id || objective"
                      class="objective-item"
                      :class="{ completed: objective.completed }"
                    >
                      <div class="objective-icon">
                        <i :class="objective.completed ? 'fas fa-check-circle' : 'far fa-circle'"></i>
                      </div>
                      <span class="objective-text">{{ objective.description }}</span>
                      <div class="objective-progress-bar">
                        <div class="progress-fill" :style="{ width: (objective.current / objective.target * 100) + '%' }"></div>
                      </div>
                      <span class="objective-progress-text">{{ objective.current }}/{{ objective.target }}</span>
                    </div>
                  </div>
                </div>
                
                <!-- 任务奖励 -->
                <div class="quest-rewards">
                  <h5>奖励</h5>
                  <div class="reward-items">
                    <template v-if="event.rewards">
                      <div 
                        v-for="(reward, idx) in (Array.isArray(event.rewards) ? event.rewards : [event.rewards])" 
                        :key="idx"
                        class="reward-item-enhanced"
                      >
                        <template v-if="typeof reward === 'object' && reward !== null">
                          <i :class="getRewardIcon((reward as any).type || 'gold')"></i>
                          <span>{{ (reward as any).value || reward }} {{ getRewardName((reward as any).type || 'gold') }}</span>
                        </template>
                        <template v-else>
                          <i :class="getRewardIcon('gold')"></i>
                          <span>{{ reward }}</span>
                        </template>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
              
              <div class="quest-action">
                <button 
                  v-if="!event.completed" 
                  class="start-quest-btn"
                  @click.stop="startEvent(event)"
                >
                  <i class="fas fa-play"></i>
                  开始任务
                </button>
                <div v-else class="quest-completed">
                  <i class="fas fa-check-double"></i>
                  已完成
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- AI助手 -->
        <div v-if="activeTab === 'ai'" class="module-card">
          <div class="module-header">
            <div class="header-left">
              <h3>
                <i class="fas fa-robot"></i>
                AI 战术顾问
              </h3>
              <p class="module-description">智能分析战斗数据，提供优化建议</p>
            </div>
            <button 
              @click="refreshAIAdvice" 
              class="refresh-ai-btn"
              :disabled="isRefreshingAI"
            >
              <i class="fas fa-sync-alt" :class="{ spinning: isRefreshingAI }"></i>
              {{ isRefreshingAI ? '分析中...' : '刷新建议' }}
            </button>
          </div>
      
      <div class="ai-suggestions">
        <div 
          v-for="suggestion in aiSuggestions" 
          :key="suggestion.id"
          class="suggestion-card"
          :class="suggestion.type"
        >
          <div class="suggestion-header">
            <i :class="getSuggestionIcon(suggestion.type)"></i>
            <h4>{{ suggestion.title }}</h4>
            <span class="suggestion-priority" :class="suggestion.priority">
              {{ suggestion.priority }}
            </span>
          </div>
          <div class="suggestion-content">
            <p>{{ suggestion.content }}</p>
            <div class="suggestion-data" v-if="suggestion.data">
              <h5>数据分析:</h5>
              <ul>
                <li v-for="(value, key) in suggestion.data" :key="String(key)">
                  {{ formatDataKey(String(key)) }}: {{ formatDataValue(value) }}
                </li>
              </ul>
            </div>
          </div>
          <div class="suggestion-actions">
            <button 
              v-for="action in suggestion.actions" 
              :key="action"
              class="suggestion-action-btn"
              @click="executeSuggestionAction(suggestion.id, action)"
            >
              {{ action }}
            </button>
          </div>
        </div>
      </div>
      
      <div class="ai-metrics">
        <h4>📊 游戏数据分析</h4>
        <div class="metrics-grid">
          <div 
            v-for="metric in gameMetrics" 
            :key="metric.type"
            class="metric-card"
          >
            <span class="metric-value">{{ metric.value }}</span>
            <span class="metric-label">{{ metric.label }}</span>
            <span class="metric-change" :class="metric.trend">
              {{ metric.change > 0 ? '+' : '' }}{{ metric.change }}%
            </span>
          </div>
        </div>
      </div>
        </div>
      </div>
      </main>
    </div>

    <!-- 通知提示 -->
    <div v-if="notification" class="notification" :class="notification.type">
      <i :class="notification.icon"></i>
      <span>{{ notification.message }}</span>
      <button @click="notification = null" class="close-btn">
        <i class="fas fa-times"></i>
      </button>
    </div>
    
    <!-- 压力详情弹窗 -->
    <div v-if="showStressModal" class="stress-modal-overlay" @click="showStressModal = false">
      <div class="stress-modal-container" @click.stop>
        <header class="stress-modal-header">
          <h2>压力与负面效果</h2>
          <button class="close-btn" @click="showStressModal = false">
            <i class="fas fa-times"></i>
          </button>
        </header>

        <main class="stress-modal-body">
          <div class="stress-summary-grid" v-if="stressStatus">
            <div class="stress-summary-card">
              <span class="label">当前等级</span>
              <span class="value">Lv.{{ stressStatus.stressLevel }}</span>
            </div>
            <div class="stress-summary-card">
              <span class="label">当前压力值</span>
              <span class="value">{{ stressStatus.currentStress }} / 100</span>
            </div>
          </div>

          <div class="stress-levels-grid">
            <div 
              v-for="level in [1,2,3,4]" 
              :key="level" 
              class="stress-level-card"
              :class="`level-${level}`"
            >
              <div class="level-header">
                <span class="level-tag">等级 {{ level }}</span>
                <span class="level-range">
                  <template v-if="level === 1">0 - 25</template>
                  <template v-else-if="level === 2">26 - 50</template>
                  <template v-else-if="level === 3">51 - 75</template>
                  <template v-else>76 - 100</template>
                </span>
              </div>

              <div class="debuff-list" v-if="stressDebuffsByLevel[level] && stressDebuffsByLevel[level].length">
                <div 
                  v-for="debuff in stressDebuffsByLevel[level]" 
                  :key="debuff.id"
                  class="debuff-row"
                  :class="debuff.debuffType"
                >
                  <div class="debuff-main">
                    <div class="debuff-name">{{ debuff.debuffName }}</div>
                    <div class="debuff-desc">{{ debuff.effectDescription }}</div>
                  </div>
                  <div class="debuff-extra">
                    <span class="chance" v-if="debuff.triggerChance">
                      {{ Math.round(debuff.triggerChance * 100) }}%
                    </span>
                  </div>
                </div>
              </div>
              <div class="debuff-empty" v-else>
                暂无配置
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
    
    <!-- 职业选择弹窗 -->
    <ClassSelectionModal 
      :show="showClassModal" 
      @close="showClassModal = false"
      @complete="handleCharacterCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { campApi, stressApi, userCardApi, userCardCharacterApi } from '@/lib/api'
import CharacterPanel from '@/components/CharacterPanel.vue'
import ShopPanel from '@/components/ShopPanel.vue'
import ClassSelectionModal from '@/components/ClassSelectionModal.vue'
import { useCampStore } from '@/stores/camp'
import { useInventoryStore } from '@/stores/inventory'
import { useShopStore } from '@/stores/shop'
import { useWalletStore } from '@/stores/wallet'
import { useAuthStore } from '@/stores/auth'

// 防抖函数
function debounce<T extends (...args: any[]) => void>(func: T, wait: number): T {
  let timeout: NodeJS.Timeout | null = null
  return ((...args: any[]) => {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }) as T
}

// 类型
import type { StressDebuffConfig } from '@/types'

// 使用 Store
const campStore = useCampStore()
const inventoryStore = useInventoryStore()
const shopStore = useShopStore()
const walletStore = useWalletStore()

// 数据定义 - 使用 store 的响应式引用，确保数据同步
const { playerCharacter } = storeToRefs(campStore)
const userWallets = ref<any[]>([])
const isUpgradingStar = ref<Record<string | number, boolean>>({})
const userCards = ref<any[]>([])
const equippedCards = ref<any[]>([])
const inventory = ref<any[]>([])
const availableEvents = ref<any[]>([])
const aiSuggestions = ref<any[]>([])
const gameMetrics = ref<any[]>([])
const activeTab = ref('cards')
const selectedCategory = ref('consumable')
const notification = ref<any>(null)
const isRefreshingAI = ref(false)
const loading = ref(false)
const showClassModal = ref(false)

// 压力系统数据
const showStressModal = ref(false)
const stressStatus = ref<any | null>(null)
const stressDebuffsByLevel = ref<Record<number, StressDebuffConfig[]>>({})

// 卡牌角色数据
const cardCharacters = ref<any[]>([])

// 卡牌筛选
const cardFilter = ref({
  type: 'all',
  rarity: 'all',
  sort: 'name'
})

// 配置数据
const functionTabs = ref([
  { id: 'cards', name: '卡组管理', icon: 'fas fa-layer-group', count: 0 },
  { id: 'inventory', name: '背包道具', icon: 'fas fa-shopping-bag', count: 0 },
  { id: 'shop', name: '商城&货币', icon: 'fas fa-store', count: 0 },
  { id: 'events', name: '任务事件', icon: 'fas fa-scroll', count: 0 },
  { id: 'ai', name: 'AI助手', icon: 'fas fa-robot', count: 0 }
])

const cardTypes = ref([
  { value: 'all', label: '全部', icon: 'fas fa-th' },
  { value: 'character', label: '角色', icon: 'fas fa-user' },
  { value: 'spell', label: '法术', icon: 'fas fa-magic' },
  { value: 'equipment', label: '装备', icon: 'fas fa-shield-alt' }
])

const rarities = ref([
  { value: 'all', label: '全部' },
  { value: 'common', label: '普通' },
  { value: 'rare', label: '稀有' },
  { value: 'epic', label: '史诗' },
  { value: 'legendary', label: '传说' }
])

const inventoryCategories = ref([
  { type: 'consumable', name: '消耗品', icon: 'fas fa-flask' },
  { type: 'material', name: '材料', icon: 'fas fa-gem' },
  { type: 'special', name: '特殊', icon: 'fas fa-star' }
])

// 精品商城已删除，shopCategories 已移除

// 计算属性
const hasUnclaimedRewards = computed(() => {
  return userWallets.value.some(w => w.balance > 0) || availableEvents.value.some(e => e.completed)
})

const hpPercentage = computed(() => {
  if (!playerCharacter.value?.maxHp) return 0
  return (playerCharacter.value.currentHp / playerCharacter.value.maxHp) * 100
})

const apPercentage = computed(() => {
  if (!playerCharacter.value?.maxActionPoints) return 0
  return (playerCharacter.value.currentActionPoints / playerCharacter.value.maxActionPoints) * 100
})

const stressPercentage = computed(() => {
  return Math.min((playerCharacter.value?.currentStress || 0), 100)
})

const stressLevelClass = computed(() => {
  const level = playerCharacter.value?.stressLevel || 1
  return `stress-level-${level}`
})

const stressDebuffs = computed(() => {
  // 模拟压力 debuff 数据
  if (!playerCharacter.value?.stressDebuffs) return []
  return (playerCharacter.value.stressDebuffs as Array<{ id: string; type: string; name: string }>).map((debuff: any, index: number) => ({
    id: debuff.id || `debuff-${index}`,
    type: debuff.type || 'mental',
    name: debuff.name || debuff.debuffName || '未知状态'
  }))
})

const unlockedSkills = computed(() => {
  return []
})

const totalSkills = computed(() => {
  return 15
})

const skillProgressPercentage = computed(() => {
  return (unlockedSkills.value.length / totalSkills.value) * 100
})

const deckLimit = computed(() => 10)

function normalizeRarity(rarity?: string) {
  return (rarity || 'common').toLowerCase()
}

// 按类型拆分卡牌：角色 / 法术 / 装备
const spellCards = computed(() => {
  return userCards.value.filter(card => card && card.cardType === 'spell')
})

const equipmentCards = computed(() => {
  return userCards.value.filter(card => card && card.cardType === 'equipment')
})

const filteredCards = computed(() => {
  let cards = [...userCards.value].filter(card => card && card.id) // 过滤掉无效数据
  
  // 类型筛选
  if (cardFilter.value.type !== 'all') {
    cards = cards.filter(card => card && card.cardType === cardFilter.value.type)
  }
  
  // 稀有度筛选
  if (cardFilter.value.rarity !== 'all') {
    cards = cards.filter(
      card => card && normalizeRarity(card.rarity) === cardFilter.value.rarity
    )
  }
  
  // 排序
  cards.sort((a, b) => {
    if (!a || !b) return 0
    switch (cardFilter.value.sort) {
      case 'name':
        return (a.name || '').localeCompare(b.name || '')
      case 'rarity':
        const rarityOrder = { common: 1, rare: 2, epic: 3, legendary: 4 }
        const aRarity = normalizeRarity((a as any).rarity) as keyof typeof rarityOrder
        const bRarity = normalizeRarity((b as any).rarity) as keyof typeof rarityOrder
        return (rarityOrder[aRarity] || 0) - (rarityOrder[bRarity] || 0)
      case 'cost':
        return (a.manaCost || 1) - (b.manaCost || 1)
      default:
        return 0
    }
  })
  
  return cards
})

const filteredInventory = computed(() => {
  return inventory.value.filter(item => item && item.itemType === selectedCategory.value)
})

// 精品商城已删除，相关计算属性已移除

// 方法
function getCurrencyIcon(type: string) {
  const icons: { [key: string]: string } = {
    gold: 'fas fa-coins gold',
    soulstone: 'fas fa-gem purple',
    crystal: 'fas fa-crystal blue'
  }
  return icons[type] || 'fas fa-coins'
}

function getCurrencyName(type: string) {
  const names: { [key: string]: string } = {
    gold: '金币',
    soulstone: '魂晶',
    crystal: '水晶'
  }
  return names[type] || '货币'
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-avatar.png'
}

function getCardIcon(type: string) {
  const icons: { [key: string]: string } = {
    character: 'fas fa-user',
    spell: 'fas fa-magic',
    equipment: 'fas fa-shield-alt'
  }
  return icons[type] || 'fas fa-question'
}

function getRarityStars(rarity: string) {
  const stars: { [key: string]: number } = {
    common: 1,
    rare: 2,
    epic: 3,
    legendary: 4
  }
  return stars[rarity] || 1
}

function getCharacterRarityClass(character: any) {
  const rarity = (character?.characterRarity || 'common').toLowerCase()
  return `rarity-${rarity}`
}

function getCharacterRarityLabel(rarity?: string) {
  const map: Record<string, string> = {
    common: '普通',
    rare: '稀有',
    epic: '史诗',
    legendary: '传说'
  }
  if (!rarity) return '普通'
  return map[rarity.toLowerCase()] || '普通'
}

function formatCharacterClass(classType?: string) {
  const classMap: Record<string, string> = {
    warrior: '战士',
    mage: '秘术师',
    occultist: '术士',
    ranger: '游侠',
    priest: '圣职者',
    rogue: '刺客'
  }
  if (!classType) return '未知职业'
  return classMap[classType.toLowerCase()] || classType
}

// 升星角色卡牌
async function upgradeCharacterStar(character: any) {
  const characterId = character.id || character.userCardCharacterId
  if (!characterId) {
    showNotification('error', '角色ID无效', 'fas fa-exclamation-triangle')
    return
  }
  
  // 检查数量是否足够（使用实际数量，不使用默认值）
  const actualQuantity = Number(character.quantity ?? 0) || 0
  if (actualQuantity < 3) {
    showNotification('error', `需要至少3张卡牌才能升星，当前只有 ${actualQuantity} 张`, 'fas fa-exclamation-triangle')
    return
  }
  
  // 检查是否已达到最大星级
  if ((character.currentStarLevel || 1) >= 5) {
    showNotification('error', '已达到最大星级', 'fas fa-exclamation-triangle')
    return
  }
  
  // 防止重复点击
  if (isUpgradingStar.value[characterId]) {
    return
  }
  
  try {
    isUpgradingStar.value[characterId] = true
    
    console.log('[CampOfficial] 开始升星:', {
      characterName: character.characterName,
      characterId: characterId,
      currentStarLevel: character.currentStarLevel || 1,
      quantity: character.quantity ?? 0
    })
    
    // 调用后端升星接口
    const response = await userCardCharacterApi.upgradeStarLevel(characterId)
    
    if (response.data.code === 200) {
      showNotification('success', `${character.characterName || '角色'} 升星成功！`, 'fas fa-star')
      
      // 刷新营地数据以获取最新的星级和数量
      await loadCampData()
    } else {
      throw new Error(response.data.message || '升星失败')
    }
  } catch (error) {
    console.error('[CampOfficial] 升星失败:', error)
    const errorMessage = error instanceof Error ? error.message : '升星失败'
    showNotification('error', errorMessage, 'fas fa-exclamation-triangle')
  } finally {
    isUpgradingStar.value[characterId] = false
  }
}

// 切换角色部署状态（待命/已上阵）
async function toggleCharacterDeploy(character: any) {
  try {
    const characterId = character.id || character.userCardCharacterId
    if (!characterId) {
      showNotification('error', '角色ID无效', 'fas fa-exclamation-triangle')
      return
    }
    
    // 切换部署状态
    const newDeployState = !character.isDeployed
    console.log('[CampOfficial] 切换角色部署状态:', {
      characterName: character.characterName,
      characterId: characterId,
      currentState: character.isDeployed,
      newState: newDeployState
    })
    
    // 调用后端API更新状态
    const response = await campApi.deployCardCharacter(String(characterId), newDeployState)
    
    if (response.data.code === 200) {
      // 更新本地状态
      character.isDeployed = newDeployState
      const statusText = newDeployState ? '已上阵' : '待命'
      showNotification('success', `${character.characterName || '角色'} ${statusText}`, 'fas fa-check-circle')
    } else {
      throw new Error(response.data.message || '更新角色状态失败')
    }
  } catch (error) {
    console.error('[CampOfficial] 切换角色部署状态失败:', error)
    const errorMessage = error instanceof Error ? error.message : '操作失败'
    showNotification('error', errorMessage, 'fas fa-exclamation-triangle')
  }
}

function isCardEquipped(cardId: string | number) {
  // 通过 userCardId 或 id 来判断
  return equippedCards.value.some(card => 
    card.id === cardId || 
    card.userCardId === cardId ||
    String(card.id) === String(cardId) ||
    String(card.userCardId) === String(cardId)
  )
}

// 默认卡组ID（用于区分不同的卡组方案）
const DEFAULT_LOADOUT_ID = 1

async function toggleEquipCard(card: any) {
  try {
    // 获取 userCardId（用户卡牌实例ID，不是模板ID）
    // 优先使用 userCardId，如果没有则使用 id（可能是从 userCards 列表来的）
    const userCardId = card.userCardId || card.id
    
    if (!userCardId) {
      console.error('[CampOfficial] 卡牌数据缺少ID:', card)
      showNotification('error', '卡牌ID无效', 'fas fa-exclamation-triangle')
      return
    }
    
    if (isCardEquipped(card.id)) {
      // 撤下装备：清除 loadout_id
      console.log('[CampOfficial] 撤下卡牌:', card.name, 'userCardId:', userCardId)
      const response = await userCardApi.updateUserCard(userCardId, { loadoutId: null })
      
      if (response.data.code === 200) {
        equippedCards.value = equippedCards.value.filter(c => c.id !== card.id && c.userCardId !== userCardId)
        showNotification('info', `已撤下 ${card.name}`, 'fas fa-info-circle')
        updateTabCounts()
      } else {
        throw new Error(response.data.message || '撤下卡牌失败')
      }
    } else {
      // 检查卡组是否已满
      if (equippedCards.value.length >= deckLimit.value) {
        showNotification('error', `卡组已满（最多 ${deckLimit.value} 张）`, 'fas fa-exclamation-circle')
        return
      }
      
      // 装备卡牌：设置 loadout_id
      console.log('[CampOfficial] 装备卡牌:', card.name, 'userCardId:', userCardId, 'loadoutId:', DEFAULT_LOADOUT_ID)
      const response = await userCardApi.updateUserCard(userCardId, { loadoutId: DEFAULT_LOADOUT_ID })
      
      if (response.data.code === 200) {
        // 重新加载卡组数据，确保数据一致性
        await loadEquippedCards()
        showNotification('success', `已装备 ${card.name}`, 'fas fa-check-circle')
        updateTabCounts()
      } else {
        throw new Error(response.data.message || '装备卡牌失败')
      }
    }
  } catch (error) {
    console.error('[CampOfficial] 装备卡牌失败:', error)
    const errorMessage = error instanceof Error ? error.message : '装备操作失败'
    showNotification('error', errorMessage, 'fas fa-exclamation-triangle')
  }
}

function viewCardDetails(card: any) {
  console.log('查看卡牌详情:', card)
}

function getItemIcon(type: string) {
  const icons: { [key: string]: string } = {
    consumable: 'fas fa-flask',
    material: 'fas fa-gem',
    equipment: 'fas fa-shield-alt',
    special: 'fas fa-star'
  }
  return icons[type] || 'fas fa-box'
}

function getCategoryItemCount(type: string) {
  return inventory.value.filter(item => item.itemType === type).length
}

// 精品商城已删除，getShopCategoryCount 函数已移除

async function useItem(item: any) {
  try {
    await inventoryStore.useItem(item.id)
    // 刷新背包数据
    await inventoryStore.fetchInventory()
    inventory.value = inventoryStore.inventory as any[]
    showNotification('success', `使用了 ${item.name}`, 'fas fa-check-circle')
    updateTabCounts()
  } catch (error) {
    console.error('使用物品失败:', error)
    showNotification('error', '使用物品失败', 'fas fa-exclamation-triangle')
  }
}

// 打开压力详情弹窗：从后端获取当前压力状态与全部debuff配置
async function openStressDetails() {
  try {
    console.log('[CampOfficial] 加载压力状态与debuff配置...')
    const [statusResp, debuffResp] = await Promise.all([
      stressApi.getStressStatus(),
      stressApi.getStressDebuffs()
    ])

    if (statusResp.data.code === 200) {
      stressStatus.value = statusResp.data.data || null
    } else {
      throw new Error(statusResp.data.message || '获取压力状态失败')
    }

    if (debuffResp.data.code === 200) {
      const list = debuffResp.data.data || []
      // 按压力等级分组，并为2、3级随机抽取一个
      const grouped: Record<number, StressDebuffConfig[]> = {}
      ;[1, 2, 3, 4].forEach(level => {
        const levelDebuffs = list.filter((d: any) => d.stressLevel === level)
        if (level === 2 || level === 3) {
          if (levelDebuffs.length > 0) {
            const idx = Math.floor(Math.random() * levelDebuffs.length)
            grouped[level] = [mapDebuffDTOToConfig(levelDebuffs[idx], level, idx)]
          } else {
            grouped[level] = []
          }
        } else {
          grouped[level] = levelDebuffs.map((d: any, idx: number) =>
            mapDebuffDTOToConfig(d, level, idx)
          )
        }
      })
      stressDebuffsByLevel.value = grouped
    } else {
      throw new Error(debuffResp.data.message || '获取压力debuff配置失败')
    }

    showStressModal.value = true
  } catch (error) {
    console.error('[CampOfficial] 加载压力信息失败:', error)
    const message = error instanceof Error ? error.message : '获取压力信息失败'
    showNotification('error', message, 'fas fa-exclamation-triangle')
  }
}

function mapDebuffDTOToConfig(dto: any, level: number, index: number): StressDebuffConfig {
  const trigger = dto.triggerChance ?? 0
  return {
    id: dto.id ?? `${level}-${index}`,
    stressLevel: dto.stressLevel ?? level,
    debuffName: dto.debuffName,
    debuffType: dto.debuffType,
    effectDescription: dto.effectDescription,
    triggerChance: Number(trigger),
    effectPayload: dto.effectPayload ? JSON.parse(dto.effectPayload) : {},
    isPersistent: dto.isPersistent ?? false,
    type: dto.debuffType,
    name: dto.debuffName,
    description: dto.effectDescription
  }
}

function getEventIcon(type: string) {
  const icons: { [key: string]: string } = {
    daily: 'fas fa-calendar-day',
    weekly: 'fas fa-calendar-week',
    special: 'fas fa-star'
  }
  return icons[type] || 'fas fa-scroll'
}

async function handleEvent(event: any) {
  try {
    const response = await campApi.completeEvent(event.id)
    if (response.data.code === 200) {
      showNotification('success', response.data.message || '完成任务成功', 'fas fa-check-circle')
      // 刷新事件列表
      await loadEventsData()
      updateTabCounts()
    }
  } catch (error) {
    console.error('完成任务失败:', error)
    showNotification('error', '完成任务失败', 'fas fa-exclamation-triangle')
  }
}

function startEvent(event: any) {
  showNotification('info', `开始任务: ${event.name || event.title}`, 'fas fa-play-circle')
}

function getRewardIcon(type: string) {
  const icons: { [key: string]: string } = {
    gold: 'fas fa-coins gold',
    exp: 'fas fa-star blue',
    item: 'fas fa-gift green'
  }
  return icons[type] || 'fas fa-gift'
}

function getSuggestionIcon(type: string) {
  const icons: { [key: string]: string } = {
    combat: 'fas fa-sword',
    deck: 'fas fa-layer-group',
    resource: 'fas fa-coins',
    strategy: 'fas fa-chess'
  }
  return icons[type] || 'fas fa-lightbulb'
}

function formatDataKey(key: string) {
  const keyMap: { [key: string]: string } = {
    winRate: '胜率',
    avgDamage: '平均伤害',
    resourceEfficiency: '资源效率',
    cardUsage: '卡牌使用率'
  }
  return keyMap[key] || key
}

function formatDataValue(value: any) {
  if (typeof value === 'number') {
    return (value * 100).toFixed(1) + '%'
  }
  return value
}

function executeSuggestionAction(suggestionId: string, action: string) {
  showNotification('success', `执行操作: ${action}`, 'fas fa-check-circle')
}

async function refreshAIAdvice() {
  try {
    isRefreshingAI.value = true
    const response = await campApi.refreshAISuggestions()
    if (response.data.code === 200) {
      aiSuggestions.value = response.data.data || []
      showNotification('success', 'AI建议已更新', 'fas fa-check-circle')
    }
  } catch (error) {
    console.error('刷新AI建议失败:', error)
    showNotification('error', '刷新AI建议失败', 'fas fa-exclamation-triangle')
  } finally {
    isRefreshingAI.value = false
  }
}

function canAfford(offer: any) {
  const wallet = userWallets.value.find(w => w.currencyType === offer.currencyType)
  const price = typeof offer.price === 'bigint' ? Number(offer.price) : offer.price
  const balance = wallet ? (typeof wallet.balance === 'bigint' ? Number(wallet.balance) : wallet.balance) : 0
  return balance >= price
}

function calculateDiscountedPrice(offer: any): number {
  const basePrice = typeof offer.price === 'bigint' ? Number(offer.price) : offer.price
  if (offer.discount && offer.discount > 0) {
    return Math.floor(basePrice * (1 - offer.discount / 100))
  }
  return basePrice
}

async function purchaseItem(offer: any) {
  try {
    console.log('[CampOfficial] ========== 开始购买商品 ==========')
    console.log('[CampOfficial] 商品信息:', {
      offerId: offer.id,
      shopOfferId: offer.shopOfferId,
      offerName: offer.name,
      price: offer.price,
      offerType: offer.offerType,
      targetId: offer.targetId,
      fullOffer: offer
    })
    
    // 检查余额
    const canBuy = canAfford(offer)
    console.log('[CampOfficial] 余额检查:', {
      canAfford: canBuy,
      goldBalance: userWallets.value.find(w => w.currencyType === 'gold')?.balance || 0,
      offerPrice: offer.price
    })
    
    if (!canBuy) {
      console.warn('[CampOfficial] ✗ 余额不足，取消购买')
      showNotification('error', '余额不足', 'fas fa-exclamation-circle')
      return
    }
    
    // 确定要使用的offerId
    const offerIdToUse = offer.id || offer.shopOfferId
    console.log('[CampOfficial] 使用offerId:', offerIdToUse)
    
    if (!offerIdToUse) {
      console.error('[CampOfficial] ✗ offerId为空，无法购买')
      showNotification('error', '商品ID无效', 'fas fa-exclamation-triangle')
      return
    }
    
    // 调用 API 购买
    console.log('[CampOfficial] 调用 shopStore.purchaseItem...')
    const result = await shopStore.purchaseItem(String(offerIdToUse), 1)
    
    console.log('[CampOfficial] 购买结果:', result)
    
    if (result.success) {
      // 刷新钱包、背包、商城与营地核心数据（包含卡牌角色）
      console.log('[CampOfficial] 刷新购买后的数据...')
      console.log('[CampOfficial] 购买的商品类型:', offer.offerType, 'targetId:', offer.targetId)
      
      // 先刷新钱包（最快）
      await walletStore.loadWallets()
      userWallets.value = walletStore.wallets as any[]
      
      // 并行刷新其他数据
      await Promise.all([
        inventoryStore.fetchInventory(),
        shopStore.fetchShopOffers(),
        loadCampData()
      ])
      
      // 更新本地状态，并映射后端字段到前端字段
      const rawInventory = inventoryStore.inventory as any[]
      inventory.value = rawInventory.map((item: any) => ({
        ...item,
        name: item.itemName || item.name, // 后端返回 itemName，前端使用 name
        description: item.itemDescription || item.description,
        itemType: item.itemType || item.type
      }))
      
      // 映射 userCards 字段
      const rawUserCards = userCards.value
      userCards.value = rawUserCards.map((card: any) => ({
        ...card,
        name: card.cardName || card.name, // 后端返回 cardName，前端使用 name
        description: card.cardDescription || card.description,
        cardType: card.cardType || card.type
      }))
      
      // 如果是卡牌类型，额外刷新卡组数据
      if (offer.offerType === 'card' || offer.offerType === 'card_character') {
        await loadEquippedCards()
      }
      
      // 等待一下确保所有数据都已更新
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 验证数据是否已更新
      console.log('[CampOfficial] 购买后数据验证:', {
        walletBalance: userWallets.value.find(w => w.currencyType === 'gold')?.balance || 0,
        inventoryCount: inventory.value.length,
        inventoryItems: inventory.value.map((i: any) => ({ id: i.id, name: i.name, quantity: i.quantity })),
        userCardsCount: userCards.value.length,
        userCards: userCards.value.map((c: any) => ({ id: c.id, name: c.name, cardId: c.cardId })),
        cardCharactersCount: cardCharacters.value.length,
        cardCharacters: cardCharacters.value.map((c: any) => ({ 
          id: c.id, 
          name: c.characterName, 
          cardCharacterId: c.cardCharacterId,
          quantity: c.quantity 
        })),
        equippedCardsCount: equippedCards.value.length
      })
      
      // 根据购买类型显示不同的提示
      if (offer.offerType === 'item') {
        // 道具类型：检查背包中是否有新物品
        const purchasedItem = inventory.value.find((item: any) => 
          item.itemId === offer.targetId || 
          item.name === offer.name ||
          String(item.id) === String(offer.targetId)
        )
        if (purchasedItem) {
          console.log('[CampOfficial] ✅ 购买的道具已添加到背包:', {
            name: purchasedItem.name,
            quantity: purchasedItem.quantity,
            itemId: purchasedItem.itemId,
            id: purchasedItem.id
          })
          showNotification('success', 
            `购买成功！${purchasedItem.name} 已添加到背包（数量: ×${purchasedItem.quantity}）`, 
            'fas fa-shopping-cart'
          )
        } else {
          console.warn('[CampOfficial] ⚠️ 未找到购买的道具:', {
            offerType: offer.offerType,
            targetId: offer.targetId,
            name: offer.name,
            allItems: inventory.value.map((i: any) => ({
              name: i.name,
              itemId: i.itemId,
              id: i.id
            }))
          })
          showNotification('success', result.message || '购买成功，请查看背包', 'fas fa-shopping-cart')
        }
      } else if (offer.offerType === 'card') {
        // 卡牌类型：检查卡组管理中是否有新卡牌
        const purchasedCard = userCards.value.find((card: any) => 
          card.cardId === offer.targetId || 
          card.name === offer.name ||
          String(card.id) === String(offer.targetId)
        )
        if (purchasedCard) {
          console.log('[CampOfficial] ✅ 购买的卡牌已添加到卡组管理:', {
            name: purchasedCard.name,
            quantity: purchasedCard.quantity,
            cardId: purchasedCard.cardId,
            id: purchasedCard.id
          })
          showNotification('success', 
            `购买成功！${purchasedCard.name} 已添加到卡组管理（数量: ×${purchasedCard.quantity}）`, 
            'fas fa-shopping-cart'
          )
        } else {
          console.warn('[CampOfficial] ⚠️ 未找到购买的卡牌:', {
            offerType: offer.offerType,
            targetId: offer.targetId,
            name: offer.name,
            allCards: userCards.value.map((c: any) => ({
              name: c.name,
              cardId: c.cardId,
              id: c.id
            }))
          })
          showNotification('success', result.message || '购买成功，请查看卡组管理', 'fas fa-shopping-cart')
        }
      } else if (offer.offerType === 'card_character') {
        // 角色卡牌类型：检查角色卡牌列表
        const purchasedCharacter = cardCharacters.value.find((c: any) => 
          c.cardCharacterId === offer.targetId || 
          c.characterName === offer.name ||
          String(c.id) === String(offer.targetId)
        )
        if (purchasedCharacter) {
          console.log('[CampOfficial] ✅ 购买的角色卡牌数量已更新:', {
            name: purchasedCharacter.characterName,
            quantity: purchasedCharacter.quantity,
            cardCharacterId: purchasedCharacter.cardCharacterId,
            id: purchasedCharacter.id
          })
          showNotification('success', 
            `购买成功！${purchasedCharacter.characterName} 当前数量: ×${purchasedCharacter.quantity}`, 
            'fas fa-shopping-cart'
          )
        } else {
          console.warn('[CampOfficial] ⚠️ 未找到购买的角色卡牌:', {
            offerType: offer.offerType,
            targetId: offer.targetId,
            name: offer.name,
            allCharacters: cardCharacters.value.map((c: any) => ({
              name: c.characterName,
              cardCharacterId: c.cardCharacterId,
              id: c.id
            }))
          })
          showNotification('success', result.message || '购买成功，请查看卡组管理', 'fas fa-shopping-cart')
        }
      } else {
        showNotification('success', result.message || '购买成功', 'fas fa-shopping-cart')
      }
      
      console.log('[CampOfficial] ✅ 购买成功，数据已刷新')
      console.log('[CampOfficial] 最终数据统计:', {
        userCards: userCards.value.length,
        cardCharacters: cardCharacters.value.length,
        inventory: inventory.value.length,
        equippedCards: equippedCards.value.length
      })
      
      // 更新选项卡计数
      updateTabCounts()
      
      // 如果当前在相关选项卡，强制刷新显示
      if (activeTab.value === 'cards' && (offer.offerType === 'card' || offer.offerType === 'card_character')) {
        // 触发响应式更新
        await new Promise(resolve => setTimeout(resolve, 100))
      }
      if (activeTab.value === 'inventory' && offer.offerType === 'item') {
        // 触发响应式更新
        await new Promise(resolve => setTimeout(resolve, 100))
      }
    } else {
      console.error('[CampOfficial] 购买失败:', result.message)
      showNotification('error', result.message || '购买失败', 'fas fa-exclamation-triangle')
    }
  } catch (error) {
    console.error('[CampOfficial] 购买异常:', error)
    const errorMessage = error instanceof Error ? error.message : '未知错误'
    showNotification('error', '购买失败: ' + errorMessage, 'fas fa-exclamation-triangle')
  }
}

// 优化的通知函数，使用防抖避免重复触发
const showNotification = debounce((type: string, message: string, icon: string) => {
  notification.value = { type, message, icon }
  setTimeout(() => {
    notification.value = null
  }, 3000)
}, 300)

// 新增的辅助方法
function formatNumber(num: number): string {
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

function getCharacterTitle(level: number): string {
  if (level >= 20) return '传说英雄'
  if (level >= 15) return '史诗战士'
  if (level >= 10) return '资深冒险者'
  if (level >= 5) return '见习战士'
  return '新手冒险者'
}

function getRarityName(rarity: string): string {
  const names: { [key: string]: string } = {
    common: '普通',
    rare: '稀有',
    epic: '史诗',
    legendary: '传说'
  }
  return names[rarity] || '未知'
}

function switchTab(tabId: string) {
  activeTab.value = tabId
}

// 处理子组件事件
async function handleEquipCard(card: any) {
  await toggleEquipCard(card)
}

function handleUnequipCard(cardId: string) {
  equippedCards.value = equippedCards.value.filter(c => c.id !== cardId)
  const card = userCards.value.find(c => c.id === cardId)
  if (card) {
    showNotification('info', `已撤下 ${card.name}`, 'fas fa-info-circle')
  }
}

function handleUseItem(item: any) {
  if (item.quantity > 0) {
    item.quantity--
    showNotification('success', `使用了 ${item.name}`, 'fas fa-check-circle')
  }
}

async function handlePurchaseItem(offer: any) {
  console.log('[CampOfficial] handlePurchaseItem 被调用:', offer)
  // 直接调用 purchaseItem 函数，它会处理所有购买逻辑（包括API调用和数据刷新）
  await purchaseItem(offer)
}

function handleInventoryCategoryChange(category: string) {
  console.log('背包分类切换:', category)
  selectedCategory.value = category
}

// 精品商城已删除，handleShopCategoryChange 函数已移除

function completedEventsCount(): number {
  return availableEvents.value.filter(e => e.completed).length
}

const eventProgressPercentage = computed(() => {
  const completed = completedEventsCount()
  return availableEvents.value.length > 0 ? (completed / availableEvents.value.length) * 100 : 0
})

function getDifficultyLabel(difficulty: string): string {
  const labels: { [key: string]: string } = {
    easy: '简单',
    normal: '普通',
    hard: '困难',
    expert: '专家'
  }
  return labels[difficulty] || '未知'
}

function getRewardName(type: string): string {
  const names: { [key: string]: string } = {
    gold: '金币',
    exp: '经验',
    item: '道具'
  }
  return names[type] || type
}

function getPriorityLabel(priority: string): string {
  const labels: { [key: string]: string } = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级'
  }
  return labels[priority] || priority
}

function getTypeLabel(type: string): string {
  const labels: { [key: string]: string } = {
    combat: '战斗',
    deck: '卡组',
    resource: '资源',
    strategy: '策略'
  }
  return labels[type] || type
}

// 初始化数据
onMounted(async () => {
  try {
    loading.value = true
    
    // 检查认证状态
    const token = localStorage.getItem('token')
    const authStore = useAuthStore()
    console.log('[CampOfficial] 初始化检查:', {
      hasToken: !!token,
      isAuthenticated: authStore.isAuthenticated,
      user: authStore.user
    })
    
    if (!token || !authStore.isAuthenticated) {
      console.warn('[CampOfficial] 用户未登录，部分功能可能无法使用')
      showNotification('warning', '请先登录以使用完整功能', 'fas fa-exclamation-triangle')
    }
    
    // 从 API 加载所有数据
    console.log('[CampOfficial] 开始并行加载所有数据...')
    await Promise.all([
      loadCampData(),
      loadWalletData(),
      loadInventoryData(),
      loadShopData(),
      loadEventsData(),
      loadAISuggestions()
    ])
    
    // 更新选项卡计数
    updateTabCounts()
    
    console.log('[CampOfficial] 营地页面初始化完成，数据汇总:', {
      userCards: userCards.value.length,
      inventory: inventory.value.length,
      shopOffers: shopStore.offers.length,
      events: availableEvents.value.length,
      wallets: userWallets.value.length,
      aiSuggestions: aiSuggestions.value.length
    })
  } catch (error: any) {
    console.error('[CampOfficial] 营地页面初始化失败:', error)
    
    // 提取更详细的错误信息
    let errorMessage = '页面初始化失败'
    if (error?.userMessage) {
      errorMessage = error.userMessage
    } else if (error?.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error?.message) {
      errorMessage = error.message
    } else if (error?.code === 'ECONNREFUSED' || error?.code === 'ERR_NETWORK') {
      errorMessage = '无法连接到后端服务器，请检查服务是否正常运行'
    }
    
    showNotification('error', errorMessage, 'fas fa-exclamation-triangle')
    console.error('[CampOfficial] 错误详情:', {
      message: errorMessage,
      code: error?.code,
      status: error?.response?.status,
      url: error?.config?.url
    })
  } finally {
    loading.value = false
  }
})

// 加载营地数据
async function loadCampData() {
  try {
    console.log('[CampOfficial] 开始加载营地数据...')
    console.log('[CampOfficial] 当前 playerCharacter 状态:', {
      hasValue: !!playerCharacter.value,
      value: playerCharacter.value
    })
    
    // fetchCampData 会自动更新 store，playerCharacter 会通过 computed 自动更新
    const data = await campStore.fetchCampData()
    
    console.log('[CampOfficial] fetchCampData 返回的数据:', {
      hasData: !!data,
      hasUserPlayerCharacter: !!data?.userPlayerCharacter,
      userPlayerCharacter: data?.userPlayerCharacter
    })
    
    if (data) {
      // 映射 userCards 字段（后端返回 cardName，前端使用 name）
      const rawUserCards = data.userCards || []
      userCards.value = rawUserCards.map((card: any) => ({
        ...card,
        name: card.cardName || card.name,
        description: card.cardDescription || card.description,
        cardType: card.cardType || card.type
      }))
      
      // 处理角色卡牌数据：确保数量正确显示
      const rawCardCharacters = data.userCardCharacters || []
      
      // 后端应该已经确保每个 cardCharacterId 只有一个记录，quantity 字段存储总数量
      // 但为了处理可能的历史数据（多个记录），按 cardCharacterId 分组合并
      const characterMap = new Map<number | string, any>()
      rawCardCharacters.forEach((char: any) => {
        // 使用 cardCharacterId + 当前星级作为唯一键，避免不同星级被合并
        const star = Number(char.currentStarLevel ?? 1) || 1
        const baseKey = char.cardCharacterId ?? char.id
        const key = baseKey ? `${baseKey}::${star}` : null
        if (!key) {
          console.warn('[CampOfficial] 角色卡牌缺少唯一标识:', char)
          return
        }
        
        if (characterMap.has(key)) {
          // 如果已有该角色，累加数量（处理历史数据可能有多个记录的情况）
          const existing = characterMap.get(key)
          const existingQty = Number(existing.quantity ?? 1) || 0
          const newQty = Number(char.quantity ?? 0) || 0
          existing.quantity = existingQty + newQty
          // 保留最新的部署状态和其他属性
          if (char.isDeployed !== undefined) {
            existing.isDeployed = char.isDeployed
          }
          // 保留最新的ID（使用最新的记录ID）
          if (char.id) {
            existing.id = char.id
          }
        } else {
          // 新角色，直接添加，保持后端返回的 quantity 值（不强制设置为1）
          characterMap.set(key, {
            ...char,
            quantity: Number(char.quantity ?? 0) || 0
          })
        }
      })
      
      cardCharacters.value = Array.from(characterMap.values())
      
      // 确保所有角色卡牌都有 quantity 字段（但不强制设置为1，保持后端返回的真实值）
      cardCharacters.value.forEach((char: any) => {
        if (char.quantity === null || char.quantity === undefined) {
          char.quantity = 0 // 如果后端没有返回 quantity，设置为 0 而不是 1
        } else {
          char.quantity = Number(char.quantity) || 0 // 统一转为数字
        }
      })
      
      console.log('[CampOfficial] 角色卡牌数据已处理:', {
        rawCount: rawCardCharacters.length,
        processedCount: cardCharacters.value.length,
        characters: cardCharacters.value.map((c: any) => ({
          name: c.characterName,
          quantity: c.quantity,
          cardCharacterId: c.cardCharacterId,
          id: c.id
        }))
      })
      
      // 加载已装备的卡牌（卡组中的卡牌）
      await loadEquippedCards()
      
      // 等待一下让 Vue 响应式系统更新
      await new Promise(resolve => setTimeout(resolve, 100))
      
      // 调试日志
      console.log('[CampOfficial] 营地数据加载完成:', {
        hasPlayerCharacter: !!data.userPlayerCharacter,
        playerCharacterId: data.userPlayerCharacter?.id,
        playerCharacterName: data.userPlayerCharacter?.playerCharacterName,
        storePlayerCharacter: !!playerCharacter.value,
        storePlayerCharacterValue: playerCharacter.value,
        equippedCardsCount: equippedCards.value.length
      })
      
      // 再次检查 store 中的 playerCharacter
      console.log('[CampOfficial] campStore.playerCharacter:', {
        hasValue: !!campStore.playerCharacter,
        value: campStore.playerCharacter
      })
    } else {
      console.warn('[CampOfficial] 营地数据为空')
    }
  } catch (error) {
    console.error('[CampOfficial] 加载营地数据失败:', error)
  }
}

// 加载已装备的卡牌（卡组中的卡牌）
async function loadEquippedCards() {
  try {
    console.log('[CampOfficial] 开始加载卡组中的卡牌...')
    // 使用 getDeckCards 获取卡组中的卡牌（根据 loadoutId）
    const response = await userCardApi.getDeckCards(DEFAULT_LOADOUT_ID)
    
    if (response.data.code === 200) {
      const deckCardsData = response.data.data || []
      // 将后端数据映射到前端格式
      equippedCards.value = deckCardsData.map((card: any) => ({
        id: card.id || card.userCardId,
        name: card.cardName || card.name,
        cardType: card.cardType,
        rarity: card.cardRarity || card.rarity,
        manaCost: card.manaCost || card.actionPointCost || 1,
        quantity: card.quantity || 1,
        userCardId: card.id, // 保存 userCardId 用于更新
        loadoutId: card.loadoutId || DEFAULT_LOADOUT_ID,
        ...card
      }))
      console.log('[CampOfficial] 卡组卡牌加载完成，共', equippedCards.value.length, '张')
    } else {
      console.warn('[CampOfficial] 获取卡组卡牌失败:', response.data.message)
      equippedCards.value = []
    }
  } catch (error: any) {
    // 静默处理错误，避免阻塞页面加载（卡组功能是可选的）
    const status = error?.response?.status
    const errorMessage = error?.response?.data?.message || error?.response?.data?.error || error?.message
    
    if (status === 400) {
      // 400 错误通常是参数错误或接口未完全实现，使用警告级别
      console.warn('[CampOfficial] ⚠️ 卡组接口返回400错误:', {
        message: errorMessage,
        url: error?.config?.url,
        loadoutId: DEFAULT_LOADOUT_ID
      })
    } else if (status === 401) {
      console.warn('[CampOfficial] ⚠️ 认证失败，跳过卡组加载')
    } else if (status === 404) {
      console.warn('[CampOfficial] ⚠️ 卡组接口不存在（404），使用空卡组')
    } else {
      // 其他错误也使用警告级别，避免在控制台显示为错误
      console.warn('[CampOfficial] ⚠️ 加载卡组卡牌失败:', errorMessage || '未知错误')
    }
    
    // 所有错误都使用空数组，不影响其他功能
    equippedCards.value = []
  }
}

// 处理角色创建完成
async function handleCharacterCreated() {
  showClassModal.value = false
  
  // 立即开始刷新数据，最多重试3次
  let retryCount = 0
  const maxRetries = 3
  let success = false
  
  while (retryCount < maxRetries && !success) {
    try {
      // 等待后端数据保存（第一次等待时间稍长，后续重试等待时间递减）
      const waitTime = retryCount === 0 ? 500 : 300
      await new Promise(resolve => setTimeout(resolve, waitTime))
      
      console.log(`开始刷新营地数据 (尝试 ${retryCount + 1}/${maxRetries})...`)
      
      // 强制刷新营地数据
      await loadCampData()
      
      // 检查数据是否加载成功
      if (playerCharacter.value) {
        console.log('✅ 角色数据加载成功:', {
          id: playerCharacter.value.id,
          name: playerCharacter.value.playerCharacterName,
          code: playerCharacter.value.playerCharacterCode
        })
        success = true
        showNotification('success', `角色创建成功！欢迎，${playerCharacter.value.playerCharacterName || '冒险者'}！`, 'fas fa-check-circle')
        break
      } else {
        retryCount++
        if (retryCount < maxRetries) {
          console.log(`第 ${retryCount} 次尝试未获取到角色，准备重试...`)
        }
      }
    } catch (error) {
      console.error(`刷新角色数据失败 (尝试 ${retryCount + 1}):`, error)
      retryCount++
      if (retryCount >= maxRetries) {
        showNotification('error', '角色创建成功，但刷新数据失败，请手动刷新页面', 'fas fa-exclamation-triangle')
        return
      }
    }
  }
  
  // 如果所有重试都失败
  if (!success) {
    console.warn('角色数据仍未加载，可能需要手动刷新')
    showNotification('warning', '角色创建成功，但数据刷新失败，请手动刷新页面', 'fas fa-exclamation-triangle')
  }
}

// 加载钱包数据
async function loadWalletData() {
  try {
    console.log('[CampOfficial] 开始加载钱包数据...')
    await walletStore.loadWallets()
    userWallets.value = walletStore.wallets as any[]
    console.log('[CampOfficial] 钱包数据加载完成:', {
      count: userWallets.value.length,
      wallets: userWallets.value
    })
  } catch (error) {
    console.error('[CampOfficial] 加载钱包数据失败:', error)
    showNotification('error', '加载钱包数据失败', 'fas fa-exclamation-triangle')
  }
}

// 加载背包数据
async function loadInventoryData() {
  try {
    console.log('[CampOfficial] 开始加载背包数据...')
    await inventoryStore.fetchInventory()
    // 映射后端字段到前端字段（后端返回 itemName，前端使用 name）
    const rawInventory = inventoryStore.inventory as any[]
    inventory.value = rawInventory.map((item: any) => ({
      ...item,
      name: item.itemName || item.name,
      description: item.itemDescription || item.description,
      itemType: item.itemType || item.type
    }))
    console.log('[CampOfficial] 背包数据加载完成:', {
      count: inventory.value.length,
      items: inventory.value.map((i: any) => ({ id: i.id, name: i.name, quantity: i.quantity }))
    })
  } catch (error) {
    console.error('[CampOfficial] 加载背包数据失败:', error)
    showNotification('error', '加载背包数据失败', 'fas fa-exclamation-triangle')
  }
}

// 加载商城数据
async function loadShopData() {
  try {
    console.log('[CampOfficial] 开始加载商城数据...')
    await shopStore.fetchShopOffers()
    console.log('[CampOfficial] 商城数据加载完成:', {
      count: shopStore.offers.length,
      offers: shopStore.offers
    })
  } catch (error) {
    console.error('[CampOfficial] 加载商城数据失败:', error)
    showNotification('error', '加载商城数据失败', 'fas fa-exclamation-triangle')
  }
}

// 加载事件数据
async function loadEventsData() {
  try {
    console.log('[CampOfficial] 开始加载事件数据...')
    const response = await campApi.getEvents()
    if (response.data.code === 200) {
      availableEvents.value = response.data.data || []
      console.log('[CampOfficial] 事件数据加载完成:', {
        count: availableEvents.value.length,
        events: availableEvents.value
      })
    } else {
      console.warn('[CampOfficial] 事件数据加载失败:', response.data.message)
    }
  } catch (error) {
    console.error('[CampOfficial] 加载事件数据失败:', error)
    showNotification('error', '加载事件数据失败', 'fas fa-exclamation-triangle')
  }
}

// 加载AI建议
async function loadAISuggestions() {
  try {
    console.log('[CampOfficial] 开始加载AI建议...')
    const response = await campApi.getAISuggestions()
    if (response.data.code === 200) {
      aiSuggestions.value = response.data.data || []
      console.log('[CampOfficial] AI建议加载完成:', {
        count: aiSuggestions.value.length,
        suggestions: aiSuggestions.value
      })
    } else {
      console.warn('[CampOfficial] AI建议加载失败:', response.data.message)
    }
  } catch (error) {
    console.error('[CampOfficial] 加载AI建议失败:', error)
    showNotification('error', '加载AI建议失败', 'fas fa-exclamation-triangle')
  }
}

// 更新选项卡计数
function updateTabCounts() {
  functionTabs.value.forEach(tab => {
    switch (tab.id) {
      case 'cards':
        tab.count = userCards.value.length + cardCharacters.value.length
        break
      case 'inventory':
        tab.count = inventory.value.length
        break
      case 'shop':
        tab.count = shopStore.offers.length
        break
      case 'events':
        tab.count = availableEvents.value.length
        break
      case 'ai':
        tab.count = aiSuggestions.value.filter(s => s.priority === 5).length
        break
    }
  })
}
</script>

<style scoped>
.camp-official {
  display: grid;
  grid-template-rows: auto 1fr;
  height: 100vh;
  background: linear-gradient(135deg, rgba(5, 5, 10, 0.35), rgba(15, 35, 60, 0.45)),
    url('/yingdi.png') center / cover no-repeat fixed;
  color: #ffffff;
  overflow: hidden;
  position: relative;
}

/* 返回首页按钮 */
.back-to-home {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
}

.back-to-home:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.back-to-home i {
  font-size: 1rem;
}

/* 顶部导航栏 */
.main-header {
  grid-row: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1rem 2rem;
  z-index: 100;
}

/* 主内容区域 */
.main-content {
  grid-row: 2;
  display: grid;
  grid-template-columns: minmax(280px, 350px) 1fr;
  height: 100%;
  overflow: hidden;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1600px;
  margin: 0 auto;
}

.logo-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-left: 180px; /* 为返回首页按钮留出空间，避免重叠 */
}

.camp-title {
  font-size: 1.8rem;
  font-weight: bold;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  text-shadow: 0 0 30px rgba(255, 215, 0, 0.3);
}

.camp-subtitle {
  font-size: 0.9rem;
  color: #9ca3af;
  margin: 0;
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.currencies-compact {
  display: flex;
  gap: 1rem;
}

.currency-compact {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem 0.75rem;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-weight: 600;
  transition: all 0.3s;
}

.currency-compact:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.currency-compact .amount {
  color: #ffd700;
  font-weight: bold;
}

.quick-stats {
  display: flex;
  gap: 1rem;
}

.stat-compact {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.08);
  padding: 0.5rem 0.75rem;
  border-radius: 15px;
  font-size: 0.9rem;
  font-weight: 600;
}

.notifications-bell {
  position: relative;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.notifications-bell:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

.notifications-bell.active {
  background: rgba(76, 175, 80, 0.3);
}

.notification-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #4caf50;
  border-radius: 50%;
  animation: pulse-dot 2s infinite;
}

.currencies {
  display: flex;
  gap: 2rem;
}

.currency-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
}

.currency-item .amount {
  font-size: 1.2rem;
  color: var(--text-primary);
}

.currency-item .currency-name {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.gold { color: #ffd700; }
.purple { color: #9c27b0; }
.blue { color: #2196f3; }

.rewards-indicator {
  background: #4caf50;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
  animation: pulse-green 2s infinite;
}

/* 布局结构 - 优化空间分配 */
.camp-official {
  display: grid;
  grid-template-rows: auto 1fr;
  height: 100vh;
  background: linear-gradient(135deg, rgba(5, 5, 10, 0.35), rgba(15, 35, 60, 0.45)),
    url('/yingdi.png') center / cover no-repeat fixed; /* 进一步降低遮罩透明度，让篝火背景更清晰 */
  color: #ffffff;
  overflow: hidden;
}

.main-header {
  grid-row: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0.75rem 1.5rem;
  z-index: 100;
}

.main-content {
  grid-row: 2;
  display: grid;
  grid-template-columns: minmax(280px, 350px) 1fr;
  height: 100%;
  overflow: hidden;
}

.create-character-prompt {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 3rem 2rem;
  text-align: center;
  width: 100%;
}

.prompt-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.prompt-icon {
  font-size: 4rem;
  color: #d4af37;
  margin-bottom: 0.5rem;
  filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.5));
}

.create-character-prompt h2 {
  font-size: 1.8rem;
  color: #ffffff;
  margin: 0;
  font-weight: bold;
}

.create-character-prompt p {
  font-size: 1rem;
  color: #9ca3af;
  margin: 0;
  line-height: 1.6;
}

.create-character-btn {
  margin-top: 0.5rem;
  padding: 14px 32px;
  background: linear-gradient(135deg, #d4af37, #ffd700);
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
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
}

.create-character-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(212, 175, 55, 0.6);
  background: linear-gradient(135deg, #ffd700, #ffed4e);
}

.create-character-btn i {
  font-size: 1.2rem;
}

.character-sidebar {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-width: 280px;
  max-width: 350px;
}

.function-area {
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-width: 0; /* 允许收缩 */
}

/* 角色卡片 */
.character-card {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
}

.character-card.premium::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 215, 0, 0.1), transparent);
  animation: shimmer 3s infinite;
}

.character-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.character-avatar-section {
  position: relative;
  width: 80px;
  height: 80px;
}

.avatar-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(76, 175, 80, 0.3), transparent);
  border-radius: 50%;
  animation: glow-pulse 2s infinite;
}

.character-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
  position: relative;
  z-index: 2;
}

.level-ring {
  position: absolute;
  bottom: -5px;
  right: -5px;
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #4caf50, #45a049);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.1);
  z-index: 3;
}

.level-text {
  font-size: 0.7rem;
  font-weight: bold;
  color: white;
}

.character-info {
  flex: 1;
}

.character-name {
  font-size: 1.3rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.25rem 0;
}

.character-title {
  font-size: 0.9rem;
  color: #9ca3af;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  display: inline-block;
}

/* 核心状态 */
.core-stats {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.stat-icon {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.stat-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-bar {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.stat-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
  position: relative;
}

.stat-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer-horizontal 2s infinite;
}

.hp-fill { background: linear-gradient(90deg, #4caf50, #45a049); }
.ap-fill { background: linear-gradient(90deg, #2196f3, #1976d2); }
.stress-fill { 
  background: linear-gradient(90deg, #ff9800, #f44336); 
}

.stat-value {
  min-width: 80px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #e8e8e8;
}

/* 快速行动 */
.quick-actions {
  display: flex;
  gap: 0.75rem;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  color: #e8e8e8;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.action-btn.primary {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.3), rgba(76, 175, 80, 0.1));
  border-color: rgba(76, 175, 80, 0.5);
}

.action-btn.secondary {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.3), rgba(33, 150, 243, 0.1));
  border-color: rgba(33, 150, 243, 0.5);
}

.action-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
}

/* 压力 Debuff */
.stress-debuffs {
  margin-bottom: 1.5rem;
}

.stress-debuffs h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.debuff-list {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.debuff-item {
  background: rgba(244, 67, 54, 0.2);
  border: 1px solid #f44336;
  color: #f44336;
  padding: 0.25rem 0.5rem;
  border-radius: 15px;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.debuff-item.mental {
  border-color: #9c27b0;
  color: #9c27b0;
  background: rgba(156, 39, 176, 0.2);
}

.debuff-item.behavioral {
  border-color: #ff9800;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.2);
}

/* 装备卡牌展示 */
.equipped-showcase {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 1rem;
}

.showcase-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #ffd700;
  margin: 0 0 1rem 0;
}

.equipped-cards-compact {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}

.equipped-card-compact {
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.equipped-card-compact:hover {
  transform: translateY(-2px) scale(1.05);
}

.equipped-card-compact .mini-card {
  width: 100%;
  aspect-ratio: 2.5/3.5;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  padding: 0.5rem;
  position: relative;
  overflow: hidden;
}

.equipped-card-compact.rare .mini-card {
  border-color: #2196f3;
  box-shadow: 0 0 10px rgba(33, 150, 243, 0.3);
}

.equipped-card-compact.epic .mini-card {
  border-color: #9c27b0;
  box-shadow: 0 0 10px rgba(156, 39, 176, 0.3);
}

.equipped-card-compact.legendary .mini-card {
  border-color: #ff9800;
  box-shadow: 0 0 15px rgba(255, 152, 0, 0.4);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.2), rgba(255, 152, 0, 0.05));
}

.mini-card i {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
}

.card-cost {
  position: absolute;
  top: 2px;
  left: 2px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.6rem;
  font-weight: bold;
}

.card-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  font-size: 0.7rem;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: all 0.3s;
  z-index: 1000;
  margin-bottom: 0.5rem;
}

.card-tooltip strong {
  display: block;
  margin-bottom: 0.25rem;
}

.card-tooltip small {
  color: #9ca3af;
}

.equipped-card-compact:hover .card-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(-4px);
}

.more-cards {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: bold;
  color: #9ca3af;
  min-height: 40px;
}

.empty-equipped {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  min-height: 40px;
  font-size: 0.7rem;
  color: #6b7280;
}

.empty-equipped i {
  font-size: 1rem;
}

/* 压力监控 */
.stress-monitor {
  background: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 12px;
  padding: 1rem;
}

.monitor-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #f87171;
  margin: 0 0 1rem 0;
}

.debuffs-compact {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.debuff-compact {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  font-size: 0.8rem;
  color: #fca5a5;
  transition: all 0.3s;
}

.debuff-compact:hover {
  background: rgba(255, 255, 255, 0.1);
}

.debuff-compact i {
  color: #f87171;
}

/* 技能树状态 */
.skills-status {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.skills-status h4 {
  color: var(--text-primary);
  min-width: 100px;
}

.skill-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--tertiary-bg);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4caf50;
  transition: width 0.3s;
}

.progress-text {
  font-size: 0.8rem;
  color: var(--text-secondary);
  min-width: 80px;
}

.manage-skills-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.manage-skills-btn:hover {
  background: #45a049;
  text-decoration: none;
  color: white;
}

/* 现代化选项卡 */
.modern-tabs {
  display: flex;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  padding: 0.75rem;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.modern-tab-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  min-width: 100px;
}

.modern-tab-btn:hover {
  background: rgba(255, 255, 255, 0.08);
}

.modern-tab-btn.active {
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
}

.tab-icon {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  font-size: 1.2rem;
}

.modern-tab-btn.active .tab-icon {
  background: linear-gradient(135deg, #4caf50, #45a049);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.tab-indicator {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: #4caf50;
  border-radius: 1px;
  transition: width 0.3s;
}

.modern-tab-btn.active .tab-indicator {
  width: 20px;
}

.tab-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #9ca3af;
}

.modern-tab-btn.active .tab-label {
  color: #ffffff;
}

.tab-badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: #f44336;
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
  min-width: 18px;
  text-align: center;
}

/* 内容区域 */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.module-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.5rem;
  animation: fadeInUp 0.6s ease;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.module-header h3 {
  font-size: 1.3rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.module-description {
  color: #9ca3af;
  font-size: 0.9rem;
  margin: 0;
}

/* 区域通用样式 */
section {
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h3 {
  color: var(--text-primary);
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* 卡组管理模块 */
.filter-group {
  display: flex;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  padding: 0.5rem;
  border-radius: 12px;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.filter-btn.active {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
  border: 1px solid rgba(76, 175, 80, 0.4);
}







.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  font-size: 1.1rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: #ffffff;
}

.stat-label {
  font-size: 0.8rem;
  color: #9ca3af;
}

/* 稀有度筛选 */
.rarity-filters {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.rarity-filter {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.85rem;
}

.rarity-filter:hover {
  background: rgba(255, 255, 255, 0.1);
}

.rarity-filter.active {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.rarity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.rarity-dot.common { background: #9e9e9e; }
.rarity-dot.rare { background: #2196f3; }
.rarity-dot.epic { background: #9c27b0; }
.rarity-dot.legendary { background: #ff9800; }

/* 增强卡牌网格 */
.cards-enhanced-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1.25rem;
  margin-top: 1rem;
}

.card-3d {
  position: relative;
  cursor: pointer;
  transition: all 0.4s;
  transform-style: preserve-3d;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.card-3d:hover {
  transform: translateY(-8px) rotateX(5deg);
}

.card-inner {
  position: relative;
  width: 100%;
  height: 240px;
  transform-style: preserve-3d;
  transition: transform 0.6s;
}

.card-frame {
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.card-frame::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transform: rotate(45deg);
  transition: all 0.5s;
  opacity: 0;
}

.card-3d:hover .card-frame::before {
  animation: card-shine 0.5s ease;
}

.card-frame.common { border-color: #9e9e9e; }
.card-frame.rare { border-color: #2196f3; box-shadow: 0 0 20px rgba(33, 150, 243, 0.3); }
.card-frame.epic { border-color: #9c27b0; box-shadow: 0 0 20px rgba(156, 39, 176, 0.3); }
.card-frame.legendary { 
  border-color: #ff9800; 
  box-shadow: 0 0 30px rgba(255, 152, 0, 0.5);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.2), rgba(255, 152, 0, 0.05));
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.mana-cost {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #4a90e2, #357abd);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.rarity-indicator {
  display: flex;
  gap: 0.125rem;
}

.rarity-indicator i {
  color: #ffd700;
  font-size: 0.6rem;
}

.card-artwork {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 1rem 0;
  position: relative;
}

.card-artwork-icon {
  font-size: 3rem;
  color: rgba(255, 255, 255, 0.7);
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

.card-bottom {
  text-align: center;
}

.card-title {
  font-size: 0.9rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
}

.card-stats-mini {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.7rem;
  color: #9ca3af;
}

.equipped-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(76, 175, 80, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #4caf50;
  backdrop-filter: blur(2px);
}

.card-quantity-badge {
  position: absolute;
  bottom: -8px;
  right: -8px;
  background: #f44336;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
}

.card-3d.equipped {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.4);
}

.card-characters-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: rgba(13, 17, 23, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.section-header.compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.section-header.compact h4 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  font-size: 1rem;
  color: #ffffff;
}

.section-description {
  margin: 0.25rem 0 0;
  color: #9ca3af;
  font-size: 0.85rem;
}

.count-badge {
  background: rgba(76, 175, 80, 0.15);
  color: #9ae6b4;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
  border: 1px solid rgba(76, 175, 80, 0.4);
}

.card-characters-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  color: #9ca3af;
  text-align: center;
}

.card-characters-empty i {
  font-size: 2rem;
  color: rgba(255, 255, 255, 0.3);
}

.card-characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.character-card-mini {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 1rem;
  background: rgba(17, 24, 39, 0.8);
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-height: 200px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.35);
  cursor: pointer;
  transition: all 0.3s ease;
}

.character-card-mini:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 255, 255, 0.2);
  background: rgba(17, 24, 39, 0.95);
}

.character-card-mini:active {
  transform: translateY(-2px);
}

.character-card-mini.rarity-common {
  border-color: rgba(158, 158, 158, 0.5);
}

.character-card-mini.rarity-rare {
  border-color: rgba(33, 150, 243, 0.4);
  box-shadow: 0 12px 30px rgba(33, 150, 243, 0.25);
}

.character-card-mini.rarity-epic {
  border-color: rgba(156, 39, 176, 0.5);
  box-shadow: 0 12px 30px rgba(156, 39, 176, 0.3);
}

.character-card-mini.rarity-legendary {
  border-color: rgba(255, 152, 0, 0.7);
  box-shadow: 0 15px 35px rgba(255, 152, 0, 0.35);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.2), rgba(17, 24, 39, 0.9));
}

.character-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.rarity-tag {
  padding: 0.2rem 0.75rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #fff;
}

.rarity-tag.rarity-common { background: rgba(158, 158, 158, 0.4); }
.rarity-tag.rarity-rare { background: rgba(33, 150, 243, 0.5); }
.rarity-tag.rarity-epic { background: rgba(156, 39, 176, 0.6); }
.rarity-tag.rarity-legendary { background: linear-gradient(135deg, #ff9800, #ffb74d); }

.quantity-tag {
  font-weight: 600;
  color: #e8e8e8;
}

.star-up-btn {
  padding: 0.25rem 0.75rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: #1f2937;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.star-up-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #fcd34d 0%, #fbbf24 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(251, 191, 36, 0.3);
}

.star-up-btn:active:not(:disabled) {
  transform: translateY(0);
}

.star-up-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: rgba(107, 114, 128, 0.5);
  color: #9ca3af;
}

.character-name {
  font-size: 1rem;
  font-weight: bold;
  color: #ffffff;
}

.character-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #9ca3af;
}

.character-meta span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.character-stats {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #cbd5f5;
}

.character-stats span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.character-description {
  font-size: 0.75rem;
  color: #a5b4fc;
  margin: 0;
  line-height: 1.3;
}

.character-status {
  margin-top: auto;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.3rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.character-status.deployed {
  background: rgba(76, 175, 80, 0.2);
  color: #9ae6b4;
  border: 1px solid rgba(76, 175, 80, 0.4);
}

.character-status.standby {
  background: rgba(156, 163, 175, 0.15);
  color: #d1d5db;
  border: 1px solid rgba(156, 163, 175, 0.3);
}

.user-card {
  background: var(--tertiary-bg);
  border: 2px solid var(--border-color);
  border-radius: 8px;
  padding: 0.75rem;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.user-card.equipped {
  border-color: #4caf50;
  background: linear-gradient(135deg, var(--tertiary-bg), rgba(76, 175, 80, 0.1));
}

.user-card.common { border-color: #9e9e9e; }
.user-card.rare { border-color: #2196f3; }
.user-card.epic { border-color: #9c27b0; }
.user-card.legendary { border-color: #ff9800; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.card-name {
  font-weight: bold;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.card-cost {
  background: var(--text-accent);
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

.card-body {
  margin-bottom: 0.5rem;
  text-align: center;
}

.card-icon {
  font-size: 1.5rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.card-stats {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.mini-stat {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.7rem;
  color: var(--text-secondary);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quantity-badge {
  background: var(--secondary-bg);
  color: var(--text-primary);
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
}

.rarity-stars {
  display: flex;
  gap: 0.125rem;
  color: #ffd700;
  font-size: 0.6rem;
}

/* 背包管理增强 */
.inventory-nav {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.inventory-nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.inventory-nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.inventory-nav-item.active {
  background: rgba(33, 150, 243, 0.2);
  border-color: rgba(33, 150, 243, 0.4);
}

.nav-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.nav-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-label {
  font-weight: 600;
  color: #e8e8e8;
}

.nav-count {
  font-size: 0.8rem;
  color: #9ca3af;
}

.items-enhanced-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.item-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.item-visual {
  position: relative;
  flex-shrink: 0;
}

.item-icon-bg {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
}

.item-quantity-indicator {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #f44336;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.item-name {
  font-size: 1rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.item-desc {
  font-size: 0.8rem;
  color: #9ca3af;
  margin: 0;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.item-tags-enhanced {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}

.tag-enhanced {
  background: rgba(33, 150, 243, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(33, 150, 243, 0.4);
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.65rem;
  font-weight: 500;
}

.item-bind-status {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.7rem;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
}

.item-bind-status.bound {
  background: rgba(244, 67, 54, 0.2);
  color: #f87171;
}

.item-bind-status.unbound {
  background: rgba(76, 175, 80, 0.2);
  color: #4ade80;
}

.item-action {
  flex-shrink: 0;
}

.use-btn {
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.use-btn:hover {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-1px);
}

/* 商城模块 */
.shop-timer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 152, 0, 0.2);
  color: #fb923c;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  border: 1px solid rgba(255, 152, 0, 0.4);
  animation: pulse-orange 2s infinite;
}

.shop-categories-modern {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.shop-category-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
  color: #9ca3af;
}

.shop-category-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.shop-category-btn.active {
  background: rgba(255, 152, 0, 0.2);
  border-color: rgba(255, 152, 0, 0.4);
  color: #fb923c;
}

.category-badge {
  background: rgba(255, 152, 0, 0.3);
  color: #fb923c;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
}

.products-modern-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
  /* 固定8个位置：2行 x 4列 */
  max-width: 100%;
}

.product-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.25rem;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.product-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.product-card.discount {
  border-color: rgba(255, 152, 0, 0.4);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.1), rgba(255, 255, 255, 0.05));
}

.product-card.empty {
  background: rgba(255, 255, 255, 0.02);
  border: 2px dashed rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
}

.empty-slot-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.9rem;
  width: 100%;
  height: 100%;
}

.empty-slot-content i {
  font-size: 2rem;
  opacity: 0.5;
}

.product-visual {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.product-icon-wrapper {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: rgba(255, 255, 255, 0.8);
}

.discount-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #f44336;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.product-info {
  margin-bottom: 1rem;
}

.product-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
}

.product-desc {
  font-size: 0.8rem;
  color: #9ca3af;
  margin: 0 0 0.75rem 0;
  line-height: 1.4;
}

.product-tags {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}

.product-tag {
  background: rgba(33, 150, 243, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(33, 150, 243, 0.4);
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.65rem;
  font-weight: 500;
}

.product-purchase {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.current-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
  color: #ffd700;
  font-size: 1.2rem;
}

.original-price {
  font-size: 0.8rem;
  color: #6b7280;
  text-decoration: line-through;
}

.buy-btn {
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.buy-btn:hover:not(.disabled) {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.buy-btn.disabled {
  background: #4b5563;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 任务管理增强 */
.event-stats {
  background: rgba(76, 175, 80, 0.1);
  color: #4ade80;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.quest-progress {
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4caf50, #45a049);
  border-radius: 4px;
  transition: width 0.5s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer-horizontal 2s infinite;
}

.progress-label {
  min-width: 80px;
  font-size: 0.8rem;
  color: #9ca3af;
  font-weight: 600;
}

.quests-enhanced-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.quest-card {
  display: flex;
  gap: 1rem;
  padding: 1.25rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.quest-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.quest-card.featured {
  border-color: rgba(255, 152, 0, 0.4);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.1), rgba(255, 255, 255, 0.05));
}

.quest-visual {
  position: relative;
  flex-shrink: 0;
}

.quest-icon-bg {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  color: rgba(255, 255, 255, 0.8);
}

.quest-icon-bg.daily { background: rgba(33, 150, 243, 0.2); border: 1px solid rgba(33, 150, 243, 0.4); }
.quest-icon-bg.weekly { background: rgba(156, 39, 176, 0.2); border: 1px solid rgba(156, 39, 176, 0.4); }
.quest-icon-bg.special { background: rgba(255, 152, 0, 0.2); border: 1px solid rgba(255, 152, 0, 0.4); }

.featured-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff9800;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.quest-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.quest-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.quest-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
}

.quest-difficulty {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
}

.quest-difficulty.easy { background: rgba(76, 175, 80, 0.2); color: #4ade80; }
.quest-difficulty.normal { background: rgba(33, 150, 243, 0.2); color: #60a5fa; }
.quest-difficulty.hard { background: rgba(255, 152, 0, 0.2); color: #fb923c; }
.quest-difficulty.expert { background: rgba(239, 68, 68, 0.2); color: #f87171; }

.quest-description {
  color: #9ca3af;
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.5;
}

.quest-objectives {
  background: rgba(255, 255, 255, 0.05);
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.quest-objectives h5 {
  font-size: 0.8rem;
  font-weight: 600;
  color: #e8e8e8;
  margin: 0 0 0.5rem 0;
}

.objectives-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.objective-item {
  display: grid;
  grid-template-columns: 24px 1fr auto 60px;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
}

.objective-icon {
  color: #9ca3af;
  font-size: 0.9rem;
}

.objective-item.completed .objective-icon {
  color: #4ade80;
}

.objective-text {
  color: #e8e8e8;
  line-height: 1.4;
}

.objective-progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.objective-progress-bar .progress-fill {
  background: linear-gradient(90deg, #4caf50, #45a049);
  height: 100%;
  border-radius: 2px;
}

.objective-progress-text {
  font-weight: 600;
  color: #e8e8e8;
  text-align: right;
}

.quest-rewards {
  background: rgba(255, 215, 0, 0.1);
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 215, 0, 0.2);
}

.quest-rewards h5 {
  font-size: 0.8rem;
  font-weight: 600;
  color: #ffd700;
  margin: 0 0 0.5rem 0;
}

.reward-items {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.reward-item-enhanced {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(255, 215, 0, 0.2);
  color: #ffd700;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
}

.quest-action {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.start-quest-btn {
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.start-quest-btn:hover {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.quest-completed {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(76, 175, 80, 0.2);
  color: #4ade80;
  padding: 0.75rem 1rem;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 600;
  border: 1px solid rgba(76, 175, 80, 0.4);
}

/* AI助手模块 */
.refresh-ai-btn {
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.refresh-ai-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #1976d2, #1565c0);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.refresh-ai-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #4b5563;
}

.ai-status-indicator {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(76, 175, 80, 0.1);
  padding: 0.75rem 1rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.status-lights {
  display: flex;
  gap: 0.25rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4b5563;
  transition: all 0.3s;
}

.status-dot.active {
  background: #4ade80;
  box-shadow: 0 0 8px rgba(74, 222, 128, 0.5);
  animation: pulse-dot 2s infinite;
}

.status-text {
  font-size: 0.8rem;
  color: #9ca3af;
  font-weight: 500;
}

.ai-advice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.25rem;
  margin-bottom: 2rem;
}

.advice-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.25rem;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.advice-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.advice-card.combat { border-left: 3px solid #f44336; }
.advice-card.deck { border-left: 3px solid #4caf50; }
.advice-card.resource { border-left: 3px solid #ffd700; }
.advice-card.strategy { border-left: 3px solid #9c27b0; }

.advice-card.high { background: rgba(239, 68, 68, 0.1); }
.advice-card.medium { background: rgba(245, 158, 11, 0.1); }
.advice-card.low { background: rgba(34, 197, 94, 0.1); }

.advice-header {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.advice-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.8);
  flex-shrink: 0;
}

.advice-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.advice-title {
  font-size: 1rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
}

.advice-badges {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.priority-badge {
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.6rem;
  font-weight: bold;
  text-transform: uppercase;
}

.priority-badge.high { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.priority-badge.medium { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
.priority-badge.low { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

.type-badge {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.6rem;
  font-weight: 500;
}

.advice-content {
  margin-bottom: 1rem;
}

.advice-text {
  color: #e8e8e8;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0 0 1rem 0;
}

.advice-metrics {
  background: rgba(255, 255, 255, 0.05);
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.metric-item {
  display: grid;
  grid-template-columns: 80px 1fr 40px;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
}

.metric-key {
  color: #9ca3af;
  font-weight: 500;
}

.metric-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.metric-fill {
  height: 100%;
  background: linear-gradient(90deg, #4caf50, #45a049);
  border-radius: 2px;
}

.metric-value {
  text-align: right;
  font-weight: 600;
  color: #e8e8e8;
}

.advice-actions {
  display: flex;
  gap: 0.5rem;
}

.advice-action-btn {
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.8rem;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.advice-action-btn:hover {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-1px);
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.suggestion-header i {
  font-size: 1.2rem;
  color: var(--text-primary);
}

.suggestion-header h4 {
  color: var(--text-primary);
  font-size: 1rem;
}

.suggestion-priority {
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
  text-transform: uppercase;
}

.suggestion-priority.high {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
}

.suggestion-priority.medium {
  background: rgba(255, 152, 0, 0.2);
  color: #ff9800;
}

.suggestion-priority.low {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.suggestion-content {
  margin-bottom: 1rem;
}

.suggestion-content p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.suggestion-data {
  background: var(--secondary-bg);
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 0.5rem;
}

.suggestion-data h5 {
  color: var(--text-primary);
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.suggestion-data ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestion-data li {
  font-size: 0.7rem;
  color: var(--text-secondary);
  padding: 0.125rem 0;
}

.suggestion-actions {
  display: flex;
  gap: 0.5rem;
}

.suggestion-action-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.suggestion-action-btn:hover {
  background: #45a049;
}

.ai-metrics {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1rem;
}

.ai-metrics h4 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}

.metric-card {
  background: var(--secondary-bg);
  padding: 0.75rem;
  border-radius: 6px;
  text-align: center;
  position: relative;
}

.metric-value {
  display: block;
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.metric-label {
  font-size: 0.7rem;
  color: var(--text-secondary);
}

.metric-change {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  font-size: 0.6rem;
  font-weight: bold;
}

.metric-change.positive {
  color: #4caf50;
}

.metric-change.negative {
  color: #f44336;
}

/* 通知提示 */
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

/* 压力详情弹窗样式 */
.stress-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  backdrop-filter: blur(6px);
}

.stress-modal-container {
  width: min(1024px, 95vw);
  max-height: 94vh;
  background: rgba(13, 17, 23, 0.95);
  border-radius: 16px;
  border: 1px solid rgba(59, 130, 246, 0.3);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.stress-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.75rem;
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
}

.stress-modal-body {
  padding: 1.75rem 1.75rem 1.5rem;
  overflow-y: auto;
}

.stress-summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.25rem;
  margin-bottom: 1.5rem;
}

.stress-summary-card {
  background: rgba(17, 24, 39, 0.9);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  border: 1px solid rgba(59, 130, 246, 0.35);
}

.stress-summary-card .label {
  display: block;
  font-size: 1rem;
  color: #9ca3af;
  margin-bottom: 0.35rem;
}

.stress-summary-card .value {
  font-size: 1.6rem;
  font-weight: 600;
  color: #e5e7eb;
}

.stress-levels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.stress-level-card {
  background: rgba(17, 24, 39, 0.9);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  border: 1px solid rgba(55, 65, 81, 0.8);
}

.stress-level-card.level-1 { border-color: var(--stress-low); }
.stress-level-card.level-2 { border-color: var(--stress-medium); }
.stress-level-card.level-3 { border-color: var(--stress-high); }
.stress-level-card.level-4 { border-color: var(--stress-max); }

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.6rem;
}

.level-tag {
  font-size: 1.05rem;
  font-weight: 600;
  color: #e5e7eb;
}

.level-range {
  font-size: 0.9rem;
  color: #9ca3af;
}

.debuff-list {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.debuff-row {
  padding: 0.35rem 0.4rem;
  border-radius: 8px;
  background: rgba(31, 41, 55, 0.9);
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
}

.debuff-row.mental { border-left: 3px solid #9c27b0; }
.debuff-row.combat { border-left: 3px solid var(--error); }
.debuff-row.behavioral { border-left: 3px solid var(--warning); }

.debuff-main {
  flex: 1;
}

.debuff-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #e5e7eb;
}

.debuff-desc {
  font-size: 0.95rem;
  color: #9ca3af;
}

.debuff-extra .chance {
  font-size: 0.9rem;
  color: var(--text-accent);
  font-weight: 600;
}

.debuff-empty {
  font-size: 0.9rem;
  color: #6b7280;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}

@keyframes shimmer-horizontal {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}

@keyframes card-shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateX(200%) translateY(200%) rotate(45deg); opacity: 0; }
}

@keyframes glow-pulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.6; }
}

@keyframes pulse-green {
  0% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(76, 175, 80, 0); }
  100% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0); }
}

@keyframes pulse-orange {
  0% { box-shadow: 0 0 0 0 rgba(255, 152, 0, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(255, 152, 0, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 152, 0, 0); }
}

@keyframes pulse-dot {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .character-sidebar {
    width: 280px;
  }
  
  .cards-enhanced-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  
  .products-modern-grid,
  .items-enhanced-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 992px) {
  .camp-official {
    flex-direction: column;
    height: auto;
  }
  
  .character-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .character-card.premium {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
  
  .function-area {
    min-height: 60vh;
  }
}

@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: minmax(260px, 300px) 1fr;
  }
  
  .character-sidebar {
    padding: 0.75rem;
  }
  
  .function-area {
    padding: 0.75rem;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .logo-section {
    margin-left: 0; /* 移动端不需要左边距 */
  }
  
  .status-bar {
    width: 100%;
    justify-content: space-around;
  }
  
  .main-content {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }
  
  .character-sidebar {
    max-width: none;
    min-height: auto;
    padding: 1rem;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .modern-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .modern-tab-btn {
    min-width: 80px;
    padding: 0.75rem 1rem;
  }
  
  .cards-enhanced-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 1rem;
  }
  
  .products-modern-grid,
  .items-enhanced-list {
    grid-template-columns: 1fr;
  }
  
  .ai-advice-grid {
    grid-template-columns: 1fr;
  }
  
  .quest-card {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .quest-header {
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }
  
  .objective-item {
    grid-template-columns: 24px 1fr 60px;
    gap: 0.25rem;
  }
  
  .shop-categories-modern {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .main-header {
    padding: 0.5rem;
  }
  
  .camp-title {
    font-size: 1.3rem;
  }
  
  .header-content {
    gap: 0.5rem;
  }
  
  .status-bar {
    flex-direction: column;
    gap: 1rem;
  }
  
  .currencies-compact,
  .quick-stats {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .modern-tab-btn {
    min-width: 80px;
    padding: 0.5rem 0.75rem;
  }
  
  .tab-label {
    font-size: 0.8rem;
  }
  
  .character-sidebar {
    padding: 0.75rem;
  }
  
  .function-area {
    padding: 0.75rem;
  }
}
  

  
  .character-sidebar {
    padding: 1rem;
  
}

@media (max-width: 480px) {
  .header-content {
    padding: 0.5rem;
  }
  
  .camp-title {
    font-size: 1.4rem;
  }
  
  .logo-section {
    margin-left: 0; /* 小屏幕不需要左边距 */
  }
  
  .currencies-compact {
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.5rem;
  }
  
  .quick-stats {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .modern-tabs {
    padding: 0.5rem;
  }
  
  .modern-tab-btn {
    min-width: 60px;
    padding: 0.5rem 0.75rem;
  }
  
  .tab-icon {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }
  
  .tab-label {
    font-size: 0.8rem;
  }
  
  .function-area {
    padding: 1rem;
  }
  
  .module-card {
    padding: 1rem;
  }
  
  .character-card.premium {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  

  
  .cards-enhanced-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 0.75rem;
  }
  
  .card-3d .card-frame {
    padding: 0.5rem;
  }
  
  .card-artwork-icon {
    font-size: 1.5rem;
  }
  
  .card-title {
    font-size: 0.75rem;
  }
  
  .mana-cost {
    width: 20px;
    height: 20px;
    font-size: 0.7rem;
  }
  
  .product-card,
  .item-card,
  .quest-card {
    padding: 0.75rem;
  }
  
  .objective-progress-text {
    display: none;
  }
  
  .shop-category-btn {
    padding: 0.5rem 0.75rem;
    font-size: 0.8rem;
  }
  
  .equipped-cards-compact {
    grid-template-columns: repeat(4, 1fr);
    gap: 0.4rem;
  }
  
  .mini-card {
    aspect-ratio: 2.5/3.5;
    padding: 0.25rem;
  }
  
  .mini-card i {
    font-size: 1rem;
  }
  
  .module-header h3 {
    font-size: 1.1rem;
  }
  
  .module-description {
    font-size: 0.8rem;
  }
  
  .stat-box {
    padding: 1rem;
  }
  
  .stat-number {
    font-size: 1.2rem;
  }
  
  .quest-card {
    flex-direction: column;
    text-align: left;
  }
  
  .quest-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .quest-objectives,
  .quest-rewards {
    padding: 0.5rem;
  }
  
  .objective-item {
    grid-template-columns: 20px 1fr 50px;
    gap: 0.25rem;
  }
}
</style>