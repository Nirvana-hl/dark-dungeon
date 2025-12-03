<template>
  <div class="inventory-panel">
    <!-- 物品分类导航 -->
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
    <div class="items-list">
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
            <div class="item-tags">
              <span 
                v-for="tag in item.tags" 
                :key="tag"
                class="tag"
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
    
    <!-- 空状态 -->
    <div v-if="filteredInventory.length === 0" class="empty-state">
      <div class="empty-icon">
        <i class="fas fa-box-open"></i>
      </div>
      <h3 class="empty-title">该分类暂无物品</h3>
      <p class="empty-desc">前往商城购买更多道具</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

interface Props {
  inventory: any[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  useItem: [item: any]
  changeCategory: [category: string]
}>()

// 状态
const selectedCategory = ref('consumable')

// 分类选项
const inventoryCategories = ref([
  { type: 'consumable', name: '消耗品', icon: 'fas fa-flask' },
  { type: 'material', name: '材料', icon: 'fas fa-gem' },
  { type: 'equipment', name: '装备', icon: 'fas fa-shield-alt' },
  { type: 'special', name: '特殊', icon: 'fas fa-star' }
])

// 计算属性
const filteredInventory = computed(() => {
  return props.inventory.filter(item => item && item.itemType === selectedCategory.value)
})

// 方法
const getCategoryItemCount = (type: string) => {
  return props.inventory.filter(item => item && item.itemType === type).length
}

const useItem = (item: any) => {
  if (item.quantity > 0) {
    emit('useItem', item)
  }
}

const getItemIcon = (type: string) => {
  const icons: { [key: string]: string } = {
    consumable: 'fas fa-flask',
    material: 'fas fa-gem',
    equipment: 'fas fa-shield-alt',
    special: 'fas fa-star'
  }
  return icons[type] || 'fas fa-box'
}

// 监听分类变化
watch(selectedCategory, (newCategory: string) => {
  emit('changeCategory', newCategory)
})
</script>

<style scoped>
.inventory-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

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

.items-list {
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

.item-tags {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}

.tag {
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

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: #9ca3af;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: #e8e8e8;
}

.empty-desc {
  font-size: 0.9rem;
  margin: 0;
  opacity: 0.8;
}

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

@media (max-width: 768px) {
  .inventory-nav {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }
  
  .items-list {
    grid-template-columns: 1fr;
  }
  
  .item-card {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .item-meta {
    flex-direction: column;
    gap: 0.5rem;
    align-items: center;
  }
}
</style>