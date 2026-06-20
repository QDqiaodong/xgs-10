export const ImageOrientation = {
  LANDSCAPE: 'landscape',
  PORTRAIT: 'portrait',
  SQUARE: 'square',
  DETAIL: 'detail'
}

export const ImageProcessingStatus = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED'
}

export function getImageUrl(img) {
  if (!img) return ''
  if (typeof img === 'string') return img
  if (typeof img === 'object' && img.url) return img.url
  return ''
}

export function getOriginalImageUrl(img) {
  if (!img || typeof img !== 'object') return getImageUrl(img)
  return img.originalUrl || img.url || ''
}

export function getCompressedImageUrl(img) {
  if (!img || typeof img !== 'object') return getImageUrl(img)
  return img.compressedUrl || img.url || ''
}

export function getThumbnailImageUrl(img) {
  if (!img || typeof img !== 'object') return getImageUrl(img)
  return img.thumbnailUrl || img.compressedUrl || img.url || ''
}

export function getDisplayImageUrl(img) {
  return getCompressedImageUrl(img)
}

export function getImageWidth(img) {
  if (!img || typeof img !== 'object') return null
  return img.width || null
}

export function getImageHeight(img) {
  if (!img || typeof img !== 'object') return null
  return img.height || null
}

export function getOriginalImageWidth(img) {
  if (!img || typeof img !== 'object') return getImageWidth(img)
  return img.originalWidth || img.width || null
}

export function getOriginalImageHeight(img) {
  if (!img || typeof img !== 'object') return getImageHeight(img)
  return img.originalHeight || img.height || null
}

export function getDisplayRatio(img) {
  if (!img || typeof img !== 'object') return null
  if (img.displayRatio != null) return img.displayRatio
  const width = getImageWidth(img)
  const height = getImageHeight(img)
  if (width && height && height !== 0) {
    return width / height
  }
  return null
}

export function isMainImage(img) {
  if (!img || typeof img !== 'object') return false
  return img.isMain === true
}

export function getImageSortOrder(img) {
  if (!img || typeof img !== 'object') return 0
  return img.sortOrder != null ? img.sortOrder : 0
}

export function getImageFormat(img) {
  if (!img || typeof img !== 'object') return null
  return img.format || null
}

export function getImageFileSize(img) {
  if (!img || typeof img !== 'object') return null
  return img.fileSize || null
}

export function getCompressedFileSize(img) {
  if (!img || typeof img !== 'object') return null
  return img.compressedFileSize || null
}

export function getProcessingStatus(img) {
  if (!img || typeof img !== 'object') return ImageProcessingStatus.COMPLETED
  return img.processingStatus || ImageProcessingStatus.COMPLETED
}

export function normalizeImageList(images) {
  if (!images || !Array.isArray(images)) return []
  
  let normalized = images.map((img, index) => {
    if (typeof img === 'string') {
      return {
        url: img,
        originalUrl: img,
        compressedUrl: img,
        thumbnailUrl: img,
        width: null,
        height: null,
        originalWidth: null,
        originalHeight: null,
        isMain: index === 0,
        sortOrder: index,
        displayRatio: null,
        format: null,
        fileSize: null,
        compressedFileSize: null,
        processingStatus: ImageProcessingStatus.COMPLETED
      }
    }
    const width = img.width != null ? img.width : null
    const height = img.height != null ? img.height : null
    let displayRatio = img.displayRatio
    if (displayRatio == null && width && height && height !== 0) {
      displayRatio = width / height
    }
    return {
      url: img.url || '',
      originalUrl: img.originalUrl || img.url || '',
      compressedUrl: img.compressedUrl || img.url || '',
      thumbnailUrl: img.thumbnailUrl || img.compressedUrl || img.url || '',
      width: width,
      height: height,
      originalWidth: img.originalWidth || width,
      originalHeight: img.originalHeight || height,
      isMain: img.isMain != null ? img.isMain : (index === 0),
      sortOrder: img.sortOrder != null ? img.sortOrder : index,
      displayRatio: displayRatio,
      format: img.format || null,
      fileSize: img.fileSize || null,
      compressedFileSize: img.compressedFileSize || null,
      processingStatus: img.processingStatus || ImageProcessingStatus.COMPLETED
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
