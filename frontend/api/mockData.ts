// 模拟数据服务 - 已废弃，请使用 API 获取真实数据
// 此文件保留仅用于类型定义，所有数据应从后端 API 获取

import type { 
  UserWallet, 
  UserCard, 
  Inventory, 
  ShopOffer,
  CampData,
  UserPlayerCharacter,
  UserCardCharacter
} from '@/types'

// 已废弃：所有模拟数据已删除，请使用 API 获取真实数据
// 如需测试数据，请使用后端提供的测试接口或数据库中的测试数据

export const mockUserWallets: UserWallet[] = [ // 已删除，使用 API
  {
    id: 'wallet-001',
    userId: 'user-001',
    currencyType: 'gold' as any,
    balance: BigInt(5000),
    updatedAt: new Date().toISOString()
  },
  {
    id: 'wallet-002', 
    userId: 'user-001',
    currencyType: 'soulstone' as any,
    balance: BigInt(150),
    updatedAt: new Date().toISOString()
  }
]

export const mockPlayerCharacter: UserPlayerCharacter | null = null // 已删除，使用 API
export const mockCardCharacters: UserCardCharacter[] = [] // 已删除，使用 API
export const mockUserCards: any[] = [] // 已删除，使用 API
export const mockInventory: any[] = [] // 已删除，使用 API
export const mockShopOffers: any[] = [] // 已删除，使用 API
export const mockEvents: any[] = [] // 已删除，使用 API
export const mockAISuggestions: any[] = [] // 已删除，使用 API
export const mockCampData: CampData | null = null // 已删除，使用 API

// 已废弃：模拟API服务已删除，请使用 @/lib/api 中的真实 API
export const mockCampApi = {
  async getCampData() {
    throw new Error('mockCampApi 已废弃，请使用 campApi.getCampData()')
  },
  async getPlayerCharacter() {
    throw new Error('mockCampApi 已废弃，请使用真实 API')
  },
  async getAvailableCardCharacters() {
    throw new Error('mockCampApi 已废弃，请使用真实 API')
  },
  async getInventory() {
    throw new Error('mockCampApi 已废弃，请使用真实 API')
  },
  async getShopOffers() {
    throw new Error('mockCampApi 已废弃，请使用真实 API')
  },
  async getEvents() {
    throw new Error('mockCampApi 已废弃，请使用真实 API')
  },
  async getAISuggestions() {
    throw new Error('mockCampApi 已废弃，请使用真实 API')
  }
}