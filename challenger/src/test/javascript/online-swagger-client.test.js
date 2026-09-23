const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');
const controls = require('../../main/resources/public/js/openapi-tool-controls.js');

const scriptSource = fs.readFileSync(
  path.resolve(__dirname, '../../main/resources/public/js/online-swagger-client.js'),
  'utf8',
);

function classList() {
  const classes = new Set();
  return {
    add(name) {
      classes.add(name);
    },
    toggle(name, enabled) {
      if (enabled) {
        classes.add(name);
      } else {
        classes.delete(name);
      }
    },
  };
}

function element(initial = {}) {
  const listeners = {};
  return Object.assign({
    addEventListener(name, listener) {
      listeners[name] = listener;
    },
    dispatch(name, event = {}) {
      return listeners[name](event);
    },
    classList: classList(),
    dataset: {},
    innerHTML: '',
    textContent: '',
    value: '',
  }, initial);
}

function swaggerHarness({ search = '', storedValues = {} } = {}) {
  const form = element();
  const urlInput = element({ value: '/api/docs/openapi.json' });
  const fileInput = element({ files: [] });
  const status = element();
  const target = element({ id: 'swagger-target' });
  const renderedSpecs = [];
  const fetchRequests = [];
  const fetchedSpec = {
    openapi: '3.1.0',
    info: { title: 'Original API', version: '1' },
    paths: {},
  };

  const client = {
    dataset: { defaultOpenapiUrl: '/api/docs/openapi.json' },
    querySelector(selector) {
      return {
        '[data-openapi-url-form]': form,
        '[data-openapi-url]': urlInput,
        '[data-openapi-file]': fileInput,
        '[data-openapi-status]': status,
        '[data-openapi-render-target]': target,
      }[selector] || null;
    },
  };

  function SwaggerUIBundle(options) {
    renderedSpecs.push(options.spec);
    return { options };
  }
  SwaggerUIBundle.presets = { apis: {} };
  SwaggerUIBundle.plugins = { DownloadUrl: {} };

  const window = {
    ApiChallengesOpenApiTextLoader: {
      fetchOpenApi(url) {
        fetchRequests.push(url);
        return Promise.resolve(fetchedSpec);
      },
    },
    ApiChallengesOpenApiToolControls: controls,
    SwaggerUIBundle,
    location: { search },
    sessionStorage: {
      getItem(key) {
        return Object.prototype.hasOwnProperty.call(storedValues, key)
          ? storedValues[key]
          : null;
      },
    },
  };

  const sandbox = {
    FileReader: class {},
    Promise,
    URLSearchParams,
    document: {
      readyState: 'complete',
      addEventListener() {},
      querySelectorAll(selector) {
        return selector === '[data-online-swagger-client]' ? [client] : [];
      },
    },
    window,
  };
  vm.runInNewContext(scriptSource, sandbox);

  return {
    fetchRequests,
    renderedSpecs,
    status,
  };
}

test('Swagger loads and renders the source specification without converting it', async () => {
  const page = swaggerHarness();
  await Promise.resolve();
  await Promise.resolve();

  assert.deepEqual(page.fetchRequests, ['/api/docs/openapi.json']);
  assert.equal(page.renderedSpecs.length, 1);
  assert.equal(page.renderedSpecs[0].info.title, 'Original API');
  assert.equal(page.status.textContent, 'Loaded OpenAPI from /api/docs/openapi.json.');
});

test('Swagger renders a converted specification handed off by the converter', () => {
  const storageKey = 'apiChallengesConvertedOpenApiSpec:test-id';
  const convertedSpec = {
    openapi: '3.1.0',
    info: { title: 'Converted API', version: '1' },
    paths: {},
  };
  const page = swaggerHarness({
    search: `?converted=${encodeURIComponent(storageKey)}`,
    storedValues: {
      [storageKey]: JSON.stringify({
        name: 'converted-openapi.json',
        spec: JSON.stringify(convertedSpec),
      }),
    },
  });

  assert.deepEqual(page.fetchRequests, []);
  assert.equal(page.renderedSpecs.length, 1);
  assert.equal(page.renderedSpecs[0].info.title, 'Converted API');
  assert.equal(
    page.status.textContent,
    'Loaded converted OpenAPI from converted-openapi.json.',
  );
});
