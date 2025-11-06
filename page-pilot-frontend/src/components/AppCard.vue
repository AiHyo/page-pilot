<script setup lang="ts">
import { computed } from 'vue'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

interface Props {
  app: API.AppVO
  showFeaturedBadge?: boolean
  showAuthor?: boolean
  actions?: Array<{
    label: string
    key: string
    type?: 'primary' | 'default' | 'link'
    condition?: boolean
  }>
}

interface Emits {
  (e: 'action', key: string, app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  showFeaturedBadge: false,
  showAuthor: false,
  actions: () => []
})

const emit = defineEmits<Emits>()

// 是否为精选应用
const isFeatured = computed(() => props.app.priority === 99)

// 处理操作点击
const handleAction = (key: string) => {
  emit('action', key, props.app)
}

// 过滤可显示的操作
const visibleActions = computed(() => {
  return props.actions.filter(action => action.condition !== false)
})
</script>

<template>
  <a-card
    :hoverable="true"
    :class="['app-card', { 'featured-card': showFeaturedBadge && isFeatured }]"
  >
    <template #cover>
      <div class="app-cover">
        <img 
          v-if="app.cover" 
          :src="app.cover" 
          :alt="app.appName"
          class="cover-image"
        />
        <div v-else class="default-cover">
          📄
        </div>
        <!-- 生成类型标签 -->
        <div v-if="app.codeGenType" class="code-type-badge">
          {{ getCodeGenTypeLabel(app.codeGenType) }}
        </div>
      </div>
    </template>
    
    <a-card-meta>
      <template #title>
        <div v-if="showFeaturedBadge && isFeatured" class="featured-title">
          {{ app.appName }}
          <span class="featured-badge">精选</span>
        </div>
        <div v-else>{{ app.appName }}</div>
      </template>
      
      <template #description>
        <div v-if="showAuthor && app.user" class="app-author">
          <a-avatar :size="20" :src="app.user.userAvatar">
            {{ app.user.userName?.[0] }}
          </a-avatar>
          <span>{{ app.user.userName || 'NoCode 官方' }}</span>
        </div>
        <div v-else class="app-create-time">
          创建于 {{ app.createTime }}
        </div>
      </template>
    </a-card-meta>
    
    <template v-if="visibleActions.length > 0" #actions>
      <a-button 
        v-for="action in visibleActions"
        :key="action.key"
        :type="action.type || 'link'"
        size="small"
        @click="handleAction(action.key)"
      >
        {{ action.label }}
      </a-button>
    </template>
  </a-card>
</template>

<style scoped>
.app-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.app-cover {
  height: 180px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  font-size: 48px;
  color: #ccc;
}

.code-type-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(114, 46, 209, 0.9);
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.featured-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.featured-badge {
  background: #ff4d4f;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.app-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-create-time {
  color: #666;
  font-size: 14px;
}

.featured-card {
  border: 2px solid #ff4d4f;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.15);
}
</style>
