# Implement — demo repro

- [x] Pin Vite `port: 5175`, `strictPort: true`; fix leftover “默认 8123” comments.
- [x] Keep env.ts / request.ts 8124-/api defaults.
- [x] Include reasoning model yaml binding, featured 2 min TTL, COS URL join.
- [x] Rewrite README 本地运行 to 8124 + 5175 + deploy.host.
- [ ] Commit those files plus `.gitattributes` Trellis journal merge rule. No `application-local.yml`.
- [ ] Verify health on 8124, login on 5175, existing HTML preview. Attempt one short generate if the model answers.

## Rollback

Revert the commit. Ports go back to 8123 defaults.
