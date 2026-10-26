# Copyable task brief for the tester bot

You are working on the Jungle Scout Angular/TypeScript admin frontend in `frontend-admin/`. The default app runs without the original Java/Quarkus backend or a database.

Build/extend an automated tester for the existing local frontend API and browser UI. First read `README.md`, `API_CONTRACT.md`, `openapi.json` and the existing `tools/*` test scripts. Preserve this contract and the current frontend workflows.

## Start and connect

- Use Node.js 24. Run `npm ci` inside `frontend-admin`.
- `npm start` launches Angular at `http://127.0.0.1:4200` and a local in-memory test API at `http://127.0.0.1:3100/api`.
- Use `BASE_URL=http://127.0.0.1:4200/api` for HTTP tests that exercise the frontend proxy. The direct API at port 3100 accesses the same store.
- No authentication, API keys, Google account, Java process or PostgreSQL process is required in local mode. Keep any sample credential literal prefixed `example_`.
- Run in the same machine/container as the app. Your own localhost cannot reach a different computer's localhost.

## What to test

1. Verify `/api/health` through both ports and compare `instanceId` before sending writes.
2. Create your own checkpoints with `POST /api/checkpoints`; save their returned IDs. Create a path with `POST /api/paths`, referencing those IDs. Never guess creation IDs.
3. Verify HTTP status codes, returned JSON, read-after-write behavior and validation errors. Use PUT for updates; PATCH is not implemented.
4. Open `/paths` and `/all-poi` in a browser and assert that created/updated values appear automatically. A successful HTTP response alone is not proof of rendered UI delivery.
5. Assert UI-to-API saves, missing/invalid fields, invalid latitude/longitude, missing IDs, empty paths, single-checkpoint paths, checkpoint order, checkpoint updates propagating to paths, deletion/detachment and stale/deleted paths.
6. For a rendered receipt, poll `GET /api/testing/frontend`. A fresh client with the expected revision and entity ID in `visiblePathIds` or `visibleCheckpointIds` reports that the Angular page found that entity in its DOM. The `/paths` page is required by the supplied `--require-ui` smoke test.
7. Inspect `GET /api/testing/requests` for successful/failed requests when diagnosing failures. Bodies are deliberately not logged.
8. Delete only records created by your test. Use `POST /api/testing/reset` only when you intentionally want to replace the entire in-memory demo store. Mock data is not production persistence, and mock metrics are not real road directions.

## Existing checks to reuse

- `npm test`: 15 isolated local API tests.
- `npm run build`: optimized Angular build.
- `npm run check:credentials`: source credential scan.
- `npm run test:smoke`: HTTP delivery against already-running servers; cleans up its own records.
- `npm run test:smoke -- --require-ui`: also verifies a receipt from an open `/paths` page.
- `npm run test:e2e -- --http-only`: starts the local app, verifies HTTP delivery and stops it.
- `npx playwright install chromium` once, then `npm run test:e2e`: starts the local app and runs full browser checks.

Keep API-only assertions and actual browser assertions distinct in the test report. Report the exact command used, which checks ran, any skipped browser checks, and any limitations. Add new tests/features without replacing the existing app with a static mock page or reconnecting it to Google/the Java backend.
