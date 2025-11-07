/**
 * Element Selector
 * 注入到预览网站的脚本，处理元素选择交互
 * 这个文件会被转换为字符串注入到 iframe 中
 */

// 消息类型（与 visualEditor.ts 保持一致）
enum MessageType {
  ENTER_EDIT_MODE = 'ENTER_EDIT_MODE',
  EXIT_EDIT_MODE = 'EXIT_EDIT_MODE',
  ELEMENT_SELECTED = 'ELEMENT_SELECTED',
  CLEAR_SELECTION = 'CLEAR_SELECTION'
}

// 元素信息接口
interface ElementInfo {
  tagName: string
  className: string
  id: string
  textContent: string
  xpath: string
}

// 消息接口
interface EditorMessage {
  type: MessageType
  data?: ElementInfo
  source: 'main' | 'preview'
  timestamp?: number
}

/**
 * Element Selector 类
 */
class ElementSelector {
  private isActive: boolean = false
  private hoveredElement: HTMLElement | null = null
  private selectedElement: HTMLElement | null = null
  
  // CSS 类名
  private readonly HOVER_CLASS = 'visual-editor-hover'
  private readonly SELECTED_CLASS = 'visual-editor-selected'

  constructor() {
    // 注入样式
    this.injectStyles()
    
    // 监听来自父窗口的消息
    window.addEventListener('message', this.handleParentMessage.bind(this))
    
    console.log('[ElementSelector] Initialized in iframe')
  }

  /**
   * 注入样式到页面
   */
  private injectStyles(): void {
    const style = document.createElement('style')
    style.textContent = `
      .${this.HOVER_CLASS} {
        outline: 2px dashed #1890ff !important;
        outline-offset: 2px;
        cursor: pointer !important;
        transition: outline 0.2s ease;
      }
      
      .${this.SELECTED_CLASS} {
        outline: 3px solid #1890ff !important;
        outline-offset: 2px;
        background-color: rgba(24, 144, 255, 0.05) !important;
        transition: all 0.2s ease;
      }
    `
    document.head.appendChild(style)
  }

  /**
   * 处理来自父窗口的消息
   */
  private handleParentMessage(event: MessageEvent): void {
    if (!event.data || typeof event.data !== 'object') {
      return
    }
    
    const message = event.data as EditorMessage
    
    // 只处理来自主网站的消息
    if (message.source !== 'main') {
      return
    }
    
    console.log('[ElementSelector] Received message:', message.type)
    
    switch (message.type) {
      case MessageType.ENTER_EDIT_MODE:
        this.activate()
        break
      case MessageType.EXIT_EDIT_MODE:
        this.deactivate()
        break
      case MessageType.CLEAR_SELECTION:
        this.removeSelectedBorder()
        break
    }
  }

  /**
   * 激活选择器
   */
  activate(): void {
    if (this.isActive) return
    
    this.isActive = true
    
    // 添加事件监听器
    document.addEventListener('mouseover', this.handleMouseOver.bind(this), true)
    document.addEventListener('mouseout', this.handleMouseOut.bind(this), true)
    document.addEventListener('click', this.handleClick.bind(this), true)
    
    console.log('[ElementSelector] Activated')
  }

  /**
   * 停用选择器
   */
  deactivate(): void {
    if (!this.isActive) return
    
    this.isActive = false
    
    // 移除事件监听器
    document.removeEventListener('mouseover', this.handleMouseOver.bind(this), true)
    document.removeEventListener('mouseout', this.handleMouseOut.bind(this), true)
    document.removeEventListener('click', this.handleClick.bind(this), true)
    
    // 清除所有边框
    this.removeHoverBorder(this.hoveredElement)
    this.removeSelectedBorder()
    
    console.log('[ElementSelector] Deactivated')
  }

  /**
   * 处理鼠标悬浮
   */
  private handleMouseOver(event: MouseEvent): void {
    if (!this.isActive) return
    
    const target = event.target as HTMLElement
    
    // 忽略已选中的元素
    if (target === this.selectedElement) return
    
    // 移除之前的悬浮边框
    if (this.hoveredElement && this.hoveredElement !== target) {
      this.removeHoverBorder(this.hoveredElement)
    }
    
    this.hoveredElement = target
    this.addHoverBorder(target)
  }

  /**
   * 处理鼠标移出
   */
  private handleMouseOut(event: MouseEvent): void {
    if (!this.isActive) return
    
    const target = event.target as HTMLElement
    
    // 只移除当前悬浮元素的边框
    if (target === this.hoveredElement) {
      this.removeHoverBorder(target)
      this.hoveredElement = null
    }
  }

  /**
   * 处理点击
   */
  private handleClick(event: MouseEvent): void {
    if (!this.isActive) return
    
    // 阻止默认行为和事件冒泡
    event.preventDefault()
    event.stopPropagation()
    
    const target = event.target as HTMLElement
    
    // 移除之前的选中边框
    this.removeSelectedBorder()
    
    // 移除悬浮边框
    this.removeHoverBorder(target)
    
    // 添加选中边框
    this.selectedElement = target
    this.addSelectedBorder(target)
    
    // 提取元素信息并发送到父窗口
    const elementInfo = this.extractElementInfo(target)
    this.sendToParent({
      type: MessageType.ELEMENT_SELECTED,
      data: elementInfo,
      source: 'preview',
      timestamp: Date.now()
    })
    
    console.log('[ElementSelector] Element selected:', elementInfo)
  }

  /**
   * 添加悬浮边框
   */
  private addHoverBorder(element: HTMLElement): void {
    if (!element) return
    element.classList.add(this.HOVER_CLASS)
  }

  /**
   * 移除悬浮边框
   */
  private removeHoverBorder(element: HTMLElement | null): void {
    if (!element) return
    element.classList.remove(this.HOVER_CLASS)
  }

  /**
   * 添加选中边框
   */
  private addSelectedBorder(element: HTMLElement): void {
    if (!element) return
    element.classList.add(this.SELECTED_CLASS)
  }

  /**
   * 移除选中边框
   */
  private removeSelectedBorder(): void {
    if (!this.selectedElement) return
    this.selectedElement.classList.remove(this.SELECTED_CLASS)
    this.selectedElement = null
  }

  /**
   * 提取元素信息
   */
  private extractElementInfo(element: HTMLElement): ElementInfo {
    const tagName = element.tagName
    const className = element.className || ''
    const id = element.id || ''
    const textContent = (element.textContent || '').trim().substring(0, 200)
    const xpath = this.getXPath(element)
    
    return {
      tagName,
      className,
      id,
      textContent,
      xpath
    }
  }

  /**
   * 生成元素的 XPath 路径
   */
  private getXPath(element: HTMLElement): string {
    // 如果元素有 ID，直接使用 ID
    if (element.id) {
      return `//*[@id="${element.id}"]`
    }
    
    const parts: string[] = []
    let current: HTMLElement | null = element
    
    while (current && current.nodeType === Node.ELEMENT_NODE) {
      let index = 0
      let sibling = current.previousSibling
      
      // 计算同名兄弟元素的索引
      while (sibling) {
        if (sibling.nodeType === Node.ELEMENT_NODE && 
            sibling.nodeName === current.nodeName) {
          index++
        }
        sibling = sibling.previousSibling
      }
      
      const tagName = current.nodeName.toLowerCase()
      const pathIndex = index > 0 ? `[${index + 1}]` : ''
      parts.unshift(`${tagName}${pathIndex}`)
      
      current = current.parentElement
    }
    
    return parts.length ? `/${parts.join('/')}` : ''
  }

  /**
   * 发送消息到父窗口
   */
  private sendToParent(message: EditorMessage): void {
    try {
      window.parent.postMessage(message, '*')
      console.log('[ElementSelector] Sent message to parent:', message.type)
    } catch (error) {
      console.error('[ElementSelector] Failed to send message:', error)
    }
  }
}

// 自动初始化
new ElementSelector()
