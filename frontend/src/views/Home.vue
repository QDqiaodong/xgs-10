<template>
  <div class="home container">
    <section class="hero">
      <h1 class="hero-title">时光里的珍贵回忆</h1>
      <p class="hero-subtitle">分享你的老物件，讲述那些年的故事</p>
    </section>

    <section class="filter-section card">
      <div class="filter-group">
        <span class="filter-label">品类筛选：</span>
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="['filter-btn', { active: selectedCategory === cat.id }]"
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
          :class="['filter-btn', { active: selectedEra === era.id }]"
          @click="toggleEra(era.id)"
        >
          {{ era.name }}
        </button>
        <button v-if="selectedEra" class="filter-btn clear" @click="selectedEra = null">
          清除筛选
        </button>
      </div>
    </section>

    <section class="hot-section" v-if="hotPosts.length > 0">
      <h2 class="section-title">🔥 热门回忆</h2>
      <div class="hot-grid">
        <router-link
          v-for="post in hotPosts.slice(0, 3)"
          :key="post.id"
          :to="`/post/${post.id}`"
          class="hot-card card"
        >
          <div class="hot-image">
            <img :src="post.images && post.images[0] ? getImageUrl(post.images[0]) : 'https://picsum.photos/400/300'" :alt="post.title" />
          </div>
          <div class="hot-content">
            <h3 class="hot-title">{{ post.title }}</h3>
            <div class="hot-meta">
              <span class="tag">{{ post.categoryName }}</span>
              <span class="tag">{{ post.eraName }}</span>
            </div>
            <p class="hot-views">👁 {{ post.viewCount }} 次浏览</p>
          </div>
        </router-link>
      </div>
    </section>

    <section class="posts-section">
      <h2 class="section-title">📜 最新分享</h2>
      <div class="posts-grid" v-if="posts.length > 0">
        <router-link
          v-for="post in posts"
          :key="post.id"
          :to="`/post/${post.id}`"
          class="post-card card"
        >
          <div class="post-image">
            <img :src="post.images && post.images[0] ? getImageUrl(post.images[0]) : 'https://picsum.photos/400/300'" :alt="post.title" />
          </div>
          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-item">物件：{{ post.itemName }}</p>
            <p class="post-excerpt">{{ post.content }}</p>
            <div class="post-tags">
              <span class="tag">{{ post.categoryName }}</span>
              <span class="tag">{{ post.eraName }}</span>
            </div>
            <div class="post-footer">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoriesAPI, erasAPI, postsAPI } from '../api'

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

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const apiBase = import.meta.env.VITE_API_BASE_URL || ''
  return apiBase.replace('/api', '') + url
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
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

const loadHotPosts = async () => {
  try {
    const res = await postsAPI.getHot()
    hotPosts.value = res.data
  } catch (e) {
    console.error('加载热门失败', e)
  }
}

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
}

.hero-title {
  font-size: 36px;
  color: #6b4f0f;
  margin-bottom: 12px;
  font-weight: 600;
}

.hero-subtitle {
  font-size: 18px;
  color: #8b6914;
  opacity: 0.8;
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
  background: #e8d5b8;
}

.filter-btn.active {
  background: linear-gradient(135deg, #d4a574 0%, #c19660 100%);
  color: #fff;
}

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
}

.hot-image {
  height: 180px;
  overflow: hidden;
}

.hot-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hot-content {
  padding: 16px;
  flex: 1;
}

.hot-title {
  font-size: 16px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 10px;
  line-height: 1.4;
}

.hot-meta {
  margin-bottom: 8px;
}

.hot-views {
  font-size: 13px;
  color: #999;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.post-card {
  display: flex;
  flex-direction: column;
}

.post-image {
  height: 220px;
  overflow: hidden;
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.post-card:hover .post-image img {
  transform: scale(1.05);
}

.post-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 8px;
  line-height: 1.4;
}

.post-item {
  font-size: 14px;
  color: #8b6914;
  margin-bottom: 10px;
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

.post-tags {
  margin-bottom: 16px;
}

.post-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
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
}
</style>
