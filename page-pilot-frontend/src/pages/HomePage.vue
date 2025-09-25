<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { CODE_GEN_TYPE_OPTIONS, getCodeGenTypeLabel } from '@/constants/codeGenType'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 表单数据
const form = ref({
  prompt: '',
})

// 分页数据
const myAppsLoading = ref(false)
const featuredAppsLoading = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const myAppsPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})
const featuredAppsPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

// 示例提示词
const examplePrompts = [
  '帮我创建一个个人博客网站，包含文章列表、文章详情、关于我等页面，使用现代化的设计风格，支持响应式布局，配色以蓝白为主，整体风格简洁专业',
  '制作一个企业官网，需要包含公司介绍、产品展示、新闻动态、联系我们等模块，采用商务风格设计，色调稳重大气，突出企业形象和专业性',
  '开发一个在线作品集网站，展示个人项目和技能，包含项目展示、技能介绍、个人简历等页面，设计要有创意感，适合设计师或开发者使用',
  '创建一个餐厅官网，包含菜单展示、在线预订、餐厅介绍、联系方式等功能，设计要温馨有食欲，配色温暖，突出美食特色和餐厅氛围'
]

// 提交创建应用
const handleSubmit = async () => {
  if (!form.value.prompt.trim()) {
    message.error('请输入提示词')
    return
  }
  
  if (!loginUserStore.loginUser.id) {
    message.error('请先登录')
    router.push('/user/login')
    return
  }

  try {
    const res = await addApp({
      appName: `应用_${Date.now()}`,
      initPrompt: form.value.prompt,
    })
    
    if (res.data.code === 0) {
      message.success('应用创建成功！')
      // 跳转到对话页面
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    message.error('创建应用失败')
  }
}

// 设置示例提示词
const setExamplePrompt = (prompt: string) => {
  form.value.prompt = prompt
}

// 加载我的应用
const loadMyApps = async (page = 1) => {
  if (!loginUserStore.loginUser.id) return
  
  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      appQueryRequest: {
        pageNum: page,
        pageSize: myAppsPagination.value.pageSize,
      }
    })
    
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPagination.value.total = res.data.data.totalRow || 0
      myAppsPagination.value.current = page
    }
  } catch (error) {
    message.error('加载我的作品失败')
  } finally {
    myAppsLoading.value = false
  }
}

// 加载精选应用
const loadFeaturedApps = async (page = 1) => {
  featuredAppsLoading.value = true
  try {
    const res = await listFeaturedAppVoByPage({
      appQueryRequest: {
        pageNum: page,
        pageSize: featuredAppsPagination.value.pageSize,
      }
    })
    
    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPagination.value.total = res.data.data.totalRow || 0
      featuredAppsPagination.value.current = page
    }
  } catch (error) {
    message.error('加载精选案例失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

// 我的应用分页变化
const onMyAppsPageChange = (page: number) => {
  loadMyApps(page)
}

// 精选应用分页变化
const onFeaturedAppsPageChange = (page: number) => {
  loadFeaturedApps(page)
}

// 处理应用卡片操作
const handleAppAction = (key: string, app: API.AppVO) => {
  switch (key) {
    case 'viewDialog':
      router.push(`/app/chat/${app.id}?view=1`)
      break
    case 'viewWork':
      if (app.deployKey) {
        const url = getDeployUrl(app.deployKey)
        console.log('部署URL:', url, '部署密钥:', app.deployKey)
        window.open(url, '_blank')
      } else {
        message.warning('该应用尚未部署')
      }
      break
    case 'enterApp':
      router.push(`/app/chat/${app.id}`)
      break
  }
}

// 我的应用操作配置
const myAppActions = [
  { label: '查看对话', key: 'viewDialog' },
  { label: '查看作品', key: 'viewWork', condition: true }
]

// 精选应用操作配置
const featuredAppActions = [
  { label: '查看对话', key: 'viewDialog' },
  { label: '查看作品', key: 'viewWork', condition: true }
]

onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div class="home-page">
    <!-- 主标题区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="main-title">
          AI 应用生成平台
        </h1>
        <p class="subtitle">一句话轻松创建网站应用</p>
        
        <!-- 输入框区域 -->
        <div class="input-section">
          <a-textarea
            v-model:value="form.prompt"
            placeholder="帮我创建个人博客网站"
            :rows="4"
            class="prompt-input"
          />
          <div class="input-actions">
            <a-button 
              type="primary" 
              class="submit-btn"
              :loading="false"
              @click="handleSubmit"
            >
              生成应用
            </a-button>
          </div>
        </div>
        
        <!-- 示例按钮 -->
        <div class="example-buttons">
          <a-button
            v-for="prompt in examplePrompts"
            :key="prompt"
            type="default"
            class="example-btn"
            @click="setExamplePrompt(prompt)"
          >
            {{ prompt }}
          </a-button>
        </div>
      </div>
    </div>

    <!-- 我的作品区域 -->
    <div v-if="loginUserStore.loginUser.id" class="section">
      <h2 class="section-title">我的作品</h2>
      <div class="apps-grid">
        <AppCard
          v-for="app in myApps"
          :key="app.id"
          :app="app"
          :actions="myAppActions.map(action => ({
            ...action,
            condition: action.key === 'viewWork' ? !!app.deployKey : true
          }))"
          @action="handleAppAction"
        />
      </div>
      
      <div v-if="myApps.length === 0 && !myAppsLoading" class="empty-state">
        <a-empty description="暂无作品，快来创建第一个吧！" />
      </div>
      
      <a-pagination
        v-if="myAppsPagination.total > 0"
        v-model:current="myAppsPagination.current"
        :total="myAppsPagination.total"
        :page-size="myAppsPagination.pageSize"
        :show-total="(total: number) => `共 ${total} 个应用`"
        class="pagination"
        @change="onMyAppsPageChange"
      />
    </div>

    <!-- 精选案例区域 -->
    <div class="section">
      <h2 class="section-title">精选案例</h2>
      <div class="apps-grid">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          :show-featured-badge="true"
          :show-author="true"
          :actions="featuredAppActions.map(action => ({
            ...action,
            condition: action.key === 'viewWork' ? !!app.deployKey : true
          }))"
          @action="handleAppAction"
        />
      </div>
      
      <div v-if="featuredApps.length === 0 && !featuredAppsLoading" class="empty-state">
        <a-empty description="暂无精选案例" />
      </div>
      
      <a-pagination
        v-if="featuredAppsPagination.total > 0"
        v-model:current="featuredAppsPagination.current"
        :total="featuredAppsPagination.total"
        :page-size="featuredAppsPagination.pageSize"
        :show-total="(total: number) => `共 ${total} 个应用`"
        class="pagination"
        @change="onFeaturedAppsPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.home-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  width: 100%;
  min-height: 100vh;
  margin: -24px;
  position: relative;
  overflow-x: hidden;
}

.home-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  pointer-events: none;
}

.hero-section {
  padding: 80px 24px 120px;
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.hero-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

.main-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
  color: white;
}

.logo-icon {
  font-size: 48px;
}

.subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.input-section {
  position: relative;
  margin-bottom: 24px;
}

.prompt-input {
  font-size: 16px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  color: #666;
}

.submit-btn {
  height: 40px;
  border-radius: 20px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.example-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.example-btn {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: white;
  backdrop-filter: blur(10px);
  font-size: 13px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.example-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.section {
  background: white;
  padding: 60px 40px;
}

.section-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 40px;
  text-align: center;
  color: #333;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1600px;
  margin: 0 auto;
}


.empty-state {
  text-align: center;
  padding: 60px 0;
}

.pagination {
  margin-top: 40px;
  text-align: center;
}
</style>
