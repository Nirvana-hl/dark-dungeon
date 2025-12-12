/**
 * 音效管理器
 * 负责管理游戏中的音效播放
 */

// 音效类型枚举
export enum SoundType {
  DRAW_CARD = 'draw_card',        // 发牌音效
  PLAY_CARD = 'play_card',        // 出牌音效
  ATTACK = 'attack',              // 攻击音效
  HIT = 'hit',                     // 受击音效
  HEAL = 'heal',                   // 治疗音效
  VICTORY = 'victory',             // 胜利音效
  DEFEAT = 'defeat'                // 失败音效
}

// 音效配置
const soundConfig: Record<SoundType, { path: string; volume: number }> = {
  [SoundType.DRAW_CARD]: { path: '/sounds/draw_card.mp3', volume: 0.5 },
  [SoundType.PLAY_CARD]: { path: '/sounds/play_card.mp3', volume: 0.6 },
  [SoundType.ATTACK]: { path: '/sounds/attack.mp3', volume: 0.7 },
  [SoundType.HIT]: { path: '/sounds/hit.mp3', volume: 0.8 },
  [SoundType.HEAL]: { path: '/sounds/heal.mp3', volume: 0.5 },
  [SoundType.VICTORY]: { path: '/sounds/victory.mp3', volume: 0.7 },
  [SoundType.DEFEAT]: { path: '/sounds/defeat.mp3', volume: 0.7 }
}

class SoundManager {
  private audioCache: Map<SoundType, HTMLAudioElement> = new Map()
  private enabled: boolean = true
  private masterVolume: number = 1.0

  /**
   * 初始化音效管理器
   */
  constructor() {
    // 从本地存储读取音效设置
    const savedEnabled = localStorage.getItem('soundEnabled')
    if (savedEnabled !== null) {
      this.enabled = savedEnabled === 'true'
    }
    
    const savedVolume = localStorage.getItem('soundVolume')
    if (savedVolume !== null) {
      this.masterVolume = parseFloat(savedVolume)
    }
  }

  /**
   * 预加载音效文件
   */
  async preloadSounds(): Promise<void> {
    const promises = Object.keys(soundConfig).map(async (type) => {
      const soundType = type as SoundType
      const config = soundConfig[soundType]
      
      try {
        const audio = new Audio(config.path)
        audio.volume = config.volume * this.masterVolume
        audio.preload = 'auto'
        
        // 等待音频加载完成
        await new Promise((resolve, reject) => {
          audio.addEventListener('canplaythrough', resolve, { once: true })
          audio.addEventListener('error', reject, { once: true })
          // 设置超时，避免无限等待
          setTimeout(() => resolve(null), 3000)
        })
        
        this.audioCache.set(soundType, audio)
      } catch (error) {
        console.warn(`[SoundManager] 无法加载音效: ${config.path}`, error)
        // 创建静默音频作为占位符，避免后续播放失败
        const silentAudio = new Audio()
        silentAudio.volume = 0
        this.audioCache.set(soundType, silentAudio)
      }
    })
    
    await Promise.allSettled(promises)
  }

  /**
   * 播放音效
   */
  playSound(type: SoundType, options?: { volume?: number; loop?: boolean }): void {
    if (!this.enabled) return

    try {
      const config = soundConfig[type]
      if (!config) {
        console.warn(`[SoundManager] 未知音效类型: ${type}`)
        return
      }
      
      // 总是创建新的音频对象，确保每次都能播放
      const audio = new Audio(config.path)
      audio.volume = (options?.volume ?? config.volume) * this.masterVolume
      
      // 设置循环
      if (options?.loop) {
        audio.loop = true
      }
      
      // 播放音效
      try {
        const playPromise = audio.play()
        
        if (playPromise !== undefined) {
          playPromise
            .then(() => {
              // 播放成功（开发环境输出日志）
              if (import.meta.env.DEV) {
                console.log(`[SoundManager] ✓ 播放音效: ${type}`)
              }
            })
            .catch((error) => {
              // 忽略用户交互限制导致的播放失败
              if (error.name !== 'NotAllowedError' && error.name !== 'AbortError') {
                console.warn(`[SoundManager] ✗ 播放音效失败: ${type} (${config.path})`, error)
                // 如果文件不存在，尝试检查文件路径
                if (error.message && error.message.includes('404')) {
                  console.warn(`[SoundManager] 音效文件不存在: ${config.path}`)
                }
              }
            })
        }
      } catch (error) {
        console.warn(`[SoundManager] 播放音效异常: ${type}`, error)
      }
    } catch (error) {
      console.warn(`[SoundManager] 播放音效异常: ${type}`, error)
    }
  }

  /**
   * 停止音效
   */
  stopSound(type: SoundType): void {
    const audio = this.audioCache.get(type)
    if (audio) {
      audio.pause()
      audio.currentTime = 0
    }
  }

  /**
   * 设置音效开关
   */
  setEnabled(enabled: boolean): void {
    this.enabled = enabled
    localStorage.setItem('soundEnabled', String(enabled))
  }

  /**
   * 设置主音量
   */
  setMasterVolume(volume: number): void {
    this.masterVolume = Math.max(0, Math.min(1, volume))
    localStorage.setItem('soundVolume', String(this.masterVolume))
    
    // 更新所有已加载音频的音量
    this.audioCache.forEach((audio, type) => {
      const config = soundConfig[type]
      audio.volume = config.volume * this.masterVolume
    })
  }

  /**
   * 获取音效开关状态
   */
  isEnabled(): boolean {
    return this.enabled
  }

  /**
   * 获取主音量
   */
  getMasterVolume(): number {
    return this.masterVolume
  }
}

// 导出单例
export const soundManager = new SoundManager()

