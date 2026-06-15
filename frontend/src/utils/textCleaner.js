export function cleanItemName(name) {
  if (!name) return ''
  let cleaned = String(name).trim()
  cleaned = cleaned.replace(/\s+/g, ' ')
  cleaned = cleaned.replace(/([，。！？、；：""''（）【】《》,.!?;:\'\"\(\)\[\]<>])\1+/g, '$1')
  cleaned = cleaned.replace(/[\s\-_·.。,，、]{2,}/g, (match) => {
    if (match.includes(' ')) return ' '
    return match.charAt(0)
  })
  cleaned = cleaned.replace(/^[\s\-_·.。,，、!！?？;；:：""''（）【】《》()\[\]<>]+/, '')
  cleaned = cleaned.replace(/[\s\-_·.。,，、!！?？;；:：""''（）【】《》()\[\]<>]+$/, '')
  cleaned = cleaned.trim()
  if (cleaned.length < 2) {
    return ''
  }
  if (cleaned.length > 50) {
    cleaned = cleaned.substring(0, 50)
  }
  return cleaned
}

export function cleanTitle(title) {
  if (!title) return ''
  let cleaned = String(title).trim()
  cleaned = cleaned.replace(/\s+/g, ' ')
  cleaned = cleaned.replace(/([，。！？、；：""''（）【】《》,.!?;:\'\"\(\)\[\]<>])\1+/g, '$1')
  cleaned = cleaned.trim()
  if (cleaned.length < 2) {
    return ''
  }
  if (cleaned.length > 200) {
    cleaned = cleaned.substring(0, 200)
  }
  return cleaned
}

export function displayItemName(name, fallback = '未知物件') {
  const cleaned = cleanItemName(name)
  return cleaned || fallback
}
