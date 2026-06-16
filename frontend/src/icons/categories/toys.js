export const toysConfig = {
  key: 'toys',
  name: '玩具玩偶',
  emoji: '🧸',
  primaryColor: '#e65100',
  secondaryColor: '#ffe0b2',
  gradient: 'linear-gradient(135deg, #ff9800 0%, #e65100 100%)',
  bgGradient: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
  borderColor: '#ff9800',
  textColor: '#bf360c',
  description: '铁皮玩具、布偶、积木等',
  keywords: ['玩具', '玩偶', '铁皮', '积木', '娃娃', '游戏', '模型'],
  svg: {
    viewBox: '0 0 64 64',
    paths: [
      {
        cx: 32,
        cy: 20,
        r: 12,
        fill: 'currentColor',
        opacity: 0.3
      },
      {
        cx: 27,
        cy: 18,
        r: 1.5,
        fill: 'currentColor',
        opacity: 0.7
      },
      {
        cx: 37,
        cy: 18,
        r: 1.5,
        fill: 'currentColor',
        opacity: 0.7
      },
      {
        d: 'M28 24c0 1.5 1.8 3 4 3s4-1.5 4-3',
        fill: 'none',
        stroke: 'currentColor',
        strokeWidth: 1.5,
        opacity: 0.7,
        strokeLinecap: 'round'
      },
      {
        d: 'M20 32h24v20H20z',
        fill: 'currentColor',
        opacity: 0.2
      },
      {
        cx: 20,
        cy: 14,
        r: 4,
        fill: 'currentColor',
        opacity: 0.4
      },
      {
        cx: 44,
        cy: 14,
        r: 4,
        fill: 'currentColor',
        opacity: 0.4
      },
      {
        d: 'M24 52h4v4h-4zM36 52h4v4h-4z',
        fill: 'currentColor',
        opacity: 0.5
      }
    ]
  }
}

export default toysConfig
