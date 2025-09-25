<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { 
  listAppVoByPageAdmin, 
  deleteAppByAdmin, 
  updateAppByAdmin 
} from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { CODE_GEN_TYPE_OPTIONS, getCodeGenTypeLabel } from '@/constants/codeGenType'
import SearchForm from '@/components/SearchForm.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 表格数据
const loading = ref(false)
const dataSource = ref<API.AppVO[]>([])
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
    key: 'appName',
    label: '应用名称',
    type: 'input' as const,
    placeholder: '请输入应用名称',
    prefix: '🔍',
    width: '220px'
  },
  {
    key: 'userId',
    label: '用户ID',
    type: 'input' as const,
    placeholder: '请输入用户ID',
    prefix: '👤',
    width: '180px'
  },
  {
    key: 'codeGenType',
    label: '代码类型',
    type: 'select' as const,
    placeholder: '请选择代码类型',
    width: '160px',
    options: CODE_GEN_TYPE_OPTIONS
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
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    ellipsis: true,
    width: 250,
    align: 'left' as const,
  },
  {
    title: '封面预览',
    dataIndex: 'cover',
    key: 'cover',
    width: 100,
    align: 'center' as const,
  },
  {
    title: '创建用户',
    dataIndex: 'user',
    key: 'user',
    width: 140,
    align: 'center' as const,
  },
  {
    title: '优先级状态',
    dataIndex: 'priority',
    key: 'priority',
    width: 120,
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
    width: 160,
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
    const res = await listAppVoByPageAdmin({
      appQueryRequest: {
        pageNum: pagination.value.current,
        pageSize: pagination.value.pageSize,
        appName: currentSearchParams.value.appName || undefined,
        userId: currentSearchParams.value.userId || undefined,
        codeGenType: currentSearchParams.value.codeGenType || undefined,
      }
    })
    
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

// 删除应用
const handleDelete = async (record: API.AppVO) => {
  try {
    const res = await deleteAppByAdmin({ id: record.id! })
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

// 切换精选状态
const handleToggleFeatured = async (record: API.AppVO) => {
  const isFeatured = record.priority === 99
  const newPriority = isFeatured ? 0 : 99
  const actionText = isFeatured ? '取消精选' : '设为精选'
  
  try {
    const res = await updateAppByAdmin(
      { id: record.id! },
      {
        id: record.id,
        appName: record.appName,
        cover: record.cover,
        priority: newPriority,
      }
    )
    if (res.data.code === 0) {
      message.success(`${actionText}成功`)
      loadData()
    } else {
      message.error(`${actionText}失败：` + res.data.message)
    }
  } catch (error) {
    message.error(`${actionText}失败`)
  }
}

// 编辑应用
const handleEdit = (record: API.AppVO) => {
  router.push(`/app/edit/${record.id}`)
}

// 查看应用
const handleView = (record: API.AppVO) => {
  router.push(`/app/chat/${record.id}`)
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
  <div class="app-manage-page">
    <div class="page-header">
      <h2>应用管理</h2>
      <p>管理系统中的所有应用</p>
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
        :scroll="{ x: 1170 }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'id'">
            <a-tag color="blue" class="id-tag">
              {{ record.id }}
            </a-tag>
          </template>

          <template v-if="column.key === 'appName'">
            <div class="app-name-cell">
              <div class="app-name-main">{{ record.appName }}</div>
              <div class="app-meta">
                <a-tag v-if="record.codeGenType" size="small" color="geekblue">
                  {{ getCodeGenTypeLabel(record.codeGenType) }}
                </a-tag>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'cover'">
            <div class="cover-cell">
              <a-image
                v-if="record.cover"
                :src="record.cover"
                :width="60"
                :height="40"
                style="object-fit: cover; border-radius: 8px"
                :preview="{ mask: '预览' }"
              />
              <div v-else class="no-cover-placeholder">
                <div class="placeholder-icon">🖼️</div>
                <span class="placeholder-text">暂无封面</span>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'user'">
            <div class="user-info">
              <a-avatar :size="28" :src="record.user?.userAvatar">
                {{ record.user?.userName?.[0] }}
              </a-avatar>
              <div class="user-details">
                <div class="user-name">{{ record.user?.userName || '未知用户' }}</div>
                <div class="user-role">创建者</div>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'priority'">
            <div class="priority-cell">
              <a-tag v-if="record.priority === 99" color="red" class="featured-tag">
                <template #icon>⭐</template>
                精选
              </a-tag>
              <a-tag v-else-if="record.priority >= 50" color="orange" class="high-priority-tag">
                高优先级 {{ record.priority }}
              </a-tag>
              <a-tag v-else-if="record.priority > 0" color="blue" class="normal-priority-tag">
                {{ record.priority }}
              </a-tag>
              <a-tag v-else color="default" class="default-priority-tag">
                默认
              </a-tag>
            </div>
          </template>

          <template v-if="column.key === 'action'">
            <div class="action-buttons">
              <a-tooltip title="查看应用详情">
                <a-button 
                  type="primary"
                  ghost
                  size="small"
                  class="action-btn view-btn"
                  @click="handleView(record)"
                >
                  <template #icon>
                    👁️
                  </template>
                </a-button>
              </a-tooltip>
              
              <a-tooltip title="编辑应用">
                <a-button 
                  type="default"
                  size="small"
                  class="action-btn edit-btn"
                  @click="handleEdit(record)"
                >
                  <template #icon>
                    ✏️
                  </template>
                </a-button>
              </a-tooltip>
              
              <a-tooltip :title="record.priority === 99 ? '取消精选' : '设为精选'">
                <a-button 
                  :type="record.priority === 99 ? 'primary' : 'default'"
                  size="small"
                  :class="['action-btn', record.priority === 99 ? 'unfeatured-btn' : 'featured-btn']"
                  @click="handleToggleFeatured(record)"
                >
                  <template #icon>
                    {{ record.priority === 99 ? '⭐' : '☆' }}
                  </template>
                </a-button>
              </a-tooltip>
              
              <a-popconfirm
                title="确定要删除这个应用吗？"
                description="删除后将无法恢复，请谨慎操作！"
                ok-text="确定删除"
                cancel-text="取消"
                ok-type="danger"
                @confirm="handleDelete(record)"
              >
                <a-tooltip title="删除应用">
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
.app-manage-page {
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

/* 应用名称单元格样式 */
.app-name-cell {
  text-align: left;
  padding: 8px 12px 8px 0;
  min-width: 200px;
}

.app-name-main {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
  line-height: 1.4;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.app-id {
  font-size: 11px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

/* 封面单元格样式 */
.cover-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  background: #fafafa;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  width: 60px;
  height: 40px;
  justify-content: center;
}

.placeholder-icon {
  font-size: 16px;
  opacity: 0.6;
}

.placeholder-text {
  font-size: 10px;
  color: #999;
  text-align: center;
}

.text-placeholder {
  color: #999;
  font-style: italic;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.user-role {
  font-size: 11px;
  color: #999;
  background: #f0f7ff;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #d6e4ff;
}

/* 优先级标签样式 */
.priority-cell {
  display: flex;
  justify-content: center;
}

.featured-tag {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.3);
}

.high-priority-tag {
  background: linear-gradient(135deg, #ffa726, #ff7043);
  border: none;
  color: white;
  font-weight: 500;
  border-radius: 6px;
}

.normal-priority-tag {
  background: linear-gradient(135deg, #42a5f5, #1e88e5);
  border: none;
  color: white;
  font-weight: 500;
  border-radius: 6px;
}

.default-priority-tag {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  color: #666;
  border-radius: 6px;
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

.view-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a6fd8, #6a42a0);
}

.edit-btn {
  background: #fff;
  border-color: #40a9ff;
  color: #40a9ff;
}

.edit-btn:hover {
  background: #40a9ff;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
}

.featured-btn {
  background: #fff;
  border-color: #faad14;
  color: #faad14;
}

.featured-btn:hover {
  background: #faad14;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.3);
}

.unfeatured-btn {
  background: linear-gradient(135deg, #faad14, #fa8c16);
  border-color: #faad14;
  color: white;
}

.unfeatured-btn:hover {
  background: linear-gradient(135deg, #fa8c16, #d46b08);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.4);
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

/* 图片样式优化 */
:deep(.ant-image) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.ant-image:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  
  .app-name-main {
    font-size: 14px;
  }
  
  .user-info {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .search-form .ant-form-item {
    width: 100%;
  }
  
  .app-manage-page {
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
