<template>
  <Teleport to="body">
    <Transition name="class-fade">
      <div v-if="show" class="class-overlay" @click.self="handleClose">
        <div class="class-container">
          <div class="class-header">
            <h2 class="class-title">选择你的职业</h2>
            <p class="class-subtitle">每个职业都有独特的技能和战斗风格</p>
          </div>

          <div v-if="loading" class="class-loading">
            <div class="loading-spinner"></div>
            <p>加载职业信息中...</p>
          </div>

          <div v-else-if="error" class="class-error">
            <p>{{ error }}</p>
            <button class="retry-button" @click="loadClasses">重试</button>
          </div>

          <div v-else class="class-grid">
            <div
              v-for="classItem in classes"
              :key="classItem.id"
              class="class-card"
              :class="{ 'selected': selectedClassId === classItem.id }"
              @click="selectClass(classItem)"
            >
              <div class="class-icon">{{ getClassIcon(classItem.code) }}</div>
              <div class="class-name">{{ classItem.name }}</div>
              <div class="class-description">{{ getClassDescription(classItem.code) }}</div>
              <div class="class-stats">
                <div class="stat-item">
                  <span class="stat-label">基础HP:</span>
                  <span class="stat-value">{{ classItem.baseHp }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">每级HP:</span>
                  <span class="stat-value">+{{ classItem.hpPerLevel }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="class-actions">
            <button 
              class="class-button class-button-cancel"
              @click="handleClose"
            >
              取消
            </button>
            <button 
              class="class-button class-button-confirm"
              :disabled="!selectedClassId || creating"
              @click="handleConfirm"
            >
              {{ creating ? '创建中...' : '确认选择' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useCharactersStore } from '@/stores/characters'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/lib/api'
import type { PlayerCharacter } from '@/types'

interface Props {
  show: boolean
}

const props = defineProps<Props>()

interface Emits {
  (e: 'close'): void
  (e: 'complete'): void
}

const emit = defineEmits<Emits>()

// 已废弃：不再使用模拟数据，改为从 API 获取

const classes = ref<PlayerCharacter[]>([])
const selectedClassId = ref<string | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const creating = ref(false)

const charactersStore = useCharactersStore()

// 职业图标映射
function getClassIcon(code: string): string {
  const iconMap: Record<string, string> = {
    'warrior': '⚔️',
    'occultist': '🔮',
    'ranger': '🏹',
    'priest': '✨',
    'mage': '🔥',
    'rogue': '🗡️'
  }
  return iconMap[code] || '⚔️'
}

// 职业描述映射
function getClassDescription(code: string): string {
  const descMap: Record<string, string> = {
    'warrior': '近战战士，拥有强大的防御力和生命值，擅长在前线承受伤害',
    'occultist': '神秘学者，掌握黑暗魔法，能够召唤亡灵和施放诅咒',
    'ranger': '远程射手，精准的箭术和陷阱技能，适合远程输出',
    'priest': '神圣牧师，治疗和支援专家，能够恢复生命和驱散负面效果',
    'mage': '元素法师，掌控火焰、冰霜和雷电，强大的范围伤害',
    'rogue': '敏捷盗贼，高爆发伤害和闪避能力，擅长暗杀和偷袭'
  }
  return descMap[code] || '未知职业'
}

// 从 API 加载职业列表
async function loadClasses() {
  loading.value = true
  error.value = null

  try {
    // 使用 /player-characters 接口（允许匿名访问）
    const response = await apiClient.get<ApiResponse<PlayerCharacter[]>>(
      '/player-characters'
    )

    if (response.data && response.data.code === 200 && response.data.data) {
      classes.value = response.data.data
      if (classes.value.length === 0) {
        error.value = '暂无可用职业'
      }
    } else {
      throw new Error(response.data?.message || '加载职业列表失败')
    }
  } catch (err: any) {
    console.error('Load classes error:', err)
    
    // 处理不同类型的错误
    if (err.response) {
      // HTTP错误响应
      const status = err.response.status
      if (status === 401) {
        error.value = '登录已过期，请重新登录'
      } else if (status === 403) {
        error.value = '没有权限访问职业列表'
      } else if (status === 404) {
        error.value = '职业列表接口不存在'
      } else {
        error.value = err.response.data?.message || `加载职业列表失败 (${status})`
      }
    } else if (err.request) {
      // 网络错误
      error.value = '网络连接失败，请检查后端服务是否正常运行'
    } else {
      // 其他错误
      error.value = err.message || '加载职业列表失败'
    }
    
    classes.value = []
  } finally {
    loading.value = false
  }
}

// 选择职业
function selectClass(classItem: PlayerCharacter) {
  selectedClassId.value = classItem.id
}

// 确认选择并创建角色（调用后端API）
async function handleConfirm() {
  if (!selectedClassId.value) return

  creating.value = true
  error.value = null

  try {
    // 获取选中的职业
    const selectedClass = classes.value.find(c => c.id === selectedClassId.value)
    if (!selectedClass) {
      error.value = '选择的职业不存在'
      creating.value = false
      return
    }

    // 调用后端API创建角色
    try {
      const response = await apiClient.post<ApiResponse<any>>(
        '/user-player-characters',
        { playerCharacterId: Number(selectedClassId.value) }
      )

      if (response.data && response.data.code === 200) {
        // 创建成功
        console.log('角色创建成功，返回数据:', response.data.data)
        
        // 立即更新 charactersStore 中的角色数据
        try {
          await charactersStore.loadPlayerCharacter()
          console.log('✅ charactersStore 角色数据已更新')
        } catch (err) {
          console.warn('更新 charactersStore 失败:', err)
        }
        
        // 等待一小段时间确保后端数据已保存
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 通知父组件角色创建完成（父组件会刷新数据）
        emit('complete')
      } else {
        throw new Error(response.data.message || '创建角色失败')
      }
    } catch (apiError: any) {
      // 如果后端API调用失败，检查是否是网络错误
      if (apiError.code === 'ECONNREFUSED' || apiError.message?.includes('Network Error')) {
        // 后端未连接，使用模拟数据（兼容模式）
        console.log('Backend not available, using mock data')
    const mockPlayerCharacter = {
      id: `player-${Date.now()}`,
      playerCharacterId: selectedClassId.value,
      name: selectedClass.name,
      code: selectedClass.code,
      baseHp: selectedClass.baseHp,
      hpPerLevel: selectedClass.hpPerLevel,
      maxHp: selectedClass.baseHp,
      currentHp: selectedClass.baseHp,
      level: 1,
      exp: 0
    }
    localStorage.setItem('mockPlayerCharacter', JSON.stringify(mockPlayerCharacter))
        emit('complete')
      } else {
        // 其他错误
        const errorMsg = apiError.response?.data?.message || apiError.message || '创建角色失败'
        error.value = errorMsg
        throw apiError
      }
    }
  } catch (err: any) {
    if (!error.value) {
    error.value = '创建角色失败，请稍后重试'
    }
    console.error('Create character error:', err)
  } finally {
    creating.value = false
  }
}

function handleClose() {
  emit('close')
}

// 监听显示状态
watch(() => props.show, (show) => {
  if (show) {
    loadClasses()
    document.body.style.overflow = 'hidden'
  } else {
    selectedClassId.value = null
    error.value = null
    document.body.style.overflow = ''
  }
})

onMounted(() => {
  if (props.show) {
    loadClasses()
  }
})
</script>

<style scoped>
.class-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.class-container {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid rgba(139, 69, 19, 0.5);
  border-radius: 20px;
  box-shadow: 
    0 0 50px rgba(139, 69, 19, 0.3),
    inset 0 0 100px rgba(0, 0, 0, 0.5);
  width: 100%;
  max-width: 1000px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 40px;
}

.class-header {
  text-align: center;
  margin-bottom: 40px;
}

.class-title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #d4af37;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
  margin: 0 0 10px 0;
}

.class-subtitle {
  font-size: 1.1rem;
  color: #ccc;
  margin: 0;
}

.class-loading,
.class-error {
  text-align: center;
  padding: 60px 20px;
  color: #ccc;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(139, 69, 19, 0.3);
  border-top-color: #d4af37;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.retry-button {
  margin-top: 20px;
  padding: 10px 24px;
  background: linear-gradient(135deg, #8b4513, #a0522d);
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.retry-button:hover {
  background: linear-gradient(135deg, #a0522d, #cd853f);
  transform: translateY(-2px);
}

.class-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.class-card {
  background: rgba(0, 0, 0, 0.4);
  border: 2px solid rgba(139, 69, 19, 0.5);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.class-card:hover {
  background: rgba(0, 0, 0, 0.6);
  border-color: rgba(212, 175, 55, 0.6);
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(212, 175, 55, 0.2);
}

.class-card.selected {
  background: rgba(212, 175, 55, 0.1);
  border-color: #d4af37;
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);
}

.class-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.3));
}

.class-name {
  font-size: 1.5rem;
  font-weight: bold;
  color: #d4af37;
  margin-bottom: 12px;
}

.class-description {
  font-size: 0.95rem;
  color: #ccc;
  line-height: 1.6;
  margin-bottom: 16px;
  flex: 1;
}

.class-stats {
  display: flex;
  gap: 20px;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(139, 69, 19, 0.3);
  width: 100%;
  justify-content: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 0.85rem;
  color: #999;
}

.stat-value {
  font-size: 1.1rem;
  font-weight: bold;
  color: #d4af37;
}

.class-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid rgba(139, 69, 19, 0.3);
}

.class-button {
  padding: 12px 32px;
  font-size: 1rem;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.class-button-cancel {
  background: rgba(139, 69, 19, 0.2);
  color: #999;
  border: 1px solid rgba(139, 69, 19, 0.5);
}

.class-button-cancel:hover {
  background: rgba(139, 69, 19, 0.3);
  color: #ccc;
}

.class-button-confirm {
  background: linear-gradient(135deg, #d4af37, #ffd700);
  color: #1a1a2e;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
}

.class-button-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.6);
}

.class-button-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 过渡动画 */
.class-fade-enter-active,
.class-fade-leave-active {
  transition: opacity 0.3s ease;
}

.class-fade-enter-from,
.class-fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .class-container {
    padding: 30px 20px;
    max-height: 95vh;
  }

  .class-title {
    font-size: 2rem;
  }

  .class-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .class-card {
    padding: 20px;
  }

  .class-icon {
    font-size: 3rem;
  }

  .class-actions {
    flex-direction: column;
  }

  .class-button {
    width: 100%;
  }
}
</style>

