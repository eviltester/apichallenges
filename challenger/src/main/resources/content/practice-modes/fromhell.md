---
title: API From Hell
seo_title: API From Hell Practice Mode for REST Clients | API Challenges
description: A deliberately awkward hard-coded API for checking how REST clients show malformed and misleading responses.
lastmod: 2026-07-24
seo_description: Try API From Hell, a hard-coded API practice mode exposing malformed data, problematic JSON/XML, mismatched content types, and missing metadata.
meta_robots: noindex,follow
---

# API From Hell

API From Hell is a small hard-coded API designed to stress REST clients.

It is important to know if your REST Client helps you spot errors, and especially if it covers errors up - e.g. does it show you well formed XML when the response was actually Malformed XML? That's what the API From Hell was designed to help you learn. 

The API deliberately returns a mix of good responses, malformed responses, valid but problematic JSON/XML, responses where the `Content-Type` header does not match the body, and status codes that conflict with response headers or bodies. This helps you evaluate whether a REST client makes important response details visible.

Known `/fromhell` paths respond with `405 Method Not Allowed` and an `Allow` header when you call them with the wrong HTTP method, except for the deliberately broken 405 example below.

- [Open API From Hell Swagger UI](/fromhell/docs/swagger-ui)
- [Download the API From Hell OpenAPI file](/fromhell/docs/swagger)

All endpoints are under:

```text
{{<ORIGIN_URL>}}/fromhell
```

## Status

`GET /fromhell/status`

This returns markdown documentation for the API with `Content-Type: text/markdown`.

Nothing is structurally wrong with this response. A REST client should show the response as text and make the content type easy to see.

{{<sim-live-request method="GET" path="/fromhell/status" details="true" summary="Try it now">}}

## Version

`GET /fromhell/version`

This returns a small JSON-looking body:

```json
{"version":"6"}
```

The awkward part is that the endpoint does not explicitly set a `Content-Type` header. A REST client should still show the response body and make it clear whether it inferred, defaulted, or omitted the response type.

{{<sim-live-request method="GET" path="/fromhell/version" details="true" summary="Try it now">}}

## Good JSON

`GET /fromhell/good/json`

This is the JSON control sample. It returns valid JSON with `Content-Type: application/json`.

A REST client should format it cleanly and show it as a valid JSON response.

{{<sim-live-request method="GET" path="/fromhell/good/json" details="true" summary="Try it now">}}

## Good XML

`GET /fromhell/good/xml`

This is the XML control sample. It returns valid XML with `Content-Type: application/xml`.

A REST client should format it cleanly and show it as a valid XML response.

{{<sim-live-request method="GET" path="/fromhell/good/xml" details="true" summary="Try it now">}}

## Additional Content Formats

These are control samples for common response formats that are not JSON or XML.
They help show whether a REST client can preview, format, download, or at least
clearly label different media types.

### Good Text

`GET /fromhell/good/text`

This returns plain text with `Content-Type: text/plain`.

A REST client should show the body as readable text and make the media type
obvious.

{{<sim-live-request method="GET" path="/fromhell/good/text" details="true" summary="Try it now">}}

### Good HTML

`GET /fromhell/good/html`

This returns simple HTML with `Content-Type: text/html`.

A REST client should make it clear whether it is rendering the HTML preview or
showing the raw source.

{{<sim-live-request method="GET" path="/fromhell/good/html" details="true" summary="Try it now">}}

### Good CSV

`GET /fromhell/good/csv`

This returns small comma-separated data with `Content-Type: text/csv`.

A REST client should show the raw CSV and, if it has table support, make the rows
and columns easy to inspect.

{{<sim-live-request method="GET" path="/fromhell/good/csv" details="true" summary="Try it now">}}

### Good YAML

`GET /fromhell/good/yaml`

This returns a small YAML document with `Content-Type: application/yaml`.

A REST client should preserve indentation and make the YAML media type visible.

{{<sim-live-request method="GET" path="/fromhell/good/yaml" details="true" summary="Try it now">}}

### Good Form URL Encoded

`GET /fromhell/good/form-urlencoded`

This returns URL-encoded key/value data with
`Content-Type: application/x-www-form-urlencoded`.

A REST client should show the raw encoded body and ideally decode the key/value
pairs clearly.

{{<sim-live-request method="GET" path="/fromhell/good/form-urlencoded" details="true" summary="Try it now">}}

### Good Octet Stream

`GET /fromhell/good/octet-stream`

This returns a small binary-looking payload with
`Content-Type: application/octet-stream`.

A REST client should make it clear whether it is showing bytes, interpreting the
payload as text, or offering it as a download.

{{<sim-live-request method="GET" path="/fromhell/good/octet-stream" details="true" summary="Try it now">}}

## Missing Content-Type

These responses contain recognizable JSON, XML, or HTML bodies, but the server
does not declare a `Content-Type`. That means any formatting or preview is based
on client inference rather than response metadata.

### JSON Body Without Content-Type

`GET /fromhell/missing-content-type/json`

This returns a JSON body without declaring `Content-Type: application/json`.

A REST client should make the missing header visible and avoid pretending the
server declared the format.

{{<sim-live-request method="GET" path="/fromhell/missing-content-type/json" details="true" summary="Try it now">}}

### XML Body Without Content-Type

`GET /fromhell/missing-content-type/xml`

This returns an XML body without declaring `Content-Type: application/xml`.

A REST client should show that the content type is absent even if it can infer
XML from the body.

{{<sim-live-request method="GET" path="/fromhell/missing-content-type/xml" details="true" summary="Try it now">}}

### HTML Body Without Content-Type

`GET /fromhell/missing-content-type/html`

This returns an HTML body without declaring `Content-Type: text/html`.

A REST client should make rendered-vs-raw behavior obvious and show that no
media type was supplied by the server.

{{<sim-live-request method="GET" path="/fromhell/missing-content-type/html" details="true" summary="Try it now">}}

## Malformed JSON

Malformed JSON examples return `200 OK` with `Content-Type: application/json`,
but the body is not valid JSON. These are designed to show whether a REST client
trusts the header/status too much, or whether it reports parser failures clearly
while still preserving the raw response.

### Missing Collection Terminator

`GET /fromhell/malformed/json`

This returns `Content-Type: application/json`, but the JSON array is missing its closing `]`.

A REST client should show that parsing or formatting failed, but it should still allow you to view the raw body.

{{<sim-live-request method="GET" path="/fromhell/malformed/json" details="true" summary="Try it now">}}

### Trailing Comma In Array

`GET /fromhell/malformed/json/trailing-comma-array`

This returns an array with a comma after the final item:

```json
[{"id":1,"name":"first"},]
```

JSON does not allow trailing commas. A REST client should show the parse failure
near the closing `]`.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/trailing-comma-array" details="true" summary="Try it now">}}

### Trailing Comma In Object

`GET /fromhell/malformed/json/trailing-comma-object`

This returns an object with a comma after the final property.

JSON does not allow trailing commas in objects either. A REST client should not
silently accept this JavaScript-style convenience as valid JSON.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/trailing-comma-object" details="true" summary="Try it now">}}

### Unquoted Object Key

`GET /fromhell/malformed/json/unquoted-key`

This returns an object where `id` is not wrapped in double quotes.

JSON object keys must be quoted strings. A REST client should report this rather
than treating the body as a JavaScript object literal.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/unquoted-key" details="true" summary="Try it now">}}

### Single Quoted String

`GET /fromhell/malformed/json/single-quoted-string`

This returns JSON-looking data where strings are wrapped in single quotes.

JSON strings must use double quotes. A REST client should flag the quote style as
invalid JSON and still let you inspect the raw body.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/single-quoted-string" details="true" summary="Try it now">}}

### Bad Escape Sequence

`GET /fromhell/malformed/json/bad-escape`

This returns a string with an unsupported escape sequence such as `\q`.

Only specific escape sequences are valid in JSON strings. A REST client should
show which escape caused parsing to fail.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/bad-escape" details="true" summary="Try it now">}}

### Truncated String

`GET /fromhell/malformed/json/truncated-string`

This returns an object where a string value starts but never closes.

This simulates a cut-off response body. A REST client should make the incomplete
string obvious instead of showing an empty or misleading formatted view.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/truncated-string" details="true" summary="Try it now">}}

### Extra Data After Document

`GET /fromhell/malformed/json/extra-data-after-document`

This returns one complete object followed by another value.

A JSON response body should contain one complete JSON document. A REST client
should report that unexpected data appears after the first parsed value.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/extra-data-after-document" details="true" summary="Try it now">}}

### Missing Colon

`GET /fromhell/malformed/json/missing-colon`

This returns an object property without the colon between the name and value.

A REST client should identify the structural syntax error and preserve the raw
body for diagnosis.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/missing-colon" details="true" summary="Try it now">}}

### Leading Zero Number

`GET /fromhell/malformed/json/leading-zero-number`

This returns a number such as `01`.

JSON numbers cannot use leading zeroes. A REST client should reject the body as
non-standard JSON rather than silently normalizing the value.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/leading-zero-number" details="true" summary="Try it now">}}

### NaN Value

`GET /fromhell/malformed/json/nan-value`

This returns `NaN` as a value.

`NaN` exists in JavaScript, but it is not a valid JSON literal. A REST client
should not accept it as ordinary JSON just because the response says
`application/json`.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/nan-value" details="true" summary="Try it now">}}

### Unclosed Object

`GET /fromhell/malformed/json/unclosed-object`

This returns an object that starts correctly but never closes with `}`.

A REST client should report the unexpected end of JSON input and still expose the
partial body.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/unclosed-object" details="true" summary="Try it now">}}

### Raw Control Character

`GET /fromhell/malformed/json/control-character`

This returns a string containing a raw control character.

JSON strings cannot contain unescaped control characters. A REST client should
show that the payload is invalid even if the control character is not visible on
screen.

{{<sim-live-request method="GET" path="/fromhell/malformed/json/control-character" details="true" summary="Try it now">}}

## Problematic JSON

Problematic JSON examples are not all malformed. Some are valid JSON that can
still cause trouble because clients round numbers, hide duplicate keys, blur
`null` and missing fields, or assume every JSON response is an object or array.

### Duplicate Keys

`GET /fromhell/problematic/json/duplicate-keys`

This returns an object with the same key more than once.

Duplicate keys are valid syntax for many parsers, but the meaning is ambiguous.
Some clients keep the first value, others keep the last, and some show both in a
viewer while generated code only sees one.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/duplicate-keys" details="true" summary="Try it now">}}

### Large Integer

`GET /fromhell/problematic/json/large-integer`

This returns an integer larger than JavaScript can safely represent exactly.

The value may be rounded by clients that store JSON numbers as floating point
values. A REST client should make the raw value visible so precision loss can be
spotted.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/large-integer" details="true" summary="Try it now">}}

### High Precision Decimal

`GET /fromhell/problematic/json/high-precision-decimal`

This returns a decimal with many significant digits.

Some viewers format or round the value. A useful REST client should let you
compare the raw response with the parsed/rendered value.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/high-precision-decimal" details="true" summary="Try it now">}}

### Exponent Number

`GET /fromhell/problematic/json/exponent-number`

This returns valid JSON numbers using exponent notation.

Exponent notation is legal, but clients may display it inconsistently or generate
awkward examples from it. The raw number text should remain easy to inspect.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/exponent-number" details="true" summary="Try it now">}}

### Null Vs Missing Field

`GET /fromhell/problematic/json/null-vs-missing`

This returns one item with a field set to `null` and another item where the field
is absent.

Those are different states: one explicitly says "no value", the other does not
send the field at all. A REST client should help you see the difference.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/null-vs-missing" details="true" summary="Try it now">}}

### Escaped Unicode

`GET /fromhell/problematic/json/escaped-unicode`

This returns escaped Unicode including a zero-width character and a visually
similar character from another alphabet.

The rendered value can look harmless while the raw value contains hidden or
confusable characters. A REST client should let you inspect the escape sequences.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/escaped-unicode" details="true" summary="Try it now">}}

### Empty Object

`GET /fromhell/problematic/json/empty-object`

This returns `{}`.

An empty object is valid JSON, but it can be a suspicious response when a schema,
documentation, or workflow implies useful fields should be present.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/empty-object" details="true" summary="Try it now">}}

### Top-Level String

`GET /fromhell/problematic/json/top-level-string`

This returns a JSON string as the entire document.

Top-level scalar JSON is valid, but many clients and generated models assume the
response will be an object or array.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/top-level-string" details="true" summary="Try it now">}}

### Top-Level Number

`GET /fromhell/problematic/json/top-level-number`

This returns a JSON number as the entire document.

A REST client should make it obvious that the whole response is a scalar value,
not a missing body or a failed parse.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/top-level-number" details="true" summary="Try it now">}}

### Top-Level Boolean

`GET /fromhell/problematic/json/top-level-boolean`

This returns a JSON boolean as the entire document.

The response is valid JSON, but it can reveal clients that expect every JSON
response to have fields or collection items.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/top-level-boolean" details="true" summary="Try it now">}}

### Top-Level Null

`GET /fromhell/problematic/json/top-level-null`

This returns the JSON literal `null` as the entire document.

`null` is different from an empty body. A REST client should show the literal
value rather than treating the response as if no content was sent.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/top-level-null" details="true" summary="Try it now">}}

### Empty Body With JSON Content-Type

`GET /fromhell/problematic/json/empty-body`

This returns `Content-Type: application/json` but no body.

An empty response body is not a JSON document. A REST client should make the
empty body obvious and avoid pretending it parsed a value.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/empty-body" details="true" summary="Try it now">}}

### UTF-8 BOM Prefix

`GET /fromhell/problematic/json/bom-prefix`

This returns a JSON object prefixed with a UTF-8 byte order mark.

Some parsers tolerate the prefix and others reject it. A REST client should keep
enough raw response detail to diagnose the leading byte order mark.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/bom-prefix" details="true" summary="Try it now">}}

### NDJSON Stream

`GET /fromhell/problematic/json/ndjson`

This returns newline-delimited JSON using `Content-Type: application/x-ndjson`.

NDJSON is useful for streams, but it is not one JSON document. A REST client
should not try to parse it as a single object or array without explaining the
format.

{{<sim-live-request method="GET" path="/fromhell/problematic/json/ndjson" details="true" summary="Try it now">}}

## Malformed XML

Malformed XML examples return `200 OK` with `Content-Type: application/xml`, but
the body is not a well-formed XML document. These are useful for checking whether
a client hides XML parser errors behind a formatted view.

### Missing Root Terminator

`GET /fromhell/malformed/xml`

This returns `Content-Type: application/xml`, but the XML document does not close the root element.

A REST client should show that parsing or formatting failed, but it should still allow you to view the raw body.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml" details="true" summary="Try it now">}}

### Mismatched Tag

`GET /fromhell/malformed/xml/mismatched-tag`

This opens an `<item>` element but closes it as `</thing>`.

XML start and end tags must match exactly. A REST client should show the
mismatched tag location and preserve the raw response.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/mismatched-tag" details="true" summary="Try it now">}}

### Unescaped Ampersand

`GET /fromhell/malformed/xml/unescaped-ampersand`

This returns text containing `Tom & Jerry`.

In XML, an ampersand must be escaped as `&amp;` unless it starts a valid entity.
A REST client should report the entity parsing problem.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/unescaped-ampersand" details="true" summary="Try it now">}}

### Duplicate Attribute

`GET /fromhell/malformed/xml/duplicate-attribute`

This returns an element with two `id` attributes.

XML elements cannot have duplicate attribute names. A REST client should report
the duplicate attribute rather than choosing one value silently.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/duplicate-attribute" details="true" summary="Try it now">}}

### Undefined Entity

`GET /fromhell/malformed/xml/undefined-entity`

This references `&unknown;` without declaring it.

Only predefined or declared entities are valid. A REST client should expose the
undefined entity error clearly.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/undefined-entity" details="true" summary="Try it now">}}

### Multiple Root Elements

`GET /fromhell/malformed/xml/multiple-root-elements`

This returns two top-level elements.

A well-formed XML document must have exactly one root element. A REST client
should report that extra document content appears after the first root closes.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/multiple-root-elements" details="true" summary="Try it now">}}

### Missing Attribute Quote

`GET /fromhell/malformed/xml/missing-attribute-quote`

This returns an attribute value without quotes.

XML attribute values must be quoted. A REST client should report the attribute
syntax error.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/missing-attribute-quote" details="true" summary="Try it now">}}

### Unclosed CDATA

`GET /fromhell/malformed/xml/unclosed-cdata`

This starts a CDATA section but never closes it with `]]>`.

A REST client should report the unterminated CDATA section and keep the raw body
available.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/unclosed-cdata" details="true" summary="Try it now">}}

### Invalid Character

`GET /fromhell/malformed/xml/invalid-character`

This returns XML containing a raw control character in text content.

Some invalid characters may not be visible. A REST client should still report the
well-formedness error and allow raw response inspection.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/invalid-character" details="true" summary="Try it now">}}

### Truncated Document

`GET /fromhell/malformed/xml/truncated-document`

This starts nested XML but ends before closing the open elements.

A REST client should report the unexpected end of document instead of showing a
partial formatted tree as if it were complete.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/truncated-document" details="true" summary="Try it now">}}

### Bad Processing Instruction

`GET /fromhell/malformed/xml/bad-processing-instruction`

This starts a processing instruction but does not close it with `?>`.

A REST client should show the processing-instruction syntax error and preserve
the original response text.

{{<sim-live-request method="GET" path="/fromhell/malformed/xml/bad-processing-instruction" details="true" summary="Try it now">}}

## Problematic XML

Problematic XML examples are mostly well-formed. They are useful for checking
whether a REST client preserves XML-specific detail instead of flattening the
document into something that looks simpler but loses meaning.

### Attributes Vs Elements

`GET /fromhell/problematic/xml/attributes-vs-elements`

This returns similar values as both attributes and child elements.

Attributes and elements are not the same thing, and in this response they even
disagree. A REST client should make it obvious where each value came from rather
than silently merging them.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/attributes-vs-elements" details="true" summary="Try it now">}}

### Empty Vs Missing Vs Nil

`GET /fromhell/problematic/xml/empty-vs-missing-vs-nil`

This shows three different states: an empty element, a missing element, and an
element marked with `xsi:nil="true"`.

Those states can mean different things to an API. A REST client should help you
see the difference instead of rendering all three as blank values.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/empty-vs-missing-vs-nil" details="true" summary="Try it now">}}

### Whitespace Significant Text

`GET /fromhell/problematic/xml/whitespace-text`

This returns values with leading, trailing, and multiline whitespace.

Whitespace in XML text can be meaningful. A REST client should let you inspect
the raw body so formatting or object conversion does not hide trimmed values.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/whitespace-text" details="true" summary="Try it now">}}

### CDATA Content

`GET /fromhell/problematic/xml/cdata-content`

This returns JSON-looking and HTML-looking text inside a CDATA section.

CDATA is text, not child markup. A REST client should preserve the content and
avoid pretending embedded markup is part of the XML tree.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/cdata-content" details="true" summary="Try it now">}}

### Mixed Content

`GET /fromhell/problematic/xml/mixed-content`

This returns text and an element inside the same parent element.

Mixed content is valid XML but awkward for clients that convert XML to simple
objects. A REST client should preserve the order of text and child elements.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/mixed-content" details="true" summary="Try it now">}}

### Processing Instruction

`GET /fromhell/problematic/xml/processing-instruction`

This returns a valid processing instruction before the root element.

Processing instructions can carry important hints, but many viewers hide them.
A REST client should make them visible in the raw or structured view.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/processing-instruction" details="true" summary="Try it now">}}

### Comments

`GET /fromhell/problematic/xml/comments`

This returns comments between normal XML elements.

Comments are part of the document text even if they are often stripped during
conversion. A REST client should make it possible to see whether comments were
present.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/comments" details="true" summary="Try it now">}}

### Encoding Declaration Mismatch

`GET /fromhell/problematic/xml/encoding-mismatch`

This returns `Content-Type: application/xml; charset=UTF-8`, but the XML
declaration says `encoding="ISO-8859-1"`.

When the transport header and XML declaration disagree, clients may choose
different encodings. A REST client should expose both values so the ambiguity is
visible.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/encoding-mismatch" details="true" summary="Try it now">}}

### DOCTYPE And DTD

`GET /fromhell/problematic/xml/doctype-dtd`

This returns XML with a safe inline DTD.

DOCTYPE and DTD declarations are valid XML, but many clients block, ignore, or
hide them for security reasons. This example has no external entity or file
access; it is only there to check how the client displays the declaration.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/doctype-dtd" details="true" summary="Try it now">}}

### Empty Body With XML Content-Type

`GET /fromhell/problematic/xml/empty-body`

This returns `Content-Type: application/xml` but no body.

An empty response body is not an XML document. A REST client should show that the
body is empty rather than pretending it parsed XML.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/empty-body" details="true" summary="Try it now">}}

### UTF-8 BOM Prefix

`GET /fromhell/problematic/xml/bom-prefix`

This returns XML prefixed with a UTF-8 byte order mark.

Some parsers tolerate the prefix and others expose it as a leading character. A
REST client should preserve enough raw detail to diagnose this if parsing or
display looks odd.

{{<sim-live-request method="GET" path="/fromhell/problematic/xml/bom-prefix" details="true" summary="Try it now">}}

## Header Says JSON, Body Is XML

`GET /fromhell/mismatch/content-type/json-xml`

This returns XML, but the response says `Content-Type: application/json`.

A REST client should make the mismatch visible. Ideally it should not hide the body just because the header is misleading.

{{<sim-live-request method="GET" path="/fromhell/mismatch/content-type/json-xml" details="true" summary="Try it now">}}

## Header Says XML, Body Is JSON

`GET /fromhell/mismatch/content-type/xml-json`

This returns JSON, but the response says `Content-Type: application/xml`.

A REST client should make the mismatch visible. Ideally it should not hide the body just because the header is misleading.

{{<sim-live-request method="GET" path="/fromhell/mismatch/content-type/xml-json" details="true" summary="Try it now">}}

## Status Code Semantic Mismatches

These endpoints use status codes that do not line up cleanly with the response headers or body. A REST client should help you inspect the raw status, headers, and body without assuming the response is sensible.

### Created Without Location

`POST /fromhell/status-code/201-no-location`

This returns `201 Created` with a JSON body but no `Location` header.

A `201 Created` response normally tells the client that a new resource now exists. The
`Location` header should identify where that resource can be fetched, for example:

```text
Location: /fromhell/things/123
```

Without that header the client can see that something was created, but it has to
guess where the new resource lives. A REST client should make the missing
`Location` header easy to spot instead of treating the response as a completely
normal create result.

{{<sim-live-request method="POST" path="/fromhell/status-code/201-no-location" details="true" summary="Try it now">}}

### Permanent Redirect Without Location

`GET /fromhell/status-code/301-no-location`

This returns `301 Moved Permanently` with no `Location` header, so there is nowhere for the client to redirect.

Redirect responses need a target. For a `301`, that target is usually the new
permanent URL for the resource:

```text
Location: https://example.com/new-place
```

Without `Location`, the status says "go somewhere else" but does not say where.
A REST client should show the status and missing header clearly, and should not
pretend that a redirect was followed.

{{<sim-live-request method="GET" path="/fromhell/status-code/301-no-location" details="true" summary="Try it now">}}

### Temporary Redirect Without Location

`GET /fromhell/status-code/302-no-location`

This returns `302 Found` with no `Location` header.

A `302` is a temporary redirect. It has the same practical requirement as the
`301` example: the response should include a `Location` header so the client knows
the temporary URL to request next. Without it, there is no useful redirect target.

Some clients automatically follow redirects, which can hide the original `302`
response. A good REST client should let you disable automatic redirects or inspect
the original response so you can see that the `Location` header is missing.

{{<sim-live-request method="GET" path="/fromhell/status-code/302-no-location" details="true" summary="Try it now">}}

### Method-Preserving Redirect Without Location

`GET /fromhell/status-code/307-no-location`

This returns `307 Temporary Redirect` with no `Location` header.

`307 Temporary Redirect` is stricter than `302`: if a client follows it, the next
request should use the same HTTP method and body as the original request. That
only works if the server provides a `Location` header.

The issue here is not only that the redirect target is missing. It is also a good
way to check whether your client explains method-preserving redirects, rather than
silently turning them into a vague redirect error.

{{<sim-live-request method="GET" path="/fromhell/status-code/307-no-location" details="true" summary="Try it now">}}

### No Content With A Body

`DELETE /fromhell/status-code/204-with-body`

This returns `204 No Content` while the endpoint attempts to send a JSON body.

`204` means the request succeeded and there is deliberately no response content.
A well-formed `204` response should not include a response body. In many APIs it
also avoids a response `Content-Type`, because there is no content to describe.

Some clients will refuse to show the body because `204` means "No Content".
For example, `curl -v` reports the response body as excess data rather than
printing it. Check the headers and raw wire response to see whether your client
reveals, hides, or rejects the contradiction.

{{<sim-live-request method="DELETE" path="/fromhell/status-code/204-with-body" details="true" summary="Try it now">}}

### Reset Content With A Body

`POST /fromhell/status-code/205-with-body`

This returns `205 Reset Content` while the endpoint attempts to send a JSON body.

`205 Reset Content` tells the client that the operation succeeded and that the
client should reset the document or form view that caused the request. Like `204`,
it should not include a response body.

This is useful for checking whether your client notices the contradiction between
the status code and the body. Some clients may still display the body, while
others may suppress it or treat it as protocol noise.

{{<sim-live-request method="POST" path="/fromhell/status-code/205-with-body" details="true" summary="Try it now">}}

### Not Modified With A Body

`GET /fromhell/status-code/304-with-body`

This returns `304 Not Modified` while the endpoint attempts to send a JSON body.

`304` is used during cache validation. It tells the client to keep using the
cached representation it already has. A normal `304` response can include cache
metadata such as `ETag`, `Date`, or `Cache-Control`, but it should not include a
message body.

As with the `204` example, some clients will refuse to show this body because
`304` is a cache validation response that should not include content. `curl -v`
reports the bytes as excess data rather than printing them.

{{<sim-live-request method="GET" path="/fromhell/status-code/304-with-body" details="true" summary="Try it now">}}

### Partial Content Without Content-Range

`GET /fromhell/status-code/206-no-content-range`

This returns `206 Partial Content` without a `Content-Range` header.

`206 Partial Content` means the server is sending only part of a larger resource,
usually because the client sent a `Range` request header. The response should say
which slice is being returned and how large the full resource is, for example:

```text
Content-Range: bytes 0-99/200
```

Without `Content-Range`, the client cannot reliably tell which part of the
resource it received, whether the range matches what it asked for, or how to
resume the next request. A REST client should make the missing range metadata
obvious.

{{<sim-live-request method="GET" path="/fromhell/status-code/206-no-content-range" details="true" summary="Try it now">}}

### Unauthorized Without WWW-Authenticate

`GET /fromhell/status-code/401-no-www-authenticate`

This returns `401 Unauthorized` without a `WWW-Authenticate` header.

`401` means the request needs authentication, and the response should include a
`WWW-Authenticate` header explaining the authentication scheme, for example:

```text
WWW-Authenticate: Basic realm="API From Hell"
```

or:

```text
WWW-Authenticate: Bearer
```

Without this header the client knows authentication failed, but it does not know
what kind of credentials the server expects. REST clients often use this header to
offer auth helpers, prompts, or token configuration.

{{<sim-live-request method="GET" path="/fromhell/status-code/401-no-www-authenticate" details="true" summary="Try it now">}}

### Method Not Allowed Without Allow

`POST /fromhell/status-code/405-no-allow`

This returns `405 Method Not Allowed` without an `Allow` header.

`405` means the path exists, but the HTTP method is not supported for that path.
The response should include an `Allow` header listing the methods that are
supported, for example:

```text
Allow: GET, HEAD, OPTIONS
```

Without `Allow`, the client can tell that the method was wrong, but it cannot tell
which method should be used instead. This is especially important for generated
clients, API explorers, and test tools that try to guide the user to a valid
request.

{{<sim-live-request method="POST" path="/fromhell/status-code/405-no-allow" details="true" summary="Try it now">}}

### Range Not Satisfiable Without Content-Range

`GET /fromhell/status-code/416-no-content-range`

This returns `416 Range Not Satisfiable` without a `Content-Range` header.

`416` means the client asked for a byte range that cannot be served. The response
should normally include `Content-Range` with an unsatisfied range marker and the
current size of the resource:

```text
Content-Range: bytes */200
```

That header matters because it tells the client why the requested range failed
and what range might be valid next time. Without it, download managers, resumable
upload/download tools, and API clients cannot easily recover or explain the
failure.

{{<sim-live-request method="GET" path="/fromhell/status-code/416-no-content-range" details="true" summary="Try it now">}}

### OK With An Error Body

`GET /fromhell/status-code/200-error-body`

This returns `200 OK` with a body that looks like an error.

The status code says the request succeeded, but the body contains fields such as
`error` and `code`. This is not always technically invalid, but it is a design
trap because client code may trust the status code while humans notice the body
looks like a failure.

A REST client should make it easy to inspect both status and body together. Test
automation should usually assert the status code and the important body fields
rather than trusting one of them alone.

{{<sim-live-request method="GET" path="/fromhell/status-code/200-error-body" details="true" summary="Try it now">}}

### Server Error With A Success Body

`GET /fromhell/status-code/500-success-body`

This returns `500 Internal Server Error` with a body that claims success.

This is the opposite of the previous example. The status says the server failed,
but the JSON body says the operation succeeded.

Client code should treat the status code as the primary transport result and avoid
blindly trusting a success-shaped body. A REST client should make the mismatch
plain, especially if it has convenience views that label responses as success or
failure.

{{<sim-live-request method="GET" path="/fromhell/status-code/500-success-body" details="true" summary="Try it now">}}
