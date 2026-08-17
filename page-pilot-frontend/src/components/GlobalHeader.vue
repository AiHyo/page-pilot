<template>
  <header class="header">
    <div class="inner">
      <RouterLink to="/" class="brand">
        <img class="logo" src="@/assets/logo.png" alt="" />
        <span>PagePilot</span>
      </RouterLink>

      <nav class="nav" aria-label="主导航">
        <button
          v-for="item in menuItems"
          :key="String(item.key)"
          type="button"
          class="nav-link"
          :class="{ active: selectedKeys[0] === item.key }"
          @click="handleNav(String(item.key))"
        >
          {{ item.title }}
        </button>
      </nav>

      <div class="aside">
        <template v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <button type="button" class="user">
              {{ loginUserStore.loginUser.userName ?? '未命名' }}
            </button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doLogout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <RouterLink v-else class="login" to="/user/login">登录</RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const selectedKeys = ref<string[]>(['/'])

router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

const originItems = [
  { key: '/', title: '首页' },
  { key: '/admin/userManage', title: '用户管理' },
  { key: '/admin/appManage', title: '应用管理' },
  { key: 'https://github.com/AiHyo', title: 'GitHub' },
]

const menuItems = computed(() =>
  originItems.filter((item) => {
    if (item.key.startsWith('/admin')) {
      return loginUserStore.loginUser.userRole === 'admin'
    }
    return true
  }),
)

const handleNav = (key: string) => {
  if (key.startsWith('http')) {
    window.open(key, '_blank', 'noopener')
    return
  }
  selectedKeys.value = [key]
  router.push(key)
}

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('已退出登录')
    await router.push('/user/login')
  } else {
    message.error('退出失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(242, 242, 247, 0.78);
  border-bottom: 0.5px solid var(--line);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
}

.inner {
  max-width: 980px;
  margin: 0 auto;
  padding: 10px 20px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink);
  text-decoration: none;
  font-size: 17px;
  font-weight: 650;
  letter-spacing: -0.02em;
}

.logo {
  width: 28px;
  height: 28px;
  border-radius: 7px;
}

.nav {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.nav-link,
.user {
  border: 0;
  background: none;
  padding: 6px 12px;
  border-radius: 980px;
  font: inherit;
  font-size: 15px;
  color: var(--mute);
  cursor: pointer;
}

.nav-link.active,
.nav-link:hover,
.user:hover {
  color: var(--ink);
  background: var(--fill);
}

.login {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  border-radius: 980px;
  background: var(--accent);
  color: #fff;
  text-decoration: none;
  font-size: 15px;
  font-weight: 590;
}

.login:hover {
  background: var(--accent-press);
  color: #fff;
}
</style>
