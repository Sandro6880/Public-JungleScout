# Windows startup and dependency recovery

Use Node.js 24 LTS from https://nodejs.org/en/download . Choose Windows and the Windows Installer (.msi) matching your computer. If you already use a Node version manager, select Node 24 through that manager instead.

After updating, close and reopen PowerShell. Restart VS Code as well if you use its integrated terminal. Check:

```powershell
node --version
npm --version
```

The Node version should begin `v24.`. Use the npm that comes with Node; a separate global npm upgrade is not needed for this project.

## Recover an existing copy after npm audit fix --force

1. Stop the running local app with Ctrl+C.
2. Copy BOTH `frontend-admin/package.json` and `frontend-admin/package-lock.json` from this corrected ZIP into your existing `frontend-admin` directory. This restores a consistent dependency set; it does not replace your app source files.
3. In PowerShell, inside that `frontend-admin` directory, run:

```powershell
npm ci
npm start
```

`npm ci` recreates the installed dependencies from the supplied lockfile. You do not need to delete the lockfile, use `--force`, or install Angular globally.

Open http://127.0.0.1:4200/paths . The local test API starts on port 3100 together with the frontend.

If you are starting from the full new ZIP instead, extract it into a new folder, enter its `frontend-admin` folder and use the same two commands.

## Why the pasted commands failed

- Node 22.11.0 is older than this Angular toolchain's supported Node 22.12+ minimum. Node 24 is the recommended, tested setup for this project.
- `npm audit fix --force` moved some Angular packages to 20.3.34 while others remained at 20.3.0. Their exact peer requirements then conflicted.
- The attempted npm 12.2.1 upgrade requires Node 22.22.2+, 24.15+ or 26+. It cannot run on Node 22.11.0, and it is not required to start this app.

This corrected package set keeps the Angular framework and compiler packages on 20.3.34 and Angular CLI/build tools on 20.3.41. `npm audit` is a read-only diagnostic command. If a future report appears, update the Angular package family together and re-run the build/tests rather than repeatedly forcing individual updates.
