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
- `implementations/java-raw-http` - Java raw socket exact-response implementation.
- `implementations/mockoon` - generated Mockoon environment.

All local implementations default to:

```text
http://localhost:3001/fromhell
```

All commands below assume the current working directory is this `api-from-hell` folder.

Use these environment variables where supported:

- `PORT` - server port.
- `FROMHELL_PREFIX` - route prefix, default `/fromhell`.
- `FROMHELL_CATALOG` - path to a catalog JSON file.

## Run The Implementations

The plain Docker examples below run one implementation at a time and expose it on
`http://localhost:3001/fromhell`. The Docker Compose examples use separate host ports so the
implementations can run side by side.

To stop a foreground local or Docker command, press `Ctrl+C`. To stop Compose services, run:

```bash
docker compose down
```

### Node.js Native HTTP

Run locally with Node.js:

```bash
node implementations/node-native-http/server.js
```

Build and run with Docker:

```bash
docker build -t api-from-hell-node-native -f implementations/node-native-http/Dockerfile .
docker run --rm -p 3001:3001 api-from-hell-node-native
```

Or run the Compose service:

```bash
docker compose up --build fromhell-node-native
```

Compose URL:

```text
http://localhost:3001/fromhell
```

### Node.js Express

Install dependencies and run locally with Node.js:

```bash
npm --prefix implementations/node-express install
npm --prefix implementations/node-express start
```

Build and run with Docker:

```bash
docker build -t api-from-hell-node-express -f implementations/node-express/Dockerfile .
docker run --rm -p 3001:3001 api-from-hell-node-express
```

Or run the Compose service:

```bash
docker compose up --build fromhell-node-express
```

Compose URL:

```text
http://localhost:3002/fromhell
```

### Python Native HTTP

Run locally with Python:

```bash
python implementations/python-native-http/server.py
```

Build and run with Docker:

```bash
docker build -t api-from-hell-python-native -f implementations/python-native-http/Dockerfile .
docker run --rm -p 3001:3001 api-from-hell-python-native
```

Or run the Compose service:

```bash
docker compose up --build fromhell-python-native
```

Compose URL:

```text
http://localhost:3003/fromhell
```

### Python Flask

Create a virtual environment, install dependencies, and run locally with Python:

```bash
python -m venv implementations/python-flask/.venv
implementations/python-flask/.venv/bin/python -m pip install -r implementations/python-flask/requirements.txt
implementations/python-flask/.venv/bin/python implementations/python-flask/server.py
```

In PowerShell, use the Windows virtual-environment Python path:

```powershell
python -m venv implementations\python-flask\.venv
implementations\python-flask\.venv\Scripts\python -m pip install -r implementations\python-flask\requirements.txt
implementations\python-flask\.venv\Scripts\python implementations\python-flask\server.py
```

Build and run with Docker:

```bash
docker build -t api-from-hell-python-flask -f implementations/python-flask/Dockerfile .
docker run --rm -p 3001:3001 api-from-hell-python-flask
```

Or run the Compose service:

```bash
docker compose up --build fromhell-python-flask
```

Compose URL:

```text
http://localhost:3004/fromhell
```

### Java Raw HTTP

Run locally with Maven and Java 21. Set `FROMHELL_CATALOG` because these commands are run from
the `api-from-hell` folder:

```bash
FROMHELL_CATALOG=catalog/fromhell-catalog.json mvn -f implementations/java-raw-http/pom.xml exec:java -Dexec.mainClass=dev.eviltester.fromhell.ApiFromHellRawHttpMain
```

In PowerShell:

```powershell
$env:FROMHELL_CATALOG = "catalog/fromhell-catalog.json"
mvn -f implementations\java-raw-http\pom.xml exec:java -Dexec.mainClass=dev.eviltester.fromhell.ApiFromHellRawHttpMain
```

Build and run with Docker:

```bash
docker build -t api-from-hell-java-raw-http -f implementations/java-raw-http/Dockerfile .
docker run --rm -p 3001:3001 api-from-hell-java-raw-http
```

Or run the Compose service:

```bash
docker compose up --build fromhell-java-raw-http
```

Compose URL:

```text
http://localhost:3005/fromhell
```

### Mockoon

`implementations/mockoon/fromhell.generated.json` is generated from the shared catalog. Install the
Mockoon CLI and run the generated environment:

```bash
npm install --global @mockoon/cli
mockoon-cli start --data implementations/mockoon/fromhell.generated.json --port 3001
```

Run with the official Mockoon CLI Docker image:

```bash
docker pull mockoon/cli:latest
docker run --rm -p 3001:3001 \
  --mount type=bind,source="$(pwd)/implementations/mockoon/fromhell.generated.json",target=/home/mockoon/data/fromhell.generated.json,readonly \
  mockoon/cli:latest \
  --data /home/mockoon/data/fromhell.generated.json \
  --port 3001
```

PowerShell users can use this Docker mount syntax:

```powershell
docker pull mockoon/cli:latest
docker run --rm -p 3001:3001 `
  --mount "type=bind,source=$PWD\implementations\mockoon\fromhell.generated.json,target=/home/mockoon/data/fromhell.generated.json,readonly" `
  mockoon/cli:latest `
  --data /home/mockoon/data/fromhell.generated.json `
  --port 3001
```

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

## Run Conformance Tests Against A Running Server

Start any API From Hell implementation first, then run the HTTP conformance suite against the
server origin. The `--base-url` value is the origin only; the runner adds the `/fromhell` prefix by
default.

Run the tests against a server on the default local port:

```bash
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3001
```

Run against a Compose service by using its mapped host port:

```bash
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3001
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3002
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3003
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3004
python tooling/conformance-tests/run_conformance.py --base-url http://localhost:3005
```

Run the full Compose matrix from PowerShell:

```powershell
$targets = @(
  @{Name='node-native'; Url='http://localhost:3001'},
  @{Name='node-express'; Url='http://localhost:3002'},
  @{Name='python-native'; Url='http://localhost:3003'},
  @{Name='python-flask'; Url='http://localhost:3004'},
  @{Name='java-raw-http'; Url='http://localhost:3005'}
)

foreach ($target in $targets) {
  Write-Host "==== $($target.Name) $($target.Url)"
  python tooling\conformance-tests\run_conformance.py --base-url $target.Url
}
```

Run against a server mounted somewhere other than `/fromhell`:

```bash
python tooling/conformance-tests/run_conformance.py \
  --base-url http://localhost:3001 \
  --prefix /custom-prefix
```

Run against a server using a different catalog file:

```bash
python tooling/conformance-tests/run_conformance.py \
  --base-url http://localhost:3001 \
  --catalog catalog/fromhell-catalog.json
```

Run optional wrong-method routing checks:

```bash
python tooling/conformance-tests/run_conformance.py \
  --base-url http://localhost:3001 \
  --check-405-routing
```

The `--check-405-routing` flag sends one unsupported method to each known path and expects
`405 Method Not Allowed` plus an `Allow` header. This is off by default so normal conformance runs
and proxy captures focus on the catalogued endpoint requests.

Conformance is strict by default:

- response status codes must match the catalog
- catalogued response headers must be present with exact values
- endpoints that omit `Content-Type` must really omit `Content-Type`
- response body bytes must match the catalog, including awkward cases such as BOM-prefixed bodies
  and bodies on `204`, `205`, and `304`
- extra wrong-method `405` routing probes are not sent unless `--check-405-routing` is supplied

Use `--allow-normalized-no-body-statuses` only when documenting a server, proxy, or platform that
intentionally suppresses bodies for statuses such as `204`, `205`, and `304`:

```bash
python tooling/conformance-tests/run_conformance.py \
  --base-url http://localhost:3001 \
  --allow-normalized-no-body-statuses
```

Run the checks through an HTTP proxy such as Burp Suite:

```bash
python tooling/conformance-tests/run_conformance.py \
  --base-url http://localhost:3001 \
  --proxy http://127.0.0.1:8080
```

## Docker Compose

Run one implementation with Compose:

```bash
docker compose up fromhell-node-native
```

Run all Compose services:

```bash
docker compose up --build
```

Each Compose service uses a different host port so several can run side by side:

- `fromhell-node-native` - `http://localhost:3001/fromhell`
- `fromhell-node-express` - `http://localhost:3002/fromhell`
- `fromhell-python-native` - `http://localhost:3003/fromhell`
- `fromhell-python-flask` - `http://localhost:3004/fromhell`
- `fromhell-java-raw-http` - `http://localhost:3005/fromhell`
