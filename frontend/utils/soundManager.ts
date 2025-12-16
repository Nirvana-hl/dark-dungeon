/**
 * 音效管理器（uni-app 版本）
 * 负责管理游戏中的音效播放
 */

// uni-app 类型声明
declare const uni: {
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  createInnerAudioContext: () => {
    src: string
    volume: number
    loop: boolean
    play: () => void
    pause: () => void
    stop: () => void
    destroy: () => void
    onPlay: (callback: () => void) => void
    onError: (callback: (error: any) => void) => void
    onEnded: (callback: () => void) => void
  }
}

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

// 音效配置（路径改为 static 目录）
const soundConfig: Record<SoundType, { path: string; volume: number }> = {
  [SoundType.DRAW_CARD]: { path: '/static/sounds/draw_card.mp3', volume: 0.5 },
  [SoundType.PLAY_CARD]: { path: '/static/sounds/play_card.mp3', volume: 0.6 },
  [SoundType.ATTACK]: { path: '/static/sounds/attack.mp3', volume: 0.7 },
  [SoundType.HIT]: { path: '/static/sounds/hit.mp3', volume: 0.8 },
  [SoundType.HEAL]: { path: '/static/sounds/heal.mp3', volume: 0.5 },
  [SoundType.VICTORY]: { path: '/static/sounds/victory.mp3', volume: 0.7 },
  [SoundType.DEFEAT]: { path: '/static/sounds/defeat.mp3', volume: 0.7 }
}

class SoundManager {
  private audioCache: Map<SoundType, ReturnType<typeof uni.createInnerAudioContext>> = new Map()
  private enabled: boolean = true
  private masterVolume: number = 1.0

  /**
   * 初始化音效管理器
   */
  constructor() {
    // 从本地存储读取音效设置
    try {
      const savedEnabled = uni.getStorageSync('soundEnabled')
      if (savedEnabled !== null && savedEnabled !== undefined) {
        this.enabled = savedEnabled === true || savedEnabled === 'true'
    }
    
      const savedVolume = uni.getStorageSync('soundVolume')
      if (savedVolume !== null && savedVolume !== undefined) {
        this.masterVolume = parseFloat(String(savedVolume)) || 1.0
      }
    } catch (error) {
      console.warn('[SoundManager] 读取音效设置失败:', error)
    }
  }

  /**
   * 预加载音效文件（uni-app 中音频会自动加载，此方法保留接口兼容性）
   */
  async preloadSounds(): Promise<void> {
    // uni-app 中音频不需要预加载，直接使用即可
    // 但可以预先创建音频上下文
    try {
      Object.keys(soundConfig).forEach((type) => {
      const soundType = type as SoundType
      const config = soundConfig[soundType]
      
      try {
          const audio = uni.createInnerAudioContext()
          audio.src = config.path
        audio.volume = config.volume * this.masterVolume
          audio.loop = false
          
          // 监听错误
          audio.onError((error) => {
            console.warn(`[SoundManager] 音效加载失败: ${config.path}`, error)
        })
        
        this.audioCache.set(soundType, audio)
      } catch (error) {
          console.warn(`[SoundManager] 无法创建音效上下文: ${config.path}`, error)
        }
      })
    } catch (error) {
      console.warn('[SoundManager] 预加载音效失败:', error)
      }
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
      
      // 创建新的音频对象（uni-app 中每次播放都创建新实例更稳定）
      const audio = uni.createInnerAudioContext()
      audio.src = config.path
      audio.volume = (options?.volume ?? config.volume) * this.masterVolume
      audio.loop = options?.loop || false
      
      // 监听播放成功
      audio.onPlay(() => {
        if (import.meta.env?.DEV) {
          console.log(`[SoundManager] ✓ 播放音效: ${type}`)
        }
      })
      
      // 监听播放结束，自动销毁
      audio.onEnded(() => {
        audio.destroy()
      })
      
      // 监听错误
      audio.onError((error) => {
        console.warn(`[SoundManager] ✗ 播放音效失败: ${type} (${config.path})`, error)
        audio.destroy()
      })
      
      // 播放音效
      try {
        audio.play()
      } catch (error) {
        console.warn(`[SoundManager] 播放音效异常: ${type}`, error)
        audio.destroy()
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
      try {
        audio.stop()
        audio.destroy()
        this.audioCache.delete(type)
      } catch (error) {
        console.warn(`[SoundManager] 停止音效失败: ${type}`, error)
      }
    }
  }

  /**
   * 设置音效开关
   */
  setEnabled(enabled: boolean): void {
    this.enabled = enabled
    try {
      uni.setStorageSync('soundEnabled', enabled)
    } catch (error) {
      console.warn('[SoundManager] 保存音效设置失败:', error)
    }
  }

  /**
   * 设置主音量
   */
  setMasterVolume(volume: number): void {
    this.masterVolume = Math.max(0, Math.min(1, volume))
    try {
      uni.setStorageSync('soundVolume', this.masterVolume)
    } catch (error) {
      console.warn('[SoundManager] 保存音量设置失败:', error)
    }
    
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

  /**
   * 销毁所有音频（清理资源）
   */
  destroy(): void {
    this.audioCache.forEach((audio) => {
      try {
        audio.destroy()
      } catch (error) {
        console.warn('[SoundManager] 销毁音频失败:', error)
      }
    })
    this.audioCache.clear()
  }
}

// 导出单例
export const soundManager = new SoundManager()
