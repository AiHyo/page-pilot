# Design — visual chat

## Boundaries

| Layer | Owns |
|---|---|
| `src/styles/tokens.css` + `DESIGN.md` | Color, type, radius. Do not invent new hexes. |
| `BasicLayout.vue` | Full-bleed for `/`, `/user/*`, `/app/chat/*`. Hide header/footer on chat. |
| `AppChatPage.vue` | Toolbar + panes + tokens. Keep script behavior. |
| Home / auth / header / card | Finish the existing uncommitted restyle; no second visual language. |

## Layout

```
Chat (>=768px)
[ toolbar: back · title · type          详情  下载  部署 ]
[ conversation (42%) | preview (58%) ]

Chat (<768px)
[ toolbar ]
[ conversation flex 1 ]
[ preview min-height 40vh ]
```

Site header/footer off on `/app/chat/:id` so the page is one workspace.

## Compatibility

- `postSse`, `sandbox`, `onPreviewIframeLoad`, owner gates, `appId` as string: no logic rewrite except class names.
- Preview iframe still `/api/static/...`.
- Ant Design stays for dropdown, spin, modal, menu. Override colors with tokens, not `color="purple"`.

## Trade-off

Hiding the global header on chat removes GitHub / admin from that screen. Home is one click via the toolbar back control. Worth it: double chrome is the actual visual break.
