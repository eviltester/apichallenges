(function () {
  'use strict';

  const SWAGGER_SESSION_SPEC_KEY = 'apiChallengesConvertedOpenApiSpec';
  const SWAGGER_SESSION_NAME_KEY = 'apiChallengesConvertedOpenApiName';

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

  function converterApi() {
    return window.ApiChallengesOpenApiTesterConverter;
  }

  function textLoaderApi() {
    return window.ApiChallengesOpenApiTextLoader;
  }

  function controlsApi() {
    return window.ApiChallengesOpenApiToolControls;
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
    const profile = client.querySelector('[data-openapi-profile]');
    const status = client.querySelector('[data-openapi-status]');
    const target = client.querySelector('[data-openapi-render-target]');
    const copyButton = client.querySelector('[data-openapi-copy-converted]');
    const downloadButton = client.querySelector('[data-openapi-download-converted]');
    const defaultOpenApiUrl = client.dataset.defaultOpenapiUrl || '/docs/openapi.json';
    const targetSelector = `#${target.id}`;
    let swaggerUi = null;
    let originalSpec = null;
    let convertedSpec = null;
    let sourceName = 'openapi';
    let loadedConvertedSpec = false;

    if (!controls) {
      status.textContent = 'The OpenAPI tool controls could not be loaded.';
      status.classList.add('online-client-status-error');
      return;
    }

    if (!loader) {
      controls.setStatus(status, 'The OpenAPI JSON/YAML loader could not be loaded.', true);
      controls.setButtons(client, controls.swaggerExportActionsSelector, false);
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

    function renderCurrent(loadedMessage) {
      if (!originalSpec) {
        return;
      }

      const api = converterApi();
      const options = controls.readOptions(client);
      let specToRender = originalSpec;
      let statusMessage = loadedMessage || `Loaded ${sourceName}.`;

      convertedSpec = null;
      controls.setButtons(client, controls.swaggerExportActionsSelector, false);

      if (options.profile !== 'original') {
        if (!api) {
          controls.setStatus(status, 'The OpenAPI converter could not be loaded.', true);
          return;
        }

        try {
          const result = api.convert(originalSpec, options);
          specToRender = result.spec;
          convertedSpec = result.spec;
          statusMessage = result.summary;
          controls.setButtons(client, controls.swaggerExportActionsSelector, true);
        } catch (error) {
          clearTarget();
          controls.setStatus(status, error.message, true);
          return;
        }
      } else if (loadedConvertedSpec) {
        convertedSpec = originalSpec;
        controls.setButtons(client, controls.swaggerExportActionsSelector, true);
      }

      renderSource({ spec: specToRender }, statusMessage);
    }

    function loadSpec(spec, name, message, isConvertedSpec) {
      originalSpec = spec;
      sourceName = name || 'openapi';
      loadedConvertedSpec = isConvertedSpec === true;
      renderCurrent(message || `Loaded ${sourceName}.`);
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
          loadSpec(spec, openApiUrl, `Loaded OpenAPI from ${openApiUrl}.`, false);
        })
        .catch(function (error) {
          clearTarget();
          originalSpec = null;
          convertedSpec = null;
          controls.setButtons(client, controls.swaggerExportActionsSelector, false);
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
          loadSpec(spec, file.name, `Loaded ${file.name} from this browser.`, false);
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

    function renderConvertedSessionSpec() {
      const storedSpec = window.sessionStorage.getItem(SWAGGER_SESSION_SPEC_KEY);
      const storedName = window.sessionStorage.getItem(SWAGGER_SESSION_NAME_KEY) || 'converted-openapi.json';

      if (!storedSpec) {
        controls.setStatus(status, 'No converted OpenAPI file was found in this browser session.', true);
        return false;
      }

      try {
        loadSpec(JSON.parse(storedSpec), storedName, `Loaded converted tester OpenAPI from ${storedName}.`, true);
        return true;
      } catch (error) {
        controls.setStatus(status, 'The converted OpenAPI file in this browser session could not be read.', true);
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

    profile.addEventListener('change', function () {
      controls.applyProfile(client, converterApi());
      renderCurrent();
    });

    client.querySelectorAll('[data-openapi-option], [data-openapi-verb]').forEach(function (input) {
      input.addEventListener('change', function () {
        controls.switchToCustomProfile(client, converterApi());
        renderCurrent();
      });
    });

    copyButton.addEventListener('click', function () {
      const api = converterApi();
      if (api && convertedSpec) {
        controls.copyText(api.stringify(convertedSpec), copyButton);
      }
    });

    downloadButton.addEventListener('click', function () {
      const api = converterApi();
      if (api && convertedSpec) {
        controls.downloadJson(api.stringify(convertedSpec), api.convertedFilename(sourceName));
      }
    });

    controls.applyProfile(client, converterApi());
    controls.setButtons(client, controls.swaggerExportActionsSelector, false);

    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.get('converted') === 'session' && renderConvertedSessionSpec()) {
      return;
    }

    const urlParameter = new URLSearchParams(window.location.search).get('url');
    urlInput.value = urlParameter || defaultOpenApiUrl;
    renderUrl(urlInput.value);
  }

  onReady(function () {
    document.querySelectorAll('[data-online-swagger-client]').forEach(initSwaggerClient);
  });
}());
