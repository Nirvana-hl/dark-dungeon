import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'

/**
 * API 基础配置
 */
const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 创建 axios 实例
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器 - 添加 Token
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器 - 处理错误
 */
apiClient.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期或无效，清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      // 可以跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * API 响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户注册
   */
  register: (username: string, email: string, password: string) => {
    return apiClient.post<ApiResponse<AuthResponse>>('/auth/register', {
      username,
      email,
      password
    })
  },

  /**
   * 用户登录
   */
  login: (email: string, password: string) => {
    return apiClient.post<ApiResponse<AuthResponse>>('/auth/login', {
      email,
      password
    })
  }
}

/**
 * 认证响应类型
 */
export interface AuthResponse {
  token: string
  userId: number
  username: string
  email: string
}

/**
 * 角色相关 API
 */
export const characterApi = {
  /**
   * 获取所有角色
   */
  getCharacters: () => {
    return apiClient.get<ApiResponse<CharacterDTO[]>>('/characters')
  },

  /**
   * 创建角色
   */
  createCharacter: (data: Partial<CharacterDTO>) => {
    return apiClient.post<ApiResponse<CharacterDTO>>('/characters', data)
  },

  /**
   * 删除角色
   */
  deleteCharacter: (id: number) => {
    return apiClient.delete<ApiResponse<void>>(`/characters/${id}`)
  },

  /**
   * 角色升星
   */
  starUp: (id: number) => {
    return apiClient.post<ApiResponse<CharacterDTO>>(`/characters/${id}/star-up`)
  }
}

/**
 * 角色 DTO
 */
export interface CharacterDTO {
  id?: number
  name: string
  classType: '战士' | '法师' | '游侠'
  stars: number
  hp: number
  mp: number
}

/**
 * 钱包相关 API
 */
export const walletApi = {
  /**
   * 获取金币
   */
  getGold: () => {
    return apiClient.get<ApiResponse<number>>('/wallet/gold')
  },

  /**
   * 消费金币
   */
  spend: (amount: number) => {
    return apiClient.post<ApiResponse<boolean>>('/wallet/spend', { amount })
  },

  /**
   * 增加金币
   */
  add: (amount: number) => {
    return apiClient.post<ApiResponse<number>>('/wallet/add', { amount })
  }
}

/**
 * 游戏相关 API
 */
export const gameApi = {
  /**
   * 获取用户卡牌
   */
  getUserCards: () => {
    return apiClient.get<ApiResponse<UserCard[]>>('/game/cards')
  },

  /**
   * 获取敌方卡牌
   */
  getEnemyCards: (stage: number, difficulty: string) => {
    return apiClient.get<ApiResponse<EnemyCard[]>>('/game/enemy-cards', {
      params: { stage, difficulty }
    })
  },

  /**
   * 获取角色特性
   */
  getCharacterTraits: () => {
    return apiClient.get<ApiResponse<Record<string, CharacterTrait>>>('/game/character-traits')
  }
}

/**
 * 用户卡牌类型
 */
export interface UserCard {
  name: string
  type: string
  quantity: number
  attack?: number
  health?: number
  effect?: string
}

/**
 * 敌方卡牌类型
 */
export interface EnemyCard {
  name: string
  type: string
  attack?: number
  health?: number
  effect?: string
  unique_play?: boolean
}

/**
 * 角色特性类型
 */
export interface CharacterTrait {
  trait_key: string
  base_power: number
  power_per_star: number
  description: string
}

export default apiClient

