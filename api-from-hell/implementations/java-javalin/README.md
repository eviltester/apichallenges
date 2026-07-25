# Java Javalin

Standalone API From Hell implementation using Javalin.

Run from this folder:

```bash
mvn exec:java -Dexec.mainClass=dev.eviltester.fromhell.ApiFromHellJavalinMain
```

Configuration:

- `PORT`, default `3001`
- `FROMHELL_PREFIX`, default `/fromhell`
- `FROMHELL_CATALOG`, default `../../catalog/fromhell-catalog.json`

Javalin/Jetty may normalize some deliberately awkward responses, especially no-body status codes.
