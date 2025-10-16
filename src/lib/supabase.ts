import { createClient } from '@supabase/supabase-js'

const SUPABASE_URL = 'https://vzklixhtzjwblqbyudmp.supabase.co'
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZ6a2xpeGh0emp3YmxxYnl1ZG1wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc3NTA4ODUsImV4cCI6MjA3MzMyNjg4NX0.YwbEGafz0T3f2bpfd-VFWGBpDyxCaqSGieDctBg5xL0'

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY)

// 获取当前会话（若未登录则返回 null）
export async function getSession() {
  const { data, error } = await supabase.auth.getSession()
  if (error) return null
  return data.session ?? null
}

// 尝试匿名登录（若你的项目启用了匿名登录策略则可用；否则返回 null）
export async function tryAnonymousSignIn() {
  // 若项目未开启匿名登录，此调用会失败；保留本地模式
  try {
    // @ts-ignore: 有的项目未启用匿名登录
    const { data, error } = await supabase.auth.signInAnonymously?.()
    if (error) return null
    return data?.session ?? null
  } catch {
    return null
  }
}