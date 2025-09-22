/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export const CODE_GEN_TYPE = {
  HTML: 'html',
  MULTI_FILE: 'multi_file'
} as const

/**
 * 代码生成类型选项
 */
export const CODE_GEN_TYPE_OPTIONS = [
  {
    label: '原生 HTML 模式',
    value: CODE_GEN_TYPE.HTML
  },
  {
    label: '原生多文件模式', 
    value: CODE_GEN_TYPE.MULTI_FILE
  }
]

/**
 * 根据值获取标签
 */
export const getCodeGenTypeLabel = (value: string): string => {
  const option = CODE_GEN_TYPE_OPTIONS.find(item => item.value === value)
  return option?.label || value
}

export type CodeGenType = typeof CODE_GEN_TYPE[keyof typeof CODE_GEN_TYPE]
