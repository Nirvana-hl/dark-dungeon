<template>
  <div class="home-container">
    <!-- 游戏标题 -->
    <div class="game-title">
      <h1>🎮 暗黑地牢肉鸽</h1>
    </div>
    
    <!-- 左侧功能区 -->
    <div class="left-sidebar">
      <RouterLink to="/camp" class="nav-button camp-button">
        <div class="button-icon">🏕️</div>
        <div class="button-text">
          <h3>营地</h3>
          <p>管理角色、装备和准备下次冒险</p>
        </div>
      </RouterLink>
      
      <RouterLink to="/skills" class="nav-button skills-button">
        <div class="button-icon">🌟</div>
        <div class="button-text">
          <h3>技能树</h3>
          <p>解锁和升级角色技能</p>
        </div>
      </RouterLink>
    </div>
    
    <!-- 右侧功能区 -->
    <div class="right-sidebar">
      <RouterLink to="/achievements" class="nav-button achievements-button">
        <div class="button-icon">🏆</div>
        <div class="button-text">
          <h3>成就</h3>
          <p>查看游戏成就和里程碑</p>
        </div>
      </RouterLink>
      
      <RouterLink to="/settings" class="nav-button settings-nav-button">
        <div class="button-icon">⚙️</div>
        <div class="button-text">
          <h3>设置</h3>
          <p>调整游戏设置和偏好</p>
        </div>
      </RouterLink>
    </div>
    
    <!-- 中央闯关按钮 -->
    <div class="center-action">
      <button class="explore-button" @click="handleStartExplore">
        <div class="button-content">
          <div class="explore-icon">⚡</div>
          <h2>开始闯关</h2>
          <p>挑战新的地牢关卡</p>
        </div>
      </button>
    </div>

    <!-- 背景故事弹窗 -->
    <StoryModal 
      :show="showStoryModal" 
      @close="handleStoryClose"
      @complete="handleStoryComplete"
    />

    <!-- 职业选择弹窗 -->
    <ClassSelectionModal 
      :show="showClassModal" 
      @close="handleClassClose"
      @complete="handleClassComplete"
    />
    
    <!-- 右上角登录/登出按钮 -->
    <div class="top-right-auth">
      <div v-if="auth.isAuthenticated" class="auth-info">
        <span class="user-name">👤 {{ auth.user?.accountName || '已登录' }}</span>
        <button class="auth-button logout-button" @click="handleLogout">
          <i class="fas fa-sign-out-alt"></i>
          登出
        </button>
      </div>
      <RouterLink v-else to="/login" class="auth-button login-button">
        <i class="fas fa-sign-in-alt"></i>
        登录
      </RouterLink>
    </div>
    
    <!-- 背景装饰 -->
    <div class="background-elements">
      <div class="floating-element element-1"></div>
      <div class="floating-element element-2"></div>
      <div class="floating-element element-3"></div>
      <div class="floating-element element-4"></div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background-image: url('/background.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  color: white;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-family: 'Arial', sans-serif;
}

/* 背景遮罩层，确保文字可读性 */
.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1;
}

/* 确保所有内容在遮罩层之上 */
.home-container > * {
  position: relative;
  z-index: 2;
}

/* 游戏标题 */
.game-title {
  position: absolute;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.game-title h1 {
  font-size: 3.5rem;
  font-weight: bold;
  background: linear-gradient(45deg, #ffd700, #ff6b6b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(255, 215, 0, 0.3);
  margin: 0;
  letter-spacing: 2px;
}

/* 左侧功能区 */
.left-sidebar {
  position: absolute;
  left: 60px;
  top: 45%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 25px;
  z-index: 10;
}

/* 右侧功能区 */
.right-sidebar {
  position: absolute;
  right: 60px;
  top: 45%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 25px;
  z-index: 10;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 22px 24px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  min-width: 280px;
  position: relative;
  overflow: hidden;
}

.nav-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, transparent, rgba(255, 255, 255, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-button:hover::before {
  opacity: 1;
}

.nav-button:hover {
  transform: translateX(10px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

.camp-button:hover {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.3), rgba(33, 150, 243, 0.1));
  border-color: #2196f3;
}

.skills-button:hover {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.3), rgba(76, 175, 80, 0.1));
  border-color: #4caf50;
}

.achievements-button:hover {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.3), rgba(255, 193, 7, 0.1));
  border-color: #ffc107;
}

.settings-nav-button:hover {
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.3), rgba(156, 39, 176, 0.1));
  border-color: #9c27b0;
}

.nav-button:hover {
  transform: translateX(0);
}

.left-sidebar .nav-button:hover {
  transform: translateX(10px);
}

.right-sidebar .nav-button:hover {
  transform: translateX(-10px);
}

.button-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.button-text h3 {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  font-weight: bold;
  color: white;
}

.button-text p {
  margin: 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.4;
}

/* 中央闯关按钮 */
.center-action {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.explore-button {
  display: block;
  text-decoration: none;
  color: white;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  animation: pulse 2s infinite, float 3s ease-in-out infinite;
}

.button-content {
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  padding: 32px 50px;
  border-radius: 25px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(255, 107, 107, 0.3);
  border: 2px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.button-content::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: shine 3s infinite;
}

.button-content:hover {
  transform: scale(1.05);
  box-shadow: 0 25px 50px rgba(255, 107, 107, 0.4);
}

.explore-icon {
  font-size: 3.5rem;
  margin-bottom: 12px;
  animation: rotate 4s linear infinite;
}

.button-content h2 {
  margin: 0 0 8px 0;
  font-size: 1.8rem;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.button-content p {
  margin: 0;
  font-size: 1rem;
  opacity: 0.95;
}


/* 背景装饰元素 */
.background-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  opacity: 0.3; /* 降低装饰元素的不透明度，让背景图更突出 */
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.element-1 {
  width: 80px;
  height: 80px;
  top: 15%;
  left: 10%;
  animation: float-up 6s ease-in-out infinite;
}

.element-2 {
  width: 60px;
  height: 60px;
  top: 25%;
  right: 15%;
  animation: float-down 7s ease-in-out infinite;
}

.element-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation: float-up 8s ease-in-out infinite;
}

.element-4 {
  width: 40px;
  height: 40px;
  bottom: 30%;
  right: 25%;
  animation: float-down 5s ease-in-out infinite;
}

/* 动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes float-up {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

@keyframes float-down {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(20px) rotate(-180deg); }
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  50% { transform: translateX(100%) translateY(100%) rotate(45deg); }
  100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

/* 右上角登录/登出按钮 */
.top-right-auth {
  position: absolute;
  top: 40px;
  right: 40px;
  z-index: 10;
}

.auth-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  font-weight: 500;
}

.auth-button {
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
  cursor: pointer;
  font-family: inherit;
}

.auth-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.login-button:hover {
  border-color: rgba(76, 175, 80, 0.5);
}

.logout-button:hover {
  border-color: rgba(244, 67, 54, 0.5);
}

.auth-button i {
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .game-title h1 {
    font-size: 2.5rem;
  }
  
  .left-sidebar {
    left: 30px;
  }
  
  .right-sidebar {
    right: 30px;
  }
  
  .nav-button {
    min-width: 240px;
    padding: 15px;
  }
  
  .button-icon {
    font-size: 2rem;
    width: 50px;
    height: 50px;
  }
  
  .button-content {
    padding: 30px 45px;
  }
  
  .explore-icon {
    font-size: 3rem;
  }
}

@media (max-width: 768px) {
  .game-title {
    top: 30px;
  }
  
  .game-title h1 {
    font-size: 2rem;
  }
  
  .left-sidebar {
    position: fixed;
    left: 10px;
    top: auto;
    bottom: 20px;
    transform: none;
    flex-direction: row;
    gap: 15px;
  }
  
  .right-sidebar {
    position: fixed;
    right: 10px;
    top: auto;
    bottom: 20px;
    transform: none;
    flex-direction: row;
    gap: 15px;
  }
  
  .nav-button {
    min-width: auto;
    padding: 12px 15px;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .button-text {
    display: none;
  }
  
  .center-action {
    bottom: 30px;
    transform: translateX(-50%);
  }
  
  .button-content {
    padding: 22px 30px;
  }
  
  .explore-icon {
    font-size: 2.2rem;
  }
  
  .button-content h2 {
    font-size: 1.3rem;
  }
  
  .button-content p {
    font-size: 0.9rem;
  }
  
  .top-right-auth {
    top: 20px;
    right: 20px;
  }
  
  .user-name {
    display: none; /* 移动端隐藏用户名 */
  }
  
  .auth-button {
    padding: 8px 16px;
    font-size: 0.8rem;
  }
}
</style>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCharactersStore } from '@/stores/characters'
import { useAuthStore } from '@/stores/auth'
import StoryModal from '@/components/StoryModal.vue'
import ClassSelectionModal from '@/components/ClassSelectionModal.vue'

const router = useRouter()
const charactersStore = useCharactersStore()
const auth = useAuthStore()

// 弹窗状态
const showStoryModal = ref(false)
const showClassModal = ref(false)

// 处理开始闯关
function handleStartExplore() {
  // 检查是否已有角色（包括模拟数据）
  const hasCharacter = charactersStore.playerCharacter || 
                       localStorage.getItem('mockPlayerCharacter')
  
  if (hasCharacter) {
    // 已有角色，直接跳转到探索页面
    router.push('/explore')
  } else {
    // 没有角色，先显示背景故事
    showStoryModal.value = true
  }
}

// 背景故事关闭
function handleStoryClose() {
  showStoryModal.value = false
}

// 背景故事完成
function handleStoryComplete() {
  showStoryModal.value = false
  // 显示职业选择
  showClassModal.value = true
}

// 职业选择关闭
function handleClassClose() {
  showClassModal.value = false
}

// 职业选择完成
async function handleClassComplete() {
  showClassModal.value = false
  // 跳转到探索页面
  router.push('/explore')
}

// 处理登出
async function handleLogout() {
  await auth.logout()
  // 登出后可以刷新页面或显示提示
  router.push('/login')
}
</script>