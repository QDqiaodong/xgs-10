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

    <section class="category-showcase-section">
      <h2 class="section-title showcase-title">
        <span class="title-icon">🗂️</span>
        类别展柜
        <span class="title-subtitle">按品类探索，发现不同类型的珍贵老物件</span>
      </h2>
      <div class="showcase-container">
        <div class="showcase-track">
          <div
            v-for="(cat, index) in categoryShowcase"
            :key="cat.id"
            :class="['showcase-card', `showcase-cat-${getCategoryKey(cat.name)}`]"
            :style="getCategoryStyleVars(cat.name)"
            @click="selectShowcaseCategory(cat.id)"
          >
            <div class="showcase-connector" v-if="index < categoryShowcase.length - 1">
              <div class="connector-line"></div>
              <div class="connector-dot"></div>
            </div>
            <div class="showcase-header">
              <div class="showcase-cat-icon">
                <CategoryIcon :category="cat.name" size="xl" mode="emoji" />
              </div>
              <div class="showcase-cat-info">
                <h3 class="showcase-cat-name">{{ cat.name }}</h3>
                <span class="showcase-cat-desc">{{ getCategoryDescription(cat.name) }}</span>
              </div>
              <div class="showcase-post-count">
                <span class="count-num">{{ cat.postCount }}</span>
                <span class="count-label">件珍藏</span>
              </div>
            </div>
            <div class="showcase-preview" v-if="cat.representativePosts && cat.representativePosts.length > 0">
              <router-link
                v-for="post in cat.representativePosts.slice(0, 3)"
                :key="post.id"
                :to="`/post/${post.id}`"
                class="showcase-preview-item"
                @click.stop
              >
                <img
                  :src="getFirstImgUrl(post.images) || 'https://picsum.photos/120/120'"
                  :alt="post.title"
                />
              </router-link>
            </div>
            <div class="showcase-enter">
              <span>进入展柜</span>
              <span class="enter-arrow">→</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="timeline-section">
      <h2 class="section-title timeline-title">
        <span class="title-icon">⏳</span>
        年代时间廊
        <span class="title-subtitle">穿越时光，重温那些年的美好记忆</span>
      </h2>
      <div class="timeline-container">
        <div class="timeline-track">
          <div
            v-for="(era, index) in eraTimeline"
            :key="era.id"
            :class="['timeline-card', `timeline-era-${getEraClass(era.name)}`]"
            @click="selectTimelineEra(era.id)"
          >
            <div class="timeline-connector" v-if="index < eraTimeline.length - 1">
              <div class="connector-line"></div>
              <div class="connector-dot"></div>
            </div>
            <div class="timeline-header">
              <div class="timeline-era-icon">{{ era.icon || getEraIcon(era.name) }}</div>
              <div class="timeline-era-info">
                <h3 class="timeline-era-name">{{ era.name }}</h3>
                <span class="timeline-era-years">{{ era.yearStart }} - {{ era.yearEnd }}</span>
              </div>
              <div class="timeline-post-count">
                <span class="count-num">{{ era.postCount }}</span>
                <span class="count-label">件珍藏</span>
              </div>
            </div>
            <p class="timeline-description">{{ era.description }}</p>
            <div class="timeline-categories" v-if="era.representativeCategories">
              <span
                v-for="cat in era.representativeCategories.split(',')"
                :key="cat"
                class="timeline-cat-tag"
              >
                {{ cat }}
              </span>
            </div>
            <div class="timeline-preview" v-if="era.representativePosts && era.representativePosts.length > 0">
              <router-link
                v-for="post in era.representativePosts.slice(0, 3)"
                :key="post.id"
                :to="`/post/${post.id}`"
                class="timeline-preview-item"
                @click.stop
              >
                <img
                  :src="getFirstImgUrl(post.images) || 'https://picsum.photos/120/120'"
                  :alt="post.title"
                />
              </router-link>
            </div>
            <div class="timeline-enter">
              <span>进入时光</span>
              <span class="enter-arrow">→</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="filter-section card">
      <div class="filter-group">
        <span class="filter-label">品类筛选：</span>
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="['filter-btn', 'filter-cat', getCategoryClass(cat.name), { active: selectedCategory === cat.id }]"
          :style="getCategoryStyleVars(cat.name)"
          @click="toggleCategory(cat.id)"
        >
          <CategoryIcon :category="cat.name" size="sm" mode="emoji" />
          {{ cat.name }}
        </button>
        <button v-if="selectedCategory" class="filter-btn clear" @click="clearCategoryFilter">
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
        <button v-if="selectedEra" class="filter-btn clear" @click="clearEraFilter">
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
          <div :class="['hot-image', getCardImageClass(getFirstImgOrientation(post.images))]">
            <div :class="['image-overlay', `overlay-${getEraClass(post.eraName)}`]"></div>
            <img :src="getFirstImgUrl(post.images) || 'https://picsum.photos/400/300'" :alt="post.title" />
            <div :class="['era-badge', `badge-${getEraClass(post.eraName)}`]">
              {{ post.eraName }}
            </div>
          </div>
          <div :class="['hot-content', `content-${getEraClass(post.eraName)}`]">
            <h3 :class="['hot-title', `title-${getEraClass(post.eraName)}`]">{{ post.title }}</h3>
            <div class="hot-summary-structured" v-if="post.itemSource || (post.emotionKeywords && post.emotionKeywords.length > 0)">
              <span class="summary-chip summary-source" v-if="post.itemSource">
                <span class="chip-icon">📦</span>{{ post.itemSource }}
              </span>
              <span class="summary-chip summary-emotion" v-for="kw in (post.emotionKeywords || []).slice(0, 2)" :key="kw">
                <span class="chip-icon">💭</span>{{ kw }}
              </span>
            </div>
            <div class="hot-meta">
              <span :class="['tag', 'tag-cat', getCategoryClass(post.categoryName)]" :style="getCategoryStyleVars(post.categoryName)">
                <CategoryIcon :category="post.categoryName" size="xs" />
                {{ post.categoryName }}
              </span>
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
          <div :class="['post-image', getCardImageClass(getFirstImgOrientation(post.images))]">
            <div :class="['image-overlay', `overlay-${getEraClass(post.eraName)}`]"></div>
            <img :src="getFirstImgUrl(post.images) || 'https://picsum.photos/400/300'" :alt="post.title" />
            <div :class="['era-badge', `badge-${getEraClass(post.eraName)}`]">
              <span class="badge-icon">{{ getEraIcon(post.eraName) }}</span>
              {{ post.eraName }}
            </div>
          </div>
          <div :class="['post-content', `content-${getEraClass(post.eraName)}`]">
            <h3 :class="['post-title', `title-${getEraClass(post.eraName)}`]">{{ post.title }}</h3>
            <p :class="['post-item', `item-${getEraClass(post.eraName)}`]">物件：{{ safeDisplayItemName(post.itemName) }}</p>
            <div class="post-summary-structured" v-if="post.itemSource || post.usageScene || (post.emotionKeywords && post.emotionKeywords.length > 0)">
              <span class="summary-chip summary-source" v-if="post.itemSource">
                <span class="chip-icon">📦</span>{{ post.itemSource }}
              </span>
              <span class="summary-chip summary-scene" v-if="post.usageScene">
                <span class="chip-icon">📍</span>{{ post.usageScene }}
              </span>
              <span class="summary-chip summary-emotion" v-for="kw in (post.emotionKeywords || []).slice(0, 3)" :key="kw">
                <span class="chip-icon">💭</span>{{ kw }}
              </span>
            </div>
            <p class="post-excerpt" v-else>{{ post.storySummary || post.content }}</p>
            <div class="post-tags">
              <span :class="['tag', 'tag-cat', getCategoryClass(post.categoryName)]" :style="getCategoryStyleVars(post.categoryName)">
                <CategoryIcon :category="post.categoryName" size="xs" />
                {{ post.categoryName }}
              </span>
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
import { 
  detectImageOrientationFromUrl, 
  detectImageOrientation,
  getCardImageClass, 
  ImageOrientation, 
  getImageUrl, 
  getImageOrientation, 
  normalizeImageList,
  getMainImage
} from '../utils/imageLayout'
import { getCategoryClass, getCategoryStyleVars, getCategoryDescription } from '../icons/categoryUtils'
import { getCategoryConfig } from '../icons/categories'
import { getEraClass, getEraIcon, sortErasDefault, normalizeEraName } from '../utils/eraUtils'

const router = useRouter()

const categories = ref([])
const categoryShowcase = ref([])
const eras = ref([])
const eraTimeline = ref([])
const posts = ref([])
const hotPosts = ref([])
const selectedCategory = ref(null)
const selectedEra = ref(null)
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 10
const imageOrientations = ref({})

const getCategoryKey = (categoryName) => {
  return getCategoryConfig(categoryName).key
}

const detectPostImagesOrientation = async (postsList) => {
  if (!postsList || postsList.length === 0) return
  for (const post of postsList) {
    const normalizedImages = normalizeImageList(post.images)
    if (normalizedImages.length > 0) {
      const firstImg = normalizedImages[0]
      const imgUrl = firstImg.url
      if (imgUrl && !imageOrientations.value[imgUrl]) {
        if (firstImg.width && firstImg.height) {
          imageOrientations.value[imgUrl] = detectImageOrientation(firstImg.width, firstImg.height)
        } else {
          try {
            const orientation = await detectImageOrientationFromUrl(imgUrl)
            imageOrientations.value[imgUrl] = orientation
          } catch (e) {
            imageOrientations.value[imgUrl] = ImageOrientation.SQUARE
          }
        }
      }
    }
  }
}

const getImageOrientationByUrl = (imgUrl) => {
  return imageOrientations.value[imgUrl] || ImageOrientation.SQUARE
}

const getFirstImage = (images) => {
  const main = getMainImage(images)
  return main || null
}

const getFirstImgOrientation = (images) => {
  if (!images || images.length === 0) return ImageOrientation.SQUARE
  const firstImg = getMainImage(images) || (images[0] && typeof images[0] === 'object' ? images[0] : null)
  if (firstImg && firstImg.width && firstImg.height) {
    return detectImageOrientation(firstImg.width, firstImg.height)
  }
  const imgUrl = getImageUrl(images && images[0] ? images[0] : '')
  return getImageOrientationByUrl(imgUrl)
}

const getFirstImgUrl = (images) => {
  if (!images || images.length === 0) return ''
  const main = getMainImage(images)
  if (main) return main.url
  return getImageUrl(images[0])
}

const loadCategories = async () => {
  try {
    const res = await categoriesAPI.getAll()
    categories.value = res.data
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const loadCategoryShowcase = async () => {
  try {
    const res = await categoriesAPI.getShowcase()
    categoryShowcase.value = res.data
    nextTick(() => {
      const allPosts = categoryShowcase.value.flatMap(cat => cat.representativePosts || [])
      detectPostImagesOrientation(allPosts)
    })
  } catch (e) {
    console.error('加载类别展柜失败', e)
  }
}

const selectShowcaseCategory = (catId) => {
  selectedCategory.value = catId
  selectedEra.value = null
  currentPage.value = 0
  loadPosts()
  setTimeout(() => {
    const filterSection = document.querySelector('.filter-section')
    if (filterSection) {
      filterSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 100)
}

const loadEras = async () => {
  try {
    const res = await erasAPI.getAll()
    let erasList = res.data || []
    erasList = erasList.map(era => ({
      ...era,
      name: normalizeEraName(era.name)
    }))
    eras.value = sortErasDefault(erasList)
  } catch (e) {
    console.error('加载年代失败', e)
  }
}

const loadEraTimeline = async () => {
  try {
    const res = await erasAPI.getTimeline()
    let timelineList = res.data || []
    timelineList = timelineList.map(era => ({
      ...era,
      name: normalizeEraName(era.name)
    }))
    eraTimeline.value = sortErasDefault(timelineList)
  } catch (e) {
    console.error('加载年代时间廊失败', e)
  }
}

const selectTimelineEra = (eraId) => {
  selectedEra.value = eraId
  selectedCategory.value = null
  currentPage.value = 0
  loadPosts()
  setTimeout(() => {
    const filterSection = document.querySelector('.filter-section')
    if (filterSection) {
      filterSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 100)
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

const clearCategoryFilter = () => {
  selectedCategory.value = null
  currentPage.value = 0
  loadPosts()
}

const clearEraFilter = () => {
  selectedEra.value = null
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
  loadCategoryShowcase()
  loadEras()
  loadEraTimeline()
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

.category-showcase-section {
  margin-bottom: 40px;
}

.showcase-title {
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: baseline;
}

.showcase-container {
  overflow-x: auto;
  padding: 10px 4px 20px;
  scrollbar-width: thin;
  scrollbar-color: var(--cat-primary, #d4a574) var(--cat-secondary, #f5e6d3);
}

.showcase-container::-webkit-scrollbar {
  height: 8px;
}

.showcase-container::-webkit-scrollbar-track {
  background: #f5e6d3;
  border-radius: 4px;
}

.showcase-container::-webkit-scrollbar-thumb {
  background: #d4a574;
  border-radius: 4px;
}

.showcase-track {
  display: flex;
  gap: 20px;
  padding-right: 20px;
  min-width: min-content;
}

.showcase-card {
  position: relative;
  flex: 0 0 280px;
  background: #fff;
  border-radius: 16px;
  padding: 24px 20px 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 2px solid transparent;
  overflow: hidden;
}

.showcase-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  opacity: 0.8;
  background: var(--cat-gradient);
}

.showcase-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(93, 78, 55, 0.2);
  border-color: var(--cat-primary);
}

.showcase-cat-homeAppliances {
  border-color: rgba(198, 40, 40, 0.2);
  background: linear-gradient(180deg, #fff 0%, #fff5f5 100%);
}
.showcase-cat-homeAppliances::before { background: linear-gradient(90deg, #c62828, #ef5350); }
.showcase-cat-homeAppliances:hover { border-color: #c62828; }

.showcase-cat-audioVideo {
  border-color: rgba(106, 27, 154, 0.2);
  background: linear-gradient(180deg, #fff 0%, #faf5fc 100%);
}
.showcase-cat-audioVideo::before { background: linear-gradient(90deg, #6a1b9a, #ab47bc); }
.showcase-cat-audioVideo:hover { border-color: #6a1b9a; }

.showcase-cat-communication {
  border-color: rgba(2, 119, 189, 0.2);
  background: linear-gradient(180deg, #fff 0%, #f0f8fc 100%);
}
.showcase-cat-communication::before { background: linear-gradient(90deg, #0277bd, #29b6f6); }
.showcase-cat-communication:hover { border-color: #0277bd; }

.showcase-cat-toys {
  border-color: rgba(230, 81, 0, 0.2);
  background: linear-gradient(180deg, #fff 0%, #fff8f0 100%);
}
.showcase-cat-toys::before { background: linear-gradient(90deg, #e65100, #ff9800); }
.showcase-cat-toys:hover { border-color: #e65100; }

.showcase-cat-stationery {
  border-color: rgba(46, 125, 50, 0.2);
  background: linear-gradient(180deg, #fff 0%, #f1f8f2 100%);
}
.showcase-cat-stationery::before { background: linear-gradient(90deg, #2e7d32, #66bb6a); }
.showcase-cat-stationery:hover { border-color: #2e7d32; }

.showcase-cat-clothing {
  border-color: rgba(173, 20, 87, 0.2);
  background: linear-gradient(180deg, #fff 0%, #fbf5f8 100%);
}
.showcase-cat-clothing::before { background: linear-gradient(90deg, #ad1457, #ec407a); }
.showcase-cat-clothing:hover { border-color: #ad1457; }

.showcase-cat-food {
  border-color: rgba(245, 124, 0, 0.2);
  background: linear-gradient(180deg, #fff 0%, #fff8f0 100%);
}
.showcase-cat-food::before { background: linear-gradient(90deg, #f57c00, #ffa726); }
.showcase-cat-food:hover { border-color: #f57c00; }

.showcase-cat-daily {
  border-color: rgba(93, 64, 55, 0.2);
  background: linear-gradient(180deg, #fff 0%, #faf6f2 100%);
}
.showcase-cat-daily::before { background: linear-gradient(90deg, #5d4037, #8d6e63); }
.showcase-cat-daily:hover { border-color: #5d4037; }

.showcase-connector {
  position: absolute;
  top: 30px;
  right: -20px;
  width: 20px;
  height: 2px;
  z-index: 1;
  display: flex;
  align-items: center;
}

.showcase-connector .connector-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, var(--cat-primary, #d4a574), transparent);
}

.showcase-connector .connector-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--cat-primary, #d4a574);
  flex-shrink: 0;
}

.showcase-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.showcase-cat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--cat-bg-gradient);
  border: 1.5px solid var(--cat-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.showcase-cat-info {
  flex: 1;
  min-width: 0;
}

.showcase-cat-name {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
  color: var(--cat-text);
}

.showcase-cat-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.showcase-post-count {
  text-align: center;
  padding: 4px 10px;
  border-radius: 8px;
  flex-shrink: 0;
  background: var(--cat-bg-gradient);
}

.showcase-post-count .count-num {
  font-size: 18px;
  font-weight: 700;
  display: block;
  line-height: 1;
  color: var(--cat-primary);
}

.showcase-post-count .count-label {
  font-size: 10px;
  color: #999;
}

.showcase-preview {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
}

.showcase-preview-item {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  transition: transform 0.3s;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.showcase-preview-item:hover {
  transform: scale(1.1);
  z-index: 2;
}

.showcase-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.showcase-enter {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s;
  background: var(--cat-bg-gradient);
  color: var(--cat-primary);
}

.showcase-card:hover .showcase-enter {
  background: var(--cat-gradient);
  color: #fff;
}

.showcase-card:hover .enter-arrow {
  transform: translateX(4px);
}

.enter-arrow {
  transition: transform 0.3s;
}

@media (max-width: 768px) {
  .showcase-card {
    flex: 0 0 250px;
  }
  .showcase-connector {
    display: none;
  }
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

.post-summary-structured,
.hot-summary-structured {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.summary-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 14px;
  font-size: 12px;
  line-height: 1.4;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-chip .chip-icon {
  font-size: 11px;
  flex-shrink: 0;
}

.summary-source {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
  border: 1px solid rgba(255, 111, 0, 0.2);
}

.summary-scene {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #0d47a1;
  border: 1px solid rgba(25, 118, 210, 0.2);
}

.summary-emotion {
  background: linear-gradient(135deg, #fce4ec, #f8bbd0);
  color: #ad1457;
  border: 1px solid rgba(173, 20, 87, 0.15);
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

.timeline-section {
  margin-bottom: 40px;
}

.timeline-title {
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: baseline;
}

.title-subtitle {
  font-size: 14px;
  color: #999;
  font-weight: normal;
  margin-left: 12px;
}

.timeline-container {
  overflow-x: auto;
  padding: 10px 4px 20px;
  scrollbar-width: thin;
  scrollbar-color: #d4a574 #f5e6d3;
}

.timeline-container::-webkit-scrollbar {
  height: 8px;
}

.timeline-container::-webkit-scrollbar-track {
  background: #f5e6d3;
  border-radius: 4px;
}

.timeline-container::-webkit-scrollbar-thumb {
  background: #d4a574;
  border-radius: 4px;
}

.timeline-track {
  display: flex;
  gap: 20px;
  padding-right: 20px;
  min-width: min-content;
}

.timeline-card {
  position: relative;
  flex: 0 0 260px;
  background: #fff;
  border-radius: 16px;
  padding: 24px 20px 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 2px solid transparent;
  overflow: hidden;
}

.timeline-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  opacity: 0.8;
}

.timeline-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(93, 78, 55, 0.2);
}

.timeline-era-60s {
  border-color: rgba(93, 64, 55, 0.2);
  background: linear-gradient(180deg, #fff 0%, #faf6f2 100%);
}
.timeline-era-60s::before { background: linear-gradient(90deg, #5d4037, #8d6e63); }
.timeline-era-60s:hover { border-color: #5d4037; }

.timeline-era-70s {
  border-color: rgba(141, 110, 99, 0.2);
  background: linear-gradient(180deg, #fff 0%, #fff8ed 100%);
}
.timeline-era-70s::before { background: linear-gradient(90deg, #bf360c, #ff8f00); }
.timeline-era-70s:hover { border-color: #bf360c; }

.timeline-era-80s {
  border-color: rgba(25, 118, 210, 0.2);
  background: linear-gradient(180deg, #fff 0%, #f0f7fe 100%);
}
.timeline-era-80s::before { background: linear-gradient(90deg, #1976d2, #42a5f5); }
.timeline-era-80s:hover { border-color: #1976d2; }

.timeline-era-90s {
  border-color: rgba(255, 111, 0, 0.2);
  background: linear-gradient(180deg, #fff 0%, #fff5ed 100%);
}
.timeline-era-90s::before { background: linear-gradient(90deg, #ff6f00, #ffab40); }
.timeline-era-90s:hover { border-color: #ff6f00; }

.timeline-era-00s {
  border-color: rgba(0, 172, 193, 0.2);
  background: linear-gradient(180deg, #fff 0%, #edfafb 100%);
}
.timeline-era-00s::before { background: linear-gradient(90deg, #00acc1, #4dd0e1); }
.timeline-era-00s:hover { border-color: #00acc1; }

.timeline-connector {
  position: absolute;
  top: 30px;
  right: -20px;
  width: 20px;
  height: 2px;
  z-index: 1;
  display: flex;
  align-items: center;
}

.connector-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, #d4a574, transparent);
}

.connector-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d4a574;
  flex-shrink: 0;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.timeline-era-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.timeline-era-60s .timeline-era-icon {
  background: linear-gradient(135deg, #d7ccc8, #bcaaa4);
  color: #3e2723;
  box-shadow: 0 2px 8px rgba(93, 64, 55, 0.2);
}

.timeline-era-70s .timeline-era-icon {
  background: linear-gradient(135deg, #ffcc80, #ffb74d);
  color: #4e342e;
  box-shadow: 0 2px 8px rgba(191, 54, 12, 0.2);
}

.timeline-era-80s .timeline-era-icon {
  background: linear-gradient(135deg, #90caf9, #64b5f6);
  color: #0d47a1;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.25);
}

.timeline-era-90s .timeline-era-icon {
  background: linear-gradient(135deg, #ffab40, #ff9100);
  color: #bf360c;
  box-shadow: 0 2px 8px rgba(255, 111, 0, 0.25);
}

.timeline-era-00s .timeline-era-icon {
  background: linear-gradient(135deg, #80deea, #4dd0e1);
  color: #006064;
  box-shadow: 0 2px 8px rgba(0, 172, 193, 0.25);
}

.timeline-era-info {
  flex: 1;
  min-width: 0;
}

.timeline-era-name {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}

.timeline-era-60s .timeline-era-name {
  color: #3e2723;
  font-family: 'STKaiti', 'KaiTi', serif;
  letter-spacing: 1px;
}

.timeline-era-70s .timeline-era-name {
  color: #5d4037;
  font-family: 'STFangsong', 'FangSong', serif;
}

.timeline-era-80s .timeline-era-name {
  color: #0d47a1;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.timeline-era-90s .timeline-era-name {
  color: #bf360c;
  letter-spacing: 0.5px;
}

.timeline-era-00s .timeline-era-name {
  color: #006064;
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  font-weight: 600;
}

.timeline-era-years {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.timeline-post-count {
  text-align: center;
  padding: 4px 10px;
  border-radius: 8px;
  flex-shrink: 0;
}

.timeline-era-60s .timeline-post-count {
  background: #efebe9;
}

.timeline-era-70s .timeline-post-count {
  background: #fff3e0;
}

.timeline-era-80s .timeline-post-count {
  background: #e3f2fd;
}

.timeline-era-90s .timeline-post-count {
  background: #fff3e0;
}

.timeline-era-00s .timeline-post-count {
  background: #e0f7fa;
}

.count-num {
  font-size: 18px;
  font-weight: 700;
  display: block;
  line-height: 1;
}

.timeline-era-60s .count-num { color: #5d4037; }
.timeline-era-70s .count-num { color: #bf360c; }
.timeline-era-80s .count-num { color: #1976d2; }
.timeline-era-90s .count-num { color: #ff6f00; }
.timeline-era-00s .count-num { color: #00acc1; }

.count-label {
  font-size: 10px;
  color: #999;
}

.timeline-description {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.timeline-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}

.timeline-cat-tag {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  background: #f5e6d3;
  color: #8b6914;
  font-weight: 500;
}

.timeline-era-60s .timeline-cat-tag {
  background: #d7ccc8;
  color: #3e2723;
}

.timeline-era-70s .timeline-cat-tag {
  background: #ffcc80;
  color: #4e342e;
}

.timeline-era-80s .timeline-cat-tag {
  background: #90caf9;
  color: #0d47a1;
}

.timeline-era-90s .timeline-cat-tag {
  background: #ffab40;
  color: #bf360c;
}

.timeline-era-00s .timeline-cat-tag {
  background: #80deea;
  color: #006064;
}

.timeline-preview {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
}

.timeline-preview-item {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  transition: transform 0.3s;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.timeline-preview-item:hover {
  transform: scale(1.1);
  z-index: 2;
}

.timeline-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.timeline-enter {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s;
}

.timeline-era-60s .timeline-enter {
  background: #efebe9;
  color: #5d4037;
}
.timeline-era-60s:hover .timeline-enter {
  background: linear-gradient(135deg, #6d4c41, #4e342e);
  color: #fff;
}

.timeline-era-70s .timeline-enter {
  background: #fff3e0;
  color: #bf360c;
}
.timeline-era-70s:hover .timeline-enter {
  background: linear-gradient(135deg, #bf360c, #8d6e63);
  color: #fff;
}

.timeline-era-80s .timeline-enter {
  background: #e3f2fd;
  color: #1976d2;
}
.timeline-era-80s:hover .timeline-enter {
  background: linear-gradient(135deg, #1976d2, #0d47a1);
  color: #fff;
}

.timeline-era-90s .timeline-enter {
  background: #fff3e0;
  color: #ff6f00;
}
.timeline-era-90s:hover .timeline-enter {
  background: linear-gradient(135deg, #ff6f00, #e64a19);
  color: #fff;
}

.timeline-era-00s .timeline-enter {
  background: #e0f7fa;
  color: #00acc1;
}
.timeline-era-00s:hover .timeline-enter {
  background: linear-gradient(135deg, #00acc1, #006064);
  color: #fff;
}

.enter-arrow {
  transition: transform 0.3s;
}

.timeline-card:hover .enter-arrow {
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .timeline-card {
    flex: 0 0 240px;
  }
  .timeline-connector {
    display: none;
  }
  .title-subtitle {
    margin-left: 0;
    margin-top: 4px;
    width: 100%;
  }
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
