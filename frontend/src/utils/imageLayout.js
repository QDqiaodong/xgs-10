export const ImageOrientation = {
  LANDSCAPE: 'landscape',
  PORTRAIT: 'portrait',
  SQUARE: 'square',
  DETAIL: 'detail'
}

export function detectImageOrientation(width, height) {
  if (!width || !height) return ImageOrientation.SQUARE
  const ratio = width / height
  if (ratio >= 1.3) return ImageOrientation.LANDSCAPE
  if (ratio <= 0.77) return ImageOrientation.PORTRAIT
  return ImageOrientation.SQUARE
}

export function detectImageOrientationFromUrl(url) {
  return new Promise((resolve) => {
    if (!url) {
      resolve(ImageOrientation.SQUARE)
      return
    }
    const img = new Image()
    img.onload = () => {
      resolve(detectImageOrientation(img.naturalWidth, img.naturalHeight))
    }
    img.onerror = () => {
      resolve(ImageOrientation.SQUARE)
    }
    img.src = url
  })
}

export function classifyImages(images) {
  if (!images || images.length === 0) return []
  return images.map((img, index) => {
    let url = img
    if (typeof img === 'object' && img.url) {
      url = img.url
    }
    return {
      url,
      originalIndex: index,
      isFirst: index === 0,
      isDetail: index >= 3 && images.length > 3
    }
  })
}

export function getLayoutForGrid(imageCount) {
  if (imageCount <= 0) return []
  if (imageCount === 1) {
    return [{ span: 'full', orientation: ImageOrientation.SQUARE }]
  }
  if (imageCount === 2) {
    return [
      { span: 'half', orientation: ImageOrientation.SQUARE },
      { span: 'half', orientation: ImageOrientation.SQUARE }
    ]
  }
  if (imageCount === 3) {
    return [
      { span: 'two-thirds', orientation: ImageOrientation.LANDSCAPE },
      { span: 'third', orientation: ImageOrientation.SQUARE },
      { span: 'third', orientation: ImageOrientation.SQUARE }
    ]
  }
  if (imageCount === 4) {
    return [
      { span: 'two-thirds', orientation: ImageOrientation.LANDSCAPE },
      { span: 'third', orientation: ImageOrientation.PORTRAIT },
      { span: 'third', orientation: ImageOrientation.SQUARE },
      { span: 'third', orientation: ImageOrientation.SQUARE }
    ]
  }
  const layout = [
    { span: 'two-thirds', orientation: ImageOrientation.LANDSCAPE },
    { span: 'third', orientation: ImageOrientation.PORTRAIT }
  ]
  for (let i = 2; i < imageCount; i++) {
    layout.push({ span: 'third', orientation: ImageOrientation.DETAIL })
  }
  return layout
}

export function getCardImageClass(orientation) {
  const classes = {
    [ImageOrientation.LANDSCAPE]: 'img-landscape',
    [ImageOrientation.PORTRAIT]: 'img-portrait',
    [ImageOrientation.SQUARE]: 'img-square',
    [ImageOrientation.DETAIL]: 'img-detail'
  }
  return classes[orientation] || 'img-square'
}
