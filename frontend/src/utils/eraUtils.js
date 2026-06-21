export const EraCode = {
  ERA_60S: '60s',
  ERA_70S: '70s',
  ERA_80S: '80s',
  ERA_90S: '90s',
  ERA_00S: '00s'
}

export const EraMeta = {
  [EraCode.ERA_60S]: {
    code: EraCode.ERA_60S,
    name: '60年代',
    icon: '★',
    colorPrimary: '#5d4037',
    colorSecondary: '#d7ccc8',
    colorAccent: '#ffe082',
    yearStart: 1960,
    yearEnd: 1969,
    sortOrder: 1
  },
  [EraCode.ERA_70S]: {
    code: EraCode.ERA_70S,
    name: '70年代',
    icon: '✿',
    colorPrimary: '#8d6e63',
    colorSecondary: '#ffcc80',
    colorAccent: '#fff59d',
    yearStart: 1970,
    yearEnd: 1979,
    sortOrder: 2
  },
  [EraCode.ERA_80S]: {
    code: EraCode.ERA_80S,
    name: '80年代',
    icon: '♪',
    colorPrimary: '#1976d2',
    colorSecondary: '#90caf9',
    colorAccent: '#e3f2fd',
    yearStart: 1980,
    yearEnd: 1989,
    sortOrder: 3
  },
  [EraCode.ERA_90S]: {
    code: EraCode.ERA_90S,
    name: '90年代',
    icon: '⚡',
    colorPrimary: '#ff6f00',
    colorSecondary: '#ffab40',
    colorAccent: '#fff8e1',
    yearStart: 1990,
    yearEnd: 1999,
    sortOrder: 4
  },
  [EraCode.ERA_00S]: {
    code: EraCode.ERA_00S,
    name: '00年代',
    icon: '◈',
    colorPrimary: '#00acc1',
    colorSecondary: '#80deea',
    colorAccent: '#e0f7fa',
    yearStart: 2000,
    yearEnd: 2009,
    sortOrder: 5
  }
}

export const ALL_ERA_CODES = Object.values(EraCode)

export function getEraCodeByName(eraName) {
  if (!eraName) return EraCode.ERA_80S
  const name = String(eraName)
  if (name.includes('60')) return EraCode.ERA_60S
  if (name.includes('70')) return EraCode.ERA_70S
  if (name.includes('80')) return EraCode.ERA_80S
  if (name.includes('90')) return EraCode.ERA_90S
  if (name.includes('00')) return EraCode.ERA_00S
  return EraCode.ERA_80S
}

export function getEraMeta(eraNameOrCode) {
  const code = getEraCodeByName(eraNameOrCode)
  return EraMeta[code] || EraMeta[EraCode.ERA_80S]
}

export function getEraClass(eraName) {
  return getEraCodeByName(eraName)
}

export function getEraIcon(eraName) {
  return getEraMeta(eraName).icon
}

export function getEraName(eraNameOrCode) {
  return getEraMeta(eraNameOrCode).name
}

export function getEraYearRange(eraNameOrCode) {
  const meta = getEraMeta(eraNameOrCode)
  return { start: meta.yearStart, end: meta.yearEnd }
}

export function getEraColorScheme(eraNameOrCode) {
  const meta = getEraMeta(eraNameOrCode)
  return {
    primary: meta.colorPrimary,
    secondary: meta.colorSecondary,
    accent: meta.colorAccent
  }
}

export function normalizeEraName(eraName) {
  return getEraMeta(eraName).name
}

export function sortErasByYear(eras, direction = 'asc') {
  if (!eras || !Array.isArray(eras)) return []
  const sorted = [...eras].sort((a, b) => {
    const yearA = a.yearStart || getEraMeta(a.name || a).yearStart
    const yearB = b.yearStart || getEraMeta(b.name || b).yearStart
    return direction === 'asc' ? yearA - yearB : yearB - yearA
  })
  return sorted
}

export function sortErasBySortOrder(eras, direction = 'asc') {
  if (!eras || !Array.isArray(eras)) return []
  const sorted = [...eras].sort((a, b) => {
    const orderA = a.sortOrder != null ? a.sortOrder : getEraMeta(a.name || a).sortOrder
    const orderB = b.sortOrder != null ? b.sortOrder : getEraMeta(b.name || b).sortOrder
    return direction === 'asc' ? orderA - orderB : orderB - orderA
  })
  return sorted
}

export function sortErasDefault(eras) {
  return sortErasBySortOrder(eras, 'asc')
}

export function isEraNameValid(eraName) {
  if (!eraName) return false
  const code = getEraCodeByName(eraName)
  return code !== EraCode.ERA_80S || eraName.includes('80')
}

export function getEraCssVars(eraNameOrCode) {
  const meta = getEraMeta(eraNameOrCode)
  return {
    '--era-primary': meta.colorPrimary,
    '--era-secondary': meta.colorSecondary,
    '--era-accent': meta.colorAccent
  }
}

export const CategoryEraRule = {
  '通讯工具': EraCode.ERA_90S,
  '家用电器': EraCode.ERA_80S,
  '影音设备': EraCode.ERA_70S
}

export function getEarliestEraForCategory(categoryName) {
  if (!categoryName) return null
  const code = CategoryEraRule[categoryName]
  return code ? EraMeta[code].name : null
}

export function isEraCategoryReasonable(eraNameOrCode, categoryName) {
  if (!eraNameOrCode || !categoryName) return true
  const minCode = CategoryEraRule[categoryName]
  if (!minCode) return true
  const eraMeta = getEraMeta(eraNameOrCode)
  const minMeta = EraMeta[minCode]
  return eraMeta.sortOrder >= minMeta.sortOrder
}

export function getEraCategoryWarning(eraNameOrCode, categoryName) {
  if (isEraCategoryReasonable(eraNameOrCode, categoryName)) return null
  const earliestName = getEarliestEraForCategory(categoryName)
  const eraName = getEraMeta(eraNameOrCode).name
  return `「${categoryName}」类物件最早见于${earliestName}，归入「${eraName}」可能不太合理，请核对年代与品类是否匹配，以提升资料可信度。`
}
