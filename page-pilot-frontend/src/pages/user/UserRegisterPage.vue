<template>
  <div class="auth">
    <RouterLink to="/user/login" class="back">‹ 登录</RouterLink>

    <header class="large-title">
      <p class="product">PagePilot</p>
      <h1>创建账号</h1>
      <p class="sub">注册后即可生成、预览和部署网站</p>
    </header>

    <form class="group" @submit.prevent="handleSubmit">
      <label class="row">
        <span>账号</span>
        <input v-model="formState.userAccount" type="text" autocomplete="username" placeholder="必填" />
      </label>
      <label class="row">
        <span>密码</span>
        <input
          v-model="formState.userPassword"
          type="password"
          autocomplete="new-password"
          placeholder="至少 8 位"
        />
      </label>
      <label class="row">
        <span>确认</span>
        <input
          v-model="formState.checkPassword"
          type="password"
          autocomplete="new-password"
          placeholder="再输入一次"
        />
      </label>
    </form>

    <button class="primary" type="button" :disabled="submitting" @click="handleSubmit">
      {{ submitting ? '创建中…' : '继续' }}
    </button>

    <p class="foot">
      已有账号？
      <RouterLink to="/user/login">登录</RouterLink>
    </p>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userRegister } from '@/api/userController.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const submitting = ref(false)
const router = useRouter()

const handleSubmit = async () => {
  if (!formState.userAccount?.trim()) {
    message.error('请填写账号')
    return
  }
  if (!formState.userPassword || formState.userPassword.length < 8) {
    message.error('密码至少 8 位')
    return
  }
  if (formState.userPassword !== formState.checkPassword) {
    message.error('两次密码不一致')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await userRegister(formState)
    if (res.data.code === 0 && res.data.data) {
      message.success('账号已创建，请登录')
      router.replace('/user/login')
    } else {
      message.error(res.data.message || '注册失败')
    }
  } catch {
    message.error('注册失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  padding: 16px 20px 48px;
  background: var(--bg);
}

.back {
  display: inline-block;
  margin: 8px 0 20px;
  color: var(--accent);
  text-decoration: none;
  font-size: 17px;
}

.large-title {
  margin-bottom: 28px;
}

.product {
  margin: 0 0 4px;
  color: var(--mute);
  font-size: 13px;
  font-weight: 590;
}

.large-title h1 {
  margin: 0 0 8px;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.15;
}

.sub {
  margin: 0;
  color: var(--mute);
  font-size: 15px;
  line-height: 1.45;
}

.group {
  background: var(--surface);
  border-radius: var(--radius);
  overflow: hidden;
  margin-bottom: 20px;
}

.row {
  display: grid;
  grid-template-columns: 72px 1fr;
  align-items: center;
  min-height: 44px;
  padding: 0 16px;
  border-bottom: 0.5px solid var(--line);
}

.row:last-child {
  border-bottom: 0;
}

.row span {
  font-size: 17px;
}

.row input {
  width: 100%;
  border: 0;
  background: transparent;
  font-size: 17px;
  color: var(--ink);
  text-align: right;
  outline: none;
}

.row input::placeholder {
  color: rgba(60, 60, 67, 0.3);
}

.primary {
  width: 100%;
  height: 50px;
  border: 0;
  border-radius: 12px;
  background: var(--accent);
  color: #fff;
  font-size: 17px;
  font-weight: 590;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.45;
  cursor: wait;
}

.primary:hover:not(:disabled) {
  background: var(--accent-press);
}

.foot {
  margin-top: 28px;
  text-align: center;
  color: var(--mute);
  font-size: 15px;
}

.foot a {
  text-decoration: none;
}
</style>
