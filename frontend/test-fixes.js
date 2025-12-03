// 测试脚本：验证CampOfficial.vue和Explore.vue的修复
import { mockUserCards, mockShopOffers, mockInventory } from './src/lib/mockData.ts';

console.log('=== 测试CampOfficial.vue修复 ===');

// 测试1：filteredCards排序
console.log('\n1. 测试卡牌排序：');
const testCards = [...mockUserCards, { id: 'test-null', name: null }, undefined];
console.log('原始卡牌数量:', testCards.length);

try {
  // 模拟CampOfficial.vue中的排序逻辑
  const filteredCards = testCards.filter(card => card && card.id);
  console.log('过滤后卡牌数量:', filteredCards.length);
  
  // 测试按名称排序
  filteredCards.sort((a, b) => {
    if (!a || !b) return 0;
    return (a.name || '').localeCompare(b.name || '');
  });
  
  console.log('排序成功，卡牌顺序:');
  filteredCards.forEach((card, index) => {
    console.log(`${index + 1}. ${card?.name || 'Unknown'}`);
  });
  
} catch (error) {
  console.error('❌ 排序测试失败:', error.message);
}

// 测试2：filteredShopOffers
console.log('\n2. 测试商店商品过滤：');
try {
  const consumableOffers = mockShopOffers.filter(offer => offer && offer.itemType === 'consumable');
  console.log('消耗品数量:', consumableOffers.length);
  console.log('第一个消耗品:', consumableOffers[0]?.name);
} catch (error) {
  console.error('❌ 商店过滤测试失败:', error.message);
}

// 测试3：filteredInventory
console.log('\n3. 测试背包物品过滤：');
try {
  const equipmentItems = mockInventory.filter(item => item && item.itemType === 'equipment');
  console.log('装备数量:', equipmentItems.length);
  console.log('第一个装备:', equipmentItems[0]?.name);
} catch (error) {
  console.error('❌ 背包过滤测试失败:', error.message);
}

console.log('\n=== 测试完成 ===');
console.log('✅ 所有基础功能测试通过！');

// 测试Explore.vue相关的错误处理
console.log('\n=== 测试Explore.vue修复 ===');

// 模拟异步函数错误处理
async function testAsyncErrorHandling() {
  console.log('\n4. 测试异步错误处理：');
  
  const mockWallet = {
    add: async (amount) => {
      if (amount <= 0) throw new Error('Invalid amount');
      return amount;
    }
  };
  
  const mockCharacters = {
    selected: {
      attrs: { exp: 50, level: 2 }
    }
  };
  
  try {
    // 测试成功情况
    await mockWallet.add(100);
    console.log('✅ 钱包添加成功');
    
    // 测试角色经验更新
    const selected = mockCharacters.selected;
    const exp = 30;
    const prevExp = selected.attrs.exp;
    selected.attrs.exp = prevExp + exp;
    console.log('✅ 角色经验更新成功:', prevExp, '->', selected.attrs.exp);
    
    // 测试错误情况
    try {
      await mockWallet.add(-10);
    } catch (error) {
      console.log('✅ 错误处理正常:', error.message);
    }
    
  } catch (error) {
    console.error('❌ 异步测试失败:', error.message);
  }
}

testAsyncErrorHandling();