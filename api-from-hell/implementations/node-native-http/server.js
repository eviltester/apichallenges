#!/usr/bin/env node

const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = Number(process.env.PORT || 3001);
const PREFIX = normalizePrefix(process.env.FROMHELL_PREFIX || "/fromhell");
const catalogPath = process.env.FROMHELL_CATALOG ||
  path.resolve(__dirname, "../../catalog/fromhell-catalog.json");
const catalog = JSON.parse(fs.readFileSync(catalogPath, "utf8"));

const commonHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS",
  "Access-Control-Allow-Headers":
    "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With"
};
const noBodyStatusCodes = new Set([204, 205, 304]);

const endpointsByPath = new Map();
for (const endpoint of catalog.endpoints) {
  const methods = endpointsByPath.get(endpoint.path) || new Map();
  methods.set(endpoint.method.toUpperCase(), endpoint);
  endpointsByPath.set(endpoint.path, methods);
}

const server = http.createServer((request, response) => {
  const parsedUrl = new URL(request.url, `http://${request.headers.host || "localhost"}`);
  const catalogPathForRequest = removePrefix(parsedUrl.pathname, PREFIX);

  if (request.method === "GET" && parsedUrl.pathname === "/docs/openapi.json") {
    sendJson(response, openApiFor(request));
    return;
  }

  if (!catalogPathForRequest || !endpointsByPath.has(catalogPathForRequest)) {
    response.writeHead(404, { ...commonHeaders });
    response.end("Not Found");
    return;
  }

  const endpointsForPath = endpointsByPath.get(catalogPathForRequest);
  const allowedMethods = allowedMethodsFor(endpointsForPath);

  if (request.method === "OPTIONS") {
    response.writeHead(204, { ...commonHeaders, Allow: allowedMethods.join(", ") });
    response.end();
    return;
  }

  const endpoint = endpointsForPath.get(request.method);
  if (!endpoint) {
    response.writeHead(405, { ...commonHeaders, Allow: allowedMethods.join(", ") });
    response.end("Method Not Allowed");
    return;
  }

  const headers = { ...commonHeaders };
  for (const header of endpoint.headers || []) {
    headers[header.name] = header.value;
  }

  sendEndpointResponse(request, response, endpoint.statusCode, headers, endpoint.body || "");
});

server.listen(PORT, () => {
  console.log(`API From Hell node-native-http listening on http://localhost:${PORT}${PREFIX}`);
});

function normalizePrefix(prefix) {
  if (!prefix || prefix === "/") {
    return "";
  }
  return "/" + prefix.replace(/^\/+|\/+$/g, "");
}

function removePrefix(requestPath, prefix) {
  if (!prefix) {
    return requestPath;
  }
  if (requestPath === prefix) {
    return "/";
  }
  if (!requestPath.startsWith(prefix + "/")) {
    return null;
  }
  return requestPath.substring(prefix.length);
}

function allowedMethodsFor(endpointsForPath) {
  const methods = Array.from(endpointsForPath.keys());
  if (methods.includes("GET") && !methods.includes("HEAD")) {
    methods.push("HEAD");
  }
  methods.push("OPTIONS");
  return [...new Set(methods)];
}

function sendJson(response, data) {
  response.writeHead(200, { ...commonHeaders, "Content-Type": "application/json" });
  response.end(JSON.stringify(data, null, 2));
}

function sendEndpointResponse(request, response, statusCode, headers, body) {
  const responseBody = request.method === "HEAD" ? "" : body;
  if (responseBody && noBodyStatusCodes.has(statusCode)) {
    sendRawResponse(response, statusCode, headers, responseBody);
    return;
  }

  response.writeHead(statusCode, headers);
  response.end(Buffer.from(responseBody, "utf8"));
}

function sendRawResponse(response, statusCode, headers, body) {
  const bodyBytes = Buffer.from(body, "utf8");
  const rawHeaders = {
    ...headers,
    "Content-Length": String(bodyBytes.length),
    "Connection": "close"
  };
  const headerLines = [
    `HTTP/1.1 ${statusCode} ${http.STATUS_CODES[statusCode] || ""}`,
    ...Object.entries(rawHeaders).map(([name, value]) => `${name}: ${value}`)
  ];

  response.socket.write(headerLines.join("\r\n") + "\r\n\r\n");
  response.socket.write(bodyBytes);
  response.socket.end();
}

function openApiFor(request) {
  const origin = `http://${request.headers.host || `localhost:${PORT}`}`;
  const paths = {};
  for (const endpoint of catalog.endpoints) {
    const fullPath = PREFIX + endpoint.path;
    paths[fullPath] = paths[fullPath] || {};
    const response = {
      description: endpoint.documentation || endpoint.label,
      headers: {}
    };
    for (const header of endpoint.headers || []) {
      response.headers[header.name] = {
        schema: { type: "string" },
        example: header.value
      };
    }
    const contentType = (endpoint.headers || [])
      .find((header) => header.name.toLowerCase() === "content-type");
    if (contentType) {
      response.content = {
        [contentType.value]: {
          schema: { type: "string" },
          example: endpoint.body || ""
        }
      };
    }
    paths[fullPath][endpoint.method.toLowerCase()] = {
      summary: endpoint.label,
      description: [
        endpoint.documentation,
        endpoint.problem ? `Problem: ${endpoint.problem}` : "",
        endpoint.expectation ? `Expected client behavior: ${endpoint.expectation}` : ""
      ].filter(Boolean).join("\n\n"),
      responses: {
        [String(endpoint.statusCode)]: response
      }
    };
  }

  return {
    openapi: "3.0.3",
    info: {
      title: catalog.name,
      version: "1.0.0",
      description: catalog.description
    },
    servers: [{ url: origin }],
    paths
  };
}
