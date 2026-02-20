export function formatDate(date) {
  if (!date) return ''
  
  // 如果是字符串且包含T，直接替换（保持服务端时间格式）
  if (typeof date === 'string' && date.includes('T')) {
      return date.replace('T', ' ')
  }

  const d = new Date(date)
  if (isNaN(d.getTime())) return date
  
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hour = d.getHours().toString().padStart(2, '0')
  const minute = d.getMinutes().toString().padStart(2, '0')
  const second = d.getSeconds().toString().padStart(2, '0')
  
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}
