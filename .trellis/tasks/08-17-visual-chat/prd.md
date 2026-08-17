# Ship iOS visual system onto chat

## Goal

Login, home, and the generate/preview chat page look like one iOS grouped product. The already-written `DESIGN.md` tokens are the source of truth. Generate (POST SSE) and sandboxed preview keep working.

## User value

A demo walk no longer jumps from an iOS home into a 2018 gray chat studio.

## Background

- User (2026-08-17): finish-work **and** ship the uncommitted visual work onto chat.
- Design read: redesign-preserve of a Vue code-generation shell. Language is **iOS grouped interface** (`DESIGN.md`). Not purple SaaS, not a new aesthetic.
- Dials: variance 4, motion 3, density 5. Light grouped background only.
- Tokens already in `page-pilot-frontend/src/styles/tokens.css` and imported from `main.ts`.
- Home / auth / header / cards are already restyled in the working tree (uncommitted). Chat (`AppChatPage.vue`) is still `#f5f5f5` / `#1890ff` / purple tag, and sits inside `BasicLayout`’s 980px column plus header/footer.
- Security contracts from `08-17-harden-optimize` stay: POST generate, iframe `sandbox` without `allow-same-origin`, string snowflake `appId`.

## Requirements

- **V1** Home, login, register, header, footer, `AppCard` use `--bg / --surface / --ink / --mute / --line / --accent` and the grouped radius. No leftover Ant Design purple primary on those surfaces.
- **V2** Chat is full-bleed: no site header/footer, no 980px clamp. Own iOS toolbar (back/home, title, type, 详情 / 下载 / 部署).
- **V3** Chat messages, composer, preview chrome use the same tokens. User bubble `--accent`, AI bubble `--surface` on `--bg`. Send control is system blue, not a circular Ant primary clash.
- **V4** Narrow viewport (`< 768px`): chat stacks (messages then preview) and remains usable. Desktop keeps side-by-side.
- **V5** Do not change generate/download/deploy/edit/sandbox behavior. Keep POST `postSse`, `sandbox` attribute, owner gates, string `appId`.
- **V6** Placeholder / generating copy stays short and plain. Drop the four-emoji feature grid and purple gradient status bar.

## Out of scope

- New features (upload, history drawer, billing).
- Dark mode (tokens are light iOS grouped).
- Restyling admin pages.
- Replacing Ant Design Vue.
- Changing backend, Vite proxy, or env defaults except if a visual file already has a needed smoothness fix and must ship with this set.
- Voice.

## Acceptance

- [ ] Login / home / chat share the same background, accent, and type stack.
- [ ] Chat at 1280px is a two-pane workspace; at 390px it stacks without horizontal overflow.
- [ ] Open `http://127.0.0.1:5175` as `demo_user`, enter HTML app `446839947227643904`: history + sandboxed preview still show; Deploy/Download still owner-gated.
- [ ] Iframe still has `sandbox` without `allow-same-origin`.
- [ ] No generate transport change.

## Key decisions

| Decision | Choice |
|---|---|
| Visual language | Existing `DESIGN.md` iOS grouped. Preserve, do not invent a second system. |
| Chat chrome | Hide global header/footer; local toolbar only. |
| Motion | CSS hover/press only. |
