# Journal - AiHyo (Part 1)

> AI development session journal
> Started: 2026-08-17

---

## 2026-08-17 smoothness epic (08-17-smoothness)

Trellis inited in `E:\project\page-pilot` (`trellis init --dsh -u AiHyo`). Parent + 5 children created.

### Services (keep running)

- Backend `http://localhost:8124/api` (`--server.port=8124`, `-Dapp.code.deploy.host=http://localhost:8124/api/static`)
- Frontend `http://127.0.0.1:5175/` (Vite `host: 127.0.0.1`, proxy → 8124)
- MySQL `page-pilot-mysql`, Redis `page-pilot-redis`

### Demo

- Account `demo_user` / `demo123456` (registered this session; DB previously only tester001/002)
- HTML app `446839947227643904` deploy `2WEl4r` featured
- Vue app `446841399874424832` deploy `IpzeVS`

### Child status

| Task | Result |
|---|---|
| `08-17-login-home` | login + session + featured list after cache flush |
| `08-17-html-sse` | 21s stream, `event:done`, preview 200 |
| `08-17-deploy` | deploy URL 200 after static-dir fix; zip ok; COS cover |
| `08-17-vue-project` | generate + npm build + dist preview + deploy 200 |
| `08-17-visual-ux` | delete 200; SSE button disabled; editor message shadow fixed |

### Bugs fixed

1. SSE `onerror` toasted “生成失败” after a normal `done` close
2. Frontend defaults pointed at 8123 (Agent). Now `/api` + proxy 8124; Vite `host: 127.0.0.1`
3. Static controller only served `code_output` — deploy URLs 404. Now also `code_deploy`
4. Vue reasoning model hardcoded `deepseek-ai/DeepSeek-V3.1`. Now reads yaml `deepseek-v4-flash`
5. Featured cache `featured_app_page` had 30min empty-list cache. 2min TTL + admin `@CacheEvict`
6. COS `baseUrl + "/" + key` double-slash when key started with `/`
7. Frontend re-POSTed chat history that the SSE handler already saved

### How to demo

1. Open `http://127.0.0.1:5175/user/login`
2. `demo_user` / `demo123456`
3. Home featured card + “我的作品”
4. Open HTML chat → preview iframe `/api/static/html_446839947227643904/`
5. Deploy / download already proven
6. Vue chat preview `/api/static/vue_project_446841399874424832/dist/index.html`


## Session 1: Harden generate, preview isolation, path jail

**Date**: 2026-08-17
**Task**: Harden generate, preview isolation, path jail
**Branch**: `master`

### Summary

Closed confirmed bugs, demo-safe vulns, and cheap optimizations. Generate is POST SSE; preview iframe is sandboxed; file tools and static paths are jailed; demo_user MD5 upgrades to BCrypt on login.

### Main Changes

- POST /app/chat/gen/code + fetch SSE client
- ProjectPathGuard + SortFields allow-list
- CSP sandbox preview + serve-time visual-editor hook

### Git Commits

| Hash | Message |
|------|---------|
| `959e77a` | (see git log) |
| `57b0687` | (see git log) |

### Testing

- [OK] ProjectPathGuardTest + SortFieldsTest; vue-tsc; login/preview/download in browser

### Status

[OK] **Completed**

### Next Steps

- Ship uncommitted iOS visual system onto login, home, and AppChatPage without breaking generate/sandbox


## Session 2: Make local demo reproducible

**Date**: 2026-08-20
**Task**: Make local demo reproducible
**Branch**: `master`

### Summary

Pinned 8124/5175, walked demo_user generate-preview-deploy, cache-busted preview, and CORS-loaded Vue modules in the sandboxed iframe.

### Main Changes

- Pin Vite 5175 / proxy 8124 and README local-run flags
- Do not block routing when get/login fails
- Cache-bust preview iframe after generate; HTML Cache-Control no-store
- /static CORS without credentials so sandboxed Vue dist ES modules load; keep iframe without allow-same-origin

### Git Commits

| Hash | Message |
|------|---------|
| `aa2dcb9` | (see git log) |
| `091d647` | (see git log) |
| `bd9b698` | (see git log) |

### Testing

- [OK] demo_user login, HTML generate, deploy 2WEl4r, Vue todo iframe

### Status

[OK] **Completed**

### Next Steps

- Keep Docker Desktop Resource Saver off; visual-chat and bootstrap-guidelines still open
