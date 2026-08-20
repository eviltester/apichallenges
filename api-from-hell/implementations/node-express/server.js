#!/usr/bin/env node

const express = require("express");
const fs = require("fs");
const path = require("path");

const PORT = Number(process.env.PORT || 3001);
const PREFIX = normalizePrefix(process.env.FROMHELL_PREFIX || "/fromhell");
const catalogPath = process.env.FROMHELL_CATALOG ||
  path.resolve(__dirname, "../../catalog/fromhell-catalog.json");
const catalog = JSON.parse(fs.readFileSync(catalogPath, "utf8"));
const app = express();
const noBodyStatusCodes = new Set([204, 205, 304]);

const endpointsByPath = new Map();
for (const endpoint of catalog.endpoints) {
  const methods = endpointsByPath.get(endpoint.path) || new Map();
  methods.set(endpoint.method.toUpperCase(), endpoint);
  endpointsByPath.set(endpoint.path, methods);
}

app.use((_request, response, next) => {
  response.set("Access-Control-Allow-Origin", "*");
  response.set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS");
  response.set(
    "Access-Control-Allow-Headers",
    "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With"
  );
  next();
});

for (const [catalogPathForEndpoint, endpointsForPath] of endpointsByPath.entries()) {
  const routePath = PREFIX + catalogPathForEndpoint;
  for (const endpoint of endpointsForPath.values()) {
    app[endpoint.method.toLowerCase()](routePath, (request, response) => {
      sendEndpointResponse(request, response, endpoint);
    });
  }

  app.options(routePath, (_request, response) => {
    response.set("Allow", allowedMethodsFor(endpointsForPath).join(", "));
    response.status(204).send("");
  });

  app.all(routePath, (_request, response) => {
    response.set("Allow", allowedMethodsFor(endpointsForPath).join(", "));
    response.status(405).send("Method Not Allowed");
  });
}

app.get("/docs/openapi.json", (request, response) => {
  response.json(openApiFor(request));
});

app.use((_request, response) => {
  response.status(404).send("Not Found");
});

app.listen(PORT, () => {
  console.log(`API From Hell node-express listening on http://localhost:${PORT}${PREFIX}`);
});

function applyEndpoint(response, endpoint) {
  response.status(endpoint.statusCode);
  for (const header of endpoint.headers || []) {
    response.set(header.name, header.value);
  }
  if (!(endpoint.headers || []).some((header) => header.name.toLowerCase() === "content-type")) {
    response.removeHeader("Content-Type");
  }
}

function sendEndpointResponse(request, response, endpoint) {
  const headers = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS",
    "Access-Control-Allow-Headers":
      "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With"
  };
  for (const header of endpoint.headers || []) {
    headers[header.name] = header.value;
  }

  const body = request.method === "HEAD" ? "" : (endpoint.body || "");
  if (body && noBodyStatusCodes.has(endpoint.statusCode)) {
    sendRawResponse(response, endpoint.statusCode, headers, body);
    return;
  }

  response.writeHead(endpoint.statusCode, headers);
  response.end(Buffer.from(body, "utf8"));
}

function sendRawResponse(response, statusCode, headers, body) {
  const bodyBytes = Buffer.from(body, "utf8");
  const rawHeaders = {
    ...headers,
    "Content-Length": String(bodyBytes.length),
    "Connection": "close"
  };
  const headerLines = [
    `HTTP/1.1 ${statusCode} ${httpStatusMessage(statusCode)}`,
    ...Object.entries(rawHeaders).map(([name, value]) => `${name}: ${value}`)
  ];

  response.socket.write(headerLines.join("\r\n") + "\r\n\r\n");
  response.socket.write(bodyBytes);
  response.socket.end();
}

function httpStatusMessage(statusCode) {
  const statuses = {
    204: "No Content",
    205: "Reset Content",
    304: "Not Modified"
  };
  return statuses[statusCode] || "";
}

function normalizePrefix(prefix) {
  if (!prefix || prefix === "/") {
    return "";
  }
  return "/" + prefix.replace(/^\/+|\/+$/g, "");
}

function allowedMethodsFor(endpointsForPath) {
  const methods = Array.from(endpointsForPath.keys());
  if (methods.includes("GET") && !methods.includes("HEAD")) {
    methods.push("HEAD");
  }
  methods.push("OPTIONS");
  return [...new Set(methods)];
}

function openApiFor(request) {
  const origin = `${request.protocol}://${request.get("host")}`;
  const paths = {};
  for (const endpoint of catalog.endpoints) {
    const fullPath = PREFIX + endpoint.path;
    paths[fullPath] = paths[fullPath] || {};
    paths[fullPath][endpoint.method.toLowerCase()] = {
      summary: endpoint.label,
      description: [endpoint.documentation, endpoint.problem, endpoint.expectation]
        .filter(Boolean)
        .join("\n\n"),
      responses: {
        [String(endpoint.statusCode)]: {
          description: endpoint.documentation || endpoint.label
        }
      }
    };
  }
  return {
    openapi: "3.0.3",
    info: { title: catalog.name, version: "1.0.0", description: catalog.description },
    servers: [{ url: origin }],
    paths
  };
}
