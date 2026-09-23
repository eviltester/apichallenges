const assert = require('node:assert/strict');
const test = require('node:test');
const placement = require('../../main/resources/public/js/openapi-parameter-placement.js');

function parameter(name, location = 'path', schema = { type: 'string' }) {
  return {
    name,
    in: location,
    required: location === 'path',
    schema,
  };
}

function specWithPath(pathItem) {
  return {
    openapi: '3.2.0',
    info: {
      title: 'Placement Test',
      version: '1.0.0',
    },
    paths: {
      '/items/{id}': pathItem,
    },
  };
}

test('evaluate distinguishes shared, operation-level, mixed, and empty placement', () => {
  const shared = placement.evaluate(specWithPath({
    parameters: [parameter('id')],
    get: { responses: {} },
  }));
  const operational = placement.evaluate(specWithPath({
    get: { parameters: [parameter('id')], responses: {} },
  }));
  const mixed = placement.evaluate(specWithPath({
    parameters: [parameter('id')],
    get: {
      parameters: [parameter('id', 'path', { type: 'integer' })],
      responses: {},
    },
  }));
  const empty = placement.evaluate({
    openapi: '3.1.0',
    info: { title: 'Empty', version: '1' },
    paths: {
      '/items': {
        get: { responses: {} },
      },
    },
  });

  assert.equal(shared.placement, 'path');
  assert.equal(operational.placement, 'operation');
  assert.equal(mixed.placement, 'mixed');
  assert.equal(mixed.metrics.overrides, 1);
  assert.equal(empty.placement, 'none');
});

test('evaluation scope can include every Path Item parameter', () => {
  const spec = specWithPath({
    parameters: [parameter('id'), parameter('X-TOKEN', 'header')],
    get: { responses: {} },
  });

  const pathOnly = placement.evaluate(spec, { scope: 'path' });
  const all = placement.evaluate(spec, { scope: 'all' });

  assert.equal(pathOnly.metrics.sharedDeclarations, 1);
  assert.equal(all.metrics.sharedDeclarations, 2);
});

test('evaluation explains when the selected scope has nothing to move', () => {
  const spec = {
    openapi: '3.1.0',
    info: { title: 'No Parameters', version: '1' },
    paths: {
      '/items': {
        get: { responses: {} },
      },
    },
  };

  const pathOnly = placement.evaluate(spec, { scope: 'path' });
  const all = placement.evaluate(spec, { scope: 'all' });

  assert.match(pathOnly.summary, /No URL path parameters found/);
  assert.match(pathOnly.summary, /nothing to move for this scope/);
  assert.match(pathOnly.summary, /Choose All Path Item parameters/);
  assert.match(all.summary, /No Path Item or operation parameters found/);
  assert.match(all.summary, /nothing to move/);
});

test('conversion to operation-level covers fixed and OpenAPI 3.2 additional operations', () => {
  const sharedId = parameter('id');
  const sharedToken = parameter('X-TOKEN', 'header');
  const postOverride = parameter('id', 'path', { type: 'integer' });
  const spec = specWithPath({
    parameters: [sharedId, sharedToken],
    get: { responses: {} },
    post: { parameters: [postOverride], responses: {} },
    query: { responses: {} },
    additionalOperations: {
      COPY: { responses: {} },
    },
  });

  const result = placement.convert(spec, { scope: 'all', target: 'operation' });
  const converted = result.spec.paths['/items/{id}'];

  assert.equal(converted.parameters, undefined);
  assert.deepEqual(converted.get.parameters, [sharedId, sharedToken]);
  assert.deepEqual(converted.post.parameters, [sharedToken, postOverride]);
  assert.deepEqual(converted.query.parameters, [sharedId, sharedToken]);
  assert.deepEqual(converted.additionalOperations.COPY.parameters, [sharedId, sharedToken]);
  assert.equal(result.metrics.sharedDeclarationsMoved, 2);
  assert.equal(result.metrics.operationCopiesAdded, 7);
  assert.equal(result.metrics.overridesPreserved, 1);
  assert.equal(result.after.placement, 'operation');
  assert.equal(spec.paths['/items/{id}'].parameters.length, 2);
});

test('path-only conversion leaves other shared parameter locations untouched', () => {
  const spec = specWithPath({
    parameters: [parameter('id'), parameter('filter', 'query')],
    get: { responses: {} },
  });

  const result = placement.convert(spec, { scope: 'path', target: 'operation' });
  const converted = result.spec.paths['/items/{id}'];

  assert.deepEqual(converted.parameters, [parameter('filter', 'query')]);
  assert.deepEqual(converted.get.parameters, [parameter('id')]);
  assert.equal(result.after.placement, 'operation');
});

test('conversion to shared path-level promotes common definitions', () => {
  const id = parameter('id', 'path', { type: 'integer' });
  const spec = specWithPath({
    get: { parameters: [id], responses: {} },
    post: { parameters: [id], responses: {} },
  });

  const result = placement.convert(spec, { target: 'path' });
  const converted = result.spec.paths['/items/{id}'];

  assert.deepEqual(converted.parameters, [id]);
  assert.equal(converted.get.parameters, undefined);
  assert.equal(converted.post.parameters, undefined);
  assert.equal(result.metrics.operationDeclarationsPromoted, 1);
  assert.equal(result.metrics.operationDeclarationsRemoved, 2);
  assert.equal(result.after.placement, 'path');
});

test('shared conversion keeps a differing operation override', () => {
  const commonId = parameter('id', 'path', { type: 'integer' });
  const overrideId = parameter('id', 'path', { type: 'string', pattern: '^[0-9]+$' });
  const spec = specWithPath({
    get: { parameters: [commonId], responses: {} },
    post: { parameters: [commonId], responses: {} },
    patch: { parameters: [overrideId], responses: {} },
  });

  const result = placement.convert(spec, { target: 'path' });
  const converted = result.spec.paths['/items/{id}'];

  assert.deepEqual(converted.parameters, [commonId]);
  assert.equal(converted.get.parameters, undefined);
  assert.equal(converted.post.parameters, undefined);
  assert.deepEqual(converted.patch.parameters, [overrideId]);
  assert.equal(result.metrics.overridesPreserved, 1);
  assert.equal(result.after.placement, 'mixed');
});

test('shared conversion reports tied definitions and missing operation parameters without guessing', () => {
  const integerId = parameter('id', 'path', { type: 'integer' });
  const stringId = parameter('id');
  const tied = specWithPath({
    get: { parameters: [integerId], responses: {} },
    post: { parameters: [stringId], responses: {} },
  });
  const missing = specWithPath({
    get: { parameters: [integerId], responses: {} },
    post: { responses: {} },
  });

  const tiedResult = placement.convert(tied, { target: 'path' });
  const missingResult = placement.convert(missing, { target: 'path' });

  assert.equal(tiedResult.changed, false);
  assert.match(tiedResult.warnings.join('\n'), /no unambiguous shared definition/);
  assert.equal(missingResult.changed, false);
  assert.match(missingResult.warnings.join('\n'), /missing from one or more operations/);
  assert.match(missingResult.warnings.join('\n'), /does not declare the \{id\}/);
});

test('local component references are compared while ambiguous external references are retained', () => {
  const localReference = { $ref: '#/components/parameters/ItemId' };
  const externalReference = { $ref: 'common.yaml#/components/parameters/ItemId' };
  const spec = specWithPath({
    parameters: [localReference, externalReference],
    get: { parameters: [parameter('id')], responses: {} },
  });
  spec.components = {
    parameters: {
      ItemId: parameter('id'),
    },
  };

  const result = placement.convert(spec, { scope: 'all', target: 'operation' });
  const converted = result.spec.paths['/items/{id}'];

  assert.deepEqual(converted.parameters, [externalReference]);
  assert.deepEqual(converted.get.parameters, [parameter('id')]);
  assert.match(result.warnings.join('\n'), /external parameter reference/);
});

test('malformed local references are reported without stopping evaluation', () => {
  const malformedReference = { $ref: '#/components/parameters/%E0%A4%A' };
  const spec = specWithPath({
    parameters: [malformedReference],
    get: { responses: {} },
  });

  const evaluation = placement.evaluate(spec, { scope: 'all' });
  const conversion = placement.convert(spec, { scope: 'all', target: 'operation' });

  assert.equal(evaluation.metrics.unresolvedParameters, 1);
  assert.match(evaluation.warnings.join('\n'), /unresolved parameter reference/);
  assert.equal(conversion.changed, true);
  assert.equal(conversion.spec.paths['/items/{id}'].parameters, undefined);
  assert.deepEqual(conversion.spec.paths['/items/{id}'].get.parameters, [malformedReference]);
});

test('evaluation reports referenced Path Items and invalid optional path parameters', () => {
  const optionalId = parameter('id');
  optionalId.required = false;
  const spec = {
    openapi: '3.1.0',
    info: { title: 'Warnings', version: '1' },
    paths: {
      '/items/{id}': {
        get: { parameters: [optionalId], responses: {} },
      },
      '/linked/{id}': {
        $ref: '#/components/pathItems/Linked',
      },
    },
  };

  const result = placement.evaluate(spec);

  assert.equal(result.metrics.optionalPathParameters, 1);
  assert.equal(result.metrics.referencedPathItemsSkipped, 1);
  assert.match(result.warnings.join('\n'), /not marked required/);
  assert.match(result.warnings.join('\n'), /referenced Path Item/);
});

test('unsupported documents receive a clear error', () => {
  assert.throws(
    () => placement.evaluate({ swagger: '2.0', paths: {} }),
    /OpenAPI 3\.x/,
  );
});
