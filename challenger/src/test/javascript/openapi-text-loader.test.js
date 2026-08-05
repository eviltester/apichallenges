const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');
const loader = require('../../main/resources/public/js/openapi-text-loader.js');

function plainObject(value) {
  return JSON.parse(JSON.stringify(value));
}

function useVendoredYamlParser(t) {
  const previousYaml = globalThis.jsyaml;
  const sandbox = {};
  const source = fs.readFileSync(
    path.resolve(__dirname, '../../main/resources/public/js/vendor/js-yaml.min.js'),
    'utf8',
  );

  vm.createContext(sandbox);
  vm.runInContext(source, sandbox);
  globalThis.jsyaml = sandbox.jsyaml;
  t.after(() => {
    globalThis.jsyaml = previousYaml;
  });
}

test('parseOpenApiText parses JSON OpenAPI text', () => {
  assert.deepEqual(
    loader.parseOpenApiText('{"openapi":"3.1.0","info":{"title":"JSON","version":"1"},"paths":{}}', 'openapi.json'),
    {
      openapi: '3.1.0',
      info: {
        title: 'JSON',
        version: '1',
      },
      paths: {},
    },
  );
});

test('parseOpenApiText parses YAML OpenAPI text through the vendored js-yaml parser', (t) => {
  useVendoredYamlParser(t);

  assert.deepEqual(
    plainObject(loader.parseOpenApiText(
      [
        'openapi: 3.1.0',
        'info:',
        '  title: YAML',
        '  version: "1"',
        'paths:',
        '  /items:',
        '    get:',
        '      responses:',
        '        "200":',
        '          description: OK',
        '',
      ].join('\n'),
      'openapi.yaml',
    )),
    {
      openapi: '3.1.0',
      info: {
        title: 'YAML',
        version: '1',
      },
      paths: {
        '/items': {
          get: {
            responses: {
              200: {
                description: 'OK',
              },
            },
          },
        },
      },
    },
  );
});

test('parseOpenApiText rejects empty files', () => {
  assert.throws(
    () => loader.parseOpenApiText('  \n  ', 'empty.yaml'),
    /selected file is empty/,
  );
});

test('parseOpenApiText reports YAML parser availability', (t) => {
  const previousYaml = globalThis.jsyaml;
  t.after(() => {
    globalThis.jsyaml = previousYaml;
  });
  delete globalThis.jsyaml;

  assert.throws(
    () => loader.parseOpenApiText('openapi: 3.1.0\npaths: {}\n', 'openapi.yaml'),
    /openapi\.yaml is not JSON, and YAML parsing is unavailable/,
  );
});

test('fetchOpenApi loads and parses JSON from a URL', async (t) => {
  const previousFetch = globalThis.fetch;
  t.after(() => {
    globalThis.fetch = previousFetch;
  });
  globalThis.fetch = async function (url) {
    assert.equal(url, '/docs/openapi.json');
    return {
      ok: true,
      status: 200,
      text: async function () {
        return '{"openapi":"3.0.3","paths":{}}';
      },
    };
  };

  assert.deepEqual(
    await loader.fetchOpenApi('/docs/openapi.json'),
    {
      openapi: '3.0.3',
      paths: {},
    },
  );
});

test('fetchOpenApi loads and parses YAML from a URL', async (t) => {
  const previousFetch = globalThis.fetch;
  useVendoredYamlParser(t);
  t.after(() => {
    globalThis.fetch = previousFetch;
  });
  globalThis.fetch = async function (url) {
    assert.equal(url, '/docs/openapi.yaml');
    return {
      ok: true,
      status: 200,
      text: async function () {
        return 'openapi: 3.0.3\npaths: {}\n';
      },
    };
  };

  assert.deepEqual(
    plainObject(await loader.fetchOpenApi('/docs/openapi.yaml')),
    {
      openapi: '3.0.3',
      paths: {},
    },
  );
});

test('fetchOpenApi reports non-success HTTP responses', async (t) => {
  const previousFetch = globalThis.fetch;
  t.after(() => {
    globalThis.fetch = previousFetch;
  });
  globalThis.fetch = async function () {
    return {
      ok: false,
      status: 404,
      text: async function () {
        return 'not found';
      },
    };
  };

  await assert.rejects(
    () => loader.fetchOpenApi('/missing/openapi.json'),
    /Could not load \/missing\/openapi\.json\. The server returned HTTP 404/,
  );
});
