const assert = require('node:assert/strict');
const test = require('node:test');
const converter = require('../../main/resources/public/js/openapi-tester-converter.js');

function sampleSpec() {
  return {
    openapi: '3.1.0',
    info: {
      title: 'Sample',
      version: '1.0.0',
    },
    components: {
      schemas: {
        Item: {
          type: 'object',
          required: ['name'],
          additionalProperties: false,
          properties: {
            name: {
              type: 'string',
              enum: ['ALPHA'],
              pattern: '^[A-Z]+$',
              minLength: 3,
              maxLength: 20,
            },
            tags: {
              type: 'array',
              minItems: 1,
              maxItems: 3,
              uniqueItems: true,
              items: {
                type: 'string',
                const: 'tag',
              },
            },
            count: {
              type: 'integer',
              minimum: 1,
              maximum: 10,
              multipleOf: 1,
            },
          },
        },
      },
      parameters: {
        RequiredToken: {
          name: 'X-TOKEN',
          in: 'header',
          required: true,
          schema: {
            type: 'string',
            pattern: '^[a-f0-9]+$',
          },
        },
      },
      requestBodies: {
        ItemBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['status'],
                additionalProperties: false,
                properties: {
                  status: {
                    type: 'string',
                    enum: ['new', 'done'],
                  },
                },
              },
            },
          },
        },
      },
      headers: {
        RateLimit: {
          schema: {
            type: 'integer',
            minimum: 0,
            maximum: 100,
          },
        },
      },
    },
    paths: {
      '/items/{id}': {
        parameters: [
          {
            name: 'id',
            in: 'path',
            required: false,
            schema: {
              type: 'integer',
              minimum: 1,
            },
          },
        ],
        get: {
          operationId: 'getItem',
          parameters: [
            {
              name: 'filter',
              in: 'query',
              required: true,
              schema: {
                type: 'string',
                enum: ['active'],
                minLength: 2,
              },
            },
          ],
          responses: {
            200: {
              description: 'OK',
            },
          },
        },
        post: {
          operationId: 'createItem',
          requestBody: {
            required: true,
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  required: ['name'],
                  additionalProperties: false,
                  properties: {
                    name: {
                      type: 'string',
                      pattern: '^[A-Z]+$',
                      minLength: 3,
                    },
                    count: {
                      type: 'integer',
                      minimum: 1,
                      maximum: 10,
                    },
                  },
                },
              },
            },
          },
          responses: {
            201: {
              description: 'Created',
            },
          },
        },
      },
      '/linked': {
        $ref: '#/components/pathItems/LinkedPath',
      },
    },
  };
}

function convert(profile, spec = sampleSpec()) {
  return converter.convert(spec, converter.profileOptions(profile));
}

test('profileOptions returns independent profile configurations', () => {
  const firstPractical = converter.profileOptions('practical');
  const secondPractical = converter.profileOptions('practical');

  firstPractical.verbs.push('trace');

  assert.deepEqual(secondPractical.verbs, ['get', 'post', 'put', 'patch', 'delete', 'options', 'head']);
  assert.deepEqual(converter.profileOptions('aggressive').verbs, converter.methods);
  assert.equal(converter.profileOptions('custom').profile, 'custom');
  assert.equal(converter.profileOptions('unknown').profile, 'original');
});

test('normaliseOptions deduplicates supported verbs and backfills profile defaults', () => {
  assert.deepEqual(
    converter.normaliseOptions({
      profile: 'custom',
      verbs: ['POST', 'post', 'TRACE', 'invalid', ''],
    }).verbs,
    ['post', 'trace'],
  );

  assert.deepEqual(
    converter.normaliseOptions({
      profile: 'aggressive',
      addMissingOperations: true,
      verbs: [],
    }).verbs,
    converter.methods,
  );
});

test('original profile returns an unchanged clone without conversion', () => {
  const spec = sampleSpec();
  const result = convert('original', spec);

  assert.equal(result.converted, false);
  assert.equal(result.summary, 'Original OpenAPI rendered unchanged.');
  assert.deepEqual(result.spec, spec);

  result.spec.info.title = 'Changed';
  assert.equal(spec.info.title, 'Sample');
});

test('practical profile adds common REST methods without overwriting existing operations', () => {
  const result = convert('practical');
  const path = result.spec.paths['/items/{id}'];

  assert.equal(result.converted, true);
  assert.equal(path.get.operationId, 'getItem');
  assert.equal(path.post.operationId, 'createItem');
  assert.ok(path.put);
  assert.ok(path.patch);
  assert.ok(path.delete);
  assert.ok(path.options);
  assert.ok(path.head);
  assert.equal(path.trace, undefined);
  assert.match(path.put.operationId, /^generatedTester_put_items_id/);
  assert.equal(path.put.responses['405'].description, 'Method not allowed or unsupported by this API.');
  assert.ok(path.put.responses.default);
});

test('practical profile relaxes path, operation, component, and header validation', () => {
  const result = convert('practical');
  const path = result.spec.paths['/items/{id}'];
  const pathParameter = path.parameters[0];
  const queryParameter = path.get.parameters[0];
  const postBody = path.post.requestBody;
  const postSchema = postBody.content['application/json'].schema;
  const itemSchema = result.spec.components.schemas.Item;
  const tokenParameter = result.spec.components.parameters.RequiredToken;
  const componentBody = result.spec.components.requestBodies.ItemBody;
  const rateLimitHeader = result.spec.components.headers.RateLimit;

  assert.equal(pathParameter.required, true);
  assert.equal(pathParameter.schema.minimum, undefined);
  assert.equal(queryParameter.required, false);
  assert.equal(queryParameter.schema.enum, undefined);
  assert.equal(queryParameter.schema.minLength, undefined);
  assert.equal(postBody.required, false);
  assert.equal(postSchema.required, undefined);
  assert.equal(postSchema.additionalProperties, true);
  assert.equal(postSchema.properties.name.pattern, undefined);
  assert.equal(postSchema.properties.name.minLength, undefined);
  assert.equal(postSchema.properties.count.minimum, undefined);
  assert.equal(postSchema.properties.count.maximum, undefined);
  assert.equal(itemSchema.required, undefined);
  assert.equal(itemSchema.additionalProperties, true);
  assert.equal(itemSchema.properties.name.enum, undefined);
  assert.equal(itemSchema.properties.name.pattern, undefined);
  assert.equal(itemSchema.properties.name.maxLength, undefined);
  assert.equal(itemSchema.properties.tags.minItems, undefined);
  assert.equal(itemSchema.properties.tags.maxItems, undefined);
  assert.equal(itemSchema.properties.tags.uniqueItems, undefined);
  assert.equal(itemSchema.properties.tags.items.const, undefined);
  assert.equal(itemSchema.properties.count.multipleOf, undefined);
  assert.equal(tokenParameter.required, false);
  assert.equal(tokenParameter.schema.pattern, undefined);
  assert.equal(componentBody.required, false);
  assert.equal(componentBody.content['application/json'].schema.required, undefined);
  assert.equal(componentBody.content['application/json'].schema.additionalProperties, true);
  assert.equal(componentBody.content['application/json'].schema.properties.status.enum, undefined);
  assert.equal(rateLimitHeader.schema.minimum, undefined);
  assert.equal(rateLimitHeader.schema.maximum, undefined);
});

test('conversion does not mutate the original spec', () => {
  const spec = sampleSpec();

  convert('practical', spec);

  assert.equal(spec.paths['/items/{id}'].parameters[0].required, false);
  assert.equal(spec.paths['/items/{id}'].get.parameters[0].required, true);
  assert.equal(spec.paths['/items/{id}'].post.requestBody.required, true);
  assert.deepEqual(spec.paths['/items/{id}'].post.requestBody.content['application/json'].schema.required, ['name']);
  assert.equal(spec.paths['/items/{id}'].post.requestBody.content['application/json'].schema.additionalProperties, false);
  assert.equal(spec.components.schemas.Item.properties.name.pattern, '^[A-Z]+$');
});

test('aggressive profile adds every OpenAPI method and loose bodies to generated body methods', () => {
  const result = convert('aggressive');
  const path = result.spec.paths['/items/{id}'];

  converter.methods.forEach((method) => {
    assert.ok(path[method], method);
  });

  assert.deepEqual(
    path.put.requestBody.content['application/json'].schema,
    {
      type: 'object',
      additionalProperties: true,
    },
  );
  assert.deepEqual(
    path.patch.requestBody.content['application/json'].schema,
    {
      type: 'object',
      additionalProperties: true,
    },
  );
  assert.equal(path.options.requestBody, undefined);
  assert.equal(path.head.requestBody, undefined);
  assert.ok(path.trace);
});

test('generated operations keep copied path parameters required', () => {
  const result = convert('aggressive');
  const generatedPut = result.spec.paths['/items/{id}'].put;

  assert.equal(generatedPut.parameters[0].name, 'id');
  assert.equal(generatedPut.parameters[0].in, 'path');
  assert.equal(generatedPut.parameters[0].required, true);
});

test('generated operation ids are stable and avoid collisions', () => {
  const spec = sampleSpec();
  spec.paths['/items/{id}'].get.operationId = 'generatedTester_put_items_id';

  const result = convert('practical', spec);

  assert.equal(result.spec.paths['/items/{id}'].put.operationId, 'generatedTester_put_items_id_2');
});

test('path item references are not modified or expanded', () => {
  const result = convert('aggressive');

  assert.deepEqual(result.spec.paths['/linked'], {
    $ref: '#/components/pathItems/LinkedPath',
  });
});

test('custom options only apply selected relaxations and methods', () => {
  const spec = sampleSpec();
  const result = converter.convert(spec, {
    profile: 'custom',
    relaxSchemaConstraints: false,
    removeRequiredProperties: false,
    makeNonPathParametersOptional: false,
    allowAdditionalProperties: false,
    makeRequestBodiesOptional: true,
    addMissingOperations: true,
    addLooseRequestBodiesToGeneratedOperations: false,
    verbs: ['trace', 'post'],
  });
  const path = result.spec.paths['/items/{id}'];
  const postSchema = path.post.requestBody.content['application/json'].schema;

  assert.equal(path.trace.operationId, 'generatedTester_trace_items_id');
  assert.equal(path.put, undefined);
  assert.equal(path.post.operationId, 'createItem');
  assert.equal(path.get.parameters[0].required, true);
  assert.equal(path.get.parameters[0].schema.enum[0], 'active');
  assert.equal(path.post.requestBody.required, false);
  assert.deepEqual(postSchema.required, ['name']);
  assert.equal(postSchema.additionalProperties, false);
  assert.equal(postSchema.properties.name.pattern, '^[A-Z]+$');
});

test('unsupported Swagger 2 specs produce a clear conversion error', () => {
  assert.throws(
    () => converter.convert({ swagger: '2.0', paths: {} }, converter.profileOptions('practical')),
    /OpenAPI 3\.x/,
  );
});

test('stringify and convertedFilename produce export-friendly JSON output', () => {
  assert.equal(
    converter.stringify({ openapi: '3.1.0', paths: {} }),
    '{\n  "openapi": "3.1.0",\n  "paths": {}\n}',
  );
  assert.equal(
    converter.convertedFilename('https://example.com/openapi.yaml?version=1'),
    'openapi-tester-openapi.json',
  );
  assert.equal(
    converter.convertedFilename('bad name?.json'),
    'bad-name-tester-openapi.json',
  );
});
