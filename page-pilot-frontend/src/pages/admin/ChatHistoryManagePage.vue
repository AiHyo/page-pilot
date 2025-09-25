<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { 
  listAllChatHistoryByPageForAdmin, 
  deleteChatHistory,
  deleteChatHistoryByAppId
} from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import SearchForm from '@/components/SearchForm.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 表格数据
const loading = ref(false)
const dataSource = ref<API.ChatHistory[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
})

// 搜索表单字段配置
const searchFields = [
  {
    key: 'message',
    label: '消息内容',
    type: 'input' as const,
    placeholder: '请输入消息内容',
    prefix: '🔍',
    width: '220px'
  },
  {
    key: 'messageType',
    label: '消息类型',
    type: 'select' as const,
    placeholder: '请选择消息类型',
    width: '140px',
    options: [
      { label: '用户消息', value: 'USER' },
      { label: 'AI消息', value: 'AI' },
    ]
  },
  {
    key: 'appId',
    label: '应用ID',
    type: 'input' as const,
    placeholder: '请输入应用ID',
    prefix: '🤖',
    width: '140px'
  },
  {
    key: 'userId',
    label: '用户ID',
    type: 'input' as const,
    placeholder: '请输入用户ID',
    prefix: '👤',
    width: '140px'
  }
]

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 140,
    align: 'center' as const,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    ellipsis: true,
    width: 300,
    align: 'left' as const,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 100,
    align: 'center' as const,
  },
  {
    title: '所属应用',
    dataIndex: 'appId',
    key: 'appId',
    width: 140,
    align: 'center' as const,
  },
  {
    title: '发送用户',
    dataIndex: 'userId',
    key: 'userId',
    width: 140,
    align: 'center' as const,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
    align: 'center' as const,
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right' as const,
    align: 'center' as const,
  },
]

// 当前搜索条件
const currentSearchParams = ref<Record<string, any>>({})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      message: currentSearchParams.value.message || undefined,
      messageType: currentSearchParams.value.messageType || undefined,
      appId: currentSearchParams.value.appId || undefined,
      userId: currentSearchParams.value.userId || undefined,
    }

    const res = await listAllChatHistoryByPageForAdmin(params)
    
    if (res.data.code === 0 && res.data.data) {
      dataSource.value = res.data.data.records || []
      pagination.value.total = res.data.data.totalRow || 0
    } else {
      message.error('加载数据失败：' + res.data.message)
    }
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = (searchParams: Record<string, any>) => {
  currentSearchParams.value = { ...searchParams }
  pagination.value.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  currentSearchParams.value = {}
  pagination.value.current = 1
  loadData()
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

// 删除单条对话记录
const handleDelete = async (record: API.ChatHistory) => {
  try {
    const res = await deleteChatHistory({ id: record.id! })
    if (res.data.code === 0) {
      message.success('删除成功')
      loadData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    message.error('删除失败')
  }
}

// 删除应用的所有对话记录
const handleDeleteByAppId = async (appId: number) => {
  try {
    const res = await deleteChatHistoryByAppId({ appId })
    if (res.data.code === 0) {
      message.success('删除成功')
      loadData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    message.error('删除失败')
  }
}

// 查看应用详情
const handleViewApp = (appId: number) => {
  router.push(`/app/chat/${appId}`)
}

// 格式化消息内容
const formatMessage = (message: string) => {
  if (!message) return ''
  return message.length > 100 ? message.substring(0, 100) + '...' : message
}

onMounted(() => {
  // 检查权限
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('无权限访问')
    router.push('/')
    return
  }
  loadData()
})
</script>

<template>
  <div class="chat-history-manage-page">
    <div class="page-header">
      <h2>对话管理</h2>
      <p>管理系统中的所有对话历史记录</p>
    </div>

    <!-- 搜索表单 -->
    <SearchForm
      :fields="searchFields"
      :loading="loading"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 数据表格 -->
    <a-card>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'id'">
            <a-tag color="blue" class="id-tag">
              {{ record.id }}
            </a-tag>
          </template>

          <template v-if="column.key === 'message'">
            <div class="message-cell">
              <div class="message-content">{{ formatMessage(record.message) }}</div>
              <a-tooltip v-if="record.message && record.message.length > 100" :title="record.message">
                <a-button type="link" size="small" class="view-full-btn">查看全文</a-button>
              </a-tooltip>
            </div>
          </template>

          <template v-if="column.key === 'messageType'">
            <a-tag 
              :color="record.messageType === 'USER' ? 'blue' : 'green'" 
              class="message-type-tag"
            >
              <template #icon>
                {{ record.messageType === 'USER' ? '👤' : '🤖' }}
              </template>
              {{ record.messageType === 'USER' ? '用户' : 'AI' }}
            </a-tag>
          </template>

          <template v-if="column.key === 'appId'">
            <div class="app-info">
              <a-tag color="purple" class="app-id-tag" @click="handleViewApp(record.appId)">
                🤖 {{ record.appId }}
              </a-tag>
            </div>
          </template>

          <template v-if="column.key === 'userId'">
            <a-tag color="orange" class="user-id-tag">
              👤 {{ record.userId }}
            </a-tag>
          </template>

          <template v-if="column.key === 'createTime'">
            <div class="time-cell">
              <div class="time-main">{{ new Date(record.createTime).toLocaleDateString() }}</div>
              <div class="time-sub">{{ new Date(record.createTime).toLocaleTimeString() }}</div>
            </div>
          </template>

          <template v-if="column.key === 'action'">
            <div class="action-buttons">
              <a-popconfirm
                title="确定要删除这条对话记录吗？"
                description="删除后将无法恢复，请谨慎操作！"
                ok-text="确定删除"
                cancel-text="取消"
                ok-type="danger"
                @confirm="handleDelete(record)"
              >
                <a-tooltip title="删除对话记录">
                  <a-button 
                    type="primary"
                    danger
                    size="small"
                    class="action-btn delete-btn"
                  >
                    <template #icon>
                      🗑️
                    </template>
                  </a-button>
                </a-tooltip>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
.chat-history-manage-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  padding: 20px 0;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}


/* ID标签样式 */
.id-tag {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  font-size: 12px;
  padding: 4px 8px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 消息内容单元格样式 */
.message-cell {
  text-align: left;
  padding: 8px 12px 8px 0;
  min-width: 250px;
}

.message-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  word-break: break-word;
  margin-bottom: 4px;
}

.view-full-btn {
  padding: 0;
  height: auto;
  font-size: 12px;
  color: #1890ff;
}

/* 消息类型标签样式 */
.message-type-tag {
  font-weight: 500;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 应用信息样式 */
.app-info {
  display: flex;
  justify-content: center;
}

.app-id-tag {
  cursor: pointer;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.app-id-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.3);
}

.user-id-tag {
  font-weight: 500;
  border-radius: 6px;
}

/* 时间单元格样式 */
.time-cell {
  text-align: center;
}

.time-main {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.time-sub {
  font-size: 12px;
  color: #999;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 14px;
  border: 1px solid transparent;
}

.delete-btn {
  background: linear-gradient(135deg, #ff4d4f, #cf1322);
  border-color: #ff4d4f;
}

.delete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
  background: linear-gradient(135deg, #ff7875, #d4380d);
}

/* 表格样式优化 */
:deep(.ant-table) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-bottom: 2px solid #dee2e6;
  font-weight: 600;
  color: #495057;
  text-align: center;
}

:deep(.ant-table-tbody > tr > td) {
  text-align: center;
  vertical-align: middle;
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 8px;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f8f9fa;
}

:deep(.ant-table-tbody > tr:nth-child(even)) {
  background: #fafbfc;
}

:deep(.ant-table-tbody > tr:nth-child(even):hover) {
  background: #f1f3f4;
}

/* 分页样式优化 */
:deep(.ant-pagination) {
  margin-top: 24px;
  text-align: center;
}

:deep(.ant-pagination-item) {
  border-radius: 8px;
  border-color: #d9d9d9;
}

:deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
}

:deep(.ant-pagination-item-active a) {
  color: white;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-btn {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .message-content {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .search-form .ant-form-item {
    width: 100%;
  }
  
  .chat-history-manage-page {
    padding: 16px;
  }
  
  .page-header h2 {
    font-size: 24px;
  }
}

/* 加载状态优化 */
:deep(.ant-spin-container) {
  min-height: 400px;
}

:deep(.ant-table-placeholder) {
  padding: 60px 0;
}

:deep(.ant-empty) {
  margin: 40px 0;
}

/* 表格行高优化 */
:deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
  min-height: 60px;
}

/* 卡片样式优化 */
:deep(.ant-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

:deep(.ant-card-body) {
  padding: 20px;
}
</style>
