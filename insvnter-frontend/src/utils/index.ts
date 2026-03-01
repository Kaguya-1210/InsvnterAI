/**
 * 通用工具函数
 */

/**
 * 延迟执行
 */
export function sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 格式化日期
 */
export function formatDate(date: Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
    const pad = (n: number) => n.toString().padStart(2, '0')
    return format
        .replace('YYYY', date.getFullYear().toString())
        .replace('MM', pad(date.getMonth() + 1))
        .replace('DD', pad(date.getDate()))
        .replace('HH', pad(date.getHours()))
        .replace('mm', pad(date.getMinutes()))
        .replace('ss', pad(date.getSeconds()))
}
