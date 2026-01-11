// uni-app API 请求封装

// uni-app 类型声明（如果未安装 @dcloudio/types，可以使用此声明）
declare const uni: {
  request: (options: {
    url: string
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
    data?: any
    header?: Record<string, string>
    timeout?: number
    success?: (res: { data: any; statusCode: number; header: Record<string, string>; cookies?: string[] }) => void
    fail?: (err: { errMsg: string; statusCode?: number; data?: any }) => void
  }) => void
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
  reLaunch: (options: { url: string }) => void
}

// 微信小程序本地调试：不要用 localhost，改用 127.0.0.1（或你后端所在局域网 IP）
// 提供可切换的地址，便于本机调试（127）和局域网联调（LAN_IP）
const LAN_BASE = 'http://26.83.153.194:8080'        // 你的局域网 IP（队友用这个）

// 允许在本地存储里手动覆盖（如在调试面板执行 uni.setStorageSync('apiBaseUrl', LAN_BASE)）
const storedBase = (() => {
  try {
    return uni.getStorageSync('apiBaseUrl')
  } catch {
    return ''
  }
})()

// 默认优先使用存储值，其次用 127，本机/微信开发者工具最稳；队友改成 LAN_BASE 即可
const API_BASE_URL = storedBase || LAN_BASE

// 请求配置接口
interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: any
  params?: Record<string, any>
  header?: Record<string, string>
  timeout?: number
}

// 响应接口（模拟 axios 响应格式）
export interface UniResponse<T = any> {
  data: T
  statusCode: number
  header: Record<string, string>
  cookies?: string[]
  config?: RequestConfig
}

// 错误接口
interface UniError {
  errMsg: string
  statusCode?: number
  data?: any
  userMessage?: string
  config?: RequestConfig
}

// 构建完整 URL
function buildUrl(url: string, params?: Record<string, any>): string {
  const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`
  
  if (params && Object.keys(params).length > 0) {
    const queryString = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    return `${fullUrl}${fullUrl.includes('?') ? '&' : '?'}${queryString}`
  }
  
  return fullUrl
}

// 获取存储的 token
function getToken(): string | null {
  try {
    return uni.getStorageSync('token') || null
  } catch (error) {
    console.error('[API] 获取 token 失败:', error)
    return null
  }
}

// 设置存储的 token
function setToken(token: string): void {
  try {
    uni.setStorageSync('token', token)
  } catch (error) {
    console.error('[API] 设置 token 失败:', error)
  }
}

// 移除存储的 token
function removeToken(): void {
  try {
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
  } catch (error) {
    console.error('[API] 移除 token 失败:', error)
  }
}

// 请求拦截器 - 添加 JWT token
function requestInterceptor(config: RequestConfig): RequestConfig {
  const token = getToken()
  
  // 设置默认请求头
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...config.header
  }

  // 添加认证 token
    if (token) {
    headers.Authorization = `Bearer ${token}`
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
  
  return {
    ...config,
    header: headers
  }
}

// 响应拦截器 - 处理通用错误
function responseInterceptor<T>(response: UniResponse<T>): UniResponse<T> {
  // 检查HTTP状态码，如果是错误状态码（>=400），应该reject
  if (response.statusCode >= 400) {
    // 构造错误对象，让错误拦截器处理
    const error: UniError = {
      errMsg: `HTTP ${response.statusCode}`,
      statusCode: response.statusCode,
      data: response.data,
      config: response.config
    }
    // 抛出错误，让错误拦截器处理
    throw error
  }
  
  console.log('[API] 响应成功:', response.config?.url, response.statusCode)
  return response
}

// 错误拦截器
function errorInterceptor(error: UniError): Promise<never> {
    const errorInfo = {
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
    status: error.statusCode,
    message: error.errMsg,
    data: error.data
    }
    console.error('[API] 响应错误详情:', errorInfo)
    
    // 网络错误
  if (error.errMsg?.includes('fail') || error.errMsg?.includes('timeout')) {
      console.error('[API] 网络连接失败 - 后端服务可能未启动或无法访问')
      error.userMessage = '无法连接到服务器，请检查后端服务是否正常运行'
    }
    // 超时错误
  else if (error.errMsg?.includes('timeout')) {
      console.error('[API] 请求超时')
      error.userMessage = '请求超时，请稍后重试'
    }
    // 401 认证错误
  else if (error.statusCode === 401) {
      console.warn('[API] 认证失败，清除token并跳转登录页')
      error.userMessage = '登录已过期，请重新登录'
    removeToken()
      // 延迟跳转，让用户看到错误信息
      setTimeout(() => {
      uni.reLaunch({
        url: '/pages/login/login'
      })
      }, 2000)
    }
    // 403 权限错误
  else if (error.statusCode === 403) {
    const token = getToken()
      console.error('[API] 403 权限错误详情:', {
        url: error.config?.url,
        hasToken: !!token,
        tokenLength: token?.length,
      responseData: error.data
      })
      error.userMessage = '没有权限访问此资源，请检查是否已登录或token是否有效'
      // 403错误通常表示token无效或过期，清除token
      // 注意：不在这里自动跳转，让调用者决定如何处理
      removeToken()
    }
    // 404 未找到
  else if (error.statusCode === 404) {
      error.userMessage = `接口不存在: ${error.config?.url}`
    }
    // 500 服务器错误
  else if (error.statusCode && error.statusCode >= 500) {
      error.userMessage = '服务器内部错误，请稍后重试'
    }
    // 400 错误
  else if (error.statusCode === 400) {
    const errorData = error.data
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
    error.userMessage = error.data?.message || error.errMsg || '请求失败'
    }
    
    return Promise.reject(error)
  }

// 核心请求函数
function request<T = any>(config: RequestConfig): Promise<UniResponse<T>> {
  return new Promise((resolve, reject) => {
    // 应用请求拦截器
    const finalConfig = requestInterceptor(config)
    
    // 构建完整 URL
    const url = buildUrl(finalConfig.url, finalConfig.params)
    
    // 发送请求
    try {
      // 记录完整请求 URL 与 headers，便于网络层排查
      // eslint-disable-next-line no-console
      console.debug('[API] Sending request', { url, method: finalConfig.method || 'GET', header: finalConfig.header })
    } catch (e) {}
    uni.request({
      url,
      method: finalConfig.method || 'GET',
      data: finalConfig.data,
      header: finalConfig.header || {},
      timeout: finalConfig.timeout || 30000,
      success: (res) => {
        // 构造响应对象（模拟 axios 格式）
        const response: UniResponse<T> = {
          data: res.data as T,
          statusCode: res.statusCode,
          header: res.header,
          cookies: res.cookies,
          config: finalConfig
        }
        
        // 应用响应拦截器
        try {
          const processedResponse = responseInterceptor(response)
          resolve(processedResponse)
        } catch (error: any) {
          // 如果响应拦截器抛出错误，通过错误拦截器处理
          if (error.statusCode) {
            // 这是一个HTTP错误，通过错误拦截器处理
            errorInterceptor(error).catch(reject)
          } else {
            // 其他类型的错误直接reject
            reject(error)
          }
        }
      },
      fail: (err) => {
        try {
          // 额外打印完整错误以便调试网络/超时问题
          // eslint-disable-next-line no-console
          console.error('[API] uni.request fail:', { url, config: finalConfig, err })
        } catch (e) {}
        // 构造错误对象
        const error: UniError = {
          errMsg: err.errMsg || '请求失败',
          statusCode: err.statusCode,
          data: err.data,
          config: finalConfig
        }
        
        // 应用错误拦截器
        errorInterceptor(error).catch(reject)
      }
    })
  })
}

// 封装常用请求方法（模拟 axios 接口）
const apiClient = {
  get<T = any>(url: string, config?: { params?: Record<string, any>; header?: Record<string, string> }) {
    return request<T>({
      url,
      method: 'GET',
      params: config?.params,
      header: config?.header
    })
  },
  
  post<T = any>(url: string, data?: any, config?: { header?: Record<string, string> }) {
    return request<T>({
      url,
      method: 'POST',
      data,
      header: config?.header
    })
  },
  
  put<T = any>(url: string, data?: any, config?: { header?: Record<string, string> }) {
    return request<T>({
      url,
      method: 'PUT',
      data,
      header: config?.header
    })
  },
  
  delete<T = any>(url: string, config?: { header?: Record<string, string> }) {
    return request<T>({
      url,
      method: 'DELETE',
      header: config?.header
    })
  },
  
  patch<T = any>(url: string, data?: any, config?: { header?: Record<string, string> }) {
    return request<T>({
      url,
      method: 'PATCH',
      data,
      header: config?.header
    })
  }
}

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
  
  // 获取卡牌角色特性
  async getCardCharacterTraits(cardCharacterId: number | string) {
    return await apiClient.get(`/card-characters/${cardCharacterId}/traits`)
  },
  
  // 获取用户卡牌
  async getUserCards() {
    return await apiClient.get('/card/user-cards')
  },
  
  // 获取敌方卡牌（从后端获取敌人的卡牌列表，包括法术、装备和角色卡）
  // 支持两种方式：1. 通过 enemyId 查询 2. 通过 stage 和 difficulty 查询
  async getEnemyCards(stageNumOrEnemyId: number, difficulty?: string) {
    if (difficulty) {
      // 通过关卡和难度查询
      return await apiClient.get(`/game/enemy-cards?stage=${stageNumOrEnemyId}&difficulty=${difficulty}`)
    } else {
      // 通过敌人ID查询
      return await apiClient.get(`/game/enemy-cards?enemyId=${stageNumOrEnemyId}`)
    }
  },
  
  // 获取关卡中所有可能的敌人列表
  async getStageEnemies(stageNum?: number, difficulty?: string) {
    const params: any = {}
    if (stageNum) params.stage = stageNum
    if (difficulty) params.difficulty = difficulty
    return await apiClient.get('/game/stage-enemies', { params })
  },
  
  // 获取敌人面板信息
  async getEnemyPanel(enemyId: number) {
    return await apiClient.get(`/game/enemy-panel?enemyId=${enemyId}`)
  },
}

// 商城相关 API 方法
export const shopApi = {
  // 获取所有商城商品
  async getAllOffers() {
    return await apiClient.get('/shop/offers')
  },

  // 按商店类型获取商品（角色 / 法术 / 装备 / 道具）
  async getOffersByType(shopType: 'item' | 'card_character' | 'spell' | 'equipment') {
    return await apiClient.get(`/shop/offers/${shopType}`)
  },

  // 刷新指定商店类型的商品（随机打乱顺序）
  async refreshShop(shopType: 'item' | 'card_character' | 'spell' | 'equipment') {
    return await apiClient.post(`/shop/refresh/${shopType}`)
  },

  // 购买商品（新版本：直接使用 offerType 和 targetId）
  async purchaseItem(payload: { offerType: 'item' | 'card' | 'card_character'; targetId: number; quantity?: number }) {
    const requestData = {
      offerType: payload.offerType,
      targetId: payload.targetId,
      quantity: payload.quantity ?? 1,
    }
    console.log('[API] 商城购买请求:', requestData)
    return await apiClient.post('/shop/purchase', requestData)
  },
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
    return await apiClient.get('/user-card-characters')
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
  },
  
  // 升星角色卡（消耗3个卡牌，星级+1，并应用面板提升）
  // 使用专门的升星接口，会自动应用 starUpgradePayload 中的属性增幅
  async upgradeStarLevel(userCardCharacterId: string | number) {
    return await apiClient.post(`/user-card-characters/${userCardCharacterId}/upgrade-star`)
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
  },

  // 获取战斗可用技能（包含被动/主动筛选）
  async getBattleSkills(playerCharacterCode: string) {
    return await apiClient.get(`/user-skills/battle/${playerCharacterCode}`)
  },

  // 获取战斗可用主动技能（只返回 active）
  async getActiveBattleSkills(playerCharacterCode: string) {
    return await apiClient.get(`/user-skills/battle/${playerCharacterCode}/active`)
  },

  // 验证并获取技能使用信息（后端会返回 manaCost 和 effectPayload 等）
  async useSkill(skillId: string | number, payload?: { currentMana?: number }) {
    return await apiClient.post(`/user-skills/${skillId}/use`, payload || {})
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
    return await apiClient.get('/achievements/list')
  },
  
  // 获取成就进度
  async getAchievementProgress() {
    return await apiClient.get('/achievements/progress')
  },
  
  // 解锁成就
  async unlockAchievement(achievementId: string) {
    return await apiClient.post('/achievements/unlock', { achievementId })
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
    LIST: '/achievements/list',
    PROGRESS: '/achievements/progress',
    UNLOCK: '/achievements/unlock',
  },
  // 统计相关
  STATISTICS: {
    CORE_METRICS: '/statistics/core-metrics',
    COMBAT_STATS: '/statistics/combat-stats',
    RESOURCE_STATS: '/statistics/resource-stats',
    ACHIEVEMENT_STATS: '/statistics/achievement-stats',
    RECENT_ACTIVITY: '/statistics/recent-activity',
  },
  // 关卡进度相关
  STAGE_PROGRESS: {
    LIST: '/user-stage-progress',
    GET: (stageNumber: number) => `/user-stage-progress/${stageNumber}`,
    PASS: (stageNumber: number) => `/user-stage-progress/${stageNumber}/pass`,
    ATTEMPT: (stageNumber: number) => `/user-stage-progress/${stageNumber}/attempt`,
    STATS: '/user-stage-progress/stats',
  },
  // 关卡相关
  STAGE: {
    LIST: '/stages',
    GET_BY_NUMBER: (stageNumber: number) => `/stages/number/${stageNumber}`,
    GET_BY_CHAPTER: (chapterNumber: number) => `/stages/chapter/${chapterNumber}`,
    GET_BOSS: '/stages/boss',
    GET_MAP: (stageNumber: number) => `/stages/${stageNumber}/map`,
  },
} as const

export type ApiEndpointKeys = keyof typeof API_ENDPOINTS

// 关卡进度相关 API 方法
export const stageProgressApi = {
  // 获取所有关卡进度
  async getAllProgress() {
    return await apiClient.get(API_ENDPOINTS.STAGE_PROGRESS.LIST)
  },
  
  // 获取单个关卡进度
  async getProgress(stageNumber: number) {
    return await apiClient.get(API_ENDPOINTS.STAGE_PROGRESS.GET(stageNumber))
  },
  
  // 通过关卡
  async passStage(stageNumber: number, chapterNumber?: number) {
    const data = chapterNumber ? { chapterNumber } : {}
    return await apiClient.post(API_ENDPOINTS.STAGE_PROGRESS.PASS(stageNumber), data)
  },
  
  // 记录关卡尝试（失败时调用）
  async recordAttempt(stageNumber: number, result: 'victory' | 'defeat', chapterNumber?: number) {
    const data: any = { result }
    if (chapterNumber) data.chapterNumber = chapterNumber
    return await apiClient.post(API_ENDPOINTS.STAGE_PROGRESS.ATTEMPT(stageNumber), data)
  },
  
  // 获取统计信息
  async getStats() {
    return await apiClient.get(API_ENDPOINTS.STAGE_PROGRESS.STATS)
  },
}

// 关卡API
export const stageApi = {
  async getAllStages() {
    return await apiClient.get(API_ENDPOINTS.STAGE.LIST)
  },
  
  async getStageByNumber(stageNumber: number) {
    return await apiClient.get(API_ENDPOINTS.STAGE.GET_BY_NUMBER(stageNumber))
  },
  
  async getStagesByChapter(chapterNumber: number) {
    return await apiClient.get(API_ENDPOINTS.STAGE.GET_BY_CHAPTER(chapterNumber))
  },
  
  async getBossStages() {
    return await apiClient.get(API_ENDPOINTS.STAGE.GET_BOSS)
  },
  
  async getStageMap(stageNumber: number) {
    return await apiClient.get(API_ENDPOINTS.STAGE.GET_MAP(stageNumber))
  }
}
