# Jungle Scout Admin

An Angular 20.3.34 / TypeScript admin frontend, updated from the old Angular 9 project so it can run with Node 24. The local mode uses a small Node HTTP server with in-memory fixtures. It does not start or contact the Java backend, PostgreSQL, Google Maps or a remote font service.

Framework/compiler packages use 20.3.34; CLI/build tools use 20.3.41. If you ran `npm audit fix --force` on the earlier ZIP, see `../WINDOWS_STARTUP.md` for recovery.

## Start on Windows, macOS or Linux

Install Node.js 24, extract the full project and open a terminal in `frontend-admin`:

```sh
npm ci
npm start
```

Open http://127.0.0.1:4200/paths. Both local servers start from this one command. Ctrl+C stops both. Use `npm ci` on the first run or after the dependency lockfile changes; use `npm start` on subsequent runs.

| Component | Address | Role |
| --- | --- | --- |
| Angular UI | http://127.0.0.1:4200 | Renders pages and handles browser interactions |
| Frontend API entry point | http://127.0.0.1:4200/api | Proxies HTTP requests to the local API |
| Local mock API | http://127.0.0.1:3100/api | Stores checkpoints and paths in memory and records requests |
| Optional original Java API | http://127.0.0.1:8080/api | Used only by the explicit Java configuration |

The browser reads `/api/state` approximately once per second. External writes appear automatically on the path and checkpoint lists. An unedited path detail refreshes too; a path draft with unsaved edits is kept until you save it. HTTP writes wait for their responses before navigating. Empty paths and one-checkpoint paths are supported.

The schematic map uses coordinates and straight line segments. Local distance is a haversine estimate along those segments and duration assumes 5 km/h. These are testing estimates, not Google walking directions. Path metric fields are recalculated by the local API. All data resets to the demo fixtures when the local API restarts.

## Test commands

```sh
npm test
npm run build
npm run check:credentials
```

`npm test` runs 15 isolated HTTP tests and starts its own temporary API instances. `npm run build` compiles the optimized Angular app. `check:credentials` scans project files and decodes PDF Flate streams; it excludes dependencies, build output and Git history and does not perform image OCR. The delivered archive was also checked separately for the original credential values and its edited PDF examples were visually reviewed.

### Small smoke test against running servers

Leave `npm start` running. In another terminal inside `frontend-admin`:

```sh
npm run test:smoke
```

This creates its own checkpoint and path, verifies reads through port 4200, tests an update and an invalid value, checks the request log, and deletes its own records. It does not reset or delete unrelated demo data. This HTTP-only run does not claim browser rendering passed.

Leave http://127.0.0.1:4200/paths open to verify actual DOM delivery:

```sh
npm run test:smoke -- --require-ui
```

To retain the test records so you can inspect them afterwards:

```sh
npm run test:smoke -- --require-ui --keep
```

A browser receipt records the revision and entity IDs found in the Angular page's rendered DOM. Receipts expire after five seconds without a heartbeat. They are local diagnostics, not an authentication mechanism.

### Automatic frontend/browser test

The self-contained runner starts both servers, waits for the correct API instance, runs the checks and shuts them down. Stop any existing `npm start` before using it.

For HTTP delivery without installing a browser:

```sh
npm run test:e2e -- --http-only
```

For actual browser checks, install Playwright's browser once and run:

```sh
npx playwright install chromium
npm run test:e2e
```

The browser suite checks API-to-DOM updates, a rendered receipt, the public `--require-ui` smoke script, UI-to-API saves, empty/single-checkpoint paths, map checkpoint creation and both creation forms. It fails on uncaught browser errors or external HTTP requests. `npm run test:browser` runs the same browser checks against servers you already started yourself.

On Windows, you can alternatively use an installed Edge browser in PowerShell:

```powershell
$env:PLAYWRIGHT_CHANNEL = "msedge"
npm run test:e2e
Remove-Item Env:PLAYWRIGHT_CHANNEL
```

Advanced runners can set `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH` to an existing compatible Chromium executable. `API_URL` and `FRONTEND_URL` can override the smoke/browser target URLs; the self-contained runner deliberately uses fixed loopback ports 3100 and 4200.

The original Karma/Protractor scaffolding is retained with the old project files. The maintained test commands for this local version are the Node and Playwright commands above.

## Other bot / tester integration

Read [TESTER_HANDOFF.md](TESTER_HANDOFF.md) for the copyable task brief, [API_CONTRACT.md](API_CONTRACT.md) for examples, and [openapi.json](openapi.json) for the formal HTTP schema. Use `http://127.0.0.1:4200/api` when testing the frontend proxy. Both that URL and the direct `3100` API use the same in-memory store.

A bot in another machine/container cannot reach your computer through its own `localhost`. Have it run `npm ci` and the test runner in the workspace containing this project. No public deployment or external account is needed.

## Optional original Java mode

`npm run start:java` starts only Angular and changes the proxy target to port 8080. Run and configure the original Java/Quarkus backend separately if you choose this mode. It polls the original `/paths` and `/checkpoints` endpoints; local `/testing/*` diagnostics are unavailable in Java mode. Local mode never silently falls back to the Java backend.

`npm run build -- --configuration java` compiles that frontend configuration. The Java server and its database are not started by these scripts and were not runtime-tested as part of this change. Its removed credentials remain `example_` placeholders.

## Troubleshooting

- `npm` is not found: install Node.js 24, reopen the terminal and check `node --version` and `npm --version`.
- `npm ci` fails after manually changing `package.json`: use `npm install` to regenerate the lockfile, then commit/share the new lockfile too.
- A port is already in use: stop the previous local process using that port. `npm start` intentionally fails rather than using an unexpected API instance or changing the port.
- The UI says the API is unavailable: keep `npm start` running. For separate terminals, run `npm run start:api` and `npm run start:frontend`.
- `--require-ui` times out: open `/paths` in a real browser, wait for Connected, and rerun. Alternatively use the automatic browser suite.
- Browser installation is blocked by your network: the HTTP-only runner still works; use an installed compatible browser for the browser test.

Toolchain compatibility: https://angular.dev/reference/versions . Proxy configuration: https://angular.dev/tools/cli/serve .
