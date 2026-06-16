import homeAppliancesConfig from './homeAppliances'
import audioVideoConfig from './audioVideo'
import communicationConfig from './communication'
import toysConfig from './toys'
import stationeryConfig from './stationery'
import clothingConfig from './clothing'
import foodConfig from './food'
import dailyConfig from './daily'

export const categoryConfigsList = [
  homeAppliancesConfig,
  audioVideoConfig,
  communicationConfig,
  toysConfig,
  stationeryConfig,
  clothingConfig,
  foodConfig,
  dailyConfig
]

export const categoryConfigsByName = {
  '家用电器': homeAppliancesConfig,
  '影音设备': audioVideoConfig,
  '通讯工具': communicationConfig,
  '玩具玩偶': toysConfig,
  '文具书籍': stationeryConfig,
  '服饰配饰': clothingConfig,
  '食品饮料': foodConfig,
  '日常用品': dailyConfig
}

export const categoryConfigsByKey = {
  homeAppliances: homeAppliancesConfig,
  audioVideo: audioVideoConfig,
  communication: communicationConfig,
  toys: toysConfig,
  stationery: stationeryConfig,
  clothing: clothingConfig,
  food: foodConfig,
  daily: dailyConfig
}

export const getCategoryConfig = (identifier) => {
  if (!identifier) return dailyConfig
  if (categoryConfigsByName[identifier]) return categoryConfigsByName[identifier]
  if (categoryConfigsByKey[identifier]) return categoryConfigsByKey[identifier]
  const byKeyword = categoryConfigsList.find(cfg =>
    cfg.keywords?.some(kw => identifier.includes(kw))
  )
  return byKeyword || dailyConfig
}

export const getCategoryIcon = (identifier) => {
  return getCategoryConfig(identifier).emoji
}

export const getCategoryColor = (identifier) => {
  return getCategoryConfig(identifier).primaryColor
}

export const getCategoryGradient = (identifier) => {
  return getCategoryConfig(identifier).gradient
}

export default {
  categoryConfigsList,
  categoryConfigsByName,
  categoryConfigsByKey,
  getCategoryConfig,
  getCategoryIcon,
  getCategoryColor,
  getCategoryGradient
}
