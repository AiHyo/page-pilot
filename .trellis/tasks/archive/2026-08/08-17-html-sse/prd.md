# HTML SSE generate + preview

## Goal

A simple prompt produces a complete HTML stream and a working preview iframe. Stream must not abort mid-way; UI must not toast failure after `event: done`.

## Acceptance Criteria

- [x] SSE chunks append to the AI bubble continuously
- [x] `event: done` closes the stream; **no** subsequent “代码生成失败”
- [x] Preview URL hits this app’s static files on 8124 (`/api/static/html_<id>/`)
- [x] Iframe shows generated HTML, not 404 / Agent page
- [ ] Rate-limit error (if hit) shows the business-error message, not a hang
