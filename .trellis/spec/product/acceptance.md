# Acceptance — smoothness epic (`08-17-smoothness`)

Interview demo on `http://localhost:5175` as `demo_user` / `demo123456`.

## Already known constraints

- Backend yaml default port is **8123**. Local must pass `--server.port=8124`.
- Vite proxy and `env.ts` default to **8123** (z-ai-agent). Local must set `VITE_PROXY_TARGET=http://localhost:8124` and preview must not assume 8123.
- Deploy host default is `http://localhost` (no port). Local must set `-Dapp.code.deploy.host=http://localhost:8124/api/static`.
- `CODE_DEPLOY_ROOT_DIR` is `tmp/code_deploy` (not `code_output`).
- Generate SSE is rate-limited: 5 / 60s / user.
- Generate is **POST** `/api/app/chat/gen/code` with `{ appId, message }`. `appId` must stay a string in JS (snowflake). GET generate must stay gone (405).
- Preview iframe is sandboxed (no `allow-same-origin`). HTML from `/api/static` carries CSP sandbox + a serve-time visual-editor hook.

## Parent done when all children pass

1. Login, home, my-apps, featured list render; empty featured is honest or seeded
2. HTML generate: SSE streams without false interrupt; preview iframe shows the page
3. Deploy + download zip; deploy URL opens; cover screenshot does not 404
4. Vue project: multi-file generate + `npm` build + `dist/index.html` preview
5. Visual edit / remaining chrome does not break generate

## Hard fail

- Voice / TTS / ChatPartner merge
- Leaving preview pointed at 8123 while API is on 8124
- Showing “生成失败” after a successful `done` event
- Showing “代码生成完成” after `business-error`
- `Number(appId)` on a snowflake id
- Serving generated HTML same-origin without sandbox (session theft)
