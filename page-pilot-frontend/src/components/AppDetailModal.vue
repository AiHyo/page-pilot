<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { deleteApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

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
  loading: false
})

const emit = defineEmits<Emits>()

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 是否为应用所有者
const isOwner = computed(() => {
  return props.app?.userId === loginUserStore.loginUser.id
})

// 是否为管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 是否可以操作
const canOperate = computed(() => {
  return isOwner.value || isAdmin.value
})

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false)
}

// 编辑应用
const handleEdit = () => {
  if (props.app?.id) {
    router.push(`/app/edit/${props.app.id}`)
    handleClose()
  }
}

// 删除应用
const handleDelete = async () => {
  if (!props.app?.id) return
  
  try {
    const res = await deleteApp({ id: props.app.id! })
    if (res.data.code === 0) {
      message.success('删除成功')
      emit('refresh')
      handleClose()
      // 如果当前在应用相关页面，跳转到首页
      if (router.currentRoute.value.path.includes(`/app/`)) {
        router.push('/')
      }
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    message.error('删除失败')
  }
}
</script>

<template>
  <a-modal
    :open="visible"
    title="应用详情"
    :footer="null"
    width="500px"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <div v-if="app" class="app-detail-content">
        <!-- 应用基础信息 -->
        <div class="detail-section">
          <h4 class="section-title">基础信息</h4>
          <div class="info-item">
            <span class="info-label">应用名称：</span>
            <span class="info-value">{{ app.appName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建者：</span>
            <div class="creator-info">
              <a-avatar :size="24" :src="app.user?.userAvatar">
                {{ app.user?.userName?.[0] }}
              </a-avatar>
              <span class="creator-name">{{ app.user?.userName }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间：</span>
            <span class="info-value">{{ app.createTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">生成类型：</span>
            <a-tag v-if="app.codeGenType" color="purple" class="code-type-tag">
              {{ getCodeGenTypeLabel(app.codeGenType) }}
            </a-tag>
            <span v-else class="info-value">未设置</span>
          </div>
          <div class="info-item">
            <span class="info-label">部署状态：</span>
            <a-tag v-if="app.deployKey" color="green">已部署</a-tag>
            <a-tag v-else color="orange">未部署</a-tag>
          </div>
          <div v-if="app.priority && app.priority > 0" class="info-item">
            <span class="info-label">优先级：</span>
            <a-tag v-if="app.priority === 99" color="red">精选应用</a-tag>
            <a-tag v-else color="blue">{{ app.priority }}</a-tag>
          </div>
        </div>

        <!-- 操作栏（仅本人或管理员可见） -->
        <div v-if="canOperate" class="detail-section operation-section">
          <h4 class="section-title">操作</h4>
          <div class="operation-buttons">
            <a-button type="primary" @click="handleEdit">
              修改应用
            </a-button>
            <a-popconfirm
              title="确定要删除这个应用吗？删除后将无法恢复！"
              ok-text="确定删除"
              cancel-text="取消"
              @confirm="handleDelete"
            >
              <a-button type="primary" danger>
                删除应用
              </a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<style scoped>
.app-detail-content {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  line-height: 1.5;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 500;
  color: #595959;
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: #262626;
  flex: 1;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.creator-name {
  color: #262626;
  font-weight: 500;
}

.operation-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.operation-buttons {
  display: flex;
  gap: 12px;
}

.code-type-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 4px 12px;
  font-size: 13px;
}
</style>
