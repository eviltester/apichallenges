const assert = require('node:assert/strict');
const test = require('node:test');
const loader = require('../../main/resources/public/js/openapi-text-loader.js');

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

test('parseOpenApiText parses YAML OpenAPI text through js-yaml', (t) => {
  const previousYaml = globalThis.jsyaml;
  t.after(() => {
    globalThis.jsyaml = previousYaml;
  });
  globalThis.jsyaml = {
    load(text) {
      assert.match(text, /openapi: 3\.1\.0/);
      assert.match(text, /title: YAML/);
      return {
        openapi: '3.1.0',
        info: {
          title: 'YAML',
          version: '1',
        },
        paths: {},
      };
    },
  };

  assert.deepEqual(
    loader.parseOpenApiText('openapi: 3.1.0\ninfo:\n  title: YAML\n  version: "1"\npaths: {}\n', 'openapi.yaml'),
    {
      openapi: '3.1.0',
      info: {
        title: 'YAML',
        version: '1',
      },
      paths: {},
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
  const previousYaml = globalThis.jsyaml;
  t.after(() => {
    globalThis.fetch = previousFetch;
    globalThis.jsyaml = previousYaml;
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
  globalThis.jsyaml = {
    load() {
      return {
        openapi: '3.0.3',
        paths: {},
      };
    },
  };

  assert.deepEqual(
    await loader.fetchOpenApi('/docs/openapi.yaml'),
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
