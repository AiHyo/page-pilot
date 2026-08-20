# Security Contracts — generate, static, paths, session

## Scenario: Code generate + preview isolation

### 1. Scope / Trigger

- Trigger: any change to generate SSE, AI file tools, static preview, session, CORS, or `sortField`.
- Why: generated HTML is attacker-controlled; snowflake ids lose precision in JS `Number`.

### 2. Signatures

- `POST /api/app/chat/gen/code` body `AppChatGenRequest { Long appId, String message }` produces `text/event-stream`.
- **Do not** add `GET /app/chat/gen/code`. GET generate is CSRF-able.
- `ProjectPathGuard.resolveInside(Path root, String userPath) -> Path`
- Static: `GET /api/static/{deployKey}/**` with `deployKey` matching `^[A-Za-z0-9_-]{1,64}$`

### 3. Contracts

- Generate SSE chunks: `data: {"data":"<chunk>"}` plus `event: done` on success.
- Generate errors: `event: business-error` with `{"error":true,"code":int,"message":string}`. Do **not** also emit `done`.
- Frontend sends `appId` as a **JSON string** (route param). Jackson maps it to `Long`. Never `Number(appId)` in the browser.
- Session attribute `USER_LOGIN_STATE` is `Long` user id. `getLoginUser` may read a leftover `User` object, then rewrite the session to the id.
- New password hashes: Hutool `BCrypt.hashpw`. Login still accepts legacy `md5(password + "aih")` and upgrades on success.
- Cookie: `http-only: true`, `same-site: lax`.
- Session CORS (`/**`): credentials only for `http://localhost:5175` and `http://127.0.0.1:5175`. Never `*` with credentials on `/**`.
- Static CORS (`/static/**`): register **before** `/**`. `allowedOriginPatterns("*")` + `allowCredentials(false)` so a sandboxed preview (`Origin: null`) can CORS-fetch Vue `dist` ES modules. Do **not** set `Access-Control-Allow-Origin` on `StaticResourceController` (duplicate headers). Do **not** add `allow-same-origin` to the iframe sandbox to “fix” Vue modules.
- HTML static responses: `Content-Security-Policy: sandbox allow-scripts allow-forms allow-downloads`, `X-Content-Type-Options: nosniff`, `Cache-Control: no-store`, `Cross-Origin-Resource-Policy: cross-origin`, inject `visual-editor-hook.js` at serve time (do not rewrite files on disk). Non-HTML static files also send CORP `cross-origin`.
- Chat iframe: `sandbox="allow-scripts allow-forms allow-downloads allow-popups"` — no `allow-same-origin`. After generate / history load / refresh, set iframe `src` with a `?t=` cache-bust so the browser does not keep pre-generate HTML.
- `sortField` goes through `SortFields.apply` allow-list. Raw client strings never enter `orderBy`.

### 4. Validation & Error Matrix

| Condition | Result |
|---|---|
| Blank / absolute / `C:` / UNC / `..` path in tools | Tool returns `错误：非法文件路径` |
| Same for static | `400` or `404`, never a file outside the key root |
| deployKey not `[A-Za-z0-9_-]{1,64}` | `400` |
| Empty generate message | `business-error` `PARAMS_ERROR` |
| GET generate | `405` |
| Unknown `sortField` | Ignored; default order |
| Featured page `totalRow == 0` | Not written to `featured_app_page` cache |

### 5. Good / Base / Bad Cases

- Good: `POST` `{"appId":"446839947227643904","message":"加一个按钮"}` as the owner.
- Base: preview `GET /api/static/html_{id}/index.html` returns HTML + CSP + `no-store` + CORP + hook. Vue `dist` JS is a CORS GET with `Origin: null`.
- Bad: `Number("446839947227643904")` in JS (precision loss). `GET .../gen/code?message=`. `../application.yml` under static.

### 6. Tests Required

- `ProjectPathGuardTest`: relative ok; `..`, `/etc/passwd`, `C:\\Windows` rejected.
- `SortFieldsTest`: allow-list hit; garbage string does not become SQL.
- Manual: `GET /api/app/chat/gen/code` is 405; `demo_user` still logs in after BCrypt upgrade.

### 7. Wrong vs Correct

#### Wrong

```java
if (path.isAbsolute()) return path; // AI tool writes anywhere
orderBy(sortField, "ascend".equals(sortOrder));
```

```ts
{ appId: Number(appId), message } // snowflake rounded
new EventSource(`/api/app/chat/gen/code?appId=${appId}&message=${msg}`)
```

#### Correct

```java
ProjectPathGuard.resolveInside(projectRoot, relativeFilePath);
SortFields.apply(queryWrapper, sortField, sortOrder, SortFields.APP);
```

```ts
await postSse('/api/app/chat/gen/code', { appId, message }, onEvent, signal)
```

## Common mistakes

- Emitting `event: done` after `business-error` makes the UI toast “代码生成完成”.
- Chat `messageType` in DB is `user`/`ai`, not `USER`/`AI`. Map both when rendering.
- Vue delete tool directory is `vue_project_{id}` (underscore). `vue_project{id}` deletes the wrong folder.
- Visual editor cannot use `iframe.contentDocument` once the iframe is sandboxed. Use the serve-time hook + `event.source === iframe.contentWindow`.
- Adding `allow-same-origin` to the chat iframe steals the parent origin (session). Vue ES modules need `/static/**` CORS without credentials, not a weaker sandbox.
- Registering `/**` CORS before `/static/**` makes Spring first-match `Origin: null` as 403.
- Setting ACAO both in `CorsConfig` and on the static controller produces duplicate CORS headers.
