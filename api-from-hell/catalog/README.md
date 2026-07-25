# API From Hell Catalog

`fromhell-catalog.json` is the canonical source for API From Hell endpoints.

Every standalone implementation should load this file and translate each endpoint into an HTTP
route. Implementations should not duplicate endpoint definitions in code.

## Endpoint Fields

- `method`: HTTP method to register.
- `path`: path relative to the API prefix. The hosted API Challenges app uses `/fromhell` as the
  prefix, so catalog path `/status` is served as `/fromhell/status`.
- `statusCode`: response status code.
- `label`: import-friendly operation name for OpenAPI and REST clients.
- `documentation`: short description of the endpoint.
- `problem`: what is deliberately unusual or broken about the response.
- `expectation`: what a REST client should make visible.
- `headers`: response headers to emit exactly.
- `body`: response body to emit.

## Wire Behavior

Some responses are intentionally awkward, such as `204` with a response body or a body with no
`Content-Type`. Frameworks, proxies, and hosts may suppress or rewrite those responses. When that
matters, document the variance rather than silently normalising the behavior.

## Validation

Use:

```bash
python ../tooling/catalog-validator/validate_catalog.py fromhell-catalog.json
```

from this directory, or from the repository root:

```bash
python api-from-hell/tooling/catalog-validator/validate_catalog.py \
  api-from-hell/catalog/fromhell-catalog.json
```
