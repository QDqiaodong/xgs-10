<template>
  <div class="archives container">
    <section class="archive-hero">
      <div class="hero-content">
        <h1 class="page-title">📚 物件年代档案</h1>
        <p class="page-desc">穿越时光的珍藏，每一件老物件都承载着一段独特的历史记忆</p>
      </div>
    </section>

    <section class="stats-section card" v-if="stats">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalPosts }}</div>
            <div class="stat-label">馆藏总数</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">📅</div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalEras }}</div>
            <div class="stat-label">年代跨度</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">🏷️</div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalCategories }}</div>
            <div class="stat-label">物品种类</div>
          </div>
        </div>
        <div class="stat-item" v-if="stats.totalPreservationStatuses">
          <div class="stat-icon">🔧</div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalPreservationStatuses }}</div>
            <div class="stat-label">保存状态</div>
          </div>
        </div>
      </div>
      <div class="preservation-stats" v-if="stats.preservationStatusStats && stats.preservationStatusStats.length > 0">
        <h4 class="sub-stat-title">保存状态分布</h4>
        <div class="preservation-bars">
          <div 
            v-for="statusStat in stats.preservationStatusStats" 
            :key="statusStat.label"
            class="preservation-bar-item"
          >
            <div class="bar-label">
              <span class="bar-icon">{{ statusStat.icon }}</span>
              <span class="bar-name">{{ statusStat.label }}</span>
              <span class="bar-count">{{ statusStat.totalCount }} 件</span>
            </div>
            <div class="bar-track">
              <div 
                class="bar-fill" 
                :style="{ 
                  width: getPercentage(statusStat.totalCount, stats.totalPosts) + '%',
                  background: statusStat.color 
                }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="filter-section card">
      <div class="filter-group">
        <span class="filter-label">年代筛选：</span>
        <button
          :class="['filter-btn', `era-${getEraClass(selectedEraName)}`, { active: !selectedEra }]"
          @click="selectedEra = null"
        >
          全部年代
        </button>
        <button
          v-for="era in eras"
          :key="era.id"
          :class="['filter-btn', `era-${getEraClass(era.name)}`, { active: selectedEra === era.id }]"
          @click="selectedEra = era.id"
        >
          <span class="era-dot"></span>
          {{ era.name }}
        </button>
      </div>
      <div class="filter-group">
        <span class="filter-label">品类筛选：</span>
        <button
          :class="['filter-btn', 'filter-cat', { active: !selectedCategory }]"
          @click="selectedCategory = null"
        >
          全部品类
        </button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="['filter-btn', 'filter-cat', getCategoryClass(cat.name), { active: selectedCategory === cat.id }]"
          :style="getCategoryStyleVars(cat.name)"
          @click="selectedCategory = cat.id"
        >
          <CategoryIcon :category="cat.name" size="sm" mode="emoji" />
          {{ cat.name }}
        </button>
      </div>
      <div class="filter-group">
        <span class="filter-label">保存状态：</span>
        <button
          :class="['filter-btn', 'filter-status', { active: !selectedPreservationStatus }]"
          @click="selectedPreservationStatus = null"
        >
          全部状态
        </button>
        <button
          v-for="status in preservationStatuses"
          :key="status.label"
          :class="['filter-btn', 'filter-status', { active: selectedPreservationStatus === status.label }]"
          :style="getStatusStyleVars(status)"
          @click="selectedPreservationStatus = status.label"
        >
          <span class="status-icon">{{ status.icon }}</span>
          {{ status.label }}
        </button>
      </div>
    </section>

    <div class="content-header">
      <h2 class="section-title">{{ viewMode === 'grouped' ? '📚 分组档案' : '📋 档案列表' }}</h2>
      <div class="view-toggle">
        <button
          :class="['toggle-btn', { active: viewMode === 'grouped' }]"
          @click="viewMode = 'grouped'"
        >
          📚 分组视图
        </button>
        <button
          :class="['toggle-btn', { active: viewMode === 'list' }]"
          @click="viewMode = 'list'"
        >
          📋 列表视图
        </button>
      </div>
    </div>

    <section class="archive-content" v-if="viewMode === 'grouped'">
      <div
        v-for="eraGroup in filteredEraGroups"
        :key="eraGroup.eraId"
        class="era-section"
      >
        <div :class="['era-header', `header-${getEraClass(eraGroup.eraName)}`]">
          <div class="era-title">
            <span class="era-icon">{{ getEraIcon(eraGroup.eraName) }}</span>
            <h2 class="era-name">{{ eraGroup.eraName }}</h2>
            <span class="era-years" v-if="eraGroup.yearStart && eraGroup.yearEnd">
              ({{ eraGroup.yearStart }} - {{ eraGroup.yearEnd }})
            </span>
          </div>
          <div class="era-count">
            <span class="count-badge">{{ eraGroup.totalCount }} 件藏品</span>
          </div>
        </div>

        <div v-if="eraGroup.categories && eraGroup.categories.length > 0">
          <div
            v-for="catGroup in eraGroup.categories"
            :key="catGroup.categoryId"
            class="category-section"
          >
            <div class="category-header" :class="getCategoryClass(catGroup.categoryName)" :style="getCategoryStyleVars(catGroup.categoryName)">
              <CategoryIcon :category="catGroup.categoryName" size="lg" mode="emoji" show-bg />
              <h3 class="category-name">{{ catGroup.categoryName }}</h3>
              <span class="category-count">{{ catGroup.count }} 件</span>
            </div>

            <div class="archive-items-grid">
              <router-link
                v-for="item in catGroup.items"
                :key="item.id"
                :to="`/post/${item.id}`"
                :class="['archive-item-card', 'card', `card-era-${getEraClass(item.eraName)}`]"
              >
                <div class="item-image-wrapper">
                  <img
                    :src="getItemImgUrl(item.images)"
                    :alt="item.itemName"
                    class="item-image"
                  />
                  <div 
                    class="preservation-badge"
                    :style="getPreservationBadgeStyle(item.preservationStatus)"
                  >
                    {{ getStatusIcon(item.preservationStatus) }} {{ item.preservationStatus || '待评估' }}
                  </div>
                </div>
                <div class="item-content">
                  <h4 class="item-name">{{ safeDisplayItemName(item.itemName) }}</h4>
                  <p class="item-title">{{ item.title }}</p>
                  <div class="item-summary-structured" v-if="item.itemSource || item.usageScene || (item.emotionKeywords && item.emotionKeywords.length > 0)">
                    <span class="summary-chip summary-source" v-if="item.itemSource">
                      <span class="chip-icon">📦</span>{{ truncateForChip(item.itemSource) }}
                    </span>
                    <span class="summary-chip summary-scene" v-if="item.usageScene">
                      <span class="chip-icon">📍</span>{{ truncateForChip(item.usageScene) }}
                    </span>
                    <span class="summary-chip summary-emotion" v-for="kw in (item.emotionKeywords || []).slice(0, 3)" :key="kw">
                      <span class="chip-icon">💭</span>{{ truncateForChip(kw) }}
                    </span>
                  </div>
                  <p class="item-summary">{{ buildArchiveSummary(item) || '暂无故事摘要' }}</p>
                  <div class="item-footer">
                    <span class="item-tags">
                      <span :class="['tag', 'tag-cat', getCategoryClass(item.categoryName)]" :style="getCategoryStyleVars(item.categoryName)">
                        <CategoryIcon :category="item.categoryName" size="xs" />
                        {{ item.categoryName }}
                      </span>
                      <span :class="['tag', `tag-era-${getEraClass(item.eraName)}`]">{{ item.eraName }}</span>
                    </span>
                  </div>
                </div>
              </router-link>
            </div>
          </div>
        </div>
        <div v-else class="empty-era">
          <p>这个年代暂无藏品</p>
        </div>
      </div>
      <div class="empty-state" v-if="filteredEraGroups.length === 0">
        <div class="empty-icon">📭</div>
        <p>暂无符合条件的档案</p>
      </div>
    </section>

    <section class="archive-list-section" v-else>
      <div class="archive-items-grid" v-if="posts.length > 0">
        <router-link
          v-for="item in posts"
          :key="item.id"
          :to="`/post/${item.id}`"
          :class="['archive-item-card', 'card', `card-era-${getEraClass(item.eraName)}`]"
        >
          <div class="item-image-wrapper">
            <img
              :src="getItemImgUrl(item.images)"
              :alt="item.itemName"
              class="item-image"
            />
            <div :class="['preservation-badge', getPreservationClass(item.preservationStatus)]">
              {{ item.preservationStatus || '待评估' }}
            </div>
          </div>
          <div class="item-content">
            <h4 class="item-name">{{ safeDisplayItemName(item.itemName) }}</h4>
            <p class="item-title">{{ item.title }}</p>
            <p class="item-summary">{{ buildArchiveSummary(item) || '暂无故事摘要' }}</p>
            <div class="item-footer">
              <span class="item-tags">
                <span :class="['tag', 'tag-cat', getCategoryClass(item.categoryName)]" :style="getCategoryStyleVars(item.categoryName)">
                  <CategoryIcon :category="item.categoryName" size="xs" />
                  {{ item.categoryName }}
                </span>
                <span :class="['tag', `tag-era-${getEraClass(item.eraName)}`]">{{ item.eraName }}</span>
              </span>
            </div>
          </div>
        </router-link>
      </div>

      <div class="pagination" v-if="viewMode === 'list' && totalPages > 1">
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

      <div class="empty-state" v-if="posts.length === 0">
        <div class="empty-icon">📭</div>
        <p>暂无符合条件的档案</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { archivesAPI } from '../api'
import { displayItemName, buildArchiveSummary, truncateForChip } from '../utils/textCleaner'
import { getCategoryClass, getCategoryStyleVars } from '../icons/categoryUtils'
import { getEraClass, getEraIcon, sortErasDefault, normalizeEraName } from '../utils/eraUtils'
import { getImageUrl, getMainImage } from '../utils/imageLayout'
import { getStatusConfig, getAllStatuses } from '../utils/preservationStatus'
import { postsAPI } from '../api'

const eras = ref([])
const categories = ref([])
const preservationStatuses = ref([])
const posts = ref([])
const groupedData = ref([])
const stats = ref(null)
const selectedEra = ref(null)
const selectedCategory = ref(null)
const selectedPreservationStatus = ref(null)
const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 20
const viewMode = ref('grouped')

const selectedEraName = computed(() => {
  const era = eras.value.find(e => e.id === selectedEra.value)
  return era ? era.name : ''
})

const filteredEraGroups = computed(() => {
  let result = groupedData.value
  if (selectedEra.value) {
    result = result.filter(g => g.eraId === selectedEra.value)
  }
  if (selectedCategory.value) {
    result = result.map(eraGroup => {
      const filteredCats = (eraGroup.categories || []).filter(
        c => c.categoryId === selectedCategory.value
      )
      const totalCount = filteredCats.reduce((sum, c) => sum + (c.count || 0), 0)
      return {
        ...eraGroup,
        categories: filteredCats,
        totalCount
      }
    }).filter(g => g.totalCount > 0)
  }
  return result
})

const safeDisplayItemName = (name) => displayItemName(name)

const getItemImgUrl = (images) => {
  if (!images || images.length === 0) return 'https://picsum.photos/400/300'
  const main = getMainImage(images)
  if (main) return main.url
  return getImageUrl(images[0])
}

const getStatusStyleVars = (status) => {
  return {
    '--status-color': status.color,
    '--status-bg': status.bgGradient,
    '--status-border': status.borderColor
  }
}

const getStatusIcon = (statusLabel) => {
  return getStatusConfig(statusLabel).icon
}

const getPreservationBadgeStyle = (statusLabel) => {
  const config = getStatusConfig(statusLabel)
  return {
    background: config.color,
    color: '#fff',
    border: `1px solid ${config.borderColor}`
  }
}

const loadPreservationStatuses = async () => {
  try {
    const res = await postsAPI.getPreservationStatuses()
    if (res.data && res.data.length > 0) {
      preservationStatuses.value = res.data.map(item => ({
        ...item,
        ...getStatusConfig(item.label)
      }))
    } else {
      preservationStatuses.value = getAllStatuses()
    }
  } catch (e) {
    console.error('加载保存状态失败，使用本地配置', e)
    preservationStatuses.value = getAllStatuses()
  }
}

const getPercentage = (count, total) => {
  if (!total || total === 0) return 0
  return Math.round((count / total) * 100)
}

const loadStats = async () => {
  try {
    const res = await archivesAPI.getStats()
    stats.value = res.data
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const loadFilters = async () => {
  try {
    const res = await archivesAPI.getList({ page: 0, size: 1 })
    let erasList = res.data.eras || []
    erasList = erasList.map(era => ({
      ...era,
      name: normalizeEraName(era.name)
    }))
    eras.value = sortErasDefault(erasList)
    categories.value = res.data.categories || []
  } catch (e) {
    console.error('加载筛选条件失败', e)
  }
}

const loadGroupedData = async () => {
  try {
    const res = await archivesAPI.getGrouped({
      categoryId: selectedCategory.value,
      preservationStatus: selectedPreservationStatus.value
    })
    let groups = res.data || []
    groups = groups.map(group => ({
      ...group,
      eraName: normalizeEraName(group.eraName)
    }))
    groups = sortErasDefault(groups.map(g => ({ ...g, sortOrder: g.sortOrder, yearStart: g.yearStart, name: g.eraName })))
      .map(sorted => {
        const original = groups.find(g => g.eraId === sorted.eraId)
        return original
      })
    groupedData.value = groups
  } catch (e) {
    console.error('加载分组档案失败', e)
  }
}

const loadListData = async () => {
  try {
    const res = await archivesAPI.getList({
      categoryId: selectedCategory.value,
      eraId: selectedEra.value,
      preservationStatus: selectedPreservationStatus.value,
      page: currentPage.value,
      size: pageSize
    })
    posts.value = res.data.posts || []
    totalPages.value = res.data.totalPages || 1
  } catch (e) {
    console.error('加载档案列表失败', e)
  }
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadListData()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

watch([selectedEra, selectedCategory, selectedPreservationStatus], () => {
  currentPage.value = 0
  if (viewMode.value === 'grouped') {
    loadGroupedData()
  } else {
    loadListData()
  }
})

watch(viewMode, () => {
  currentPage.value = 0
  if (viewMode.value === 'grouped') {
    loadGroupedData()
  } else {
    loadListData()
  }
})

onMounted(() => {
  loadStats()
  loadFilters()
  loadPreservationStatuses()
  loadGroupedData()
  loadListData()
})
</script>

<style scoped>
.archive-hero {
  text-align: center;
  padding: 40px 0 50px;
  position: relative;
  background: linear-gradient(135deg, rgba(139, 105, 20, 0.08) 0%, rgba(107, 79, 15, 0.05) 100%);
  border-radius: 16px;
  margin-bottom: 30px;
}

.page-title {
  font-size: 36px;
  color: #6b4f0f;
  margin-bottom: 12px;
  font-weight: 600;
  letter-spacing: 2px;
}

.page-desc {
  font-size: 18px;
  color: #8b6914;
  opacity: 0.8;
}

.stats-section {
  padding: 24px;
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fdf6e3 0%, #f5e6d3 100%);
  border-radius: 12px;
  border: 1px solid #e8d5b8;
}

.stat-icon {
  font-size: 36px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #6b4f0f;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #8b6914;
}

.preservation-stats {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed #e8d5b8;
}

.sub-stat-title {
  font-size: 16px;
  color: #5d4e37;
  margin-bottom: 16px;
  font-weight: 600;
}

.preservation-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preservation-bar-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bar-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.bar-icon {
  font-size: 14px;
}

.bar-name {
  color: #5d4e37;
  font-weight: 500;
}

.bar-count {
  margin-left: auto;
  color: #8b6914;
  font-weight: 600;
}

.bar-track {
  height: 8px;
  background: #f5e6d3;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
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
  border: 2px solid transparent;
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

.era-60s { background: #e8ddd4; color: #3e2723; border: 2px solid #5d4037; }
.era-60s:hover { background: #d7ccc8; }
.era-60s.active { background: linear-gradient(135deg, #6d4c41 0%, #4e342e 100%); color: #fff; border-color: #4e342e; }
.era-60s .era-dot { background: #5d4037; }
.era-60s.active .era-dot { background: #ffd54f; }

.era-70s { background: #ffecd1; color: #5d4037; border: 2px solid #a1887f; }
.era-70s:hover { background: #ffe0b2; }
.era-70s.active { background: linear-gradient(135deg, #bf360c 0%, #8d6e63 100%); color: #fff; border-color: #bf360c; }
.era-70s .era-dot { background: #bf360c; }
.era-70s.active .era-dot { background: #fff176; }

.era-80s { background: #d4e9fc; color: #0d47a1; border: 2px solid #1976d2; }
.era-80s:hover { background: #bbdefb; }
.era-80s.active { background: linear-gradient(135deg, #1976d2 0%, #0d47a1 100%); color: #fff; border-color: #0d47a1; }
.era-80s .era-dot { background: #1976d2; }
.era-80s.active .era-dot { background: #81d4fa; }

.era-90s { background: #ffe0cc; color: #bf360c; border: 2px solid #ff6f00; }
.era-90s:hover { background: #ffcc99; }
.era-90s.active { background: linear-gradient(135deg, #ff6f00 0%, #e64a19 100%); color: #fff; border-color: #e65100; }
.era-90s .era-dot { background: #ff6f00; }
.era-90s.active .era-dot { background: #ffd180; }

.era-00s { background: #cff5f8; color: #006064; border: 2px solid #00acc1; }
.era-00s:hover { background: #b2ebf2; }
.era-00s.active { background: linear-gradient(135deg, #00acc1 0%, #006064 100%); color: #fff; border-color: #006064; }
.era-00s .era-dot { background: #00acc1; }
.era-00s.active .era-dot { background: #84ffff; }

.filter-btn .era-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.filter-status {
  background: var(--status-bg);
  color: var(--status-color);
  border: 2px solid var(--status-border);
  font-weight: 600;
}

.filter-status:hover {
  transform: translateY(-1px);
  opacity: 0.9;
}

.filter-status.active {
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.1);
  opacity: 1;
}

.status-icon {
  font-size: 14px;
}

.preservation-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(8px);
}

.era-section {
  margin-bottom: 40px;
}

.era-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-radius: 12px 12px 0 0;
  margin-bottom: 0;
}

.header-60s { background: linear-gradient(135deg, #5d4037 0%, #4e342e 100%); }
.header-70s { background: linear-gradient(135deg, #bf360c 0%, #8d6e63 100%); }
.header-80s { background: linear-gradient(135deg, #1976d2 0%, #0d47a1 100%); }
.header-90s { background: linear-gradient(135deg, #ff6f00 0%, #e64a19 100%); }
.header-00s { background: linear-gradient(135deg, #00acc1 0%, #006064 100%); }

.era-title {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.era-icon {
  font-size: 24px;
}

.era-name {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.era-years {
  font-size: 14px;
  opacity: 0.9;
}

.era-count .count-badge {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

.category-section {
  background: #fff;
  border: 1px solid #f0e6d8;
  border-top: none;
  padding: 24px;
}

.category-section:last-child {
  border-radius: 0 0 12px 12px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #e8d5b8;
}

.category-icon {
  font-size: 24px;
}

.category-name {
  font-size: 18px;
  font-weight: 600;
  color: #5d4e37;
  margin: 0;
}

.category-count {
  margin-left: auto;
  font-size: 13px;
  color: #8b6914;
  background: #fdf6e3;
  padding: 4px 12px;
  border-radius: 12px;
}

.archive-items-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.archive-item-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s;
}

.archive-item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.card-era-60s {
  border-top: 4px solid #5d4037;
  background: linear-gradient(135deg, #fff 0%, #f5f0eb 50%, #efe5db 100%);
}
.card-era-70s {
  border-top: 4px solid #8d6e63;
  background: linear-gradient(135deg, #fff 0%, #fff5e6 50%, #ffeacc 100%);
}
.card-era-80s {
  border-top: 4px solid #1976d2;
  background: linear-gradient(135deg, #fff 0%, #eff6fc 50%, #dcebf9 100%);
}
.card-era-90s {
  border-top: 4px solid #ff6f00;
  background: linear-gradient(135deg, #fff 0%, #fff4e8 50%, #ffe6d1 100%);
}
.card-era-00s {
  border-top: 4px solid #00acc1;
  background: linear-gradient(135deg, #fff 0%, #edfafb 50%, #d6f3f6 100%);
}

.item-image-wrapper {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f5f0eb;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.archive-item-card:hover .item-image {
  transform: scale(1.08);
}

.preservation-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(8px);
}

.item-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 6px;
}

.item-title {
  font-size: 13px;
  color: #8b6914;
  margin-bottom: 8px;
  font-style: italic;
}

.item-summary {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.item-summary-structured {
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

.item-footer {
  margin-top: auto;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
}

.tag-cat {
  background: #f5e6d3;
  color: #8b6914;
}

.tag-era-60s { background: #d7ccc8; color: #3e2723; border: 1px solid #8d6e63; font-weight: 500; }
.tag-era-70s { background: #ffcc80; color: #4e342e; border: 1px solid #ef6c00; font-weight: 500; }
.tag-era-80s { background: #90caf9; color: #0d47a1; border: 1px solid #1976d2; font-weight: 600; }
.tag-era-90s { background: #ffab40; color: #bf360c; border: 1px solid #ff6f00; font-weight: 600; }
.tag-era-00s { background: #80deea; color: #006064; border: 1px solid #00acc1; font-weight: 500; }

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  color: #6b4f0f;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-toggle {
  display: flex;
  gap: 8px;
}

.toggle-btn {
  padding: 8px 16px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #d4a574;
  color: #d4a574;
  font-size: 13px;
  transition: all 0.3s;
}

.toggle-btn:hover {
  background: #fdf6e3;
}

.toggle-btn.active {
  background: linear-gradient(135deg, #d4a574 0%, #c19660 100%);
  color: #fff;
  border-color: #c19660;
}

.empty-era {
  background: #fff;
  border: 1px solid #f0e6d8;
  border-top: none;
  padding: 40px;
  text-align: center;
  color: #999;
  border-radius: 0 0 12px 12px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state p {
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

@media (max-width: 1024px) {
  .archive-items-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 28px;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .archive-items-grid {
    grid-template-columns: 1fr;
  }
  .era-name {
    font-size: 18px;
  }
  .category-header {
    flex-wrap: wrap;
  }
}
</style>
