(function () {
  'use strict';

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

  function setStatus(statusElement, message, isError) {
    statusElement.textContent = message;
    statusElement.classList.toggle('online-client-status-error', isError === true);
  }

  function parseOpenApiText(text, filename) {
    const trimmed = text.trim();
    if (!trimmed) {
      throw new Error('The selected file is empty.');
    }

    if (trimmed.charAt(0) === '{' || trimmed.charAt(0) === '[') {
      return JSON.parse(trimmed);
    }

    if (window.jsyaml && typeof window.jsyaml.load === 'function') {
      return window.jsyaml.load(trimmed);
    }

    throw new Error(`${filename || 'This file'} is not JSON, and YAML parsing is unavailable.`);
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
    const form = client.querySelector('[data-openapi-url-form]');
    const urlInput = client.querySelector('[data-openapi-url]');
    const fileInput = client.querySelector('[data-openapi-file]');
    const status = client.querySelector('[data-openapi-status]');
    const target = client.querySelector('[data-openapi-render-target]');
    const defaultOpenApiUrl = client.dataset.defaultOpenapiUrl || '/docs/openapi.json';
    const targetSelector = `#${target.id}`;
    let swaggerUi = null;

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

    function renderUrl(rawUrl) {
      const openApiUrl = rawUrl.trim();
      if (!openApiUrl) {
        setStatus(status, 'Enter an OpenAPI or Swagger URL to load.', true);
        return;
      }

      renderSource({ url: openApiUrl }, `Loading OpenAPI from ${openApiUrl}`);
    }

    function renderFile(file) {
      if (!file) {
        return;
      }

      const reader = new FileReader();
      reader.addEventListener('load', function () {
        try {
          const spec = parseOpenApiText(String(reader.result || ''), file.name);
          renderSource({ spec: spec }, `Loaded ${file.name} from this browser.`);
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

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      renderUrl(urlInput.value);
    });

    fileInput.addEventListener('change', function () {
      renderFile(fileInput.files && fileInput.files[0]);
    });

    client.querySelectorAll('[data-openapi-example]').forEach(function (button) {
      button.addEventListener('click', function () {
        urlInput.value = button.dataset.openapiExample;
        renderUrl(urlInput.value);
      });
    });

    const urlParameter = new URLSearchParams(window.location.search).get('url');
    urlInput.value = urlParameter || defaultOpenApiUrl;
    renderUrl(urlInput.value);
  }

  onReady(function () {
    document.querySelectorAll('[data-online-swagger-client]').forEach(initSwaggerClient);
  });
}());
