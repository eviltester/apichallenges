# Known Platform Differences

Some API From Hell examples deliberately violate common HTTP expectations. Different frameworks,
servers, proxies, and deployment platforms may normalize those responses.

## `204`, `205`, And `304` With Bodies

The catalog includes endpoints that attempt to send a body with status codes that normally imply no
body:

- `DELETE /fromhell/status-code/204-with-body`
- `POST /fromhell/status-code/205-with-body`
- `GET /fromhell/status-code/304-with-body`

Low-level servers may emit the body locally. Frameworks or hosted platforms may suppress it.
Production proxies may also remove it before it reaches the client.

Use a proxy between the API and the API client when you need to inspect the actual bytes that reach
the client.

## Missing `Content-Type`

The catalog includes responses with body content but no explicit `Content-Type`. Some frameworks set
a default type automatically. Implementations should suppress framework defaults where possible and
document when they cannot.

## Generated OpenAPI

OpenAPI describes the intended catalog behavior. It cannot fully express every wire-level defect,
especially missing headers or deliberately contradictory status/body combinations.
