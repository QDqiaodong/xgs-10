export const foodConfig = {
  key: 'food',
  name: '食品饮料',
  emoji: '🍬',
  primaryColor: '#f57c00',
  secondaryColor: '#ffe0b2',
  gradient: 'linear-gradient(135deg, #ffa726 0%, #f57c00 100%)',
  bgGradient: 'linear-gradient(135deg, #fff8e1 0%, #ffe0b2 100%)',
  borderColor: '#ffa726',
  textColor: '#e65100',
  description: '糖果、饮料、零食等',
  keywords: ['食品', '饮料', '糖果', '零食', '罐头', '饼干', '汽水', '麦乳精'],
  svg: {
    viewBox: '0 0 64 64',
    paths: [
      {
        d: 'M10 20c0-6 6-10 12-10h20c6 0 12 4 12 10v4H10v-4z',
        fill: 'currentColor',
        opacity: 0.25
      },
      {
        d: 'M12 24h40v24c0 4-4 8-10 8H22c-6 0-10-4-10-8V24z',
        fill: 'currentColor',
        opacity: 0.2
      },
      {
        d: 'M16 28h32v16H16z',
        fill: 'currentColor',
        opacity: 0.4
      },
      {
        cx: 24,
        cy: 36,
        r: 3,
        fill: 'currentColor',
        opacity: 0.7
      },
      {
        cx: 34,
        cy: 34,
        r: 2.5,
        fill: 'currentColor',
        opacity: 0.7
      },
      {
        cx: 42,
        cy: 38,
        r: 2,
        fill: 'currentColor',
        opacity: 0.7
      },
      {
        d: 'M22 12l4-6h12l4 6H22z',
        fill: 'currentColor',
        opacity: 0.35
      }
    ]
  }
}

export default foodConfig
