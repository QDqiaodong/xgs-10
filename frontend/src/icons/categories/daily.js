export const dailyConfig = {
  key: 'daily',
  name: '日常用品',
  emoji: '🪑',
  primaryColor: '#5d4037',
  secondaryColor: '#d7ccc8',
  gradient: 'linear-gradient(135deg, #8d6e63 0%, #5d4037 100%)',
  bgGradient: 'linear-gradient(135deg, #efebe9 0%, #d7ccc8 100%)',
  borderColor: '#8d6e63',
  textColor: '#3e2723',
  description: '家具、器皿、日常小物等',
  keywords: ['日常', '用品', '家具', '器皿', '生活', '工具', '家用'],
  svg: {
    viewBox: '0 0 64 64',
    paths: [
      {
        d: 'M14 20h36v4H14z',
        fill: 'currentColor',
        opacity: 0.45
      },
      {
        d: 'M18 24v22h4V24zM42 24v22h4V24z',
        fill: 'currentColor',
        opacity: 0.35
      },
      {
        d: 'M14 46h36v3H14z',
        fill: 'currentColor',
        opacity: 0.4
      },
      {
        d: 'M24 24c0-3 3-6 8-6s8 3 8 6',
        fill: 'none',
        stroke: 'currentColor',
        strokeWidth: 2.5,
        opacity: 0.55,
        strokeLinecap: 'round'
      }
    ]
  }
}

export default dailyConfig
