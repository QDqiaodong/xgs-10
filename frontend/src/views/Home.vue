<template>
  <div class="home container">
    <section class="hero">
      <h1 class="hero-title">时光里的珍贵回忆</h1>
      <p class="hero-subtitle">分享你的老物件，讲述那些年的故事</p>
      <div class="era-decorations">
        <span class="deco-star deco-60s">★</span>
        <span class="deco-flower deco-70s">✿</span>
        <span class="deco-note deco-80s">♪</span>
        <span class="deco-bolt deco-90s">⚡</span>
        <span class="deco-chip deco-00s">◈</span>
      </div>
    </section>

    <section class="filter-section card">
      <div class="filter-group">
        <span class="filter-label">品类筛选：</span>
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="['filter-btn', 'filter-cat', { active: selectedCategory === cat.id }]"
          @click="toggleCategory(cat.id)"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          {{ cat.name }}
        </button>
        <button v-if="selectedCategory" class="filter-btn clear" @click="selectedCategory = null">
          清除筛选
        </button>
      </div>
      <div class="filter-group">
        <span class="filter-label">年代筛选：</span>
        <button
          v-for="era in eras"
          :key="era.id"
          :class="['filter-btn', 'filter-era', `era-${getEraClass(era.name)}`, { active: selectedEra === era.id }]"
          @click="toggleEra(era.id)"
        >
          <span class="era-dot"></span>
          {{ era.name }}
        </button>
        <button v-if="selectedEra" class="filter-btn clear" @click="selectedEra = null">
          清除筛选
        </button>
      </div>
    </section>

    <section class="hot-section" v-if="hotPosts.length > 0">
      <h2 class="section-title">
        <span class="title-icon">🔥</span>
        热门回忆
      </h2>
      <div class="hot-grid">
        <router-link
          v-for="post in hotPosts.slice(0, 3)"
          :key="post.id"
          :to="`/post/${post.id}`"
          :class="['hot-card', 'card', `card-era-${getEraClass(post.eraName)}`]"
        >
          <div :class="['hot-image', getCardImageClass(getImageOrientation(post.images && post.images[0] ? getImageUrl(post.images[0]) : ''))]">
            <div :class="['image-overlay', `overlay-${getEraClass(post.eraName)}`]"></div>
            <img :src="post.images && post.images[0] ? getImageUrl(post.images[0]) : 'https://picsum.photos/400/300'" :alt="post.title" />
            <div :class="['era-badge', `badge-${getEraClass(post.eraName)}`]">
              {{ post.eraName }}
            </div>
          </div>
          <div :class="['hot-content', `content-${getEraClass(post.eraName)}`]">
            <h3 :class="['hot-title', `title-${getEraClass(post.eraName)}`]">{{ post.title }}</h3>
            <div class="hot-meta">
              <span class="tag tag-cat">{{ post.categoryName }}</span>
              <span :class="['tag', `tag-era-${getEraClass(post.eraName)}`]">{{ post.eraName }}</span>
            </div>
            <p class="hot-views">👁 {{ post.viewCount }} 次浏览</p>
          </div>
        </router-link>
      </div>
    </section>

    <section class="posts-section">
      <h2 class="section-title">
        <span class="title-icon">📜</span>
        最新分享
      </h2>
      <div class="posts-grid" v-if="posts.length > 0">
        <router-link
          v-for="post in posts"
          :key="post.id"
          :to="`/post/${post.id}`"
          :class="['post-card', 'card', `card-era-${getEraClass(post.eraName)}`]"
        >
          <div :class="['post-image', getCardImageClass(getImageOrientation(post.images && post.images[0] ? getImageUrl(post.images[0]) : ''))]">
            <div :class="['image-overlay', `overlay-${getEraClass(post.eraName)}`]"></div>
            <img :src="post.images && post.images[0] ? getImageUrl(post.images[0]) : 'https://picsum.photos/400/300'" :alt="post.title" />
            <div :class="['era-badge', `badge-${getEraClass(post.eraName)}`]">
              <span class="badge-icon">{{ getEraIcon(post.eraName) }}</span>
              {{ post.eraName }}
            </div>
          </div>
          <div :class="['post-content', `content-${getEraClass(post.eraName)}`]">
            <h3 :class="['post-title', `title-${getEraClass(post.eraName)}`]">{{ post.title }}</h3>
            <p :class="['post-item', `item-${getEraClass(post.eraName)}`]">物件：{{ safeDisplayItemName(post.itemName) }}</p>
            <p class="post-excerpt">{{ post.content }}</p>
            <div class="post-tags">
              <span class="tag tag-cat">{{ post.categoryName }}</span>
              <span :class="['tag', `tag-era-${getEraClass(post.eraName)}`]">{{ post.eraName }}</span>
            </div>
            <div :class="['post-footer', `footer-${getEraClass(post.eraName)}`]">
              <span class="post-author">{{ post.authorName }}</span>
              <div class="post-stats">
                <span>👁 {{ post.viewCount }}</span>
                <span>💬 {{ post.commentCount }}</span>
                <span>⭐ {{ post.favoriteCount }}</span>
              </div>
            </div>
          </div>
        </router-link>
      </div>
      <div class="empty-state" v-else>
        <p>暂无相关内容，去发布第一个回忆吧~</p>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button
          :class="['page-btn', { disabled: currentPage === 0 }]"
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 0"
        >
          上一页
        </button>
        <span class="page-info">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
        <button
          :class="['page-btn', { disabled: currentPage >= totalPages - 1 }]"
          @click="changePage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
        >
          下一页
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { categoriesAPI, erasAPI, postsAPI } from '../api'
import { displayItemName } from '../utils/textCleaner'
import { detectImageOrientationFromUrl, getCardImageClass, ImageOrientation } from '../utils/imageLayout'

const router = useRouter()

const categories = ref([])
const eras = ref([])
const posts = ref([])
const hotPosts = ref([])
const selectedCategory = ref(null)
const selectedEra = ref(null)
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 10
const imageOrientations = ref({})

const detectPostImagesOrientation = async (postsList) => {
  if (!postsList || postsList.length === 0) return
  for (const post of postsList) {
    if (post.images && post.images.length > 0) {
      const firstImg = getImageUrl(post.images[0])
      if (firstImg && !imageOrientations.value[firstImg]) {
        try {
          const orientation = await detectImageOrientationFromUrl(firstImg)
          imageOrientations.value[firstImg] = orientation
        } catch (e) {
          imageOrientations.value[firstImg] = ImageOrientation.SQUARE
        }
      }
    }
  }
}

const getImageOrientation = (imgUrl) => {
  return imageOrientations.value[imgUrl] || ImageOrientation.SQUARE
}

const getEraClass = (eraName) => {
  if (!eraName) return '80s'
  if (eraName.includes('60')) return '60s'
  if (eraName.includes('70')) return '70s'
  if (eraName.includes('80')) return '80s'
  if (eraName.includes('90')) return '90s'
  if (eraName.includes('00')) return '00s'
  return '80s'
}

const getEraIcon = (eraName) => {
  const cls = getEraClass(eraName)
  const icons = {
    '60s': '★',
    '70s': '✿',
    '80s': '♪',
    '90s': '⚡',
    '00s': '◈'
  }
  return icons[cls] || '♪'
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}

const loadCategories = async () => {
  try {
    const res = await categoriesAPI.getAll()
    categories.value = res.data
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const loadEras = async () => {
  try {
    const res = await erasAPI.getAll()
    eras.value = res.data
  } catch (e) {
    console.error('加载年代失败', e)
  }
}

const loadPosts = async () => {
  try {
    const res = await postsAPI.getList({
      categoryId: selectedCategory.value,
      eraId: selectedEra.value,
      page: currentPage.value,
      size: pageSize
    })
    posts.value = res.data.content
    totalPages.value = res.data.totalPages
    nextTick(() => detectPostImagesOrientation(posts.value))
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

const loadHotPosts = async () => {
  try {
    const res = await postsAPI.getHot()
    hotPosts.value = res.data
    nextTick(() => detectPostImagesOrientation(hotPosts.value))
  } catch (e) {
    console.error('加载热门失败', e)
  }
}

const safeDisplayItemName = (name) => displayItemName(name)

const toggleCategory = (id) => {
  selectedCategory.value = selectedCategory.value === id ? null : id
  currentPage.value = 0
  loadPosts()
}

const toggleEra = (id) => {
  selectedEra.value = selectedEra.value === id ? null : id
  currentPage.value = 0
  loadPosts()
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadPosts()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

onMounted(() => {
  loadCategories()
  loadEras()
  loadPosts()
  loadHotPosts()
})
</script>

<style scoped>
.hero {
  text-align: center;
  padding: 40px 0 50px;
  position: relative;
}

.hero-title {
  font-size: 36px;
  color: #6b4f0f;
  margin-bottom: 12px;
  font-weight: 600;
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 18px;
  color: #8b6914;
  opacity: 0.8;
}

.era-decorations {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 40px;
  font-size: 28px;
}

.deco-star {
  color: #5d4037;
  animation: pulse 3s ease-in-out infinite;
}

.deco-flower {
  color: #8b4513;
  animation: pulse 3s ease-in-out infinite 0.3s;
}

.deco-note {
  color: #1565c0;
  animation: pulse 3s ease-in-out infinite 0.6s;
}

.deco-bolt {
  color: #e65100;
  animation: pulse 3s ease-in-out infinite 0.9s;
}

.deco-chip {
  color: #00838f;
  animation: pulse 3s ease-in-out infinite 1.2s;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.2); opacity: 1; }
}

.filter-section {
  padding: 24px;
  margin-bottom: 30px;
}

.filter-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-weight: 500;
  color: #5d4e37;
  margin-right: 4px;
}

.filter-btn {
  padding: 6px 16px;
  border-radius: 20px;
  background: #f5e6d3;
  color: #8b6914;
  font-size: 14px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-btn:hover {
  transform: translateY(-1px);
}

.filter-cat {
  background: #f5e6d3;
  color: #8b6914;
}

.filter-cat:hover {
  background: #e8d5b8;
}

.filter-cat.active {
  background: linear-gradient(135deg, #d4a574 0%, #c19660 100%);
  color: #fff;
}

.filter-era {
  position: relative;
  padding-left: 30px;
  font-weight: 600;
  border-width: 2px;
  padding-top: 7px;
  padding-bottom: 7px;
}

.filter-era .era-dot {
  position: absolute;
  left: 12px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  transition: all 0.3s;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.8);
}

.era-60s { background: #e8ddd4; color: #3e2723; border: 2px solid #5d4037; }
.era-60s .era-dot { background: #5d4037; }
.era-60s:hover { background: #d7ccc8; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(93, 64, 55, 0.2); }
.era-60s.active { background: linear-gradient(135deg, #6d4c41 0%, #4e342e 100%); color: #fff; border-color: #4e342e; }
.era-60s.active .era-dot { background: #ffd54f; box-shadow: 0 0 8px #ffd54f; }

.era-70s { background: #ffecd1; color: #5d4037; border: 2px solid #a1887f; }
.era-70s .era-dot { background: #bf360c; }
.era-70s:hover { background: #ffe0b2; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(191, 54, 12, 0.2); }
.era-70s.active { background: linear-gradient(135deg, #bf360c 0%, #8d6e63 100%); color: #fff; border-color: #bf360c; }
.era-70s.active .era-dot { background: #fff176; box-shadow: 0 0 8px #fff176; }

.era-80s { background: #d4e9fc; color: #0d47a1; border: 2px solid #1976d2; }
.era-80s .era-dot { background: #1976d2; }
.era-80s:hover { background: #bbdefb; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(25, 118, 210, 0.25); }
.era-80s.active { background: linear-gradient(135deg, #1976d2 0%, #0d47a1 100%); color: #fff; border-color: #0d47a1; }
.era-80s.active .era-dot { background: #81d4fa; box-shadow: 0 0 8px #81d4fa; }

.era-90s { background: #ffe0cc; color: #bf360c; border: 2px solid #ff6f00; }
.era-90s .era-dot { background: #ff6f00; }
.era-90s:hover { background: #ffcc99; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(255, 111, 0, 0.25); }
.era-90s.active { background: linear-gradient(135deg, #ff6f00 0%, #e64a19 100%); color: #fff; border-color: #e65100; }
.era-90s.active .era-dot { background: #ffd180; box-shadow: 0 0 8px #ffd180; }

.era-00s { background: #cff5f8; color: #006064; border: 2px solid #00acc1; }
.era-00s .era-dot { background: #00acc1; }
.era-00s:hover { background: #b2ebf2; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0, 172, 193, 0.25); }
.era-00s.active { background: linear-gradient(135deg, #00acc1 0%, #006064 100%); color: #fff; border-color: #006064; }
.era-00s.active .era-dot { background: #84ffff; box-shadow: 0 0 8px #84ffff; }

.filter-btn.clear {
  background: #fff;
  border: 1px dashed #d4a574;
  color: #d4a574;
}

.cat-icon {
  font-size: 16px;
}

.section-title {
  font-size: 24px;
  color: #6b4f0f;
  margin-bottom: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 22px;
}

.hot-section {
  margin-bottom: 40px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.hot-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.post-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-era-60s {
  border-top: 5px solid #5d4037;
  border-left: 2px solid rgba(93, 64, 55, 0.15);
  border-right: 2px solid rgba(93, 64, 55, 0.15);
  border-bottom: 2px solid rgba(93, 64, 55, 0.15);
  background: linear-gradient(135deg, #fff 0%, #f5f0eb 50%, #efe5db 100%);
}
.card-era-60s:hover {
  box-shadow: 0 8px 24px rgba(93, 64, 55, 0.25);
  border-left-color: rgba(93, 64, 55, 0.3);
  border-right-color: rgba(93, 64, 55, 0.3);
}

.card-era-70s {
  border-top: 5px solid #8d6e63;
  border-left: 2px solid rgba(141, 110, 99, 0.15);
  border-right: 2px solid rgba(141, 110, 99, 0.15);
  border-bottom: 2px solid rgba(141, 110, 99, 0.15);
  background: linear-gradient(135deg, #fff 0%, #fff5e6 50%, #ffeacc 100%);
}
.card-era-70s:hover {
  box-shadow: 0 8px 24px rgba(141, 110, 99, 0.25);
  border-left-color: rgba(141, 110, 99, 0.3);
  border-right-color: rgba(141, 110, 99, 0.3);
}

.card-era-80s {
  border-top: 5px solid #1976d2;
  border-left: 2px solid rgba(25, 118, 210, 0.15);
  border-right: 2px solid rgba(25, 118, 210, 0.15);
  border-bottom: 2px solid rgba(25, 118, 210, 0.15);
  background: linear-gradient(135deg, #fff 0%, #eff6fc 50%, #dcebf9 100%);
}
.card-era-80s:hover {
  box-shadow: 0 8px 24px rgba(25, 118, 210, 0.28);
  border-left-color: rgba(25, 118, 210, 0.3);
  border-right-color: rgba(25, 118, 210, 0.3);
}

.card-era-90s {
  border-top: 5px solid #ff6f00;
  border-left: 2px solid rgba(255, 111, 0, 0.15);
  border-right: 2px solid rgba(255, 111, 0, 0.15);
  border-bottom: 2px solid rgba(255, 111, 0, 0.15);
  background: linear-gradient(135deg, #fff 0%, #fff4e8 50%, #ffe6d1 100%);
}
.card-era-90s:hover {
  box-shadow: 0 8px 24px rgba(255, 111, 0, 0.28);
  border-left-color: rgba(255, 111, 0, 0.3);
  border-right-color: rgba(255, 111, 0, 0.3);
}

.card-era-00s {
  border-top: 5px solid #00acc1;
  border-left: 2px solid rgba(0, 172, 193, 0.15);
  border-right: 2px solid rgba(0, 172, 193, 0.15);
  border-bottom: 2px solid rgba(0, 172, 193, 0.15);
  background: linear-gradient(135deg, #fff 0%, #edfafb 50%, #d6f3f6 100%);
}
.card-era-00s:hover {
  box-shadow: 0 8px 24px rgba(0, 172, 193, 0.28);
  border-left-color: rgba(0, 172, 193, 0.3);
  border-right-color: rgba(0, 172, 193, 0.3);
}

.hot-image, .post-image {
  position: relative;
  overflow: hidden;
}

.hot-image {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0eb;
}
.post-image {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0eb;
}

.hot-image.img-landscape { height: 170px; }
.hot-image.img-portrait { height: 220px; }
.hot-image.img-square { height: 190px; }
.hot-image.img-detail { height: 170px; }

.post-image.img-landscape { height: 200px; }
.post-image.img-portrait { height: 280px; }
.post-image.img-square { height: 240px; }
.post-image.img-detail { height: 200px; }

.hot-image img, .post-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.5s;
  background: #faf7f2;
}

.hot-image.img-landscape img,
.post-image.img-landscape img {
  object-fit: cover;
}

.hot-image.img-square img,
.post-image.img-square img {
  object-fit: cover;
}

.hot-card:hover .hot-image img,
.post-card:hover .post-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0.22;
  mix-blend-mode: multiply;
}

.overlay-60s {
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(93, 64, 55, 0.45) 2px,
    rgba(93, 64, 55, 0.45) 3px
  ),
  linear-gradient(180deg, rgba(93, 64, 55, 0.15) 0%, transparent 100%);
}

.overlay-70s {
  background: radial-gradient(circle at 30% 30%, rgba(255, 183, 77, 0.5) 0%, transparent 50%),
              radial-gradient(circle at 70% 70%, rgba(212, 106, 58, 0.4) 0%, transparent 50%),
              linear-gradient(180deg, rgba(191, 54, 12, 0.1) 0%, transparent 100%);
}

.overlay-80s {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.35) 0%, transparent 50%),
              linear-gradient(225deg, rgba(156, 39, 176, 0.25) 0%, transparent 50%),
              linear-gradient(180deg, rgba(25, 118, 210, 0.1) 0%, transparent 100%);
}

.overlay-90s {
  background: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 4px,
    rgba(255, 111, 0, 0.25) 4px,
    rgba(255, 111, 0, 0.25) 5px
  ),
  linear-gradient(180deg, rgba(255, 111, 0, 0.12) 0%, transparent 100%);
}

.overlay-00s {
  background: linear-gradient(180deg, rgba(77, 208, 225, 0.3) 0%, rgba(0, 150, 136, 0.22) 100%);
}

.era-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.badge-icon {
  font-size: 14px;
}

.badge-60s {
  background: rgba(93, 64, 55, 0.95);
  color: #ffe082;
  border: 1.5px solid rgba(255, 224, 130, 0.4);
  font-family: 'STKaiti', 'KaiTi', serif;
  letter-spacing: 1.5px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.badge-70s {
  background: rgba(141, 110, 99, 0.95);
  color: #fff59d;
  border: 1.5px solid rgba(255, 245, 157, 0.4);
  font-family: 'STFangsong', 'FangSong', serif;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.25);
}

.badge-80s {
  background: rgba(25, 118, 210, 0.95);
  color: #e3f2fd;
  border: 1.5px solid rgba(227, 242, 253, 0.4);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.badge-90s {
  background: rgba(255, 111, 0, 0.95);
  color: #fff8e1;
  border: 1.5px solid rgba(255, 248, 225, 0.4);
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 0 rgba(0,0,0,0.2);
}

.badge-00s {
  background: rgba(0, 172, 193, 0.95);
  color: #e0f7fa;
  border: 1.5px solid rgba(224, 247, 250, 0.4);
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.hot-content {
  padding: 16px;
  flex: 1;
}

.post-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-60s {
  background: repeating-linear-gradient(
    180deg,
    transparent,
    transparent 31px,
    rgba(93, 64, 55, 0.05) 31px,
    rgba(93, 64, 55, 0.05) 32px
  );
}

.content-70s {
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%238d6e63' fill-opacity='0.04'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.content-80s {
  background: linear-gradient(90deg, rgba(227, 242, 253, 0.4) 0%, transparent 30%, transparent 70%, rgba(227, 242, 253, 0.4) 100%);
}

.content-90s {
  background: radial-gradient(circle at 95% 5%, rgba(255, 111, 0, 0.06) 0%, transparent 40%);
}

.content-00s {
  background: linear-gradient(135deg, rgba(224, 247, 250, 0.3) 0%, transparent 50%);
}

.hot-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  line-height: 1.4;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.4;
}

.title-60s {
  color: #3e2723;
  font-family: 'STKaiti', 'KaiTi', 'SimSun', serif;
  letter-spacing: 1px;
}

.title-70s {
  color: #5d4037;
  font-family: 'STFangsong', 'FangSong', 'SimHei', sans-serif;
  font-weight: 700;
}

.title-80s {
  color: #0d47a1;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.title-90s {
  color: #bf360c;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.title-00s {
  color: #006064;
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  font-weight: 500;
}

.post-item {
  font-size: 14px;
  margin-bottom: 10px;
  font-style: italic;
}

.item-60s {
  color: #6d4c41;
  font-family: 'STKaiti', 'KaiTi', serif;
}

.item-70s {
  color: #8d6e63;
  font-family: 'STFangsong', 'FangSong', serif;
}

.item-80s {
  color: #1565c0;
}

.item-90s {
  color: #e65100;
  font-weight: 500;
}

.item-00s {
  color: #00838f;
}

.post-excerpt {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-meta {
  margin-bottom: 8px;
}

.post-tags {
  margin-bottom: 16px;
}

.tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  margin-right: 8px;
}

.tag-cat {
  background: #f5e6d3;
  color: #8b6914;
}

.tag-era-60s {
  background: #d7ccc8;
  color: #3e2723;
  font-family: 'STKaiti', 'KaiTi', serif;
  border: 1.5px solid #8d6e63;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.tag-era-70s {
  background: #ffcc80;
  color: #4e342e;
  font-family: 'STFangsong', 'FangSong', serif;
  border: 1.5px solid #ef6c00;
  font-weight: 600;
}

.tag-era-80s {
  background: #90caf9;
  color: #0d47a1;
  border: 1.5px solid #1976d2;
  font-weight: 700;
}

.tag-era-90s {
  background: #ffab40;
  color: #bf360c;
  border: 1.5px solid #ff6f00;
  font-weight: 700;
}

.tag-era-00s {
  background: #80deea;
  color: #006064;
  border: 1.5px solid #00acc1;
  font-weight: 600;
}

.hot-views {
  font-size: 13px;
  color: #999;
}

.post-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
}

.footer-60s {
  border-top: 1px dashed #bcaaa4;
}

.footer-70s {
  border-top: 1px dashed #d7ccc8;
}

.footer-80s {
  border-top: 1px solid #bbdefb;
}

.footer-90s {
  border-top: 2px dotted #ffab40;
}

.footer-00s {
  border-top: 1px solid #b2ebf2;
}

.post-author {
  font-size: 13px;
  color: #999;
}

.post-stats {
  display: flex;
  gap: 14px;
  font-size: 13px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
}

.page-btn {
  padding: 8px 20px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #d4a574;
  color: #d4a574;
  font-size: 14px;
  transition: all 0.3s;
}

.page-btn:hover:not(.disabled) {
  background: #d4a574;
  color: #fff;
}

.page-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .hot-grid {
    grid-template-columns: 1fr;
  }
  .posts-grid {
    grid-template-columns: 1fr;
  }
  .hero-title {
    font-size: 28px;
  }
  .era-decorations {
    gap: 24px;
    font-size: 22px;
  }
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}
</style>
