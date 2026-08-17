<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { deleteApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import { formatTime } from '@/utils/formatTime'

interface Props {
  visible: boolean
  app?: API.AppVO
  loading?: boolean
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'refresh'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<Emits>()

const router = useRouter()
const loginUserStore = useLoginUserStore()

const isOwner = computed(
  () => String(props.app?.userId ?? '') === String(loginUserStore.loginUser.id ?? ''),
)
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
const canOperate = computed(() => isOwner.value || isAdmin.value)

const rows = computed(() => {
  const app = props.app
  if (!app) {
    return []
  }
  return [
    { label: '名称', value: app.appName || '未命名' },
    { label: '作者', value: app.user?.userName || '未知' },
    { label: '创建', value: formatTime(app.createTime) },
    { label: '类型', value: app.codeGenType ? getCodeGenTypeLabel(app.codeGenType) : '未设置' },
    { label: '部署', value: app.deployKey ? '已发布' : '未发布' },
    ...(app.priority === 99 ? [{ label: '标记', value: '精选' }] : []),
  ]
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleEdit = () => {
  if (props.app?.id) {
    router.push(`/app/edit/${props.app.id}`)
    handleClose()
  }
}

const handleDelete = async () => {
  if (!props.app?.id) return
  try {
    const res = await deleteApp({ id: props.app.id })
    if (res.data.code === 0) {
      message.success('已删除')
      emit('refresh')
      handleClose()
      if (router.currentRoute.value.path.includes('/app/')) {
        router.push('/')
      }
    } else {
      message.error(res.data.message || '删除失败')
    }
  } catch {
    message.error('删除失败')
  }
}
</script>

<template>
  <a-modal
    :open="visible"
    title="应用"
    :footer="null"
    width="420px"
    centered
    :body-style="{ padding: '8px 0 16px' }"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <div v-if="app" class="sheet">
        <ul class="group">
          <li v-for="row in rows" :key="row.label">
            <span>{{ row.label }}</span>
            <strong>{{ row.value }}</strong>
          </li>
        </ul>
        <div v-if="canOperate" class="ops">
          <button type="button" class="edit" @click="handleEdit">修改名称</button>
          <a-popconfirm
            title="删除后无法恢复。确定删除这个应用吗？"
            ok-text="删除"
            cancel-text="取消"
            @confirm="handleDelete"
          >
            <button type="button" class="danger">删除</button>
          </a-popconfirm>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<style scoped>
.sheet {
  padding: 0 4px;
}

.group {
  margin: 0;
  padding: 0;
  list-style: none;
  background: var(--bg);
  border-radius: var(--radius);
  overflow: hidden;
}

.group li {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--line);
  font-size: 15px;
}

.group li:last-child {
  border-bottom: 0;
}

.group span {
  color: var(--mute);
}

.group strong {
  font-weight: 510;
  text-align: right;
}

.ops {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

.ops button {
  flex: 1;
  height: 40px;
  border: 0;
  border-radius: 10px;
  font: inherit;
  font-weight: 590;
  cursor: pointer;
}

.edit {
  background: var(--fill);
  color: var(--accent);
}

.danger {
  background: rgba(255, 59, 48, 0.12);
  color: var(--danger);
}
</style>
