# Local admin API contract

Base URL through the frontend: `http://127.0.0.1:4200/api`.
Direct local API: `http://127.0.0.1:3100/api`.
Both access the same in-memory store. All nonempty write bodies use `Content-Type: application/json`. No credentials are required. Collection POST returns 201, GET/PUT returns 200, deletion returns 204, and invalid values return JSON errors with the appropriate status.

| Method | Path after `/api` | Behavior |
| --- | --- | --- |
| GET | `/health` | Mode, instance ID, revision and storage kind |
| GET | `/state` | Consistent snapshot: revision, paths and checkpoints |
| GET / POST | `/checkpoints` | List / create checkpoints |
| GET / PUT / DELETE | `/checkpoints/{id}` | Read / update / delete one checkpoint |
| GET / POST | `/paths` | List / create paths |
| GET / PUT / DELETE | `/paths/{id}` | Read / update / delete one path |
| GET | `/paths/{id}/checkpoints` | Read ordered checkpoint membership |
| POST / PUT / DELETE | `/paths/{id}/checkpoints` | Append / replace order / detach an array of checkpoint IDs or `{ "id": n }` references |
| GET | `/testing/requests` | Last 50 business requests, capped by a 200-entry history |
| GET | `/testing/frontend` | Fresh browser receipts (last five seconds) |
| POST | `/testing/frontend` | Angular DOM heartbeat; test diagnostics only |
| POST | `/testing/reset` | Reset all data to initial fixtures; send `{}` |

## Example write sequence

Create a checkpoint:

```http
POST http://127.0.0.1:4200/api/checkpoints
Content-Type: application/json

{"name":"example_test_checkpoint","cpLocation":"Example location","gpsLat":"48.27","gpsLng":"14.25","details":"Example details"}
```

Read its `id` from the response, then create a path using that ID:

```json
{"name":"example_test_path","checkpoints":[{"id":4}]}
```

The `4` above is illustrative. Always use the actual response ID. Send that body to `POST /api/paths`. The API also accepts `[4]` as the checkpoint reference array. A path can have no checkpoints. Supplied full checkpoint objects are resolved by ID to the canonical checkpoint record; editing embedded checkpoint fields does not change that record. Use `PUT /api/checkpoints/{id}` for checkpoint edits.

Update with `PUT /api/paths/{id}`:

```json
{"name":"example_updated_path"}
```

PUT supports partial updates. Omitted fields keep their current value. Supply `checkpoints` to replace membership/order. `pathLength` and `pathDuration` are read-only local estimates and are recalculated even if included by legacy clients. Delete your path first, then delete the checkpoint(s) you created.

## Fields and validation

- Checkpoint: `id` (assigned by server), `name` (required, nonblank string, max 160), `cpLocation` (optional string, max 250), `gpsLat` (required finite number/numeric string from -90 to 90), `gpsLng` (required finite number/numeric string from -180 to 180), `details` (optional string, max 4000). Coordinates are returned as strings; numeric zero is valid. Null, booleans and blank coordinates are invalid.
- Path: `id` (assigned by server), `name` (required, nonblank string, max 160), `checkpoints` (array of existing references, no duplicate IDs, max 500), `pathLength` (string, km) and `pathDuration` (string, HH:MM:SS).
- Omit `id` on create. If present on update, it must match the URL. Unknown write fields are rejected.
- Updating a checkpoint changes the checkpoint data read through all attached paths. Deleting a checkpoint detaches it from every path. Deleting a path does not delete its checkpoints.
- Successful writes increase the global `revision`; rejected requests do not. Reset also increases it monotonically.
- Errors: 400 invalid data/JSON, 403 unsupported browser Origin, 404 missing resource/endpoint, 405 unsupported method, 413 body over 1 MiB, 415 unsupported nonempty body content type. Error body: `{ "error": "Explanation", "requestId": "req_123" }`.
- Responses include `X-Mock-Revision` and `X-Request-ID`. Browser origins are restricted to `http://127.0.0.1:4200` and `http://localhost:4200`. Non-browser local test clients need no Origin header.

## Rendered frontend evidence

Angular polls the state, renders it, then reports a heartbeat based on the DOM's revision and entity attributes. `GET /api/testing/frontend` returns:

```json
{"revision":5,"clients":[{"clientId":"example_ui_client","revision":5,"route":"/paths","visiblePathIds":[1,2],"visibleCheckpointIds":[],"lastSeenMs":0}]}
```

IDs/timestamps are illustrative. Only fresh clients are returned. A receipt for a specific ID is useful evidence of a rendered row; direct browser assertions should still inspect the expected text when testing field values. `clientId` is a public diagnostic label, not a secret or access token.

See `openapi.json` for machine-readable request and response schemas. These endpoints define the local mock contract; they do not claim every original Java response/status is identical.
