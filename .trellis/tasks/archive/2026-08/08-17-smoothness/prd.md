# Page-Pilot smoothness epic

## Goal

Every user-facing path is fluent: pages render, SSE is not interrupted (and does not look interrupted), HTML generate + preview works, deploy + download work, Vue project mode builds. Fix real bugs found in self-check.

This is one parent. Verify children in order. Keep `:8124` and `:5175` running.

## Child map

| Child | Focus | Status |
|---|---|---|
| `08-17-login-home` | Login, register, home, featured, my apps | done |
| `08-17-html-sse` | Create HTML app, SSE stream, preview iframe | done |
| `08-17-deploy` | Deploy, static URL, COS cover, zip download | done |
| `08-17-vue-project` | Complex prompt → VUE_PROJECT → npm build → preview | done |
| `08-17-visual-ux` | Visual editor + remaining chrome | done |

## Constraints

- Ports: backend **8124**, frontend **5175** (`--strictPort`)
- Do not start a second GUI for DSH
- Do not merge ChatPartner voice
- Prefer fixing defaults so local multi-project does not require a 6-flag memory

## Acceptance Criteria

- [x] All five children meet their PRD boxes
- [x] `http://127.0.0.1:5175` demo as `demo_user` / `demo123456` walks generate → preview → deploy without a false error toast
- [x] Vue project has at least one successful local `vue_project_*` output with `dist/`
