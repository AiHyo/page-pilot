// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /api/chat-history */
export async function addChatHistory(
  body: API.ChatHistoryAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/api/chat-history', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /api/chat-history/${param0} */
export async function deleteChatHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteChatHistoryParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/api/chat-history/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /api/chat-history/admin/list/page/vo */
export async function listAllChatHistoryByPageForAdmin(
  body: API.ChatHistoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistory>('/api/chat-history/admin/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /api/chat-history/app/${param0} */
export async function listAppChatHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listAppChatHistoryParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponsePageChatHistory>(`/api/chat-history/app/${param0}`, {
    method: 'GET',
    params: {
      // pageSize has a default value: 10
      pageSize: '10',
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /api/chat-history/app/${param0} */
export async function deleteChatHistoryByAppId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteChatHistoryByAppIdParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/api/chat-history/app/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /api/chat-history/app/${param0}/latest */
export async function getLatestChatHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLatestChatHistoryParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseListChatHistoryVO>(`/api/chat-history/app/${param0}/latest`, {
    method: 'GET',
    params: {
      // limit has a default value: 10
      limit: '10',
      ...queryParams,
    },
    ...(options || {}),
  })
}
