<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { DownOutlined } from '@ant-design/icons-vue'
import { getAppVoById, deployApp, deleteApp } from '@/api/appController'
import { getLatestChatHistory, listAppChatHistory, addChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import { getPreviewUrl } from '@/config/env'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import aiAvatarUrl from '@/assets/aiAvatar.png'
import myAxios from '@/request'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.id as string

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

// 权限相关
const isOwner = ref(false)

// 应用详情弹窗
const detailModalVisible = ref(false)

// 滚动相关
const chatMessagesRef = ref<HTMLElement>()
const userHasScrolled = ref(false)
const isAtBottom = ref(true)

// 加载历史消息
const loadHistoryMessages = async () => {
  if (historyLoaded.value) return
  
  historyLoading.value = true
  try {
    const res = await getLatestChatHistory({ appId: appId as any, limit: 10 })
    if (res.data.code === 0 && res.data.data) {
      const historyMessages = res.data.data.map((item: API.ChatHistoryVO) => ({
        id: item.id?.toString() || Date.now().toString(),
        type: item.messageType === 'USER' ? 'user' : 'ai' as 'user' | 'ai',
        content: item.message || '',
        timestamp: new Date(item.createTime || '').toLocaleTimeString(),
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
        previewUrl.value = getPreviewUrl(app.value?.codeGenType || '', appId)
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
      appId: appId as any, 
      pageSize: 10,
      lastCreateTime: lastCreateTime.value
    })
    
    if (res.data.code === 0 && res.data.data?.records) {
      const newMessages = res.data.data.records.map((item: API.ChatHistory) => ({
        id: item.id?.toString() || Date.now().toString(),
        type: item.messageType === 'USER' ? 'user' : 'ai' as 'user' | 'ai',
        content: item.message || '',
        timestamp: new Date(item.createTime || '').toLocaleTimeString(),
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
    const res = await getAppVoById({ id: appId as any })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data
      
      // 检查是否为应用所有者
      isOwner.value = app.value.userId === loginUserStore.loginUser.id
      
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
  } catch (error) {
    message.error('加载应用失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async (content: string, isInitial = false) => {
  if (!content.trim() && !isInitial) return

  const messageContent = isInitial ? content : userInput.value.trim()
  if (!messageContent) return

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
    
    // 保存用户消息到后端
    try {
      await addChatHistory({
        message: messageContent,
        messageType: 'USER',
        appId: appId as any
      })
    } catch (error) {
      console.error('保存用户消息失败:', error)
    }
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
  scrollToBottom()

  try {
    // 调用SSE接口
    const eventSource = new EventSource(`/api/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(messageContent)}`)

    eventSource.onmessage = (event) => {
      try {
        // 后端返回的数据格式是 {"data": "内容"}
        const data = JSON.parse(event.data)
        const aiMessage = messages.value.find(msg => msg.id === aiMessageId)
        if (aiMessage && data.data) {
          // 先检查当前滚动状态
          checkIfAtBottom()
          const wasAtBottom = isAtBottom.value
          
          aiMessage.content += data.data
          
          // 如果用户在底部或者没有手动滚动过，则自动滚动
          if (wasAtBottom || !userHasScrolled.value) {
            // 使用双重nextTick确保DOM更新完成
            nextTick(() => {
              nextTick(() => {
                if (chatMessagesRef.value) {
                  chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
                }
              })
            })
          }
        }
      } catch (parseError) {
        console.error('解析SSE数据失败:', parseError)
      }
    }

    // 监听结束事件
    eventSource.addEventListener('done', () => {
      eventSource.close()
      // 防止重复触发
      if (isGenerating.value) {
        isGenerating.value = false
        generationComplete.value = true
        showPreview.value = true
        previewUrl.value = getPreviewUrl(app.value?.codeGenType || '', appId)
        message.success('代码生成完成！')
        
        // 保存AI消息到后端
        const aiMessage = messages.value.find(msg => msg.id === aiMessageId)
        if (aiMessage && aiMessage.content) {
          try {
            addChatHistory({
              message: aiMessage.content,
              messageType: 'AI',
              appId: appId as any
            })
          } catch (error) {
            console.error('保存AI消息失败:', error)
          }
        }
      }
    })

    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      eventSource.close()
      isGenerating.value = false
      message.error('代码生成失败，请重试')
    }

  } catch (error) {
    console.error('发送消息失败:', error)
    message.error('发送消息失败')
    isGenerating.value = false
  }
}

// 部署应用
const handleDeploy = async () => {
  if (!generationComplete.value) {
    message.warning('请等待代码生成完成后再部署')
    return
  }

  deploying.value = true
  try {
    const res = await deployApp({ appId: appId as any })
    if (res.data.code === 0) {
      message.success(`部署成功！访问地址：${res.data.data}`)
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    message.error('部署失败')
  } finally {
    deploying.value = false
  }
}

// 下载应用代码
const handleDownload = async () => {
  if (!generationComplete.value) {
    message.warning('请等待代码生成完成后再下载')
    return
  }

  downloading.value = true
  try {
    const response = await myAxios.get(`/app/download/${appId}`, {
      responseType: 'blob'
    })
    
    // 从响应头获取文件名
    const contentDisposition = response.headers['content-disposition']
    let fileName = `${appId}.zip`
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = fileNameMatch[1]
      }
    }
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/zip' })
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
  } catch (error: any) {
    console.error('下载失败:', error)
    if (error.response?.status === 404) {
      message.error('应用代码不存在，请先生成代码')
    } else if (error.response?.status === 403) {
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

// 编辑应用
const handleEdit = () => {
  router.push(`/app/edit/${appId}`)
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

onMounted(() => {
  loadApp()
})
</script>

<template>
  <div class="app-chat-page">
    <a-spin :spinning="loading" class="full-height">
      <!-- 顶部栏 -->
      <div class="top-bar">
        <div class="app-info">
          <a-dropdown>
            <a-button type="text" class="app-selector">
              <span class="app-icon">🤖</span>
              <span class="app-name">{{ app?.appName || '个人博客生成器' }}</span>
              <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="router.push('/')">
                  返回首页
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-tag v-if="app?.codeGenType" color="purple" class="code-type-tag">
            {{ getCodeGenTypeLabel(app.codeGenType) }}
          </a-tag>
        </div>
        <div class="top-actions">
          <a-button @click="showAppDetail">
            应用详情
          </a-button>
          <a-button
            :loading="downloading"
            :disabled="!generationComplete || !isOwner"
            @click="handleDownload"
          >
            <template #icon>📥</template>
            下载代码
          </a-button>
          <a-button
            type="primary"
            danger
            :loading="deploying"
            :disabled="!generationComplete"
            @click="handleDeploy"
          >
            部署网站
          </a-button>
          <a-button type="text" class="menu-btn">
            ☰ 菜单
          </a-button>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 左侧对话区域 -->
        <div class="chat-panel">
          <div class="chat-header">
            <h3>用户消息</h3>
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
              <a-button 
                type="dashed" 
                :loading="historyLoading"
                @click="loadMoreHistory"
                class="load-more-btn"
                block
              >
                <template #icon v-if="!historyLoading">📜</template>
                {{ historyLoading ? '加载中...' : '加载更多历史消息' }}
              </a-button>
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
              <div class="status-content">
                <a-spin size="small" />
                <span class="status-text">AI 正在生成中，请稍候...</span>
                <div class="typing-dots">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>
              </div>
            </div>

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
                <a-button type="text" size="small" :disabled="!isOwner">📎 上传</a-button>
                <a-button type="text" size="small" :disabled="!isOwner">💾 保存</a-button>
                <a-button type="text" size="small" :disabled="!isOwner">💬 历史</a-button>
              </div>
              <a-button
                type="primary"
                :loading="isGenerating"
                :disabled="!userInput.trim() || !isOwner"
                @click="sendMessage(userInput)"
                class="send-btn"
              >
                {{ isGenerating ? '生成中...' : '发送' }}
              </a-button>
            </div>
          </div>
        </div>

        <!-- 右侧预览区域 -->
        <div class="preview-panel">
          <div class="preview-header">
            <h3>生成后的网页展示</h3>
            <div class="preview-actions">
              <a-button type="text" size="small">🔄 刷新</a-button>
              <a-button type="text" size="small">📱 响应式</a-button>
            </div>
          </div>

          <div class="preview-content">
            <!-- 生成中状态 -->
            <div v-if="isGenerating && !showPreview" class="generating-preview">
              <div class="generating-content">
                <div class="loading-animation">
                  <div class="loading-dots">
                    <div class="dot"></div>
                    <div class="dot"></div>
                    <div class="dot"></div>
                  </div>
                </div>
                <h3>正在生成网站...</h3>
                <p>AI正在根据您的需求创建网站，请稍候</p>
                <div class="progress-info">
                  <div class="progress-step active">
                    <span class="step-icon">📝</span>
                    <span>分析需求</span>
                  </div>
                  <div class="progress-step active">
                    <span class="step-icon">🎨</span>
                    <span>设计界面</span>
                  </div>
                  <div class="progress-step active">
                    <span class="step-icon">💻</span>
                    <span>生成代码</span>
                  </div>
                  <div class="progress-step">
                    <span class="step-icon">🚀</span>
                    <span>完成部署</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 初始等待状态 -->
            <div v-else-if="!showPreview && !isGenerating" class="preview-placeholder">
              <div class="placeholder-content">
                <div class="welcome-icon">🚀</div>
                <h3>准备生成您的网站</h3>
                <p>请在左侧描述您想要创建的网站类型和功能需求</p>
                <div class="placeholder-features">
                  <div class="feature-item">
                    <span class="feature-icon">⚡</span>
                    <span>快速生成</span>
                  </div>
                  <div class="feature-item">
                    <span class="feature-icon">🎨</span>
                    <span>精美设计</span>
                  </div>
                  <div class="feature-item">
                    <span class="feature-icon">📱</span>
                    <span>响应式布局</span>
                  </div>
                  <div class="feature-item">
                    <span class="feature-icon">🔧</span>
                    <span>易于定制</span>
                  </div>
                </div>
                <div class="getting-started">
                  <p class="tip">💡 提示：描述越详细，生成效果越好哦！</p>
                </div>
              </div>
            </div>

            <iframe
              v-if="showPreview"
              :src="previewUrl"
              class="preview-iframe"
              frameborder="0"
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
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  margin: -24px;
}

.full-height {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.top-bar {
  background: white;
  border-bottom: 1px solid #e8e8e8;
  padding: 8px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.app-icon {
  font-size: 20px;
}

.code-type-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 4px 12px;
  font-size: 13px;
}

.top-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.main-content {
  flex: 1;
  display: flex;
  min-height: 0;
  height: calc(100vh - 60px); /* 减去顶部栏高度 */
}

.chat-panel {
  width: 45%;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0; /* 允许flex子项收缩 */
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.chat-header h3 {
  margin: 0;
  color: #333;
}

.chat-messages {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  background: #f9f9f9;
  position: relative;
  scroll-behavior: smooth;
  min-height: 0; /* 允许收缩 */
}

.message {
  margin-bottom: 24px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  margin-left: 40px;
  padding: 12px 16px;
  border-radius: 8px;
  max-width: 80%;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
  margin-left: auto;
  margin-right: 40px;
}

.ai-message .message-content {
  background: white;
  border: 1px solid #e8e8e8;
}

.ai-content {
  margin: 0;
  padding: 0;
  background: transparent;
  border: none;
}

/* AI消息内容的Markdown样式优化 */
.ai-message .ai-content :deep(.markdown-content) {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.ai-message .ai-content :deep(pre) {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 12px;
  margin: 8px 0;
  font-size: 13px;
}

.ai-message .ai-content :deep(code) {
  background: #f1f3f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 13px;
}

.user-content {
  font-size: 14px;
  line-height: 1.6;
}

/* 移除旧的生成指示器样式，已替换为状态提示条 */

.scroll-to-bottom {
  position: absolute;
  bottom: 16px;
  right: 16px;
  z-index: 10;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-input {
  border-top: 1px solid #e8e8e8;
  padding: 16px 20px;
  background: white;
  flex-shrink: 0; /* 防止输入区域被压缩 */
  position: relative;
}

/* 生成状态提示条样式 */
.generating-status-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 16px;
  margin: -16px -20px 12px -20px;
  border-radius: 0;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.status-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.status-text {
  flex: 1;
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  animation: typingBounce 1.4s infinite ease-in-out both;
}

.typing-dots .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dots .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.input-field {
  margin-bottom: 12px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.send-btn {
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.preview-panel {
  width: 55%;
  background: white;
  display: flex;
  flex-direction: column;
}

.preview-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  color: #333;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-content {
  flex: 1;
  position: relative;
  background: #f5f5f5;
}

.preview-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.placeholder-content {
  text-align: center;
  max-width: 400px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.placeholder-content h3 {
  font-size: 24px;
  margin-bottom: 12px;
  color: #333;
  font-weight: 600;
}

.placeholder-content p {
  color: #666;
  margin-bottom: 32px;
  font-size: 16px;
  line-height: 1.5;
}

.placeholder-features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 32px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 14px;
  color: #495057;
}

.feature-icon {
  font-size: 18px;
}

.getting-started {
  padding: 16px;
  background: linear-gradient(135deg, #e3f2fd, #f3e5f5);
  border-radius: 12px;
  border: 1px solid #e1e5e9;
}

.tip {
  margin: 0;
  color: #6366f1;
  font-size: 14px;
  font-weight: 500;
}

/* 生成中状态样式 */
.generating-preview {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.generating-content {
  text-align: center;
  max-width: 400px;
}

.loading-animation {
  margin-bottom: 24px;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.generating-content h3 {
  font-size: 24px;
  margin-bottom: 12px;
  color: #1e293b;
  font-weight: 600;
}

.generating-content p {
  color: #64748b;
  margin-bottom: 32px;
  font-size: 16px;
  line-height: 1.5;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border-radius: 12px;
  background: white;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  flex: 1;
}

.progress-step.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.step-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.progress-step span:last-child {
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
}

.progress-step.active span:last-child {
  color: #4338ca;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 加载更多按钮样式 */
.load-more-container {
  padding: 16px 20px 8px 20px;
  text-align: center;
}

.load-more-btn {
  border-radius: 8px;
  border: 2px dashed #d9d9d9;
  background: #fafafa;
  color: #666;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.load-more-btn:hover {
  border-color: #40a9ff;
  background: #f0f8ff;
  color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.15);
}

.load-more-btn:active {
  transform: translateY(0);
}

.disabled-input {
  cursor: not-allowed !important;
  background-color: #f5f5f5 !important;
  opacity: 0.6;
}

.disabled-input:hover {
  border-color: #d9d9d9 !important;
}

</style>
