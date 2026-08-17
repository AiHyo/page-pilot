<template>
  <a-layout
    class="basic-layout"
    :class="{ 'is-auth': isAuthPage, 'is-home': isHomePage, 'is-chat': isChatPage }"
  >
    <GlobalHeader v-if="!isAuthPage && !isChatPage" />
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>
    <GlobalFooter v-if="!isAuthPage && !isChatPage" />
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()
const isAuthPage = computed(() => route.path.startsWith('/user/'))
const isHomePage = computed(() => route.path === '/')
const isChatPage = computed(() => route.path.startsWith('/app/chat'))
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: var(--bg);
}

.main-content {
  max-width: 980px;
  width: 100%;
  padding: 20px 20px 64px;
  margin: 0 auto;
  background: transparent;
}

.is-home .main-content,
.is-auth .main-content,
.is-chat .main-content {
  max-width: none;
  padding: 0;
  margin: 0;
}

.is-chat {
  min-height: 100dvh;
  height: 100dvh;
  overflow: hidden;
}

.is-chat .main-content {
  height: 100%;
}
</style>
