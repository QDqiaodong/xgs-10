<template>
  <div class="post-detail container" v-if="post">
    <router-link to="/" class="back-link">← 返回列表</router-link>

    <article class="detail-card card">
      <header class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span class="tag">{{ post.categoryName }}</span>
          <span class="tag">{{ post.eraName }}</span>
          <span class="author">作者：{{ post.authorName }}</span>
          <span class="date">{{ formatDate(post.createdAt) }}</span>
        </div>
      </header>

      <div class="post-images" v-if="post.images && post.images.length > 0">
        <div
          v-for="(img, idx) in post.images"
          :key="idx"
          class="post-image-item"
        >
          <img :src="getImageUrl(img)" :alt="`${post.title} ${idx + 1}`" />
        </div>
      </div>

      <div class="post-body">
        <div class="info-row">
          <span class="info-label">物件名称：</span>
          <span class="info-value">{{ post.itemName }}</span>
        </div>

        <div class="content-section">
          <h3 class="section-title">📝 物件介绍</h3>
          <p class="content-text">{{ post.content }}</p>
        </div>

        <div class="content-section" v-if="post.story">
          <h3 class="section-title">📖 物件来历</h3>
          <p class="content-text">{{ post.story }}</p>
        </div>

        <div class="content-section" v-if="post.memory">
          <h3 class="section-title">💭 个人回忆</h3>
          <p class="content-text">{{ post.memory }}</p>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { postsAPI, commentsAPI, favoritesAPI } from '../api'
import { getSessionId } from '../utils/session'

const route = useRoute()
const postId = computed(() => route.params.id)

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const commentAuthor = ref('')
const isFavorited = ref(false)
const userSession = getSessionId()

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const apiBase = import.meta.env.VITE_API_BASE_URL || ''
  return apiBase.replace('/api', '') + url
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

const loadPost = async () => {
  try {
    const res = await postsAPI.getDetail(postId.value)
    post.value = res.data
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

.post-image-item {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.post-image-item img {
  width: 100%;
  max-height: 500px;
  object-fit: contain;
  background: #f9f9f9;
}

.post-body {
  margin-bottom: 20px;
}

.info-row {
  margin-bottom: 16px;
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

@media (max-width: 768px) {
  .detail-card {
    padding: 20px;
  }
  .post-title {
    font-size: 22px;
  }
}
</style>
