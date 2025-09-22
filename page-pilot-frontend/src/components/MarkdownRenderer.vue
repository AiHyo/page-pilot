<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js'

// 配置highlight.js支持的语言
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import html from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import json from 'highlight.js/lib/languages/json'
import markdown from 'highlight.js/lib/languages/markdown'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('html', html)
hljs.registerLanguage('css', css)
hljs.registerLanguage('json', json)
hljs.registerLanguage('markdown', markdown)

const props = defineProps<{
  content: string
}>()

// 配置marked
const renderer = new marked.Renderer()

// 自定义代码块渲染
renderer.code = (code: string, language: string | undefined) => {
  const validLanguage = language && hljs.getLanguage(language) ? language : 'plaintext'
  const highlightedCode = hljs.highlight(code, { language: validLanguage }).value
  
  return `<pre class="hljs"><code class="hljs language-${validLanguage}">${highlightedCode}</code></pre>`
}

// 自定义行内代码渲染
renderer.codespan = (code: string) => {
  return `<code class="inline-code">${code}</code>`
}

marked.setOptions({
  renderer,
  gfm: true,
  breaks: true,
  pedantic: false,
  sanitize: false,
  smartLists: true,
  smartypants: false,
})

const renderedContent = computed(() => {
  if (!props.content) return ''
  
  try {
    const html = marked(props.content)
    // 使用DOMPurify清理HTML，防止XSS攻击
    return DOMPurify.sanitize(html)
  } catch (error) {
    console.error('Markdown渲染错误:', error)
    return `<pre>${props.content}</pre>`
  }
})
</script>

<style scoped>
.markdown-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

/* 标题样式 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 16px 0 8px 0;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-content :deep(h1) {
  font-size: 24px;
  border-bottom: 2px solid #eee;
  padding-bottom: 8px;
}

.markdown-content :deep(h2) {
  font-size: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 6px;
}

.markdown-content :deep(h3) {
  font-size: 18px;
}

.markdown-content :deep(h4) {
  font-size: 16px;
}

/* 段落样式 */
.markdown-content :deep(p) {
  margin: 8px 0;
}

/* 列表样式 */
.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
}

/* 代码块样式 */
.markdown-content :deep(pre) {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
  padding: 16px;
  margin: 16px 0;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.45;
}

.markdown-content :deep(pre code) {
  background: none;
  border: none;
  padding: 0;
  font-size: inherit;
}

/* 行内代码样式 */
.markdown-content :deep(.inline-code) {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 3px;
  padding: 2px 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  color: #d73a49;
}

/* 引用样式 */
.markdown-content :deep(blockquote) {
  margin: 16px 0;
  padding: 0 16px;
  border-left: 4px solid #dfe2e5;
  background: #f6f8fa;
  color: #6a737d;
}

/* 表格样式 */
.markdown-content :deep(table) {
  border-collapse: collapse;
  margin: 16px 0;
  width: 100%;
}

.markdown-content :deep(table th),
.markdown-content :deep(table td) {
  border: 1px solid #dfe2e5;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(table th) {
  background: #f6f8fa;
  font-weight: 600;
}

/* 分割线样式 */
.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #e1e4e8;
  margin: 24px 0;
}

/* 链接样式 */
.markdown-content :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

/* 强调样式 */
.markdown-content :deep(strong) {
  font-weight: 600;
}

.markdown-content :deep(em) {
  font-style: italic;
}

/* 删除线样式 */
.markdown-content :deep(del) {
  text-decoration: line-through;
}
</style>
