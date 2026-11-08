# Jungle Scout

Jungle Scout began as a diploma project for exploring European rainforests. It represents a walk as an ordered route through checkpoints, each with a location and a description. The project is evolving into a year-round city guide for residents and visitors who want to discover interesting places, follow themed walks, and learn the history behind each stop.

The repository brings together an Android app for exploring routes, an admin frontend for managing them, and the original backend for storing and serving the data. The [roadmap](ROADMAP.md) outlines the work toward the first city pilot and the transition to open pedestrian navigation.

## What you can use today

The local admin frontend lets you create and edit paths and checkpoints, attach existing checkpoints to a path, and change the order of stops. You can also create a checkpoint from a position on the schematic map. Paths can share checkpoints, so editing a shared checkpoint updates it across the paths that use it.

The local map draws straight segments between coordinates. Distance is estimated along those segments, and duration assumes a walking speed of 5 km/h. The local API loads demo data into memory each time it starts. This setup is useful for exploring the editor and testing the route and checkpoint API on your own machine.

| Directory | Contents |
| --- | --- |
| [`frontend-admin/`](frontend-admin/) | Angular and TypeScript admin UI, Node.js mock API, and automated tests |
| [`backend/`](backend/) | Original Java 11 and Quarkus REST API with PostgreSQL |
| [`Android-Frontend/`](Android-Frontend/) | Kotlin Android app with route, checkpoint, and map screens |
| [`frontend/`](frontend/) | Additional Android map prototype sources |
| [`docs/`](docs/) | Project materials and local admin validation notes |

## Run the local admin

Install Node.js 24. The project also accepts Node.js 22.12 or later within the 22.x series. From a terminal, run the following commands.

```sh
git clone https://github.com/Sandro6880/Public-JungleScout.git
cd Public-JungleScout/frontend-admin
npm ci
npm start
```

Open [the path list](http://127.0.0.1:4200/paths). `npm start` launches Angular on port `4200` and the local API on port `3100`. The frontend proxies `/api` requests to that API. Press `Ctrl+C` in the terminal to stop both processes.

## Verify the local setup

From `frontend-admin/`, run the API tests, build the UI, and check delivery through the frontend proxy. Stop `npm start` before running the self-contained end-to-end command so it can start its own servers.

```sh
npm test
npm run build
npm run test:e2e -- --http-only
```

For the full browser checks, install Chromium with `npx playwright install chromium` and run `npm run test:e2e`. See the [admin setup guide](frontend-admin/README.md) for test details and troubleshooting. The [API contract](frontend-admin/API_CONTRACT.md) describes the local endpoints and data formats.

## Original Android and Java setup

The original integration uses a separate PostgreSQL development database and the Java backend. Configure the database connection locally using the settings in [`application.properties`](backend/src/main/resources/application.properties). With Java 11, run `sh ./mvnw compile quarkus:dev` from `backend/` on macOS or Linux, or `./mvnw.cmd compile quarkus:dev` in Windows PowerShell. The [backend guide](backend/README.md) covers the Maven workflow.

Open `Android-Frontend/` in Android Studio. Set the Retrofit `BASE_URL` to a backend address reachable from the device or emulator, ending in `/api/`. The original map screens use Google Maps configuration. The API address is defined in [`JungleScoutApiService.kt`](Android-Frontend/app/src/main/java/at/htl/android_frontend/network/JungleScoutApiService.kt).

To connect the admin UI to the original Java API on port `8080`, start it with `npm run start:java` from `frontend-admin/`.

## License

Jungle Scout is distributed under the [Mozilla Public License 2.0](LICENSE).

