# Implement — visual chat

## Checklist

- [x] `BasicLayout`: `isChatPage` full-bleed; hide header/footer on `/app/chat`.
- [x] `AppChatPage` template: iOS toolbar, quieter labels, drop emoji feature grid and purple generating bar.
- [x] `AppChatPage` styles: tokens only; two-pane desktop; stacked mobile; user bubble accent; AI surface.
- [x] Confirm home / auth / header / `AppCard` already use tokens; fix leftover `#1890ff` / purple if any.
- [x] Browser: login → home → HTML chat at 1280 and 390. Preview sandbox still set. No generate transport change.

## Validation

- `npx vue-tsc --noEmit` in `page-pilot-frontend`
- Manual on `http://127.0.0.1:5175` as `demo_user`

## Do not touch

- Generate POST / `sse.ts` / path jail / cookie / CORS
- Admin pages
- Backend Java except files already dirty from smoothness if they must stay uncommitted
