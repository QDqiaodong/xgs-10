export const homeAppliancesConfig = {
  key: 'homeAppliances',
  name: '家用电器',
  emoji: '🏠',
  primaryColor: '#c62828',
  secondaryColor: '#ffcdd2',
  gradient: 'linear-gradient(135deg, #ef5350 0%, #c62828 100%)',
  bgGradient: 'linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%)',
  borderColor: '#ef5350',
  textColor: '#b71c1c',
  description: '电视机、洗衣机、冰箱等',
  keywords: ['家电', '电器', '电视', '冰箱', '洗衣机', '风扇', '空调', '收音机'],
  svg: {
    viewBox: '0 0 64 64',
    paths: [
      {
        d: 'M10 18h44a2 2 0 0 1 2 2v28a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2V20a2 2 0 0 1 2-2z',
        fill: 'currentColor',
        opacity: 0.15
      },
      {
        d: 'M14 22h36v20H14z',
        fill: 'currentColor',
        opacity: 0.25
      },
      {
        d: 'M16 24h32v16H16z',
        fill: 'currentColor',
        opacity: 0.5
      },
      {
        d: 'M26 48h12v2H26z',
        fill: 'currentColor',
        opacity: 0.6
      },
      {
        d: 'M30 12h4v6h-4z',
        fill: 'currentColor',
        opacity: 0.4
      },
      {
        cx: 48,
        cy: 46,
        r: 2,
        fill: 'currentColor',
        opacity: 0.8
      }
    ]
  }
}

export default homeAppliancesConfig
