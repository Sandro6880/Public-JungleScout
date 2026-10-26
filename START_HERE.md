# Jungle Scout: local admin frontend

The admin frontend is Angular/TypeScript. The original backend is Java 11 / Quarkus with PostgreSQL.
The default local setup starts the Angular UI and a small in-memory Node test API. Java, a database and API credentials are not needed.

## Start

Use Node.js 24 (Node 22.12+ in the 22.x series is also supported). Extract this ZIP, open a terminal in its root and run:

```sh
cd frontend-admin
npm ci
npm start
```

Open http://127.0.0.1:4200/paths. Keep the terminal open. Ctrl+C stops both servers.

| Purpose | URL |
| --- | --- |
| Admin UI | http://127.0.0.1:4200 |
| API through the frontend proxy | http://127.0.0.1:4200/api |
| Direct local test API | http://127.0.0.1:3100/api |

## Quick checks

In a second terminal inside `frontend-admin`, with `npm start` still running:

```sh
npm run test:smoke
```

For proof that a browser rendered the sent values, leave `/paths` open and run:

```sh
npm run test:smoke -- --require-ui
```

For a single command that starts its own servers, checks HTTP delivery and then stops them, stop `npm start` first and run:

```sh
npm run test:e2e -- --http-only
```

The full browser suite is described in `frontend-admin/README.md`. The other-bot instructions are in `frontend-admin/TESTER_HANDOFF.md`. The machine-readable API contract is `frontend-admin/openapi.json`.

Credential verification is recorded in `docs/CREDENTIAL_VERIFICATION.md`. All identified embedded credentials were replaced with `example_` placeholders. This ZIP includes the original Android projects, Java backend and documentation as well as the updated admin frontend.
