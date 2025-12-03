<template>
  <div class="ai-assistant" :class="{ 'expanded': isExpanded, 'minimized': isMinimized }">
    <!-- AI助手头部 -->
    <header class="assistant-header" @click="toggleExpanded">
      <div class="assistant-avatar">
        <div class="avatar-glow"></div>
        <i class="fas fa-robot"></i>
        <div v-if="isTyping" class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      
      <div class="assistant-info">
        <h3>AI助手</h3>
        <p class="assistant-status">{{ getStatusText() }}</p>
      </div>
      
      <div class="assistant-controls">
        <button 
          v-if="!isMinimized"
          class="control-btn minimize"
          @click.stop="toggleMinimized"
          title="最小化"
        >
          <i class="fas fa-minus"></i>
        </button>
        <button 
          v-if="!isMinimized"
          class="control-btn close"
          @click.stop="toggleMinimized"
          title="关闭"
        >
          <i class="fas fa-times"></i>
        </button>
        <button 
          v-else
          class="control-btn open"
          @click.stop="toggleMinimized"
          title="打开助手"
        >
          <i class="fas fa-comment-dots"></i>
        </button>
      </div>
    </header>

    <!-- 展开的内容区域 -->
    <div v-if="isExpanded && !isMinimized" class="assistant-content">
      <!-- 快捷建议 -->
      <div class="quick-suggestions" v-if="suggestions.length > 0">
        <h4>智能建议</h4>
        <div class="suggestions-grid">
          <div 
            v-for="suggestion in suggestions" 
            :key="suggestion.id"
            class="suggestion-card"
            :class="suggestion.type"
            @click="applySuggestion(suggestion)"
          >
            <div class="suggestion-icon">
              <i :class="getSuggestionIcon(suggestion.type)"></i>
            </div>
            <div class="suggestion-content">
              <h5>{{ suggestion.title }}</h5>
              <p>{{ suggestion.description }}</p>
            </div>
            <div class="suggestion-priority" :class="suggestion.priority">
              {{ getPriorityText(suggestion.priority) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天界面 -->
      <div class="chat-interface">
        <div class="chat-messages" ref="messagesContainer">
          <div 
            v-for="message in chatMessages" 
            :key="message.id"
            class="message"
            :class="message.role"
          >
            <div class="message-avatar">
              <i :class="message.role === 'assistant' ? 'fas fa-robot' : 'fas fa-user'"></i>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              
              <!-- AI建议操作 -->
              <div v-if="message.actions && message.actions.length > 0" class="message-actions">
                <button 
                  v-for="action in message.actions" 
                  :key="action.id"
                  class="action-btn"
                  :class="action.type"
                  @click="executeAction(action)"
                >
                  <i :class="action.icon"></i>
                  {{ action.label }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <div class="input-container">
            <button 
              v-for="quickAction in quickActions"
              :key="quickAction.type"
              class="quick-action-btn"
              :class="quickAction.type"
              @click="sendQuickAction(quickAction)"
              :title="quickAction.description"
            >
              <i :class="quickAction.icon"></i>
            </button>
            
            <input 
              v-model="inputMessage"
              type="text" 
              placeholder="向AI助手询问游戏策略..."
              @keypress="handleKeyPress"
              :disabled="isTyping"
              ref="messageInput"
            />
            
            <button 
              class="send-btn"
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isTyping"
            >
              <i class="fas fa-paper-plane"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 分析面板 -->
      <div class="analysis-panel" v-if="showAnalysis">
        <h4>游戏分析</h4>
        <div class="analysis-grid">
          <div class="analysis-item">
            <div class="analysis-label">胜率分析</div>
            <div class="analysis-value" :class="getAnalysisClass(gameData.winRate)">
              {{ (gameData.winRate * 100).toFixed(1) }}%
            </div>
          </div>
          <div class="analysis-item">
            <div class="analysis-label">平均回合数</div>
            <div class="analysis-value">{{ gameData.avgRounds }}</div>
          </div>
          <div class="analysis-item">
            <div class="analysis-label">卡牌使用率</div>
            <div class="analysis-value">{{ (gameData.cardUsage * 100).toFixed(1) }}%</div>
          </div>
          <div class="analysis-item">
            <div class="analysis-label">压力管理</div>
            <div class="analysis-value" :class="getAnalysisClass(gameData.stressManagement)">
              {{ gameData.stressManagement }}/100
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新消息提示 -->
    <div 
      v-if="unreadCount > 0 && isMinimized" 
      class="unread-badge"
      @click="toggleMinimized"
    >
      {{ unreadCount }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  actions?: Array<{
    id: string
    type: string
    label: string
    icon: string
    action: () => void
  }>
}

interface Suggestion {
  id: string
  type: 'combat' | 'strategy' | 'equipment' | 'stress'
  title: string
  description: string
  priority: 'low' | 'medium' | 'high'
  action: () => void
}

interface QuickAction {
  type: 'analyze' | 'suggest' | 'help' | 'optimize'
  label: string
  icon: string
  description: string
  message: string
}

// 响应式数据
const isExpanded = ref(false)
const isMinimized = ref(false)
const isTyping = ref(false)
const inputMessage = ref('')
const showAnalysis = ref(false)
const unreadCount = ref(0)

const chatMessages = ref<ChatMessage[]>([
  {
    id: '1',
    role: 'assistant',
    content: '你好！我是你的AI助手，可以为你提供游戏策略建议、战斗分析和优化方案。有什么可以帮助你的吗？',
    timestamp: new Date()
  }
])

const suggestions = ref<Suggestion[]>([
  {
    id: '1',
    type: 'combat',
    title: '优化卡组搭配',
    description: '根据你的战斗数据，建议增加更多高攻击力角色卡',
    priority: 'high',
    action: () => {
      showMessage('正在为你分析最优卡组搭配...', 'info')
    }
  },
  {
    id: '2',
    type: 'strategy',
    title: '调整战斗策略',
    description: '你的压力值较高，建议优先部署治疗型角色',
    priority: 'medium',
    action: () => {
      showMessage('压力管理策略已更新', 'success')
    }
  },
  {
    id: '3',
    type: 'equipment',
    title: '装备升级推荐',
    description: '检测到可以升级关键装备，提升战斗效率',
    priority: 'low',
    action: () => {
      showMessage('正在生成装备升级方案...', 'info')
    }
  }
])

const quickActions: QuickAction[] = [
  {
    type: 'analyze',
    label: '分析',
    icon: 'fas fa-chart-line',
    description: '分析当前游戏状态',
    message: '请帮我分析当前的游戏状态'
  },
  {
    type: 'suggest',
    label: '建议',
    icon: 'fas fa-lightbulb',
    description: '获取智能建议',
    message: '请给我一些游戏建议'
  },
  {
    type: 'help',
    label: '帮助',
    icon: 'fas fa-question-circle',
    description: '获取游戏帮助',
    message: '我需要游戏帮助'
  },
  {
    type: 'optimize',
    label: '优化',
    icon: 'fas fa-cogs',
    description: '优化角色配置',
    message: '请帮我优化角色配置'
  }
]

const gameData = ref({
  winRate: 0.65,
  avgRounds: 12,
  cardUsage: 0.78,
  stressManagement: 85
})

// 计算属性
const messagesContainer = ref<HTMLElement | null>(null)
const messageInput = ref<HTMLInputElement | null>(null)

// 方法
function toggleExpanded() {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
    })
  }
}

function toggleMinimized() {
  isMinimized.value = !isMinimized.value
  if (!isMinimized.value) {
    isExpanded.value = true
  }
}

function getStatusText(): string {
  if (isTyping.value) return '思考中...'
  if (suggestions.value.length > 0) return `有${suggestions.value.length}条建议`
  return '在线'
}

function getSuggestionIcon(type: string): string {
  switch (type) {
    case 'combat': return 'fas fa-sword'
    case 'strategy': return 'fas fa-chess'
    case 'equipment': return 'fas fa-shield-alt'
    case 'stress': return 'fas fa-brain'
    default: return 'fas fa-info-circle'
  }
}

function getPriorityText(priority: string): string {
  switch (priority) {
    case 'high': return '高'
    case 'medium': return '中'
    case 'low': return '低'
    default: return '低'
  }
}

function getAnalysisClass(value: number): string {
  if (value >= 80) return 'good'
  if (value >= 60) return 'medium'
  return 'poor'
}

function applySuggestion(suggestion: Suggestion) {
  suggestion.action()
  
  // 添加用户操作消息
  addMessage(`用户采纳了建议: ${suggestion.title}`, 'user')
  
  // 添加AI回复
  setTimeout(() => {
    addMessage(`已执行"${suggestion.title}"建议，效果将在后续战斗中体现。`, 'assistant')
  }, 1000)
}

function sendQuickAction(action: QuickAction) {
  inputMessage.value = action.message
  sendMessage()
}

async function sendMessage() {
  if (!inputMessage.value.trim() || isTyping.value) return

  const userMessage = inputMessage.value.trim()
  addMessage(userMessage, 'user')
  inputMessage.value = ''

  // 显示AI思考状态
  isTyping.value = true

  try {
    // 模拟AI回复
    await simulateAIResponse(userMessage)
  } catch (error) {
    addMessage('抱歉，我现在无法处理您的请求。请稍后再试。', 'assistant')
  } finally {
    isTyping.value = false
  }
}

function addMessage(content: string, role: 'user' | 'assistant') {
  const message: ChatMessage = {
    id: Date.now().toString(),
    role,
    content,
    timestamp: new Date()
  }

  // 如果是AI助手消息，可能包含操作按钮
  if (role === 'assistant') {
    message.actions = generateMessageActions(content)
  }

  chatMessages.value.push(message)
  
  nextTick(() => {
    scrollToBottom()
  })

  // 如果最小化状态，更新未读计数
  if (isMinimized.value && role === 'assistant') {
    unreadCount.value++
  }
}

function generateMessageActions(content: string): ChatMessage['actions'] {
  const actions: ChatMessage['actions'] = []

  // 根据消息内容生成相关操作
  if (content.includes('卡组') || content.includes('优化')) {
    actions.push({
      id: 'optimize-deck',
      type: 'primary',
      label: '优化卡组',
      icon: 'fas fa-layer-group',
      action: () => {
        showMessage('正在优化卡组配置...', 'info')
      }
    })
  }

  if (content.includes('分析') || content.includes('数据')) {
    actions.push({
      id: 'show-analysis',
      type: 'secondary',
      label: '查看分析',
      icon: 'fas fa-chart-bar',
      action: () => {
        showAnalysis.value = !showAnalysis.value
      }
    })
  }

  return actions
}

async function simulateAIResponse(userMessage: string) {
  await new Promise(resolve => setTimeout(resolve, 1500))

  const responses = [
    '基于你的战斗数据，我建议增加一些治疗型角色来提高生存能力。',
    '你的卡牌使用率很高，但可以考虑增加一些控制类法术。',
    '从统计来看，你的平均战斗回合数较长，建议优化进攻策略。',
    '压力管理做得不错，继续保持良好的营地休息习惯。',
    '我注意到某些卡牌使用频率较低，可能需要调整卡组搭配。'
  ]

  const response = responses[Math.floor(Math.random() * responses.length)]
  addMessage(response, 'assistant')
}

function executeAction(action: any) {
  action.action()
}

function handleKeyPress(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function formatMessage(content: string): string {
  // 简单的markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

function formatTime(timestamp: Date): string {
  return timestamp.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

function showMessage(message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') {
  addMessage(message, 'assistant')
}

// 生命周期
onMounted(() => {
  // 初始化时可以加载历史消息
  if (messageInput.value) {
    messageInput.value.focus()
  }
})

watch(isExpanded, (newVal) => {
  if (newVal) {
    nextTick(() => {
      messageInput.value?.focus()
    })
  }
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 360px;
  background: var(--secondary-bg);
  border-radius: 12px;
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--border-color);
  z-index: 1000;
  transition: all 0.3s ease;
}

.ai-assistant.minimized {
  width: 60px;
  height: 60px;
}

.ai-assistant.expanded {
  height: 600px;
  max-height: 80vh;
}

.assistant-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--tertiary-bg);
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  user-select: none;
  position: relative;
}

.assistant-avatar {
  position: relative;
  width: 40px;
  height: 40px;
}

.avatar-glow {
  position: absolute;
  inset: -4px;
  background: radial-gradient(circle, var(--text-accent), transparent);
  border-radius: 50%;
  opacity: 0.3;
  animation: glow-pulse 2s infinite;
}

.assistant-avatar i {
  width: 100%;
  height: 100%;
  background: var(--text-accent);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  position: relative;
  z-index: 2;
}

.typing-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  display: flex;
  gap: 2px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: var(--success);
  border-radius: 50%;
  animation: typing-bounce 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.assistant-info {
  flex: 1;
  min-width: 0;
}

.assistant-info h3 {
  margin: 0 0 2px 0;
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
}

.assistant-status {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.assistant-controls {
  display: flex;
  gap: 4px;
}

.control-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 12px;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.assistant-content {
  padding: 16px;
  height: calc(100% - 64px);
  overflow-y: auto;
}

.ai-assistant.minimized .assistant-content {
  display: none;
}

.quick-suggestions {
  margin-bottom: 16px;
}

.quick-suggestions h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
}

.suggestions-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-card {
  background: var(--tertiary-bg);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.suggestion-card:hover {
  border-color: var(--text-accent);
  transform: translateY(-1px);
}

.suggestion-icon {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.suggestion-card.combat .suggestion-icon { color: var(--error); }
.suggestion-card.strategy .suggestion-icon { color: var(--info); }
.suggestion-card.equipment .suggestion-icon { color: var(--warning); }
.suggestion-card.stress .suggestion-icon { color: var(--epic); }

.suggestion-content {
  flex: 1;
  min-width: 0;
}

.suggestion-content h5 {
  margin: 0 0 2px 0;
  font-size: 13px;
  font-weight: bold;
  color: var(--text-primary);
}

.suggestion-content p {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.3;
}

.suggestion-priority {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
}

.suggestion-priority.high { background: var(--error); color: white; }
.suggestion-priority.medium { background: var(--warning); color: white; }
.suggestion-priority.low { background: var(--success); color: white; }

.chat-interface {
  height: calc(100% - 120px);
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
  padding-right: 4px;
}

.message {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.message.assistant .message-avatar {
  background: var(--text-accent);
  color: white;
}

.message.user .message-avatar {
  background: var(--info);
  color: white;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  background: var(--tertiary-bg);
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.4;
  color: var(--text-primary);
  word-wrap: break-word;
}

.message.user .message-text {
  background: var(--info);
  color: white;
}

.message-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
  text-align: right;
}

.message.user .message-time {
  text-align: left;
}

.message-actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.action-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--text-primary);
  padding: 4px 8px;
  border-radius: 15px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover {
  background: var(--text-accent);
  border-color: var(--text-accent);
  color: white;
}

.action-btn.primary {
  background: var(--text-accent);
  border-color: var(--text-accent);
  color: white;
}

.action-btn.secondary {
  background: var(--info);
  border-color: var(--info);
  color: white;
}

.chat-input {
  margin-top: auto;
}

.input-container {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--tertiary-bg);
  border-radius: 20px;
  padding: 4px;
}

.quick-action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 12px;
}

.quick-action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.quick-action-btn.analyze { color: var(--info); }
.quick-action-btn.suggest { color: var(--success); }
.quick-action-btn.help { color: var(--warning); }
.quick-action-btn.optimize { color: var(--epic); }

.input-container input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  padding: 8px 12px;
  font-size: 13px;
  outline: none;
}

.input-container input::placeholder {
  color: var(--text-muted);
}

.send-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--text-accent);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 12px;
}

.send-btn:hover:not(:disabled) {
  background: #d84315;
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.analysis-panel {
  margin-top: 16px;
  padding: 12px;
  background: var(--tertiary-bg);
  border-radius: 8px;
}

.analysis-panel h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.analysis-item {
  text-align: center;
  padding: 8px;
  background: var(--secondary-bg);
  border-radius: 6px;
}

.analysis-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.analysis-value {
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
}

.analysis-value.good { color: var(--success); }
.analysis-value.medium { color: var(--warning); }
.analysis-value.poor { color: var(--error); }

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--error);
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
  cursor: pointer;
  z-index: 10;
}

@keyframes glow-pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}

@media (max-width: 768px) {
  .ai-assistant {
    right: 10px;
    bottom: 10px;
    left: 10px;
    width: auto;
  }
  
  .ai-assistant.expanded {
    height: 70vh;
  }
  
  .suggestions-grid {
    gap: 6px;
  }
  
  .analysis-grid {
    grid-template-columns: 1fr;
  }
}
</style>