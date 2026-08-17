# Harden / polish page-pilot

## Goal

Fix every confirmed functional bug, close every demo-safe security hole in the audit, and apply cheap optimizations — without breaking the local demo (`demo_user` / `demo123456` on `127.0.0.1:5175` + backend `8124`).

## User value

Generate, preview, deploy, and download keep working. Generated sites and AI file tools cannot read the host disk or take over the session. The happy path stays fast enough for a live walkthrough.

## Background

- User (2026-08-17): use Trellis; fix bugs; then 完善漏洞; then optimize. Follow-up: **尽可能多的 fix 和 optimize**.
- Decision: 「漏洞」= security list in `research/audit-findings.md`, plus the dead-button product holes already listed as bugs. Maximize coverage that does not invalidate existing demo logins.
- Smoothness epic already made login → SSE → HTML preview → deploy → Vue build work. Its Trellis children are still `planning`; visual redesign remains uncommitted. **Do not archive that epic or restyle untouched pages in this task.**
- Ports stay **8124** / **5175**. No ChatPartner voice.

## Requirements

Do the work in this order. A later phase must not reopen a closed earlier box.

### R1 — Functional bugs

- **B1** Vue `deleteFile` uses the same project root as write/read/modify: `vue_project_{id}`.
- **B2** `GET /user/get/vo` returns a desensitized `UserVO` for a logged-in caller. It must not require admin and must not return `userPassword`.
- **B3** Chat **Deploy** is disabled for non-owners, same as Download.
- **B4** Zip download must not save a JSON error body as `.zip`.
- **B5** Dead chrome: wire **preview refresh** to reload the iframe; hide 上传 / 保存 / 历史 / 菜单 / 响应式 (no fake controls).
- **B6** Screenshot ChromeDriver is not a process-global racy static; it is closable and not used by two deploys at once.
- **B7** Directory-list tool output is line-oriented.
- **B8** Markdown parse fallback must not inject unsanitized HTML.
- **B9** A `business-error` SSE must not toast “代码生成完成”. Frontend treats business-error as terminal and ignores a following `done`.

### R2 — Security

Close **S1–S12** and **S15** (length only). Demo-safe constraints:

- **S1** AI file tools reject absolute paths and any path whose normalized form is outside that app’s `code_output` directory. One shared resolver for write/read/modify/delete/dir.
- **S2 / S3** Static files: normalized path must stay under `code_output/{key}` or `code_deploy/{key}`. HTML responses are isolated from the app session (iframe sandbox + script injection for visual edit). Generated JS must not read the session cookie or call `/api` as the user.
- **S4** Code generation is no longer a cookie-authenticated state-changing **GET**. Frontend streams via `fetch` POST + SSE parse. Prompt is not placed in the query string.
- **S5** `sortField` is an allow-list of real columns (or ignored). Public featured list cannot accept raw SQL order fragments.
- **S6** CORS allows only local demo origins (`http://127.0.0.1:5175`, `http://localhost:5175`) with credentials — not `*`.
- **S7 / S9** Session stores user id only (accept a leftover `User` object from old Redis sessions). Cookie: `HttpOnly` + `SameSite=Lax` explicit.
- **S8** Keep accepting current MD5+salt hashes so `demo_user` still logs in. On successful MD5 login, re-hash with BCrypt and save. New registrations use BCrypt.
- **S10** Visual-editor `postMessage` accepts only `event.source === iframe.contentWindow`.
- **S11** Public `POST /chat-history` can only create `user` messages. AI turns stay server-written.
- **S12** Rate-limit IP uses `remoteAddr` only (no client `X-Forwarded-For`).
- **S15** Input guardrail max length matches create-app prompt (8192), not 1000.

### R3 — Optimize

- **O1** Screenshot: one-at-a-time driver; no extra 2s sleep when `document.readyState` is already `complete`.
- **O2** Do not cache an empty featured page.
- **O3** Single-app `getAppVO` may stay as-is (list path already batched).
- **O4** Satisfied by S4 (POST body, not query string).
- **O5** Visual editor injects on iframe `load` (plus server-injected script), not a hard 2s timer.
- **O6** Vue `npm install` skipped when that project already has `node_modules`.

## Out of scope

- Voice / TTS / ChatPartner.
- New product features (billing, OAuth, upload/save/history implementations).
- Rotating gitignored COS / DeepSeek keys.
- Changing committed `application.yml` MySQL `root/123456`.
- Disabling Knife4j.
- Changing the admin-created default password API (`12345678`) — would break admin UI with no display path.
- Archiving `08-17-smoothness` or filling `00-bootstrap-guidelines`.
- Restyling pages that this task does not already have to touch.
- Push to remote.

## Acceptance criteria

- [ ] B1–B9 behave as specified.
- [ ] S1–S12 and S15 (length) closed as specified.
- [ ] File tools and static controller reject `..`, absolute paths, and escaped traversal.
- [ ] Generate is POST; query-string EventSource generate is gone.
- [ ] Preview iframe cannot use the login session; visual edit still can select elements.
- [ ] `demo_user` / `demo123456` still logs in (MD5 upgrade-on-login is allowed).
- [ ] Happy path on `http://127.0.0.1:5175`: login → open existing HTML app → preview loads → deploy/download still work. New generate streams without a false failure toast.
- [ ] O1, O2, O5, O6 applied.
- [ ] Uncommitted visual redesign files are not part of this change set unless a listed bug/vuln file already had to change.

## Key decisions

| Decision | Choice |
|---|---|
| 漏洞 meaning | Security audit items + dead-button cleanup |
| Scope | Maximize fix + optimize; demo login must survive |
| Password | BCrypt for new hashes; MD5 still accepted once then upgraded |
| Preview isolation | iframe `sandbox` without `allow-same-origin`; inject editor script when serving HTML; validate `event.source` |
| Generate transport | POST SSE via `fetch`, not `EventSource` GET |
| Dead buttons | Hide, except preview refresh |
| Task shape | One complex task (no child split) so one implement pass can cover all three phases |
