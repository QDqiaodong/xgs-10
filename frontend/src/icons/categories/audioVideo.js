export const audioVideoConfig = {
  key: 'audioVideo',
  name: '影音设备',
  emoji: '📺',
  primaryColor: '#6a1b9a',
  secondaryColor: '#e1bee7',
  gradient: 'linear-gradient(135deg, #ab47bc 0%, #6a1b9a 100%)',
  bgGradient: 'linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)',
  borderColor: '#ab47bc',
  textColor: '#4a148c',
  description: '电视机、录音机、收音机等',
  keywords: ['影音', '电视', '录音机', '收音机', '磁带', '音响', 'DVD', 'VCD'],
  svg: {
    viewBox: '0 0 64 64',
    paths: [
      {
        d: 'M8 16h48a2 2 0 0 1 2 2v24a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V18a2 2 0 0 1 2-2z',
        fill: 'currentColor',
        opacity: 0.15
      },
      {
        d: 'M12 20h40v16H12z',
        fill: 'currentColor',
        opacity: 0.3
      },
      {
        d: 'M16 24h32v8H16z',
        fill: 'currentColor',
        opacity: 0.55
      },
      {
        cx: 52,
        cy: 44,
        r: 3,
        fill: 'currentColor',
        opacity: 0.7
      },
      {
        cx: 45,
        cy: 44,
        r: 1.5,
        fill: 'currentColor',
        opacity: 0.5
      },
      {
        d: 'M20 50h24v3H20z',
        fill: 'currentColor',
        opacity: 0.35
      },
      {
        d: 'M14 50h3v3h-3zM47 50h3v3h-3z',
        fill: 'currentColor',
        opacity: 0.5
      }
    ]
  }
}

export default audioVideoConfig
