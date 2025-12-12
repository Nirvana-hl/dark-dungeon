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
    
    <!-- 右侧功能区（成就和设置） -->
    <div class="right-sidebar">
      <RouterLink to="/achievements" class="nav-button achievements-button">
        <div class="button-icon">🏆</div>
        <div class="button-text">
          <h3>成就</h3>
          <p>查看已解锁的成就</p>
        </div>
      </RouterLink>
      
      <RouterLink to="/settings" class="nav-button settings-button">
        <div class="button-icon">⚙️</div>
        <div class="button-text">
          <h3>设置</h3>
          <p>游戏设置和选项</p>
        </div>
      </RouterLink>
    </div>
    
    <!-- 底部中央闯关按钮 -->
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
      @skip="handleStorySkip"
    />

    <!-- 职业选择弹窗 -->
    <ClassSelectionModal 
      :show="showClassModal" 
      @close="handleClassClose"
      @complete="handleClassComplete"
    />
    
    <!-- 右上角登录/登出按钮 -->
    <div class="top-right-auth">
      <button 
        v-if="!auth.isAuthenticated" 
        class="auth-button login-button"
        @click="goLogin"
      >
        <i class="fas fa-sign-in-alt"></i>
        登录
      </button>
      <div v-else class="auth-info">
        <span class="account-name">{{ auth.user?.accountName || '用户' }}</span>
        <button 
          class="auth-button logout-button"
          @click="signOut"
        >
          <i class="fas fa-sign-out-alt"></i>
          登出
        </button>
      </div>
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
  z-index: 0;
}

/* 游戏标题 */
.game-title {
  position: absolute;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  pointer-events: none;
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
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 30px;
  z-index: 10;
}

/* 右侧功能区（成就和设置） */
.right-sidebar {
  position: absolute;
  right: 60px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 30px;
  z-index: 10;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
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
  transform: translateX(-10px);
}

.settings-button:hover {
  background: linear-gradient(135deg, rgba(158, 158, 158, 0.3), rgba(158, 158, 158, 0.1));
  border-color: #9e9e9e;
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

/* 底部中央闯关按钮 */
.center-action {
  position: absolute;
  bottom: 80px;
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
  background: linear-gradient(135deg, #4a1a1a, #2d1b1b);
  padding: 24px 40px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(139, 69, 19, 0.6);
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
  background: linear-gradient(45deg, transparent, rgba(139, 69, 19, 0.2), transparent);
  animation: shine 4s infinite;
}

.button-content:hover {
  transform: scale(1.03);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.15);
  border-color: rgba(160, 82, 45, 0.8);
  background: linear-gradient(135deg, #5a2a2a, #3d2b2b);
}

.explore-icon {
  font-size: 2.5rem;
  margin-bottom: 10px;
  animation: rotate 6s linear infinite;
  opacity: 0.8;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

.button-content h2 {
  margin: 0 0 8px 0;
  font-size: 1.5rem;
  font-weight: bold;
  color: #d4a574;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8), 0 0 4px rgba(139, 69, 19, 0.5);
}

.button-content p {
  margin: 0;
  font-size: 0.9rem;
  color: #b8a082;
  opacity: 0.85;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
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

.account-name {
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.auth-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  cursor: pointer;
  font-family: inherit;
}

.auth-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.login-button:hover {
  border-color: #4caf50;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.3), rgba(76, 175, 80, 0.1));
}

.logout-button:hover {
  border-color: #ef4444;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(239, 68, 68, 0.1));
}

.auth-button i {
  font-size: 1rem;
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
    padding: 20px 35px;
  }
  
  .button-content h2 {
    font-size: 1.3rem;
  }
  
  .button-content p {
    font-size: 0.85rem;
  }
  
  .explore-icon {
    font-size: 2rem;
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
    bottom: 60px;
  }
  
  .button-content {
    padding: 18px 28px;
  }
  
  .button-content h2 {
    font-size: 1.2rem;
  }
  
  .button-content p {
    font-size: 0.8rem;
  }
  
  .explore-icon {
    font-size: 1.8rem;
    margin-bottom: 8px;
  }
  
  .top-right-auth {
    top: 20px;
    right: 20px;
  }
  
  .auth-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-end;
  }
  
  .account-name {
    padding: 8px 16px;
    font-size: 0.8rem;
  }
  
  .auth-button {
    padding: 10px 16px;
    font-size: 0.85rem;
  }
}
</style>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCharactersStore } from '@/stores/characters'
import { useAuthStore } from '@/stores/auth'
import { useCampStore } from '@/stores/camp'
import { userCardCharacterApi } from '@/lib/api'
import StoryModal from '@/components/StoryModal.vue'
import ClassSelectionModal from '@/components/ClassSelectionModal.vue'

const router = useRouter()
const charactersStore = useCharactersStore()
const auth = useAuthStore()
const campStore = useCampStore()

// 弹窗状态
const showStoryModal = ref(false)
const showClassModal = ref(false)

// 获取背景故事已显示标记的key
function getStoryShownKey(): string | null {
  const userId = auth.user?.id
  if (!userId) return null
  return `storyShown_${userId}`
}

// 检查背景故事是否已经显示过
function hasStoryBeenShown(): boolean {
  const key = getStoryShownKey()
  if (!key) return false
  const shown = localStorage.getItem(key)
  return shown === 'true'
}

// 标记背景故事已显示
function markStoryAsShown(): void {
  const key = getStoryShownKey()
  if (key) {
    localStorage.setItem(key, 'true')
    console.log('[Home] 已标记背景故事为已显示，用户ID:', auth.user?.id)
  }
}

// 处理开始闯关
async function handleStartExplore() {
  try {
    // 首先强制刷新营地数据，确保数据是最新的
    console.log('[Home] 开始检查角色状态，先刷新营地数据...')
    try {
      await campStore.fetchCampData()
      console.log('[Home] 营地数据刷新完成')
    } catch (error) {
      console.warn('[Home] 加载营地数据失败:', error)
    }
    
    // 检查是否在营地选择了角色（已上阵的角色）
    // 使用更宽松的检查条件：isDeployed 为 true、1 或非空字符串都视为已上阵
    const checkIsDeployed = (char: any): boolean => {
      if (char.isDeployed === true || char.isDeployed === 1 || char.isDeployed === 'true') {
        return true
      }
      // 也检查 Boolean 转换
      return Boolean(char.isDeployed) === true
    }
    
    // 方法1：从campStore中检查是否有已上阵的角色
    let hasDeployedCharacter = false
    const userCardCharacters = campStore.userCardCharacters || []
    
    console.log('[Home] 检查角色数据:', {
      totalCharacters: userCardCharacters.length,
      characters: userCardCharacters.map((char: any) => ({
        id: char.id,
        name: char.characterName || char.name,
        isDeployed: char.isDeployed,
        isDeployedType: typeof char.isDeployed
      }))
    })
    
    if (userCardCharacters.length > 0) {
      // 如果store中有数据，直接检查
      hasDeployedCharacter = userCardCharacters.some(checkIsDeployed)
      const deployedChars = userCardCharacters.filter(checkIsDeployed)
      console.log('[Home] 从store检查上阵角色:', {
        totalCharacters: userCardCharacters.length,
        hasDeployed: hasDeployedCharacter,
        deployedCount: deployedChars.length,
        deployedChars: deployedChars.map((char: any) => ({
          id: char.id,
          name: char.characterName || char.name,
          isDeployed: char.isDeployed
        }))
      })
    }
    
    // 方法2：如果store中没有已上阵角色，调用API再次确认
    if (!hasDeployedCharacter) {
      try {
        console.log('[Home] store中未找到上阵角色，调用API确认...')
        const response = await userCardCharacterApi.getDeployedCardCharacters()
        if (response.data?.code === 200) {
          const deployedChars = response.data.data || []
          hasDeployedCharacter = Array.isArray(deployedChars) && deployedChars.length > 0
          console.log('[Home] API返回已上阵角色数量:', deployedChars.length, {
            chars: deployedChars.map((char: any) => ({
              id: char.id,
              name: char.characterName || char.name,
              isDeployed: char.isDeployed
            }))
          })
        }
      } catch (error) {
        console.warn('[Home] 获取已上阵角色失败:', error)
        // API调用失败时，如果store中有角色数据，使用store的判断结果
        // 如果store中也没有数据，则认为没有上阵角色
      }
    }
    
    if (hasDeployedCharacter) {
      // 已有上阵角色，营地已选择完毕，直接跳转到探索页面
      console.log('[Home] ✅ 检测到已上阵角色，直接跳转到探索页面')
      router.push('/explore')
    } else {
      // 没有上阵角色，检查是否已经显示过背景故事
      const storyShown = hasStoryBeenShown()
      if (storyShown) {
        // 已经显示过背景故事，直接跳转到探索页面
        console.log('[Home] 背景故事已显示过，直接跳转到探索页面')
        router.push('/explore')
      } else {
        // 没有显示过背景故事，显示背景故事
        console.log('[Home] ❌ 未检测到已上阵角色且未显示过背景故事，显示背景故事')
        showStoryModal.value = true
      }
    }
  } catch (error) {
    console.error('[Home] 检查角色状态失败:', error)
    // 出错时，如果没有主角，也显示背景故事
    const hasPlayerCharacter = charactersStore.playerCharacter || 
                               localStorage.getItem('mockPlayerCharacter')
    if (hasPlayerCharacter) {
      router.push('/explore')
    } else {
      // 检查是否已经显示过背景故事
      const storyShown = hasStoryBeenShown()
      if (storyShown) {
        router.push('/explore')
      } else {
        showStoryModal.value = true
      }
    }
  }
}

// 背景故事关闭
function handleStoryClose() {
  showStoryModal.value = false
  // 标记背景故事已显示（即使关闭也算看过）
  markStoryAsShown()
}

// 背景故事跳过 - 直接跳转到探索界面
function handleStorySkip() {
  showStoryModal.value = false
  // 标记背景故事已显示
  markStoryAsShown()
  // 直接跳转到探索页面
  router.push('/explore')
}

// 背景故事完成
function handleStoryComplete() {
  showStoryModal.value = false
  // 标记背景故事已显示
  markStoryAsShown()
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

// 跳转到登录页
function goLogin() {
  router.push('/login')
}

// 登出
async function signOut() {
  await auth.logout()
  // 登出后跳转到登录页
  router.push('/login')
}
</script>