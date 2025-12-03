<template>
  <Teleport to="body">
    <Transition name="story-fade">
      <div v-if="show" class="story-overlay" @click.self="handleSkip">
        <div class="story-container">
          <!-- 背景装饰 -->
          <div class="story-background">
            <div class="flame flame-1"></div>
            <div class="flame flame-2"></div>
            <div class="flame flame-3"></div>
          </div>

          <!-- 故事内容 -->
          <div class="story-content">
            <div class="story-title">暗黑地牢</div>
            
            <div class="story-text-container">
              <div 
                v-for="(paragraph, index) in storyParagraphs" 
                :key="index"
                class="story-paragraph"
                :class="{ 'visible': currentParagraph >= index }"
              >
                <p>{{ paragraph }}</p>
              </div>
            </div>

            <!-- 进度指示器 -->
            <div class="story-progress">
              <div 
                v-for="(_, index) in storyParagraphs" 
                :key="index"
                class="progress-dot"
                :class="{ 'active': currentParagraph >= index }"
              ></div>
            </div>

            <!-- 操作按钮 -->
            <div class="story-actions">
              <button 
                v-if="currentParagraph < storyParagraphs.length - 1"
                class="story-button story-button-next"
                @click="nextParagraph"
              >
                继续
              </button>
              <button 
                v-else
                class="story-button story-button-start"
                @click="handleStart"
              >
                开始冒险
              </button>
              <button 
                class="story-button story-button-skip"
                @click="handleSkip"
              >
                跳过
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'

interface Props {
  show: boolean
}

const props = defineProps<Props>()

interface Emits {
  (e: 'close'): void
  (e: 'complete'): void
}

const emit = defineEmits<Emits>()

// 背景故事内容
const storyParagraphs = [
  '在遥远的过去，一座古老的地牢深埋于地下。传说中，这里曾是古代文明的遗迹，埋藏着无尽的宝藏与秘密。',
  '然而，随着时光流逝，黑暗的力量逐渐侵蚀了这片土地。邪恶的诅咒笼罩着地牢的每一个角落，将死者的灵魂囚禁于此。',
  '无数勇敢的冒险者曾踏入此地，但很少有人能够活着回来。那些幸存者带回了令人恐惧的传说：扭曲的怪物、诡异的陷阱、以及那永不消散的黑暗。',
  '如今，你作为一名新的冒险者，决定踏入这座暗黑地牢。你的目标很明确：深入地下，揭开隐藏的秘密，并活着离开。',
  '但记住，在这片被诅咒的土地上，每一步都可能是你的最后一步。压力会逐渐侵蚀你的意志，而死亡随时可能降临。',
  '选择你的职业，准备好你的装备，然后踏入这无尽的黑暗。你的命运，将由你自己书写...'
]

const currentParagraph = ref(0)
let autoPlayTimer: any = null

// 监听显示状态
watch(() => props.show, (show) => {
  if (show) {
    currentParagraph.value = 0
    startAutoPlay()
    document.body.style.overflow = 'hidden'
  } else {
    stopAutoPlay()
    document.body.style.overflow = ''
  }
})

// 自动播放
function startAutoPlay() {
  stopAutoPlay()
  autoPlayTimer = setInterval(() => {
    if (currentParagraph.value < storyParagraphs.length - 1) {
      currentParagraph.value++
    } else {
      stopAutoPlay()
    }
  }, 4000) // 每4秒切换一段
}

function stopAutoPlay() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

function nextParagraph() {
  if (currentParagraph.value < storyParagraphs.length - 1) {
    currentParagraph.value++
    stopAutoPlay()
    startAutoPlay()
  }
}

function handleStart() {
  emit('complete')
}

function handleSkip() {
  emit('close')
}

onMounted(() => {
  if (props.show) {
    startAutoPlay()
  }
})

onUnmounted(() => {
  stopAutoPlay()
  document.body.style.overflow = ''
})
</script>

<style scoped>
.story-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.story-container {
  position: relative;
  width: 100%;
  max-width: 900px;
  min-height: 600px;
  background: rgba(0, 0, 0, 0.7);
  border: 2px solid rgba(139, 69, 19, 0.5);
  border-radius: 20px;
  box-shadow: 
    0 0 50px rgba(139, 69, 19, 0.3),
    inset 0 0 100px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.story-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  opacity: 0.3;
}

.flame {
  position: absolute;
  width: 100px;
  height: 150px;
  background: radial-gradient(ellipse at center, 
    rgba(255, 100, 0, 0.8) 0%,
    rgba(255, 150, 0, 0.4) 30%,
    transparent 70%);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: flicker 2s ease-in-out infinite;
}

.flame-1 {
  bottom: 20px;
  left: 10%;
  animation-delay: 0s;
}

.flame-2 {
  bottom: 20px;
  right: 10%;
  animation-delay: 0.7s;
}

.flame-3 {
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 1.4s;
}

@keyframes flicker {
  0%, 100% {
    transform: scale(1) translateY(0);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.1) translateY(-10px);
    opacity: 0.5;
  }
}

.story-content {
  position: relative;
  z-index: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 600px;
}

.story-title {
  font-size: 3.5rem;
  font-weight: bold;
  color: #d4af37;
  text-shadow: 
    0 0 10px rgba(212, 175, 55, 0.5),
    0 0 20px rgba(212, 175, 55, 0.3),
    0 0 30px rgba(212, 175, 55, 0.2);
  margin-bottom: 40px;
  letter-spacing: 4px;
  animation: title-glow 3s ease-in-out infinite;
}

@keyframes title-glow {
  0%, 100% {
    text-shadow: 
      0 0 10px rgba(212, 175, 55, 0.5),
      0 0 20px rgba(212, 175, 55, 0.3),
      0 0 30px rgba(212, 175, 55, 0.2);
  }
  50% {
    text-shadow: 
      0 0 20px rgba(212, 175, 55, 0.8),
      0 0 30px rgba(212, 175, 55, 0.5),
      0 0 40px rgba(212, 175, 55, 0.3);
  }
}

.story-text-container {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 30px;
  margin-bottom: 40px;
}

.story-paragraph {
  font-size: 1.3rem;
  line-height: 2;
  color: #e0e0e0;
  text-align: center;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s ease;
  max-width: 800px;
  margin: 0 auto;
}

.story-paragraph.visible {
  opacity: 1;
  transform: translateY(0);
}

.story-paragraph p {
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.story-progress {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
}

.progress-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(139, 69, 19, 0.3);
  border: 2px solid rgba(139, 69, 19, 0.5);
  transition: all 0.3s ease;
}

.progress-dot.active {
  background: #d4af37;
  border-color: #d4af37;
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.6);
}

.story-actions {
  display: flex;
  gap: 20px;
}

.story-button {
  padding: 14px 32px;
  font-size: 1.1rem;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.story-button-next {
  background: linear-gradient(135deg, #8b4513, #a0522d);
  color: #fff;
  box-shadow: 0 4px 15px rgba(139, 69, 19, 0.4);
}

.story-button-next:hover {
  background: linear-gradient(135deg, #a0522d, #cd853f);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 69, 19, 0.6);
}

.story-button-start {
  background: linear-gradient(135deg, #d4af37, #ffd700);
  color: #1a1a2e;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
}

.story-button-start:hover {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.6);
}

.story-button-skip {
  background: rgba(139, 69, 19, 0.2);
  color: #999;
  border: 1px solid rgba(139, 69, 19, 0.5);
}

.story-button-skip:hover {
  background: rgba(139, 69, 19, 0.3);
  color: #ccc;
}

/* 过渡动画 */
.story-fade-enter-active,
.story-fade-leave-active {
  transition: opacity 0.5s ease;
}

.story-fade-enter-from,
.story-fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .story-container {
    min-height: 500px;
  }

  .story-content {
    padding: 40px 30px;
    min-height: 500px;
  }

  .story-title {
    font-size: 2.5rem;
    margin-bottom: 30px;
  }

  .story-paragraph {
    font-size: 1.1rem;
    line-height: 1.8;
  }

  .story-button {
    padding: 12px 24px;
    font-size: 1rem;
  }

  .story-actions {
    flex-direction: column;
    width: 100%;
  }

  .story-button {
    width: 100%;
  }
}
</style>

