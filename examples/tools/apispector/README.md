# API tests

This folder is an [API Spector](https://github.com/testsmith-io/api-spector) workspace.
Everything here is plain JSON — diff it, commit it, review it like any other code.

## Layout

```
apichallenges.spector      ← workspace manifest (this is what you "open")
collections/              ← your request collections
environments/             ← per-env variable files (dev, staging, prod, …)
mocks/                    ← saved mock servers (optional)
contracts/                ← pinned OpenAPI snapshots (optional)
.gitignore                ← excludes secrets, generated docs, run reports
.vscode/settings.json     ← maps *.spector to JSON for editor highlighting
```

## Open the workspace

```bash
# launches the GUI in this folder
npx -y @testsmith/api-spector ui
```

## Run the tests from CI

```bash
npx -y @testsmith/api-spector run \
  --workspace ./apichallenges.spector \
  --environment ci \
  --output reports/results.xml --format junit
```

Other useful commands:

| Command | What it does |
|---|---|
| `api-spector run`       | Execute requests / assertions |
| `api-spector mock`      | Start mock servers from this workspace |
| `api-spector contract`  | Manage and run pinned contract snapshots |
| `api-spector wsdl`      | Inspect a WSDL or import as collection / mock |

## A note on secrets

Secret values (passwords, OAuth client secrets, API keys) are stored in your
OS keychain — **not** in this folder. Environment files only reference the
keychain entry by name, so it's safe to commit them.
