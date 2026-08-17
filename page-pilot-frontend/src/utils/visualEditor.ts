/**
 * Visual Editor Manager
 * 与服务端注入的 iframe hook 通信；不读取 contentDocument。
 */

export enum MessageType {
  ENTER_EDIT_MODE = 'ENTER_EDIT_MODE',
  EXIT_EDIT_MODE = 'EXIT_EDIT_MODE',
  ELEMENT_SELECTED = 'ELEMENT_SELECTED',
  CLEAR_SELECTION = 'CLEAR_SELECTION'
}

export interface ElementInfo {
  tagName: string
  className: string
  id: string
  textContent: string
  xpath: string
}

export interface EditorMessage {
  type: MessageType
  data?: ElementInfo
  source: 'main' | 'preview'
  timestamp?: number
}

export class VisualEditorManager {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode: boolean = false
  private selectedElement: ElementInfo | null = null
  private messageHandler: ((message: EditorMessage) => void) | null = null
  private boundHandleMessage: ((event: MessageEvent) => void) | null = null

  init(iframe: HTMLIFrameElement, onMessage: (message: EditorMessage) => void): void {
    this.iframe = iframe
    this.messageHandler = onMessage
    this.boundHandleMessage = this.handleMessage.bind(this)
    window.addEventListener('message', this.boundHandleMessage)
  }

  enterEditMode(): void {
    if (!this.iframe) {
      console.warn('[VisualEditor] iframe not initialized')
      return
    }
    this.isEditMode = true
    this.sendMessage({
      type: MessageType.ENTER_EDIT_MODE,
      source: 'main',
      timestamp: Date.now()
    })
  }

  exitEditMode(): void {
    if (!this.iframe) {
      console.warn('[VisualEditor] iframe not initialized')
      return
    }
    this.isEditMode = false
    this.selectedElement = null
    this.sendMessage({
      type: MessageType.EXIT_EDIT_MODE,
      source: 'main',
      timestamp: Date.now()
    })
  }

  clearSelection(): void {
    if (!this.iframe) {
      console.warn('[VisualEditor] iframe not initialized')
      return
    }
    this.selectedElement = null
    this.sendMessage({
      type: MessageType.CLEAR_SELECTION,
      source: 'main',
      timestamp: Date.now()
    })
  }

  getSelectedElement(): ElementInfo | null {
    return this.selectedElement
  }

  destroy(): void {
    if (this.boundHandleMessage) {
      window.removeEventListener('message', this.boundHandleMessage)
    }
    this.iframe = null
    this.messageHandler = null
    this.boundHandleMessage = null
    this.isEditMode = false
    this.selectedElement = null
  }

  private sendMessage(message: EditorMessage): void {
    if (!this.iframe || !this.iframe.contentWindow) {
      console.warn('[VisualEditor] Cannot send message: iframe not ready')
      return
    }
    try {
      this.iframe.contentWindow.postMessage(message, '*')
    } catch (error) {
      console.error('[VisualEditor] Failed to send message:', error)
    }
  }

  private handleMessage(event: MessageEvent): void {
    if (!this.iframe?.contentWindow || event.source !== this.iframe.contentWindow) {
      return
    }
    if (!event.data || typeof event.data !== 'object') {
      return
    }
    const message = event.data as EditorMessage
    if (message.source !== 'preview') {
      return
    }
    if (message.type === MessageType.ELEMENT_SELECTED && message.data) {
      this.selectedElement = message.data
    }
    if (this.messageHandler) {
      try {
        this.messageHandler(message)
      } catch (error) {
        console.error('[VisualEditor] Error in message handler:', error)
      }
    }
  }
}
