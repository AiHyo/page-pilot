<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { 
  getAppVoById, 
  updateApp, 
  updateAppByAdmin,
  getAppById 
} from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.id as string
const formRef = ref<FormInstance>()

// 页面状态
const loading = ref(false)
const submitting = ref(false)
const isAdmin = ref(false)
const canEdit = ref(false)

// 应用数据
const app = ref<API.AppVO>()
const formData = ref({
  appName: '',
  cover: '',
  priority: 0,
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称' },
    { max: 80, message: '应用名称不能超过80个字符' },
  ],
  cover: [
    { max: 255, message: '封面链接不能超过255个字符' },
  ],
  priority: [
    { type: 'number', min: 0, max: 999, message: '优先级范围为0-999' },
  ],
}

// 加载应用信息
const loadApp = async () => {
  loading.value = true
  try {
    // 检查用户权限
    isAdmin.value = loginUserStore.loginUser.userRole === 'admin'
    
    let res
    if (isAdmin.value) {
      res = await getAppById({ id: appId })
    } else {
      res = await getAppVoById({ id: appId })
    }
    
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data
      
      // 检查编辑权限
      canEdit.value = isAdmin.value || app.value.userId === loginUserStore.loginUser.id
      
      if (!canEdit.value) {
        message.error('无权限编辑此应用')
        router.push('/')
        return
      }
      
      // 填充表单数据
      formData.value = {
        appName: app.value.appName || '',
        cover: app.value.cover || '',
        priority: app.value.priority || 0,
      }
    } else {
      message.error('获取应用信息失败：' + res.data.message)
      router.push('/')
    }
  } catch (error) {
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    submitting.value = true
    
    let res
    if (isAdmin.value) {
      // 管理员可以修改所有字段
      res = await updateAppByAdmin(
        { id: appId },
        {
          id: appId,
          appName: formData.value.appName,
          cover: formData.value.cover,
          priority: formData.value.priority,
        }
      )
    } else {
      // 普通用户只能修改应用名称
      res = await updateApp(
        { id: appId },
        {
          id: appId,
          appName: formData.value.appName,
        }
      )
    }
    
    if (res.data.code === 0) {
      message.success('保存成功！')
      router.back()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.back()
}

// 上传封面图片
const handleUploadChange = (info: any) => {
  // 这里可以添加图片上传逻辑
  // 暂时使用输入框输入URL
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
  <div class="app-edit-page">
    <a-spin :spinning="loading" class="full-height">
      <div class="page-header">
        <div class="header-content">
          <h2>编辑应用</h2>
          <p>修改应用的基本信息</p>
        </div>
        <div class="header-actions">
          <a-button @click="handleCancel">
            取消
          </a-button>
          <a-button 
            type="primary" 
            :loading="submitting"
            @click="handleSubmit"
          >
            保存
          </a-button>
        </div>
      </div>

      <div class="page-content">
        <a-row :gutter="32">
          <!-- 表单区域 -->
          <a-col :span="18">
            <a-card title="基本信息">
              <a-form
                ref="formRef"
                :model="formData"
                :rules="rules"
                layout="vertical"
                :label-col="{ span: 24 }"
                :wrapper-col="{ span: 24 }"
              >
                <a-form-item label="应用名称" name="appName">
                  <a-input
                    v-model:value="formData.appName"
                    placeholder="请输入应用名称"
                    :max-length="80"
                    show-count
                  />
                </a-form-item>

                <a-form-item 
                  v-if="isAdmin" 
                  label="应用封面" 
                  name="cover"
                >
                  <a-input
                    v-model:value="formData.cover"
                    placeholder="请输入封面图片URL"
                    :max-length="255"
                  />
                  <div class="help-text">
                    支持 http:// 或 https:// 开头的图片链接
                  </div>
                </a-form-item>

                <a-form-item 
                  v-if="isAdmin" 
                  label="优先级" 
                  name="priority"
                >
                  <a-input-number
                    v-model:value="formData.priority"
                    :min="0"
                    :max="999"
                    style="width: 200px"
                    placeholder="请输入优先级"
                  />
                  <div class="help-text">
                    数值越大优先级越高，99表示精选应用
                  </div>
                </a-form-item>

                <!-- 只读信息 -->
                <a-divider>其他信息</a-divider>
                
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="应用ID">
                      <a-input :value="app?.id" readonly />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="代码生成类型">
                      <a-input :value="app?.codeGenType || '未设置'" readonly />
                    </a-form-item>
                  </a-col>
                </a-row>

                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="创建时间">
                      <a-input :value="app?.createTime" readonly />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="更新时间">
                      <a-input :value="app?.updateTime" readonly />
                    </a-form-item>
                  </a-col>
                </a-row>

                <a-form-item v-if="app?.deployedTime" label="部署时间">
                  <a-input :value="app.deployedTime" readonly />
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>

          <!-- 预览区域 -->
          <a-col :span="6">
            <a-card title="预览">
              <div class="preview-container">
                <div class="app-preview">
                  <div class="preview-cover">
                    <img 
                      v-if="formData.cover" 
                      :src="formData.cover" 
                      :alt="formData.appName"
                      @error="$event.target.style.display='none'"
                    />
                    <div v-else class="no-cover">
                      📄
                    </div>
                  </div>
                  <div class="preview-info">
                    <h4 class="preview-title">
                      {{ formData.appName || '应用名称' }}
                      <a-tag v-if="isAdmin && formData.priority === 99" color="red" size="small">
                        精选
                      </a-tag>
                    </h4>
                    <div class="preview-meta">
                      <div class="creator-info">
                        <a-avatar :size="20" :src="app?.user?.userAvatar">
                          {{ app?.user?.userName?.[0] }}
                        </a-avatar>
                        <span class="creator-name">
                          {{ app?.user?.userName || '创建者' }}
                        </span>
                      </div>
                      <div class="create-time">
                        {{ app?.createTime }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 权限说明 -->
              <a-alert
                v-if="!isAdmin"
                message="权限说明"
                description="普通用户只能修改应用名称，其他信息需要管理员权限才能修改。"
                type="info"
                show-icon
                style="margin-top: 16px"
              />
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.app-edit-page {
  min-height: 100vh;
  background: #f5f5f5;
  margin: -24px;
}

.full-height {
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px 40px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
}

.header-content p {
  margin: 0;
  color: #666;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.page-content {
  padding: 24px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.help-text {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.preview-container {
  margin-bottom: 16px;
}

.app-preview {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.preview-cover {
  height: 120px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.preview-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-cover {
  font-size: 32px;
  color: #ccc;
}

.preview-info {
  padding: 12px;
}

.preview-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-meta {
  font-size: 12px;
  color: #666;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.creator-name {
  font-size: 12px;
}

.create-time {
  font-size: 11px;
  color: #999;
}
</style>
