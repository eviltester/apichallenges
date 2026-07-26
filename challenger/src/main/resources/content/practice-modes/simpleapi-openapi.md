---
title: Simple API OpenAPI JSON Files
seo_title: Simple API OpenAPI JSON Downloads | API Practice Mode
description: Download the OpenAPI JSON files for the Simple API.
lastmod: 2026-02-18
seo_description: Use Simple API OpenAPI JSON to practice safely, understand request-response behavior, and build confidence with guided exercises before advanced.
showads: true
---

# Simple API OpenAPI Files

Download an OpenAPI JSON file to use in your REST Client.

## File Download Links

OpenAPI JSON files are available in the default format and in specific OpenAPI versions. The default `openapi.json` endpoint currently returns OpenAPI v 3.1.

- OpenAPI v 3.0 JSON [standard validation](/simpleapi/docs/openapi-3.0.json) [download](/simpleapi/docs/openapi-3.0.json?download) - [less validation](/simpleapi/docs/openapi-3.0.json?permissive) [download](/simpleapi/docs/openapi-3.0.json?permissive&download)
- OpenAPI v 3.1 JSON [standard validation](/simpleapi/docs/openapi-3.1.json) [download](/simpleapi/docs/openapi-3.1.json?download) - [less validation](/simpleapi/docs/openapi-3.1.json?permissive) [download](/simpleapi/docs/openapi-3.1.json?permissive&download)
- OpenAPI v 3.2 JSON [standard validation](/simpleapi/docs/openapi-3.2.json) [download](/simpleapi/docs/openapi-3.2.json?download) - [less validation](/simpleapi/docs/openapi-3.2.json?permissive) [download](/simpleapi/docs/openapi-3.2.json?permissive&download)

OpenAPI 3.2 describes `QUERY` as a native method. OpenAPI 3.0 and 3.1 include `QUERY` details using a vendor extension so tools that do not yet understand OpenAPI 3.2 can still load the file.

Example `QUERY` request:

```http
QUERY /simpleapi/items HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Accept: application/json

type=book
```

## Normal OpenAPI File Explained

The Normal OpenAPI File is intended for use as though you were a user.

It only lists endpoints that are valid to use, and has additional validation on the URL Parameters that you can enter.

When this type of file is loaded into a Swagger UI Generation application it makes it easy to USE the API but makes it harder to TEST the API.

- [OpenAPI v 3.0 JSON](/simpleapi/docs/openapi-3.0.json?download)
- [OpenAPI v 3.1 JSON](/simpleapi/docs/openapi-3.1.json?download)
- [OpenAPI v 3.2 JSON](/simpleapi/docs/openapi-3.2.json?download)

## Permissive OpenAPI File Explained

The Permissive OpenAPI File is intended for testing.

It lists all the end points with more Verbs i.e. even verbs that the API defines as not available.

The parameters are also possible to send as empty and type validation is not performed on the parameter values. This makes it possible to use Swagger UI applications to test more extreme situations.

- [OpenAPI v 3.0 JSON](/simpleapi/docs/openapi-3.0.json?permissive&download)
- [OpenAPI v 3.1 JSON](/simpleapi/docs/openapi-3.1.json?permissive&download)
- [OpenAPI v 3.2 JSON](/simpleapi/docs/openapi-3.2.json?permissive&download)
