<template>
  <main class="battle-field">
    <!-- 上下：战斗区域 -->
    <view class="battle-arena">
      <!-- 敌人首领改为放在对战槽位（后排中间 position 4），取消独立面板 -->

      <!-- 敌人对战区域 -->
      <view class="battle-area">
        <view class="enemy-battle-zone">
        <view class="battle-slots enemy-slots">
          <!-- 3x2网格布局：前排位置1,2,3，后排位置4,5,6 -->
          <view class="battle-slot enemy-slot" :class="{ 'occupied': getEnemyMinionAtPosition(0) }">
            <view
              v-if="getEnemyMinionAtPosition(0)"
              class="character-card enemy-card"
              :class="{
                'hit-effect': hasHitEffect(getEnemyMinionAtPosition(0)!.id),
                'attack-target': Object.values(attackingMinions).some(a => a.targetId === getEnemyMinionAtPosition(0)!.id),
                'attacking': isAttacking(getEnemyMinionAtPosition(0)!.id)
              }"
              :data-minion-id="getEnemyMinionAtPosition(0)!.id"
              :data-attacker-id="getEnemyMinionAtPosition(0)!.id"
            >
              <view
                v-if="isAttacking(getEnemyMinionAtPosition(0)!.id)"
                class="character-card-attack-clone enemy-attack-clone"
                :style="getAttackStyle(getEnemyMinionAtPosition(0)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(0)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(0)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(0)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(0)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(0)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getEnemyMinionAtPosition(0)!.id)"
                class="damage-number enemy-damage"
                :key="getHitEffect(getEnemyMinionAtPosition(0)!.id)?.timestamp"
              >
                -{{ getHitEffect(getEnemyMinionAtPosition(0)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getEnemyMinionAtPosition(0)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getEnemyMinionAtPosition(0)!.shield && getEnemyMinionAtPosition(0)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getEnemyMinionAtPosition(0)!.shield }}</text>
                  </view>
                  <view v-if="getEnemyMinionAtPosition(0)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(0)!.name, 'enemy').color]">
                  {{ iconFor(getEnemyMinionAtPosition(0)!.name, 'enemy').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(0)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(0)!.health }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">1</text>
            </view>
          </view>
          <view class="battle-slot enemy-slot" :class="{ 'occupied': getEnemyMinionAtPosition(1) }">
            <view
              v-if="getEnemyMinionAtPosition(1)"
              class="character-card enemy-card"
              :class="{
                'hit-effect': hasHitEffect(getEnemyMinionAtPosition(1)!.id),
                'attack-target': Object.values(attackingMinions).some(a => a.targetId === getEnemyMinionAtPosition(1)!.id),
                'attacking': isAttacking(getEnemyMinionAtPosition(1)!.id)
              }"
              :data-minion-id="getEnemyMinionAtPosition(1)!.id"
              :data-attacker-id="getEnemyMinionAtPosition(1)!.id"
            >
              <view
                v-if="isAttacking(getEnemyMinionAtPosition(1)!.id)"
                class="character-card-attack-clone enemy-attack-clone"
                :style="getAttackStyle(getEnemyMinionAtPosition(1)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(1)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(1)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(1)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(1)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(1)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getEnemyMinionAtPosition(1)!.id)"
                class="damage-number enemy-damage"
                :key="getHitEffect(getEnemyMinionAtPosition(1)!.id)?.timestamp"
              >
                -{{ getHitEffect(getEnemyMinionAtPosition(1)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getEnemyMinionAtPosition(1)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getEnemyMinionAtPosition(1)!.shield && getEnemyMinionAtPosition(1)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getEnemyMinionAtPosition(1)!.shield }}</text>
                  </view>
                  <view v-if="getEnemyMinionAtPosition(1)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(1)!.name, 'enemy').color]">
                  {{ iconFor(getEnemyMinionAtPosition(1)!.name, 'enemy').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(1)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(1)!.health }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">2</text>
            </view>
          </view>
          <view class="battle-slot enemy-slot" :class="{ 'occupied': getEnemyMinionAtPosition(2) }">
            <view
              v-if="getEnemyMinionAtPosition(2)"
              class="character-card enemy-card"
              :class="{
                'hit-effect': hasHitEffect(getEnemyMinionAtPosition(2)!.id),
                'attack-target': Object.values(attackingMinions).some(a => a.targetId === getEnemyMinionAtPosition(2)!.id),
                'attacking': isAttacking(getEnemyMinionAtPosition(2)!.id)
              }"
              :data-minion-id="getEnemyMinionAtPosition(2)!.id"
              :data-attacker-id="getEnemyMinionAtPosition(2)!.id"
            >
              <view
                v-if="isAttacking(getEnemyMinionAtPosition(2)!.id)"
                class="character-card-attack-clone enemy-attack-clone"
                :style="getAttackStyle(getEnemyMinionAtPosition(2)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(2)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(2)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(2)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(2)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(2)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getEnemyMinionAtPosition(2)!.id)"
                class="damage-number enemy-damage"
                :key="getHitEffect(getEnemyMinionAtPosition(2)!.id)?.timestamp"
              >
                -{{ getHitEffect(getEnemyMinionAtPosition(2)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getEnemyMinionAtPosition(2)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getEnemyMinionAtPosition(2)!.shield && getEnemyMinionAtPosition(2)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getEnemyMinionAtPosition(2)!.shield }}</text>
                  </view>
                  <view v-if="getEnemyMinionAtPosition(2)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(2)!.name, 'enemy').color]">
                  {{ iconFor(getEnemyMinionAtPosition(2)!.name, 'enemy').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(2)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(2)!.health }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">3</text>
            </view>
          </view>
          <view class="battle-slot enemy-slot" :class="{ 'occupied': getEnemyMinionAtPosition(3) }">
            <view
              v-if="getEnemyMinionAtPosition(3)"
              class="character-card enemy-card"
              :class="{
                'hit-effect': hasHitEffect(getEnemyMinionAtPosition(3)!.id),
                'attack-target': Object.values(attackingMinions).some(a => a.targetId === getEnemyMinionAtPosition(3)!.id),
                'attacking': isAttacking(getEnemyMinionAtPosition(3)!.id)
              }"
              :data-minion-id="getEnemyMinionAtPosition(3)!.id"
              :data-attacker-id="getEnemyMinionAtPosition(3)!.id"
            >
              <view
                v-if="isAttacking(getEnemyMinionAtPosition(3)!.id)"
                class="character-card-attack-clone enemy-attack-clone"
                :style="getAttackStyle(getEnemyMinionAtPosition(3)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(3)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(3)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(3)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(3)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(3)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getEnemyMinionAtPosition(3)!.id)"
                class="damage-number enemy-damage"
                :key="getHitEffect(getEnemyMinionAtPosition(3)!.id)?.timestamp"
              >
                -{{ getHitEffect(getEnemyMinionAtPosition(3)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getEnemyMinionAtPosition(3)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getEnemyMinionAtPosition(3)!.shield && getEnemyMinionAtPosition(3)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getEnemyMinionAtPosition(3)!.shield }}</text>
                  </view>
                  <view v-if="getEnemyMinionAtPosition(3)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(3)!.name, 'enemy').color]">
                  {{ iconFor(getEnemyMinionAtPosition(3)!.name, 'enemy').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(3)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(3)!.health }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">4</text>
            </view>
          </view>
          <view class="battle-slot enemy-slot" :class="{ 'occupied': getEnemyMinionAtPosition(4) }">
            <view
              v-if="getEnemyMinionAtPosition(4)"
              class="character-card enemy-card"
              :class="{
                'hit-effect': hasHitEffect(getEnemyMinionAtPosition(4)!.id),
                'attack-target': Object.values(attackingMinions).some(a => a.targetId === getEnemyMinionAtPosition(4)!.id),
                'attacking': isAttacking(getEnemyMinionAtPosition(4)!.id)
              }"
              :data-minion-id="getEnemyMinionAtPosition(4)!.id"
              :data-attacker-id="getEnemyMinionAtPosition(4)!.id"
            >
              <view
                v-if="isAttacking(getEnemyMinionAtPosition(4)!.id)"
                class="character-card-attack-clone enemy-attack-clone"
                :style="getAttackStyle(getEnemyMinionAtPosition(4)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(4)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(4)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(4)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(4)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(4)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getEnemyMinionAtPosition(4)!.id)"
                class="damage-number enemy-damage"
                :key="getHitEffect(getEnemyMinionAtPosition(4)!.id)?.timestamp"
              >
                -{{ getHitEffect(getEnemyMinionAtPosition(4)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getEnemyMinionAtPosition(4)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getEnemyMinionAtPosition(4)!.shield && getEnemyMinionAtPosition(4)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getEnemyMinionAtPosition(4)!.shield }}</text>
                  </view>
                  <view v-if="getEnemyMinionAtPosition(4)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(4)!.name, 'enemy').color]">
                  {{ iconFor(getEnemyMinionAtPosition(4)!.name, 'enemy').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(4)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(4)!.health }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">5</text>
            </view>
          </view>
          <view class="battle-slot enemy-slot" :class="{ 'occupied': getEnemyMinionAtPosition(5) }">
            <view
              v-if="getEnemyMinionAtPosition(5)"
              class="character-card enemy-card"
              :class="{
                'hit-effect': hasHitEffect(getEnemyMinionAtPosition(5)!.id),
                'attack-target': Object.values(attackingMinions).some(a => a.targetId === getEnemyMinionAtPosition(5)!.id),
                'attacking': isAttacking(getEnemyMinionAtPosition(5)!.id)
              }"
              :data-minion-id="getEnemyMinionAtPosition(5)!.id"
              :data-attacker-id="getEnemyMinionAtPosition(5)!.id"
            >
              <view
                v-if="isAttacking(getEnemyMinionAtPosition(5)!.id)"
                class="character-card-attack-clone enemy-attack-clone"
                :style="getAttackStyle(getEnemyMinionAtPosition(5)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(5)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(5)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(5)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(5)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(5)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getEnemyMinionAtPosition(5)!.id)"
                class="damage-number enemy-damage"
                :key="getHitEffect(getEnemyMinionAtPosition(5)!.id)?.timestamp"
              >
                -{{ getHitEffect(getEnemyMinionAtPosition(5)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getEnemyMinionAtPosition(5)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getEnemyMinionAtPosition(5)!.shield && getEnemyMinionAtPosition(5)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getEnemyMinionAtPosition(5)!.shield }}</text>
                  </view>
                  <view v-if="getEnemyMinionAtPosition(5)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(5)!.name, 'enemy').color]">
                  {{ iconFor(getEnemyMinionAtPosition(5)!.name, 'enemy').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(5)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getEnemyMinionAtPosition(5)!.health }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">6</text>
            </view>
          </view>
        </view>
        </view>
      
        <view class="battle-divider" aria-hidden="true">
          <view class="divider-line"></view>
          <view class="divider-icon">⚔️</view>
          <view class="divider-line"></view>
        </view>

      <!-- 玩家对战区域 -->
      <view class="player-battle-zone" :class="{ 'ally-turn-active': turn === 'player' }">
        <view class="battle-slots player-slots">
          <!-- 3x2网格布局：前排位置1,2,3，后排位置4,5,6 -->
          <view class="battle-slot player-slot" :class="{ 'occupied': getMinionAtPosition(0) }">
            <view
              v-if="getMinionAtPosition(0)"
              class="character-card ally-card"
              :class="{
                'hit-effect': hasHitEffect(getMinionAtPosition(0)!.id),
                'attacking': isAttacking(getMinionAtPosition(0)!.id)
              }"
              :data-minion-id="getMinionAtPosition(0)!.id"
              :data-attacker-id="getMinionAtPosition(0)!.id"
              @dragover.prevent
              @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(0)!.id })"
            >
              <view
                v-if="isAttacking(getMinionAtPosition(0)!.id)"
                class="character-card-attack-clone"
                :style="getAttackStyle(getMinionAtPosition(0)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(0)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(0)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(0)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(0)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(0)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getMinionAtPosition(0)!.id)"
                class="damage-number"
                :key="getHitEffect(getMinionAtPosition(0)!.id)?.timestamp"
              >
                -{{ getHitEffect(getMinionAtPosition(0)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getMinionAtPosition(0)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getMinionAtPosition(0)!.shield && getMinionAtPosition(0)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getMinionAtPosition(0)!.shield }}</text>
                  </view>
                  <view v-if="getMinionAtPosition(0)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getMinionAtPosition(0)!.name, 'ally').color]">
                  {{ iconFor(getMinionAtPosition(0)!.name, 'ally').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getMinionAtPosition(0)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getMinionAtPosition(0)!.health }}</text>
                </view>
              </view>
              <view v-if="getMinionAtPosition(0)!.equipmentNames && getMinionAtPosition(0)!.equipmentNames!.length" class="equipment-dots">
                <text
                  v-for="(eq, idx) in getMinionAtPosition(0)!.equipmentNames"
                  :key="idx"
                  class="equipment-dot"
                  :title="`已装备：${eq}`"
                ></text>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">1</text>
            </view>
          </view>
          <view class="battle-slot player-slot" :class="{ 'occupied': getMinionAtPosition(1) }">
            <view
              v-if="getMinionAtPosition(1)"
              class="character-card ally-card"
              :class="{
                'hit-effect': hasHitEffect(getMinionAtPosition(1)!.id),
                'attacking': isAttacking(getMinionAtPosition(1)!.id)
              }"
              :data-minion-id="getMinionAtPosition(1)!.id"
              :data-attacker-id="getMinionAtPosition(1)!.id"
              @dragover.prevent
              @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(1)!.id })"
            >
              <view
                v-if="isAttacking(getMinionAtPosition(1)!.id)"
                class="character-card-attack-clone"
                :style="getAttackStyle(getMinionAtPosition(1)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(1)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(1)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(1)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(1)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(1)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getMinionAtPosition(1)!.id)"
                class="damage-number"
                :key="getHitEffect(getMinionAtPosition(1)!.id)?.timestamp"
              >
                -{{ getHitEffect(getMinionAtPosition(1)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getMinionAtPosition(1)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getMinionAtPosition(1)!.shield && getMinionAtPosition(1)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getMinionAtPosition(1)!.shield }}</text>
                  </view>
                  <view v-if="getMinionAtPosition(1)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getMinionAtPosition(1)!.name, 'ally').color]">
                  {{ iconFor(getMinionAtPosition(1)!.name, 'ally').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getMinionAtPosition(1)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getMinionAtPosition(1)!.health }}</text>
                </view>
              </view>
              <view v-if="getMinionAtPosition(1)!.equipmentNames && getMinionAtPosition(1)!.equipmentNames!.length" class="equipment-dots">
                <text
                  v-for="(eq, idx) in getMinionAtPosition(1)!.equipmentNames"
                  :key="idx"
                  class="equipment-dot"
                  :title="`已装备：${eq}`"
                ></text>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">2</text>
            </view>
          </view>
          <view class="battle-slot player-slot" :class="{ 'occupied': getMinionAtPosition(2) }">
            <view
              v-if="getMinionAtPosition(2)"
              class="character-card ally-card"
              :class="{
                'hit-effect': hasHitEffect(getMinionAtPosition(2)!.id),
                'attacking': isAttacking(getMinionAtPosition(2)!.id)
              }"
              :data-minion-id="getMinionAtPosition(2)!.id"
              :data-attacker-id="getMinionAtPosition(2)!.id"
              @dragover.prevent
              @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(2)!.id })"
            >
              <view
                v-if="isAttacking(getMinionAtPosition(2)!.id)"
                class="character-card-attack-clone"
                :style="getAttackStyle(getMinionAtPosition(2)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(2)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(2)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(2)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(2)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(2)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getMinionAtPosition(2)!.id)"
                class="damage-number"
                :key="getHitEffect(getMinionAtPosition(2)!.id)?.timestamp"
              >
                -{{ getHitEffect(getMinionAtPosition(2)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getMinionAtPosition(2)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getMinionAtPosition(2)!.shield && getMinionAtPosition(2)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getMinionAtPosition(2)!.shield }}</text>
                  </view>
                  <view v-if="getMinionAtPosition(2)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getMinionAtPosition(2)!.name, 'ally').color]">
                  {{ iconFor(getMinionAtPosition(2)!.name, 'ally').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getMinionAtPosition(2)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getMinionAtPosition(2)!.health }}</text>
                </view>
              </view>
              <view v-if="getMinionAtPosition(2)!.equipmentNames && getMinionAtPosition(2)!.equipmentNames!.length" class="equipment-dots">
                <text
                  v-for="(eq, idx) in getMinionAtPosition(2)!.equipmentNames"
                  :key="idx"
                  class="equipment-dot"
                  :title="`已装备：${eq}`"
                ></text>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">3</text>
            </view>
          </view>
          <view class="battle-slot player-slot" :class="{ 'occupied': getMinionAtPosition(3) }">
            <view
              v-if="getMinionAtPosition(3)"
              class="character-card ally-card"
              :class="{
                'hit-effect': hasHitEffect(getMinionAtPosition(3)!.id),
                'attacking': isAttacking(getMinionAtPosition(3)!.id)
              }"
              :data-minion-id="getMinionAtPosition(3)!.id"
              :data-attacker-id="getMinionAtPosition(3)!.id"
              @dragover.prevent
              @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(3)!.id })"
            >
              <view
                v-if="isAttacking(getMinionAtPosition(3)!.id)"
                class="character-card-attack-clone"
                :style="getAttackStyle(getMinionAtPosition(3)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(3)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(3)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(3)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(3)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(3)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getMinionAtPosition(3)!.id)"
                class="damage-number"
                :key="getHitEffect(getMinionAtPosition(3)!.id)?.timestamp"
              >
                -{{ getHitEffect(getMinionAtPosition(3)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getMinionAtPosition(3)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getMinionAtPosition(3)!.shield && getMinionAtPosition(3)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getMinionAtPosition(3)!.shield }}</text>
                  </view>
                  <view v-if="getMinionAtPosition(3)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getMinionAtPosition(3)!.name, 'ally').color]">
                  {{ iconFor(getMinionAtPosition(3)!.name, 'ally').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getMinionAtPosition(3)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getMinionAtPosition(3)!.health }}</text>
                </view>
              </view>
              <view v-if="getMinionAtPosition(3)!.equipmentNames && getMinionAtPosition(3)!.equipmentNames!.length" class="equipment-dots">
                <text
                  v-for="(eq, idx) in getMinionAtPosition(3)!.equipmentNames"
                  :key="idx"
                  class="equipment-dot"
                  :title="`已装备：${eq}`"
                ></text>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">4</text>
            </view>
          </view>
          <view class="battle-slot player-slot" :class="{ 'occupied': getMinionAtPosition(4) }">
            <view
              v-if="getMinionAtPosition(4)"
              class="character-card ally-card"
              :class="{
                'hit-effect': hasHitEffect(getMinionAtPosition(4)!.id),
                'attacking': isAttacking(getMinionAtPosition(4)!.id)
              }"
              :data-minion-id="getMinionAtPosition(4)!.id"
              :data-attacker-id="getMinionAtPosition(4)!.id"
              @dragover.prevent
              @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(4)!.id })"
            >
              <view
                v-if="isAttacking(getMinionAtPosition(4)!.id)"
                class="character-card-attack-clone"
                :style="getAttackStyle(getMinionAtPosition(4)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(4)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(4)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(4)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(4)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(4)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getMinionAtPosition(4)!.id)"
                class="damage-number"
                :key="getHitEffect(getMinionAtPosition(4)!.id)?.timestamp"
              >
                -{{ getHitEffect(getMinionAtPosition(4)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getMinionAtPosition(4)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getMinionAtPosition(4)!.shield && getMinionAtPosition(4)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getMinionAtPosition(4)!.shield }}</text>
                  </view>
                  <view v-if="getMinionAtPosition(4)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getMinionAtPosition(4)!.name, 'ally').color]">
                  {{ iconFor(getMinionAtPosition(4)!.name, 'ally').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getMinionAtPosition(4)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getMinionAtPosition(4)!.health }}</text>
                </view>
              </view>
              <view v-if="getMinionAtPosition(4)!.equipmentNames && getMinionAtPosition(4)!.equipmentNames!.length" class="equipment-dots">
                <text
                  v-for="(eq, idx) in getMinionAtPosition(4)!.equipmentNames"
                  :key="idx"
                  class="equipment-dot"
                  :title="`已装备：${eq}`"
                ></text>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">5</text>
            </view>
          </view>
          <view class="battle-slot player-slot" :class="{ 'occupied': getMinionAtPosition(5) }">
            <view
              v-if="getMinionAtPosition(5)"
              class="character-card ally-card"
              :class="{
                'hit-effect': hasHitEffect(getMinionAtPosition(5)!.id),
                'attacking': isAttacking(getMinionAtPosition(5)!.id)
              }"
              :data-minion-id="getMinionAtPosition(5)!.id"
              :data-attacker-id="getMinionAtPosition(5)!.id"
              @dragover.prevent
              @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(5)!.id })"
            >
              <view
                v-if="isAttacking(getMinionAtPosition(5)!.id)"
                class="character-card-attack-clone"
                :style="getAttackStyle(getMinionAtPosition(5)!.id)"
              >
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(5)!.name }}</view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(5)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(5)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(5)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(5)!.health }}</text>
                  </view>
                </view>
              </view>
              <view
                v-if="hasHitEffect(getMinionAtPosition(5)!.id)"
                class="damage-number"
                :key="getHitEffect(getMinionAtPosition(5)!.id)?.timestamp"
              >
                -{{ getHitEffect(getMinionAtPosition(5)!.id)?.damage }}
              </view>
              <view class="character-header">
                <view class="character-name">{{ getMinionAtPosition(5)!.name }}</view>
                <view class="header-badges">
                  <view v-if="getMinionAtPosition(5)!.shield && getMinionAtPosition(5)!.shield! > 0" class="shield-badge">
                    <text class="shield-icon">🛡️</text>
                    <text class="shield-value">{{ getMinionAtPosition(5)!.shield }}</text>
                  </view>
                  <view v-if="getMinionAtPosition(5)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                    <text class="status-icon">😴</text>
                  </view>
                </view>
              </view>
              <view class="character-avatar">
                <text :class="['avatar-icon', iconFor(getMinionAtPosition(5)!.name, 'ally').color]">
                  {{ iconFor(getMinionAtPosition(5)!.name, 'ally').emoji }}
                </text>
              </view>
              <view class="character-stats">
                <view class="stat-item attack-stat">
                  <text class="stat-icon">⚔️</text>
                  <text class="stat-value">{{ getMinionAtPosition(5)!.attack }}</text>
                </view>
                <view class="stat-item hp-stat">
                  <text class="stat-icon">❤️</text>
                  <text class="stat-value">{{ getMinionAtPosition(5)!.health }}</text>
                </view>
              </view>
              <view v-if="getMinionAtPosition(5)!.equipmentNames && getMinionAtPosition(5)!.equipmentNames!.length" class="equipment-dots">
                <text
                  v-for="(eq, idx) in getMinionAtPosition(5)!.equipmentNames"
                  :key="idx"
                  class="equipment-dot"
                  :title="`已装备：${eq}`"
                ></text>
              </view>
            </view>
            <view v-else class="empty-slot">
              <text class="slot-hint">6</text>
            </view>
          </view>
        </view>
      </view>
    </view>

      <!-- 玩家信息区域 已移除 -->
    </view>

    <!-- 日志已隐藏 -->
  </main>

<!-- 攻击克隆体层：渲染所有正在攻击的克隆体，保证在最上层显示 -->
<div class="attack-clone-layer" :aria-hidden="true">
  <div
    v-for="(entry, idx) in attackingClones"
    :key="entry[0]"
    class="character-card-attack-clone"
    :class="{
      'enemy-attack-clone': entry[1].isBoss || enemyBoard.some((m: Minion) => m.id === entry[0]),
      'enemy-boss-attack-clone': entry[1].isBoss && !enemyBoard.some((m: Minion) => m.id === entry[0])
    }"
    :style="getAttackStyle(entry[0])"
    :data-attack-id="entry[0]"
  >
    <div class="character-header">
      <div class="character-name">{{ nameForClone(entry[0]) }}</div>
    </div>
    <div class="character-avatar">
      <text class="avatar-icon">⚔️</text>
    </div>
    <div class="character-stats">
      <div class="stat-item attack-stat"><text class="stat-icon">⚔️</text><text class="stat-value"></text></div>
    </div>
  </div>
</div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, getCurrentInstance, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { useGameStore, type Minion } from '@/stores/game'
import { useCampStore } from '@/stores/camp'

// uni-app runtime object (小程序环境)
declare const uni: any

const game = useGameStore()
const campStore = useCampStore()
const gameStore = game as any
// Use direct access instead of storeToRefs to avoid reactivity issues in mini-program
const heroHP = computed(() => gameStore.heroHP)
const enemyHP = computed(() => gameStore.enemyHP)
const board = computed(() => gameStore.board)
const enemyBoard = computed(() => gameStore.enemyBoard)
const enemyHand = computed(() => gameStore.enemyHand)
const enemyDeck = computed(() => gameStore.enemyDeck)
const enemyMana = computed(() => gameStore.enemyMana)
const enemyManaMax = computed(() => gameStore.enemyManaMax)
const enemyDeckExhausted = computed(() => gameStore.enemyDeckExhausted)
const hasEnemyPlayedCards = computed(() => gameStore.hasEnemyPlayedCards)
const currentEnemyId = computed(() => gameStore.currentEnemyId)
const enemyPanel = computed(() => gameStore.enemyPanel)
const turn = computed(() => gameStore.turn)
const logs = computed(() => gameStore.logs)
const mana = computed(() => gameStore.mana)
const manaMax = computed(() => gameStore.manaMax)
const hand = computed(() => gameStore.hand)
const deck = computed(() => gameStore.deck)
const deckExhausted = computed(() => gameStore.deckExhausted)

// 受击反馈状态
const hitEffects = ref<Map<string, { timestamp: number; damage: number }>>(new Map())
const playerHitEffect = ref<{ timestamp: number; damage: number } | null>(null)
const enemyBossHitEffect = ref<{ timestamp: number; damage: number } | null>(null)

// 攻击动画状态 - 存储攻击者的绝对位置信息
const attackingMinions = ref<Record<string, { targetId: string; isBoss: boolean; x: number; y: number; targetX: number; targetY: number }>>({})

// 攻击队列 - 确保攻击动画一个一个进行
const attackQueue = ref<Array<{ attackerId: string; targetId: string | null; isBoss: boolean }>>([])

// 计算攻击位移 - 使用固定定位，计算从攻击者到目标的绝对位置
function calculateAttackOffset(attackerId: string, targetId: string | null, isBoss: boolean, isEnemyAttacker: boolean = false): Promise<{ x: number; y: number; targetX: number; targetY: number }> {
  return new Promise<{ x: number; y: number; targetX: number; targetY: number }>((resolve) => {
    // 小程序环境使用 uni selectorQuery
    if (typeof uni !== 'undefined' && typeof uni.createSelectorQuery === 'function') {
      try {
        const q = uni.createSelectorQuery()
        // 确保选择卡牌元素而不是克隆体，选择器优先匹配 data-minion-id，因为它是卡牌的主要标识
        const attackerSel = `[data-minion-id="${attackerId}"]`
        const targetSel = isBoss
          ? (isEnemyAttacker ? '.ally-zone .player-avatar-container' : '.enemy-zone .player-avatar-container')
          : `[data-minion-id="${targetId}"]`

        const calls: any[] = []
        calls.push(new Promise(resolveSel => {
          q.select(attackerSel).boundingClientRect((rect: any) => resolveSel(rect)).exec()
        }))
        calls.push(new Promise(resolveSel => {
          if (targetSel) q.select(targetSel).boundingClientRect((rect: any) => resolveSel(rect)).exec()
          else resolveSel(null)
        }))

        // 增加延迟确保DOM完全渲染
        setTimeout(async () => {
          try {
            const [a, t] = await Promise.all(calls)
            const aRect = a && a[0] ? a[0] : a
            const tRect = t && t[0] ? t[0] : t

            console.log(`[BattleField] 小程序坐标计算 - 攻击者: ${attackerId}, 目标: ${targetId}, isBoss: ${isBoss}`)
            console.log(`[BattleField] 原始查询结果 - 攻击者:`, a, `目标:`, t)
            console.log(`[BattleField] 处理后矩形 - 攻击者:`, aRect, `目标:`, tRect)
            console.log(`[BattleField] 选择器 - 攻击者: ${attackerSel}, 目标: ${targetSel}`)

            if (aRect && tRect) {
              const attackerX = aRect.left + aRect.width / 2
              const attackerY = aRect.top + aRect.height / 2
              const targetX = tRect.left + tRect.width / 2
              const targetY = tRect.top + tRect.height / 2
              console.log(`[BattleField] 计算坐标 - 攻击者(${attackerX}, ${attackerY}), 目标(${targetX}, ${targetY})`)
              resolve({ x: attackerX, y: attackerY, targetX, targetY })
            } else {
              console.log(`[BattleField] 使用fallback坐标 - attackerId: ${attackerId}, targetId: ${targetId}, isBoss: ${isBoss}, isEnemyAttacker: ${isEnemyAttacker}`)
              console.log(`[BattleField] aRect:`, aRect, `tRect:`, tRect)

              // fallback positions - 上下布局：敌方在上(y小)，玩家在下(y大)
              // 基于典型手机屏幕(400px宽)和卡牌位置(120x120px槽位)
              // 为不同位置的卡牌提供不同的fallback坐标
              let fallbackX = 200, fallbackY = 150, targetFallbackX = 200, targetFallbackY = 400

              if (isBoss) {
                if (isEnemyAttacker) {
                  fallbackX = 200; fallbackY = 150; targetFallbackX = 200; targetFallbackY = 450
                } else {
                  fallbackX = 200; fallbackY = 450; targetFallbackX = 200; targetFallbackY = 150
                }
              } else {
                // 根据卡牌ID尝试推断位置
                if (attackerId && typeof attackerId === 'string') {
                  if (attackerId.includes('enemy')) {
                    fallbackX = 200; fallbackY = 150 // 敌方在上
                  } else {
                    fallbackX = 200; fallbackY = 400 // 玩家在下
                  }
                }
                if (targetId && typeof targetId === 'string') {
                  if (targetId.includes('enemy')) {
                    targetFallbackX = 200; targetFallbackY = 150 // 敌方在上
                  } else {
                    targetFallbackX = 200; targetFallbackY = 400 // 玩家在下
                  }
                }
              }

              // 根据卡牌位置动态计算更准确的fallback坐标
              console.log(`[BattleField] 小程序使用智能fallback - attackerId: ${attackerId}, targetId: ${targetId}`)
              const attackerPosition = getAttackerPosition(attackerId)
              const targetPosition = targetId ? getAttackerPosition(targetId) : null

              if (attackerPosition !== null) {
                fallbackX = attackerPosition.x
                fallbackY = attackerPosition.y
              }
              if (targetPosition !== null) {
                targetFallbackX = targetPosition.x
                targetFallbackY = targetPosition.y
              }

              console.log(`[BattleField] 小程序智能fallback坐标: (${fallbackX}, ${fallbackY}) -> (${targetFallbackX}, ${targetFallbackY})`)
            console.log(`[BattleField] 小程序位置详情 - 攻击者ID: ${attackerId}, 目标ID: ${targetId}, isBoss: ${isBoss}, isEnemyAttacker: ${isEnemyAttacker}`)
              resolve({ x: fallbackX, y: fallbackY, targetX: targetFallbackX, targetY: targetFallbackY })
            }
          } catch (e) {
            // fallback positions - 上下布局：敌方在上(y小)，玩家在下(y大)
            // 基于典型手机屏幕(400px宽)和卡牌位置(120x120px槽位)
            if (isBoss) {
              if (isEnemyAttacker) resolve({ x: 200, y: 150, targetX: 200, targetY: 450 }) // 敌方从上飞向下攻击玩家
              else resolve({ x: 200, y: 450, targetX: 200, targetY: 150 }) // 玩家从下飞向上攻击敌方
            } else {
              if (isEnemyAttacker) resolve({ x: 200, y: 150, targetX: 200, targetY: 400 }) // 敌方从上飞向下攻击玩家随从
              else resolve({ x: 200, y: 400, targetX: 200, targetY: 150 }) // 玩家从下飞向上攻击敌方随从
            }
          }
        }, 50)
      } catch (e) {
        // fallback positions - 上下布局：敌方在上(y小)，玩家在下(y大)
        // 基于典型手机屏幕(400px宽)和卡牌位置(120x120px槽位)
        if (isBoss) {
          if (isEnemyAttacker) resolve({ x: 200, y: 150, targetX: 200, targetY: 450 }) // 敌方从上飞向下攻击玩家
          else resolve({ x: 200, y: 450, targetX: 200, targetY: 150 }) // 玩家从下飞向上攻击敌方
        } else {
          if (isEnemyAttacker) resolve({ x: 200, y: 150, targetX: 200, targetY: 400 }) // 敌方从上飞向下攻击玩家随从
          else resolve({ x: 200, y: 400, targetX: 200, targetY: 150 }) // 玩家从下飞向上攻击敌方随从
        }
      }
    } else {
      // 浏览器环境使用DOM API，增加延迟确保DOM完全渲染
      setTimeout(() => {
        try {
          // 确保选择卡牌元素而不是克隆体，优先匹配 data-minion-id
          let attackerEl = document.querySelector(`[data-minion-id="${attackerId}"]`) as HTMLElement | null

          let targetEl: HTMLElement | null = null
          if (isBoss) {
            targetEl = isEnemyAttacker
              ? document.querySelector('.ally-zone .player-avatar-container') as HTMLElement | null
              : document.querySelector('.enemy-zone .player-avatar-container') as HTMLElement | null
          } else if (targetId) {
            // 确保选择卡牌元素而不是克隆体
            targetEl = document.querySelector(`[data-minion-id="${targetId}"]`) as HTMLElement | null
          }

          console.log(`[BattleField] 浏览器坐标计算 - 攻击者: ${attackerId}, 目标: ${targetId}, isBoss: ${isBoss}`)
          console.log(`[BattleField] 攻击者元素:`, attackerEl, `目标元素:`, targetEl)
          console.log(`[BattleField] 攻击者是否正在攻击:`, isAttacking(attackerId))
          console.log(`[BattleField] 选择器 - 攻击者: [data-minion-id="${attackerId}"], 目标: ${targetId ? `[data-minion-id="${targetId}"]` : (isBoss ? (isEnemyAttacker ? '.ally-zone .player-avatar-container' : '.enemy-zone .player-avatar-container') : 'null')}`)

          if (attackerEl && targetEl) {
            const attackerRect = attackerEl.getBoundingClientRect()
            const targetRect = targetEl.getBoundingClientRect()
            console.log(`[BattleField] 攻击者边界矩形:`, attackerRect, `目标边界矩形:`, targetRect)
            const attackerX = attackerRect.left + attackerRect.width / 2
            const attackerY = attackerRect.top + attackerRect.height / 2
            const targetX = targetRect.left + targetRect.width / 2
            const targetY = targetRect.top + targetRect.height / 2
            console.log(`[BattleField] 计算坐标 - 攻击者(${attackerX}, ${attackerY}), 目标(${targetX}, ${targetY})`)
            resolve({ x: attackerX, y: attackerY, targetX, targetY })
          } else {
            console.log(`[BattleField] 浏览器使用fallback坐标 - attackerId: ${attackerId}, targetId: ${targetId}, isBoss: ${isBoss}, isEnemyAttacker: ${isEnemyAttacker}`)
            console.log(`[BattleField] attackerEl:`, attackerEl, `targetEl:`, targetEl)

            // fallback positions - 上下布局：敌方在上(y小)，玩家在下(y大)
            // 基于典型手机屏幕(400px宽)和卡牌位置(120x120px槽位)
            // 为不同位置的卡牌提供不同的fallback坐标
            let fallbackX = 200, fallbackY = 150, targetFallbackX = 200, targetFallbackY = 400

            if (isBoss) {
              if (isEnemyAttacker) {
                fallbackX = 200; fallbackY = 150; targetFallbackX = 200; targetFallbackY = 450
              } else {
                fallbackX = 200; fallbackY = 450; targetFallbackX = 200; targetFallbackY = 150
              }
            } else {
              // 根据卡牌ID尝试推断位置
              if (attackerId && typeof attackerId === 'string') {
                if (attackerId.includes('enemy')) {
                  fallbackX = 200; fallbackY = 150 // 敌方在上
                } else {
                  fallbackX = 200; fallbackY = 400 // 玩家在下
                }
              }
              if (targetId && typeof targetId === 'string') {
                if (targetId.includes('enemy')) {
                  targetFallbackX = 200; targetFallbackY = 150 // 敌方在上
                } else {
                  targetFallbackX = 200; targetFallbackY = 400 // 玩家在下
                }
              }
            }

            // 根据卡牌位置动态计算更准确的fallback坐标
            console.log(`[BattleField] 浏览器使用智能fallback - attackerId: ${attackerId}, targetId: ${targetId}`)
            const attackerPosition = getAttackerPosition(attackerId)
            const targetPosition = targetId ? getAttackerPosition(targetId) : null

            if (attackerPosition !== null) {
              fallbackX = attackerPosition.x
              fallbackY = attackerPosition.y
            }
            if (targetPosition !== null) {
              targetFallbackX = targetPosition.x
              targetFallbackY = targetPosition.y
            }

            console.log(`[BattleField] 浏览器智能fallback坐标: (${fallbackX}, ${fallbackY}) -> (${targetFallbackX}, ${targetFallbackY})`)
            console.log(`[BattleField] 浏览器位置详情 - 攻击者ID: ${attackerId}, 目标ID: ${targetId}, isBoss: ${isBoss}, isEnemyAttacker: ${isEnemyAttacker}`)
            resolve({ x: fallbackX, y: fallbackY, targetX: targetFallbackX, targetY: targetFallbackY })
          }
        } catch (e) {
          // fallback positions - 上下布局：敌方在上(y小)，玩家在下(y大)
          if (isBoss) {
            if (isEnemyAttacker) resolve({ x: 400, y: 200, targetX: 400, targetY: 600 }) // 敌方从上飞向下攻击玩家
            else resolve({ x: 400, y: 600, targetX: 400, targetY: 200 }) // 玩家从下飞向上攻击敌方
          } else {
            if (isEnemyAttacker) resolve({ x: 400, y: 200, targetX: 400, targetY: 500 }) // 敌方从上飞向下攻击玩家随从
            else resolve({ x: 400, y: 500, targetX: 400, targetY: 200 }) // 玩家从下飞向上攻击敌方随从
          }
        }
      }, 50)
    }
  })
}

// 执行攻击动画
async function executeAttackAnimation(attackerId: string, targetId: string | null, isBoss: boolean, isEnemyAttacker: boolean) {
  console.log(`[BattleField] 开始执行攻击动画: ${attackerId}, 目标: ${targetId}, isBoss: ${isBoss}, isEnemy: ${isEnemyAttacker}`)

  // 获取攻击位置信息
  const positions = await calculateAttackOffset(attackerId, targetId || null, isBoss, isEnemyAttacker)
  console.log(`[BattleField] 攻击位置计算完成:`, positions)

  // 通过新对象触发响应式更新，确保动画立即生效
  attackingMinions.value = {
    ...attackingMinions.value,
    [attackerId]: {
      targetId: targetId || '',
      isBoss: isBoss,
      x: positions.x,
      y: positions.y,
      targetX: positions.targetX,
      targetY: positions.targetY
    }
  }
  console.log(`[BattleField] 攻击状态已设置，当前攻击数量: ${Object.keys(attackingMinions.value).length}`)

  // 等待DOM更新，确保动画能正确开始
  await nextTick()

  if (isEnemyAttacker) {
    const setTime = Date.now()
    console.log(`[BattleField] 敌人卡牌攻击动画已设置: ${attackerId} 在 ${setTime}, 位置:`, positions)
  }

  console.log(`[BattleField] 攻击动画设置完成，等待CSS动画执行`)
}

// 监听攻击事件
async function handleAttackStart(event: any) {
  console.log(`[BattleField] 收到攻击开始事件:`, event.detail)
  const { attackerId, targetId, isBoss } = event.detail || event
  if (attackerId && (targetId || isBoss)) {
    // 跳过敌人本体攻击（不需要动画）
    if (attackerId === 'enemy-boss') {
      console.log(`[BattleField] 跳过敌人本体攻击: ${attackerId}`)
      return
    }

    // 判断是否是敌人攻击（通过检查 attackerId 是否在 enemyBoard 中）
    const isEnemyAttacker = enemyBoard.value.some((m: Minion) => m.id === attackerId)

    if (isEnemyAttacker) {
      const eventTime = Date.now()
      console.log(`[BattleField] 敌人卡牌攻击动画开始: ${attackerId} 在 ${eventTime}`)
    }

    // 检查是否已经有其他攻击正在进行，如果有则加入队列等待
    if (Object.keys(attackingMinions.value).length > 0) {
      console.log(`[BattleField] 检测到其他攻击正在进行，将 ${attackerId} 加入攻击队列等待`)
      attackQueue.value.push({ attackerId, targetId, isBoss: !!isBoss })
      return
    }

    // 开始执行攻击动画
    console.log(`[BattleField] 开始执行攻击动画: ${attackerId}`)
    await executeAttackAnimation(attackerId, targetId, !!isBoss, isEnemyAttacker)
    await nextTick()
    try {
      console.debug('[BattleField] triggering animateAttackClones after nextTick', { attackerId })
      animateAttackClones()
    } catch (e) {
      console.warn('[BattleField] animateAttackClones call failed', e)
    }

    if (isEnemyAttacker) {
      const setTime = Date.now()
      console.log(`[BattleField] 敌人卡牌攻击动画已设置: ${attackerId} 在 ${setTime}`)
    }
  }
}

function handleAttackEnd(event: any) {
  console.log(`[BattleField] 收到攻击结束事件:`, event.detail)
  const { attackerId } = event.detail || event
  if (attackerId) {
    const endTime = Date.now()
    const isEnemyAttacker = enemyBoard.value.some((m: Minion) => m.id === attackerId)
    if (isEnemyAttacker) {
      console.log(`[BattleField] 敌人卡牌攻击动画结束事件: ${attackerId} 在 ${endTime}`)
      console.log(`[BattleField] 当前正在进行的攻击数量: ${Object.keys(attackingMinions.value).length}`)
    }

    const next = { ...attackingMinions.value }
    delete next[attackerId]
    attackingMinions.value = next

    if (isEnemyAttacker) {
      console.log(`[BattleField] 攻击已从 attackingMinions 中移除: ${attackerId}, 剩余攻击数: ${Object.keys(attackingMinions.value).length}`)
    }

    // 检查队列中是否有等待的攻击，如果有则执行下一个
    if (attackQueue.value.length > 0) {
      const nextAttack = attackQueue.value.shift()
      if (nextAttack) {
        console.log(`[BattleField] 执行队列中的下一个攻击: ${nextAttack.attackerId}`)
        const isEnemyAttackerNext = enemyBoard.value.some((m: Minion) => m.id === nextAttack.attackerId)
        executeAttackAnimation(nextAttack.attackerId, nextAttack.targetId, nextAttack.isBoss, isEnemyAttackerNext)
      }
    } else {
      console.log(`[BattleField] 攻击队列为空，无下一个攻击`)
    }
  }
}

// 检查是否正在攻击
function isAttacking(minionId: string): boolean {
  return minionId in attackingMinions.value
}

function getAttackTarget(minionId: string): { targetId: string; isBoss: boolean; x: number; y: number; targetX: number; targetY: number } | undefined {
  return attackingMinions.value[minionId]
}

// 获取攻击样式 - 使用固定定位跳出方框
function getAttackStyle(minionId: string): string {
  const target = getAttackTarget(minionId)
  if (!target) return ''
  console.log(`[BattleField] getAttackStyle - ${minionId} 原始坐标: (${target.x.toFixed(1)}, ${target.y.toFixed(1)}) -> (${target.targetX.toFixed(1)}, ${target.targetY.toFixed(1)})`)

  // 克隆体有负边距 margin-left: -50px; margin-top: -70px;
  // 这意味着视觉中心相对于 left/top 有偏移
  // 所以要让视觉中心在 (x,y) 位置，需要设置 left = x - 50, top = y - 70
  const visualOffsetX = -50
  const visualOffsetY = -70

  const startX = target.x + visualOffsetX
  const startY = target.y + visualOffsetY
  const endX = target.targetX + visualOffsetX
  const endY = target.targetY + visualOffsetY

  console.log(`[BattleField] getAttackStyle - ${minionId} 最终坐标: (${startX.toFixed(1)}, ${startY.toFixed(1)}) -> (${endX.toFixed(1)}, ${endY.toFixed(1)})`)

  return `
    position: fixed;
    left: ${startX}px;
    top: ${startY}px;
    --start-x: ${startX}px;
    --start-y: ${startY}px;
    --target-x: ${endX}px;
    --target-y: ${endY}px;
    z-index: 10000;
  `
}

// 监听受击事件
function handleEnemyHit(event: CustomEvent) {
  const { minionId, damage, beforeHealth, afterHealth } = event.detail
  const next = new Map(hitEffects.value)
  next.set(minionId, { timestamp: Date.now(), damage })
  hitEffects.value = next
  // 3秒后清除效果
  setTimeout(() => {
    const cleared = new Map(hitEffects.value)
    cleared.delete(minionId)
    hitEffects.value = cleared
  }, 3000)

  // 如果有血量变化信息，可以用于显示更详细的反馈
  if (beforeHealth !== undefined && afterHealth !== undefined) {
    console.log(`[BattleField] ${minionId} 受击: ${beforeHealth} -> ${afterHealth} (伤害: ${damage})`)
  }
}

function handleAllyHit(event: CustomEvent) {
  const { minionId, damage } = event.detail
  const next = new Map(hitEffects.value)
  next.set(minionId, { timestamp: Date.now(), damage })
  hitEffects.value = next
  setTimeout(() => {
    const cleared = new Map(hitEffects.value)
    cleared.delete(minionId)
    hitEffects.value = cleared
  }, 3000)
}

function handlePlayerHit(event: CustomEvent) {
  const { damage } = event.detail
  playerHitEffect.value = { timestamp: Date.now(), damage }
  setTimeout(() => {
    playerHitEffect.value = null
  }, 2000)
}

function handleEnemyBossHit(event: CustomEvent) {
  const { damage } = event.detail
  enemyBossHitEffect.value = { timestamp: Date.now(), damage }
  setTimeout(() => {
    enemyBossHitEffect.value = null
  }, 2000)
}

// 检查是否有受击效果
function hasHitEffect(minionId: string): boolean {
  return hitEffects.value.has(minionId)
}

function getHitEffect(minionId: string): { timestamp: number; damage: number } | undefined {
  return hitEffects.value.get(minionId)
}

// 在小程序环境中完全不使用全局事件系统，所有通信通过直接方法调用
// onMounted(() => {
//   // 不再需要事件监听器，所有逻辑通过 emitEvent 和直接方法调用处理
// })

// 在小程序环境中完全不使用全局事件系统
// onUnmounted(() => {
//   // 不再需要移除事件监听器
// })

// 获取用于克隆体渲染的名字（优先 board/enemyBoard/敌方面板）
function nameForClone(attackerId: string) {
  const ally = board.value.find((m: Minion) => m.id === attackerId)
  if (ally) return ally.name
  const enemy = enemyBoard.value.find((m: Minion) => m.id === attackerId)
  if (enemy) return enemy.name
  if (enemyPanel.value && (enemyPanel.value as any).id === attackerId) return (enemyPanel.value as any).name || '敌方'
  return attackerId
}

// expose a computed array for template rendering (avoids using Array.from in template)
const attackingClones = computed(() => Object.entries(attackingMinions.value))

// 初始化时验证所有位置坐标
validateAllPositions()

// 根据attackerId估算卡牌位置（用于fallback坐标计算）
function getAttackerPosition(attackerId: string): { x: number; y: number } | null {
  console.log(`[BattleField] getAttackerPosition: ${attackerId}`)

  // 检查敌方卡牌
  const enemyMinion = enemyBoard.value.find((m: Minion) => m.id === attackerId)
  if (enemyMinion) {
    console.log(`[BattleField] 找到敌方卡牌: ${attackerId}, position: ${enemyMinion.position}`)
    const pos = getPositionBySlot(enemyMinion.position, true)
    console.log(`[BattleField] 敌方卡牌 ${attackerId} 位置 ${enemyMinion.position} 坐标: (${pos.x}, ${pos.y})`)
    return pos
  }

  // 检查玩家卡牌
  const playerMinion = board.value.find((m: Minion) => m.id === attackerId)
  if (playerMinion) {
    console.log(`[BattleField] 找到玩家卡牌: ${attackerId}, position: ${playerMinion.position}`)
    const pos = getPositionBySlot(playerMinion.position, false)
    console.log(`[BattleField] 玩家卡牌 ${attackerId} 位置 ${playerMinion.position} 坐标: (${pos.x}, ${pos.y})`)
    return pos
  }

  console.log(`[BattleField] 未找到卡牌: ${attackerId}`)
  return null
}

// 验证所有位置的坐标计算（用于调试）
function validateAllPositions() {
  console.log('[BattleField] === 验证所有位置坐标 ===')
  for (let pos = 0; pos < 6; pos++) {
    const enemyPos = getPositionBySlot(pos, true)
    const playerPos = getPositionBySlot(pos, false)
    console.log(`位置 ${pos}: 敌方(${enemyPos.x}, ${enemyPos.y}) 玩家(${playerPos.x}, ${playerPos.y})`)
  }
  console.log('[BattleField] === 验证完成 ===')
}

// 根据slot位置估算卡牌中心坐标
function getPositionBySlot(position: number, isEnemy: boolean): { x: number; y: number } {
  // 简化坐标计算，使用更合理的估算值
  // 基于实际观察到的卡牌位置进行估算

  const col = position % 3
  const row = Math.floor(position / 3)

  // X坐标：3列布局，列宽约130px
  const colWidth = 130
  const x = col * colWidth + 65 // 列中心

  // Y坐标：敌方和玩家区域分离
  let y = 0
  if (isEnemy) {
    // 敌方：上方区域
    y = row === 0 ? 140 : 200 // 前排/后排
  } else {
    // 玩家：下方区域
    y = row === 0 ? 350 : 410 // 前排/后排
  }

  console.log(`[BattleField] 位置 ${position} (${isEnemy ? '敌方' : '玩家'}) 坐标: (${x}, ${y})`)
  return { x, y }
}

// 小程序环境动画实现
function animateAttackClones() {
  if (typeof uni !== 'undefined') {
    // 小程序环境：使用简化的定时器动画
    for (const [attackerId, meta] of attackingClones.value) {
      try {
        // 在小程序环境中，我们使用定时器来模拟动画
        // 直接设置最终位置，让CSS动画处理移动
        setTimeout(() => {
          // 动画结束后清除状态
          const next = { ...attackingMinions.value }
          delete next[attackerId]
          attackingMinions.value = next

          // 处理队列中的下一个攻击
          if (attackQueue.value.length > 0) {
            const nextAttack = attackQueue.value.shift()
            if (nextAttack) {
              executeAttackAnimation(nextAttack.attackerId, nextAttack.targetId, nextAttack.isBoss, enemyBoard.value.some((m: Minion) => m.id === nextAttack.attackerId))
            }
          }
        }, 900) // 900ms后清除
      } catch (e) {
        console.warn('[BattleField] Miniapp animation failed for', attackerId, e)
      }
    }
  } else {
    // 浏览器环境：使用原来的JS动画实现
    for (const [attackerId, meta] of attackingClones.value) {
      try {
        const el = document.querySelector(`[data-attack-id="${attackerId}"]`) as HTMLElement | null
        if (!el) continue
        const elEl = el

        const startX = meta.x || 0
        const startY = meta.y || 0
        const targetX = meta.targetX || startX + 200
        const targetY = meta.targetY || startY

        elEl.style.position = 'fixed'
        elEl.style.left = `${startX}px`
        elEl.style.top = `${startY}px`
        elEl.style.opacity = '1'
        elEl.style.transition = 'none'

        const duration = 900
        const startTime = performance.now()

        function step(now: number) {
          const t = Math.min(1, (now - startTime) / duration)
          const eased = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t
          const curX = startX + (targetX - startX) * eased
          const curY = startY + (targetY - startY) * eased

          try {
            elEl.style.left = `${curX}px`
            elEl.style.top = `${curY}px`
          } catch (e) {}

          if (t < 1) {
            requestAnimationFrame(step)
          } else {
            try {
              elEl.style.transition = 'opacity 160ms linear'
              elEl.style.opacity = '0'
            } catch (e) {}
          }
        }

        requestAnimationFrame(step)
      } catch (e) {
        console.warn('[BattleField] Browser animation failed for', attackerId, e)
      }
    }
  }
}

// watch attackingClones to trigger JS animation fallback
watch(attackingClones, (newVal, oldVal) => {
  if (newVal.length > 0) {
    // trigger after DOM updates
    setTimeout(() => animateAttackClones(), 30)
  }
})

// 调试：监听 enemyPanel 变化
watch(enemyPanel, (newVal) => {
  console.log('[BattleField] enemyPanel 变化:', newVal)
}, { immediate: true, deep: true })

const props = defineProps<{
  draggingEquipCard: import('@/stores/game').Card | null
}>()

const emit = defineEmits<{
  (e: 'equip-to-minion', payload: { minionId: string; cardId?: string }): void
  (e: 'deploy-card', payload: { cardId: string; position: number }): void
  (e: 'play-card', payload: string): void
}>()

// Expose a method to resolve touch-drag drop from parent (mini-program compatible)
async function resolveTouchDrop(payload: { cardId: string; cardType: string; x: number; y: number; canAfford: boolean }) {
  const { cardId, cardType, x, y, canAfford } = payload
  // Prefer DOM-based detection of slot positions so deployment matches visual slots.
  // Falls back to calculated grid if DOM query fails.
  let slots: any[] = []

  // Try uni-app selectorQuery first (for mini-program compatibility)
  if (typeof uni !== 'undefined' && typeof uni.createSelectorQuery === 'function') {
    try {
      const query = uni.createSelectorQuery()
      const slotEls = await new Promise<any[]>((resolve) => {
        query.selectAll('.player-slots .battle-slot').boundingClientRect((data: any) => {
          resolve(data || [])
        }).exec()
      })
      if (slotEls && slotEls.length === 6) {
        slots = slotEls.map((rect: any, i: number) => ({
          left: rect.left,
          top: rect.top,
          width: rect.width,
          height: rect.height,
          index: i
        }))
      }
    } catch (e) {
      console.warn('[BattleField] uni selectorQuery failed, falling back to DOM query', e)
    }
  }

  // Fallback to DOM query (for web environment)
  if (slots.length === 0) {
    try {
      const slotEls = Array.from(document.querySelectorAll('.player-slots .battle-slot')) as HTMLElement[]
      if (slotEls.length === 6) {
        slots = slotEls.map((el, i) => {
          const r = el.getBoundingClientRect()
          return {
            left: r.left,
            top: r.top,
            width: r.width,
            height: r.height,
            index: i
          }
        })
      }
    } catch (e) {
      console.warn('[BattleField] DOM slot query failed, falling back to estimated slots', e)
    }
  }

  // Fallback estimated layout if DOM-based detection not available
  if (slots.length === 0) {
    const slotWidth = 120
    const slotHeight = 120
    const gap = 12
    const padding = 16
    const battleAreaTop = 0
    const playerSlotsTop = battleAreaTop + (slotHeight + gap) * 2 + padding
    for (let row = 0; row < 2; row++) {
      for (let col = 0; col < 3; col++) {
        const slotIndex = row * 3 + col
        const left = col * (slotWidth + gap) + padding
        const top = playerSlotsTop + row * (slotHeight + gap)
        slots.push({
          left,
          top,
          width: slotWidth,
          height: slotHeight,
          index: slotIndex
        })
      }
    }
  }

  // Estimate hand rect near bottom of battle area as a fallback
  const handRect = {
    left: 16,
    top: (slots.length ? Math.max(...slots.map(s => s.top)) + 140 : window.innerHeight - 260),
    width: 390,
    height: 160
  }

  const inHand = handRect && x >= handRect.left && x <= handRect.left + handRect.width && y >= handRect.top && y <= handRect.top + handRect.height

  // Spell: released outside hand -> play
  if (cardType === 'spell' && !inHand && canAfford) {
    emit('deploy-card', { cardId, position: -1 })
    return
  }

  for (let i = 0; i < slots.length; i++) {
    const s = slots[i]
    console.log(`[BattleField] Checking slot ${i}: touch(${x},${y}) vs slot(${s.left},${s.top},${s.width}x${s.height})`)
    if (x >= s.left && x <= s.left + s.width && y >= s.top && y <= s.top + s.height) {
      const occupied = getMinionAtPosition(s.index)
      console.log(`[BattleField] Touch hit slot ${i}, occupied:`, occupied ? occupied.id : 'none')
      if (cardType === 'character' && !occupied) {
        console.log(`[BattleField] Deploying character card ${cardId} to position ${s.index}`)
        emit('deploy-card', { cardId, position: s.index })
        return
      }
      if (cardType === 'equipment' && occupied) {
        console.log(`[BattleField] Equipping card ${cardId} to minion ${occupied.id}`)
        console.log(`[BattleField] Emitting equip-to-minion event`)
        emit('equip-to-minion', { minionId: occupied.id, cardId: cardId })
        return
      }
      if (cardType === 'spell' && !inHand && canAfford) {
        emit('deploy-card', { cardId, position: -1 })
        return
      }
    }
  }

  if (cardType === 'equipment') {
    for (let i = 0; i < 6; i++) {
      const occupied = getMinionAtPosition(i)
      if (occupied) {
        const slot = slots.find(s => s.index === i)
        if (slot && x >= slot.left && x <= slot.left + slot.width && y >= slot.top && y <= slot.top + slot.height) {
          emit('equip-to-minion', { minionId: occupied.id })
          return
        }
      }
    }
  }

  if (cardType === 'spell' && !inHand && canAfford) {
    emit('deploy-card', { cardId, position: -1 })
  }
}

// 暴露给外部调用的攻击方法
function triggerAttackAnimation(attackerId: string, targetId: string | null, isBoss: boolean) {
  console.log(`[BattleField] 外部调用攻击动画: ${attackerId}, targetId: ${targetId}, isBoss: ${isBoss}`)
  handleAttackStart({ detail: { attackerId, targetId, isBoss } } as any)
}

// expose to parent
defineExpose({ resolveTouchDrop, onCardTouchStart, onCardTouchMove, onCardTouchEnd, triggerAttackAnimation })

// touch drag state (component-local, more reliable)
const touchState = {
  startX: 0,
  startY: 0,
  moved: false,
  cardId: '' as string | null,
  cardType: '' as string | null,
  srcEl: null as HTMLElement | null
}

// drag clone element that follows touch/mouse during drag
let dragCloneEl: HTMLElement | null = null
let dragCloneOffsetX = 0
let dragCloneOffsetY = 0

function createDragClone(cardId: string, clientX: number, clientY: number) {
  // In miniapp (微信/支付宝/etc) environments DOM APIs are limited.
  // If running inside a miniapp, skip DOM-based clone and let page-level fallback handle visuals.
  try {
    if (typeof (globalThis as any).wx !== 'undefined' || typeof (globalThis as any).my !== 'undefined' || typeof (globalThis as any).swan !== 'undefined') {
      console.log('[BattleField] miniapp environment detected - skipping DOM clone for cardId=', cardId)
      return
    }
    removeDragClone()
    const src = (touchState.srcEl as HTMLElement) || (typeof document !== 'undefined' ? (document.querySelector(`.fan-card[data-card-id="${cardId}"]`) || document.querySelector(`.fan-card[data-card-id='${cardId}']`)) : null)
    if (!src) {
      console.warn('[BattleField] createDragClone: source element not available for cardId=', cardId)
      return
    }
    console.log('[BattleField] createDragClone - found source element for cardId=', cardId)
    // Ensure we have an actual HTMLElement (handle Vue component instance proxies)
    let el: any = src
    if (typeof el.getBoundingClientRect !== 'function') {
      if (el.$el && typeof el.$el.getBoundingClientRect === 'function') {
        el = el.$el
      } else if (el.el && typeof el.el.getBoundingClientRect === 'function') {
        el = el.el
      } else if ((el as HTMLElement).nodeType === 1) {
        el = el as HTMLElement
      } else {
        // fallback: try querying DOM by data attribute
        const fallback = document.querySelector(`.fan-card[data-card-id="${cardId}"]`) || document.querySelector(`.fan-card[data-card-id='${cardId}']`)
        if (fallback && typeof (fallback as HTMLElement).getBoundingClientRect === 'function') {
          el = fallback as HTMLElement
        } else {
          console.warn('[BattleField] createDragClone: could not resolve HTMLElement for src, cardId=', cardId, 'src=', src)
          return
        }
      }
    }
    const rect = (el as HTMLElement).getBoundingClientRect()
    const clone = (el as HTMLElement).cloneNode(true) as HTMLElement
    clone.style.position = 'fixed'
    clone.style.left = '0px'
    clone.style.top = '0px'
    clone.style.width = rect.width + 'px'
    clone.style.height = rect.height + 'px'
    clone.style.zIndex = '9999'
    clone.style.pointerEvents = 'none'
    clone.style.opacity = '0.95'
    clone.style.transform = 'translate(-50%, -50%) scale(1.05)'
    clone.style.transition = 'transform 0.05s linear'
    document.body.appendChild(clone)
    dragCloneEl = clone
    // record offset so cursor is near center
    dragCloneOffsetX = rect.width / 2
    dragCloneOffsetY = rect.height / 2
    moveDragClone(clientX, clientY)
  } catch (e) {
    console.warn('[BattleField] createDragClone failed for cardId=', cardId, 'err=', e)
  }
}

function moveDragClone(clientX: number, clientY: number) {
  if (!dragCloneEl) return
  // debug
  // console.log('[BattleField] moveDragClone to', clientX, clientY)
  const x = clientX - dragCloneOffsetX
  const y = clientY - dragCloneOffsetY
  dragCloneEl.style.transform = `translate(${x}px, ${y}px) scale(1.05)`
}

function removeDragClone() {
  if (dragCloneEl && dragCloneEl.parentElement) {
    try {
      dragCloneEl.parentElement.removeChild(dragCloneEl)
      console.log('[BattleField] removeDragClone removed element')
    } catch (e) {
      console.warn('[BattleField] removeDragClone failed', e)
    }
  }
  dragCloneEl = null
}

function onCardTouchStart(cardId: string, cardType: string, e: any) {
  if (!e.touches || e.touches.length === 0) return
  console.log('[BattleField] onCardTouchStart', { cardId, cardType, x: e.touches[0].clientX, y: e.touches[0].clientY })
  touchState.startX = e.touches[0].clientX
  touchState.startY = e.touches[0].clientY
  touchState.moved = false
  touchState.cardId = cardId
  touchState.cardType = cardType
  // record source element reference if provided (desktop or web)
  try {
    touchState.srcEl = (e && (e.currentTarget as HTMLElement)) || (e && (e.target as HTMLElement)) || null
  } catch (err) {
    touchState.srcEl = null
  }
}

function onCardTouchMove(cardId: string, e: any) {
  if (!touchState.cardId || touchState.cardId !== cardId) return
  if (!e.touches || e.touches.length === 0) return
  const dx = Math.abs(e.touches[0].clientX - touchState.startX)
  const dy = Math.abs(e.touches[0].clientY - touchState.startY)
  if (dx > 8 || dy > 8) touchState.moved = true

  // create or move drag clone to follow finger
  if (touchState.moved) {
    const tx = e.touches[0].clientX
    const ty = e.touches[0].clientY
    if (!dragCloneEl) {
      createDragClone(cardId, tx, ty)
    } else {
      moveDragClone(tx, ty)
    }
  }
}

function onCardTouchEnd(cardId: string, cardType: string, e: any) {
  if (!touchState.cardId || touchState.cardId !== cardId) {
    touchState.cardId = null
    touchState.cardType = null
    touchState.moved = false
    try { removeDragClone() } catch (e) {}
    return
  }
  if (!touchState.moved) {
    touchState.cardId = null
    touchState.cardType = null
    touchState.moved = false
    try { removeDragClone() } catch (e) {}
    return
  }

  const touch = (e.changedTouches && e.changedTouches[0]) || null
  if (!touch) {
    touchState.cardId = null
    touchState.cardType = null
    touchState.moved = false
    try { removeDragClone() } catch (e) {}
    return
  }

  // Use calculated slot positions instead of selectorQuery (小程序兼容)
  console.log('[BattleField] Using calculated positions for touch detection')

  // Try using refs to get positions if selectorQuery fails
  const slots: any[] = []
  const chars: any[] = []

  // For player slots - get their positions via refs if available
  // Since we can't easily get refs of dynamically generated elements,
  // let's use a different approach: check if touch coordinates fall within known slot areas

  // Define slot positions based on grid layout (3x2 grid)
  const slotWidth = 120  // from CSS
  const slotHeight = 120 // from CSS
  const gap = 12         // from CSS
  const padding = 16     // from CSS

  // Player slots are in the bottom half of battle area
  const battleAreaTop = 0 // We'll need to calculate this relative to viewport
  const playerSlotsTop = battleAreaTop + (slotHeight + gap) * 2 + padding // Bottom row

  console.log('[BattleField] Using calculated slot positions for touch detection')

  // Calculate slot positions (assuming 3x2 grid, player slots in bottom 2 rows)
  // First try DOM-based detection for accuracy
  try {
    const slotEls = Array.from(document.querySelectorAll('.player-slots .battle-slot')) as HTMLElement[]
    if (slotEls.length === 6) {
      for (let i = 0; i < slotEls.length; i++) {
        const el = slotEls[i]
        if (!el) continue
        const r = el.getBoundingClientRect()
        slots.push({
          left: r.left,
          top: r.top,
          width: r.width,
          height: r.height,
          index: i
        })
      }
    } else {
      // Fallback to calculated grid if DOM nodes aren't found
      for (let row = 0; row < 2; row++) {
        for (let col = 0; col < 3; col++) {
          const slotIndex = row * 3 + col
          const left = col * (slotWidth + gap) + padding
          const top = playerSlotsTop + row * (slotHeight + gap)
          slots.push({
            left,
            top,
            width: slotWidth,
            height: slotHeight,
            index: slotIndex
          })
        }
      }
    }
  } catch (e) {
    // DOM query failed; fallback to calculated grid
    for (let row = 0; row < 2; row++) {
      for (let col = 0; col < 3; col++) {
        const slotIndex = row * 3 + col
        const left = col * (slotWidth + gap) + padding
        const top = playerSlotsTop + row * (slotHeight + gap)
        slots.push({
          left,
          top,
          width: slotWidth,
          height: slotHeight,
          index: slotIndex
        })
      }
    }
  }

  console.log('[BattleField] calculated slots:', slots)

  // For hand area, assume it's below the battle area
  const handRect = {
    left: 16,
    top: playerSlotsTop + 2 * (slotHeight + gap) + 40, // Rough estimate
    width: 390,
    height: 220
  }

  console.log('[BattleField] using estimated handRect:', handRect)

  const x = touch.clientX
  const y = touch.clientY
  const inHand = handRect && x >= handRect.left && x <= handRect.left + handRect.width && y >= handRect.top && y <= handRect.top + handRect.height

  console.log(`[BattleField] touch pos: (${x}, ${y}), inHand: ${inHand}`)

  // Spell released outside hand -> play
  if (cardType === 'spell' && !inHand) {
    console.log('[BattleField] playing spell card:', cardId)
    emit('play-card', cardId)
    touchState.cardId = null
    touchState.cardType = null
    touchState.moved = false
    return
  }

  // Check player slots
  for (let i = 0; i < slots.length; i++) {
    const s = slots[i]
    console.log(`[BattleField] checking slot ${i}`, s, 'touch pos:', x, y)
    if (x >= s.left && x <= s.left + s.width && y >= s.top && y <= s.top + s.height) {
      const occupied = getMinionAtPosition(s.index)
      console.log(`[BattleField] slot ${i} (pos ${s.index}) occupied:`, occupied)
      if (cardType === 'character' && !occupied) {
        console.log(`[BattleField] deploying character ${cardId} to position ${s.index}`)
        emit('deploy-card', { cardId, position: s.index })
        touchState.cardId = null
        touchState.cardType = null
        touchState.moved = false
        return
      }
      if (cardType === 'equipment' && occupied) {
        console.log(`[BattleField] equipping ${cardId} to minion ${occupied.id}`)
        emit('equip-to-minion', { minionId: occupied.id })
        touchState.cardId = null
        touchState.cardType = null
        touchState.moved = false
        return
      }
      if (cardType === 'spell' && !inHand) {
        console.log('[BattleField] playing spell card:', cardId)
        emit('play-card', cardId)
        touchState.cardId = null
        touchState.cardType = null
        touchState.moved = false
        return
      }
    }
  }

  // For equipment, also check if touch is over any existing character (simplified approach)
  if (cardType === 'equipment') {
    // Check all player slots to see if any have characters
    for (let i = 0; i < 6; i++) {
      const occupied = getMinionAtPosition(i)
      if (occupied) {
        // Check if touch is roughly over this slot area
        const slot = slots.find(s => s.index === i)
        if (slot && x >= slot.left && x <= slot.left + slot.width && y >= slot.top && y <= slot.top + slot.height) {
          console.log(`[BattleField] equipping ${cardId} to minion ${occupied.id} at slot ${i}`)
          emit('equip-to-minion', { minionId: occupied.id })
          touchState.cardId = null
          touchState.cardType = null
          touchState.moved = false
          return
        }
      }
    }
  }

  // Fallback for spell outside hand
  if (cardType === 'spell' && !inHand) {
    console.log('[BattleField] playing spell card (fallback):', cardId)
    emit('play-card', cardId)
  }

  touchState.cardId = null
  touchState.cardType = null
  touchState.moved = false
}

// 从营地数据获取最大血量，如果没有则使用默认值100
const heroHPMax = computed(() => campStore.playerCharacter?.maxHp || 100)
// 敌人最大HP从敌人面板获取，如果没有则使用默认值100
const enemyHPMax = computed(() => enemyPanel.value?.hp || 100)
// 获取玩家角色名称
const playerCharacterName = computed(() => campStore.playerCharacter?.playerCharacterName || '冒险者')
// 获取玩家角色图标
const playerCharacterIcon = computed(() => {
  const name = playerCharacterName.value
  return iconFor(name, 'ally')
})
// 获取敌方角色名称
const enemyCharacterName = computed(() => enemyPanel.value?.name || '敌方')
// 获取敌方角色图标
const enemyCharacterIcon = computed(() => {
  const name = enemyCharacterName.value
  return iconFor(name, 'enemy')
})

const heroHPPercent = computed(() => Math.max(0, Math.min(100, (heroHP.value / heroHPMax.value) * 100)))
const enemyHPPercent = computed(() => Math.max(0, Math.min(100, (enemyHP.value / enemyHPMax.value) * 100)))

// 简易图标映射（如未引入字体图标，将显示 emoji）
function iconFor(name: string, side: 'enemy' | 'ally') {
  const lower = name.toLowerCase()
  if (side === 'enemy') {
    if (lower.includes('守门者')) return { emoji: '🛡️', color: 'text-red-400' }
    if (lower.includes('龙')) return { emoji: '🐉', color: 'text-red-400' }
    if (lower.includes('猎手') || lower.includes('刺客')) return { emoji: '🗡️', color: 'text-purple-400' }
    return { emoji: '👹', color: 'text-rose-400' }
  } else {
    if (lower.includes('骑士')) return { emoji: '🛡️', color: 'text-yellow-400' }
    if (lower.includes('弓') || lower.includes('游侠')) return { emoji: '🏹', color: 'text-blue-400' }
    if (lower.includes('祭司') || lower.includes('法师')) return { emoji: '✨', color: 'text-indigo-400' }
    return { emoji: '⚔️', color: 'text-emerald-400' }
  }
}

// 计算角色HP百分比
function getHPPercent(current: number, max: number) {
  return Math.max(0, Math.min(100, (current / max) * 100))
}

// 获取HP颜色类
function getHPColorClass(percent: number) {
  if (percent > 60) return 'hp-healthy'
  if (percent > 30) return 'hp-warning'
  return 'hp-danger'
}

// 获取指定位置的角色
function getMinionAtPosition(position: number) {
  return board.value.find((m: Minion) => m.position === position)
}

// 获取指定位置的敌方角色
function getEnemyMinionAtPosition(position: number) {
  const minion = enemyBoard.value.find((m: Minion) => m.position === position)
  if (minion) return minion
  // 如果是后排中间（约定位置4），且存在 enemyPanel，则返回一个临时的首领实体用于渲染
  if (position === 4 && enemyPanel.value) {
    return {
      id: 'enemy-boss',
      name: enemyPanel.value.name || '敌方首领',
      attack: enemyPanel.value.attack ?? 0,
      health: enemyHP.value,
      shield: enemyPanel.value.armor ?? 0,
      canAttack: true,
      equipmentNames: [] as string[] | undefined,
      position
    } as any
  }
  return null
}

// 判定该位置渲染的实体是否为首领
function isBossAtPosition(position: number): boolean {
  const e = getEnemyMinionAtPosition(position)
  return !!e && (e as any).id === 'enemy-boss'
}

// 判断首领是否可被攻击：仅当其他敌方槽位没有随从时可攻击
function isBossAttackable(): boolean {
  // 如果 enemyBoard 中存在任意一个 position !== 4 的随从，则首领不可攻击
  return !enemyBoard.value.some((m: Minion) => m.position !== 4)
}

// 处理位置槽的拖拽悬停
function handleSlotDragOver(event: DragEvent, position: number) {
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// 处理位置槽的拖拽放置
function handleSlotDrop(event: DragEvent, position: number) {
  const cardId = event.dataTransfer?.getData('text/plain')
  if (cardId) {
    emit('deploy-card', { cardId, position })
  }
}
</script>

<style scoped>
.battle-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px; /* 减小区块间距，使敌方区域靠近顶部 */
  padding: 8px 12px 0 12px; /* 缩小上内边距并移除底部填充，紧贴页面顶部区域 */
  position: relative;
  z-index: 1;
  overflow: visible;
}

/* 战斗区域：上下布局，敌方在上，我方在下 */
.battle-arena {
  display: flex;
  flex-direction: column;
  gap: 24px; /* 稍微加大中间空间，避免图标与卡牌重叠 */
  height: 100%;
  overflow: visible;
}

/* 区域通用样式 */
.enemy-panel-zone, .enemy-battle-zone, .player-battle-zone, .player-info-zone {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

/* 强制区域顺序，避免渲染或样式导致显示顺序错乱 */
.battle-arena > .enemy-panel-zone { order: 1; }
.battle-arena > .enemy-battle-zone { order: 2; }
.battle-arena > .player-battle-zone { order: 3; }
.battle-arena > .player-info-zone { order: 4; }

/* 敌方面板区域（竖版：头像在上，血条在中，信息卡在下） */
.enemy-panel-zone {
  border-color: rgba(239, 68, 68, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  min-height: 120px;
  background: linear-gradient(180deg, rgba(31, 41, 55, 0.6), rgba(17, 24, 39, 0.35));
}

/* 敌人对战区域 */
.enemy-battle-zone {
  border-color: rgba(239, 68, 68, 0.3);
  flex: 1;
  /* 去掉外部 padding，内部由 .battle-slots 控制布局与内边距，确保高亮边框与槽区域一致 */
  padding: 0;
  box-sizing: border-box;
}

/* 玩家对战区域 */
.player-battle-zone {
  border-color: rgba(59, 130, 246, 0.3);
  flex: 1;
  /* 去掉父容器 padding，内边距交给 .battle-slots 管理，以便高亮边框贴合对战区 */
  padding: 0;
  box-sizing: border-box;
}

/* 玩家信息区域 */
.player-info-zone {
  border-color: rgba(59, 130, 246, 0.3);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px 16px;
  min-height: 80px;
}

/* 激活状态 */
.enemy-panel-zone.enemy-turn-active {
  border-color: rgba(239, 68, 68, 0.8);
  border-width: 3px;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4), inset 0 0 30px rgba(239, 68, 68, 0.1);
  animation: zone-highlight 2s ease-in-out infinite;
}

.player-battle-zone.ally-turn-active {
  border-color: rgba(59, 130, 246, 0.8);
  border-width: 3px;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.4), inset 0 0 30px rgba(59, 130, 246, 0.1);
  animation: zone-highlight 2s ease-in-out infinite;
}

@keyframes zone-highlight {
  0%, 100% {
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.4), inset 0 0 30px rgba(59, 130, 246, 0.1);
  }
  50% {
    box-shadow: 0 0 30px rgba(59, 130, 246, 0.6), inset 0 0 40px rgba(59, 130, 246, 0.2);
  }
}

/* 区域信息面板 */
.zone-info-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 10px 6px;
  min-height: 80px;
}

/* 竖排信息面板 */
.vertical-info-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 110px;
  max-width: 130px;
  width: 110px;
  padding: 10px 6px;
}

/* 玩家头像容器 */
.player-avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 8px;
}

.vertical-name-text {
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
  color: #e2e8f0;
  padding: 0;
  white-space: nowrap;
  text-align: center;
  width: 100%;
  margin-bottom: 6px;
}

/* 玩家头像框 */
.player-avatar-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(139, 92, 246, 0.3));
  border: 3px solid rgba(59, 130, 246, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.player-avatar-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

/* 敌方头像框 */
.player-avatar-circle.enemy-avatar {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(220, 38, 38, 0.3));
  border: 3px solid rgba(239, 68, 68, 0.5);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.player-avatar-circle.enemy-avatar:hover {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
}

/* 放大敌方头像（视觉层级） */
.player-avatar-circle.enemy-avatar {
  width: 72px;
  height: 72px;
  border-width: 4px;
}

.player-avatar-icon {
  font-size: 2rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

/* 固定位置槽 - 3x2网格布局 */
.battle-slots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 10px;
  width: 100%;
  max-width: 100%;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-height: 220px; /* 进一步减小高度以适配单屏 */
  /* 内部留白改为在这里控制，这样父容器的高亮/边框能紧贴此区域 */
  padding: 10px;
  box-sizing: border-box;
}

/* 敌方插槽：把前排（DOM 前3个）显示到下方，后排（DOM 后3个）显示到上方 */
.enemy-slots .battle-slot:nth-child(-n+3) {
  grid-row: 2;
}
.enemy-slots .battle-slot:nth-child(n+4) {
  grid-row: 1;
}

/* 整体对战区域外框（铺满屏幕宽度）*/
.battle-area {
  position: relative;
  margin: 0; /* 移除额外外边距，上移界面 */
}
.battle-area::before {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 100vw;
  top: -12px; /* 缩小上方外扩，整体上移 */
  height: calc(100% + 24px);
  border-radius: 18px;
  border: 3px solid rgba(59, 130, 246, 0.22);
  box-shadow: 0 6px 30px rgba(59, 130, 246, 0.18), inset 0 0 20px rgba(59,130,246,0.06);
  pointer-events: none;
  z-index: 1;
  /* 保证边框在对战槽下面一点，内容在上面 */
  background: transparent;
}

/* 中间分隔线与图标 */
.battle-divider {
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  top: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: calc(100% - 24px); /* 延长到更靠近两侧 */
  box-sizing: border-box;
  z-index: 6;
  pointer-events: none;
  /* Visible dashed divider between enemy and player zones */
  padding: 0 8px;
  height: 1px;
}
.battle-divider .divider-line {
  flex: 1;
  height: 0;
  border-top: 2px dashed rgba(239,68,68,0.95); /* 改为红色虚线 */
  border-radius: 2px;
  opacity: 0.9;
}
.battle-divider .divider-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(15,23,42,0.95), rgba(30,41,59,0.95)); /* 恢复深色背景 */
  border: 1px solid rgba(148,163,184,0.12);
  color: #e2e8f0; /* 图标颜色恢复为原来（亮色） */
  font-size: 18px;
  flex: none;
  box-shadow: 0 6px 20px rgba(0,0,0,0.45);
  backdrop-filter: blur(4px);
}

/* 允许显示战斗中间的虚线分隔（以前为视觉移除，现需展示） */
.battle-divider { display: flex !important; }

/* 固定位置槽 */
.battle-slot {
  width: 120px;
  height: 120px;
  min-width: 120px;
  min-height: 120px;
  border: 2px dashed rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: visible;
  box-sizing: border-box;
  flex-shrink: 0;
}

.battle-slot.occupied {
  border: 2px solid rgba(148, 163, 184, 0.5);
  padding: 0;
}

/* 首领所在槽位高亮（鲜红色） */
.battle-slot[data-minion-id="enemy-boss"] {
  border-style: solid;
  border-width: 3px;
  border-color: rgba(239, 68, 68, 0.95);
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.15), inset 0 0 12px rgba(239,68,68,0.08);
}

.battle-slot:not(.occupied):hover {
  border-color: rgba(59, 130, 246, 0.6);
  background: rgba(59, 130, 246, 0.1);
}

.empty-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(148, 163, 184, 0.5);
  font-size: 0.75rem;
}

.slot-hint {
  opacity: 0.6;
}

/* 隐藏位置表现（数字与占位虚线），只保留已占位的卡牌显示 */
.battle-slot:not(.occupied) .slot-hint {
  display: none !important;
}

/* 角色卡片 */
.character-card {
  background: rgba(15, 23, 42, 0.6);
  border: 2px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  min-width: 120px;
  box-sizing: border-box;
  flex-shrink: 0;
  z-index: 10;
}

.character-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
  pointer-events: none;
  clip-path: inset(0 round 12px);
}

.character-card:hover::before {
  left: 100%;
}

.character-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.enemy-card {
  border-color: rgba(239, 68, 68, 0.4);
}

.enemy-card:hover {
  border-color: rgba(239, 68, 68, 0.6);
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.2);
}

.ally-card {
  border-color: rgba(59, 130, 246, 0.4);
}

.ally-card:hover {
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.2);
}

.equipment-dots {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
}

.equipment-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 2px solid rgba(248, 250, 252, 0.9);
  background: radial-gradient(circle at 30% 30%, #fbbf24, #92400e);
  box-shadow: 0 0 6px rgba(251, 191, 36, 0.8);
}

.character-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  flex-wrap: nowrap; /* keep name and badges on single line */
  padding: 4px 0; /* small vertical padding to align badges with name */
}

.header-badges {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
  margin-left: 6px;
}

.character-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #e2e8f0;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap; /* single-line name to save vertical space */
  line-height: 1.2;
  min-height: 1.4em; /* reduce min-height so bottom stats have room */
  margin-right: 6px;
}

.shield-badge, .star-badge, .status-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 6px;
  font-size: 0.6875rem;
}

.shield-badge {
  background: rgba(139, 92, 246, 0.2);
  border-color: rgba(139, 92, 246, 0.4);
}

.status-badge.summoning-sickness {
  background: rgba(55, 65, 81, 0.35);
  border-color: rgba(55, 65, 81, 0.55);
  color: #9ca3af;
  opacity: 0.9;
}

.shield-icon, .star-icon, .status-icon {
  font-size: 0.75rem;
}

.shield-value, .star-value, .status-text {
  font-weight: 700;
  color: #e2e8f0;
  font-size: 0.625rem;
}

.character-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  margin: 4px 0;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.character-stats {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  flex-shrink: 0;
  margin-top: 4px; /* small spacing from avatar/name to stats */
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.attack-stat {
  color: #fbbf24;
}

.hp-stat {
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 0.75rem;
}

.stat-value {
  font-size: 0.8125rem;
}

/* 横排布局的HP条 */
.hp-display.horizontal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  max-width: 100%;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.hp-bar-container.horizontal {
  width: 100%;
  max-width: 100%;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  overflow: hidden;
}

.hp-bar-bg.horizontal {
  width: 100%;
  max-width: 100%;
  height: 14px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 7px;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
  flex-shrink: 0;
}

.hp-bar-container.horizontal .hp-bar-fill {
  height: 100%;
  width: 100%;
  border-radius: 8px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.hp-bar-container.horizontal .hp-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer-horizontal 2s infinite;
}

@keyframes shimmer-horizontal {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.hp-text.horizontal {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 0.7rem;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.hp-value {
  color: #e2e8f0;
}

.hp-separator {
  color: #94a3b8;
}

.hp-max {
  color: #94a3b8;
}

.enemy-hp .hp-value {
  color: #f87171;
}

.ally-hp .hp-value {
  color: #60a5fa;
}

/* HP条渐变 */
.hp-bar-container.horizontal .hp-bar-fill.hp-healthy {
  background: linear-gradient(90deg, #10b981, #059669);
}

.hp-bar-container.horizontal .hp-bar-fill.hp-warning {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.hp-bar-container.horizontal .hp-bar-fill.hp-danger {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* 敌人面板信息 */
.enemy-panel-info {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 8px;
  padding: 10px 16px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(239, 68, 68, 0.12);
  border-radius: 12px;
  flex-wrap: wrap;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.02);
}

.panel-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 0.75rem;
}

.panel-stat-icon {
  font-size: 1rem;
}

.panel-stat-label {
  color: #94a3b8;
  font-weight: 500;
  font-size: 0.65rem;
}

.panel-stat-value {
  color: #e2e8f0;
  font-weight: 700;
  font-size: 0.875rem;
  min-width: 24px;
  text-align: right;
}

.enemy-difficulty-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-left: 8px;
}

.enemy-difficulty-badge.difficulty-normal {
  background: rgba(148, 163, 184, 0.2);
  color: #cbd5e1;
  border: 1px solid rgba(148, 163, 184, 0.4);
}

.enemy-difficulty-badge.difficulty-hard {
  background: rgba(251, 191, 36, 0.2);
  color: #fcd34d;
  border: 1px solid rgba(251, 191, 36, 0.4);
}

.enemy-difficulty-badge.difficulty-boss {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

/* 受击反馈效果 */
.hit-effect {
  animation: hit-flash 0.5s ease;
}

@keyframes hit-flash {
  0% {
    transform: scale(1);
    filter: brightness(1);
  }
  15% {
    transform: scale(1.05);
    filter: brightness(1.5);
  }
  30% {
    transform: scale(0.98);
    filter: brightness(0.8);
  }
  45% {
    transform: scale(1.02);
    filter: brightness(1.3);
  }
  60% {
    transform: scale(0.99);
    filter: brightness(0.9);
  }
  100% {
    transform: scale(1);
    filter: brightness(1);
  }
}

/* 伤害数字显示 */
.damage-number {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.5rem;
  font-weight: 700;
  color: #ef4444;
  text-shadow:
    0 0 10px rgba(239, 68, 68, 0.8),
    0 2px 4px rgba(0, 0, 0, 0.8);
  z-index: 1000;
  pointer-events: none;
  animation: damage-float 1s ease-out forwards;
  user-select: none;
}

.enemy-damage {
  color: #fbbf24;
  text-shadow:
    0 0 10px rgba(251, 191, 36, 0.8),
    0 2px 4px rgba(0, 0, 0, 0.8);
}

.player-damage {
  color: #ef4444;
  font-size: 1.25rem;
}

.enemy-boss-damage {
  color: #fbbf24;
  font-size: 1.25rem;
}

@keyframes damage-float {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -80%) scale(1.2);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -120%) scale(0.9);
  }
}

/* 玩家头像受击效果 */
.player-avatar-container.hit-effect {
  animation: avatar-hit-shake 0.5s ease;
}

@keyframes avatar-hit-shake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-3px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(3px);
  }
}

/* 攻击动画效果 - 原卡片半透明 */
.character-card.attacking {
  opacity: 0.3;
  pointer-events: none;
}

/* 攻击克隆体 - 跳出方框攻击 */
.character-card-attack-clone {
  position: fixed;
  width: 90px;
  height: 120px;
  min-width: 90px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.95));
  border: 2px solid rgba(59, 130, 246, 0.8);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2147483647 !important;
  pointer-events: none;
  animation: attack-move-clone 0.9s ease-in-out forwards;
  transform-origin: center center;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.6), 0 0 20px rgba(59, 130, 246, 0.4);
  will-change: transform, left, top;
  margin-left: -50px;
  margin-top: -70px;
}

.character-card-attack-clone .character-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 3px;
  flex-wrap: wrap;
  padding: 2px 6px;
}

.character-card-attack-clone .character-name {
  font-size: 0.75rem;
  font-weight: 700;
  color: #e2e8f0;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.character-card-attack-clone .character-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  margin: 3px 0;
  flex-shrink: 0;
}

.character-card-attack-clone .avatar-icon {
  font-size: 1.4rem;
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.3));
}

.character-card-attack-clone .character-stats {
  display: flex;
  justify-content: space-between;
  gap: 3px;
  flex-shrink: 0;
  padding: 2px 6px;
}

.character-card-attack-clone .stat-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.625rem;
  font-weight: 600;
}

.character-card-attack-clone .attack-stat {
  color: #fbbf24;
}

.character-card-attack-clone .hp-stat {
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.character-card-attack-clone .stat-icon {
  font-size: 0.625rem;
}

.character-card-attack-clone .stat-value {
  font-size: 0.6875rem;
}

/* 敌人攻击克隆体 - 使用红色主题 */
.character-card-attack-clone.enemy-attack-clone {
  background: linear-gradient(145deg, rgba(59, 15, 15, 0.95), rgba(42, 8, 8, 0.95));
  border: 2px solid rgba(239, 68, 68, 0.8);
  box-shadow: 0 8px 32px rgba(239, 68, 68, 0.6), 0 0 20px rgba(239, 68, 68, 0.4);
}

/* 敌人本体攻击克隆体 */
.character-card-attack-clone.enemy-boss-attack-clone {
  background: linear-gradient(145deg, rgba(59, 15, 15, 0.95), rgba(42, 8, 8, 0.95));
  border: 2px solid rgba(239, 68, 68, 0.9);
  box-shadow: 0 8px 32px rgba(239, 68, 68, 0.7), 0 0 20px rgba(239, 68, 68, 0.5);
  width: 75px;
  height: 105px;
  min-width: 75px;
  margin-left: -40px;
  margin-top: -55px;
}

/* 允许攻击克隆体/伤害数字跨越容器，不被裁剪 */
.battle-field,
.battle-arena,
.enemy-panel-zone,
.enemy-battle-zone,
.player-battle-zone,
.player-info-zone,
.battle-slots,
.battle-slot {
  overflow: visible !important;
  position: relative;
}

/* 卡牌本体保持裁剪，限制内部光效不出界 */
.character-card {
  overflow: hidden;
}

@keyframes attack-move-clone {
  0% {
    left: var(--start-x, 200px);
    top: var(--start-y, 400px);
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  40% {
    left: var(--target-x, 800px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.1);
    opacity: 1;
  }
  50% {
    left: calc(var(--target-x, 800px) + 15px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.3) rotate(12deg);
    opacity: 1;
  }
  60% {
    left: var(--target-x, 800px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.1);
    opacity: 1;
  }
  100% {
    left: var(--start-x, 200px);
    top: var(--start-y, 400px);
    transform: translate(0, 0) scale(0.9);
    opacity: 0;
  }
}

@keyframes attack-glow {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(1.5);
  }
}

/* 攻击目标高亮 */
.character-card.attack-target {
  animation: target-flash 0.3s ease-out;
  box-shadow: 0 0 20px rgba(251, 191, 36, 0.6);
}

@keyframes target-flash {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.5);
  }
}

/* 敌人本体受击目标 */
.player-avatar-container.hit-effect[data-is-boss="true"] {
  animation: avatar-hit-shake 0.5s ease, boss-target-flash 0.3s ease-out;
}

@keyframes boss-target-flash {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.3);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .battle-field {
    padding: 12px;
  }

  .characters-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 12px;
  }

  .hp-display:not(.horizontal) {
    min-width: 150px;
  }

  .hp-text {
    min-width: 50px;
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .characters-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .character-card {
    padding: 8px;
  }

  .avatar-icon {
    font-size: 2rem;
  }

  .hp-display:not(.horizontal) {
    min-width: 120px;
  }
}

/* 移除对战区外框（隐藏之前用于全屏高亮的伪元素） */
.battle-area::before {
  display: none !important;
}
/* 覆盖：移除所有战斗区的高亮边框与阴影（最终覆盖，避免残留样式） */
.battle-area,
.battle-arena,
.enemy-battle-zone,
.player-battle-zone,
.enemy-panel-zone,
.battle-slots,
.battle-slot,
.character-card,
.character-card-attack-clone,
.player-avatar-circle,
.player-avatar-circle.enemy-avatar {
  /* 仅移除投影/发光效果，保留边框与背景以保留结构表现 */
  box-shadow: none !important;
}

/* 移除激活态的发光（但保留边框变色等表现） */
.enemy-panel-zone.enemy-turn-active,
.player-battle-zone.ally-turn-active,
.character-card.attack-target,
.character-card.attacking {
  box-shadow: none !important;
}

</style>

/* 仅去掉战斗区域与首领的实线/主题边框，保留槽位虚线与卡片边框 */
<style scoped>
.battle-area,
.battle-arena,
.enemy-battle-zone,
.player-battle-zone,
.enemy-panel-zone {
  border: none !important;
  border-color: transparent !important;
}

/* 删除首领槽的红色突出边框，但保留槽的虚线占位样式 */
.battle-slot[data-minion-id="enemy-boss"] {
  border: none !important;
}
</style>
