export const ImageOrientation = {
  LANDSCAPE: 'landscape',
  PORTRAIT: 'portrait',
  SQUARE: 'square',
  DETAIL: 'detail'
}

export function getImageUrl(img) {
  if (!img) return ''
  if (typeof img === 'string') return img
  if (typeof img === 'object' && img.url) return img.url
  return ''
}

export function getImageWidth(img) {
  if (!img || typeof img !== 'object') return null
  return img.width || null
}

export function getImageHeight(img) {
  if (!img || typeof img !== 'object') return null
  return img.height || null
}

export function isMainImage(img) {
  if (!img || typeof img !== 'object') return false
  return img.isMain === true
}

export function getImageSortOrder(img) {
  if (!img || typeof img !== 'object') return 0
  return img.sortOrder != null ? img.sortOrder : 0
}

export function normalizeImageList(images) {
  if (!images || !Array.isArray(images)) return []
  
  let normalized = images.map((img, index) => {
    if (typeof img === 'string') {
      return {
        url: img,
        width: null,
        height: null,
        isMain: index === 0,
        sortOrder: index
      }
    }
    return {
      url: img.url || '',
      width: img.width != null ? img.width : null,
      height: img.height != null ? img.height : null,
      isMain: img.isMain != null ? img.isMain : (index === 0),
      sortOrder: img.sortOrder != null ? img.sortOrder : index
    }
  })
  
  normalized.sort((a, b) => a.sortOrder - b.sortOrder)
  
  const hasMain = normalized.some(img => img.isMain)
  if (!hasMain && normalized.length > 0) {
    normalized[0].isMain = true
  }
  
  return normalized
}

export function getMainImage(images) {
  const normalized = normalizeImageList(images)
  const main = normalized.find(img => img.isMain)
  return main || (normalized.length > 0 ? normalized[0] : null)
}

export function detectImageOrientation(width, height) {
  if (!width || !height) return ImageOrientation.SQUARE
  const ratio = width / height
  if (ratio >= 1.3) return ImageOrientation.LANDSCAPE
  if (ratio <= 0.77) return ImageOrientation.PORTRAIT
  return ImageOrientation.SQUARE
}

export function getImageOrientation(img) {
  if (!img) return ImageOrientation.SQUARE
  const width = typeof img === 'object' ? img.width : null
  const height = typeof img === 'object' ? img.height : null
  if (width && height) {
    return detectImageOrientation(width, height)
  }
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

export function getImageSizeInfo(img) {
  if (!img || typeof img !== 'object') return null
  if (img.width && img.height) {
    return {
      width: img.width,
      height: img.height,
      orientation: detectImageOrientation(img.width, img.height),
      hasSize: true
    }
  }
  return null
}

export function classifyImages(images) {
  if (!images || images.length === 0) return []
  const normalized = normalizeImageList(images)
  return normalized.map((img, index) => {
    return {
      url: img.url,
      originalIndex: index,
      isFirst: index === 0,
      isMain: img.isMain,
      sortOrder: img.sortOrder,
      width: img.width,
      height: img.height,
      isDetail: index >= 3 && normalized.length > 3
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
