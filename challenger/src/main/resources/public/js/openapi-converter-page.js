(function () {
  'use strict';

  const CONVERTED_SESSION_KEY_PREFIX = 'apiChallengesConvertedOpenApiSpec:';
  const CONVERTED_SESSION_TTL_MS = 24 * 60 * 60 * 1000;

  function onReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback);
      return;
    }
    callback();
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

  function convertedSessionStorage() {
    if (!window.sessionStorage) {
      throw new Error('Browser session storage is required to open this OpenAPI file in an embedded client.');
    }

    return window.sessionStorage;
  }

  function convertedStorageKey() {
    if (window.crypto && typeof window.crypto.randomUUID === 'function') {
      return `${CONVERTED_SESSION_KEY_PREFIX}${window.crypto.randomUUID()}`;
    }

    return `${CONVERTED_SESSION_KEY_PREFIX}${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }

  function cleanupConvertedSpecs(storage) {
    const expiresBefore = Date.now() - CONVERTED_SESSION_TTL_MS;

    for (let index = storage.length - 1; index >= 0; index--) {
      const key = storage.key(index);
      if (!key || !key.startsWith(CONVERTED_SESSION_KEY_PREFIX)) {
        continue;
      }

      try {
        const payload = JSON.parse(storage.getItem(key) || '{}');
        if (!payload.createdAt || payload.createdAt < expiresBefore) {
          storage.removeItem(key);
        }
      } catch (error) {
        storage.removeItem(key);
      }
    }
  }

  function storeOpenApiSpec(spec, filename) {
    const storage = convertedSessionStorage();
    cleanupConvertedSpecs(storage);

    const key = convertedStorageKey();
    storage.setItem(key, JSON.stringify({
      createdAt: Date.now(),
      name: filename || 'converted-openapi.json',
      spec: spec,
    }));
    return key;
  }

  function clientUrlWithParameter(clientPath, name, value) {
    const separator = clientPath.indexOf('?') >= 0 ? '&' : '?';
    return `${clientPath}${separator}${name}=${encodeURIComponent(value)}`;
  }

  function initConverter(tool) {
    const api = converterApi();
    const loader = textLoaderApi();
    const controls = controlsApi();
    const form = tool.querySelector('[data-openapi-url-form]');
    const urlInput = tool.querySelector('[data-openapi-url]');
    const fileInput = tool.querySelector('[data-openapi-file]');
    const profile = tool.querySelector('[data-openapi-profile]');
    const status = tool.querySelector('[data-openapi-status]');
    const output = tool.querySelector('[data-openapi-output]');
    const copyButton = tool.querySelector('[data-openapi-copy-converted]');
    const downloadButton = tool.querySelector('[data-openapi-download-converted]');
    const openClientButtons = [].slice.call(tool.querySelectorAll('[data-openapi-open-client]'));
    let originalSpec = null;
    let convertedSpec = null;
    let sourceName = 'openapi';
    let sourceUrl = '';

    if (!controls) {
      status.textContent = 'The OpenAPI tool controls could not be loaded.';
      status.classList.add('online-client-status-error');
      return;
    }

    if (!api) {
      controls.setStatus(status, 'The OpenAPI converter could not be loaded.', true);
      controls.setButtons(tool, controls.allExportActionsSelector, false);
      return;
    }

    if (!loader) {
      controls.setStatus(status, 'The OpenAPI JSON/YAML loader could not be loaded.', true);
      controls.setButtons(tool, controls.allExportActionsSelector, false);
      return;
    }

    function renderConversion(loadedMessage) {
      const options = controls.readOptions(tool);
      convertedSpec = null;
      output.value = '';
      controls.setButtons(tool, controls.swaggerExportActionsSelector, false);

      if (!originalSpec) {
        controls.setButtons(tool, controls.embeddedClientActionsSelector, false);
        controls.setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a tester profile.', false);
        return;
      }

      controls.setButtons(tool, controls.embeddedClientActionsSelector, true);

      if (options.profile === 'original') {
        controls.setStatus(status, `${loadedMessage || `Loaded ${sourceName}.`} Open it in an embedded client, or select Practical, Aggressive, or Custom to create a tester OpenAPI file.`, false);
        return;
      }

      try {
        const result = api.convert(originalSpec, options);
        convertedSpec = result.spec;
        output.value = api.stringify(convertedSpec);
        controls.setButtons(tool, controls.swaggerExportActionsSelector, true);
        controls.setStatus(status, result.summary, false);
      } catch (error) {
        controls.setButtons(tool, controls.embeddedClientActionsSelector, false);
        controls.setStatus(status, error.message, true);
      }
    }

    function loadSpec(spec, name, url) {
      originalSpec = spec;
      sourceName = name || 'openapi';
      sourceUrl = url || '';
      renderConversion(`Loaded ${sourceName}.`);
    }

    function loadUrl(rawUrl) {
      const openApiUrl = rawUrl.trim();
      if (!openApiUrl) {
        controls.setStatus(status, 'Enter an OpenAPI or Swagger URL to load.', true);
        return;
      }

      controls.setStatus(status, `Loading OpenAPI from ${openApiUrl}`, false);
      loader.fetchOpenApi(openApiUrl)
        .then(function (spec) {
          loadSpec(spec, openApiUrl, openApiUrl);
        })
        .catch(function (error) {
          originalSpec = null;
          convertedSpec = null;
          sourceUrl = '';
          output.value = '';
          controls.setButtons(tool, controls.allExportActionsSelector, false);
          controls.setStatus(status, error.message, true);
        });
    }

    function loadFile(file) {
      if (!file) {
        return;
      }

      const reader = new FileReader();
      reader.addEventListener('load', function () {
        try {
          loadSpec(loader.parseOpenApiText(String(reader.result || ''), file.name), file.name, '');
        } catch (error) {
          originalSpec = null;
          convertedSpec = null;
          sourceUrl = '';
          output.value = '';
          controls.setButtons(tool, controls.allExportActionsSelector, false);
          controls.setStatus(status, error.message, true);
        }
      });
      reader.addEventListener('error', function () {
        controls.setStatus(status, `Could not read ${file.name}.`, true);
      });
      reader.readAsText(file);
    }

    profile.addEventListener('change', function () {
      controls.applyProfile(tool, api);
      renderConversion();
    });

    tool.querySelectorAll('[data-openapi-option], [data-openapi-verb]').forEach(function (input) {
      input.addEventListener('change', function () {
        controls.switchToCustomProfile(tool, api);
        renderConversion();
      });
    });

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      loadUrl(urlInput.value);
    });

    fileInput.addEventListener('change', function () {
      loadFile(fileInput.files && fileInput.files[0]);
    });

    copyButton.addEventListener('click', function () {
      if (convertedSpec) {
        controls.copyText(api.stringify(convertedSpec), copyButton);
      }
    });

    downloadButton.addEventListener('click', function () {
      if (convertedSpec) {
        controls.downloadJson(api.stringify(convertedSpec), api.convertedFilename(sourceName));
      }
    });

    openClientButtons.forEach(function (button) {
      button.addEventListener('click', function () {
        if (!originalSpec) {
          return;
        }

        const clientPath = button.dataset.openapiClientPath
          || `/tools/online-clients/${button.dataset.openapiOpenClient || 'swagger'}`;

        try {
          if (convertedSpec) {
            const storageKey = storeOpenApiSpec(convertedSpec, api.convertedFilename(sourceName));
            window.location.href = clientUrlWithParameter(clientPath, 'converted', storageKey);
            return;
          }

          if (sourceUrl) {
            window.location.href = clientUrlWithParameter(clientPath, 'url', sourceUrl);
            return;
          }

          const storageKey = storeOpenApiSpec(originalSpec, sourceName);
          window.location.href = clientUrlWithParameter(clientPath, 'converted', storageKey);
        } catch (error) {
          controls.setStatus(status, error.message, true);
        }
      });
    });

    const urlParameter = new URLSearchParams(window.location.search).get('url');
    if (urlParameter) {
      urlInput.value = urlParameter;
      loadUrl(urlParameter);
    } else {
      controls.applyProfile(tool, api);
      controls.setButtons(tool, controls.allExportActionsSelector, false);
      controls.setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a tester profile.', false);
    }
  }

  onReady(function () {
    document.querySelectorAll('[data-openapi-converter]').forEach(initConverter);
  });
}());
