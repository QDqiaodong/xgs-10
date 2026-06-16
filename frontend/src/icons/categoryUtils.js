import { getCategoryConfig } from './categories'

export const getCategoryClass = (categoryName) => {
  const config = getCategoryConfig(categoryName)
  return `cat-${config.key}`
}

export const getCategoryStyleVars = (categoryName) => {
  const config = getCategoryConfig(categoryName)
  return {
    '--cat-primary': config.primaryColor,
    '--cat-secondary': config.secondaryColor,
    '--cat-gradient': config.gradient,
    '--cat-bg-gradient': config.bgGradient,
    '--cat-border': config.borderColor,
    '--cat-text': config.textColor
  }
}

export const createCategoryStyleString = (categoryName) => {
  const vars = getCategoryStyleVars(categoryName)
  return Object.entries(vars)
    .map(([k, v]) => `${k}: ${v}`)
    .join('; ')
}

export const getCategoryDescription = (categoryName) => {
  return getCategoryConfig(categoryName).description
}

export const getCategoryKeywords = (categoryName) => {
  return getCategoryConfig(categoryName).keywords || []
}

export default {
  getCategoryClass,
  getCategoryStyleVars,
  createCategoryStyleString,
  getCategoryDescription,
  getCategoryKeywords
}
