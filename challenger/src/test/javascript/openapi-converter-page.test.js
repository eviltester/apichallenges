const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');
const controls = require('../../main/resources/public/js/openapi-tool-controls.js');

const scriptSource = fs.readFileSync(
  path.resolve(__dirname, '../../main/resources/public/js/openapi-converter-page.js'),
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
    contains(name) {
      return classes.has(name);
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
    disabled: false,
    textContent: '',
    value: '',
  }, initial);
}

function sessionStorage() {
  const values = new Map();
  return {
    get length() {
      return values.size;
    },
    key(index) {
      return Array.from(values.keys())[index] || null;
    },
    getItem(key) {
      return values.has(key) ? values.get(key) : null;
    },
    setItem(key, value) {
      values.set(key, value);
    },
    removeItem(key) {
      values.delete(key);
    },
  };
}

function converterPageHarness({
  fetchError = null,
  openApiUrl = '/docs/openapi.json',
  openApiText = '{"openapi":"3.1.0"}\n',
  placementChanges = true,
  placementTarget = 'keep',
  profile = 'original',
  sourceSearch = '',
} = {}) {
  const form = element();
  const urlInput = element({ value: openApiUrl });
  const loadStatus = element({ hidden: true });
  const fileInput = element({ files: [] });
  const profileInput = element({ value: profile });
  const status = element();
  const placementStatus = element();
  const placementMessage = element();
  const placementComparison = element({ hidden: true });
  const placementCurrent = element();
  const placementOutput = element();
  const placementWarnings = element({ hidden: true });
  const evaluatePlacementButton = element();
  const placementScope = element({ value: 'path' });
  const placementTargets = ['keep', 'path', 'operation'].map((value) => element({
    checked: value === placementTarget,
    value,
  }));
  const output = element();
  const outputLabel = element({ textContent: 'Converted OpenAPI JSON' });
  const copyButton = element();
  const downloadButton = element();
  const openClientButton = element({
    dataset: {
      openapiOpenClient: 'scalar',
      openapiClientPath: '/tools/online-clients/scalar',
    },
  });
  const customOptions = element({ open: false });
  const optionInputs = [];
  const verbInputs = [];
  const storedSpecs = sessionStorage();
  const fetchedSpec = {
    openapi: '3.1.0',
    info: {
      title: 'Fetched API',
      version: '1.0.0',
    },
    paths: {},
  };
  const parsedSpec = {
    openapi: '3.1.0',
    info: {
      title: 'File API',
      version: '1.0.0',
    },
    paths: {},
  };
  const placementCalls = {
    convert: 0,
    evaluate: 0,
  };

  const tool = {
    querySelector(selector) {
      return {
        '[data-openapi-url-form]': form,
        '[data-openapi-url]': urlInput,
        '[data-openapi-load-status]': loadStatus,
        '[data-openapi-file]': fileInput,
        '[data-openapi-profile]': profileInput,
        '[data-openapi-status]': status,
        '[data-openapi-placement-status]': placementStatus,
        '[data-openapi-placement-message]': placementMessage,
        '[data-openapi-placement-comparison]': placementComparison,
        '[data-openapi-placement-current]': placementCurrent,
        '[data-openapi-placement-output]': placementOutput,
        '[data-openapi-placement-warnings]': placementWarnings,
        '[data-openapi-evaluate-placement]': evaluatePlacementButton,
        '[data-openapi-placement-scope]': placementScope,
        '[data-openapi-placement-target]:checked': placementTargets.find((input) => input.checked),
        '[data-openapi-output]': output,
        '[data-openapi-output-label]': outputLabel,
        '[data-openapi-copy-converted]': copyButton,
        '[data-openapi-download-converted]': downloadButton,
        '[data-openapi-custom-options]': customOptions,
      }[selector] || null;
    },
    querySelectorAll(selector) {
      if (selector === '[data-openapi-open-client]') {
        return [openClientButton];
      }
      if (selector === '[data-openapi-option]') {
        return optionInputs;
      }
      if (selector === '[data-openapi-verb]') {
        return verbInputs;
      }
      if (selector === '[data-openapi-verb]:checked') {
        return verbInputs.filter((input) => input.checked);
      }
      if (selector === '[data-openapi-option], [data-openapi-verb]') {
        return optionInputs.concat(verbInputs);
      }
      if (selector === '[data-openapi-placement-scope], [data-openapi-placement-target]') {
        return [placementScope].concat(placementTargets);
      }
      if (selector === controls.allExportActionsSelector) {
        return [copyButton, downloadButton, openClientButton];
      }
      if (selector === controls.swaggerExportActionsSelector) {
        return [copyButton, downloadButton];
      }
      if (selector === controls.embeddedClientActionsSelector) {
        return [openClientButton];
      }
      if (selector === controls.placementControlSelector) {
        return [placementScope].concat(placementTargets, [evaluatePlacementButton]);
      }
      return [];
    },
  };

  class TestFileReader {
    constructor() {
      this.listeners = {};
      this.result = '';
    }

    addEventListener(name, listener) {
      this.listeners[name] = listener;
    }

    readAsText(file) {
      this.result = file.content;
      this.listeners.load();
    }
  }

  const window = {
    ApiChallengesOpenApiTesterConverter: {
      profileOptions(selectedProfile) {
        return {
          profile: selectedProfile,
          verbs: [],
        };
      },
      convert(spec) {
        return {
          spec: {
            ...spec,
            info: {
              ...spec.info,
              title: 'Converted API',
            },
          },
          summary: 'Converted API.',
        };
      },
      stringify(spec) {
        return JSON.stringify(spec, null, 2);
      },
      convertedFilename() {
        return 'converted-openapi.json';
      },
    },
    ApiChallengesOpenApiTextLoader: {
      fetchOpenApiDocument(url) {
        assert.equal(url, openApiUrl);
        if (fetchError) {
          return Promise.reject(fetchError);
        }
        return Promise.resolve({
          spec: fetchedSpec,
          text: openApiText,
        });
      },
      parseOpenApiText(text, name) {
        assert.equal(text, '{"openapi":"3.1.0"}');
        assert.equal(name, 'local-openapi.json');
        return parsedSpec;
      },
    },
    ApiChallengesOpenApiParameterPlacement: {
      evaluate() {
        placementCalls.evaluate += 1;
        return {
          placement: 'none',
          summary: 'No matching parameters: 0 shared and 0 operation-level declarations; 0 warnings.',
          warnings: [],
        };
      },
      convert(spec) {
        placementCalls.convert += 1;
        return {
          spec: placementChanges
            ? {
              ...spec,
              info: {
                ...spec.info,
                title: 'Placement Converted API',
              },
            }
            : spec,
          changed: placementChanges,
          before: {
            summary: 'Shared path-level: 1 shared and 0 operation-level declarations; 0 warnings.',
          },
          after: {
            summary: 'Operation-level: 0 shared and 1 operation-level declarations; 0 warnings.',
          },
          summary: placementChanges
            ? 'Converted toward operation-level placement.'
            : 'No parameter placement changes were needed. No URL path parameters found; there is nothing to move for this scope.',
          warnings: [],
        };
      },
    },
    ApiChallengesOpenApiToolControls: controls,
    crypto: {
      randomUUID() {
        return 'stored-original-file';
      },
    },
    location: {
      href: '',
      search: sourceSearch,
    },
    sessionStorage: storedSpecs,
  };

  const sandbox = {
    Date,
    FileReader: TestFileReader,
    Math,
    Promise,
    URLSearchParams,
    document: {
      readyState: 'complete',
      addEventListener() {},
      querySelectorAll(selector) {
        return selector === '[data-openapi-converter]' ? [tool] : [];
      },
    },
    window,
  };
  vm.runInNewContext(scriptSource, sandbox);

  return {
    copyButton,
    downloadButton,
    fileInput,
    form,
    loadStatus,
    openClientButton,
    output,
    outputLabel,
    evaluatePlacementButton,
    placementCalls,
    placementComparison,
    placementCurrent,
    placementMessage,
    placementOutput,
    placementStatus,
    placementWarnings,
    status,
    storedSpecs,
    window,
  };
}

test('converter opens original URL specs directly in embedded clients', async () => {
  const page = converterPageHarness();

  page.form.dispatch('submit', {
    preventDefault() {},
  });
  await Promise.resolve();

  assert.equal(page.copyButton.disabled, true);
  assert.equal(page.downloadButton.disabled, true);
  assert.equal(page.openClientButton.disabled, false);
  assert.match(page.status.textContent, /Open it in an embedded client/);

  page.openClientButton.dispatch('click');

  assert.equal(
    page.window.location.href,
    '/tools/online-clients/scalar?url=%2Fdocs%2Fopenapi.json',
  );
});

test('converter shows progress and success beside the URL control for an absolute URL', async () => {
  const openApiUrl = 'http://localhost:4567/api/docs/openapi.json';
  const openApiText = '{\n  "openapi": "3.1.0"\n}\n';
  const page = converterPageHarness({ openApiText, openApiUrl });

  page.form.dispatch('submit', {
    preventDefault() {},
  });

  assert.equal(page.loadStatus.hidden, false);
  assert.equal(page.loadStatus.textContent, `Loading OpenAPI from ${openApiUrl}...`);
  assert.equal(page.loadStatus.classList.contains('online-client-status-error'), false);

  await Promise.resolve();

  assert.equal(
    page.loadStatus.textContent,
    `Loaded OpenAPI from ${openApiUrl}. Original length: ${openApiText.length} bytes; lines: 4 lines.`,
  );
  assert.equal(page.loadStatus.classList.contains('online-client-status-error'), false);
  assert.equal(page.openClientButton.disabled, false);
});

test('converter shows URL loading failures beside the URL control', async () => {
  const page = converterPageHarness({
    fetchError: new Error('Could not load /docs/openapi.json. The server returned HTTP 404.'),
  });

  page.form.dispatch('submit', {
    preventDefault() {},
  });
  await Promise.resolve();
  await Promise.resolve();

  assert.equal(page.loadStatus.hidden, false);
  assert.match(page.loadStatus.textContent, /server returned HTTP 404/);
  assert.equal(page.loadStatus.classList.contains('online-client-status-error'), true);
  assert.equal(page.status.classList.contains('online-client-status-error'), true);
  assert.equal(page.openClientButton.disabled, true);
});

test('converter opens original local file specs through browser-session handoff', () => {
  const page = converterPageHarness();

  page.fileInput.files = [
    {
      name: 'local-openapi.json',
      content: '{"openapi":"3.1.0"}',
    },
  ];
  page.fileInput.dispatch('change');

  assert.equal(page.copyButton.disabled, true);
  assert.equal(page.downloadButton.disabled, true);
  assert.equal(page.openClientButton.disabled, false);
  assert.equal(
    page.loadStatus.textContent,
    'Loaded OpenAPI from local-openapi.json. Original length: 19 bytes; lines: 1 lines.',
  );

  page.openClientButton.dispatch('click');

  const storageKey = 'apiChallengesConvertedOpenApiSpec:stored-original-file';
  assert.equal(
    page.window.location.href,
    `/tools/online-clients/scalar?converted=${encodeURIComponent(storageKey)}`,
  );
  const storedPayload = JSON.parse(page.storedSpecs.getItem(storageKey));
  assert.equal(Number.isInteger(storedPayload.createdAt), true);
  assert.deepEqual(storedPayload, {
    createdAt: storedPayload.createdAt,
    name: 'local-openapi.json',
    spec: {
      openapi: '3.1.0',
      info: {
        title: 'File API',
        version: '1.0.0',
      },
      paths: {},
    },
  });
});

test('converter automatically evaluates placement and supports an explicit rerun', async () => {
  const page = converterPageHarness();

  page.form.dispatch('submit', {
    preventDefault() {},
  });
  await Promise.resolve();

  assert.equal(page.placementCalls.evaluate, 1);
  assert.match(page.placementMessage.textContent, /No matching parameters/);
  assert.equal(page.placementComparison.hidden, true);
  assert.equal(page.placementWarnings.hidden, true);

  page.evaluatePlacementButton.dispatch('click');
  assert.equal(page.placementCalls.evaluate, 2);
  assert.match(page.placementMessage.textContent, /Evaluation complete/);
});

test('converter exports a placement-only conversion', async () => {
  const page = converterPageHarness({ placementTarget: 'operation' });

  page.form.dispatch('submit', {
    preventDefault() {},
  });
  await Promise.resolve();

  assert.equal(page.placementCalls.convert, 1);
  assert.equal(page.copyButton.disabled, false);
  assert.equal(page.downloadButton.disabled, false);
  assert.match(page.output.value, /Placement Converted API/);
  assert.equal(page.placementComparison.hidden, false);
  assert.equal(
    page.placementCurrent.textContent,
    'Current: Shared path-level: 1 shared and 0 operation-level declarations; 0 warnings.',
  );
  assert.equal(
    page.placementOutput.textContent,
    'Output: Operation-level: 0 shared and 1 operation-level declarations; 0 warnings.',
  );
  const outputLines = page.output.value.split(/\r\n|\r|\n/).length;
  assert.equal(
    page.outputLabel.textContent,
    `Converted OpenAPI JSON - length: ${page.output.value.length} bytes (+${page.output.value.length - 20} diff), lines: ${outputLines} lines (+${outputLines - 2} diff)`,
  );
  assert.match(page.status.textContent, /Converted toward operation-level placement/);
});

test('converter explains and exports a placement conversion with no matching parameters', async () => {
  const page = converterPageHarness({
    placementChanges: false,
    placementTarget: 'operation',
  });

  page.form.dispatch('submit', {
    preventDefault() {},
  });
  await Promise.resolve();

  assert.equal(page.placementCalls.convert, 1);
  assert.equal(page.copyButton.disabled, false);
  assert.equal(page.downloadButton.disabled, false);
  assert.match(page.output.value, /Fetched API/);
  assert.match(page.placementMessage.textContent, /nothing to move for this scope/);
  assert.equal(page.placementComparison.hidden, true);
  assert.match(page.status.textContent, /No parameter placement changes were needed/);
});
