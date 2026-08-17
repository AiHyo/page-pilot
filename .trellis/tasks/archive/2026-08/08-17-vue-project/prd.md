# Vue project generate + build

## Goal

A complex interactive prompt routes to `VUE_PROJECT`, writes multiple files, `VueProjectBuilder` runs `npm install && npm run build`, preview uses `dist/index.html`.

## Acceptance Criteria

- [x] `tmp/code_output/vue_project_<id>/` exists with `package.json`
- [x] `dist/index.html` exists after build
- [x] Chat page preview loads that dist (`/api/static/vue_project_<id>/dist/index.html` 200)
- [x] Failure (npm missing / network) is a visible error, not a silent iframe 404

## Evidence

- App `446841399874424832` routed to `vue_project`
- SSE `event:done` after tool writes (index.html, package.json, vite.config.js, App.vue, pages, components)
- `dist/` produced by async builder; deploy `IpzeVS` 200
- First attempt failed: reasoning model hardcoded `deepseek-ai/DeepSeek-V3.1`. Fixed to yaml `deepseek-v4-flash`
