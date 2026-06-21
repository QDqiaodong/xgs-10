<template>
  <div class="post-detail container">
    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">正在加载回忆...</p>
    </div>

    <div class="not-found-state" v-else-if="notFound">
      <div class="not-found-icon">📭</div>
      <h1 class="not-found-title">回忆未找到</h1>
      <p class="not-found-message">{{ errorMessage || '该档案不存在或已被删除' }}</p>
      <div class="not-found-actions">
        <router-link to="/" class="btn btn-primary">
          <span class="btn-icon">🏠</span> 返回首页
        </router-link>
        <router-link to="/archives" class="btn btn-secondary">
          <span class="btn-icon">📚</span> 浏览档案
        </router-link>
      </div>
      <div class="not-found-id" v-if="postId">
        您访问的档案编号：<span class="id-highlight">#{{ postId }}</span>
      </div>
    </div>

    <div class="error-state" v-else-if="error && !notFound">
      <div class="error-icon">⚠️</div>
      <h1 class="error-title">加载失败</h1>
      <p class="error-message">{{ errorMessage || '加载回忆时出现问题，请稍后重试' }}</p>
      <button class="btn btn-primary" @click="retryLoad">
        <span class="btn-icon">🔄</span> 重新加载
      </button>
    </div>

    <div v-else-if="post">
    <router-link to="/" class="back-link">← 返回列表</router-link>

    <article class="detail-card card">
      <header class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span class="author">作者：{{ post.authorName }}</span>
          <span class="date">{{ formatDate(post.createdAt) }}</span>
        </div>
      </header>

      <div class="split-layout">
        <div class="split-left">
          <div class="main-image-area">
            <div 
              class="main-image-wrapper vintage-photo-frame" 
              :class="`frame-era-${getEraClass(post.eraName)}`"
              v-if="post.images && post.images.length > 0"
            >
              <div class="frame-paper-texture"></div>
              <div class="frame-inner">
                <img
                  :src="getImageUrl(mainImage)"
                  :alt="post.title"
                  class="main-image"
                />
              </div>
            </div>
            <div class="main-image-placeholder" v-else>
              <span class="placeholder-icon">📷</span>
              <span class="placeholder-text">暂无图片</span>
            </div>

            <div class="thumbnail-list" v-if="post.images && post.images.length > 1">
              <div
                v-for="(img, idx) in post.images"
                :key="idx"
                :class="['thumbnail-item', 'vintage-photo-frame', `frame-era-${getEraClass(post.eraName)}`, { active: idx === activeImageIndex }]"
                @click="activeImageIndex = idx"
              >
                <div class="frame-inner">
                  <img :src="getImageUrl(img)" :alt="`${post.title} ${idx + 1}`" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="split-right">
          <div class="story-content story-content-top mobile-first">
            <div class="content-section" v-if="buildDetailSummary(post) || post.itemSource || (post.emotionKeywords && post.emotionKeywords.length > 0)">
              <div class="section-header">
                <span class="section-icon">📋</span>
                <h3 class="section-title">故事摘要</h3>
              </div>
              <div class="summary-structured" v-if="post.sourceTypeName || post.itemSource || post.usageScene || (post.emotionKeywords && post.emotionKeywords.length > 0)">
                <div class="summary-row source-archive-row" v-if="post.sourceTypeName || post.itemSource">
                  <span class="summary-label">� 来源档案</span>
                  <div class="source-archive-info">
                    <span class="source-type-badge" :style="getSourceTypeStyle(post)">
                      <span class="source-type-icon">{{ post.sourceTypeIcon || '📦' }}</span>
                      <span class="source-type-name">{{ post.sourceTypeName || '来源档案' }}</span>
                    </span>
                    <span class="source-detail-text" v-if="post.itemSource">{{ truncateForChip(post.itemSource, 60) }}</span>
                  </div>
                </div>
                <div class="summary-row" v-if="post.usageScene">
                  <span class="summary-label">📍 场景</span>
                  <span class="summary-value summary-scene-value">{{ truncateForChip(post.usageScene, 60) }}</span>
                </div>
                <div class="summary-row" v-if="post.emotionKeywords && post.emotionKeywords.length > 0">
                  <span class="summary-label">💭 情感</span>
                  <div class="summary-emotion-tags">
                    <span class="emotion-tag" v-for="kw in post.emotionKeywords" :key="kw">{{ truncateForChip(kw) }}</span>
                  </div>
                </div>
              </div>
              <p class="summary-text" v-if="buildDetailSummary(post)">{{ buildDetailSummary(post) }}</p>
            </div>
          </div>

          <div :class="['nameplate-card', { 'nameplate-collapsed': !effectiveNameplateExpanded }]">
            <div class="nameplate-header" @click="toggleNameplate">
              <div class="nameplate-icon">📜</div>
              <h2 class="nameplate-title">物件铭牌</h2>
              <div class="nameplate-decoration-line"></div>
              <span class="nameplate-toggle-icon" :class="{ expanded: effectiveNameplateExpanded }">▼</span>
            </div>

            <div class="nameplate-compact-summary" v-if="!effectiveNameplateExpanded">
              <span class="nameplate-compact-name">{{ safeDisplayItemName(post.itemName) }}</span>
              <span class="era-badge">{{ post.eraName }}</span>
              <span
                :class="['category-badge', getCategoryClass(post.categoryName)]"
                :style="getCategoryStyleVars(post.categoryName)"
              >
                <CategoryIcon :category="post.categoryName" size="xs" />
                {{ post.categoryName }}
              </span>
            </div>

            <div class="nameplate-body" :class="{ 'nameplate-body-hidden': !effectiveNameplateExpanded }">
              <div class="nameplate-row">
                <span class="nameplate-label">
                  <span class="label-icon">🏷️</span>
                  物件名称
                </span>
                <span class="nameplate-value nameplate-item-name">
                  {{ safeDisplayItemName(post.itemName) }}
                </span>
              </div>

              <div class="nameplate-row">
                <span class="nameplate-label">
                  <span class="label-icon">🕰️</span>
                  所属年代
                </span>
                <span class="nameplate-value">
                  <span class="era-badge">{{ post.eraName }}</span>
                </span>
              </div>

              <div class="nameplate-row">
                <span class="nameplate-label">
                  <span class="label-icon">📦</span>
                  物件类别
                </span>
                <span class="nameplate-value">
                  <span
                    :class="['category-badge', getCategoryClass(post.categoryName)]"
                    :style="getCategoryStyleVars(post.categoryName)"
                  >
                    <CategoryIcon :category="post.categoryName" size="xs" />
                    {{ post.categoryName }}
                  </span>
                </span>
              </div>

              <div class="nameplate-row" v-if="post.sourceTypeName || post.itemSource">
                <span class="nameplate-label">
                  <span class="label-icon">�</span>
                  来源档案
                </span>
                <span class="nameplate-value">
                  <div class="nameplate-source-archive">
                    <span class="source-type-badge nameplate-source-badge" :style="getSourceTypeStyle(post)">
                      <span class="source-type-icon">{{ post.sourceTypeIcon || '�' }}</span>
                      <span class="source-type-name">{{ post.sourceTypeName || '来源档案' }}</span>
                    </span>
                    <span class="nameplate-source-detail" v-if="post.itemSource">{{ post.itemSource }}</span>
                  </div>
                </span>
              </div>

              <div class="nameplate-row" v-if="post.usageScene">
                <span class="nameplate-label">
                  <span class="label-icon">📍</span>
                  使用场景
                </span>
                <span class="nameplate-value">{{ post.usageScene }}</span>
              </div>

              <div class="nameplate-row" v-if="post.preservationStatus">
                <span class="nameplate-label">
                  <span class="label-icon">✨</span>
                  保存状态
                </span>
                <span class="nameplate-value">
                  <span :class="['preservation-badge', getPreservationClass(post.preservationStatus)]">
                    <span class="status-icon">{{ getPreservationIcon(post.preservationStatus) }}</span>
                    {{ post.preservationStatus }}
                  </span>
                </span>
              </div>
              <div class="nameplate-row" v-if="post.emotionKeywords && post.emotionKeywords.length > 0">
                <span class="nameplate-label">
                  <span class="label-icon">💭</span>
                  情感关键词
                </span>
                <span class="nameplate-value">
                  <span class="nameplate-emotion-tags">
                    <span class="emotion-tag" v-for="kw in post.emotionKeywords" :key="kw">{{ kw }}</span>
                  </span>
                </span>
              </div>
            </div>

            <div class="nameplate-footer">
              <div class="decoration-corner tl"></div>
              <div class="decoration-corner tr"></div>
              <div class="decoration-corner bl"></div>
              <div class="decoration-corner br"></div>
            </div>
          </div>

          <div class="story-content">
            <div class="content-section story-summary-desktop" v-if="buildDetailSummary(post) || post.itemSource || (post.emotionKeywords && post.emotionKeywords.length > 0)">
              <div class="section-header">
                <span class="section-icon">📋</span>
                <h3 class="section-title">故事摘要</h3>
              </div>
              <div class="summary-structured" v-if="post.sourceTypeName || post.itemSource || post.usageScene || (post.emotionKeywords && post.emotionKeywords.length > 0)">
                <div class="summary-row source-archive-row" v-if="post.sourceTypeName || post.itemSource">
                  <span class="summary-label">� 来源档案</span>
                  <div class="source-archive-info">
                    <span class="source-type-badge" :style="getSourceTypeStyle(post)">
                      <span class="source-type-icon">{{ post.sourceTypeIcon || '📦' }}</span>
                      <span class="source-type-name">{{ post.sourceTypeName || '来源档案' }}</span>
                    </span>
                    <span class="source-detail-text" v-if="post.itemSource">{{ truncateForChip(post.itemSource, 60) }}</span>
                  </div>
                </div>
                <div class="summary-row" v-if="post.usageScene">
                  <span class="summary-label">📍 场景</span>
                  <span class="summary-value summary-scene-value">{{ truncateForChip(post.usageScene, 60) }}</span>
                </div>
                <div class="summary-row" v-if="post.emotionKeywords && post.emotionKeywords.length > 0">
                  <span class="summary-label">💭 情感</span>
                  <div class="summary-emotion-tags">
                    <span class="emotion-tag" v-for="kw in post.emotionKeywords" :key="kw">{{ truncateForChip(kw) }}</span>
                  </div>
                </div>
              </div>
              <p class="summary-text" v-if="buildDetailSummary(post)">{{ buildDetailSummary(post) }}</p>
            </div>

            <div class="story-layers">
              <div class="story-layer" v-if="post.eraBackground">
                <div class="story-layer-header">
                  <span class="story-layer-icon">🏛️</span>
                  <h3 class="story-layer-title">年代背景</h3>
                  <button
                    v-if="shouldCollapseStory(post.eraBackground)"
                    class="toggle-story-btn"
                    @click="toggleLayer('eraBackground')"
                  >
                    {{ isLayerExpanded('eraBackground') ? '收起 ↑' : '展开 ↓' }}
                  </button>
                </div>
                <p class="story-layer-content">{{ getLayerDisplayText(post.eraBackground, 'eraBackground') }}</p>
              </div>

              <div class="story-layer" v-if="post.story">
                <div class="story-layer-header">
                  <span class="story-layer-icon">🎁</span>
                  <h3 class="story-layer-title">物件来历</h3>
                  <button
                    v-if="shouldCollapseStory(post.story)"
                    class="toggle-story-btn"
                    @click="toggleLayer('story')"
                  >
                    {{ isLayerExpanded('story') ? '收起 ↑' : '展开 ↓' }}
                  </button>
                </div>
                <p class="story-layer-content">{{ getLayerDisplayText(post.story, 'story') }}</p>
              </div>

              <div class="story-layer" v-if="post.memory">
                <div class="story-layer-header">
                  <span class="story-layer-icon">💭</span>
                  <h3 class="story-layer-title">个人记忆</h3>
                  <button
                    v-if="shouldCollapseStory(post.memory)"
                    class="toggle-story-btn"
                    @click="toggleLayer('memory')"
                  >
                    {{ isLayerExpanded('memory') ? '收起 ↑' : '展开 ↓' }}
                  </button>
                </div>
                <p class="story-layer-content">{{ getLayerDisplayText(post.memory, 'memory') }}</p>
              </div>

              <div class="story-layer" v-if="post.currentStatus">
                <div class="story-layer-header">
                  <span class="story-layer-icon">📍</span>
                  <h3 class="story-layer-title">现状</h3>
                  <button
                    v-if="shouldCollapseStory(post.currentStatus)"
                    class="toggle-story-btn"
                    @click="toggleLayer('currentStatus')"
                  >
                    {{ isLayerExpanded('currentStatus') ? '收起 ↑' : '展开 ↓' }}
                  </button>
                </div>
                <p class="story-layer-content">{{ getLayerDisplayText(post.currentStatus, 'currentStatus') }}</p>
              </div>

              <div class="story-layer" v-if="post.content && !post.story && !post.memory && !post.eraBackground && !post.currentStatus">
                <div class="story-layer-header">
                  <span class="story-layer-icon">📝</span>
                  <h3 class="story-layer-title">物件介绍</h3>
                  <button
                    v-if="shouldCollapseStory(post.content)"
                    class="toggle-story-btn"
                    @click="toggleLayer('content')"
                  >
                    {{ isLayerExpanded('content') ? '收起 ↑' : '展开 ↓' }}
                  </button>
                </div>
                <p class="story-layer-content">{{ getLayerDisplayText(post.content, 'content') }}</p>
              </div>
            </div>

            <div class="content-section" v-if="post.timelineEvents && post.timelineEvents.length > 0">
              <div class="section-header">
                <span class="section-icon">📅</span>
                <h3 class="section-title">故事年表</h3>
              </div>
              <div class="timeline">
                <div
                  v-for="(event, idx) in post.timelineEvents"
                  :key="event.id"
                  class="timeline-item"
                >
                  <div class="timeline-line" v-if="idx < post.timelineEvents.length - 1"></div>
                  <div :class="['timeline-dot', getEventTypeClass(event.eventType)]">
                    <span class="dot-icon">{{ getEventTypeIcon(event.eventType) }}</span>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span :class="['event-tag', getEventTagClass(event.eventType)]">
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

            <div class="content-section" v-if="post.restorationRecords && post.restorationRecords.length > 0">
              <div class="section-header">
                <span class="section-icon">🔧</span>
                <h3 class="section-title">修复记录</h3>
                <button class="add-restoration-btn" @click="openRestorationModal">
                  <span class="btn-icon">+</span> 添加修复记录
                </button>
              </div>
              <div class="restoration-timeline">
                <div
                  v-for="(record, idx) in post.restorationRecords"
                  :key="record.id"
                  class="restoration-item"
                >
                  <div class="restoration-line" v-if="idx < post.restorationRecords.length - 1"></div>
                  <div :class="['restoration-dot', getRestorationTypeClass(record.restorationType)]">
                    <span class="dot-icon">{{ getRestorationTypeIcon(record.restorationType) }}</span>
                  </div>
                  <div class="restoration-content">
                    <div class="restoration-header">
                      <span :class="['restoration-tag', getRestorationTypeClass(record.restorationType)]">
                        {{ getRestorationTypeLabel(record.restorationType, record.customType) }}
                      </span>
                      <span class="restoration-date">{{ formatRestorationDate(record.restorationDate) }}</span>
                    </div>
                    <div class="restoration-title-row">
                      <h4 class="restoration-title">{{ record.title }}</h4>
                      <button class="delete-restoration-btn" @click="deleteRestorationRecord(record.id)" title="删除记录">
                        🗑️
                      </button>
                    </div>
                    <p class="restoration-desc" v-if="record.description">{{ record.description }}</p>

                    <div class="restoration-images" v-if="record.beforeImage || record.afterImage">
                      <div class="restoration-image-wrapper" v-if="record.beforeImage">
                        <span class="image-label">修复前</span>
                        <img :src="record.beforeImage" alt="修复前" class="restoration-image" />
                      </div>
                      <div class="restoration-image-wrapper" v-if="record.afterImage">
                        <span class="image-label">修复后</span>
                        <img :src="record.afterImage" alt="修复后" class="restoration-image" />
                      </div>
                    </div>

                    <div class="restoration-meta">
                      <div class="meta-item" v-if="record.materials">
                        <span class="meta-label">🧰 材料：</span>
                        <span class="meta-value">{{ record.materials }}</span>
                      </div>
                      <div class="meta-item" v-if="record.cost">
                        <span class="meta-label">💰 费用：</span>
                        <span class="meta-value">¥{{ record.cost.toFixed(2) }}</span>
                      </div>
                      <div class="meta-item" v-if="record.restorer">
                        <span class="meta-label">👤 修复人：</span>
                        <span class="meta-value">{{ record.restorer }}</span>
                      </div>
                    </div>

                    <div class="restoration-status" v-if="record.preservationStatusBefore || record.preservationStatusAfter">
                      <div class="status-transition">
                        <span v-if="record.preservationStatusBefore" class="status-before">
                          {{ record.preservationStatusBefore }}
                        </span>
                        <span class="status-arrow" v-if="record.preservationStatusBefore && record.preservationStatusAfter">→</span>
                        <span v-if="record.preservationStatusAfter" class="status-after">
                          {{ record.preservationStatusAfter }}
                        </span>
                      </div>
                    </div>

                    <p class="restoration-notes" v-if="record.notes">
                      📝 {{ record.notes }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div class="content-section" v-if="!post.restorationRecords || post.restorationRecords.length === 0">
              <div class="section-header">
                <span class="section-icon">🔧</span>
                <h3 class="section-title">修复记录</h3>
                <button class="add-restoration-btn" @click="openRestorationModal">
                  <span class="btn-icon">+</span> 添加修复记录
                </button>
              </div>
              <div class="empty-restoration">
                <p class="empty-icon">🔧</p>
                <p class="empty-text">暂无修复记录，记录下老物件的修复历程吧~</p>
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

    <div class="modal-overlay" v-if="showRestorationModal" @click.self="closeRestorationModal">
      <div class="modal-content restoration-modal">
        <div class="modal-header">
          <h3 class="modal-title">🔧 添加修复记录</h3>
          <button class="modal-close" @click="closeRestorationModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label class="form-label">修复类型 *</label>
            <div class="restoration-type-grid">
              <div
                v-for="type in restorationTypes"
                :key="type.value"
                :class="['type-option', { active: restorationForm.restorationType === type.value }]"
                @click="restorationForm.restorationType = type.value"
              >
                <span class="type-icon">{{ type.icon }}</span>
                <span class="type-label">{{ type.label }}</span>
              </div>
            </div>
          </div>

          <div class="form-row" v-if="restorationForm.restorationType === 'CUSTOM'">
            <label class="form-label">自定义类型 *</label>
            <input
              v-model="restorationForm.customType"
              type="text"
              class="form-input"
              placeholder="请输入自定义修复类型"
            />
          </div>

          <div class="form-row">
            <label class="form-label">修复日期 *</label>
            <input
              v-model="restorationForm.restorationDate"
              type="date"
              class="form-input"
            />
          </div>

          <div class="form-row">
            <label class="form-label">记录标题 *</label>
            <input
              v-model="restorationForm.title"
              type="text"
              class="form-input"
              placeholder="简要描述这次修复工作"
            />
          </div>

          <div class="form-row">
            <label class="form-label">详细描述</label>
            <textarea
              v-model="restorationForm.description"
              class="form-textarea"
              rows="3"
              placeholder="详细描述修复过程、遇到的问题等"
            ></textarea>
          </div>

          <div class="form-grid">
            <div class="form-row">
              <label class="form-label">修复前图片URL</label>
              <input
                v-model="restorationForm.beforeImage"
                type="text"
                class="form-input"
                placeholder="https://..."
              />
            </div>
            <div class="form-row">
              <label class="form-label">修复后图片URL</label>
              <input
                v-model="restorationForm.afterImage"
                type="text"
                class="form-input"
                placeholder="https://..."
              />
            </div>
          </div>

          <div class="form-row">
            <label class="form-label">使用材料</label>
            <input
              v-model="restorationForm.materials"
              type="text"
              class="form-input"
              placeholder="例如：WD-40除锈剂、细砂纸、防锈油"
            />
          </div>

          <div class="form-grid">
            <div class="form-row">
              <label class="form-label">修复费用 (元)</label>
              <input
                v-model="restorationForm.cost"
                type="number"
                step="0.01"
                min="0"
                class="form-input"
                placeholder="0.00"
              />
            </div>
            <div class="form-row">
              <label class="form-label">修复人</label>
              <input
                v-model="restorationForm.restorer"
                type="text"
                class="form-input"
                placeholder="例如：自己动手 / 张师傅"
              />
            </div>
          </div>

          <div class="form-grid">
            <div class="form-row">
              <label class="form-label">修复前状态</label>
              <input
                v-model="restorationForm.preservationStatusBefore"
                type="text"
                class="form-input"
                placeholder="例如：多处锈迹、漆面脱落"
              />
            </div>
            <div class="form-row">
              <label class="form-label">修复后状态</label>
              <input
                v-model="restorationForm.preservationStatusAfter"
                type="text"
                class="form-input"
                placeholder="例如：锈迹清除、表面光滑"
              />
            </div>
          </div>

          <div class="form-row">
            <label class="form-label">备注</label>
            <textarea
              v-model="restorationForm.notes"
              class="form-textarea"
              rows="2"
              placeholder="修复心得、注意事项等"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeRestorationModal">取消</button>
          <button class="btn btn-primary" @click="submitRestorationRecord" :disabled="!restorationForm.title.trim() || !restorationForm.restorationDate">
            保存记录
          </button>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { postsAPI, commentsAPI, favoritesAPI, restorationAPI } from '../api'
import { getSessionId } from '../utils/session'
import { displayItemName, buildDetailSummary, shouldCollapseStory, truncateForDetailSummary, truncateForChip } from '../utils/textCleaner'
import { 
  detectImageOrientationFromUrl, 
  detectImageOrientation,
  getCardImageClass, 
  ImageOrientation, 
  getLayoutForGrid,
  getImageUrl,
  normalizeImageList
} from '../utils/imageLayout'
import { getCategoryClass, getCategoryStyleVars } from '../icons/categoryUtils'
import { normalizeEraName, getEraClass } from '../utils/eraUtils'
import CategoryIcon from '../components/CategoryIcon.vue'

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
const activeImageIndex = ref(0)
const isNameplateExpanded = ref(false)
const isMobile = ref(false)
const expandedLayers = ref({})
const loading = ref(true)
const notFound = ref(false)
const error = ref(false)
const errorMessage = ref('')
const showRestorationModal = ref(false)
const restorationRecords = ref([])
const restorationForm = ref({
  restorationType: 'CLEANING',
  customType: '',
  restorationDate: '',
  title: '',
  description: '',
  beforeImage: '',
  afterImage: '',
  materials: '',
  cost: '',
  restorer: '',
  notes: '',
  preservationStatusBefore: '',
  preservationStatusAfter: ''
})
const restorationTypes = [
  { value: 'CLEANING', label: '清洁', icon: '🧹' },
  { value: 'REPLACEMENT', label: '补件', icon: '🔧' },
  { value: 'RUST_REMOVAL', label: '除锈', icon: '✨' },
  { value: 'RENOVATION', label: '翻新', icon: '🆕' },
  { value: 'PAINTING', label: '上漆', icon: '🎨' },
  { value: 'REPAIR', label: '维修', icon: '🔨' },
  { value: 'MAINTENANCE', label: '保养', icon: '💧' },
  { value: 'POLISHING', label: '抛光', icon: '💎' },
  { value: 'WOOD_TREATMENT', label: '木艺处理', icon: '🪵' },
  { value: 'ELECTRONIC_REPAIR', label: '电子维修', icon: '⚡' },
  { value: 'CUSTOM', label: '自定义', icon: '📝' }
]

const toggleLayer = (key) => {
  expandedLayers.value[key] = !expandedLayers.value[key]
}

const isLayerExpanded = (key) => {
  return !!expandedLayers.value[key]
}

const getLayerDisplayText = (text, key) => {
  if (!text) return ''
  if (!shouldCollapseStory(text) || isLayerExpanded(key)) {
    return text
  }
  return truncateForDetailSummary(text, 400)
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const effectiveNameplateExpanded = computed(() => {
  return !isMobile.value || isNameplateExpanded.value
})

const toggleNameplate = () => {
  if (isMobile.value) {
    isNameplateExpanded.value = !isNameplateExpanded.value
  }
}

const resetState = () => {
  post.value = null
  comments.value = []
  newComment.value = ''
  commentAuthor.value = ''
  isFavorited.value = false
  imageOrientations.value = {}
  imageLayouts.value = []
  activeImageIndex.value = 0
  isNameplateExpanded.value = false
  loading.value = true
  notFound.value = false
  error.value = false
  errorMessage.value = ''
  restorationRecords.value = []
}

const getRestorationTypeIcon = (type) => {
  const icons = {
    CLEANING: '🧹',
    REPLACEMENT: '🔧',
    RUST_REMOVAL: '✨',
    RENOVATION: '🆕',
    PAINTING: '🎨',
    REPAIR: '🔨',
    MAINTENANCE: '💧',
    POLISHING: '💎',
    WOOD_TREATMENT: '🪵',
    ELECTRONIC_REPAIR: '⚡',
    CUSTOM: '📝'
  }
  return icons[type] || '🔧'
}

const getRestorationTypeLabel = (type, customType) => {
  if (type === 'CUSTOM' && customType) {
    return customType
  }
  const labels = {
    CLEANING: '清洁',
    REPLACEMENT: '补件',
    RUST_REMOVAL: '除锈',
    RENOVATION: '翻新',
    PAINTING: '上漆',
    REPAIR: '维修',
    MAINTENANCE: '保养',
    POLISHING: '抛光',
    WOOD_TREATMENT: '木艺处理',
    ELECTRONIC_REPAIR: '电子维修',
    CUSTOM: '自定义'
  }
  return labels[type] || '修复'
}

const getRestorationTypeClass = (type) => {
  const classes = {
    CLEANING: 'restoration-cleaning',
    REPLACEMENT: 'restoration-replacement',
    RUST_REMOVAL: 'restoration-rust',
    RENOVATION: 'restoration-renovation',
    PAINTING: 'restoration-painting',
    REPAIR: 'restoration-repair',
    MAINTENANCE: 'restoration-maintenance',
    POLISHING: 'restoration-polishing',
    WOOD_TREATMENT: 'restoration-wood',
    ELECTRONIC_REPAIR: 'restoration-electronic',
    CUSTOM: 'restoration-custom'
  }
  return classes[type] || 'restoration-default'
}

const formatRestorationDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const openRestorationModal = () => {
  restorationForm.value = {
    restorationType: 'CLEANING',
    customType: '',
    restorationDate: new Date().toISOString().split('T')[0],
    title: '',
    description: '',
    beforeImage: '',
    afterImage: '',
    materials: '',
    cost: '',
    restorer: '',
    notes: '',
    preservationStatusBefore: '',
    preservationStatusAfter: ''
  }
  showRestorationModal.value = true
}

const closeRestorationModal = () => {
  showRestorationModal.value = false
}

const submitRestorationRecord = async () => {
  if (!restorationForm.value.title.trim()) {
    alert('请输入修复记录标题')
    return
  }
  if (!restorationForm.value.restorationDate) {
    alert('请选择修复日期')
    return
  }

  try {
    const formData = {
      postId: postId.value,
      restorationType: restorationForm.value.restorationType,
      customType: restorationForm.value.restorationType === 'CUSTOM' ? restorationForm.value.customType : null,
      restorationDate: restorationForm.value.restorationDate,
      title: restorationForm.value.title.trim(),
      description: restorationForm.value.description.trim() || null,
      beforeImage: restorationForm.value.beforeImage.trim() || null,
      afterImage: restorationForm.value.afterImage.trim() || null,
      materials: restorationForm.value.materials.trim() || null,
      cost: restorationForm.value.cost ? parseFloat(restorationForm.value.cost) : null,
      restorer: restorationForm.value.restorer.trim() || null,
      notes: restorationForm.value.notes.trim() || null,
      preservationStatusBefore: restorationForm.value.preservationStatusBefore.trim() || null,
      preservationStatusAfter: restorationForm.value.preservationStatusAfter.trim() || null,
      sortOrder: restorationRecords.value.length
    }

    await restorationAPI.create(formData)
    closeRestorationModal()
    loadRestorationRecords()
    loadPost()
  } catch (e) {
    console.error('提交修复记录失败', e)
    alert('提交失败，请稍后重试')
  }
}

const deleteRestorationRecord = async (id) => {
  if (!confirm('确定要删除这条修复记录吗？')) {
    return
  }
  try {
    await restorationAPI.delete(id)
    loadRestorationRecords()
    loadPost()
  } catch (e) {
    console.error('删除修复记录失败', e)
    alert('删除失败，请稍后重试')
  }
}

const loadRestorationRecords = async () => {
  try {
    const res = await restorationAPI.getByPost(postId.value)
    restorationRecords.value = res.data || []
  } catch (e) {
    console.error('加载修复记录失败', e)
  }
}

watch(postId, () => {
  resetState()
  loadPost()
  loadComments()
  checkFavorite()
  loadRestorationRecords()
})

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadPost()
  loadComments()
  checkFavorite()
  loadRestorationRecords()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})

const mainImage = computed(() => {
  if (!post.value || !post.value.images || post.value.images.length === 0) return null
  const normalized = normalizeImageList(post.value.images)
  const mainImg = normalized.find(img => img.isMain) || normalized[activeImageIndex.value] || normalized[0]
  return mainImg
})

const detectDetailImagesOrientation = async (images) => {
  if (!images || images.length === 0) return
  const normalizedImages = normalizeImageList(images)
  imageLayouts.value = getLayoutForGrid(normalizedImages.length)
  for (let i = 0; i < normalizedImages.length; i++) {
    const img = normalizedImages[i]
    const imgUrl = img.url
    if (imgUrl && !imageOrientations.value[imgUrl]) {
      if (img.width && img.height) {
        imageOrientations.value[imgUrl] = detectImageOrientation(img.width, img.height)
      } else {
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
}

const getImgOrientation = (img) => {
  const imgUrl = getImageUrl(img)
  return imageOrientations.value[imgUrl] || ImageOrientation.SQUARE
}

const safeDisplayItemName = (name) => displayItemName(name)

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

const getEventTypeClass = (type) => {
  if (!type) return 'event-default'
  const validTypes = ['ACQUISITION', 'USAGE', 'IDLE', 'DISPOSAL']
  const upperType = type.toUpperCase()
  if (validTypes.includes(upperType)) {
    return `event-${upperType.toLowerCase()}`
  }
  return 'event-default'
}

const getEventTagClass = (type) => {
  if (!type) return 'tag-default'
  const validTypes = ['ACQUISITION', 'USAGE', 'IDLE', 'DISPOSAL']
  const upperType = type.toUpperCase()
  if (validTypes.includes(upperType)) {
    return `tag-${upperType.toLowerCase()}`
  }
  return 'tag-default'
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

const getSourceTypeStyle = (post) => {
  if (!post || !post.sourceTypeName) {
    return {
      background: 'linear-gradient(135deg, #f5f5f5, #e0e0e0)',
      color: '#666',
      border: '1px solid #d9d9d9'
    }
  }
  const typeColors = {
    '家庭传承': { bg: 'linear-gradient(135deg, #fff3e0, #ffe0b2)', color: '#e65100', border: '1px solid rgba(255, 111, 0, 0.3)' },
    '旧货市场': { bg: 'linear-gradient(135deg, #fbe9e7, #ffccbc)', color: '#bf360c', border: '1px solid rgba(191, 54, 12, 0.3)' },
    '单位留存': { bg: 'linear-gradient(135deg, #e3f2fd, #bbdefb)', color: '#0d47a1', border: '1px solid rgba(13, 71, 161, 0.3)' },
    '童年用品': { bg: 'linear-gradient(135deg, #fce4ec, #f8bbd0)', color: '#ad1457', border: '1px solid rgba(173, 20, 87, 0.3)' }
  }
  return typeColors[post.sourceTypeName] || {
    background: 'linear-gradient(135deg, #f5f5f5, #e0e0e0)',
    color: '#666',
    border: '1px solid #d9d9d9'
  }
}

const loadPost = async () => {
  try {
    loading.value = true
    notFound.value = false
    error.value = false
    errorMessage.value = ''
    
    const res = await postsAPI.getDetail(postId.value)
    let postData = res.data
    if (postData && postData.eraName) {
      postData.eraName = normalizeEraName(postData.eraName)
    }
    if (postData && postData.timelineEvents && Array.isArray(postData.timelineEvents)) {
      postData.timelineEvents = postData.timelineEvents.filter(event => event && event.id).map(event => ({
        ...event,
        eventType: event.eventType || 'USAGE'
      }))
    }
    post.value = postData
    nextTick(() => {
      if (post.value && post.value.images) {
        detectDetailImagesOrientation(post.value.images)
      }
    })
  } catch (e) {
    console.error('加载帖子失败', e)
    const status = e.response?.status
    const message = e.response?.data?.message
    
    if (status === 404) {
      notFound.value = true
      errorMessage.value = message || '该档案不存在或已被删除'
    } else {
      error.value = true
      errorMessage.value = message || '加载回忆时出现问题，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

const retryLoad = () => {
  resetState()
  loadPost()
  loadComments()
  checkFavorite()
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
</script>

<style scoped>
.loading-state,
.not-found-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f5e6d3;
  border-top-color: #d4a574;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
  color: #8b6914;
  margin: 0;
}

.not-found-icon,
.error-icon {
  font-size: 72px;
  margin-bottom: 20px;
}

.not-found-title,
.error-title {
  font-size: 28px;
  font-weight: 700;
  color: #5d4e37;
  margin: 0 0 12px 0;
}

.not-found-message,
.error-message {
  font-size: 16px;
  color: #666;
  margin: 0 0 30px 0;
  max-width: 500px;
  line-height: 1.6;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  justify-content: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #d4a574 0%, #c19660 100%);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(212, 165, 116, 0.4);
}

.btn-secondary {
  background: #fff;
  color: #8b6914;
  border: 2px solid #d4a574;
}

.btn-secondary:hover {
  background: #fdf6e3;
  transform: translateY(-2px);
}

.btn-icon {
  font-size: 16px;
}

.not-found-id {
  font-size: 14px;
  color: #999;
  padding: 10px 20px;
  background: #f5f5f5;
  border-radius: 20px;
}

.id-highlight {
  font-weight: 600;
  color: #8b6914;
  font-family: 'Courier New', monospace;
}

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

.post-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.post-title {
  font-size: 28px;
  font-weight: 600;
  color: #5d4e37;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.post-meta .author,
.post-meta .date {
  font-size: 13px;
  color: #999;
}

.split-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 32px;
  margin-bottom: 24px;
}

.split-left {
  position: sticky;
  top: 20px;
  align-self: start;
}

.split-right {
  min-width: 0;
}

.main-image-area {
  position: relative;
}

.main-image-wrapper {
  position: relative;
  border-radius: 8px;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
}

.main-image-wrapper::before {
  display: none;
}

.main-image-wrapper .frame-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #faf7f2;
  border-radius: 2px;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 0;
}

.main-image-placeholder {
  aspect-ratio: 4 / 3;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f0eb 0%, #e8d5b8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  box-shadow: 0 8px 32px rgba(93, 78, 55, 0.15);
}

.placeholder-icon {
  font-size: 48px;
  opacity: 0.5;
}

.placeholder-text {
  font-size: 14px;
  color: #999;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.thumbnail-item {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.3s;
  padding: 3px;
}

.thumbnail-item::before {
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
}

.thumbnail-item:hover {
  opacity: 1;
  transform: translateY(-2px);
}

.thumbnail-item.active {
  opacity: 1;
  box-shadow: 0 0 0 2px currentColor;
}

.thumbnail-item .frame-inner {
  width: 100%;
  height: 100%;
  border-radius: 2px;
  overflow: hidden;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nameplate-card {
  position: relative;
  background: linear-gradient(135deg, #fefcf8 0%, #faf5ed 100%);
  border: 1px solid #e8d5b8;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 28px;
  box-shadow: 
    0 4px 20px rgba(93, 78, 55, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.nameplate-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px double #d4a574;
}

.nameplate-icon {
  font-size: 22px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #d4a574 0%, #b8956a 100%);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(212, 165, 116, 0.3);
}

.nameplate-title {
  font-size: 18px;
  font-weight: 700;
  color: #6b4f0f;
  margin: 0;
  letter-spacing: 2px;
}

.nameplate-decoration-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, #d4a574 0%, transparent 100%);
  margin-left: 8px;
}

.nameplate-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.nameplate-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.nameplate-label {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 90px;
  font-size: 13px;
  font-weight: 600;
  color: #8b6914;
  padding: 6px 12px;
  background: linear-gradient(135deg, #fff7e6 0%, #ffe4b8 100%);
  border-radius: 6px;
  border: 1px solid #e8d5b8;
  white-space: nowrap;
}

.label-icon {
  font-size: 14px;
}

.nameplate-value {
  flex: 1;
  font-size: 15px;
  color: #5d4e37;
  padding: 6px 0;
  font-weight: 500;
  line-height: 1.6;
}

.nameplate-item-name {
  font-size: 18px;
  font-weight: 700;
  color: #5d4037;
  letter-spacing: 1px;
}

.nameplate-emotion-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
}

.era-badge {
  display: inline-block;
  padding: 4px 14px;
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
  color: #fff;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.25);
}

.category-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preservation-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
}

.preservation-excellent {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.preservation-good {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #096dd9;
  border: 1px solid #91d5ff;
}

.preservation-worn {
  background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
  color: #d48806;
  border: 1px solid #ffe58f;
}

.preservation-damaged {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #cf1322;
  border: 1px solid #ffa39e;
}

.nameplate-source-archive {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nameplate-source-badge {
  align-self: flex-start;
  font-size: 13px;
  padding: 5px 14px;
}

.nameplate-source-detail {
  font-size: 14px;
  color: #5d4e37;
  line-height: 1.6;
}

.preservation-default {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  color: #595959;
  border: 1px solid #d9d9d9;
}

.status-icon {
  font-size: 13px;
}

.nameplate-footer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.decoration-corner {
  position: absolute;
  width: 16px;
  height: 16px;
  border: 2px solid #d4a574;
}

.decoration-corner.tl {
  top: 8px;
  left: 8px;
  border-right: none;
  border-bottom: none;
  border-top-left-radius: 4px;
}

.decoration-corner.tr {
  top: 8px;
  right: 8px;
  border-left: none;
  border-bottom: none;
  border-top-right-radius: 4px;
}

.decoration-corner.bl {
  bottom: 8px;
  left: 8px;
  border-right: none;
  border-top: none;
  border-bottom-left-radius: 4px;
}

.decoration-corner.br {
  bottom: 8px;
  right: 8px;
  border-left: none;
  border-top: none;
  border-bottom-right-radius: 4px;
}

.story-content {
  min-width: 0;
}

.story-content-top.mobile-first {
  display: none;
}

.story-summary-desktop {
  display: block;
}

.nameplate-toggle-icon {
  font-size: 12px;
  color: #8b6914;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.nameplate-toggle-icon.expanded {
  transform: rotate(180deg);
}

.nameplate-header {
  cursor: pointer;
  user-select: none;
}

.nameplate-compact-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 12px 0 4px 0;
}

.nameplate-compact-name {
  font-size: 16px;
  font-weight: 700;
  color: #5d4037;
  letter-spacing: 1px;
}

.nameplate-body-hidden {
  max-height: 0;
  overflow: hidden;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
  transition: all 0.3s ease;
}

.nameplate-body:not(.nameplate-body-hidden) {
  max-height: 2000px;
  opacity: 1;
  transition: all 0.3s ease;
}

.nameplate-card.nameplate-collapsed {
  padding-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.section-icon {
  font-size: 18px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5e6d3 0%, #e8d5b8 100%);
  border-radius: 8px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #6b4f0f;
  margin: 0;
}

.content-section {
  margin-bottom: 28px;
}

.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: #6b4f0f;
  padding: 18px 22px;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  border-left: 4px solid #faad14;
  border-radius: 0 10px 10px 0;
  font-style: italic;
  margin: 0;
}

.summary-structured {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  border-left: 4px solid #faad14;
  border-radius: 0 10px 10px 0;
}

.summary-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.summary-label {
  font-size: 14px;
  font-weight: 600;
  color: #8b6914;
  white-space: nowrap;
  flex-shrink: 0;
  min-width: 70px;
}

.summary-value {
  font-size: 14px;
  color: #5d4e37;
  line-height: 1.6;
}

.summary-source-value {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
  padding: 4px 14px;
  border-radius: 16px;
  border: 1px solid rgba(255, 111, 0, 0.2);
}

.summary-scene-value {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #0d47a1;
  padding: 4px 14px;
  border-radius: 16px;
  border: 1px solid rgba(25, 118, 210, 0.2);
}

.source-archive-row {
  flex-direction: column;
  gap: 8px;
}

.source-archive-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.source-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.source-type-icon {
  font-size: 16px;
}

.source-type-name {
  letter-spacing: 1px;
}

.source-detail-text {
  font-size: 13px;
  color: #6b5b47;
  line-height: 1.6;
  padding-left: 4px;
}

.summary-emotion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.emotion-tag {
  padding: 4px 14px;
  border-radius: 16px;
  font-size: 13px;
  background: linear-gradient(135deg, #fce4ec, #f8bbd0);
  color: #ad1457;
  border: 1px solid rgba(173, 20, 87, 0.15);
  font-weight: 500;
}

.story-layers {
  display: flex;
  flex-direction: column;
  gap: 18px;
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
  box-shadow: 0 4px 16px rgba(139, 105, 20, 0.12);
}

.story-layer:nth-child(1) {
  border-left-color: #722ed1;
  background: linear-gradient(135deg, #f9f0ff 0%, #faf5ff 100%);
}

.story-layer:nth-child(2) {
  border-left-color: #52c41a;
  background: linear-gradient(135deg, #f6ffed 0%, #fcffe6 100%);
}

.story-layer:nth-child(3) {
  border-left-color: #1890ff;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
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
  font-size: 18px;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
}

.story-layer-title {
  font-size: 17px;
  font-weight: 600;
  color: #5d4e37;
  margin: 0;
}

.toggle-story-btn {
  margin-left: auto;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(139, 105, 20, 0.1);
  color: #8b6914;
  border: 1px solid rgba(139, 105, 20, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.toggle-story-btn:hover {
  background: rgba(139, 105, 20, 0.2);
  transform: translateY(-1px);
}

.story-layer-content {
  font-size: 15px;
  line-height: 1.9;
  color: #5d4e37;
  white-space: pre-wrap;
  margin: 0;
  padding-left: 44px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  margin-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #999;
}

.favorite-btn {
  padding: 10px 28px;
  border-radius: 20px;
  font-size: 14px;
  background: #f5e6d3;
  color: #8b6914;
  transition: all 0.3s;
  font-weight: 500;
}

.favorite-btn:hover {
  background: #e8d5b8;
}

.favorite-btn.favorited {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
}

.comments-section {
  padding: 28px;
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
  font-weight: 600;
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
  margin: 0;
}

.empty-comments {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.timeline {
  position: relative;
  padding-left: 44px;
}

.timeline-item {
  position: relative;
  padding-bottom: 28px;
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
  left: -44px;
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

.timeline-dot.event-default {
  background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
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
  font-weight: 600;
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

.tag-default {
  background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
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

@media (max-width: 1024px) {
  .split-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .split-left {
    position: static;
  }

  .story-layer-content {
    padding-left: 0;
  }
}

@media (max-width: 768px) {
  .detail-card {
    padding: 20px;
  }

  .post-title {
    font-size: 22px;
  }

  .story-content-top.mobile-first {
    display: block;
    margin-bottom: 20px;
  }

  .story-summary-desktop {
    display: none;
  }

  .nameplate-card {
    padding: 18px;
  }

  .nameplate-card.nameplate-collapsed {
    padding: 14px 18px;
  }

  .nameplate-header {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }

  .nameplate-card:not(.nameplate-collapsed) .nameplate-header {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 2px double #d4a574;
  }

  .nameplate-title {
    font-size: 16px;
  }

  .nameplate-compact-summary {
    padding: 10px 0 2px 0;
  }

  .nameplate-compact-name {
    font-size: 15px;
  }

  .nameplate-row {
    flex-direction: column;
    gap: 6px;
  }

  .nameplate-label {
    min-width: auto;
    align-self: flex-start;
  }

  .nameplate-item-name {
    font-size: 16px;
  }

  .thumbnail-item {
    width: 52px;
    height: 52px;
  }

  .story-layer {
    padding: 16px;
  }

  .timeline {
    padding-left: 36px;
  }

  .timeline-dot {
    left: -36px;
    width: 28px;
    height: 28px;
  }

  .timeline-line {
    left: 13px;
  }

  .post-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .stats {
    gap: 16px;
  }

  .comments-section {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .form-actions input {
    max-width: none;
  }
}

.add-restoration-btn {
  margin-left: auto;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: #fff;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s;
}

.add-restoration-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.add-restoration-btn .btn-icon {
  font-size: 16px;
  font-weight: bold;
}

.restoration-timeline {
  position: relative;
  padding-left: 44px;
}

.restoration-item {
  position: relative;
  padding-bottom: 28px;
}

.restoration-item:last-child {
  padding-bottom: 0;
}

.restoration-line {
  position: absolute;
  left: 15px;
  top: 40px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #52c41a 0%, #95de64 100%);
}

.restoration-dot {
  position: absolute;
  left: -44px;
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

.restoration-dot.restoration-cleaning {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.restoration-dot.restoration-replacement {
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
}

.restoration-dot.restoration-rust {
  background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
}

.restoration-dot.restoration-renovation {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.restoration-dot.restoration-painting {
  background: linear-gradient(135deg, #eb2f96 0%, #c41d7f 100%);
}

.restoration-dot.restoration-repair {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.restoration-dot.restoration-maintenance {
  background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
}

.restoration-dot.restoration-polishing {
  background: linear-gradient(135deg, #2f54eb 0%, #1d39c4 100%);
}

.restoration-dot.restoration-wood {
  background: linear-gradient(135deg, #a0d911 0%, #7cb305 100%);
}

.restoration-dot.restoration-electronic {
  background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);
}

.restoration-dot.restoration-custom {
  background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
}

.restoration-dot.restoration-default {
  background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
}

.restoration-content {
  background: linear-gradient(135deg, #f6ffed 0%, #fcffe6 100%);
  border-radius: 12px;
  padding: 16px 20px;
  border-left: 3px solid #52c41a;
  transition: all 0.3s;
}

.restoration-content:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.1);
}

.restoration-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.restoration-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.restoration-tag.restoration-cleaning {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.restoration-tag.restoration-replacement {
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
}

.restoration-tag.restoration-rust {
  background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
}

.restoration-tag.restoration-renovation {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.restoration-tag.restoration-painting {
  background: linear-gradient(135deg, #eb2f96 0%, #c41d7f 100%);
}

.restoration-tag.restoration-repair {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.restoration-tag.restoration-maintenance {
  background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
}

.restoration-tag.restoration-polishing {
  background: linear-gradient(135deg, #2f54eb 0%, #1d39c4 100%);
}

.restoration-tag.restoration-wood {
  background: linear-gradient(135deg, #a0d911 0%, #7cb305 100%);
}

.restoration-tag.restoration-electronic {
  background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);
}

.restoration-tag.restoration-custom {
  background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
}

.restoration-tag.restoration-default {
  background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
}

.restoration-date {
  font-size: 13px;
  color: #999;
}

.restoration-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.restoration-title {
  font-size: 16px;
  font-weight: 600;
  color: #5d4e37;
  margin: 0;
  flex: 1;
}

.delete-restoration-btn {
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  opacity: 0.6;
  transition: all 0.3s;
  border-radius: 4px;
}

.delete-restoration-btn:hover {
  opacity: 1;
  background: rgba(245, 34, 45, 0.1);
}

.restoration-desc {
  font-size: 14px;
  line-height: 1.7;
  color: #6b5b47;
  margin-bottom: 12px;
  white-space: pre-wrap;
}

.restoration-images {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.restoration-image-wrapper {
  flex: 1;
  min-width: 150px;
  text-align: center;
}

.image-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  font-weight: 500;
}

.restoration-image {
  width: 100%;
  max-width: 200px;
  border-radius: 8px;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.restoration-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-label {
  color: #8b6914;
  font-weight: 500;
}

.meta-value {
  color: #5d4e37;
}

.restoration-status {
  margin-bottom: 12px;
}

.status-transition {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  border-radius: 20px;
  font-size: 13px;
}

.status-before {
  color: #d48806;
  font-weight: 500;
}

.status-arrow {
  color: #52c41a;
  font-weight: bold;
  font-size: 16px;
}

.status-after {
  color: #389e0d;
  font-weight: 500;
}

.restoration-notes {
  font-size: 13px;
  color: #8b6914;
  font-style: italic;
  margin: 0;
  padding: 8px 12px;
  background: rgba(139, 105, 20, 0.05);
  border-radius: 6px;
  border-left: 3px solid #faad14;
}

.empty-restoration {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #f6ffed 0%, #fcffe6 100%);
  border-radius: 12px;
}

.empty-restoration .empty-icon {
  font-size: 48px;
  margin: 0 0 12px 0;
  opacity: 0.5;
}

.empty-restoration .empty-text {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.restoration-modal {
  max-width: 700px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #5d4e37;
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: all 0.3s;
}

.modal-close:hover {
  background: #e0e0e0;
  color: #666;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.form-row {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #5d4e37;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e0d5c5;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  background: #fff;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #d4a574;
  box-shadow: 0 0 0 3px rgba(212, 165, 116, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e0d5c5;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  transition: all 0.3s;
  background: #fff;
  font-family: inherit;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #d4a574;
  box-shadow: 0 0 0 3px rgba(212, 165, 116, 0.1);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.restoration-type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 8px;
}

.type-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border: 2px solid #e0d5c5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.type-option:hover {
  border-color: #d4a574;
  background: #fdf6e3;
}

.type-option.active {
  border-color: #52c41a;
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
}

.type-option .type-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.type-option .type-label {
  font-size: 12px;
  color: #5d4e37;
  font-weight: 500;
}

@media (max-width: 768px) {
  .restoration-timeline {
    padding-left: 36px;
  }

  .restoration-dot {
    left: -36px;
    width: 28px;
    height: 28px;
  }

  .restoration-line {
    left: 13px;
  }

  .restoration-images {
    flex-direction: column;
  }

  .restoration-image-wrapper {
    min-width: 100%;
  }

  .restoration-image {
    max-width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .restoration-type-grid {
    grid-template-columns: repeat(auto-fill, minmax(75px, 1fr));
  }

  .modal-content {
    max-height: 95vh;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
