# Implementation Notes

API From Hell implementations are intentionally thin.

Each implementation should:

- load `catalog/fromhell-catalog.json`;
- register one route for each `method + path` pair;
- prepend the implementation prefix, normally `/fromhell`;
- emit the catalog status, headers, and body as directly as the framework allows;
- return `405 Method Not Allowed` with `Allow` for known paths called with unsupported methods;
- return `204` for `OPTIONS` on known paths;
- return `404` for unknown paths.

The catalog is the contract. If endpoint behavior needs to change, change the catalog first and then
run conformance checks against every implementation.
