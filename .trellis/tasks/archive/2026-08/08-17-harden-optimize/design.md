# Design — harden / polish page-pilot

## Boundaries

| Layer | Owns | Must not |
|---|---|---|
| `com.aih.pagepilot.utils.ProjectPathGuard` | Jail a relative path under a root | Live in each file tool separately |
| `com.aih.pagepilot.common.SortFields` | Allow-list `sortField` → column | Pass client strings into `orderBy` |
| File tools | Call the guard with `vue_project_{id}` | Accept absolute paths |
| `StaticResourceController` | Resolve + jail + HTML isolation headers + inject editor hook | String-contains `..` only |
| `AppController` | POST generate DTO + existing authz | Keep GET generate |
| `UserServiceImpl` | Session id, password verify/upgrade | Put `User` (with hash) in Redis |
| Frontend `AppChatPage` + small `sse.ts` | POST fetch SSE parse, owner-gated deploy, blob download check, hide dead chrome | `new EventSource` with the prompt in the query |

Do not restyle `HomePage` / auth pages. Do not touch Trellis bookkeeping.

## Contracts

### Generate

- **Before:** `GET /api/app/chat/gen/code?appId=&message=` + `EventSource`
- **After:** `POST /api/app/chat/gen/code`  
  Body: `{ "appId": number, "message": string }`  
  Response: same SSE (`data: {"data": "..."}` chunks, `event: done`, `event: business-error`)  
  `withCredentials` via `fetch` (same-origin `/api` proxy).
- Keep the GET mapping **removed** (not dual-supported) so CSRF-able GET is gone.
- New DTO `AppChatGenRequest` next to `AppDeployRequest`.
- Frontend helper `page-pilot-frontend/src/utils/sse.ts`: `POST` + read `ReadableStream`, split on `\n\n`, dispatch `message` / named events. AbortController on unmount.

### Path jail

```
resolveInside(root, userPath) -> Path
  reject null/blank, Windows prefixes, absolute Path
  resolved = root.toAbsolutePath().normalize().resolve(userPath).normalize()
  require resolved.startsWith(root.normalize())
```

All five tools use `CODE_OUTPUT_ROOT_DIR / vue_project_{appId}` as root (`FileDeleteTool` today misses `_`).

Static: `deployKey` must match `[A-Za-z0-9_-]{1,64}`; `resourcePath` jailed under `code_output/{key}` then `code_deploy/{key}`. Use `Path` not `new File(root, key + path)`.

### Preview isolation vs visual edit

Generated HTML is attacker-controlled and today shares origin with `/api`.

1. Chat iframe: `sandbox="allow-scripts allow-forms allow-downloads allow-popups"` — **no** `allow-same-origin`.
2. When serving `*.html`, add  
   `Content-Security-Policy: sandbox allow-scripts allow-forms allow-downloads`  
   and `X-Content-Type-Options: nosniff`.
3. Parent cannot use `contentDocument`. `StaticResourceController` appends a small hook script before `</body>` (or at end of file) that listens for parent messages and posts `source: 'preview'` selections. Same behavior as today’s `visualEditor.ts` injector.
4. Parent `postMessage` target is the iframe `contentWindow`; inbound messages accepted only if `event.source === iframe.contentWindow`.
5. Cookie `HttpOnly` so even a missed isolation path cannot read `document.cookie`.

### Session and password

- Attribute `USER_LOGIN_STATE` value: `Long` user id.
- `getLoginUser`: if attribute is `User` (old session), take `id`; if `Long`/`Number`, use it; then `getById`.
- `getEncryptPassword` becomes “hash for storage” using Hutool `BCrypt.hashpw`.
- Verify: if value looks like BCrypt (`$2a$` / `$2b$` / `$2y$`) use `BCrypt.checkpw`; else compare legacy `md5(password + "aih")`. On legacy match, `updateById` with BCrypt hash.
- `LoginUserVO` / `UserVO` unchanged (no password field).

### Sort allow-list

| Query | Allowed fields |
|---|---|
| App | `id`, `createTime`, `updateTime`, `priority`, `appName`, `userId` |
| User | `id`, `createTime`, `updateTime`, `userAccount`, `userName`, `userRole` |
| ChatHistory | `id`, `createTime`, `updateTime` |

Unknown / blank → existing default order. Never pass the raw string into `orderBy`.

### CORS / cookie / rate limit

- `allowedOriginPatterns("http://localhost:5175", "http://127.0.0.1:5175")`.
- `application.yml` cookie: `http-only: true`, `same-site: lax`, keep `max-age`.
- Rate limiter IP: `request.getRemoteAddr()` only.

### Chat history create

`POST /chat-history` forces `messageType` to user (`MessageTypeEnum.USER`). Ignore client-supplied type.

### Screenshot / Vue build

- Convert `WebScreenshotUtils` to a Spring `@Component` with a `synchronized` lock around `get` + screenshot + wait. `@PreDestroy` quits the driver. Wait for `readyState=complete` only; drop the extra `Thread.sleep(2000)` unless readyState never completes (then cap at 500ms).
- Existing `WebScreenshotUtilsTest` will need the bean or a thin static delegate; prefer injecting the component in production and keeping a package-visible method the test can call, or `@SpringBootTest` — do **not** hit GitHub in unit tests if we touch the test (make it not network-dependent if we edit it). Prefer not expanding that test’s live-network behavior.
- `VueProjectBuilder.executeNpmInstall`: skip when `new File(projectDir, "node_modules").isDirectory()`.

### Featured cache

`@Cacheable` `unless` when the page has `totalRow == 0` (empty featured must not occupy the 2-minute slot). Keep existing `@CacheEvict` on admin update.

### Frontend UX

- Deploy `:disabled="!generationComplete || !isOwner"`.
- Download: if blob `type` is JSON or first bytes are `{`, parse and toast `message`.
- Preview toolbar refresh: `previewIframeRef.contentWindow.location.reload()` or bump `previewUrl` with a cache-buster query.
- Remove/hide 上传、保存、历史、菜单、响应式.
- `business-error` handler sets a `sawBusinessError` flag; `done` handler no-ops if that flag or `!isGenerating`.
- Markdown fallback: `DOMPurify.sanitize` of escaped text, not raw `content`.

## Data flow

```
User prompt
  → POST /app/chat/gen/code (session cookie)
  → Auth + user rate limit + owner check
  → PromptSafetyInputGuardrail (≤8192)
  → tools resolve paths via ProjectPathGuard
  → SSE chunks → fetch parser → MarkdownRenderer (purified)
  → preview iframe sandbox → /api/static/{type}_{id}/...
       HTML: CSP sandbox + injected editor hook
       hook postMessage → parent (event.source check)
```

## Compatibility

- Existing `demo_user` MD5 row keeps working; first login upgrades hash.
- Old Redis sessions holding a `User` object still resolve.
- OpenAPI/ts `chatToGenCode` GET helper may remain unused; chat page must not call it.
- Deployed `tmp/code_deploy/{key}` URLs unchanged.
- Visual edit requires the new injected hook; old generated HTML without the hook: controller injects on **serve**, not on disk. No rewrite of existing files.

## Trade-offs

| Choice | Why | Cost |
|---|---|---|
| Drop GET generate instead of dual-stack | Dual-stack leaves CSRF open | Anyone bookmarking the GET URL breaks |
| iframe sandbox vs `contentDocument` | Isolation is the actual S2 fix | Must inject hook at serve time |
| BCrypt upgrade-on-login vs keep MD5 | User asked to maximize; demo login survives | One extra write on first login |
| Skip `npm install` when `node_modules` exists | Fast re-deploy | Stale deps if package.json changed — acceptable for demo |

## Rollback

Revert the commit(s) for this task. Users already upgraded to BCrypt still login. Users still on MD5 still login. No schema migration.

If POST generate ships without frontend, chat breaks — ship backend + `AppChatPage` / `sse.ts` together.
