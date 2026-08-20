# Design — demo repro

## Boundaries

- `page-pilot-frontend/vite.config.ts`: host, port 5175, strictPort, proxy 8124.
- `page-pilot-frontend/src/config/env.ts`, `request.ts`, tracked `.env` / `.env.development`: same-origin `/api`.
- `ReasoningStreamingChatModelConfig`: `modelName` from `@ConfigurationProperties`.
- `RedisCacheManagerConfig` / `TencentCosManager`: already correct in the working tree; include in the same commit.
- `README.md`: the only human start path.

Do not change generate POST, sandbox, or session cookie contracts.

## Compatibility

- People who still run backend on 8123 can set `VITE_PROXY_TARGET=http://localhost:8123`.
- Deploy URLs for existing apps stay `/api/static/{deployKey}/`.
