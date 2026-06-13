<template>
  <div class="publish container">
    <div class="publish-card card">
      <h1 class="page-title">✨ 分享你的老物件</h1>
      <p class="page-desc">上传物件照片，讲述它的故事，让更多人一起回味那段时光</p>

      <form @submit.prevent="handleSubmit" class="publish-form">
        <div class="form-section">
          <h2 class="section-title">📷 物件照片</h2>
          <div class="image-upload-area">
            <div
              v-for="(img, idx) in previewImages"
              :key="idx"
              class="image-preview"
            >
              <img :src="img" alt="preview" />
              <button type="button" class="remove-btn" @click="removeImage(idx)">×</button>
            </div>
            <label v-if="previewImages.length < 6" class="upload-btn">
              <span class="upload-icon">+</span>
              <span class="upload-text">添加图片</span>
              <input
                type="file"
                accept="image/*"
                multiple
                @change="handleImageSelect"
                hidden
              />
            </label>
          </div>
          <p class="upload-tip">最多上传6张图片，支持自动压缩优化</p>
        </div>

        <div class="form-section">
          <h2 class="section-title">📋 基本信息</h2>

          <div class="form-row">
            <div class="form-group">
              <label>标题 *</label>
              <input
                v-model="form.title"
                type="text"
                placeholder="给你的回忆起个标题"
                required
              />
            </div>
            <div class="form-group">
              <label>物件名称 *</label>
              <input
                v-model="form.itemName"
                type="text"
                placeholder="例如：铁皮青蛙、双卡录音机"
                required
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>所属品类 *</label>
              <select v-model="form.categoryId" required>
                <option value="">请选择品类</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.icon }} {{ cat.name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>所属年代 *</label>
              <select v-model="form.eraId" required>
                <option value="">请选择年代</option>
                <option v-for="era in eras" :key="era.id" :value="era.id">
                  {{ era.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>你的昵称</label>
            <input
              v-model="form.authorName"
              type="text"
              placeholder="不填则显示为匿名用户"
            />
          </div>
        </div>

        <div class="form-section">
          <h2 class="section-title">📝 故事内容</h2>

          <div class="form-group">
            <label>物件介绍 *</label>
            <textarea
              v-model="form.content"
              placeholder="简单介绍一下这个老物件..."
              rows="3"
              required
            ></textarea>
          </div>

          <div class="form-group">
            <label>物件来历</label>
            <textarea
              v-model="form.story"
              placeholder="这个物件是怎么来的？背后有什么故事？"
              rows="3"
            ></textarea>
          </div>

          <div class="form-group">
            <label>个人回忆</label>
            <textarea
              v-model="form.memory"
              placeholder="看到它你想起了什么？分享你的个人回忆..."
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-outline" @click="resetForm">
            重置
          </button>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? '发布中...' : '发布回忆' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoriesAPI, erasAPI, postsAPI } from '../api'
import { processImage, createImagePreview } from '../utils/imageProcessor'

const router = useRouter()

const categories = ref([])
const eras = ref([])
const previewImages = ref([])
const selectedFiles = ref([])
const submitting = ref(false)

const form = reactive({
  title: '',
  itemName: '',
  categoryId: '',
  eraId: '',
  authorName: '',
  content: '',
  story: '',
  memory: ''
})

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

const handleImageSelect = async (e) => {
  const files = Array.from(e.target.files)
  for (const file of files) {
    if (previewImages.value.length >= 6) break
    if (!file.type.startsWith('image/')) continue

    try {
      const processed = await processImage(file)
      const preview = await createImagePreview(processed)
      previewImages.value.push(preview)
      selectedFiles.value.push(processed)
    } catch (err) {
      console.error('图片处理失败', err)
    }
  }
  e.target.value = ''
}

const removeImage = (index) => {
  previewImages.value.splice(index, 1)
  selectedFiles.value.splice(index, 1)
}

const resetForm = () => {
  Object.keys(form).forEach(key => {
    form[key] = ''
  })
  previewImages.value = []
  selectedFiles.value = []
}

const handleSubmit = async () => {
  if (submitting.value) return

  submitting.value = true
  try {
    const formData = new FormData()

    formData.append('title', form.title)
    formData.append('itemName', form.itemName)
    formData.append('categoryId', Number(form.categoryId))
    formData.append('eraId', Number(form.eraId))
    if (form.authorName) formData.append('authorName', form.authorName)
    formData.append('content', form.content)
    if (form.story) formData.append('story', form.story)
    if (form.memory) formData.append('memory', form.memory)

    selectedFiles.value.forEach((file) => {
      formData.append('images', file)
    })

    const res = await postsAPI.create(formData)
    alert('发布成功！')
    router.push(`/post/${res.data.id}`)
  } catch (e) {
    console.error('发布失败', e)
    alert('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadEras()
})
</script>

<style scoped>
.publish-card {
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #5d4e37;
  text-align: center;
  margin-bottom: 8px;
}

.page-desc {
  text-align: center;
  color: #999;
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.form-section:last-of-type {
  border-bottom: none;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #6b4f0f;
  margin-bottom: 16px;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}

.image-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 120px;
  height: 120px;
  border: 2px dashed #d4a574;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #d4a574;
  transition: all 0.3s;
  background: #fdf6e3;
}

.upload-btn:hover {
  border-color: #c19660;
  color: #c19660;
  background: #f5e6d3;
}

.upload-icon {
  font-size: 32px;
  line-height: 1;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 12px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #5d4e37;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e0d5c5;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #d4a574;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.form-actions .btn {
  padding: 12px 40px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .publish-card {
    padding: 20px;
  }
  .form-row {
    grid-template-columns: 1fr;
  }
  .page-title {
    font-size: 22px;
  }
}
</style>
