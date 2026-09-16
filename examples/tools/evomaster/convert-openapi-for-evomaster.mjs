#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";

const HTTP_METHODS = new Set([
  "get",
  "put",
  "post",
  "delete",
  "options",
  "head",
  "patch",
  "trace",
]);

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

const args = process.argv.slice(2);

if (args.length < 2 || args.includes("--help") || args.includes("-h")) {
  console.log(`Usage:
  node convert-openapi-for-evomaster.mjs <input-openapi.json> <output-openapi.json> [options]

Options:
  --keep-path-level      Keep path-level parameters after copying them to operations
  --no-strong-schemas    Only move parameters; do not infer missing schema types
  --add-uuid-format      Preserve or add format: uuid for UUID-like parameters and examples
`);
  process.exit(args.includes("--help") || args.includes("-h") ? 0 : 1);
}

const inputPath = args[0];
const outputPath = args[1];
const keepPathLevel = args.includes("--keep-path-level");
const strongSchemas = !args.includes("--no-strong-schemas");
const addUuidFormat = args.includes("--add-uuid-format");

const report = {
  pathParameterBlocks: 0,
  operationParameterCopies: 0,
  duplicateOperationParametersSkipped: 0,
  emptyParameterSchemasStrengthened: 0,
  schemaTypesAdded: 0,
  schemaFormatsAdded: 0,
  schemaFormatsRemoved: 0,
  minLengthAdded: 0,
  maxLengthAdded: 0,
};

const spec = JSON.parse(fs.readFileSync(inputPath, "utf8"));

movePathParametersToOperations(spec);

if (strongSchemas) {
  strengthenDocumentSchemas(spec);
}

fs.mkdirSync(path.dirname(path.resolve(outputPath)), { recursive: true });
fs.writeFileSync(outputPath, `${JSON.stringify(spec, null, 2)}\n`, "utf8");

console.log(JSON.stringify({
  input: path.resolve(inputPath),
  output: path.resolve(outputPath),
  options: {
    keepPathLevel,
    strongSchemas,
    addUuidFormat,
  },
  report,
}, null, 2));

function movePathParametersToOperations(openapi) {
  for (const pathItem of Object.values(openapi.paths ?? {})) {
    const pathLevelParameters = asArray(pathItem.parameters);
    if (pathLevelParameters.length === 0) continue;

    report.pathParameterBlocks += 1;

    for (const [method, operation] of Object.entries(pathItem)) {
      if (!HTTP_METHODS.has(method) || !operation || typeof operation !== "object") {
        continue;
      }

      const existing = asArray(operation.parameters);
      const copied = [];

      for (const param of pathLevelParameters) {
        if (existing.some((candidate) => sameParameter(candidate, param))) {
          report.duplicateOperationParametersSkipped += 1;
          continue;
        }

        copied.push(clone(param));
        report.operationParameterCopies += 1;
      }

      if (copied.length > 0 || existing.length > 0) {
        operation.parameters = [...copied, ...existing];
      }
    }

    if (!keepPathLevel) {
      delete pathItem.parameters;
    }
  }
}

function strengthenDocumentSchemas(openapi) {
  for (const pathItem of Object.values(openapi.paths ?? {})) {
    for (const [method, operation] of Object.entries(pathItem)) {
      if (!HTTP_METHODS.has(method) || !operation || typeof operation !== "object") {
        continue;
      }

      for (const parameter of asArray(operation.parameters)) {
        strengthenParameter(parameter);
      }

      for (const mediaType of Object.values(operation.requestBody?.content ?? {})) {
        strengthenSchema(mediaType.schema);
      }

      for (const response of Object.values(operation.responses ?? {})) {
        for (const mediaType of Object.values(response.content ?? {})) {
          strengthenSchema(mediaType.schema);
        }
      }
    }
  }

  for (const parameter of Object.values(openapi.components?.parameters ?? {})) {
    strengthenParameter(parameter);
  }

  for (const schema of Object.values(openapi.components?.schemas ?? {})) {
    strengthenSchema(schema);
  }
}

function strengthenParameter(parameter) {
  if (!parameter || typeof parameter !== "object") return;

  if (!parameter.schema || typeof parameter.schema !== "object") {
    parameter.schema = {};
  }

  const wasEmpty = Object.keys(parameter.schema).length === 0;
  const inferred = inferParameterSchema(parameter);

  mergeMissingSchemaFields(parameter.schema, inferred);

  if (wasEmpty && Object.keys(parameter.schema).length > 0) {
    report.emptyParameterSchemasStrengthened += 1;
  }

  strengthenSchema(parameter.schema, parameter);
}

function strengthenSchema(schema, context = {}) {
  if (!schema || typeof schema !== "object") return;
  if (schema.$ref) return;

  normalizeUuidFormat(schema);

  if (!schema.type) {
    const inferred = inferSchemaFromContext(schema, context);
    mergeMissingSchemaFields(schema, inferred);
  }

  addStringConstraintsFromDescription(schema);

  if (schema.properties && typeof schema.properties === "object") {
    for (const [name, propertySchema] of Object.entries(schema.properties)) {
      strengthenSchema(propertySchema, { name, ...propertySchema });
    }
  }

  strengthenSchema(schema.items);

  for (const key of ["allOf", "anyOf", "oneOf"]) {
    for (const child of asArray(schema[key])) {
      strengthenSchema(child);
    }
  }

  if (schema.additionalProperties && typeof schema.additionalProperties === "object") {
    strengthenSchema(schema.additionalProperties);
  }
}

function inferParameterSchema(parameter) {
  const name = String(parameter.name ?? "").toLowerCase();
  const location = String(parameter.in ?? "").toLowerCase();

  if (name === "x-challenger") {
    return uuidSchema();
  }

  if (name === "x-auth-token" || name === "authorization") {
    return { type: "string" };
  }

  if (name === "guid" || name.endsWith("guid")) {
    return uuidSchema();
  }

  if (location === "path" && ["id", "cartid", "relatedid"].includes(name)) {
    const fromExample = inferSchemaFromValue(parameter.example);
    if (fromExample?.type === "integer") return fromExample;
    return { type: "integer" };
  }

  return inferSchemaFromContext(parameter.schema ?? {}, parameter);
}

function inferSchemaFromContext(schema, context = {}) {
  const fromExample = inferSchemaFromValue(schema.example ?? context.example);
  if (fromExample) return fromExample;

  const name = String(context.name ?? "").toLowerCase();
  const description = String(schema.description ?? context.description ?? "").toLowerCase();

  if (name === "donestatus" || description.includes("boolean")) {
    return { type: "boolean" };
  }

  if (name === "guid" || name.endsWith("guid")) {
    return uuidSchema();
  }

  if (["id", "cartid", "relatedid"].includes(name)) {
    return { type: "integer" };
  }

  if (description.includes("string") || description.includes("text") || description.includes("title")) {
    return { type: "string" };
  }

  return null;
}

function inferSchemaFromValue(value) {
  if (value === undefined || value === null) return null;

  if (Array.isArray(value)) {
    return { type: "array" };
  }

  switch (typeof value) {
    case "boolean":
      return { type: "boolean" };
    case "number":
      return Number.isInteger(value) ? { type: "integer" } : { type: "number" };
    case "string":
      return UUID_PATTERN.test(value) ? uuidSchema() : { type: "string" };
    case "object":
      return { type: "object" };
    default:
      return null;
  }
}

function mergeMissingSchemaFields(target, inferred) {
  if (!inferred) return;

  if (!target.type && inferred.type) {
    target.type = inferred.type;
    report.schemaTypesAdded += 1;
  }

  if (!target.format && inferred.format) {
    target.format = inferred.format;
    report.schemaFormatsAdded += 1;
  }
}

function addStringConstraintsFromDescription(schema) {
  if (!schema || typeof schema !== "object") return;

  const description = String(schema.description ?? "");
  if (!description) return;

  if (schema.type === "string" && schema.minLength === undefined && /can\s+not\s+be\s+empty|cannot\s+be\s+empty/i.test(description)) {
    schema.minLength = 1;
    report.minLengthAdded += 1;
  }

  if (schema.type === "string" && schema.maxLength === undefined) {
    const match = description.match(/maximum\s+length\s+allowed\s+is\s+(\d+)/i);
    if (match) {
      schema.maxLength = Number(match[1]);
      report.maxLengthAdded += 1;
    }
  }
}

function normalizeUuidFormat(schema) {
  if (!addUuidFormat && schema.format === "uuid") {
    delete schema.format;
    report.schemaFormatsRemoved += 1;
  }
}

function sameParameter(a, b) {
  return a?.name === b?.name && a?.in === b?.in;
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function uuidSchema() {
  return addUuidFormat ? { type: "string", format: "uuid" } : { type: "string" };
}
