# Java Raw HTTP

Standalone API From Hell implementation using raw Java sockets.

This implementation writes HTTP responses directly so the deliberately awkward catalog responses
are observable on the wire, including bodies on statuses such as `204` and `304` that frameworks
and proxies often normalize away.

Run from this folder:

```bash
mvn exec:java -Dexec.mainClass=dev.eviltester.fromhell.ApiFromHellRawHttpMain
```

Configuration:

- `PORT`, default `3001`
- `FROMHELL_PREFIX`, default `/fromhell`
- `FROMHELL_CATALOG`, default `../../catalog/fromhell-catalog.json`
