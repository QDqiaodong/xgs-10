<template>
  <span
    :class="['category-icon-wrapper', sizeClass, { 'with-bg': showBg, 'animated': animated }]"
    :style="wrapperStyle"
    :title="config.name"
  >
    <svg
      v-if="mode === 'svg'"
      :viewBox="config.svg.viewBox"
      class="category-svg"
      :style="svgStyle"
    >
      <template v-for="(path, idx) in config.svg.paths" :key="idx">
        <path
          v-if="path.d"
          :d="path.d"
          :fill="path.fill"
          :fill-opacity="path.opacity"
          :stroke="path.stroke"
          :stroke-width="path.strokeWidth"
          :stroke-linecap="path.strokeLinecap"
        />
        <circle
          v-else-if="path.cx !== undefined"
          :cx="path.cx"
          :cy="path.cy"
          :r="path.r"
          :fill="path.fill"
          :fill-opacity="path.opacity"
        />
        <rect
          v-else-if="path.x !== undefined"
          :x="path.x"
          :y="path.y"
          :width="path.width"
          :height="path.height"
          :fill="path.fill"
          :fill-opacity="path.opacity"
        />
      </template>
    </svg>
    <span v-else class="category-emoji" :style="emojiStyle">
      {{ config.emoji }}
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { getCategoryConfig } from '../icons/categories'

const props = defineProps({
  category: {
    type: [String, Object],
    required: true
  },
  mode: {
    type: String,
    default: 'emoji',
    validator: (v) => ['emoji', 'svg'].includes(v)
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['xs', 'sm', 'md', 'lg', 'xl', '2xl'].includes(v)
  },
  showBg: {
    type: Boolean,
    default: false
  },
  animated: {
    type: Boolean,
    default: false
  },
  color: {
    type: String,
    default: ''
  }
})

const config = computed(() => {
  if (typeof props.category === 'object') {
    if (props.category.name) return getCategoryConfig(props.category.name)
    return getCategoryConfig(props.category)
  }
  return getCategoryConfig(props.category)
})

const sizeMap = {
  xs: { wrapper: '16px', icon: '12px' },
  sm: { wrapper: '20px', icon: '16px' },
  md: { wrapper: '28px', icon: '20px' },
  lg: { wrapper: '36px', icon: '26px' },
  xl: { wrapper: '48px', icon: '34px' },
  '2xl': { wrapper: '64px', icon: '46px' }
}

const sizeClass = computed(() => `icon-${props.size}`)

const currentColor = computed(() => props.color || config.value.primaryColor)

const wrapperStyle = computed(() => {
  const s = sizeMap[props.size]
  const style = {
    width: s.wrapper,
    height: s.wrapper
  }
  if (props.showBg) {
    style.background = config.value.bgGradient
    style.borderColor = config.value.borderColor
  }
  return style
})

const svgStyle = computed(() => ({
  color: currentColor.value
}))

const emojiStyle = computed(() => {
  const s = sizeMap[props.size]
  return {
    fontSize: s.icon,
    lineHeight: s.wrapper
  }
})
</script>

<style scoped>
.category-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.category-icon-wrapper.with-bg {
  border-width: 1.5px;
  border-style: solid;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.category-icon-wrapper.animated:hover {
  transform: scale(1.1) rotate(-3deg);
}

.category-icon-wrapper.icon-xs { border-radius: 4px; }
.category-icon-wrapper.icon-sm { border-radius: 6px; }
.category-icon-wrapper.icon-lg { border-radius: 10px; }
.category-icon-wrapper.icon-xl { border-radius: 12px; }
.category-icon-wrapper.icon-2xl { border-radius: 16px; }

.category-svg {
  width: 80%;
  height: 80%;
  display: block;
}

.category-emoji {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
</style>
