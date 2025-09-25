<script setup lang="ts">
import { ref, watch } from 'vue'

interface SearchField {
  key: string
  label: string
  type: 'input' | 'select'
  placeholder?: string
  options?: Array<{ label: string; value: string | number }>
  prefix?: string
  width?: string
}

interface Props {
  fields: SearchField[]
  loading?: boolean
}

interface Emits {
  (e: 'search', values: Record<string, any>): void
  (e: 'reset'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

// 表单数据
const formData = ref<Record<string, any>>({})

// 初始化表单数据
const initFormData = () => {
  const data: Record<string, any> = {}
  props.fields.forEach(field => {
    data[field.key] = field.type === 'select' ? undefined : ''
  })
  formData.value = data
}

// 监听字段变化，重新初始化表单
watch(() => props.fields, initFormData, { immediate: true })

// 搜索
const handleSearch = () => {
  emit('search', { ...formData.value })
}

// 重置
const handleReset = () => {
  initFormData()
  emit('reset')
}

// 回车搜索
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    handleSearch()
  }
}
</script>

<template>
  <a-card class="search-card">
    <a-form layout="inline" :model="formData" class="search-form">
      <a-form-item 
        v-for="field in fields" 
        :key="field.key"
        :label="field.label"
      >
        <!-- 输入框 -->
        <a-input 
          v-if="field.type === 'input'"
          v-model:value="formData[field.key]" 
          :placeholder="field.placeholder"
          :style="{ width: field.width || '220px' }"
          allow-clear
          @keydown="handleKeyDown"
        >
          <template v-if="field.prefix" #prefix>
            {{ field.prefix }}
          </template>
        </a-input>
        
        <!-- 选择框 -->
        <a-select 
          v-else-if="field.type === 'select'"
          v-model:value="formData[field.key]" 
          :placeholder="field.placeholder"
          :style="{ width: field.width || '160px' }"
          allow-clear
        >
          <a-select-option 
            v-for="option in field.options" 
            :key="option.value" 
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item>
        <a-space>
          <a-button 
            type="primary" 
            :loading="loading"
            @click="handleSearch" 
            class="search-btn"
          >
            <template #icon>🔍</template>
            搜索
          </a-button>
          <a-button @click="handleReset" class="reset-btn">
            <template #icon>🔄</template>
            重置
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<style scoped>
.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

.search-form {
  padding: 8px 0;
}

.search-form .ant-form-item {
  margin-bottom: 16px;
}

.search-form .ant-form-item-label > label {
  font-weight: 500;
  color: #333;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.reset-btn {
  border-radius: 8px;
  border-color: #d9d9d9;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .search-form .ant-form-item {
    width: 100%;
  }
}
</style>
