import axios, { AxiosInstance, AxiosResponse } from 'axios'

// API 配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 创建 axios 实例
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 添加 JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('[API] 请求已添加认证token:', {
        url: config.url,
        method: config.method?.toUpperCase(),
        tokenLength: token.length,
        tokenPreview: token.substring(0, 20) + '...'
      })
    } else {
      console.warn('[API] 请求未包含认证token:', {
        url: config.url,
        method: config.method?.toUpperCase()
      })
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理通用错误
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('[API] 响应成功:', response.config.url, response.status)
    return response
  },
  (error: any) => {
    const errorInfo = {
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
      status: error.response?.status,
      statusText: error.response?.statusText,
      message: error.response?.data?.message || error.message,
      data: error.response?.data,
      code: error.code
    }
    console.error('[API] 响应错误详情:', errorInfo)
    
    // 网络错误
    if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
      console.error('[API] 网络连接失败 - 后端服务可能未启动或无法访问')
      error.userMessage = '无法连接到服务器，请检查后端服务是否正常运行'
    }
    // 超时错误
    else if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      console.error('[API] 请求超时')
      error.userMessage = '请求超时，请稍后重试'
    }
    // 401 认证错误
    else if (error.response?.status === 401) {
      console.warn('[API] 认证失败，清除token并跳转登录页')
      error.userMessage = '登录已过期，请重新登录'
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 延迟跳转，让用户看到错误信息
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    }
    // 403 权限错误
    else if (error.response?.status === 403) {
      const token = localStorage.getItem('token')
      console.error('[API] 403 权限错误详情:', {
        url: error.config?.url,
        hasToken: !!token,
        tokenLength: token?.length,
        responseData: error.response?.data
      })
      error.userMessage = '没有权限访问此资源，请检查是否已登录或token是否有效'
    }
    // 404 未找到
    else if (error.response?.status === 404) {
      error.userMessage = `接口不存在: ${error.config?.url}`
    }
    // 500 服务器错误
    else if (error.response?.status >= 500) {
      error.userMessage = '服务器内部错误，请稍后重试'
    }
    // 400 错误
    else if (error.response?.status === 400) {
      const errorData = error.response?.data
      const errorMessage = errorData?.message || errorData?.error || '请求参数错误'
      console.warn('[API] 400 错误详情:', {
        url: error.config?.url,
        message: errorMessage,
        data: errorData
      })
      error.userMessage = errorMessage
    }
    // 其他错误
    else {
      error.userMessage = error.response?.data?.message || error.message || '请求失败'
    }
    
    return Promise.reject(error)
  }
)

// 通用 API 响应接口
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应接口
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 游戏相关 API 方法
export const gameApi = {
  // 获取角色特性
  async getCharacterTraits() {
    return await apiClient.get('/character/traits')
  },
  
  // 获取用户卡牌
  async getUserCards() {
    return await apiClient.get('/card/user-cards')
  },
  
  // 获取敌方卡牌
  async getEnemyCards(stageNum: number, difficulty: string) {
    return await apiClient.get(`/card/enemy-cards?stage=${stageNum}&difficulty=${difficulty}`)
  }
}

// 营地相关 API 方法
export const campApi = {
  // 获取营地数据（聚合接口）
  async getCampData() {
    return await apiClient.get('/camp/dashboard')
  },
  
  // 获取玩家角色信息
  async getPlayerCharacter() {
    return await apiClient.get('/camp/player-character')
  },
  
  // 获取可用的卡牌角色
  async getAvailableCardCharacters() {
    return await apiClient.get('/camp/card-characters')
  },
  
  // 部署/撤下卡牌角色
  async deployCardCharacter(userCardCharacterId: string, deploy: boolean) {
    return await apiClient.post('/camp/deploy-card-character', {
      userCardCharacterId,
      deploy
    })
  },
  
  // 获取背包物品
  async getInventory() {
    return await apiClient.get('/camp/inventory')
  },
  
  // 使用物品
  async useItem(inventoryId: string) {
    return await apiClient.post('/camp/use-item', { inventoryId })
  },
  
  // 获取商店商品
  async getShopOffers() {
    return await apiClient.get('/camp/shop-offers')
  },
  
  // 获取指定商店类型的商品（最多8个）
  async getShopOffersByType(shopType: 'item' | 'card' | 'card_character') {
    return await apiClient.get(`/camp/shop-offers/${shopType}`)
  },
  
  // 刷新指定商店类型的商品
  async refreshShop(shopType: 'item' | 'card' | 'card_character') {
    return await apiClient.post(`/camp/refresh-shop/${shopType}`)
  },
  
  // 购买商品
  async purchaseItem(offerId: string, quantity: number = 1) {
    console.log('[API] 发送购买请求:', {
      url: '/camp/purchase',
      offerId: offerId,
      shopOfferId: Number(offerId),
      quantity: quantity
    })
    // 后端期望的字段名是 shopOfferId
    const requestData = { 
      shopOfferId: Number(offerId), // 转换为数字类型
      quantity: quantity 
    }
    console.log('[API] 购买请求数据:', requestData)
    const response = await apiClient.post('/camp/purchase', requestData)
    console.log('[API] 购买响应:', {
      code: response.data.code,
      message: response.data.message,
      data: response.data.data
    })
    return response
  },
  
  // 获取任务事件
  async getEvents() {
    return await apiClient.get('/camp/events')
  },
  
  // 完成任务事件
  async completeEvent(eventId: string) {
    return await apiClient.post('/camp/complete-event', { eventId })
  },
  
  // 获取AI建议
  async getAISuggestions() {
    return await apiClient.get('/camp/ai-suggestions')
  },
  
  // 刷新AI建议
  async refreshAISuggestions() {
    return await apiClient.post('/camp/refresh-ai-suggestions')
  }
}

// 用户卡牌相关 API 方法
export const userCardApi = {
  // 获取卡组中的卡牌
  async getDeckCards(loadoutId?: number) {
    const url = loadoutId ? `/user-cards/deck?loadoutId=${loadoutId}` : '/user-cards/deck'
    return await apiClient.get(url)
  },
  
  // 更新用户卡牌
  async updateUserCard(cardId: number | string, data: { loadoutId?: number | null }) {
    return await apiClient.put(`/user-cards/${cardId}`, data)
  }
}

// 用户角色卡牌实例 API
export const userCardCharacterApi = {
  // 获取已部署的角色卡
  async getDeployedCardCharacters() {
    return await apiClient.get('/user-card-characters/deployed')
  },
  
  // 获取全部角色卡
  async getAllCardCharacters() {
    return await apiClient.get('/user-card-characters')
  }
}

// 技能相关 API 方法
export const skillApi = {
  // 获取职业技能树
  async getSkillTree(playerCharacterCode: string) {
    return await apiClient.get(`/skills/${playerCharacterCode}`)
  },
  
  // 获取已解锁技能
  async getUnlockedSkills() {
    return await apiClient.get('/user-skills')
  },
  
  // 解锁技能
  async unlockSkill(skillId: string) {
    return await apiClient.post('/user-skills/unlock', { skillId })
  }
}

// 压力系统相关 API
export const stressApi = {
  // 获取当前压力状态
  async getStressStatus() {
    return await apiClient.get('/stress/status')
  },
  
  // 获取压力debuff配置
  async getStressDebuffs() {
    return await apiClient.get('/stress/debuffs')
  },
  
  // 缓解压力（营地设施）
  async relieveStress(facilityType: 'tavern' | 'chapel' | 'sanctum') {
    return await apiClient.post('/stress/relieve', { facilityType })
  }
}

// 成就相关 API
export const achievementApi = {
  // 获取成就列表
  async getAchievements() {
    return await apiClient.get('/achievement/list')
  },
  
  // 获取成就进度
  async getAchievementProgress() {
    return await apiClient.get('/achievement/progress')
  },
  
  // 解锁成就
  async unlockAchievement(achievementId: string) {
    return await apiClient.post('/achievement/unlock', { achievementId })
  }
}

// 统计相关 API
export const statisticsApi = {
  // 获取核心指标
  async getCoreMetrics(period: string = '30days') {
    return await apiClient.get(`/statistics/core-metrics?period=${period}`)
  },
  
  // 获取战斗统计
  async getCombatStats(period: string = '30days') {
    return await apiClient.get(`/statistics/combat-stats?period=${period}`)
  },
  
  // 获取资源统计
  async getResourceStats(period: string = '30days') {
    return await apiClient.get(`/statistics/resource-stats?period=${period}`)
  },
  
  // 获取成就统计
  async getAchievementStats() {
    return await apiClient.get('/statistics/achievement-stats')
  },
  
  // 获取最近活动
  async getRecentActivity(limit: number = 10) {
    return await apiClient.get(`/statistics/recent-activity?limit=${limit}`)
  }
}

export default apiClient

// API 端点常量
export const API_ENDPOINTS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',
  },
  // 用户相关
  USER: {
    INFO: '/user/info',
    UPDATE: '/user/update',
  },
  // 角色相关
  CHARACTER: {
    PLAYER: '/character/player',
    PLAYER_INSTANCE: '/character/player/instance',
    CARD: '/character/card',
    CARD_INSTANCE: '/character/card/instance',
    CARD_TRAITS: '/character/card/traits',
  },
  // 技能相关
  SKILL: {
    LIST: '/skill/list',
    UNLOCK: '/skill/unlock',
    USER_SKILLS: '/skill/user-skills',
  },
  // 钱包相关
  WALLET: {
    INFO: '/wallet/info',
    ADD: '/wallet/add',
    CONSUME: '/wallet/consume',
  },
  // 卡牌相关
  CARD: {
    LIST: '/card/list',
    USER_CARDS: '/card/user-cards',
    EQUIP: '/card/equip',
    UNEQUIP: '/card/unequip',
    UPGRADE: '/card/upgrade',
  },
  // 道具相关
  ITEM: {
    LIST: '/item/list',
    INVENTORY: '/item/inventory',
    USE: '/item/use',
  },
  // 商城相关
  SHOP: {
    OFFERS: '/shop/offers',
    BUY: '/shop/buy',
  },
  // 地牢相关
  DUNGEON: {
    LIST: '/dungeon/list',
    START: '/dungeon/start',
    END: '/dungeon/end',
  },
  // 游戏相关
  GAME: {
    STATUS: '/game/status',
    SAVE: '/game/save',
    LOAD: '/game/load',
  },
  // 成就相关
  ACHIEVEMENT: {
    LIST: '/achievement/list',
    PROGRESS: '/achievement/progress',
    UNLOCK: '/achievement/unlock',
  },
  // 统计相关
  STATISTICS: {
    CORE_METRICS: '/statistics/core-metrics',
    COMBAT_STATS: '/statistics/combat-stats',
    RESOURCE_STATS: '/statistics/resource-stats',
    ACHIEVEMENT_STATS: '/statistics/achievement-stats',
    RECENT_ACTIVITY: '/statistics/recent-activity',
  },
} as const

export type ApiEndpointKeys = keyof typeof API_ENDPOINTS
