# Jungle Scout roadmap

The goal is to turn a diploma project for European rainforest trails into a city guide for residents and visitors. A walk should help someone discover places they might otherwise pass by and understand the history of each stop. The first pilot will focus on one city, with walks for different interests and options for different seasons.

The [README](README.md) describes the current Android, admin, and backend components. The milestones below describe proposed work. Each one has a result that can be reviewed before starting the next.

## 1. Clarify the project structure

- [ ] Identify which Android source tree to maintain and document the purpose of the other source tree. Review the existing admin and backend workflows before deciding what to move or replace.
- [ ] Separate generated caches and archived builds from the maintained source layout so contributors can reproduce builds from the source and dependency lockfiles.
- [ ] Document one development setup for the maintained components, including local configuration, demo data, and how each app connects to the API.
- [ ] Agree on a shared contract for routes and checkpoints. Capture compatibility checks for the operations the admin and Android app rely on.

Verify that a contributor can run the admin from a clean checkout and find the entry points for each maintained component.

## 2. Build a persistent city catalog

- [ ] Add city and neighborhood metadata so the catalog can contain more than one area. Keep routes as ordered collections of reusable checkpoints.
- [ ] Add fields for short descriptions, longer stories, source references, photos and credits, and practical details such as access and seasonality. Record when practical information was checked.
- [ ] Define how existing rainforest routes migrate into the new catalog while preserving checkpoint references and order.
- [ ] Make local workspaces survive restarts. Keep demo and test data in separate stores, and provide a way to recover from a damaged workspace.

Verify that an existing route and a new city walk both survive a restart with their checkpoint order and relationships intact.

## 3. Make city content easier to maintain

- [ ] Improve the route and checkpoint editor with reliable drafts, clear save feedback, and easier map and ordering controls.
- [ ] Add import and export with a preview so authors can review invalid or duplicate entries before saving. Provide route variants for comparing possible detours and alternative stops.
- [ ] Add route quality checks for issues such as unusually long gaps or missing content. Link each finding to the place where an author can fix it.
- [ ] Add checkpoint inspection runs so maintainers can record issues and notes at each stop, pause an inspection, and review the results afterwards.
- [ ] Give authors a way to preview and review a route before publishing it. Serve published content to visitors and restrict editing to maintainers.

Verify that an author can assemble a city walk, correct a flagged issue, and preview the route as a visitor would see it.

## 4. Build the visitor guide

- [ ] Add a city and neighborhood browser. Let visitors filter walks by theme, estimated duration, and season or indoor/outdoor suitability where that information is available.
- [ ] Create a route overview with a map, ordered stops, and practical details. Add checkpoint pages with stories, photographs, and source references.
- [ ] Build the main browse and walk flow for the maintained Android app. Test it on a phone with realistic content from the pilot city.

Verify that a new visitor can find a walk that suits their available time and read the story at each stop in order.

## 5. Add open pedestrian navigation

- [ ] Evaluate an open-source pedestrian routing engine using a small map extract for the pilot city. Document the map data, licenses, attribution, and how to run the prototype locally.
- [ ] Separate map display from routing so the apps can render a route and its directions through a shared interface. Calculate a walking route between each pair of ordered checkpoints and show the resulting geometry, directions, and estimates.
- [ ] Handle missing pedestrian connections explicitly. Keep the checkpoint list usable and show which part of the walk needs review when routing fails.
- [ ] Compare generated directions against known walks and field inspections, including crossings, barriers, and disconnected paths. Use the results to decide when a route is ready to publish.

Verify that the pilot can route a walk along the pedestrian network and identify a missing connection for review.

## 6. Pilot the guide and introduce the new name

- [ ] Publish a small set of curated walks in the pilot city. Use checkpoint inspection runs to record problems, check stories and sources, and test the directions on foot.
- [ ] Collect feedback on finding, starting, and following a walk. Use it to refine the visitor experience and the admin workflow.
- [ ] Document how to contribute code or route content. Include the licenses and attribution for maps, photos, and other third-party material used in the pilot.
- [ ] Choose the public name once the city guide is ready. Update the app identity and project documentation as part of that release.

Verify that a visitor can discover and follow a published pilot walk, and that a contributor can reproduce the development setup and submit a correction.
