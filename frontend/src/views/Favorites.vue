<template>
  <div class="favorites container">
    <h1 class="page-title">⭐ 我的收藏</h1>
    <p class="page-desc">收藏的怀旧内容都在这里</p>

    <div class="posts-grid" v-if="favorites.length > 0">
      <router-link
        v-for="post in favorites"
        :key="post.id"
        :to="`/post/${post.id}`"
        class="post-card card"
      >
        <div :class="['post-image', getCardImageClass(getImageOrientation(post.images && post.images[0] ? getImageUrl(post.images[0]) : ''))]">
          <img :src="post.images && post.images[0] ? getImageUrl(post.images[0]) : 'https://picsum.photos/400/300'" :alt="post.title" />
        </div>
        <div class="post-content">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-item">物件：{{ safeDisplayItemName(post.itemName) }}</p>
          <p class="post-excerpt">{{ post.content }}</p>
          <div class="post-tags">
            <span :class="['tag', 'tag-cat', getCategoryClass(post.categoryName)]" :style="getCategoryStyleVars(post.categoryName)">
              <CategoryIcon :category="post.categoryName" size="xs" />
              {{ post.categoryName || '未分类' }}
            </span>
            <span class="tag">{{ post.eraName || '未知年代' }}</span>
          </div>
          <div class="post-footer">
            <span class="post-author">{{ post.authorName }}</span>
            <div class="post-stats">
              <span>👁 {{ post.viewCount }}</span>
              <span>💬 {{ post.commentCount }}</span>
            </div>
          </div>
        </div>
      </router-link>
    </div>

    <div class="empty-state" v-else>
      <div class="empty-icon">📭</div>
      <p>还没有收藏任何内容</p>
      <router-link to="/" class="btn btn-primary">去发现精彩回忆</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { favoritesAPI } from '../api'
import { getSessionId } from '../utils/session'
import { displayItemName } from '../utils/textCleaner'
import { detectImageOrientationFromUrl, getCardImageClass, ImageOrientation } from '../utils/imageLayout'
import { getCategoryClass, getCategoryStyleVars } from '../icons/categoryUtils'

const favorites = ref([])
const userSession = getSessionId()
const imageOrientations = ref({})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}

const detectImagesOrientation = async (postsList) => {
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

const safeDisplayItemName = (name) => displayItemName(name)

const loadFavorites = async () => {
  try {
    const res = await favoritesAPI.getUserFavorites(userSession)
    favorites.value = res.data
    nextTick(() => detectImagesOrientation(favorites.value))
  } catch (e) {
    console.error('加载收藏失败', e)
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 8px;
}

.page-desc {
  color: #999;
  margin-bottom: 30px;
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
  height: 200px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0eb;
}

.post-image.img-landscape { height: 180px; }
.post-image.img-portrait { height: 260px; }
.post-image.img-square { height: 220px; }
.post-image.img-detail { height: 180px; }

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.5s;
  background: #faf7f2;
}

.post-image.img-landscape img,
.post-image.img-square img {
  object-fit: cover;
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
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state p {
  color: #999;
  margin-bottom: 24px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .posts-grid {
    grid-template-columns: 1fr;
  }
  .page-title {
    font-size: 22px;
  }
}
</style>
