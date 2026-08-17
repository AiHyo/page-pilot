(function() {
  if (document.getElementById('visual-editor-script') && window.__pagePilotVisualEditor) {
    return;
  }
  window.__pagePilotVisualEditor = true;

  var MessageType = {
    ENTER_EDIT_MODE: 'ENTER_EDIT_MODE',
    EXIT_EDIT_MODE: 'EXIT_EDIT_MODE',
    ELEMENT_SELECTED: 'ELEMENT_SELECTED',
    CLEAR_SELECTION: 'CLEAR_SELECTION'
  };

  function ElementSelector() {
    this.isActive = false;
    this.hoveredElement = null;
    this.selectedElement = null;
    this.HOVER_CLASS = 'visual-editor-hover';
    this.SELECTED_CLASS = 'visual-editor-selected';
    this.boundMouseOver = this.handleMouseOver.bind(this);
    this.boundMouseOut = this.handleMouseOut.bind(this);
    this.boundClick = this.handleClick.bind(this);
    this.injectStyles();
    window.addEventListener('message', this.handleParentMessage.bind(this));
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
    (document.head || document.documentElement).appendChild(style);
  };

  ElementSelector.prototype.handleParentMessage = function(event) {
    if (!event.data || typeof event.data !== 'object') return;
    if (event.source !== window.parent) return;
    var message = event.data;
    if (message.source !== 'main') return;
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
    document.addEventListener('mouseover', this.boundMouseOver, true);
    document.addEventListener('mouseout', this.boundMouseOut, true);
    document.addEventListener('click', this.boundClick, true);
  };

  ElementSelector.prototype.deactivate = function() {
    if (!this.isActive) return;
    this.isActive = false;
    document.removeEventListener('mouseover', this.boundMouseOver, true);
    document.removeEventListener('mouseout', this.boundMouseOut, true);
    document.removeEventListener('click', this.boundClick, true);
    this.removeHoverBorder(this.hoveredElement);
    this.removeSelectedBorder();
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
    this.sendToParent({
      type: MessageType.ELEMENT_SELECTED,
      data: this.extractElementInfo(target),
      source: 'preview',
      timestamp: Date.now()
    });
  };

  ElementSelector.prototype.addHoverBorder = function(element) {
    if (!element || !element.classList) return;
    element.classList.add(this.HOVER_CLASS);
  };

  ElementSelector.prototype.removeHoverBorder = function(element) {
    if (!element || !element.classList) return;
    element.classList.remove(this.HOVER_CLASS);
  };

  ElementSelector.prototype.addSelectedBorder = function(element) {
    if (!element || !element.classList) return;
    element.classList.add(this.SELECTED_CLASS);
  };

  ElementSelector.prototype.removeSelectedBorder = function() {
    if (!this.selectedElement) return;
    if (this.selectedElement.classList) {
      this.selectedElement.classList.remove(this.SELECTED_CLASS);
    }
    this.selectedElement = null;
  };

  ElementSelector.prototype.extractElementInfo = function(element) {
    var tagName = element.tagName || '';
    var className = element.className || '';
    if (typeof className !== 'string') {
      className = className.baseVal || '';
    }
    var id = element.id || '';
    var textContent = (element.textContent || '').trim().substring(0, 200);
    return {
      tagName: tagName,
      className: className,
      id: id,
      textContent: textContent,
      xpath: this.getXPath(element)
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
        if (sibling.nodeType === Node.ELEMENT_NODE && sibling.nodeName === current.nodeName) {
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
    } catch (error) {
      console.error('[ElementSelector] Failed to send message:', error);
    }
  };

  new ElementSelector();
})();
