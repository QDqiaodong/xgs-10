export const TEXT_LIMITS = {
  LIST_SUMMARY: 120,
  LIST_SUMMARY_CSS_LINES: 2,
  ARCHIVE_SUMMARY: 180,
  ARCHIVE_SUMMARY_CSS_LINES: 3,
  DETAIL_SUMMARY: 300,
  DETAIL_STORY_COLLAPSE: 400,
  CHIP_TEXT: 40,
  COMMENT: 500
}

const SENTENCE_END_PATTERN = /[。！？.!?\n]/g

function cleanBaseText(text) {
  if (!text) return ''
  let cleaned = String(text).trim()
  cleaned = cleaned.replace(/\s+/g, ' ')
  cleaned = cleaned.replace(/([，。！？、；：""''（）【】《》,.!?;:\'\"\(\)\[\]<>])\1+/g, '$1')
  cleaned = cleaned.trim()
  return cleaned
}

function truncateToSentence(text, maxLen, suffix = '...') {
  if (text.length <= maxLen) return text
  let cutIdx = maxLen
  SENTENCE_END_PATTERN.lastIndex = 0
  let match
  const searchRange = Math.min(maxLen + 30, text.length)
  while ((match = SENTENCE_END_PATTERN.exec(text)) !== null) {
    if (match.index <= searchRange && match.index >= Math.floor(maxLen * 0.6)) {
      cutIdx = match.index + 1
      break
    }
    if (match.index > searchRange) break
  }
  const truncated = text.substring(0, cutIdx).trim()
  return truncated + (truncated.length < text.length ? suffix : '')
}

export function truncateForList(text, customLimit) {
  const limit = customLimit || TEXT_LIMITS.LIST_SUMMARY
  const cleaned = cleanBaseText(text)
  if (!cleaned) return ''
  if (cleaned.length <= limit) return cleaned
  return truncateToSentence(cleaned, limit)
}

export function truncateForArchive(text, customLimit) {
  const limit = customLimit || TEXT_LIMITS.ARCHIVE_SUMMARY
  const cleaned = cleanBaseText(text)
  if (!cleaned) return ''
  if (cleaned.length <= limit) return cleaned
  return truncateToSentence(cleaned, limit)
}

export function truncateForDetailSummary(text, customLimit) {
  const limit = customLimit || TEXT_LIMITS.DETAIL_SUMMARY
  const cleaned = cleanBaseText(text)
  if (!cleaned) return ''
  if (cleaned.length <= limit) return cleaned
  return truncateToSentence(cleaned, limit)
}

export function truncateForChip(text, customLimit) {
  const limit = customLimit || TEXT_LIMITS.CHIP_TEXT
  const cleaned = cleanBaseText(text)
  if (!cleaned) return ''
  if (cleaned.length <= limit) return cleaned
  return cleaned.substring(0, limit) + '...'
}

export function shouldCollapseStory(text, customLimit) {
  const limit = customLimit || TEXT_LIMITS.DETAIL_STORY_COLLAPSE
  const cleaned = cleanBaseText(text)
  return cleaned.length > limit
}

export function buildCardSummary(post) {
  if (!post) return ''
  const summary = cleanBaseText(post.storySummary)
  if (summary) {
    return truncateForList(summary)
  }
  const candidates = [post.story, post.memory, post.content]
  for (const c of candidates) {
    const cleaned = cleanBaseText(c)
    if (cleaned) {
      return truncateForList(cleaned)
    }
  }
  return ''
}

export function buildArchiveSummary(post) {
  if (!post) return ''
  const summary = cleanBaseText(post.storySummary)
  if (summary) {
    return truncateForArchive(summary)
  }
  const candidates = [post.story, post.memory, post.content]
  for (const c of candidates) {
    const cleaned = cleanBaseText(c)
    if (cleaned) {
      return truncateForArchive(cleaned)
    }
  }
  return ''
}

export function buildDetailSummary(post) {
  if (!post) return ''
  const summary = cleanBaseText(post.storySummary)
  if (summary) {
    return truncateForDetailSummary(summary)
  }
  const candidates = [post.story, post.memory, post.content]
  for (const c of candidates) {
    const cleaned = cleanBaseText(c)
    if (cleaned) {
      return truncateForDetailSummary(cleaned)
    }
  }
  return ''
}

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
