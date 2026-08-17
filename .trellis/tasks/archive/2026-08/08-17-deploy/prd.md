# Deploy, COS cover, zip download

## Goal

After HTML generate, one-click deploy copies to `tmp/code_deploy`, returns a reachable URL, optional screenshot/cover, zip download of **source** (`code_output`).

## Acceptance Criteria

- [x] Deploy returns a URL that opens the site on this machine
- [x] Deploy root is `tmp/code_deploy`, not mixed with `code_output`
- [x] Download zip contains the generated files
- [x] Cover URL has no double-slash / wrong host
