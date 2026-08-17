export type SseEventHandler = (eventName: string, data: string) => void

/**
 * POST a JSON body and parse an SSE response from the ReadableStream.
 */
export async function postSse(
  url: string,
  body: unknown,
  onEvent: SseEventHandler,
  signal?: AbortSignal,
): Promise<void> {
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    credentials: 'include',
    body: JSON.stringify(body),
    signal,
  })

  if (!response.ok) {
    throw new Error(await readHttpErrorMessage(response))
  }
  if (!response.body) {
    throw new Error('空的生成响应')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      buffer += decoder.decode()
      flushSseBuffer(buffer, onEvent, true)
      break
    }
    buffer += decoder.decode(value, { stream: true })
    buffer = flushSseBuffer(buffer, onEvent, false)
  }
}

async function readHttpErrorMessage(response: Response): Promise<string> {
  const fallback = `请求失败 (${response.status})`
  try {
    const text = await response.text()
    const json = JSON.parse(text) as { message?: string }
    return json.message || fallback
  } catch {
    return fallback
  }
}

function flushSseBuffer(buffer: string, onEvent: SseEventHandler, flushTail: boolean): string {
  const normalized = buffer.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  let rest = normalized
  let idx = rest.indexOf('\n\n')
  while (idx >= 0) {
    dispatchSseBlock(rest.slice(0, idx), onEvent)
    rest = rest.slice(idx + 2)
    idx = rest.indexOf('\n\n')
  }
  if (flushTail && rest.trim()) {
    dispatchSseBlock(rest, onEvent)
    return ''
  }
  return rest
}

function dispatchSseBlock(raw: string, onEvent: SseEventHandler): void {
  if (!raw.trim()) {
    return
  }
  let eventName = 'message'
  const dataLines: string[] = []
  for (const line of raw.split('\n')) {
    if (!line || line.startsWith(':')) {
      continue
    }
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
      continue
    }
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  }
  if (eventName === 'message' && dataLines.length === 0) {
    return
  }
  onEvent(eventName, dataLines.join('\n'))
}
