<template>
  <div class="post-detail container" v-if="post">
    <router-link to="/" class="back-link">← 返回列表</router-link>

    <article class="detail-card card">
      <header class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span :class="['tag', 'tag-cat', getCategoryClass(post.categoryName)]" :style="getCategoryStyleVars(post.categoryName)">
            <CategoryIcon :category="post.categoryName" size="xs" />
            {{ post.categoryName }}
          </span>
          <span class="tag">{{ post.eraName }}</span>
          <span class="author">作者：{{ post.authorName }}</span>
          <span class="date">{{ formatDate(post.createdAt) }}</span>
        </div>
      </header>

      <div class="post-images post-images-grid" v-if="post.images && post.images.length > 0">
        <div
          v-for="(img, idx) in post.images"
          :key="idx"
          :class="[
            'post-image-item',
            getImageLayoutClass(idx),
            getCardImageClass(getImgOrientation(getImageUrl(img)))
          ]"
        >
          <img :src="getImageUrl(img)" :alt="`${post.title} ${idx + 1}`" />
        </div>
      </div>

      <div class="post-body">
        <div class="archive-info-grid">
          <div class="info-row">
            <span class="info-label">物件名称：</span>
            <span class="info-value">{{ safeDisplayItemName(post.itemName) }}</span>
          </div>
          <div class="info-row" v-if="post.preservationStatus">
            <span class="info-label">保存状态：</span>
            <span :class="['info-value', 'status-value', getPreservationClass(post.preservationStatus)]">
              <span class="status-icon">{{ getPreservationIcon(post.preservationStatus) }}</span>
              {{ post.preservationStatus }}
            </span>
          </div>
        </div>

        <div class="content-section" v-if="post.storySummary">
          <h3 class="section-title">📋 故事摘要</h3>
          <p class="summary-text">{{ post.storySummary }}</p>
        </div>

        <div class="story-layers">
          <div class="story-layer" v-if="post.story">
            <div class="story-layer-header">
              <span class="story-layer-icon">🎁</span>
              <h3 class="story-layer-title">物件来历</h3>
            </div>
            <p class="story-layer-content">{{ post.story }}</p>
          </div>

          <div class="story-layer" v-if="post.memory">
            <div class="story-layer-header">
              <span class="story-layer-icon">💭</span>
              <h3 class="story-layer-title">使用记忆</h3>
            </div>
            <p class="story-layer-content">{{ post.memory }}</p>
          </div>

          <div class="story-layer" v-if="post.eraBackground">
            <div class="story-layer-header">
              <span class="story-layer-icon">🏛️</span>
              <h3 class="story-layer-title">时代背景</h3>
            </div>
            <p class="story-layer-content">{{ post.eraBackground }}</p>
          </div>

          <div class="story-layer" v-if="post.currentStatus">
            <div class="story-layer-header">
              <span class="story-layer-icon">📍</span>
              <h3 class="story-layer-title">现状</h3>
            </div>
            <p class="story-layer-content">{{ post.currentStatus }}</p>
          </div>

          <div class="story-layer" v-if="post.content && !post.story && !post.memory && !post.eraBackground && !post.currentStatus">
            <div class="story-layer-header">
              <span class="story-layer-icon">📝</span>
              <h3 class="story-layer-title">物件介绍</h3>
            </div>
            <p class="story-layer-content">{{ post.content }}</p>
          </div>
        </div>

        <div class="content-section" v-if="post.timelineEvents && post.timelineEvents.length > 0">
          <h3 class="section-title">📅 故事年表</h3>
          <div class="timeline">
            <div
              v-for="(event, idx) in post.timelineEvents"
              :key="event.id"
              class="timeline-item"
            >
              <div class="timeline-line" v-if="idx < post.timelineEvents.length - 1"></div>
              <div :class="['timeline-dot', `event-${event.eventType.toLowerCase()}`]">
                <span class="dot-icon">{{ getEventTypeIcon(event.eventType) }}</span>
              </div>
              <div class="timeline-content">
                <div class="timeline-header">
                  <span :class="['event-tag', `tag-${event.eventType.toLowerCase()}`]">
                    {{ getEventTypeLabel(event.eventType) }}
                  </span>
                  <span class="timeline-date">{{ formatEventDate(event.eventDate) }}</span>
                </div>
                <h4 class="timeline-title">{{ event.title }}</h4>
                <p class="timeline-desc" v-if="event.description">{{ event.description }}</p>
                <div class="timeline-location" v-if="event.location">
                  📍 {{ event.location }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <footer class="post-footer">
        <div class="stats">
          <span>👁 {{ post.viewCount }} 浏览</span>
          <span>💬 {{ post.commentCount }} 留言</span>
          <span>⭐ {{ post.favoriteCount }} 收藏</span>
        </div>
        <button
          :class="['favorite-btn', { favorited: isFavorited }]"
          @click="toggleFavorite"
        >
          {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
        </button>
      </footer>
    </article>

    <section class="comments-section card">
      <h2 class="comments-title">💬 互动留言 ({{ comments.length }})</h2>

      <div class="comment-form">
        <textarea
          v-model="newComment"
          placeholder="分享你的相似经历或补充物件相关知识..."
          rows="3"
        ></textarea>
        <div class="form-actions">
          <input
            v-model="commentAuthor"
            type="text"
            placeholder="你的昵称（可选）"
          />
          <button class="btn btn-primary" @click="submitComment" :disabled="!newComment.trim()">
            发表留言
          </button>
        </div>
      </div>

      <div class="comments-list" v-if="comments.length > 0">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-author">{{ comment.authorName }}</span>
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
          </div>
          <p class="comment-content">{{ comment.content }}</p>
        </div>
      </div>
      <div class="empty-comments" v-else>
        暂无留言，来说说你的回忆吧~
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { postsAPI, commentsAPI, favoritesAPI } from '../api'
import { getSessionId } from '../utils/session'
import { displayItemName } from '../utils/textCleaner'
import { detectImageOrientationFromUrl, getCardImageClass, ImageOrientation, getLayoutForGrid } from '../utils/imageLayout'
import { getCategoryClass, getCategoryStyleVars } from '../icons/categoryUtils'

const route = useRoute()
const postId = computed(() => route.params.id)

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const commentAuthor = ref('')
const isFavorited = ref(false)
const userSession = getSessionId()
const imageOrientations = ref({})
const imageLayouts = ref([])

const detectDetailImagesOrientation = async (images) => {
  if (!images || images.length === 0) return
  imageLayouts.value = getLayoutForGrid(images.length)
  for (let i = 0; i < images.length; i++) {
    const imgUrl = getImageUrl(images[i])
    if (imgUrl && !imageOrientations.value[imgUrl]) {
      const layout = imageLayouts.value[i]
      if (layout && layout.orientation) {
        imageOrientations.value[imgUrl] = layout.orientation
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

const getImgOrientation = (imgUrl) => {
  return imageOrientations.value[imgUrl] || ImageOrientation.SQUARE
}

const safeDisplayItemName = (name) => displayItemName(name)

const getImageLayoutClass = (idx) => {
  if (imageLayouts.value[idx]) {
    return `layout-${imageLayouts.value[idx].span}`
  }
  return 'layout-full'
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatEventDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getEventTypeIcon = (type) => {
  const icons = {
    ACQUISITION: '🎁',
    USAGE: '✨',
    IDLE: '📦',
    DISPOSAL: '🚪'
  }
  return icons[type] || '📌'
}

const getEventTypeLabel = (type) => {
  const labels = {
    ACQUISITION: '获得',
    USAGE: '使用',
    IDLE: '闲置',
    DISPOSAL: '去向'
  }
  return labels[type] || '事件'
}

const getPreservationClass = (status) => {
  if (!status) return 'preservation-default'
  const s = status.toLowerCase()
  if (s.includes('完好') || s.includes('完美') || s.includes('新')) return 'preservation-excellent'
  if (s.includes('正常') || s.includes('良好') || s.includes('功能')) return 'preservation-good'
  if (s.includes('锈') || s.includes('磨损') || s.includes('旧')) return 'preservation-worn'
  if (s.includes('破损') || s.includes('坏') || s.includes('故障')) return 'preservation-damaged'
  return 'preservation-default'
}

const getPreservationIcon = (status) => {
  const cls = getPreservationClass(status)
  const icons = {
    'preservation-excellent': '✨',
    'preservation-good': '✅',
    'preservation-worn': '🔶',
    'preservation-damaged': '🔴',
    'preservation-default': '📦'
  }
  return icons[cls] || '📦'
}

const loadPost = async () => {
  try {
    const res = await postsAPI.getDetail(postId.value)
    post.value = res.data
    nextTick(() => {
      if (post.value && post.value.images) {
        detectDetailImagesOrientation(post.value.images)
      }
    })
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

const loadComments = async () => {
  try {
    const res = await commentsAPI.getByPost(postId.value)
    comments.value = res.data
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

const checkFavorite = async () => {
  try {
    const res = await favoritesAPI.check(postId.value, userSession)
    isFavorited.value = res.data.favorited
  } catch (e) {
    console.error('检查收藏状态失败', e)
  }
}

const toggleFavorite = async () => {
  try {
    const res = await favoritesAPI.toggle(postId.value, userSession)
    isFavorited.value = res.data.favorited
    if (post.value) {
      post.value.favoriteCount += isFavorited.value ? 1 : -1
    }
  } catch (e) {
    console.error('切换收藏状态失败', e)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await commentsAPI.create({
      postId: postId.value,
      content: newComment.value.trim(),
      authorName: commentAuthor.value.trim() || '匿名用户'
    })
    newComment.value = ''
    commentAuthor.value = ''
    loadComments()
    if (post.value) {
      post.value.commentCount++
    }
  } catch (e) {
    console.error('发表评论失败', e)
  }
}

onMounted(() => {
  loadPost()
  loadComments()
  checkFavorite()
})
</script>

<style scoped>
.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: #8b6914;
  font-size: 14px;
}

.back-link:hover {
  color: #6b4f0f;
}

.detail-card {
  padding: 30px;
  margin-bottom: 24px;
}

.post-title {
  font-size: 28px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 16px;
}

.post-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.post-meta .author,
.post-meta .date {
  font-size: 13px;
  color: #999;
}

.post-images {
  margin: 24px 0;
}

.post-images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.post-image-item {
  border-radius: 10px;
  overflow: hidden;
  background: #f5f0eb;
  display: flex;
  align-items: center;
  justify-content: center;
  grid-column: span 3;
}

.post-image-item.layout-full { grid-column: span 3; }
.post-image-item.layout-half { grid-column: span 3; }
.post-image-item.layout-two-thirds { grid-column: span 2; grid-row: span 2; }
.post-image-item.layout-third { grid-column: span 1; }

@media (max-width: 768px) {
  .post-images-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .post-image-item,
  .post-image-item.layout-full,
  .post-image-item.layout-half,
  .post-image-item.layout-two-thirds,
  .post-image-item.layout-third {
    grid-column: span 2;
    grid-row: span 1;
  }
}

.post-image-item.img-landscape {
  aspect-ratio: 16 / 9;
}
.post-image-item.img-portrait {
  aspect-ratio: 3 / 4;
}
.post-image-item.img-square {
  aspect-ratio: 1 / 1;
}
.post-image-item.img-detail {
  aspect-ratio: 4 / 3;
}

.post-image-item img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #faf7f2;
}

.post-image-item.img-landscape img,
.post-image-item.img-square img {
  object-fit: cover;
}

.post-body {
  margin-bottom: 20px;
}

.archive-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.info-row {
  margin-bottom: 0;
  padding: 12px 16px;
  background: #fdf6e3;
  border-radius: 8px;
}

.info-label {
  font-weight: 500;
  color: #8b6914;
}

.info-value {
  color: #5d4e37;
}

.status-value {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.status-icon {
  font-size: 14px;
}

.preservation-excellent {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #389e0d;
}

.preservation-good {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #096dd9;
}

.preservation-worn {
  background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
  color: #d48806;
}

.preservation-damaged {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #cf1322;
}

.preservation-default {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  color: #595959;
}

.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: #6b4f0f;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  border-left: 4px solid #faad14;
  border-radius: 0 8px 8px 0;
  font-style: italic;
}

.content-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #6b4f0f;
  margin-bottom: 12px;
}

.content-text {
  font-size: 15px;
  line-height: 1.8;
  color: #5d4e37;
  white-space: pre-wrap;
}

.story-layers {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.story-layer {
  background: #fdf6e3;
  border-radius: 12px;
  padding: 20px 24px;
  border-left: 4px solid #d4a574;
  transition: all 0.3s ease;
}

.story-layer:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(139, 105, 20, 0.1);
}

.story-layer:nth-child(1) {
  border-left-color: #52c41a;
  background: linear-gradient(135deg, #f6ffed 0%, #fcffe6 100%);
}

.story-layer:nth-child(2) {
  border-left-color: #1890ff;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
}

.story-layer:nth-child(3) {
  border-left-color: #722ed1;
  background: linear-gradient(135deg, #f9f0ff 0%, #faf5ff 100%);
}

.story-layer:nth-child(4) {
  border-left-color: #fa8c16;
  background: linear-gradient(135deg, #fff7e6 0%, #fffaf0 100%);
}

.story-layer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.story-layer-icon {
  font-size: 20px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
}

.story-layer-title {
  font-size: 17px;
  font-weight: 600;
  color: #5d4e37;
  margin: 0;
}

.story-layer-content {
  font-size: 15px;
  line-height: 1.85;
  color: #5d4e37;
  white-space: pre-wrap;
  margin: 0;
  padding-left: 46px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #999;
}

.favorite-btn {
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 14px;
  background: #f5e6d3;
  color: #8b6914;
  transition: all 0.3s;
}

.favorite-btn:hover {
  background: #e8d5b8;
}

.favorite-btn.favorited {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
}

.comments-section {
  padding: 24px;
}

.comments-title {
  font-size: 20px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 24px;
}

.comment-form textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0d5c5;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  margin-bottom: 12px;
  background: #fff;
}

.comment-form textarea:focus {
  border-color: #d4a574;
}

.form-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.form-actions input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e0d5c5;
  border-radius: 8px;
  font-size: 14px;
  max-width: 200px;
}

.form-actions input:focus {
  border-color: #d4a574;
}

.comments-list {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #8b6914;
  font-size: 14px;
}

.comment-date {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  line-height: 1.7;
  color: #5d4e37;
}

.empty-comments {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.timeline {
  position: relative;
  padding-left: 40px;
}

.timeline-item {
  position: relative;
  padding-bottom: 32px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-line {
  position: absolute;
  left: 15px;
  top: 40px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #d4a574 0%, #e8d5b8 100%);
}

.timeline-dot {
  position: absolute;
  left: -40px;
  top: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.timeline-dot.event-acquisition {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.timeline-dot.event-usage {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.timeline-dot.event-idle {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.timeline-dot.event-disposal {
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
}

.dot-icon {
  font-size: 14px;
}

.timeline-content {
  background: #fdf6e3;
  border-radius: 12px;
  padding: 16px 20px;
  border-left: 3px solid #d4a574;
  transition: all 0.3s;
}

.timeline-content:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(139, 105, 20, 0.1);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.event-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
}

.tag-acquisition {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.tag-usage {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.tag-idle {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.tag-disposal {
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
}

.timeline-date {
  font-size: 13px;
  color: #999;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 8px;
}

.timeline-desc {
  font-size: 14px;
  line-height: 1.7;
  color: #6b5b47;
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.timeline-location {
  font-size: 13px;
  color: #8b6914;
}

@media (max-width: 768px) {
  .detail-card {
    padding: 20px;
  }
  .post-title {
    font-size: 22px;
  }
  .archive-info-grid {
    grid-template-columns: 1fr;
  }
  .timeline {
    padding-left: 32px;
  }
  .timeline-dot {
    left: -32px;
    width: 28px;
    height: 28px;
  }
  .timeline-line {
    left: 13px;
  }
  .story-layer {
    padding: 16px;
  }
  .story-layer-content {
    padding-left: 0;
  }
}
</style>
