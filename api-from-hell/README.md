# API From Hell

API From Hell is a deliberately awkward API for testing REST clients. The endpoints are defined in
one shared JSON catalog and can be served by multiple standalone implementations.

## Shared Source

The canonical endpoint configuration is:

```text
catalog/fromhell-catalog.json
```

Do not duplicate endpoint definitions in implementation code. Implementations should load the
catalog and translate entries into HTTP routes.

## Implementations

- `implementations/node-native-http` - Node.js built-in HTTP server.
- `implementations/node-express` - Express implementation.
- `implementations/python-native-http` - Python standard-library HTTP server.
- `implementations/python-flask` - Flask implementation.
- `implementations/java-javalin` - Javalin implementation.
- `implementations/mockoon` - generated Mockoon environment.

All implementations default to:

```text
http://localhost:3001/fromhell
```

Use these environment variables where supported:

- `PORT` - server port.
- `FROMHELL_PREFIX` - route prefix, default `/fromhell`.
- `FROMHELL_CATALOG` - path to a catalog JSON file.

## Tooling

Validate the shared catalog:

```bash
python tooling/catalog-validator/validate_catalog.py catalog/fromhell-catalog.json
```

Generate OpenAPI:

```bash
python tooling/openapi-generator/generate_openapi.py \
  catalog/fromhell-catalog.json \
  --output docs/openapi.generated.json
```

Generate Mockoon:

```bash
python tooling/mockoon-generator/generate_mockoon.py \
  catalog/fromhell-catalog.json \
  --output implementations/mockoon/fromhell.generated.json
```

Run conformance checks against a running server:

```bash
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3001
```

## Docker Compose

Run one implementation:

```bash
docker compose up fromhell-node-native
```

Each compose service uses a different port so several can run side by side.
