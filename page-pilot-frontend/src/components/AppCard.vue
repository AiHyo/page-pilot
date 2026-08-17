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

const props = withDefaults(defineProps<Props>(), {
  showFeaturedBadge: false,
  showAuthor: false,
  actions: () => [],
})

const emit = defineEmits<{
  action: [key: string, app: API.AppVO]
}>()

const isFeatured = computed(() => props.app.priority === 99)
const visibleActions = computed(() => props.actions.filter((action) => action.condition !== false))
</script>

<template>
  <article class="card" :class="{ featured: showFeaturedBadge && isFeatured }">
    <div class="cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <span v-else class="blank">暂无预览</span>
    </div>
    <div class="body">
      <div class="title-row">
        <h3>{{ app.appName }}</h3>
        <span v-if="app.codeGenType" class="kind">{{ getCodeGenTypeLabel(app.codeGenType) }}</span>
      </div>
      <p v-if="showAuthor && app.user">{{ app.user.userName || '匿名' }}</p>
      <p v-else>创建于 {{ app.createTime }}</p>
      <div v-if="visibleActions.length" class="acts">
        <button
          v-for="action in visibleActions"
          :key="action.key"
          type="button"
          @click="emit('action', action.key, app)"
        >
          {{ action.label }}
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow);
}

.cover {
  height: 148px;
  background: #e5e5ea;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.blank {
  display: grid;
  place-items: center;
  height: 100%;
  color: var(--mute);
  font-size: 13px;
}

.body {
  padding: 12px 14px 14px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.body h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 650;
  letter-spacing: -0.02em;
  line-height: 1.25;
}

.kind {
  flex: 0 0 auto;
  color: var(--mute);
  font-size: 12px;
  font-weight: 510;
}

.body p {
  margin: 4px 0 0;
  color: var(--mute);
  font-size: 13px;
}

.acts {
  display: flex;
  gap: 16px;
  margin-top: 10px;
}

.acts button {
  border: 0;
  background: none;
  padding: 0;
  color: var(--accent);
  font-size: 15px;
  font-weight: 510;
  cursor: pointer;
}

.featured .kind {
  color: var(--accent);
}
</style>
