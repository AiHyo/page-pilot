<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, updateAppByAdmin, getAppById } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import { formatTime } from '@/utils/formatTime'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.id as string
const loading = ref(false)
const submitting = ref(false)
const isAdmin = ref(false)
const app = ref<API.AppVO>()
const formData = ref({
  appName: '',
  cover: '',
  priority: 0,
})

const loadApp = async () => {
  loading.value = true
  try {
    isAdmin.value = loginUserStore.loginUser.userRole === 'admin'
    const res = isAdmin.value
      ? await getAppById({ id: appId as unknown as number })
      : await getAppVoById({ id: appId as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data
      const canEdit =
        isAdmin.value ||
        String(app.value.userId ?? '') === String(loginUserStore.loginUser.id ?? '')
      if (!canEdit) {
        message.error('无权限编辑此应用')
        router.push('/')
        return
      }
      formData.value = {
        appName: app.value.appName || '',
        cover: app.value.cover || '',
        priority: app.value.priority || 0,
      }
    } else {
      message.error(res.data.message || '获取应用失败')
      router.push('/')
    }
  } catch {
    message.error('获取应用失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  const name = formData.value.appName.trim()
  if (!name) {
    message.error('请填写应用名称')
    return
  }
  if (name.length > 80) {
    message.error('名称不能超过 80 个字')
    return
  }
  submitting.value = true
  try {
    const res = isAdmin.value
      ? await updateAppByAdmin(
          { id: appId as unknown as number },
          {
            id: appId as unknown as number,
            appName: name,
            cover: formData.value.cover,
            priority: formData.value.priority,
          },
        )
      : await updateApp(
          { id: appId as unknown as number },
          { id: appId as unknown as number, appName: name },
        )
    if (res.data.code === 0) {
      message.success('已保存')
      router.back()
    } else {
      message.error(res.data.message || '保存失败')
    }
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!loginUserStore.loginUser.id) {
    message.error('请先登录')
    router.push('/user/login')
    return
  }
  loadApp()
})
</script>

<template>
  <div class="edit">
    <header class="large-title">
      <button type="button" class="back" @click="router.back()">取消</button>
      <h1>编辑</h1>
      <button type="button" class="save" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '保存中' : '完成' }}
      </button>
    </header>

    <a-spin :spinning="loading">
      <label class="field-label">名称</label>
      <div class="group">
        <input v-model="formData.appName" maxlength="80" placeholder="应用名称" />
      </div>

      <template v-if="isAdmin">
        <label class="field-label">管理员</label>
        <div class="group">
          <label class="row">
            <span>封面</span>
            <input v-model="formData.cover" placeholder="https://" />
          </label>
          <label class="row">
            <span>优先级</span>
            <input v-model.number="formData.priority" type="number" min="0" max="999" />
          </label>
        </div>
      </template>

      <label class="field-label">只读</label>
      <ul class="group facts">
        <li>
          <span>类型</span>
          <strong>{{ app?.codeGenType ? getCodeGenTypeLabel(app.codeGenType) : '未设置' }}</strong>
        </li>
        <li>
          <span>创建</span>
          <strong>{{ formatTime(app?.createTime) }}</strong>
        </li>
        <li>
          <span>更新</span>
          <strong>{{ formatTime(app?.updateTime) }}</strong>
        </li>
      </ul>
    </a-spin>
  </div>
</template>

<style scoped>
.edit {
  max-width: 560px;
  margin: 0 auto;
  padding: 12px 20px 48px;
}

.large-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0 20px;
}

.large-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.back,
.save {
  border: 0;
  background: none;
  padding: 6px 4px;
  color: var(--accent);
  font: inherit;
  font-weight: 590;
  cursor: pointer;
}

.save:disabled {
  opacity: 0.4;
}

.field-label {
  display: block;
  margin: 18px 16px 8px;
  color: var(--mute);
  font-size: 13px;
}

.group {
  background: var(--surface);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}

.group input {
  width: 100%;
  border: 0;
  padding: 12px 14px;
  background: transparent;
  color: var(--ink);
  font: inherit;
  font-size: 17px;
  outline: none;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  border-top: 1px solid var(--line);
}

.row:first-child {
  border-top: 0;
}

.row span {
  flex: 0 0 72px;
  color: var(--mute);
  font-size: 15px;
}

.row input {
  padding-left: 0;
}

.facts {
  margin: 0;
  padding: 0;
  list-style: none;
}

.facts li {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border-top: 1px solid var(--line);
  font-size: 15px;
}

.facts li:first-child {
  border-top: 0;
}

.facts span {
  color: var(--mute);
}

.facts strong {
  font-weight: 510;
}
</style>
