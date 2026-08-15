(function (root, factory) {
  'use strict';

  const api = factory(root);

  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  }

  if (root) {
    root.ApiChallengesOnlineOpenApiUiClient = api;
  }
}(typeof globalThis !== 'undefined' ? globalThis : this, function (root) {
  'use strict';

  const DEFAULT_OPENAPI_URL = '/api/docs/openapi.json';
  const CONVERTED_SESSION_KEY_PREFIX = 'apiChallengesConvertedOpenApiSpec:';
  const OPENAPI_EXPLORER_THEME_STYLE_ID = 'api-challenges-openapi-explorer-theme';
  const OPENAPI_EXPLORER_NESTED_THEME_STYLE_ID = 'api-challenges-openapi-explorer-nested-theme';
  const CLIENT_LABELS = {
    'openapi-explorer': 'OpenAPI Explorer',
    scalar: 'Scalar',
    stoplight: 'Stoplight Elements',
    zudoku: 'Zudoku',
    redoc: 'Redoc',
  };
  const ZUDOKU_SESSION_KEY_PREFIX = 'api-challenges-zudoku-spec-';
  const SITE_THEME_PALETTES = {
    'clean-docs': {
      surface: '#ffffff',
      surfaceSoft: '#eef7f6',
      surfaceStrong: '#e3eef8',
      text: '#102033',
      muted: '#526272',
      border: '#d5e0ea',
      accent: '#0f6795',
      accentStrong: '#0b3f73',
      accentContrast: '#ffffff',
      secondaryAccent: '#12a594',
      codeBg: '#0e1726',
      codeText: '#e6f2ff',
    },
    'learning-platform': {
      surface: '#fffdf8',
      surfaceSoft: '#fff1d6',
      surfaceStrong: '#ffe4ba',
      text: '#172033',
      muted: '#665b4b',
      border: '#ead8bd',
      accent: '#ea580c',
      accentStrong: '#c2410c',
      accentContrast: '#ffffff',
      secondaryAccent: '#0f766e',
      codeBg: '#13213a',
      codeText: '#f7fbff',
    },
    'dark-lab': {
      surface: '#0b181c',
      surfaceSoft: '#102429',
      surfaceStrong: '#17343a',
      text: '#e7fbff',
      muted: '#9fb8bd',
      border: '#21444b',
      accent: '#22d3c5',
      accentStrong: '#2dd4bf',
      accentContrast: '#021213',
      secondaryAccent: '#38bdf8',
      codeBg: '#03090b',
      codeText: '#d9fff8',
    },
  };

  function onReady(callback) {
    if (!root || !root.document) {
      return;
    }

    if (root.document.readyState === 'loading') {
      root.document.addEventListener('DOMContentLoaded', callback);
      return;
    }
    callback();
  }

  function textLoaderApi() {
    return root && root.ApiChallengesOpenApiTextLoader;
  }

  function controlsApi() {
    return root && root.ApiChallengesOpenApiToolControls;
  }

  function readConvertedSpecPayload(storageKey) {
    if (!root.sessionStorage) {
      throw new Error('Browser session storage is required to open this OpenAPI file.');
    }

    if (!storageKey || !storageKey.startsWith(CONVERTED_SESSION_KEY_PREFIX)) {
      throw new Error('The OpenAPI file reference is not valid.');
    }

    const storedPayload = root.sessionStorage.getItem(storageKey);
    if (!storedPayload) {
      throw new Error('No OpenAPI file was found in this browser session.');
    }

    const payload = JSON.parse(storedPayload);
    const spec = typeof payload.spec === 'string' ? JSON.parse(payload.spec) : payload.spec;
    if (!spec || typeof spec !== 'object') {
      throw new Error('The OpenAPI file in this browser session could not be read.');
    }

    return {
      name: payload.name || 'converted-openapi.json',
      spec: spec,
    };
  }

  function setStatus(statusElement, message, isError) {
    const controls = controlsApi();
    if (controls) {
      controls.setStatus(statusElement, message, isError);
      return;
    }

    statusElement.textContent = message;
    statusElement.classList.toggle('online-client-status-error', isError === true);
  }

  function clientLabel(clientType) {
    return CLIENT_LABELS[clientType] || 'OpenAPI UI';
  }

  function supportedClientTypes() {
    return Object.keys(CLIENT_LABELS);
  }

  function clearTarget(target) {
    target.innerHTML = '';
  }

  function createSpecObjectUrl(spec) {
    const blob = new root.Blob([JSON.stringify(spec)], { type: 'application/json' });
    return root.URL.createObjectURL(blob);
  }

  function revokeSpecObjectUrl(specUrl) {
    if (specUrl && root && root.URL && typeof root.URL.revokeObjectURL === 'function') {
      root.URL.revokeObjectURL(specUrl);
    }
  }

  function escapeHtmlAttribute(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function currentThemeName() {
    if (root && root.document && root.document.documentElement) {
      return root.document.documentElement.getAttribute('data-theme') || '';
    }
    return '';
  }

  function siteThemePalette(themeName) {
    return SITE_THEME_PALETTES[themeName] || SITE_THEME_PALETTES['clean-docs'];
  }

  function appendThemeParameter(url, themeName) {
    if (!themeName) {
      return url;
    }
    return `${url}&theme=${encodeURIComponent(themeName)}`;
  }

  function zudokuEmbedUrl(specUrl, themeName) {
    return appendThemeParameter(
      `/zudoku-embed/~endpoints?apiUrl=${encodeURIComponent(specUrl)}`,
      themeName,
    );
  }

  function zudokuSessionEmbedUrl(storageKey, themeName) {
    return appendThemeParameter(
      `/zudoku-embed/~endpoints?sessionKey=${encodeURIComponent(storageKey)}`,
      themeName,
    );
  }

  function storeZudokuSpec(spec) {
    if (!root.sessionStorage) {
      throw new Error('Zudoku needs browser session storage to render this OpenAPI file.');
    }

    for (let index = root.sessionStorage.length - 1; index >= 0; index--) {
      const storageKey = root.sessionStorage.key(index);
      if (storageKey && storageKey.startsWith(ZUDOKU_SESSION_KEY_PREFIX)) {
        root.sessionStorage.removeItem(storageKey);
      }
    }

    const storageKey = `${ZUDOKU_SESSION_KEY_PREFIX}${Date.now()}-${Math.random().toString(36).slice(2)}`;
    root.sessionStorage.setItem(storageKey, JSON.stringify(spec));
    return storageKey;
  }

  function openApiExplorerThemeStyles() {
    return `
      :host {
        background: var(--api-challenges-openapi-surface, var(--white)) !important;
        color: var(--api-challenges-openapi-text, var(--gray)) !important;
        color-scheme: var(--api-challenges-openapi-color-scheme, light);
      }

      #the-main-body,
      .main-content,
      .main-content-inner,
      .api-content,
      .operation-content,
      .operation-details,
      .response-area,
      .schema,
      .m-markdown,
      .markdown-body {
        background: var(--api-challenges-openapi-surface, var(--white)) !important;
        color: inherit !important;
      }

      #operations-root,
      .operations-root,
      #overview,
      .section-padding,
      #api-title,
      .regular-font,
      .focused-mode-content {
        background: var(--api-challenges-openapi-surface, var(--white)) !important;
        color: var(--api-challenges-openapi-text, var(--gray)) !important;
      }

      .m-markdown,
      .m-markdown p,
      .m-markdown li,
      #operations-root,
      #operations-root *,
      .operations-root,
      .operations-root *,
      .operation-details,
      .operation-details p,
      .schema,
      .schema p,
      .schema span,
      .markdown-body,
      .markdown-body p,
      .markdown-body li {
        color: inherit !important;
      }

      button,
      input,
      select,
      textarea,
      .m-btn,
      .m-btn.outline-primary {
        background: var(--api-challenges-openapi-surface-soft, var(--white)) !important;
        border-color: var(--api-challenges-openapi-border, var(--gray)) !important;
        color: var(--api-challenges-openapi-text, var(--gray)) !important;
      }

      a,
      .operation-name,
      .nav-scroll a {
        color: var(--api-challenges-openapi-link, var(--blue)) !important;
      }

      pre,
      code,
      textarea,
      .font-mono,
      .code,
      .code-block,
      .hljs,
      .request-body,
      .response-body,
      .response-headers,
      .sample-code {
        background: var(--api-challenges-openapi-code-bg, #0e1726) !important;
        border-color: var(--api-challenges-openapi-border, var(--gray)) !important;
        color: var(--api-challenges-openapi-code-text, #e6f2ff) !important;
        font-family: var(--mono-font, ui-monospace, SFMono-Regular, Consolas, monospace) !important;
        text-shadow: none !important;
      }

      pre *,
      code *,
      textarea *,
      .font-mono *,
      .code *,
      .code-block *,
      .hljs *,
      .request-body *,
      .response-body *,
      .response-headers *,
      .sample-code * {
        background: transparent !important;
        color: var(--api-challenges-openapi-code-text, #e6f2ff) !important;
        text-shadow: none !important;
      }

      .nav-bar,
      .nav-scroll,
      .nav-bar-info,
      .nav-bar-section,
      .nav-bar-section-wrapper,
      .nav-bar-tag-and-paths,
      .nav-bar-path {
        background: var(--api-challenges-openapi-nav-bg, #21445f) !important;
        color: var(--api-challenges-openapi-nav-text, #ffffff) !important;
      }

      .nav-bar *,
      .nav-scroll *,
      .nav-bar-path *,
      .nav-bar-section-title {
        color: var(--api-challenges-openapi-nav-text, #ffffff) !important;
        opacity: 1 !important;
      }

      .nav-bar-path:hover,
      .nav-bar-path:focus-within,
      .nav-bar-path.active {
        background: var(--api-challenges-openapi-nav-active-bg, rgba(255, 255, 255, 0.16)) !important;
      }
    `;
  }

  function openApiExplorerNestedThemeStyles() {
    return `
      :host {
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
        color-scheme: var(--api-challenges-openapi-color-scheme, light);
      }

      :host([data-api-challenges-openapi-code]),
      :host([data-api-challenges-openapi-code]) .fs-exclude,
      .fs-exclude,
      pre,
      code {
        background: var(--api-challenges-openapi-code-bg, #0e1726) !important;
        border-color: var(--api-challenges-openapi-border, #d5e0ea) !important;
        color: var(--api-challenges-openapi-code-text, #e6f2ff) !important;
        font-family: var(--mono-font, ui-monospace, SFMono-Regular, Consolas, monospace) !important;
        text-shadow: none !important;
      }

      pre *,
      code * {
        background: transparent !important;
        text-shadow: none !important;
      }

      .token,
      .token.header,
      .token.header-value {
        color: var(--api-challenges-openapi-code-text, #e6f2ff) !important;
      }

      .token.string {
        color: var(--api-challenges-openapi-code-token-string, #7ee2b8) !important;
      }

      .token.property {
        color: var(--api-challenges-openapi-code-token-property, #f0abfc) !important;
      }

      .token.keyword,
      .token.header-name.keyword {
        color: var(--api-challenges-openapi-code-token-keyword, #93c5fd) !important;
      }

      .token.boolean,
      .token.function,
      .token.number {
        color: var(--api-challenges-openapi-code-token-value, #fda4af) !important;
      }

      .token.operator,
      .token.punctuation {
        color: var(--api-challenges-openapi-code-token-operator, #cbd5e1) !important;
      }

      button,
      input,
      select,
      textarea,
      .m-btn,
      .tab-btn {
        background: var(--api-challenges-openapi-surface-soft, var(--white, #ffffff)) !important;
        border-color: var(--api-challenges-openapi-border, var(--gray, #d5e0ea)) !important;
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
        text-shadow: none !important;
      }

      button:hover,
      button:focus-visible,
      .m-btn:hover,
      .m-btn:focus-visible,
      .tab-btn:hover,
      .tab-btn:focus-visible,
      .tab-btn.active,
      .m-btn.active,
      [aria-selected="true"] {
        background: var(
          --api-challenges-openapi-surface-strong,
          var(--api-challenges-openapi-surface-soft, var(--white, #ffffff))
        ) !important;
        border-color: var(--api-challenges-openapi-link, var(--blue, #0f6795)) !important;
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
      }

      table,
      thead,
      tbody,
      tr,
      th,
      td,
      .table,
      .m-table {
        background: var(--api-challenges-openapi-surface, var(--white, #ffffff)) !important;
        border-color: var(--api-challenges-openapi-border, var(--gray, #d5e0ea)) !important;
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
      }

      .table > div:first-child,
      .table > div:first-child * {
        background: var(--api-challenges-openapi-surface-soft, var(--white, #ffffff)) !important;
        border-color: var(--api-challenges-openapi-border, var(--gray, #d5e0ea)) !important;
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
      }

      .key-label,
      .requiredStar,
      .param-description,
      p,
      li {
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
      }

      .string:not(.token) {
        color: var(--api-challenges-openapi-schema-string, #087443) !important;
      }

      .boolean:not(.token),
      .integer:not(.token),
      .number:not(.token) {
        color: var(--api-challenges-openapi-schema-value, #b42318) !important;
      }

      .toolbar-copy-btn {
        background: var(--api-challenges-openapi-surface-soft, var(--white, #ffffff)) !important;
        border-color: var(--api-challenges-openapi-border, var(--gray, #d5e0ea)) !important;
        color: var(--api-challenges-openapi-text, var(--gray, #102033)) !important;
      }
    `;
  }

  function upsertShadowStyle(shadowRoot, styleId, styleText) {
    if (!shadowRoot || !root.document) {
      return null;
    }

    let style = shadowRoot.querySelector(`#${styleId}`);
    if (!style) {
      style = root.document.createElement('style');
      style.id = styleId;
      shadowRoot.appendChild(style);
    }
    style.textContent = styleText;
    return style;
  }

  function markOpenApiExplorerShadowHost(host) {
    if (!host || typeof host.setAttribute !== 'function') {
      return;
    }

    const tagName = String(host.tagName || '').toLowerCase();
    if (tagName === 'syntax-highlighter') {
      host.setAttribute('data-api-challenges-openapi-code', 'true');
    }
  }

  function collectOpenApiExplorerShadowHosts(node, hosts) {
    if (!node || typeof node.querySelectorAll !== 'function') {
      return hosts;
    }

    Array.prototype.forEach.call(node.querySelectorAll('*'), function (element) {
      if (element.shadowRoot) {
        hosts.push(element);
        collectOpenApiExplorerShadowHosts(element.shadowRoot, hosts);
      }
    });
    return hosts;
  }

  function applyOpenApiExplorerNestedTheme(explorer) {
    if (!explorer || !explorer.shadowRoot) {
      return;
    }

    const nestedThemeStyles = openApiExplorerNestedThemeStyles();
    collectOpenApiExplorerShadowHosts(explorer.shadowRoot, []).forEach(function (host) {
      markOpenApiExplorerShadowHost(host);
      upsertShadowStyle(host.shadowRoot, OPENAPI_EXPLORER_NESTED_THEME_STYLE_ID, nestedThemeStyles);
    });
  }

  function setupOpenApiExplorerThemeObserver(explorer) {
    if (!root.MutationObserver || !explorer || !explorer.shadowRoot || !explorer.dataset) {
      return;
    }
    if (explorer.dataset.apiChallengesOpenApiExplorerThemeObserver === 'true') {
      return;
    }

    const observer = new root.MutationObserver(function () {
      applyOpenApiExplorerNestedTheme(explorer);
    });
    observer.observe(explorer.shadowRoot, {
      childList: true,
      subtree: true,
    });
    explorer.dataset.apiChallengesOpenApiExplorerThemeObserver = 'true';
  }

  function applyOpenApiExplorerTheme(explorer) {
    if (!explorer || !explorer.shadowRoot || !root.document) {
      return;
    }

    upsertShadowStyle(
      explorer.shadowRoot,
      OPENAPI_EXPLORER_THEME_STYLE_ID,
      openApiExplorerThemeStyles(),
    );
    applyOpenApiExplorerNestedTheme(explorer);
    setupOpenApiExplorerThemeObserver(explorer);
  }

  function scheduleOpenApiExplorerTheme(explorer) {
    applyOpenApiExplorerTheme(explorer);

    if (root && typeof root.requestAnimationFrame === 'function') {
      root.requestAnimationFrame(function () {
        applyOpenApiExplorerTheme(explorer);
      });
    }

    if (root && typeof root.setTimeout === 'function') {
      root.setTimeout(function () {
        applyOpenApiExplorerTheme(explorer);
      }, 250);
    }
  }

  function renderOpenApiExplorer(target, spec) {
    if (!root.customElements || !root.customElements.get('openapi-explorer')) {
      throw new Error('OpenAPI Explorer could not be loaded. Check your network connection.');
    }

    const explorer = root.document.createElement('openapi-explorer');
    explorer.setAttribute('collapse', 'true');
    target.appendChild(explorer);

    if (typeof explorer.loadSpec !== 'function') {
      throw new Error('OpenAPI Explorer could not load parsed OpenAPI content.');
    }

    const loadResult = explorer.loadSpec(spec);
    if (loadResult && typeof loadResult.then === 'function') {
      return loadResult.then(function () {
        scheduleOpenApiExplorerTheme(explorer);
      });
    }

    scheduleOpenApiExplorerTheme(explorer);
    return loadResult;
  }

  function renderScalar(target, spec) {
    if (!root.Scalar || typeof root.Scalar.createApiReference !== 'function') {
      throw new Error('Scalar could not be loaded. Check your network connection.');
    }

    const darkMode = currentThemeName() === 'dark-lab';
    const mounted = root.Scalar.createApiReference(target, {
      content: spec,
      darkMode: darkMode,
      forceDarkModeState: darkMode ? 'dark' : 'light',
      defaultHttpClient: {
        targetKey: 'shell',
        clientKey: 'curl',
      },
      agent: {
        disabled: true,
      },
      hideDarkModeToggle: true,
      showDeveloperTools: 'never',
    });
    target.dataset.scalarMounted = mounted ? 'true' : 'false';
  }

  function renderStoplight(target, spec) {
    if (!root.customElements || !root.customElements.get('elements-api')) {
      throw new Error('Stoplight Elements could not be loaded. Check your network connection.');
    }

    const elementsApi = root.document.createElement('elements-api');
    elementsApi.setAttribute('router', 'hash');
    const mobileLayout = typeof root.matchMedia === 'function'
      ? root.matchMedia('(max-width: 700px)')
      : { matches: false };
    const applyLayout = () => {
      elementsApi.setAttribute('layout', mobileLayout.matches ? 'stacked' : 'sidebar');
    };
    applyLayout();
    if (typeof mobileLayout.addEventListener === 'function') {
      mobileLayout.addEventListener('change', applyLayout);
    } else if (typeof mobileLayout.addListener === 'function') {
      mobileLayout.addListener(applyLayout);
    }
    elementsApi.tryItCredentialsPolicy = 'same-origin';
    target.appendChild(elementsApi);
    elementsApi.apiDescriptionDocument = JSON.stringify(spec, null, 2);
  }

  function renderZudoku(target, spec) {
    const iframe = root.document.createElement('iframe');
    const storageKey = storeZudokuSpec(spec);
    const setFrameThemeName = function (themeName) {
      if (typeof iframe.setAttribute === 'function') {
        iframe.setAttribute('data-theme', themeName || '');
        return;
      }
      iframe.themeName = themeName || '';
    };
    const frameThemeName = function () {
      if (typeof iframe.getAttribute === 'function') {
        return iframe.getAttribute('data-theme') || '';
      }
      return iframe.themeName || '';
    };
    const syncFrameTheme = function () {
      const themeName = currentThemeName();
      setFrameThemeName(themeName);
      iframe.src = zudokuSessionEmbedUrl(storageKey, themeName);
    };

    iframe.className = 'online-openapi-ui-frame';
    iframe.title = 'Zudoku OpenAPI UI';
    syncFrameTheme();
    target.appendChild(iframe);

    if (root.MutationObserver && root.document && root.document.documentElement) {
      const observer = new root.MutationObserver(function () {
        const themeName = currentThemeName();
        if ((themeName || '') !== frameThemeName()) {
          syncFrameTheme();
        }
      });
      observer.observe(root.document.documentElement, {
        attributes: true,
        attributeFilter: ['data-theme'],
      });
    }
  }

  function redocTheme() {
    const themeName = currentThemeName();
    const palette = siteThemePalette(themeName);
    const dangerColor = themeName === 'dark-lab' ? '#fca5a5' : '#b42318';
    return {
      colors: {
        primary: {
          main: palette.accent,
        },
        text: {
          primary: palette.text,
          secondary: palette.muted,
        },
        border: {
          dark: palette.border,
          light: palette.border,
        },
        responses: {
          success: {
            color: '#16a34a',
          },
          error: {
            color: dangerColor,
          },
        },
        http: {
          get: '#2563eb',
          post: '#16a34a',
          put: '#ea580c',
          delete: '#dc2626',
          patch: '#0891b2',
          options: palette.secondaryAccent,
          head: palette.secondaryAccent,
        },
      },
      schema: {
        linesColor: palette.border,
        requireLabelColor: dangerColor,
      },
      sidebar: {
        backgroundColor: palette.surfaceSoft,
        textColor: palette.text,
        activeTextColor: palette.accentStrong,
      },
      rightPanel: {
        backgroundColor: palette.surfaceStrong,
        textColor: palette.text,
      },
      typography: {
        fontFamily: 'var(--body-font)',
        headings: {
          fontFamily: 'var(--heading-font)',
          fontWeight: '700',
        },
        code: {
          backgroundColor: palette.codeBg,
          color: palette.codeText,
          fontFamily: 'var(--mono-font)',
        },
      },
    };
  }

  function renderRedoc(target, spec) {
    if (!root.Redoc || typeof root.Redoc.init !== 'function') {
      throw new Error('Redoc could not be loaded. Check your network connection.');
    }

    return new root.Promise(function (resolve, reject) {
      let settled = false;
      const finish = function () {
        if (!settled) {
          settled = true;
          resolve();
        }
      };
      const fail = function (error) {
        if (!settled) {
          settled = true;
          reject(error);
        }
      };
      const pollTimer = root.setInterval(function () {
        if (target.children && target.children.length > 0) {
          root.clearInterval(pollTimer);
          root.clearTimeout(timeoutTimer);
          finish();
        }
      }, 250);
      const timeoutTimer = root.setTimeout(function () {
        root.clearInterval(pollTimer);
        if (target.children && target.children.length > 0) {
          finish();
          return;
        }
        fail(new Error('Redoc did not render this OpenAPI file.'));
      }, 10000);

      try {
        const initResult = root.Redoc.init(spec, {
          hideHostname: false,
          requiredPropsFirst: true,
          scrollYOffset: 72,
          theme: redocTheme(),
        }, target, function () {
          root.clearInterval(pollTimer);
          root.clearTimeout(timeoutTimer);
          finish();
        });

        if (initResult && typeof initResult.then === 'function') {
          initResult.then(finish).catch(fail);
        }
      } catch (error) {
        root.clearInterval(pollTimer);
        root.clearTimeout(timeoutTimer);
        fail(error);
      }
    });
  }

  const RENDERERS = {
    'openapi-explorer': renderOpenApiExplorer,
    scalar: renderScalar,
    stoplight: renderStoplight,
    zudoku: renderZudoku,
    redoc: renderRedoc,
  };

  function initOpenApiUiClient(client) {
    const loader = textLoaderApi();
    const form = client.querySelector('[data-openapi-url-form]');
    const urlInput = client.querySelector('[data-openapi-url]');
    const fileInput = client.querySelector('[data-openapi-file]');
    const status = client.querySelector('[data-openapi-status]');
    const target = client.querySelector('[data-openapi-render-target]');
    const clientType = client.dataset.openapiUi || client.dataset.client || '';
    const renderer = RENDERERS[clientType];
    const label = clientLabel(clientType);
    const defaultOpenApiUrl = client.dataset.defaultOpenapiUrl || DEFAULT_OPENAPI_URL;
    let currentSpecUrl = '';

    if (!renderer) {
      setStatus(status, 'This OpenAPI UI client is not configured.', true);
      return;
    }

    if (!loader) {
      setStatus(status, 'The OpenAPI JSON/YAML loader could not be loaded.', true);
      return;
    }

    function renderSpec(spec, sourceName) {
      revokeSpecObjectUrl(currentSpecUrl);
      currentSpecUrl = createSpecObjectUrl(spec);
      clearTarget(target);

      try {
        const renderResult = renderer(target, spec, currentSpecUrl, sourceName);
        if (renderResult && typeof renderResult.then === 'function') {
          renderResult
            .then(function () {
              setStatus(status, `Loaded ${sourceName} in ${label}.`, false);
            })
            .catch(function (error) {
              clearTarget(target);
              setStatus(status, error.message, true);
            });
          return;
        }

        setStatus(status, `Loaded ${sourceName} in ${label}.`, false);
      } catch (error) {
        clearTarget(target);
        setStatus(status, error.message, true);
      }
    }

    function renderUrl(rawUrl) {
      const openApiUrl = rawUrl.trim();
      if (!openApiUrl) {
        setStatus(status, 'Enter an OpenAPI or Swagger URL to load.', true);
        return;
      }

      setStatus(status, `Loading OpenAPI from ${openApiUrl}`, false);
      loader.fetchOpenApi(openApiUrl)
        .then(function (spec) {
          renderSpec(spec, openApiUrl);
        })
        .catch(function (error) {
          clearTarget(target);
          setStatus(status, error.message, true);
        });
    }

    function renderFile(file) {
      if (!file) {
        return;
      }

      const reader = new root.FileReader();
      reader.addEventListener('load', function () {
        try {
          const spec = loader.parseOpenApiText(String(reader.result || ''), file.name);
          renderSpec(spec, file.name);
        } catch (error) {
          clearTarget(target);
          setStatus(status, error.message, true);
        }
      });
      reader.addEventListener('error', function () {
        clearTarget(target);
        setStatus(status, `Could not read ${file.name}.`, true);
      });
      reader.readAsText(file);
    }

    function renderConvertedSpec(storageKey) {
      try {
        const payload = readConvertedSpecPayload(storageKey);
        renderSpec(payload.spec, payload.name);
      } catch (error) {
        clearTarget(target);
        setStatus(status, error.message, true);
      }
    }

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      renderUrl(urlInput.value);
    });

    fileInput.addEventListener('change', function () {
      renderFile(fileInput.files && fileInput.files[0]);
    });

    root.addEventListener('beforeunload', function () {
      revokeSpecObjectUrl(currentSpecUrl);
    });

    const searchParams = new root.URLSearchParams(root.location.search);
    const convertedParameter = searchParams.get('converted');
    if (convertedParameter) {
      renderConvertedSpec(convertedParameter);
      return;
    }

    const urlParameter = searchParams.get('url');
    urlInput.value = urlParameter || defaultOpenApiUrl;
    renderUrl(urlInput.value);
  }

  onReady(function () {
    root.document.querySelectorAll('[data-online-openapi-ui-client]').forEach(initOpenApiUiClient);
  });

  return {
    clientLabel: clientLabel,
    supportedClientTypes: supportedClientTypes,
    renderOpenApiExplorer: renderOpenApiExplorer,
    renderScalar: renderScalar,
    renderStoplight: renderStoplight,
    renderZudoku: renderZudoku,
    renderRedoc: renderRedoc,
    redocTheme: redocTheme,
    readConvertedSpecPayload: readConvertedSpecPayload,
    zudokuSessionEmbedUrl: zudokuSessionEmbedUrl,
    zudokuEmbedUrl: zudokuEmbedUrl,
    escapeHtmlAttribute: escapeHtmlAttribute,
  };
}));
