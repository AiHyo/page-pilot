# Product Guidelines (Page-Pilot)

> Code-generation product. Not a voice product. Local demo ports: backend **8124**, frontend **5175**.

---

## Guidelines Index

| Guide | Description |
|-------|-------------|
| [Acceptance](./acceptance.md) | Smoothness epic: what “usable” means |

---

## Pre-Development Checklist

- [ ] Do not attach ChatPartner voice
- [ ] Default Java port in yaml is 8123 — local multi-project **must** use 8124
- [ ] Frontend preview/proxy must not silently hit z-ai-agent (8123)
- [ ] After a generate/deploy change, walk [Acceptance](./acceptance.md)
- [ ] Keep generate as POST SSE; do not restore GET EventSource
- [ ] Keep preview iframe sandboxed; do not use `contentDocument` inject
- [ ] Read [backend security contracts](../backend/security-contracts.md) before touching file tools, static, session, or `sortField`

---

## Quality Check

- SSE `done` must not be followed by a false “生成失败” toast
- Preview iframe must load the **generated** site, not 404 / wrong project
- Deploy URL must be reachable on this machine (`/api/static/...` locally)
