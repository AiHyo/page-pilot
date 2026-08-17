# Implement — harden / polish page-pilot

Do **R1 → R2 → R3** in order. Backend + matching frontend for generate/isolation ship in the same pass.

## 1. Shared utilities (R1 B1 + R2 S1/S5)

- [x] Add `src/main/java/com/aih/pagepilot/utils/ProjectPathGuard.java` (`resolveInside`).
- [x] Add `src/main/java/com/aih/pagepilot/common/SortFields.java` + apply in `AppServiceImpl`, `UserServiceImpl`, `ChatHistoryServiceImpl`.
- [x] Add unit tests: `ProjectPathGuardTest`, `SortFieldsTest`.
- [x] Point all five AI file tools at `vue_project_{appId}` + the guard. Fix `FileDeleteTool` missing `_`. Line-break `FileDirReadTool` entries.

## 2. Functional leftovers that do not depend on generate (R1)

- [x] `UserController.getUserVOById`: login required, `getById` + `getUserVO` (never call admin `getUserById`).
- [x] `AppChatPage`: owner-gate Deploy; hide 上传/保存/历史/菜单/响应式; wire preview refresh.
- [x] Download: reject JSON-as-zip.
- [x] `MarkdownRenderer` fallback: sanitize/escape, no raw HTML.

## 3. Security core (R2)

- [x] `StaticResourceController`: deployKey charset, `Path` jail, HTML CSP sandbox + inject editor hook, nosniff.
- [x] `AppChatGenRequest` + `POST /app/chat/gen/code`; **delete** GET mapping.
- [x] Frontend `src/utils/sse.ts` + switch `AppChatPage` off `EventSource`. Business-error is terminal (`B9`).
- [x] `visualEditor.ts`: drop `contentDocument` inject; rely on server hook; `event.source === iframe.contentWindow`.
- [x] `CorsConfig` localhost/127.0.0.1:5175 only.
- [x] Cookie `http-only` + `same-site: lax` in `application.yml`.
- [x] Session stores `Long` id; read still accepts old `User`.
- [x] Password: BCrypt store + MD5 verify + upgrade-on-login (Hutool `BCrypt`).
- [x] `POST /chat-history` forces USER type.
- [x] `RateLimitAspect` uses `remoteAddr` only.
- [x] Guardrail max length 8192.

## 4. Screenshot + cache + Vue install (R1 B6 + R3)

- [x] `WebScreenshotUtils` → Spring bean, synchronized, no mandatory 2s sleep, `@PreDestroy`.
- [x] Featured `@Cacheable` `unless` empty `totalRow`.
- [x] `VueProjectBuilder` skip `npm install` when `node_modules` exists.
- [x] Visual editor: iframe `load` (no 2s `setTimeout` as the only trigger).

## 5. Validation

```text
# unit
mvn -q -Dtest=ProjectPathGuardTest,SortFieldsTest test

# backend still compiles (JDK 25 / Lombok 1.18.40)
mvn -q -DskipTests compile

# frontend
cd page-pilot-frontend && npx vue-tsc --noEmit && npm run lint
```

Manual (services already on 8124 / 5175 if up):

1. Login `demo_user` / `demo123456` — must succeed (MD5 upgrade ok).
2. Open existing HTML app `446839947227643904` — preview 200, no session leak via iframe (sandbox attribute present).
3. Send a short follow-up generate — POST SSE streams; no false 生成失败; `done` toasts success once.
4. Rate-limit / empty message — business-error, no “代码生成完成”.
5. Deploy + download on owned app; download of a missing app is not a zip.
6. Non-owner view: Deploy disabled.
7. `GET /api/app/list/featured?sortField=id;select` (or similar) must not 500 / must ignore bad field.

## Risky files / rollback

| File | Risk |
|---|---|
| `AppController` generate mapping | Chat dead if frontend not updated together |
| `UserServiceImpl` password/session | Lock out demo if MD5 path removed |
| `StaticResourceController` + iframe sandbox | Visual edit / preview blank if hook/CSP wrong |
| `WebScreenshotUtils` lifecycle | Deploy cover fails if driver init throws |

Rollback: revert the task commit. No DB migration. BCrypt-upgraded rows remain valid.

## Ready for `task.py start`

- [x] `prd.md` converged, no blocking questions
- [x] `design.md` present
- [x] this file present
- [x] `implement.jsonl` / `check.jsonl` curated (Phase 1.3)
- [x] User approved the final planning summary
