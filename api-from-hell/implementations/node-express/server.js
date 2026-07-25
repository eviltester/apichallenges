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
    app[endpoint.method.toLowerCase()](routePath, (_request, response) => {
      applyEndpoint(response, endpoint);
      response.send(endpoint.body || "");
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
