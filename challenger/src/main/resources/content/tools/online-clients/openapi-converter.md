---
title: OpenAPI Converter
seo_title: Convert and Restructure OpenAPI Specifications Online
description: Convert an OpenAPI JSON or YAML file in the browser, expand it for exploratory testing, or move parameters between shared Path Item and operation-level declarations.
lastmod: 2026-09-21
seo_description: Convert OpenAPI files in the browser, expand operations and schemas for testing, and move parameters between shared Path Item and operation-level declarations.
schema_type: WebPage
og_type: website
showads: true
---

# OpenAPI Converter

Convert an OpenAPI 3 JSON or YAML file into a more permissive testing specification, change where parameters are declared, or combine both transformations.

<section class="openapi-converter-editor" aria-labelledby="openapi-converter-editor-heading">
  <h2 id="openapi-converter-editor-heading">Use The OpenAPI Converter</h2>
  <p>Load an OpenAPI 3 specification, optionally expand its documented coverage, choose how Path Item parameters are declared, and then copy, download, or open the resulting specification. All conversion happens in your browser.</p>
  <div class="openapi-converter-tool" data-openapi-converter>
    <section class="openapi-converter-workflow-section" aria-labelledby="openapi-converter-load-heading">
      <h3 id="openapi-converter-load-heading">Load OpenAPI Spec</h3>
      <p>Load a JSON or YAML specification from a URL, or choose a local file.</p>
      <form class="online-swagger-controls" data-openapi-url-form>
        <label>
          OpenAPI URL
          <input data-openapi-url type="text" value="/api/docs/openapi.json" placeholder="https://example.com/openapi.json">
        </label>
        <button type="submit">Open URL</button>
      </form>
      <p class="online-client-status" data-openapi-load-status role="status" aria-live="polite" hidden></p>
      <div class="online-swagger-file-row">
        <label>
          Open local JSON or YAML file
          <input data-openapi-file type="file" accept=".json,.yaml,.yml,application/json,text/yaml,application/yaml">
        </label>
      </div>
    </section>
    <div class="openapi-tester-controls" data-openapi-tester-controls>
      <section class="openapi-converter-workflow-section" aria-labelledby="openapi-converter-expansion-heading">
        <h3 id="openapi-converter-expansion-heading">Expand OpenAPI Coverage</h3>
        <p>Choose how broadly the converter should relax validation rules and add operations for exploratory testing. Leave <strong>No expansion</strong> selected to preserve the loaded operation coverage.</p>
        <fieldset class="openapi-converter-section">
          <legend>Expansion settings</legend>
          <div class="openapi-profile-row">
            <label>
              Expansion profile
              <select data-openapi-profile>
                <option value="original">No expansion</option>
                <option value="practical">Practical</option>
                <option value="aggressive">Aggressive</option>
                <option value="custom">Custom</option>
              </select>
            </label>
          </div>
          <details class="openapi-converter-options" data-openapi-custom-options>
            <summary>Custom expansion options</summary>
            <ul class="openapi-option-grid">
              <li><label><input type="checkbox" data-openapi-option="relaxSchemaConstraints"> Remove schema validation constraints</label></li>
              <li><label><input type="checkbox" data-openapi-option="removeRequiredProperties"> Remove required body fields</label></li>
              <li><label><input type="checkbox" data-openapi-option="makeNonPathParametersOptional"> Make non-path parameters optional</label></li>
              <li><label><input type="checkbox" data-openapi-option="allowAdditionalProperties"> Allow extra object properties</label></li>
              <li><label><input type="checkbox" data-openapi-option="makeRequestBodiesOptional"> Make request bodies optional</label></li>
              <li><label><input type="checkbox" data-openapi-option="addMissingOperations"> Add missing HTTP methods</label></li>
              <li><label><input type="checkbox" data-openapi-option="addLooseRequestBodiesToGeneratedOperations"> Add loose JSON bodies to generated methods</label></li>
            </ul>
            <ul class="openapi-verb-grid" aria-label="HTTP methods to add">
              <li><label><input type="checkbox" data-openapi-verb value="get"> GET</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="post"> POST</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="put"> PUT</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="patch"> PATCH</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="delete"> DELETE</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="options"> OPTIONS</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="head"> HEAD</label></li>
              <li><label><input type="checkbox" data-openapi-verb value="trace"> TRACE</label></li>
            </ul>
          </details>
        </fieldset>
      </section>
      <section class="openapi-converter-workflow-section" aria-labelledby="openapi-converter-placement-heading">
        <h3 id="openapi-converter-placement-heading">Operation-Level vs Shared Path Configuration</h3>
        <p>Evaluate whether selected parameters are shared on each Path Item or repeated on operations, then keep or convert their placement.</p>
        <fieldset class="openapi-converter-section" data-openapi-placement>
          <legend>Parameter placement settings</legend>
          <div class="openapi-placement-controls">
            <label>
              Parameter scope
              <select data-openapi-placement-scope data-openapi-placement-control disabled>
                <option value="path">URL path parameters</option>
                <option value="all">All Path Item parameters</option>
              </select>
            </label>
            <div class="openapi-placement-targets" role="radiogroup" aria-label="Output parameter placement">
              <span>Output placement</span>
              <label><input type="radio" name="openapi-converter-placement" value="keep" data-openapi-placement-target data-openapi-placement-control checked disabled> Keep current</label>
              <label><input type="radio" name="openapi-converter-placement" value="path" data-openapi-placement-target data-openapi-placement-control disabled> Shared path-level</label>
              <label><input type="radio" name="openapi-converter-placement" value="operation" data-openapi-placement-target data-openapi-placement-control disabled> Operation-level</label>
            </div>
            <div class="openapi-converter-actions">
              <button type="button" data-openapi-evaluate-placement data-openapi-placement-control disabled>Evaluate current spec</button>
            </div>
          </div>
          <div class="openapi-placement-status" data-openapi-placement-status role="status">
            <p data-openapi-placement-message>Load an OpenAPI specification to evaluate parameter placement.</p>
            <ul data-openapi-placement-comparison hidden>
              <li><strong data-openapi-placement-current></strong></li>
              <li><strong data-openapi-placement-output></strong></li>
            </ul>
          </div>
          <p class="openapi-placement-warnings" data-openapi-placement-warnings hidden></p>
        </fieldset>
      </section>
      <section class="openapi-converter-workflow-section" aria-labelledby="openapi-converter-output-heading">
        <h3 id="openapi-converter-output-heading">Converted OpenAPI Spec</h3>
        <p>Select an expansion or placement transformation to generate JSON that can be copied or downloaded. With no transformation selected, the original specification remains available to the embedded clients.</p>
        <div class="openapi-converter-actions">
          <button type="button" data-openapi-copy-converted disabled>Copy converted JSON</button>
          <button type="button" data-openapi-download-converted disabled>Download converted JSON</button>
        </div>
        <p class="online-client-status" data-openapi-status role="status">Load an OpenAPI JSON or YAML file, then choose a conversion.</p>
        <label class="openapi-converter-output-label">
          <span data-openapi-output-label>Converted OpenAPI JSON</span>
          <textarea class="openapi-converter-output" data-openapi-output readonly spellcheck="false"></textarea>
        </label>
      </section>
    </div>
    <section class="openapi-converter-client-launch" aria-labelledby="openapi-converter-client-heading">
      <h3 id="openapi-converter-client-heading">Open in Embedded Client</h3>
      <p>Open the original OpenAPI file, or the converted file when a transformation is selected, in one of the embedded API clients on this site.</p>
      <div class="openapi-converter-actions openapi-converter-client-actions">
        <button type="button" data-openapi-open-client="swagger" data-openapi-client-path="/tools/online-clients/swagger" data-openapi-open-swagger disabled>Open in Swagger UI</button>
        <button type="button" data-openapi-open-client="openapi-explorer" data-openapi-client-path="/tools/online-clients/openapi-explorer" disabled>Open in OpenAPI Explorer</button>
        <button type="button" data-openapi-open-client="scalar" data-openapi-client-path="/tools/online-clients/scalar" disabled>Open in Scalar</button>
        <button type="button" data-openapi-open-client="stoplight" data-openapi-client-path="/tools/online-clients/stoplight" disabled>Open in Stoplight Elements</button>
        <button type="button" data-openapi-open-client="zudoku" data-openapi-client-path="/tools/online-clients/zudoku" disabled>Open in Zudoku</button>
        <button type="button" data-openapi-open-client="redoc" data-openapi-client-path="/tools/online-clients/redoc" disabled>Open in Redoc</button>
      </div>
    </section>
  </div>
</section>

## Convert OpenAPI Specifications In Your Browser

Strict OpenAPI files are useful documentation, but they can also limit testing. A schema might require a field, restrict a value to an enum, hide an unsupported HTTP method, or stop Swagger UI and REST clients from making the unusual request you want to investigate.

This browser tool converts an OpenAPI 3 file into a less restrictive tester specification, changes its parameter placement, or does both. The conversion happens in your browser. Local files are not uploaded to API Challenges.

The output is useful for exploratory API testing, negative testing, contract comparison, and importing a more flexible API description into tools that can read OpenAPI files.

## Create Practical Or Aggressive OpenAPI Testing Files

The `Practical` profile removes common schema validation restrictions and adds the common REST methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`, and `HEAD`. Use it when you want a less restrictive file that still feels close to a normal REST API contract.

The `Aggressive` profile applies the same relaxation and adds every selectable OpenAPI method to every path, including `TRACE`. It also adds loose JSON request bodies to generated body-capable operations. Use it when you want to push an API harder and see how the real server behaves outside the documented happy path.

Choose `Custom` when you want to decide exactly which validation rules to remove and which HTTP methods to add.

## Move Shared And Operation-Level Parameters

OpenAPI parameters can be shared by every operation on a Path Item or declared directly on each operation. Evaluate the loaded specification to see whether its selected parameters are shared, operation-level, mixed, or absent. Convert toward either placement while preserving operation-level overrides.

Use `URL path parameters` for parameters associated with `{name}` segments. Use `All Path Item parameters` when shared query, header, and cookie parameters should move as well.

## Download A Less Restrictive OpenAPI File For REST Client Testing

After conversion, copy the JSON or download the tester OpenAPI file. You can import the converted file into REST clients that support OpenAPI, or use it as a reference while sending requests with the [Basic Client](/tools/online-clients/basic-client).

For more tool options, compare the [REST/HTTP client summary reviews](/tools/clients/summary-reviews), read the [REST/HTTP Clients overview](/tools/clients), or review detailed notes for [Bruno](/tools/clients/bruno), [Postman](/tools/clients/postman), [Insomnia](/tools/clients/insomnia), and [cURL](/tools/clients/curl).

## Use Converted OpenAPI Files In Embedded Clients And REST Clients

Use the `Open in Embedded Client` buttons to render the loaded OpenAPI file in [Swagger UI](/tools/online-clients/swagger), [OpenAPI Explorer](/tools/online-clients/openapi-explorer), [Scalar](/tools/online-clients/scalar), [Stoplight Elements](/tools/online-clients/stoplight), [Zudoku](/tools/online-clients/zudoku), or [Redoc](/tools/online-clients/redoc). When a transformation changes the specification, the embedded client opens the converted result; otherwise it opens the original URL or local file content.

If this is your first time using OpenAPI or REST clients, follow [How to Test REST APIs](/tutorials/rest-api-testing), the [API Simulator Walkthrough](/tutorials/api-simulator-walkthrough), the [Swagger UI and Tools](/tools/online-clients/swagger/about) guide, or the [OpenAPI for API Testing](/reference/openapi) reference first.

For examples of standard and less-validating OpenAPI files generated by this site, compare the [API Challenges OpenAPI Files](/apichallenges/openapi), [API Simulator OpenAPI Files](/practice-modes/simulation-openapi), [Simple API OpenAPI Files](/practice-modes/simpleapi-openapi), and [Buggy API OpenAPI Files](/practice-modes/shoppingcart-openapi).

## CORS Limits For Browser OpenAPI Conversion

Because this converter runs in the browser, URL loading is limited by CORS.

CORS means Cross-Origin Resource Sharing. It is the browser rule that controls whether JavaScript from one origin can call another origin. If the OpenAPI URL does not allow this site, the browser may block loading the file. Download the file and open it from disk if the server does not allow browser access.

The converted file can describe methods such as `TRACE`, but browsers may still block some methods or requests when you try them from Swagger UI. Use a desktop REST client, command line client, or [HTTP proxy](/tools/proxies) when you need to avoid browser limits or inspect raw traffic.

<script src="/js/vendor/js-yaml.min.js"></script>
<script src="/js/openapi-text-loader.js" defer></script>
<script src="/js/openapi-tester-converter.js" defer></script>
<script src="/js/openapi-parameter-placement.js" defer></script>
<script src="/js/openapi-tool-controls.js" defer></script>
<script src="/js/openapi-converter-page.js" defer></script>
