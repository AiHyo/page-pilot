# Make local demo reproducible

## Goal

A second machine (or a restarted shell) can start PagePilot on **8124 / 5175** without remembering extra flags, and `demo_user` can still walk generate → preview → deploy.

## User value

The demo does not depend on one person’s command history.

## Background

- User (2026-08-20): continue into this phase after visual/harden.
- Working tree still has uncommitted smoothness wiring: frontend no longer defaults to 8123, Vue model reads yaml, featured cache 2 min, COS URL join.
- `application.yml` server.port remains **8123**. Local must override to **8124** (8123 is z-ai-agent on this machine).
- `.env` files contain only ports/paths, no API keys. `application-local.yml` stays gitignored.

## Requirements

- **D1** Frontend defaults: API `/api`, Vite proxy `http://localhost:8124`, deploy domain `http://localhost:8124/api/static`, Vite `host 127.0.0.1` **port 5175 strictPort**.
- **D2** `request.ts` must not fall back to `http://localhost:8123/api`.
- **D3** Vue reasoning model name comes from yaml (`langchain4j.open-ai.chat-model.model-name`), not a hardcoded ModelScope id.
- **D4** Featured cache TTL for `featured_app_page` is 2 minutes. COS `getFileUrl` does not produce `//` in the path.
- **D5** README local-run section matches D1 and documents `--server.port=8124` plus `-Dapp.code.deploy.host=http://localhost:8124/api/static`.
- **D6** Do not commit `application-local.yml` or API/COS keys.
- **D7** After wiring is in, walk as `demo_user`: login, open existing HTML app preview, then one **new** HTML generate if the model is reachable. Fix anything that breaks.

## Out of scope

- Changing production yaml port for everyone to 8124 without documenting the Agent conflict.
- Admin restyle, bootstrap guidelines, voice.
- Push to remote.

## Acceptance

- [ ] `npm run dev` in `page-pilot-frontend` listens on `127.0.0.1:5175` and proxies `/api` to 8124.
- [ ] README start commands match the ports used in the last successful demo.
- [ ] Login `demo_user` / `demo123456` on 5175 still works; existing HTML preview still sandboxed.
- [ ] No secrets in the commit.

## Key decisions

| Decision | Choice |
|---|---|
| Backend yaml port | Keep 8123 in `application.yml`; README requires 8124 locally |
| Frontend port | 5175 in `vite.config.ts` |
| `.env` | Commit the already-tracked files if they only contain ports |
