// 基础类型定义
export type UUID = string
export type Timestamp = string

// 枚举类型
export enum UserStatus {
  ACTIVE = 'active',
  BANNED = 'banned',
  DORMANT = 'dormant'
}

export enum Rarity {
  COMMON = 'common',
  RARE = 'rare',
  EPIC = 'epic',
  LEGENDARY = 'legendary'
}

export enum CharacterClass {
  WARRIOR = 'warrior',
  OCCULTIST = 'occultist',
  RANGER = 'ranger',
  PRIEST = 'priest',
  MAGE = 'mage',
  ROGUE = 'rogue'
}

export enum Faction {
  HUMAN = 'human',
  OCCULT = 'occult',
  MACHINE = 'machine',
  BEAST = 'beast',
  UNDEAD = 'undead',
  DIVINE = 'divine',
  OTHER = 'other'
}

export enum CardType {
  SPELL = 'spell',
  EQUIPMENT = 'equipment'
}

export enum SlotType {
  WEAPON = 'weapon',
  ARMOR = 'armor',
  TRINKET = 'trinket',
  NONE = 'none'
}

export enum ItemType {
  CONSUMABLE = 'consumable',
  MATERIAL = 'material',
  BLUEPRINT = 'blueprint',
  CURRENCY_BUNDLE = 'currency_bundle',
  COSMETIC = 'cosmetic'
}

export enum TraitType {
  POSITIVE = 'positive',
  NEGATIVE = 'negative',
  NEUTRAL = 'neutral'
}

export enum StressDebuffType {
  MENTAL = 'mental',
  COMBAT = 'combat',
  BEHAVIORAL = 'behavioral'
}

export enum Difficulty {
  EASY = 'easy',
  NORMAL = 'normal',
  HARD = 'hard',
  EXPERT = 'expert'
}

export enum LocationType {
  CAMP = 'camp',
  DUNGEON = 'dungeon'
}

export enum BindStatus {
  UNBOUND = 'unbound',
  BOUND = 'bound'
}

export enum CurrencyType {
  GOLD = 'gold',
  SOULSTONE = 'soulstone'
}

// 用户相关接口
export interface User {
  id: UUID | number | string
  accountName: string
  email?: string
  playerLevel?: number
  playerExp?: number
  createdAt?: Timestamp
  status?: UserStatus
}

export interface UserWallet {
  id: UUID
  userId: UUID
  currencyType: CurrencyType
  balance: bigint
  updatedAt: Timestamp
}

// 玩家角色相关接口
export interface PlayerCharacter {
  id: UUID
  code: string
  name: string
  baseHp: number
  hpPerLevel: number
  lore: string
}

export interface UserPlayerCharacter {
  id: UUID
  userId: UUID
  playerCharacterId: UUID
  maxHp: number
  currentHp: number
  maxActionPoints: number
  currentActionPoints: number
  currentStress: number
  stressLevel: number
  stressDebuffs: Record<string, any>[]
}

// 卡牌角色相关接口
export interface CardCharacter {
  id: UUID
  code: string
  name: string
  class: CharacterClass
  faction: Faction
  rarity: Rarity
  baseHp: number
  baseAttack: number
  actionPointCost: number
  baseStarLevel: number
  maxStarLevel: number
  starUpgradePayload: Record<string, any>
  traits: Record<string, any>[]
  shopPrice: number
  lore: string
}

export interface UserCardCharacter {
  id: UUID
  userId: UUID
  cardCharacterId: UUID
  currentHp: number
  currentArmor: number
  isDeployed: boolean
  deployedRound: number
  currentStarLevel: number
}

export interface CardCharacterTrait {
  id: UUID
  cardCharacterId: UUID
  name: string
  type: TraitType
  effectPayload: Record<string, any>
  scalingPayload: Record<string, any>
  description: string
}

// 技能相关接口
export interface Skill {
  id: UUID
  code: string
  playerCharacterCode: string
  name: string
  description: string
  effectPayload: Record<string, any>
  requiredLevel: number
  positionInTree: {
    row: number
    column: number
  }
  unlockPath?: string[]
  isUnlocked?: boolean
  canUnlock?: boolean
  skillType?: 'active' | 'passive' // 技能类型：后端解析effectPayload后返回
}

export interface UserPlayerCharacterSkill {
  id: UUID
  userPlayerCharacterId: UUID
  skillId: UUID
  unlockedAt: Timestamp
}

// 压力debuff相关接口
export interface StressDebuffConfig {
  id: UUID
  stressLevel: number
  debuffName: string
  debuffType: StressDebuffType
  effectDescription: string
  triggerChance: number
  effectPayload: Record<string, any>
  isPersistent: boolean
  // 为了兼容组件中的使用，添加别名属性
  type: StressDebuffType
  name: string
  description: string
}

// 卡牌相关接口
export interface Card {
  id: UUID
  code: string
  name: string
  cardType: CardType
  rarity: Rarity
  slotType: SlotType
  statModifiers: Record<string, any>
  effectPayload: Record<string, any>
  campUnlockCondition?: Record<string, any>
  shopPrice?: Record<string, any>
  description: string
}

export interface UserCard {
  id: UUID
  userId: UUID
  cardId: UUID
  quantity: number
  level: number
  loadoutId?: UUID
  equippedToUserCardCharacterId?: UUID
  acquiredAt: Timestamp
  acquiredSource: string
  lastUsedAt?: Timestamp
}

// 道具相关接口
export interface Item {
  id: UUID
  code: string
  name: string
  itemType: ItemType
  rarity: Rarity
  effectPayload: Record<string, any>
  stackLimit: number
  shopPrice: number
  description: string
}

export interface Inventory {
  id: UUID
  userId: UUID
  itemId: UUID
  quantity: number
  bindStatus: BindStatus
  lastUpdatedAt: Timestamp
}

// 商城相关接口
export interface ShopOffer {
  id: UUID
  offerType: 'card' | 'item' | 'bundle'
  targetId: UUID
  name: string
  description: string
  rarity: Rarity
  currencyType: CurrencyType
  price: bigint
  displayOrder: number
  stock?: number
  refreshRule?: Record<string, any>
}

// 地牢相关接口
export interface Dungeon {
  id: UUID
  name: string
  difficulty: Difficulty
  recommendedCards: Record<string, any>[]
  description: string
}

export interface Run {
  id: UUID
  userId: UUID
  dungeonId: UUID
  preparationSnapshot: Record<string, any>
  status: 'in_progress' | 'completed' | 'failed'
  currentRound?: number
  usedActionPoints?: number
  maxRoundActionPoints?: number
  startedAt: Timestamp
  completedAt?: Timestamp
}

// 敌人相关接口
export interface Enemy {
  id: UUID
  name: string
  difficulty: Difficulty
  baseStats: Record<string, any>
}

export interface EnemyCard {
  id: UUID
  enemyId: UUID
  cardId: UUID
  weight: number
}

// 事件相关接口
export interface Event {
  id: UUID
  name: string
  locationType: LocationType
  description: string
  effectPayload: Record<string, any>
}

// 统计相关接口
export interface PlayerAction {
  id: UUID
  userId: UUID
  actionType: string
  sourceScene: 'camp' | 'dungeon' | 'battle'
  metadata: Record<string, any>
  timestamp: Timestamp
}

export interface Achievement {
  id: UUID
  name: string
  category: 'progression' | 'mastery' | 'collection' | 'social'
  description: string
  requirements: Record<string, any>
}

export interface GameMetric {
  id: UUID
  metricType: string
  date: string
  value: bigint
  dimensionPayload?: Record<string, any>
}

// 请求/响应DTO接口
export interface LoginRequest {
  accountName?: string
  email?: string
  password: string
}

export interface RegisterRequest {
  accountName: string
  email: string
  password: string
  confirmPassword?: string
}

export interface AuthResponse {
  token: string
  userId: UUID | number
  accountName: string
  email?: string
}

export interface CharacterDTO {
  id: UUID
  name: string
  level: number
  currentHp: number
  maxHp: number
  currentActionPoints: number
  maxActionPoints: number
  currentStress: number
  stressLevel: number
}

// 装备相关接口
export interface EquipmentRequest {
  userCardId: UUID
  userCardCharacterId: UUID
}

export interface UpgradeRequest {
  userCardId: UUID
  consumeQuantity: number
}

// 商城购买请求
export interface PurchaseRequest {
  offerId: UUID
  quantity?: number
}

// 游戏状态接口
export interface GameState {
  user: User
  playerCharacter: UserPlayerCharacter
  cardCharacters: UserCardCharacter[]
  userCards: UserCard[]
  inventory: Inventory[]
  wallet: UserWallet[]
  currentRun?: Run
}

export interface CampData {
  playerCharacter: UserPlayerCharacter
  availableCardCharacters: UserCardCharacter[]
  userCards: UserCard[]
  inventory: Inventory[]
  shopOffers: ShopOffer[]
  availableSkills: Skill[]
  unlockedSkills: UserPlayerCharacterSkill[]
}