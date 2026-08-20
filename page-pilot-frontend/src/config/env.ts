import { CODE_GEN_TYPE } from '@/constants/codeGenType'

/**
 * 环境配置
 */
export const ENV_CONFIG = {
  // 应用部署域名（本地多项目时覆盖为 http://localhost:8124/api/static）
  DEPLOY_DOMAIN: import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost:8124/api/static',
  
  // 预览走 Vite /api 代理，避免写死 8123（与 z-ai-agent 冲突）
  PREVIEW_DOMAIN: import.meta.env.VITE_PREVIEW_DOMAIN || '',
  
  // 空则走相对路径 /api，由 vite proxy 转到后端
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || '/api'
}

/**
 * 获取应用部署URL
 * @param deployKey 部署密钥
 * @returns 完整的部署URL
 */
export const getDeployUrl = (deployKey: string): string => {
  return `${ENV_CONFIG.DEPLOY_DOMAIN}/${deployKey}/`
}

/**
 * 获取应用预览URL
 * @param codeGenType 代码生成类型
 * @param appId 应用ID
 * @returns 完整的预览URL
 */
export const getPreviewUrl = (codeGenType: string, appId: string): string => {
  const previewHost = ENV_CONFIG.PREVIEW_DOMAIN
  const baseUrl = previewHost
    ? `${previewHost.replace(/\/$/, '')}/api/static/${codeGenType}_${appId}/`
    : `/api/static/${codeGenType}_${appId}/`
  
  // 如果是 Vue 项目，浏览地址需要添加 dist 后缀
  if (codeGenType === CODE_GEN_TYPE.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}

/**
 * 获取API完整URL
 * @param path API路径
 * @returns 完整的API URL
 */
export const getApiUrl = (path: string): string => {
  return `${ENV_CONFIG.API_BASE_URL}${path.startsWith('/') ? path : '/' + path}`
}
