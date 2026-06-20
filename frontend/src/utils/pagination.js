export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 10,
  MIN_PAGE: 0,
  MIN_SIZE: 1,
  MAX_SIZE: 100
}

export function validatePage(page) {
  const num = parseInt(page, 10)
  if (isNaN(num) || num < PAGINATION.MIN_PAGE) {
    return PAGINATION.DEFAULT_PAGE
  }
  return num
}

export function validateSize(size, defaultSize = PAGINATION.DEFAULT_SIZE) {
  const num = parseInt(size, 10)
  if (isNaN(num) || num < PAGINATION.MIN_SIZE) {
    return defaultSize
  }
  if (num > PAGINATION.MAX_SIZE) {
    return PAGINATION.MAX_SIZE
  }
  return num
}

export function sanitizePaginationParams(params, defaultSize) {
  return {
    ...params,
    page: validatePage(params.page),
    size: validateSize(params.size, defaultSize)
  }
}
