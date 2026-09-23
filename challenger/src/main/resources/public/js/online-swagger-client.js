(function () {
  'use strict';

  const SWAGGER_SESSION_SPEC_KEY = 'apiChallengesConvertedOpenApiSpec';
  const SWAGGER_SESSION_NAME_KEY = 'apiChallengesConvertedOpenApiName';
  const CONVERTED_SESSION_KEY_PREFIX = 'apiChallengesConvertedOpenApiSpec:';

  function onReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback);
      return;
    }
    callback();
  }

  function hasSwaggerUi() {
    return typeof window.SwaggerUIBundle === 'function';
  }

  function textLoaderApi() {
    return window.ApiChallengesOpenApiTextLoader;
  }

  function controlsApi() {
    return window.ApiChallengesOpenApiToolControls;
  }

  function readConvertedSpecPayload(storageKey) {
    if (!window.sessionStorage) {
      throw new Error('Browser session storage is required to open this OpenAPI file.');
    }

    if (storageKey === 'session') {
      const legacySpec = window.sessionStorage.getItem(SWAGGER_SESSION_SPEC_KEY);
      const legacyName = window.sessionStorage.getItem(SWAGGER_SESSION_NAME_KEY)
        || 'converted-openapi.json';

      if (!legacySpec) {
        throw new Error('No OpenAPI file was found in this browser session.');
      }

      return {
        name: legacyName,
        spec: JSON.parse(legacySpec),
      };
    }

    if (!storageKey || !storageKey.startsWith(CONVERTED_SESSION_KEY_PREFIX)) {
      throw new Error('The OpenAPI file reference is not valid.');
    }

    const storedPayload = window.sessionStorage.getItem(storageKey);
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

  function swaggerOptions(targetSelector, source) {
    const presets = [window.SwaggerUIBundle.presets.apis];
    if (window.SwaggerUIStandalonePreset) {
      presets.push(window.SwaggerUIStandalonePreset);
    }

    return Object.assign({
      dom_id: targetSelector,
      deepLinking: true,
      displayRequestDuration: true,
      presets: presets,
      plugins: [
        window.SwaggerUIBundle.plugins.DownloadUrl,
      ],
      layout: window.SwaggerUIStandalonePreset ? 'StandaloneLayout' : 'BaseLayout',
      syntaxHighlight: { activated: false },
    }, source);
  }

  function initSwaggerClient(client) {
    const loader = textLoaderApi();
    const controls = controlsApi();
    const form = client.querySelector('[data-openapi-url-form]');
    const urlInput = client.querySelector('[data-openapi-url]');
    const fileInput = client.querySelector('[data-openapi-file]');
    const status = client.querySelector('[data-openapi-status]');
    const target = client.querySelector('[data-openapi-render-target]');
    const defaultOpenApiUrl = client.dataset.defaultOpenapiUrl || '/api/docs/openapi.json';
    const targetSelector = `#${target.id}`;
    let swaggerUi = null;

    if (!controls) {
      status.textContent = 'The OpenAPI tool controls could not be loaded.';
      status.classList.add('online-client-status-error');
      return;
    }

    if (!loader) {
      controls.setStatus(status, 'The OpenAPI JSON/YAML loader could not be loaded.', true);
      return;
    }

    function clearTarget() {
      target.innerHTML = '';
    }

    function renderSource(source, statusMessage) {
      if (!hasSwaggerUi()) {
        controls.setStatus(status, 'Swagger UI could not be loaded. Check your network connection.', true);
        return;
      }

      clearTarget();
      swaggerUi = window.SwaggerUIBundle(swaggerOptions(targetSelector, source));
      window.ApiChallengesOnlineSwagger = swaggerUi;
      controls.setStatus(status, statusMessage, false);
    }

    function loadSpec(spec, name, message) {
      renderSource({ spec: spec }, message || `Loaded ${name || 'openapi'}.`);
    }

    function renderUrl(rawUrl) {
      const openApiUrl = rawUrl.trim();
      if (!openApiUrl) {
        controls.setStatus(status, 'Enter an OpenAPI or Swagger URL to load.', true);
        return;
      }

      controls.setStatus(status, `Loading OpenAPI from ${openApiUrl}`, false);
      loader.fetchOpenApi(openApiUrl)
        .then(function (spec) {
          loadSpec(spec, openApiUrl, `Loaded OpenAPI from ${openApiUrl}.`);
        })
        .catch(function (error) {
          clearTarget();
          controls.setStatus(status, error.message, true);
        });
    }

    function renderFile(file) {
      if (!file) {
        return;
      }

      const reader = new FileReader();
      reader.addEventListener('load', function () {
        try {
          const spec = loader.parseOpenApiText(String(reader.result || ''), file.name);
          loadSpec(spec, file.name, `Loaded ${file.name} from this browser.`);
        } catch (error) {
          clearTarget();
          controls.setStatus(status, error.message, true);
        }
      });
      reader.addEventListener('error', function () {
        clearTarget();
        controls.setStatus(status, `Could not read ${file.name}.`, true);
      });
      reader.readAsText(file);
    }

    function renderConvertedSpec(storageKey) {
      try {
        const payload = readConvertedSpecPayload(storageKey);
        loadSpec(payload.spec, payload.name, `Loaded converted OpenAPI from ${payload.name}.`);
        return true;
      } catch (error) {
        controls.setStatus(status, error.message, true);
        return false;
      }
    }

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      renderUrl(urlInput.value);
    });

    fileInput.addEventListener('change', function () {
      renderFile(fileInput.files && fileInput.files[0]);
    });

    const searchParams = new URLSearchParams(window.location.search);
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
    document.querySelectorAll('[data-online-swagger-client]').forEach(initSwaggerClient);
  });
}());
