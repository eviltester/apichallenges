const assert = require('node:assert/strict');
const test = require('node:test');
const client = require('../../main/resources/public/js/online-openapi-ui-client.js');

test('supportedClientTypes includes every hosted OpenAPI UI page', () => {
  assert.deepEqual(client.supportedClientTypes(), [
    'openapi-explorer',
    'scalar',
    'stoplight',
    'zudoku',
    'redoc',
  ]);
});

test('clientLabel returns readable labels for page status messages', () => {
  assert.equal(client.clientLabel('openapi-explorer'), 'OpenAPI Explorer');
  assert.equal(client.clientLabel('scalar'), 'Scalar');
  assert.equal(client.clientLabel('stoplight'), 'Stoplight Elements');
  assert.equal(client.clientLabel('zudoku'), 'Zudoku');
  assert.equal(client.clientLabel('redoc'), 'Redoc');
  assert.equal(client.clientLabel('unknown'), 'OpenAPI UI');
});

test('readConvertedSpecPayload reads converter handoff data from browser session storage', () => {
  const originalSessionStorage = global.sessionStorage;
  const spec = {
    openapi: '3.1.0',
    info: { title: 'Stored API', version: '1.0.0' },
    paths: {},
  };
  const key = 'apiChallengesConvertedOpenApiSpec:test-key';
  const storage = new Map([
    [key, JSON.stringify({ name: 'stored-api.json', spec })],
  ]);

  global.sessionStorage = {
    getItem: (storageKey) => storage.get(storageKey) || null,
  };

  try {
    assert.deepEqual(client.readConvertedSpecPayload(key), {
      name: 'stored-api.json',
      spec,
    });
  } finally {
    global.sessionStorage = originalSessionStorage;
  }
});

test('readConvertedSpecPayload rejects missing or invalid converter handoff keys', () => {
  const originalSessionStorage = global.sessionStorage;
  global.sessionStorage = {
    getItem: () => null,
  };

  try {
    assert.throws(
      () => client.readConvertedSpecPayload('not-a-converter-key'),
      /OpenAPI file reference is not valid/,
    );
    assert.throws(
      () => client.readConvertedSpecPayload('apiChallengesConvertedOpenApiSpec:missing'),
      /No OpenAPI file was found/,
    );
  } finally {
    global.sessionStorage = originalSessionStorage;
  }
});

test('zudokuEmbedUrl encodes the OpenAPI URL for the same-origin iframe host page', () => {
  const url = client.zudokuEmbedUrl('blob:http://localhost:4568/79fc0bbe-f23b-49c6-9dfa-6d3d623da729');

  assert.equal(
    url,
    '/zudoku-embed/~endpoints?apiUrl=blob%3Ahttp%3A%2F%2Flocalhost%3A4568%2F79fc0bbe-f23b-49c6-9dfa-6d3d623da729',
  );
});

test('zudokuSessionEmbedUrl can pass the active site theme to the iframe host page', () => {
  const url = client.zudokuSessionEmbedUrl('api-challenges-zudoku-spec-1', 'dark-lab');

  assert.equal(
    url,
    '/zudoku-embed/~endpoints?sessionKey=api-challenges-zudoku-spec-1&theme=dark-lab',
  );
});

test('renderZudoku stores parsed OpenAPI JSON and points the iframe at the session handoff route', () => {
  const originalDocument = global.document;
  const originalSessionStorage = global.sessionStorage;
  const originalDateNow = Date.now;
  const originalMathRandom = Math.random;
  const spec = {
    openapi: '3.1.0',
    info: { title: 'Zudoku API', version: '1.0.0' },
    paths: {},
  };
  const storage = new Map([
    ['api-challenges-zudoku-spec-old', '{"old":true}'],
    ['unrelated', 'keep'],
  ]);
  let iframe;
  let appendedElement;

  Date.now = () => 1786359000000;
  Math.random = () => 0.25;
  global.sessionStorage = {
    get length() {
      return storage.size;
    },
    key: (index) => Array.from(storage.keys())[index] || null,
    getItem: (key) => storage.get(key) || null,
    setItem: (key, value) => {
      storage.set(key, value);
    },
    removeItem: (key) => {
      storage.delete(key);
    },
  };
  global.document = {
    createElement: (name) => {
      assert.equal(name, 'iframe');
      iframe = {};
      return iframe;
    },
  };

  try {
    client.renderZudoku({
      appendChild: (element) => {
        appendedElement = element;
      },
    }, spec);

    const expectedKey = 'api-challenges-zudoku-spec-1786359000000-9';
    assert.equal(iframe.className, 'online-openapi-ui-frame');
    assert.equal(iframe.title, 'Zudoku OpenAPI UI');
    assert.equal(iframe.src, `/zudoku-embed/~endpoints?sessionKey=${expectedKey}`);
    assert.equal(storage.has('api-challenges-zudoku-spec-old'), false);
    assert.equal(storage.get('unrelated'), 'keep');
    assert.deepEqual(JSON.parse(storage.get(expectedKey)), spec);
    assert.equal(appendedElement, iframe);
  } finally {
    Date.now = originalDateNow;
    Math.random = originalMathRandom;
    global.document = originalDocument;
    global.sessionStorage = originalSessionStorage;
  }
});

test('renderOpenApiExplorer loads parsed specs instead of asking Explorer to fetch blob URLs', async () => {
  const originalCustomElements = global.customElements;
  const originalDocument = global.document;
  const spec = {
    openapi: '3.0.0',
    info: { title: 'Local file API', version: '1.0.0' },
    paths: {},
  };
  const attributes = {};
  let loadedSpec;
  let appendedElement;

  global.customElements = {
    get: (name) => name === 'openapi-explorer',
  };
  global.document = {
    createElement: (name) => {
      assert.equal(name, 'openapi-explorer');
      return {
        setAttribute: (key, value) => {
          attributes[key] = value;
        },
        loadSpec: async (openApiSpec) => {
          loadedSpec = openApiSpec;
        },
      };
    },
  };

  try {
    await client.renderOpenApiExplorer({
      appendChild: (element) => {
        appendedElement = element;
      },
    }, spec, 'blob:http://localhost:4568/local-spec');

    assert.equal(loadedSpec, spec);
    assert.equal(attributes['spec-url'], undefined);
    assert.equal(attributes['hide-authentication'], 'true');
    assert.equal(attributes.collapse, 'true');
    assert.ok(appendedElement);
  } finally {
    global.customElements = originalCustomElements;
    global.document = originalDocument;
  }
});

test('renderOpenApiExplorer injects a theme bridge into Explorer shadow DOM', async () => {
  const originalCustomElements = global.customElements;
  const originalDocument = global.document;
  const spec = {
    openapi: '3.0.0',
    info: { title: 'Themed API', version: '1.0.0' },
    paths: {},
  };
  let injectedStyle;

  global.customElements = {
    get: (name) => name === 'openapi-explorer',
  };
  global.document = {
    createElement: (name) => {
      if (name === 'style') {
        return {};
      }

      assert.equal(name, 'openapi-explorer');
      return {
        shadowRoot: {
          querySelector: (selector) => {
            assert.equal(selector, '#api-challenges-openapi-explorer-theme');
            return injectedStyle ? injectedStyle : null;
          },
          appendChild: (style) => {
            injectedStyle = style;
          },
        },
        setAttribute: () => {},
        loadSpec: async () => {},
      };
    },
  };

  try {
    await client.renderOpenApiExplorer({
      appendChild: () => {},
    }, spec);

    assert.equal(injectedStyle.id, 'api-challenges-openapi-explorer-theme');
    assert.match(injectedStyle.textContent, /--api-challenges-openapi-surface/);
    assert.match(injectedStyle.textContent, /--api-challenges-openapi-nav-text/);
    assert.match(injectedStyle.textContent, /nav-bar-path/);
    assert.match(injectedStyle.textContent, /main-content/);
  } finally {
    global.customElements = originalCustomElements;
    global.document = originalDocument;
  }
});

test('renderScalar passes the current site theme state to Scalar', () => {
  const originalDocument = global.document;
  const originalScalar = global.Scalar;
  const spec = {
    openapi: '3.1.0',
    info: { title: 'Scalar API', version: '1.0.0' },
    paths: {},
  };
  let capturedOptions;

  global.document = {
    documentElement: {
      getAttribute: (name) => (name === 'data-theme' ? 'dark-lab' : null),
    },
  };
  global.Scalar = {
    createApiReference: (target, options) => {
      capturedOptions = options;
      return true;
    },
  };

  try {
    const target = { dataset: {} };
    client.renderScalar(target, spec);

    assert.equal(target.dataset.scalarMounted, 'true');
    assert.equal(capturedOptions.content, spec);
    assert.equal(capturedOptions.darkMode, true);
    assert.equal(capturedOptions.forceDarkModeState, 'dark');
    assert.equal(capturedOptions.hideDarkModeToggle, true);
    assert.equal(capturedOptions.showDeveloperTools, 'never');
  } finally {
    global.document = originalDocument;
    global.Scalar = originalScalar;
  }
});

test('renderStoplight passes OpenAPI content as JSON text after appending the element', () => {
  const originalCustomElements = global.customElements;
  const originalDocument = global.document;
  const originalMatchMedia = global.matchMedia;
  const spec = {
    openapi: '3.1.0',
    info: { title: 'Stoplight API', version: '1.0.0' },
    paths: {},
  };
  const attributes = {};
  let appended = false;
  let documentWasAssignedAfterAppend = false;
  let assignedDocument;
  let credentialsPolicy;
  let responsiveListenerRegistered = false;

  global.customElements = {
    get: (name) => name === 'elements-api',
  };
  global.matchMedia = (query) => {
    assert.equal(query, '(max-width: 700px)');
    return {
      matches: false,
      addEventListener: (event) => {
        assert.equal(event, 'change');
        responsiveListenerRegistered = true;
      },
    };
  };
  global.document = {
    createElement: (name) => {
      assert.equal(name, 'elements-api');
      return {
        setAttribute: (key, value) => {
          attributes[key] = value;
        },
        set tryItCredentialsPolicy(value) {
          credentialsPolicy = value;
        },
        set apiDescriptionDocument(value) {
          documentWasAssignedAfterAppend = appended;
          assignedDocument = value;
        },
      };
    },
  };

  try {
    client.renderStoplight({
      appendChild: () => {
        appended = true;
      },
    }, spec);

    assert.equal(attributes.router, 'hash');
    assert.equal(attributes.layout, 'sidebar');
    assert.equal(responsiveListenerRegistered, true);
    assert.equal(credentialsPolicy, 'same-origin');
    assert.equal(documentWasAssignedAfterAppend, true);
    assert.equal(typeof assignedDocument, 'string');
    assert.deepEqual(JSON.parse(assignedDocument), spec);
  } finally {
    global.customElements = originalCustomElements;
    global.document = originalDocument;
    global.matchMedia = originalMatchMedia;
  }
});

test('renderRedoc passes parseable site colors through the Redoc theme option', async () => {
  const originalRedoc = global.Redoc;
  const originalDocument = global.document;
  const spec = {
    openapi: '3.1.0',
    info: { title: 'Redoc API', version: '1.0.0' },
    paths: {},
  };
  const target = {
    children: [{}],
  };
  let capturedSpec;
  let capturedOptions;
  let capturedTarget;

  global.document = {
    documentElement: {
      getAttribute: (name) => (name === 'data-theme' ? 'dark-lab' : null),
    },
  };
  global.Redoc = {
    init: (openApiSpec, options, renderTarget, done) => {
      capturedSpec = openApiSpec;
      capturedOptions = options;
      capturedTarget = renderTarget;
      done();
    },
  };

  try {
    await client.renderRedoc(target, spec);

    assert.equal(capturedSpec, spec);
    assert.equal(capturedTarget, target);
    assert.equal(capturedOptions.theme.colors.primary.main, '#22d3c5');
    assert.equal(capturedOptions.theme.colors.text.primary, '#e7fbff');
    assert.equal(capturedOptions.theme.colors.responses.error.color, '#fca5a5');
    assert.equal(capturedOptions.theme.schema.requireLabelColor, '#fca5a5');
    assert.equal(capturedOptions.theme.sidebar.backgroundColor, '#102429');
    assert.equal(capturedOptions.theme.rightPanel.backgroundColor, '#17343a');
  } finally {
    global.Redoc = originalRedoc;
    global.document = originalDocument;
  }
});

test('renderStoplight uses stacked layout for phone-width viewports', () => {
  const originalCustomElements = global.customElements;
  const originalDocument = global.document;
  const originalMatchMedia = global.matchMedia;
  const attributes = {};

  global.customElements = {
    get: (name) => name === 'elements-api',
  };
  global.matchMedia = () => ({ matches: true });
  global.document = {
    createElement: (name) => {
      assert.equal(name, 'elements-api');
      return {
        setAttribute: (key, value) => {
          attributes[key] = value;
        },
        set tryItCredentialsPolicy(value) {},
        set apiDescriptionDocument(value) {},
      };
    },
  };

  try {
    client.renderStoplight({
      appendChild: () => {},
    }, {
      openapi: '3.1.0',
      info: { title: 'Stoplight API', version: '1.0.0' },
      paths: {},
    });

    assert.equal(attributes.layout, 'stacked');
  } finally {
    global.customElements = originalCustomElements;
    global.document = originalDocument;
    global.matchMedia = originalMatchMedia;
  }
});
