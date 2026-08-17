# Error Handling

> How errors are handled in this project.

---

## Overview

<!--
Document your project's error handling conventions here.

Questions to answer:
- What error types do you define?
- How are errors propagated?
- How are errors logged?
- How are errors returned to clients?
-->

(To be filled by the team)

---

## Error Types

<!-- Custom error classes/types -->

(To be filled by the team)

---

## Error Handling Patterns

`BusinessException` → `GlobalExceptionHandler` → JSON `BaseResponse` for normal HTTP.

SSE (`Accept: text/event-stream` or URI contains `/chat/gen/code`): write `event: business-error` only. Do **not** emit `event: done` after an error — the chat page treats `done` as success.

Frontend generate client is `postSse` in `page-pilot-frontend/src/utils/sse.ts`. `business-error` is terminal.

---

## API Error Responses

<!-- Standard error response format -->

(To be filled by the team)

---

## Common Mistakes

<!-- Error handling mistakes your team has made -->

(To be filled by the team)
