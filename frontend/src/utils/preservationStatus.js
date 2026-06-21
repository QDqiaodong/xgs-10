export const PRESERVATION_STATUS = {
  PERFECT: {
    label: '完好',
    icon: '✓',
    color: '#2e7d32',
    bgGradient: 'linear-gradient(135deg, #e8f5e9, #c8e6c9)',
    borderColor: '#2e7d32'
  },
  REPAIRABLE: {
    label: '可修复',
    icon: '🔧',
    color: '#f57c00',
    bgGradient: 'linear-gradient(135deg, #fff3e0, #ffe0b2)',
    borderColor: '#f57c00'
  },
  MISSING_PARTS: {
    label: '缺件',
    icon: '⚠️',
    color: '#c62828',
    bgGradient: 'linear-gradient(135deg, #ffebee, #ffcdd2)',
    borderColor: '#c62828'
  },
  IMAGE_ONLY: {
    label: '仅留影像',
    icon: '📷',
    color: '#6a1b9a',
    bgGradient: 'linear-gradient(135deg, #f3e5f5, #e1bee7)',
    borderColor: '#6a1b9a'
  },
  UNDER_REPAIR: {
    label: '修复中',
    icon: '⚙️',
    color: '#1565c0',
    bgGradient: 'linear-gradient(135deg, #e3f2fd, #bbdefb)',
    borderColor: '#1565c0'
  },
  WORN: {
    label: '磨损',
    icon: '📜',
    color: '#6d4c41',
    bgGradient: 'linear-gradient(135deg, #efebe9, #d7ccc8)',
    borderColor: '#6d4c41'
  },
  UNKNOWN: {
    label: '待评估',
    icon: '❓',
    color: '#757575',
    bgGradient: 'linear-gradient(135deg, #fafafa, #eeeeee)',
    borderColor: '#757575'
  }
}

export const getStatusConfig = (statusLabel) => {
  if (!statusLabel) return PRESERVATION_STATUS.UNKNOWN

  const normalized = statusLabel.trim()

  for (const key in PRESERVATION_STATUS) {
    if (PRESERVATION_STATUS[key].label === normalized) {
      return PRESERVATION_STATUS[key]
    }
  }

  if (normalized.includes('完好') || normalized.includes('完美') || normalized.includes('正常') || normalized.includes('如新') || normalized.includes('保存')) {
    return PRESERVATION_STATUS.PERFECT
  }
  if (normalized.includes('修复') && normalized.includes('中')) {
    return PRESERVATION_STATUS.UNDER_REPAIR
  }
  if (normalized.includes('可修复') || normalized.includes('能修') || normalized.includes('待修复') || normalized.includes('修复') || normalized.includes('修理') || normalized.includes('维修') || normalized.includes('破损') || normalized.includes('损坏')) {
    return PRESERVATION_STATUS.REPAIRABLE
  }
  if (normalized.includes('缺') || normalized.includes('少') || normalized.includes('丢失') || normalized.includes('不完整')) {
    return PRESERVATION_STATUS.MISSING_PARTS
  }
  if (normalized.includes('仅留影像') || normalized.includes('只有照片') || normalized.includes('只有图片') || normalized.includes('仅剩照片') || normalized.includes('实物遗失') || normalized.includes('实物丢失')) {
    return PRESERVATION_STATUS.IMAGE_ONLY
  }
  if (normalized.includes('磨损') || normalized.includes('旧') || normalized.includes('锈') || normalized.includes('老化') || normalized.includes('划痕')) {
    return PRESERVATION_STATUS.WORN
  }

  return PRESERVATION_STATUS.UNKNOWN
}

export const getAllStatuses = () => {
  return Object.keys(PRESERVATION_STATUS).map(key => ({
    ...PRESERVATION_STATUS[key],
    status: key
  }))
}

export const normalizePreservationStatus = (status) => {
  return getStatusConfig(status).label
}
