# Local admin validation

Verified on 24 October 2026 with Node.js 24.22.1 and npm 11.9.0.

| Check | Result |
| --- | --- |
| Fresh install from the delivered lockfile (`npm ci`) | PASS |
| Optimized Angular build (`npm run build`) | PASS |
| Optional Java-mode frontend compilation | PASS |
| Isolated mock API tests (`npm test`) | PASS: 15/15 |
| HTTP integration (`npm run test:e2e -- --http-only`) | PASS |
| API-created values automatically appear in Angular | PASS |
| Public smoke script with `--require-ui` and a DOM receipt | PASS |
| External updates appear in an unedited path detail | PASS |
| UI saves are readable through the local API | PASS |
| Empty and single-checkpoint path detail views | PASS |
| Click-to-create checkpoint on the local map | PASS |
| Path and checkpoint creation forms | PASS |
| Uncaught browser errors | None |
| External browser HTTP requests in local mode | None |
| OpenAPI internal references | PASS: 145 references resolved across 10 routes |
| Repeatable source credential scanner | PASS |

The full browser suite ran through Playwright using a local headless Chromium 138 executable override. The default Playwright browser CDN was unavailable in this execution environment, so that download was not used for the successful run. The project supports either the normal Playwright browser install, an installed Edge channel, or an explicit compatible Chromium executable. The browser binary itself is not included in the ZIP.

No Java runtime, PostgreSQL database, Google Maps call, external account or real credential was used for the passing local/frontend checks. The optional Java-mode configuration was compiled; the original Java backend was not runtime-tested. The local API keeps data in memory and recomputes approximate straight-line route metrics.

The original credential values and all final ZIP members are checked before delivery. See `CREDENTIAL_VERIFICATION.md` for that scope and the exact placeholder names. Generated build files, dependencies, private test tooling and browser binaries are excluded from the deliverable archive; `npm ci` recreates the project dependencies.
