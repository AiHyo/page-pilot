<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const form = ref({ prompt: '' })
const myAppsLoading = ref(false)
const featuredAppsLoading = ref(false)
const creating = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const myAppsPagination = ref({ current: 1, pageSize: 20, total: 0 })
const featuredAppsPagination = ref({ current: 1, pageSize: 20, total: 0 })

const examplePrompts = [
  {
    label: '名片',
    prompt: '生成一个极简个人名片单页，深蓝白配色，包含姓名、职位、三句简介和 GitHub 按钮。只要一个 HTML 文件。',
  },
  {
    label: '企业站',
    prompt: '制作一个企业官网，包含公司介绍、产品展示、新闻动态、联系我们，商务风格，色调稳重大气。',
  },
  {
    label: '作品集',
    prompt: '开发一个在线作品集网站，展示个人项目和技能，包含项目展示、技能介绍、个人简历。',
  },
  {
    label: '餐厅',
    prompt: '创建一个餐厅官网，包含菜单展示、在线预订、餐厅介绍、联系方式，配色温暖。',
  },
]

const handleSubmit = async () => {
  if (!form.value.prompt.trim()) {
    message.error('请先描述你想要的页面')
    return
  }
  if (!loginUserStore.loginUser.id) {
    message.error('请先登录')
    router.push({ path: '/user/login', query: { redirect: '/' } })
    return
  }
  if (creating.value) return
  creating.value = true
  try {
    const res = await addApp({
      appName: `应用_${Date.now()}`,
      initPrompt: form.value.prompt,
    })
    if (res.data.code === 0) {
      message.success('已创建')
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error(res.data.message || '创建失败')
    }
  } catch {
    message.error('创建失败')
  } finally {
    creating.value = false
  }
}

const setExamplePrompt = (prompt: string) => {
  form.value.prompt = prompt
}

const loadMyApps = async (page = 1) => {
  if (!loginUserStore.loginUser.id) return
  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      appQueryRequest: { pageNum: page, pageSize: myAppsPagination.value.pageSize },
    })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPagination.value.total = res.data.data.totalRow || 0
      myAppsPagination.value.current = page
    }
  } catch {
    message.error('作品列表加载失败')
  } finally {
    myAppsLoading.value = false
  }
}

const loadFeaturedApps = async (page = 1) => {
  featuredAppsLoading.value = true
  try {
    const res = await listFeaturedAppVoByPage({
      appQueryRequest: { pageNum: page, pageSize: featuredAppsPagination.value.pageSize },
    })
    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPagination.value.total = res.data.data.totalRow || 0
      featuredAppsPagination.value.current = page
    }
  } catch {
    message.error('精选列表加载失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

const handleAppAction = (key: string, app: API.AppVO) => {
  switch (key) {
    case 'viewDialog':
      router.push(`/app/chat/${app.id}?view=1`)
      break
    case 'viewWork':
      if (app.deployKey) {
        window.open(getDeployUrl(app.deployKey), '_blank')
      } else {
        message.warning('该应用尚未部署')
      }
      break
    case 'enterApp':
      router.push(`/app/chat/${app.id}`)
      break
  }
}

const myAppActions = [
  { label: '对话', key: 'viewDialog' },
  { label: '成品', key: 'viewWork', condition: true },
]
const featuredAppActions = [
  { label: '对话', key: 'viewDialog' },
  { label: '成品', key: 'viewWork', condition: true },
]

onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div class="home">
    <header class="large-title">
      <h1>PagePilot</h1>
      <p>描述你想要的页面，生成可预览、可部署的网站。</p>
    </header>

    <section class="composer">
      <label class="field-label" for="prompt">新应用</label>
      <div class="composer-card">
        <textarea
          id="prompt"
          v-model="form.prompt"
          rows="5"
          :disabled="creating"
          placeholder="例如：深蓝白配色的个人名片，包含姓名、职位和 GitHub 按钮"
        />
        <div class="composer-bar">
          <div class="chips">
            <button
              v-for="item in examplePrompts"
              :key="item.label"
              type="button"
              class="chip"
              @click="setExamplePrompt(item.prompt)"
            >
              {{ item.label }}
            </button>
          </div>
          <button class="primary" type="button" :disabled="creating" @click="handleSubmit">
            {{ creating ? '创建中…' : '生成' }}
          </button>
        </div>
      </div>
    </section>

    <section v-if="loginUserStore.loginUser.id" class="block">
      <div class="block-head">
        <h2>我的作品</h2>
        <span>{{ myAppsPagination.total }}</span>
      </div>
      <div v-if="myApps.length" class="cards">
        <AppCard
          v-for="app in myApps"
          :key="app.id"
          :app="app"
          :actions="myAppActions.map((action) => ({
            ...action,
            condition: action.key === 'viewWork' ? !!app.deployKey : true,
          }))"
          @action="handleAppAction"
        />
      </div>
      <p v-else-if="!myAppsLoading" class="empty">还没有作品。写一句描述即可创建。</p>
      <a-pagination
        v-if="myAppsPagination.total > myAppsPagination.pageSize"
        v-model:current="myAppsPagination.current"
        :total="myAppsPagination.total"
        :page-size="myAppsPagination.pageSize"
        class="pager"
        @change="loadMyApps"
      />
    </section>

    <section class="block">
      <div class="block-head">
        <h2>精选</h2>
        <span>{{ featuredAppsPagination.total }}</span>
      </div>
      <div v-if="featuredApps.length" class="cards">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          :show-featured-badge="true"
          :show-author="true"
          :actions="featuredAppActions.map((action) => ({
            ...action,
            condition: action.key === 'viewWork' ? !!app.deployKey : true,
          }))"
          @action="handleAppAction"
        />
      </div>
      <p v-else-if="!featuredAppsLoading" class="empty">暂无精选案例。</p>
    </section>
  </div>
</template>

<style scoped>
.home {
  max-width: 980px;
  margin: 0 auto;
  padding: 12px 20px 56px;
  background: var(--bg);
}

.large-title {
  padding: 12px 4px 20px;
}

.large-title h1 {
  margin: 0 0 6px;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.03em;
}

.large-title p {
  margin: 0;
  max-width: 36em;
  color: var(--mute);
  font-size: 17px;
  line-height: 1.4;
}

.composer {
  margin-bottom: 32px;
}

.field-label {
  display: block;
  margin: 0 16px 8px;
  color: var(--mute);
  font-size: 13px;
  font-weight: 590;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.composer-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: 14px 14px 12px;
  box-shadow: var(--shadow);
}

.composer-card textarea {
  width: 100%;
  border: 0;
  resize: vertical;
  background: transparent;
  color: var(--ink);
  font-size: 17px;
  line-height: 1.45;
  outline: none;
}

.composer-card textarea::placeholder {
  color: rgba(60, 60, 67, 0.35);
}

.composer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  height: 28px;
  padding: 0 12px;
  border: 0;
  border-radius: 980px;
  background: var(--fill);
  color: var(--ink);
  font-size: 13px;
  font-weight: 510;
  cursor: pointer;
}

.chip:hover {
  background: rgba(120, 120, 128, 0.24);
}

.primary {
  flex: 0 0 auto;
  height: 36px;
  padding: 0 16px;
  border: 0;
  border-radius: 980px;
  background: var(--accent);
  color: #fff;
  font-size: 15px;
  font-weight: 590;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.45;
  cursor: wait;
}

.block {
  margin-top: 8px;
}

.block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 8px 4px 12px;
}

.block-head h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.block-head span {
  color: var(--mute);
  font-size: 15px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.empty {
  margin: 0;
  padding: 20px 16px;
  background: var(--surface);
  border-radius: var(--radius);
  color: var(--mute);
  font-size: 15px;
}

.pager {
  margin-top: 16px;
}

@media (max-width: 640px) {
  .composer-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .primary {
    width: 100%;
    height: 44px;
  }
}
</style>
