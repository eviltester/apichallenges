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

function converterPageHarness({ profile = 'original', sourceSearch = '' } = {}) {
  const form = element();
  const urlInput = element({ value: '/docs/openapi.json' });
  const fileInput = element({ files: [] });
  const profileInput = element({ value: profile });
  const status = element();
  const output = element();
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

  const tool = {
    querySelector(selector) {
      return {
        '[data-openapi-url-form]': form,
        '[data-openapi-url]': urlInput,
        '[data-openapi-file]': fileInput,
        '[data-openapi-profile]': profileInput,
        '[data-openapi-status]': status,
        '[data-openapi-output]': output,
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
      if (selector === controls.allExportActionsSelector) {
        return [copyButton, downloadButton, openClientButton];
      }
      if (selector === controls.swaggerExportActionsSelector) {
        return [copyButton, downloadButton];
      }
      if (selector === controls.embeddedClientActionsSelector) {
        return [openClientButton];
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
      fetchOpenApi(url) {
        assert.equal(url, '/docs/openapi.json');
        return Promise.resolve(fetchedSpec);
      },
      parseOpenApiText(text, name) {
        assert.equal(text, '{"openapi":"3.1.0"}');
        assert.equal(name, 'local-openapi.json');
        return parsedSpec;
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
    openClientButton,
    output,
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
