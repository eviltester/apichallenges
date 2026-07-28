# RoboDepot Standalone API

RoboDepot is a standalone Javalin/Thingifier app for a deliberately buggy warehouse robot API.

It is public, requires no login, stores data in memory by default, and uses constrained enum, integer, and boolean fields so testers can practice CRUD and relationship testing without free-text abuse.

## Run Locally

From the repository root:

```sh
mvn -pl robodepot package
java -jar robodepot/target/robodepot.jar
```

Open:

- Home and live map: <http://localhost:4567/>
- API docs: <http://localhost:4567/robodepot/docs>
- Swagger UI: <http://localhost:4567/robodepot/docs/swagger-ui>
- Data Explorer: <http://localhost:4567/robodepot/gui/entities>
- OpenAPI JSON: <http://localhost:4567/robodepot/docs/openapi.json>

## Run Clean Mode

RoboDepot is buggy by default. Disable the deliberate bugs with:

```sh
java -jar robodepot/target/robodepot.jar -robodepotbugs=none
```

## Docker

Build from the repository root:

```sh
docker build -f robodepot/Dockerfile -t robodepot-api .
docker run --rm -p 4567:4567 robodepot-api
```

To run clean mode:

```sh
docker run --rm -p 4567:4567 robodepot-api -port=4567 -robodepotbugs=none
```

## Main Endpoints

The API endpoints are under `/robodepot`:

| Endpoint | Purpose |
|---|---|
| `/robodepot/robotmodels` | Read-only robot model catalog |
| `/robodepot/skus` | Read-only SKU catalog |
| `/robodepot/zones` | CRUD warehouse zones |
| `/robodepot/robots` | CRUD robots |
| `/robodepot/jobs` | CRUD robot jobs |
| `/robodepot/stock` | CRUD stock records |

The live map calls the hidden `POST /robodepot/tick-forward` endpoint before refreshing. It can only advance once every 20 seconds and returns `429` with `Retry-After` when called too quickly.
