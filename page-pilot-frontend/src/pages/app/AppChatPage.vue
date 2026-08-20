<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

import { getAppVoById, deployApp } from '@/api/appController'
import { getLatestChatHistory, listAppChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import { getPreviewUrl } from '@/config/env'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import aiAvatarUrl from '@/assets/aiAvatar.png'
import myAxios from '@/request'
import { VisualEditorManager, type ElementInfo, type EditorMessage, MessageType } from '@/utils/visualEditor'
import { postSse } from '@/utils/sse'
import { formatTime } from '@/utils/formatTime'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.id as string
// OpenAPI types use number; snowflake ids stay strings at runtime to keep precision.
const appIdParam = appId as unknown as number

// 应用信息
const app = ref<API.AppVO>()
const loading = ref(false)
const deploying = ref(false)
const downloading = ref(false)

const messages = ref<Array<{
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: string
  createTime?: string
}>>([])
const userInput = ref('')
const isGenerating = ref(false)
const generationComplete = ref(false)

// 历史消息加载相关
const historyLoading = ref(false)
const hasMoreHistory = ref(true)
const lastCreateTime = ref<string | undefined>(undefined)
const historyLoaded = ref(false)

// 预览相关
const previewUrl = ref('')
const showPreview = ref(false)

const assignPreviewUrl = () => {
  const base = getPreviewUrl(app.value?.codeGenType || '', appId)
  previewUrl.value = `${base}${base.includes('?') ? '&' : '?'}t=${Date.now()}`
}

// 权限相关
const isOwner = ref(false)

// 应用详情弹窗
const detailModalVisible = ref(false)

// 滚动相关
const chatMessagesRef = ref<HTMLElement>()
const userHasScrolled = ref(false)
const isAtBottom = ref(true)

// 可视化编辑相关
const isEditMode = ref(false)
const selectedElement = ref<ElementInfo | null>(null)
const visualEditorManager = ref<VisualEditorManager | null>(null)
const previewIframeRef = ref<HTMLIFrameElement | null>(null)
const generateAbort = ref<AbortController | null>(null)
const sawBusinessError = ref(false)

const toChatRole = (messageType?: string): 'user' | 'ai' =>
  (messageType || '').toLowerCase() === 'user' ? 'user' : 'ai'

// 加载历史消息
const loadHistoryMessages = async () => {
  if (historyLoaded.value) return

  historyLoading.value = true
  try {
    const res = await getLatestChatHistory({ appId: appIdParam, limit: 10 })
    if (res.data.code === 0 && res.data.data) {
      const historyMessages = res.data.data.map((item: API.ChatHistoryVO) => ({
        id: item.id?.toString() || Date.now().toString(),
        type: toChatRole(item.messageType),
        content: item.message || '',
        timestamp: formatTime(item.createTime) || new Date(item.createTime || '').toLocaleTimeString(),
        createTime: item.createTime
      }))

      // 按时间升序排列
      historyMessages.sort((a, b) => new Date(a.createTime || '').getTime() - new Date(b.createTime || '').getTime())

      messages.value = historyMessages

      // 设置最早的消息时间用于分页
      if (historyMessages.length > 0) {
        lastCreateTime.value = historyMessages[0].createTime
        hasMoreHistory.value = historyMessages.length >= 10
      }

      historyLoaded.value = true

      // 如果有历史消息且消息数量>=2，显示预览
      if (historyMessages.length >= 2) {
        showPreview.value = true
        assignPreviewUrl()
        generationComplete.value = true
      }

      // 滚动到底部
      nextTick(() => {
        scrollToBottom(true)
      })
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
  } finally {
    historyLoading.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  if (historyLoading.value || !hasMoreHistory.value || !lastCreateTime.value) return

  historyLoading.value = true
  try {
    const res = await listAppChatHistory({
      appId: appIdParam,
      pageSize: 10,
      lastCreateTime: lastCreateTime.value
    })

    if (res.data.code === 0 && res.data.data?.records) {
      const newMessages = res.data.data.records.map((item: API.ChatHistory) => ({
        id: item.id?.toString() || Date.now().toString(),
        type: toChatRole(item.messageType),
        content: item.message || '',
        timestamp: formatTime(item.createTime) || new Date(item.createTime || '').toLocaleTimeString(),
        createTime: item.createTime
      }))

      // 按时间升序排列
      newMessages.sort((a, b) => new Date(a.createTime || '').getTime() - new Date(b.createTime || '').getTime())

      // 添加到消息列表开头
      messages.value = [...newMessages, ...messages.value]

      // 更新分页信息
      if (newMessages.length > 0) {
        lastCreateTime.value = newMessages[0].createTime
        hasMoreHistory.value = newMessages.length >= 10
      } else {
        hasMoreHistory.value = false
      }
    }
  } catch (error) {
    console.error('加载更多历史消息失败:', error)
    message.error('加载更多历史消息失败')
  } finally {
    historyLoading.value = false
  }
}

// 加载应用信息
const loadApp = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appIdParam })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data

      // 检查是否为应用所有者
      isOwner.value = String(app.value.userId ?? '') === String(loginUserStore.loginUser.id ?? '')

      // 先加载历史消息
      await loadHistoryMessages()

      // 只有在是自己的应用且没有对话历史时才自动发送初始消息
      if (isOwner.value && messages.value.length === 0 && app.value.initPrompt) {
        await sendMessage(app.value.initPrompt, true)
      }
    } else {
      message.error('加载应用失败')
      router.push('/')
    }
  } catch {
    message.error('加载应用失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async (content: string, isInitial = false) => {
  if (!content.trim() && !isInitial) return

  let messageContent = isInitial ? content : userInput.value.trim()
  if (!messageContent) return

  // 如果有选中元素，添加元素上下文
  if (selectedElement.value && !isInitial) {
    const elementContext = formatElementContext(selectedElement.value)
    messageContent = `${elementContext}\n\n${messageContent}`
  }

  // 添加用户消息
  const userMessage = {
    id: Date.now().toString(),
    type: 'user' as const,
    content: messageContent,
    timestamp: new Date().toLocaleTimeString(),
    createTime: new Date().toISOString()
  }

  if (!isInitial) {
    messages.value.push(userMessage)
    scrollToBottom()
    // User/AI turns are persisted by the backend SSE handler.
  }

  // 添加AI消息占位符
  const aiMessageId = Date.now().toString() + '_ai'
  const aiMessage = {
    id: aiMessageId,
    type: 'ai' as const,
    content: '',
    timestamp: new Date().toLocaleTimeString(),
    createTime: new Date().toISOString()
  }
  messages.value.push(aiMessage)

  userInput.value = ''
  isGenerating.value = true
  generationComplete.value = false
  sawBusinessError.value = false
  scrollToBottom()

  generateAbort.value?.abort()
  const controller = new AbortController()
  generateAbort.value = controller

  const appendAiChunk = (chunk: string) => {
    const current = messages.value.find(msg => msg.id === aiMessageId)
    if (!current || !chunk) {
      return
    }
    checkIfAtBottom()
    const wasAtBottom = isAtBottom.value
    current.content += chunk
    if (wasAtBottom || !userHasScrolled.value) {
      nextTick(() => {
        nextTick(() => {
          if (chatMessagesRef.value) {
            chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
          }
        })
      })
    }
  }

  const failGeneration = (errorMessage: string) => {
    sawBusinessError.value = true
    isGenerating.value = false
    const current = messages.value.find(msg => msg.id === aiMessageId)
    if (current) {
      current.content = `❌ ${errorMessage}`
    }
    message.error(errorMessage)
    controller.abort()
  }

  const finishGeneration = () => {
    if (sawBusinessError.value || !isGenerating.value) {
      return
    }
    isGenerating.value = false
    generationComplete.value = true
    showPreview.value = true
    assignPreviewUrl()
    message.success('代码生成完成！')
    if (selectedElement.value) {
      clearSelectedElement()
      exitEditMode()
    }
  }

  try {
    await postSse(
      '/api/app/chat/gen/code',
      { appId, message: messageContent },
      (eventName, data) => {
        if (eventName === 'business-error') {
          try {
            const errorData = JSON.parse(data || '{}') as { message?: string }
            failGeneration(errorData.message || '生成过程中出现错误')
          } catch (parseError) {
            console.error('解析错误事件失败:', parseError, '原始数据:', data)
            failGeneration('服务器返回错误')
          }
          return
        }
        if (eventName === 'done') {
          finishGeneration()
          return
        }
        if (eventName !== 'message' || sawBusinessError.value) {
          return
        }
        try {
          const parsed = JSON.parse(data) as { data?: string }
          appendAiChunk(parsed.data || '')
        } catch (parseError) {
          console.error('解析SSE数据失败:', parseError)
        }
      },
      controller.signal,
    )
    if (isGenerating.value && !sawBusinessError.value) {
      isGenerating.value = false
      message.error('代码生成失败，请重试')
    }
  } catch (error) {
    if (controller.signal.aborted || sawBusinessError.value) {
      return
    }
    console.error('发送消息失败:', error)
    isGenerating.value = false
    message.error(error instanceof Error ? error.message : '发送消息失败')
  }
}

// 部署应用
const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const refreshCoverAfterDeploy = async () => {
  for (let i = 0; i < 8; i++) {
    await wait(2000)
    try {
      const res = await getAppVoById({ id: appIdParam })
      if (res.data.code === 0 && res.data.data) {
        app.value = res.data.data
        if (res.data.data.cover) {
          return
        }
      }
    } catch {
      return
    }
  }
}

const handleDeploy = async () => {
  if (!isOwner.value) {
    message.warning('仅应用所有者可以部署')
    return
  }
  if (!generationComplete.value) {
    message.warning('请等待代码生成完成后再部署')
    return
  }

  deploying.value = true
  try {
    const res = await deployApp({ appId: appIdParam })
    if (res.data.code === 0) {
      message.success(`部署成功！访问地址：${res.data.data}`)
      void refreshCoverAfterDeploy()
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch {
    message.error('部署失败')
  } finally {
    deploying.value = false
  }
}

const toastIfJsonBlob = async (blob: Blob): Promise<boolean> => {
  const type = (blob.type || '').toLowerCase()
  const head = (await blob.slice(0, 8).text()).trimStart()
  if (!type.includes('json') && !head.startsWith('{')) {
    return false
  }
  try {
    const parsed = JSON.parse(await blob.text()) as { message?: string }
    message.error(parsed.message || '下载失败')
  } catch {
    message.error('下载失败')
  }
  return true
}

const parseDownloadFileName = (contentDisposition: string | undefined, fallback: string) => {
  if (!contentDisposition) return fallback
  const utf8 = contentDisposition.match(/filename\*=(?:UTF-8''|)([^;]+)/i)
  if (utf8?.[1]) {
    try {
      return decodeURIComponent(utf8[1].trim().replace(/^["']|["']$/g, ''))
    } catch {
      return utf8[1].trim().replace(/^["']|["']$/g, '')
    }
  }
  const quoted = contentDisposition.match(/filename="([^"]+)"/i)
  if (quoted?.[1]) return quoted[1]
  const plain = contentDisposition.match(/filename=([^;]+)/i)
  if (plain?.[1]) return plain[1].trim().replace(/^["']|["']$/g, '')
  return fallback
}

// 下载应用代码
const handleDownload = async () => {
  if (!isOwner.value) {
    message.warning('仅应用所有者可以下载')
    return
  }
  if (!generationComplete.value) {
    message.warning('请等待代码生成完成后再下载')
    return
  }

  downloading.value = true
  try {
    const response = await myAxios.get(`/app/download/${appId}`, {
      responseType: 'blob'
    })

    const fileName = parseDownloadFileName(
      response.headers['content-disposition'],
      `${appId}.zip`,
    )

    const rawBlob = response.data instanceof Blob
      ? response.data
      : new Blob([response.data])
    if (await toastIfJsonBlob(rawBlob)) {
      return
    }

    const blob = new Blob([rawBlob], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('代码下载成功！')
  } catch (error: unknown) {
    console.error('下载失败:', error)
    const axiosError = error as { response?: { data?: Blob; status?: number } }
    if (axiosError.response?.data instanceof Blob && await toastIfJsonBlob(axiosError.response.data)) {
      return
    }
    if (axiosError.response?.status === 404) {
      message.error('应用代码不存在，请先生成代码')
    } else if (axiosError.response?.status === 403) {
      message.error('无权限下载该应用代码')
    } else {
      message.error('下载失败，请重试')
    }
  } finally {
    downloading.value = false
  }
}

// 显示应用详情
const showAppDetail = () => {
  detailModalVisible.value = true
}

// 刷新应用数据
const handleRefresh = () => {
  loadApp()
}

// 滚动到底部
const scrollToBottom = (force = false) => {
  nextTick(() => {
    if (chatMessagesRef.value && (isAtBottom.value || force || !userHasScrolled.value)) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      // 更新状态
      setTimeout(() => {
        isAtBottom.value = true
        if (force) {
          userHasScrolled.value = false
        }
      }, 50) // 给滚动一点时间完成
    }
  })
}

// 检查是否在底部
const checkIfAtBottom = () => {
  if (chatMessagesRef.value) {
    const { scrollTop, scrollHeight, clientHeight } = chatMessagesRef.value
    const threshold = 30 // 增加容差到30px，更容易触发自动滚动
    isAtBottom.value = scrollTop + clientHeight >= scrollHeight - threshold
  }
}

// 处理滚动事件
const handleScroll = () => {
  checkIfAtBottom()
  if (!isAtBottom.value) {
    userHasScrolled.value = true
  }
}

// 回车发送
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage(userInput.value)
  }
}

// 可视化编辑相关方法
const toggleEditMode = () => {
  try {
    if (isEditMode.value) {
      exitEditMode()
    } else {
      enterEditMode()
    }
  } catch (error) {
    console.error('[AppChatPage] Error toggling edit mode:', error)
    message.error('切换编辑模式失败')
  }
}

const enterEditMode = () => {
  try {
    if (!isOwner.value) {
      message.warning('仅应用所有者可以使用编辑模式')
      return
    }

    if (!generationComplete.value) {
      message.warning('请等待代码生成完成后再使用编辑模式')
      return
    }

    if (!visualEditorManager.value) {
      message.error('可视化编辑器未初始化，请刷新页面重试')
      console.error('[AppChatPage] Visual editor manager not initialized')
      return
    }

    isEditMode.value = true
    visualEditorManager.value.enterEditMode()
    message.info('已进入编辑模式，点击预览网站中的元素进行选择')
    console.log('[AppChatPage] Entered edit mode')
  } catch (error) {
    console.error('[AppChatPage] Error entering edit mode:', error)
    message.error('进入编辑模式失败')
    isEditMode.value = false
  }
}

const exitEditMode = () => {
  try {
    isEditMode.value = false
    visualEditorManager.value?.exitEditMode()
    clearSelectedElement()
    console.log('[AppChatPage] Exited edit mode')
  } catch (error) {
    console.error('[AppChatPage] Error exiting edit mode:', error)
    message.error('退出编辑模式失败')
  }
}

const handleElementSelected = (element: ElementInfo) => {
  try {
    selectedElement.value = element
    console.log('[AppChatPage] Element selected:', element)
  } catch (error) {
    console.error('[AppChatPage] Error handling element selection:', error)
    message.error('处理元素选择失败')
  }
}

const clearSelectedElement = () => {
  try {
    selectedElement.value = null
    visualEditorManager.value?.clearSelection()
    console.log('[AppChatPage] Cleared selected element')
  } catch (error) {
    console.error('[AppChatPage] Error clearing selected element:', error)
  }
}

const formatElementContext = (element: ElementInfo): string => {
  const parts = []

  if (element.tagName) {
    parts.push(`标签: ${element.tagName}`)
  }

  if (element.className) {
    parts.push(`类名: ${element.className}`)
  }

  if (element.id) {
    parts.push(`ID: ${element.id}`)
  }

  if (element.textContent) {
    const content = element.textContent.substring(0, 50)
    parts.push(`内容: "${content}${element.textContent.length > 50 ? '...' : ''}"`)
  }

  return `[编辑元素] ${parts.join(', ')}`
}

const handleEditorMessage = (editorMessage: EditorMessage) => {
  try {
    if (editorMessage.type === MessageType.ELEMENT_SELECTED && editorMessage.data) {
      handleElementSelected(editorMessage.data)
    }
  } catch (error) {
    console.error('[AppChatPage] Error handling editor message:', error)
    message.error('处理元素选择消息失败')
  }
}

// 初始化可视化编辑器
const initVisualEditor = () => {
  try {
    if (previewIframeRef.value) {
      console.log('[AppChatPage] Initializing visual editor...')
      visualEditorManager.value = new VisualEditorManager()
      visualEditorManager.value.init(previewIframeRef.value, handleEditorMessage)
      console.log('[AppChatPage] Visual editor initialized successfully')
    } else {
      console.warn('[AppChatPage] Preview iframe not found, visual editor not initialized')
    }
  } catch (error) {
    console.error('[AppChatPage] Failed to initialize visual editor:', error)
    message.error('可视化编辑器初始化失败')
  }
}

const refreshPreview = () => {
  if (!showPreview.value && !generationComplete.value) {
    return
  }
  showPreview.value = true
  assignPreviewUrl()
}

const onPreviewIframeLoad = () => {
  try {
    visualEditorManager.value?.destroy()
    initVisualEditor()
    if (isEditMode.value) {
      visualEditorManager.value?.enterEditMode()
    }
  } catch (error) {
    console.error('[AppChatPage] Error on preview load:', error)
  }
}

onMounted(() => {
  loadApp()
})

onUnmounted(() => {
  generateAbort.value?.abort()
  try {
    visualEditorManager.value?.destroy()
  } catch (error) {
    console.error('[AppChatPage] Error destroying visual editor:', error)
  }
})
</script>

<template>
  <div class="app-chat-page">
    <a-spin :spinning="loading" class="full-height">
      <div class="top-bar">
        <div class="app-info">
          <button type="button" class="back" @click="router.push('/')">首页</button>
          <div class="title-block">
            <h1 class="app-name">{{ app?.appName || '应用' }}</h1>
            <p v-if="app?.codeGenType" class="code-type">{{ getCodeGenTypeLabel(app.codeGenType) }}</p>
          </div>
        </div>
        <div class="top-actions">
          <button type="button" class="ghost" @click="showAppDetail">详情</button>
          <button
            type="button"
            class="ghost"
            :disabled="!generationComplete || !isOwner || downloading"
            @click="handleDownload"
          >
            {{ downloading ? '打包中' : '下载' }}
          </button>
          <button
            type="button"
            class="accent"
            :disabled="!generationComplete || !isOwner || deploying"
            @click="handleDeploy"
          >
            {{ deploying ? '部署中' : '部署' }}
          </button>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 左侧对话区域 -->
        <div class="chat-panel">
          <div class="chat-header">
            <h2>对话</h2>
          </div>

          <!-- 消息列表 -->
          <div
            ref="chatMessagesRef"
            class="chat-messages"
            @scroll="handleScroll"
          >
            <!-- 加载更多历史消息按钮 -->
            <div
              v-if="hasMoreHistory && historyLoaded && messages.length > 0"
              class="load-more-container"
            >
              <button
                type="button"
                class="load-more-btn"
                :disabled="historyLoading"
                @click="loadMoreHistory"
              >
                {{ historyLoading ? '加载中' : '更早的消息' }}
              </button>
            </div>

            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['message', msg.type === 'user' ? 'user-message' : 'ai-message']"
            >
              <div class="message-header">
                <a-avatar
                  :src="msg.type === 'user' ? loginUserStore.loginUser.userAvatar : aiAvatarUrl"
                  class="message-avatar"
                >
                  {{ msg.type === 'user' ? loginUserStore.loginUser.userName?.[0] : 'AI' }}
                </a-avatar>
                <span class="message-time">{{ msg.timestamp }}</span>
              </div>
              <div class="message-content">
                <MarkdownRenderer
                  v-if="msg.type === 'ai'"
                  :content="msg.content"
                  class="ai-content"
                />
                <div v-else class="user-content">{{ msg.content }}</div>
              </div>
            </div>

            <!-- 移除原有的生成中提示，改为在输入框区域显示 -->

            <!-- 滚动到底部按钮 -->
            <div
              v-if="userHasScrolled && !isAtBottom"
              class="scroll-to-bottom"
              @click="scrollToBottom(true)"
            >
              <a-button type="primary" shape="circle" size="small">
                ↓
              </a-button>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input">
            <!-- 生成状态提示条 -->
            <div v-if="isGenerating" class="generating-status-bar">
              正在生成
            </div>

            <!-- 选中元素提示 -->
            <a-alert
              v-if="selectedElement"
              type="info"
              closable
              @close="clearSelectedElement"
              class="selected-element-alert"
            >
              <template #message>
                <div class="selected-element-info">
                  <span class="element-label">已选中元素:</span>
                  <span class="element-tag">&lt;{{ selectedElement.tagName.toLowerCase() }}</span>
                  <span v-if="selectedElement.className" class="element-class">class="{{ selectedElement.className }}"</span>
                  <span v-if="selectedElement.id" class="element-id">id="{{ selectedElement.id }}"</span>
                  <span class="element-tag">&gt;</span>
                  <span v-if="selectedElement.textContent" class="element-text">
                    - "{{ selectedElement.textContent.substring(0, 30) }}{{ selectedElement.textContent.length > 30 ? '...' : '' }}"
                  </span>
                </div>
              </template>
            </a-alert>

            <a-tooltip
              v-if="!isOwner"
              title="无法在别人的作品下对话哦~"
              placement="top"
            >
              <a-textarea
                v-model:value="userInput"
                placeholder="请描述你想生成的网站，越详细效果越好哦"
                :rows="3"
                :disabled="isGenerating || !isOwner"
                @keydown="handleKeyDown"
                class="input-field"
                :class="{ 'disabled-input': !isOwner }"
              />
            </a-tooltip>
            <a-textarea
              v-else
              v-model:value="userInput"
              placeholder="请描述你想生成的网站，越详细效果越好哦"
              :rows="3"
              :disabled="isGenerating"
              @keydown="handleKeyDown"
              class="input-field"
            />
            <div class="input-actions">
              <div class="left-actions">
                <a-tooltip :title="isEditMode ? '退出编辑模式' : ((!isOwner) ? '仅应用所有者可编辑' : (!generationComplete) ? '请等待代码生成完成' : '进入编辑模式')">
                  <a-button
                    type="text"
                    size="small"
                    :disabled="!isOwner || !generationComplete"
                    :class="{ 'edit-mode-active': isEditMode }"
                    @click="toggleEditMode"
                    class="edit-mode-btn"
                  >
                    {{ isEditMode ? '编辑中' : '编辑' }}
                  </a-button>
                </a-tooltip>
              </div>
              <button
                type="button"
                class="send-btn"
                :disabled="!userInput.trim() || !isOwner || isGenerating"
                @click="sendMessage(userInput)"
              >
                {{ isGenerating ? '生成中' : '发送' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧预览区域 -->
        <div class="preview-panel">
          <div class="preview-header">
            <h2>预览</h2>
            <button type="button" class="ghost" @click="refreshPreview">刷新</button>
          </div>

          <div class="preview-content">
            <!-- 生成中状态 -->
            <div v-if="isGenerating && !showPreview" class="preview-empty">
              <div class="empty-card">
                <h3>正在生成</h3>
                <p>写完后这里会打开这个应用的页面。</p>
              </div>
            </div>
            <div v-else-if="!showPreview && !isGenerating" class="preview-empty">
              <div class="empty-card">
                <h3>预览</h3>
                <p>左侧发一句需求。生成结束后，页面会出现在这里。</p>
              </div>
            </div>

            <iframe
              v-if="showPreview"
              ref="previewIframeRef"
              :src="previewUrl"
              class="preview-iframe"
              frameborder="0"
              sandbox="allow-scripts allow-forms allow-downloads allow-popups"
              @load="onPreviewIframeLoad"
            />
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
      v-model:visible="detailModalVisible"
      :app="app"
      :loading="loading"
      @refresh="handleRefresh"
    />
  </div>
</template>

<style scoped>
.app-chat-page {
  height: 100dvh;
  background: var(--bg);
  display: flex;
  flex-direction: column;
  color: var(--ink);
}

.full-height {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.full-height :deep(.ant-spin-container) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.82);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(16px);
  flex-shrink: 0;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.title-block {
  min-width: 0;
}

.app-name {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.02em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-type {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--mute);
}

.top-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

.back,
.ghost,
.accent,
.send-btn,
.load-more-btn {
  border: 0;
  cursor: pointer;
  font: inherit;
}

.back,
.ghost {
  background: transparent;
  color: var(--accent);
  padding: 6px 8px;
  border-radius: 8px;
}

.back:hover,
.ghost:hover:not(:disabled) {
  background: var(--fill);
}

.accent,
.send-btn {
  background: var(--accent);
  color: #fff;
  border-radius: 10px;
  padding: 7px 14px;
  font-weight: 600;
}

.accent:hover:not(:disabled),
.send-btn:hover:not(:disabled) {
  background: var(--accent-press);
}

.accent:disabled,
.send-btn:disabled,
.ghost:disabled,
.load-more-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.main-content {
  flex: 1;
  display: flex;
  min-height: 0;
}

.chat-panel,
.preview-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}

.chat-panel {
  width: 42%;
  background: var(--bg);
  border-right: 1px solid var(--line);
}

.preview-panel {
  width: 58%;
  background: var(--surface);
}

.chat-header,
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid var(--line);
}

.chat-header h2,
.preview-header h2 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--mute);
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  position: relative;
  min-height: 0;
}

.message {
  margin-bottom: 16px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-time {
  font-size: 12px;
  color: var(--mute);
}

.message-content {
  margin-left: 40px;
  padding: 10px 14px;
  border-radius: 16px;
  max-width: 86%;
}

.user-message .message-content {
  background: var(--accent);
  color: #fff;
  margin-left: auto;
  margin-right: 0;
}

.ai-message .message-content {
  background: var(--surface);
  box-shadow: var(--shadow);
}

.ai-message .ai-content :deep(.markdown-content) {
  font-size: 14px;
  line-height: 1.55;
  color: var(--ink);
}

.ai-message .ai-content :deep(pre) {
  background: var(--bg);
  border-radius: 10px;
  padding: 12px;
  margin: 8px 0;
  font-size: 13px;
}

.user-content {
  font-size: 15px;
  line-height: 1.45;
}

.scroll-to-bottom {
  position: absolute;
  bottom: 16px;
  right: 16px;
}

.chat-input {
  border-top: 1px solid var(--line);
  padding: 12px 16px 16px;
  background: var(--surface);
  flex-shrink: 0;
}

.generating-status-bar {
  margin: -12px -16px 12px;
  padding: 8px 16px;
  background: var(--fill);
  color: var(--ink);
  font-size: 13px;
}

.input-field {
  margin-bottom: 10px;
}

.input-field :deep(textarea) {
  border-radius: 12px;
  border-color: var(--line);
  background: var(--bg);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-mode-active {
  color: var(--accent);
  font-weight: 600;
}

.preview-content {
  flex: 1;
  position: relative;
  background: var(--bg);
  min-height: 0;
}

.preview-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.empty-card {
  max-width: 280px;
  padding: 20px 18px;
  background: var(--surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  text-align: left;
}

.empty-card h3 {
  margin: 0 0 6px;
  font-size: 17px;
}

.empty-card p {
  margin: 0;
  color: var(--mute);
  font-size: 15px;
  line-height: 1.45;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.load-more-container {
  padding: 0 0 12px;
  text-align: center;
}

.load-more-btn {
  background: var(--surface);
  color: var(--accent);
  border-radius: 10px;
  padding: 8px 14px;
  box-shadow: var(--shadow);
}

.disabled-input :deep(textarea) {
  opacity: 0.55;
}

.selected-element-alert {
  margin-bottom: 10px;
}

.selected-element-info {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
}

@media (max-width: 767px) {
  .main-content {
    flex-direction: column;
  }

  .chat-panel,
  .preview-panel {
    width: 100%;
  }

  .chat-panel {
    flex: 1;
    border-right: 0;
    border-bottom: 1px solid var(--line);
  }

  .preview-panel {
    flex: 0 0 42%;
    min-height: 220px;
  }

  .app-name {
    max-width: 42vw;
  }

  .top-bar {
    padding: 8px 10px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .app-chat-page * {
    transition: none;
  }
}
</style>
