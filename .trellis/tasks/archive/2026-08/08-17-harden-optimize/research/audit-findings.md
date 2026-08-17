# Audit findings — page-pilot (2026-08-17)

Source: repository inspection of controllers, AI file tools, session/CORS, static serving, frontend chat/preview. Not a live pentest.

## Functional bugs (code-backed)

| ID | Severity | Evidence | Effect |
|---|---|---|---|
| B1 | High | `FileDeleteTool.java:30` uses `"vue_project" + appId`; write/read/modify use `"vue_project_" + appId` | Vue delete tool targets the wrong directory |
| B2 | High | `UserController.java:129-134` `getUserVOById` calls `getUserById` which is `@AuthCheck(ADMIN)` | Public user-VO lookup always requires admin; non-admin callers get 403 |
| B3 | Medium | `AppChatPage.vue:664-672` Deploy button only checks `generationComplete`, not `isOwner` | Non-owner can click Deploy; backend then 403 |
| B4 | Medium | `AppChatPage.vue:371-413` download treats any 2xx blob as zip; error JSON becomes a corrupt zip | Failed download can look successful |
| B5 | Medium | `AppChatPage.vue:845` / `823-825` Refresh / 上传 / 保存 / 历史 / 菜单 / 响应式 are dead buttons | UX holes, not wired |
| B6 | Medium | `WebScreenshotUtils.java:27-33,144` process-wide static `WebDriver`; `@PreDestroy` on a non-Spring class | Concurrent deploy screenshots race; driver never closed by Spring |
| B7 | Low | `FileDirReadTool.java:76-79` appends names with no newlines | Directory listing is unreadable to the model |
| B8 | Low | `MarkdownRenderer.vue:66` fallback `` `<pre>${props.content}</pre>` `` is unsanitized | If `marked` throws, raw HTML can inject |
| B9 | Low | `SSE` `GlobalExceptionHandler.java:80` also emits `event: done` after `business-error` | Frontend may treat a failed generate as success (`AppChatPage.vue:294-309`) |

## Security (code-backed)

| ID | Severity | Evidence | Effect |
|---|---|---|---|
| S1 | Critical | `FileWriteTool.java:51-54`, `FileModifyTool.java:39-44`, `FileReadTool.java:33-38`, `FileDeleteTool.java:28-33`, `FileDirReadTool.java:51-56` accept absolute paths | Prompt-injected model can read/write/delete any path the JVM user can touch |
| S2 | High | Preview iframe `AppChatPage.vue:914-920` loads same-origin `/api/static/...` with no `sandbox`; generated JS is attacker-controlled | Generated page can steal session cookie (same origin as API) |
| S3 | High | `StaticResourceController.java:77-89` only string-contains `..`; no `Path.normalize` / root jail | Encoded / normalized path traversal into `tmp/` or beyond |
| S4 | High | `AppController.java:321` generate is a **state-changing GET** (`EventSource` URL) | Top-level GET CSRF can trigger generation (SameSite=Lax sends cookies on navigations) |
| S5 | High | `AppServiceImpl.java:182`, `UserServiceImpl.java:178`, `ChatHistoryServiceImpl.java:203` `orderBy(sortField, …)` with client string; featured list is public GET | Unvalidated sort column — SQL injection risk via MyBatis-Flex |
| S6 | Medium | `CorsConfig.java:16-23` `allowedOriginPatterns("*")` + `allowCredentials(true)` | Any origin echoed; relies only on browser SameSite |
| S7 | Medium | `UserServiceImpl.java:106` stores full `User` (incl. `userPassword` hash) in session | Password hash lives in Redis session |
| S8 | Medium | `UserServiceImpl.java:182-186` MD5 + static salt `"aih"` | Weak password storage |
| S9 | Medium | Session cookie (`application.yml:29-31`) sets only `max-age`; no explicit `HttpOnly` / `SameSite` / `Secure` | Cookie flags implicit |
| S10 | Medium | `visualEditor.ts:182,488` `postMessage(..., '*')`; `handleMessage` does not check `event.origin` | Any window can spoof `source: 'preview'` and inject fake element context into the next generate |
| S11 | Medium | `ChatHistoryController.java:53-77` owner can POST `messageType=ai` | Fake AI turns enter memory (`loadChatHistoryToMemory`) |
| S12 | Medium | `RateLimitAspect.java:111` trusts `X-Forwarded-For` | Unauthenticated IP limiter can be spoofed |
| S13 | Low | `UserController.java:105-106` admin-created users get password `12345678` | Default password |
| S14 | Low | `application.yml` commits `root` / `123456`; Knife4j enabled | Local-demo defaults in tree |
| S15 | Low | `PromptSafetyInputGuardrail.java` keyword/regex only; 1000-char cap | Easy to bypass; also blocks legitimate long prompts |

## Optimization (code-backed)

| ID | Note |
|---|---|
| O1 | Shared static ChromeDriver + 2s sleep per screenshot (`WebScreenshotUtils.java:117,144`) — serialize + reuse or pool |
| O2 | Featured cache already 2 min (`RedisCacheManagerConfig.java:49-50`); empty-list cache still possible because nulls are disabled but empty Page is not null |
| O3 | `getAppVO` single-item path still N+1 (`AppServiceImpl.java:111-115`); list path is batched |
| O4 | Chat generate via GET + query string logs the full prompt in access logs / Referer |
| O5 | Visual editor waits a hard 2s then injects (`AppChatPage.vue:608-611`) |
| O6 | Vue `npm install` on every deploy (`VueProjectBuilder.java:52`) |

## Previous-session leftovers (not this request)

- `08-17-smoothness` PRD marks all 5 children done; child `task.json` still `planning`; working tree still has the visual redesign (DESIGN.md, tokens, login/register).
- `00-bootstrap-guidelines` still `in_progress` with empty backend spec files.

## Out of this audit

- Live exploit confirmation against running 8124/5175
- COS / DeepSeek keys in gitignored `application-local.yml` (local only)
- Full LLM jailbreak evaluation
