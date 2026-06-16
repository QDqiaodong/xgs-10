export const clothingConfig = {
  key: 'clothing',
  name: '服饰配饰',
  emoji: '👕',
  primaryColor: '#ad1457',
  secondaryColor: '#f8bbd0',
  gradient: 'linear-gradient(135deg, #ec407a 0%, #ad1457 100%)',
  bgGradient: 'linear-gradient(135deg, #fce4ec 0%, #f8bbd0 100%)',
  borderColor: '#ec407a',
  textColor: '#880e4f',
  description: '衣服、帽子、手表等',
  keywords: ['服饰', '衣服', '配饰', '帽子', '手表', '围巾', '鞋子', '首饰'],
  svg: {
    viewBox: '0 0 64 64',
    paths: [
      {
        d: 'M22 12l-4 8 4 4v30h20V24l4-4-4-8-6 4h-8l-6-4z',
        fill: 'currentColor',
        opacity: 0.2
      },
      {
        d: 'M24 14l-2 5 2 2v28h16V21l2-2-2-5-4 3h-8l-4-3z',
        fill: 'currentColor',
        opacity: 0.4
      },
      {
        cx: 32,
        cy: 17,
        r: 2,
        fill: 'currentColor',
        opacity: 0.6
      },
      {
        d: 'M28 50h8v2h-8z',
        fill: 'currentColor',
        opacity: 0.5
      }
    ]
  }
}

export default clothingConfig
