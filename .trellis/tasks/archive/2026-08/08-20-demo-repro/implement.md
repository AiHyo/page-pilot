# Implement — demo repro

- [x] Pin Vite `port: 5175`, `strictPort: true`; fix leftover “默认 8123” comments.
- [x] Keep env.ts / request.ts 8124-/api defaults.
- [x] Include reasoning model yaml binding, featured 2 min TTL, COS URL join.
- [x] Rewrite README 本地运行 to 8124 + 5175 + deploy.host.
- [x] Commit those files plus `.gitattributes` Trellis journal merge rule. No `application-local.yml`.
- [x] Verify health on 8124, login on 5175, existing HTML preview. Attempt one short generate if the model answers.
- [x] Cache-bust preview iframe after generate; HTML `Cache-Control: no-store`.
- [x] `/static/**` CORS without credentials so sandboxed Vue `origin=null` can load ES modules.

## Rollback

Revert the commit. Ports go back to 8123 defaults.
