/** Format API LocalDateTime strings for the iOS chrome. */
export function formatTime(value?: string): string {
  if (!value) {
    return ''
  }
  const date = new Date(value.includes('T') ? value : value.replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const now = new Date()
  const sameYear = date.getFullYear() === now.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  if (sameYear) {
    return `${month}月${day}日 ${hh}:${mm}`
  }
  return `${date.getFullYear()}年${month}月${day}日 ${hh}:${mm}`
}
