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

  function setStatus(statusElement, message, isError) {
    statusElement.textContent = message;
    statusElement.classList.toggle('online-client-status-error', isError === true);
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

  function readOptions(client) {
    const profile = client.querySelector('[data-openapi-profile]').value;
    const verbs = [].slice.call(client.querySelectorAll('[data-openapi-verb]:checked')).map(function (input) {
      return input.value;
    });
    const options = {
      profile: profile,
      verbs: verbs,
    };

    client.querySelectorAll('[data-openapi-option]').forEach(function (input) {
      options[input.dataset.openapiOption] = input.checked;
    });

    return options;
  }

  function writeOptions(client, options) {
    client.querySelectorAll('[data-openapi-option]').forEach(function (input) {
      input.checked = options[input.dataset.openapiOption] === true;
    });

    client.querySelectorAll('[data-openapi-verb]').forEach(function (input) {
      input.checked = options.verbs.indexOf(input.value) >= 0;
    });
  }

  function hasSelectedTesterOptions(client) {
    return [].slice.call(client.querySelectorAll('[data-openapi-option], [data-openapi-verb]')).some(function (input) {
      return input.checked;
    });
  }

  function profileChanged(client) {
    const profile = client.querySelector('[data-openapi-profile]').value;
    const api = converterApi();

    if (profile !== 'custom' && api) {
      writeOptions(client, api.profileOptions(profile));
    } else if (profile === 'custom' && api && !hasSelectedTesterOptions(client)) {
      writeOptions(client, api.profileOptions('practical'));
    }

    const customOptions = client.querySelector('[data-openapi-custom-options]');
    if (customOptions && profile === 'custom') {
      customOptions.open = true;
    }
  }

  function setCustomProfile(client) {
    const profile = client.querySelector('[data-openapi-profile]');
    if (profile.value !== 'custom') {
      profile.value = 'custom';
      profileChanged(client);
    }
  }

  function setExportButtons(client, enabled) {
    client.querySelectorAll('[data-openapi-copy-converted], [data-openapi-download-converted]').forEach(function (button) {
      button.disabled = !enabled;
    });
  }

  function copyText(text, button) {
    function copied() {
      const originalText = button.textContent;
      button.textContent = 'Copied';
      window.setTimeout(function () {
        button.textContent = originalText;
      }, 1500);
    }

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(copied);
      return;
    }

    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', 'readonly');
    textarea.style.position = 'fixed';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    textarea.remove();
    copied();
  }

  function downloadJson(text, filename) {
    const blob = new Blob([text], { type: 'application/json' });
    const link = document.createElement('a');

    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.setTimeout(function () {
      URL.revokeObjectURL(link.href);
    }, 0);
  }

  function initSwaggerClient(client) {
    const loader = textLoaderApi();
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

    if (!loader) {
      setStatus(status, 'The OpenAPI JSON/YAML loader could not be loaded.', true);
      setExportButtons(client, false);
      return;
    }

    function clearTarget() {
      target.innerHTML = '';
    }

    function renderSource(source, statusMessage) {
      if (!hasSwaggerUi()) {
        setStatus(status, 'Swagger UI could not be loaded. Check your network connection.', true);
        return;
      }

      clearTarget();
      swaggerUi = window.SwaggerUIBundle(swaggerOptions(targetSelector, source));
      window.ApiChallengesOnlineSwagger = swaggerUi;
      setStatus(status, statusMessage, false);
    }

    function renderCurrent(loadedMessage) {
      if (!originalSpec) {
        return;
      }

      const api = converterApi();
      const options = readOptions(client);
      let specToRender = originalSpec;
      let statusMessage = loadedMessage || `Loaded ${sourceName}.`;

      convertedSpec = null;
      setExportButtons(client, false);

      if (options.profile !== 'original') {
        if (!api) {
          setStatus(status, 'The OpenAPI converter could not be loaded.', true);
          return;
        }

        try {
          const result = api.convert(originalSpec, options);
          specToRender = result.spec;
          convertedSpec = result.spec;
          statusMessage = result.summary;
          setExportButtons(client, true);
        } catch (error) {
          clearTarget();
          setStatus(status, error.message, true);
          return;
        }
      } else if (loadedConvertedSpec) {
        convertedSpec = originalSpec;
        setExportButtons(client, true);
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
        setStatus(status, 'Enter an OpenAPI or Swagger URL to load.', true);
        return;
      }

      setStatus(status, `Loading OpenAPI from ${openApiUrl}`, false);
      loader.fetchOpenApi(openApiUrl)
        .then(function (spec) {
          loadSpec(spec, openApiUrl, `Loaded OpenAPI from ${openApiUrl}.`, false);
        })
        .catch(function (error) {
          clearTarget();
          originalSpec = null;
          convertedSpec = null;
          setExportButtons(client, false);
          setStatus(status, error.message, true);
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
          setStatus(status, error.message, true);
        }
      });
      reader.addEventListener('error', function () {
        clearTarget();
        setStatus(status, `Could not read ${file.name}.`, true);
      });
      reader.readAsText(file);
    }

    function renderConvertedSessionSpec() {
      const storedSpec = window.sessionStorage.getItem(SWAGGER_SESSION_SPEC_KEY);
      const storedName = window.sessionStorage.getItem(SWAGGER_SESSION_NAME_KEY) || 'converted-openapi.json';

      if (!storedSpec) {
        setStatus(status, 'No converted OpenAPI file was found in this browser session.', true);
        return false;
      }

      try {
        loadSpec(JSON.parse(storedSpec), storedName, `Loaded converted tester OpenAPI from ${storedName}.`, true);
        return true;
      } catch (error) {
        setStatus(status, 'The converted OpenAPI file in this browser session could not be read.', true);
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
      profileChanged(client);
      renderCurrent();
    });

    client.querySelectorAll('[data-openapi-option], [data-openapi-verb]').forEach(function (input) {
      input.addEventListener('change', function () {
        setCustomProfile(client);
        renderCurrent();
      });
    });

    copyButton.addEventListener('click', function () {
      const api = converterApi();
      if (api && convertedSpec) {
        copyText(api.stringify(convertedSpec), copyButton);
      }
    });

    downloadButton.addEventListener('click', function () {
      const api = converterApi();
      if (api && convertedSpec) {
        downloadJson(api.stringify(convertedSpec), api.convertedFilename(sourceName));
      }
    });

    profileChanged(client);
    setExportButtons(client, false);

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
