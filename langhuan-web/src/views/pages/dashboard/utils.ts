import type { UserStat } from './types'

// 格式化文件大小
export function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 计算用户有效率
export function calculateEffectiveRate(user: UserStat): number {
    if (user.questionCount === 0) return 0
    return Math.round((user.validAnswerCount / user.questionCount) * 100)
}

// 获取有效率标签类型
export function getEffectiveRateType(rate: number): 'success' | 'warning' | 'danger' {
    if (rate >= 90) return 'success'
    if (rate >= 70) return 'warning'
    return 'danger'
}