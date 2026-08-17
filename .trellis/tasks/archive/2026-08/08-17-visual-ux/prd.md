# Visual edit and remaining UX

## Goal

After generate/preview/deploy are green, check visual element-select edit, app detail modal, delete app, and that generate stays disabled while streaming.

## Acceptance Criteria

- [x] Generate button disabled while `isGenerating`
- [x] Visual edit does not break the next SSE turn (same-origin preview `/api/static`; `handleEditorMessage` no longer shadows antd `message`)
- [x] Delete app removes chat history without 500 (`DELETE /api/app/{id}` → 200)
