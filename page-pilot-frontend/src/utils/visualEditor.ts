/**
 * Visual Editor Manager
 * 管理可视化编辑功能的 iframe 通信和状态
 */

// 消息类型枚举
export enum MessageType {
  ENTER_EDIT_MODE = 'ENTER_EDIT_MODE',
  EXIT_EDIT_MODE = 'EXIT_EDIT_MODE',
  ELEMENT_SELECTED = 'ELEMENT_SELECTED',
  CLEAR_SELECTION = 'CLEAR_SELECTION'
}

// 元素信息接口
export interface ElementInfo {
  tagName: string
  className: string
  id: string
  textContent: string
  xpath: string
}

// 消息接口
export interface EditorMessage {
  type: MessageType
  data?: ElementInfo
  source: 'main' | 'preview'
  timestamp?: number
}

// Visual Editor Manager 类
export class VisualEditorManager {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode: boolean = false
  private selectedElement: ElementInfo | null = null
  private messageHandler: ((message: EditorMessage) => void) | null = null
  private boundHandleMessage: ((event: MessageEvent) => void) | null = null

  /**
   * 初始化 Visual Editor Manager
   * @param iframe - 预览网站的 iframe 元素
   * @param onMessage - 接收消息的回调函数
   */
  init(iframe: HTMLIFrameElement, onMessage: (message: EditorMessage) => void): void {
    console.log('[VisualEditor] Initializing...')
    this.iframe = iframe
    this.messageHandler = onMessage
    
    // 绑定消息处理函数
    this.boundHandleMessage = this.handleMessage.bind(this)
    window.addEventListener('message', this.boundHandleMessage)
    console.log('[VisualEditor] Message listener added')
    
    // 检查 iframe 状态
    console.log('[VisualEditor] iframe readyState:', iframe.contentDocument?.readyState)
    console.log('[VisualEditor] iframe src:', iframe.src)
    
    // 在 iframe 加载完成后注入选择器脚本
    const tryInject = () => {
      console.log('[VisualEditor] Trying to inject script...')
      this.injectSelectorScript()
      
      // 如果注入失败，3秒后重试一次
      setTimeout(() => {
        const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document
        if (iframeDoc && !iframeDoc.getElementById('visual-editor-script')) {
          console.log('[VisualEditor] Script not found, retrying injection...')
          this.injectSelectorScript()
        }
      }, 3000)
    }
    
    if (iframe.contentWindow && iframe.contentDocument?.readyState === 'complete') {
      console.log('[VisualEditor] iframe already loaded, injecting immediately')
      tryInject()
    } else {
      console.log('[VisualEditor] Waiting for iframe to load...')
      iframe.addEventListener('load', () => {
        console.log('[VisualEditor] iframe load event fired')
        // 等待一小段时间确保 iframe 内容完全渲染
        setTimeout(tryInject, 500)
      })
    }
    
    console.log('[VisualEditor] Initialized')
  }

  /**
   * 进入编辑模式
   */
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
    
    console.log('[VisualEditor] Entered edit mode')
  }

  /**
   * 退出编辑模式
   */
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
    
    console.log('[VisualEditor] Exited edit mode')
  }

  /**
   * 清除选中元素
   */
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
    
    console.log('[VisualEditor] Cleared selection')
  }

  /**
   * 获取当前选中元素
   */
  getSelectedElement(): ElementInfo | null {
    return this.selectedElement
  }

  /**
   * 销毁 Visual Editor Manager
   */
  destroy(): void {
    if (this.boundHandleMessage) {
      window.removeEventListener('message', this.boundHandleMessage)
    }
    
    this.iframe = null
    this.messageHandler = null
    this.boundHandleMessage = null
    this.isEditMode = false
    this.selectedElement = null
    
    console.log('[VisualEditor] Destroyed')
  }

  /**
   * 发送消息到 iframe
   * @param message - 要发送的消息
   */
  private sendMessage(message: EditorMessage): void {
    if (!this.iframe || !this.iframe.contentWindow) {
      console.warn('[VisualEditor] Cannot send message: iframe not ready')
      return
    }
    
    try {
      // 发送消息到 iframe，使用 '*' 作为 targetOrigin（同域名）
      this.iframe.contentWindow.postMessage(message, '*')
      console.log('[VisualEditor] Sent message:', message.type)
    } catch (error) {
      console.error('[VisualEditor] Failed to send message:', error)
    }
  }

  /**
   * 处理接收到的消息
   * @param event - MessageEvent
   */
  private handleMessage(event: MessageEvent): void {
    // 验证消息来源（基本安全检查）
    if (!event.data || typeof event.data !== 'object') {
      return
    }
    
    const message = event.data as EditorMessage
    
    // 只处理来自预览网站的消息
    if (message.source !== 'preview') {
      return
    }
    
    console.log('[VisualEditor] Received message:', message.type)
    
    // 处理元素选中消息
    if (message.type === MessageType.ELEMENT_SELECTED && message.data) {
      this.selectedElement = message.data
    }
    
    // 调用回调函数
    if (this.messageHandler) {
      try {
        this.messageHandler(message)
      } catch (error) {
        console.error('[VisualEditor] Error in message handler:', error)
      }
    }
  }

  /**
   * 注入选择器脚本到 iframe
   */
  private injectSelectorScript(): void {
    console.log('[VisualEditor] Attempting to inject script...')
    
    if (!this.iframe) {
      console.error('[VisualEditor] iframe is null')
      return
    }
    
    if (!this.iframe.contentWindow) {
      console.error('[VisualEditor] iframe.contentWindow is null')
      return
    }

    try {
      const iframeDoc = this.iframe.contentDocument || this.iframe.contentWindow.document
      
      if (!iframeDoc) {
        console.error('[VisualEditor] Cannot access iframe document (possible cross-origin issue)')
        return
      }
      
      console.log('[VisualEditor] iframe document accessible, checking for existing script...')
      
      // 检查是否已经注入过
      if (iframeDoc.getElementById('visual-editor-script')) {
        console.log('[VisualEditor] Script already injected')
        return
      }

      console.log('[VisualEditor] Creating and injecting script...')
      const script = iframeDoc.createElement('script')
      script.id = 'visual-editor-script'
      script.textContent = this.getSelectorScriptContent()
      
      if (!iframeDoc.body) {
        console.error('[VisualEditor] iframe body not found')
        return
      }
      
      iframeDoc.body.appendChild(script)
      console.log('[VisualEditor] ✅ Selector script injected successfully!')
      
      // 验证脚本是否真的被添加了
      const injectedScript = iframeDoc.getElementById('visual-editor-script')
      console.log('[VisualEditor] Script verification:', injectedScript ? 'Found' : 'Not found')
    } catch (error) {
      console.error('[VisualEditor] ❌ Failed to inject script:', error)
      console.error('[VisualEditor] Error details:', {
        name: (error as Error).name,
        message: (error as Error).message,
        stack: (error as Error).stack
      })
    }
  }

  /**
   * 获取选择器脚本内容
   */
  private getSelectorScriptContent(): string {
    return `
      (function() {
        // 消息类型
        var MessageType = {
          ENTER_EDIT_MODE: 'ENTER_EDIT_MODE',
          EXIT_EDIT_MODE: 'EXIT_EDIT_MODE',
          ELEMENT_SELECTED: 'ELEMENT_SELECTED',
          CLEAR_SELECTION: 'CLEAR_SELECTION'
        };

        // Element Selector 类
        var ElementSelector = (function() {
          function ElementSelector() {
            this.isActive = false;
            this.hoveredElement = null;
            this.selectedElement = null;
            this.HOVER_CLASS = 'visual-editor-hover';
            this.SELECTED_CLASS = 'visual-editor-selected';
            
            this.injectStyles();
            window.addEventListener('message', this.handleParentMessage.bind(this));
            console.log('[ElementSelector] Initialized in iframe');
          }

          ElementSelector.prototype.injectStyles = function() {
            var style = document.createElement('style');
            style.textContent = 
              '.' + this.HOVER_CLASS + ' {' +
              '  outline: 2px dashed #1890ff !important;' +
              '  outline-offset: 2px;' +
              '  cursor: pointer !important;' +
              '  transition: outline 0.2s ease;' +
              '}' +
              '.' + this.SELECTED_CLASS + ' {' +
              '  outline: 3px solid #1890ff !important;' +
              '  outline-offset: 2px;' +
              '  background-color: rgba(24, 144, 255, 0.05) !important;' +
              '  transition: all 0.2s ease;' +
              '}';
            document.head.appendChild(style);
          };

          ElementSelector.prototype.handleParentMessage = function(event) {
            if (!event.data || typeof event.data !== 'object') return;
            var message = event.data;
            if (message.source !== 'main') return;
            
            console.log('[ElementSelector] Received message:', message.type);
            
            switch (message.type) {
              case MessageType.ENTER_EDIT_MODE:
                this.activate();
                break;
              case MessageType.EXIT_EDIT_MODE:
                this.deactivate();
                break;
              case MessageType.CLEAR_SELECTION:
                this.removeSelectedBorder();
                break;
            }
          };

          ElementSelector.prototype.activate = function() {
            if (this.isActive) return;
            this.isActive = true;
            
            document.addEventListener('mouseover', this.handleMouseOver.bind(this), true);
            document.addEventListener('mouseout', this.handleMouseOut.bind(this), true);
            document.addEventListener('click', this.handleClick.bind(this), true);
            
            console.log('[ElementSelector] Activated');
          };

          ElementSelector.prototype.deactivate = function() {
            if (!this.isActive) return;
            this.isActive = false;
            
            document.removeEventListener('mouseover', this.handleMouseOver.bind(this), true);
            document.removeEventListener('mouseout', this.handleMouseOut.bind(this), true);
            document.removeEventListener('click', this.handleClick.bind(this), true);
            
            this.removeHoverBorder(this.hoveredElement);
            this.removeSelectedBorder();
            
            console.log('[ElementSelector] Deactivated');
          };

          ElementSelector.prototype.handleMouseOver = function(event) {
            if (!this.isActive) return;
            var target = event.target;
            if (target === this.selectedElement) return;
            
            if (this.hoveredElement && this.hoveredElement !== target) {
              this.removeHoverBorder(this.hoveredElement);
            }
            
            this.hoveredElement = target;
            this.addHoverBorder(target);
          };

          ElementSelector.prototype.handleMouseOut = function(event) {
            if (!this.isActive) return;
            var target = event.target;
            
            if (target === this.hoveredElement) {
              this.removeHoverBorder(target);
              this.hoveredElement = null;
            }
          };

          ElementSelector.prototype.handleClick = function(event) {
            if (!this.isActive) return;
            
            event.preventDefault();
            event.stopPropagation();
            
            var target = event.target;
            this.removeSelectedBorder();
            this.removeHoverBorder(target);
            
            this.selectedElement = target;
            this.addSelectedBorder(target);
            
            var elementInfo = this.extractElementInfo(target);
            this.sendToParent({
              type: MessageType.ELEMENT_SELECTED,
              data: elementInfo,
              source: 'preview',
              timestamp: Date.now()
            });
            
            console.log('[ElementSelector] Element selected:', elementInfo);
          };

          ElementSelector.prototype.addHoverBorder = function(element) {
            if (!element) return;
            element.classList.add(this.HOVER_CLASS);
          };

          ElementSelector.prototype.removeHoverBorder = function(element) {
            if (!element) return;
            element.classList.remove(this.HOVER_CLASS);
          };

          ElementSelector.prototype.addSelectedBorder = function(element) {
            if (!element) return;
            element.classList.add(this.SELECTED_CLASS);
          };

          ElementSelector.prototype.removeSelectedBorder = function() {
            if (!this.selectedElement) return;
            this.selectedElement.classList.remove(this.SELECTED_CLASS);
            this.selectedElement = null;
          };

          ElementSelector.prototype.extractElementInfo = function(element) {
            var tagName = element.tagName;
            var className = element.className || '';
            var id = element.id || '';
            var textContent = (element.textContent || '').trim().substring(0, 200);
            var xpath = this.getXPath(element);
            
            return {
              tagName: tagName,
              className: className,
              id: id,
              textContent: textContent,
              xpath: xpath
            };
          };

          ElementSelector.prototype.getXPath = function(element) {
            if (element.id) {
              return '//*[@id="' + element.id + '"]';
            }
            
            var parts = [];
            var current = element;
            
            while (current && current.nodeType === Node.ELEMENT_NODE) {
              var index = 0;
              var sibling = current.previousSibling;
              
              while (sibling) {
                if (sibling.nodeType === Node.ELEMENT_NODE && 
                    sibling.nodeName === current.nodeName) {
                  index++;
                }
                sibling = sibling.previousSibling;
              }
              
              var tagName = current.nodeName.toLowerCase();
              var pathIndex = index > 0 ? '[' + (index + 1) + ']' : '';
              parts.unshift(tagName + pathIndex);
              
              current = current.parentElement;
            }
            
            return parts.length ? '/' + parts.join('/') : '';
          };

          ElementSelector.prototype.sendToParent = function(message) {
            try {
              window.parent.postMessage(message, '*');
              console.log('[ElementSelector] Sent message to parent:', message.type);
            } catch (error) {
              console.error('[ElementSelector] Failed to send message:', error);
            }
          };

          return ElementSelector;
        })();

        // 自动初始化
        new ElementSelector();
      })();
    `;
  }
}
