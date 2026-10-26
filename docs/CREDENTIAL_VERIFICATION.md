# Credential verification

Verified again on 24 October 2026 for this delivered project archive.

| Credential setting | Replacement |
| --- | --- |
| Google Maps Android key | `example_google_maps_android_api_key` |
| Google Directions Android key | `example_google_maps_directions_api_key` |
| Other Google Maps key copies | `example_google_maps_api_key` (removed entirely from the admin runtime, which now uses a local map preview) |
| Mail sender | `example_sender@example.invalid` |
| Mail password | `example_email_password` |
| Database username | `example_database_username` |
| Database password | `example_database_password` |

The original three Google key values, mail sender and mail password were checked against all project/ZIP members. Known credential literals and configuration values were checked for the `example_` prefix. Provider-token/private-key patterns were scanned. PDFs were checked using extracted text and decoded streams; documentation images had been OCR-scanned, and the modified thesis examples were visually verified. The old credential screenshot was removed from the rewritten PDF's embedded objects, not just covered visually. Both PDFs and all Android/backend project files are retained.

The independent recheck of the previously delivered 203-file ZIP passed. The final updated archive is scanned again before delivery, including its added source and documentation. No supported live-looking credential patterns or original credential strings were found. No credential was tested against a service, so revocation/active status is outside this check. No Git history was supplied.

For repeatable source checks, run `npm run check:credentials` inside `frontend-admin`. That dependency-free Node script excludes dependency/build folders and Git history and does not do image OCR. Full source verification is not a claim that every possible obfuscated/unknown credential format can be detected.
