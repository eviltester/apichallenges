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

  function converterApi() {
    return window.ApiChallengesOpenApiTesterConverter;
  }

  function textLoaderApi() {
    return window.ApiChallengesOpenApiTextLoader;
  }

  function controlsApi() {
    return window.ApiChallengesOpenApiToolControls;
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
    const openSwaggerButton = tool.querySelector('[data-openapi-open-swagger]');
    let originalSpec = null;
    let convertedSpec = null;
    let sourceName = 'openapi';

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
      controls.setButtons(tool, controls.allExportActionsSelector, false);

      if (!originalSpec) {
        controls.setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a tester profile.', false);
        return;
      }

      if (options.profile === 'original') {
        controls.setStatus(status, `${loadedMessage || `Loaded ${sourceName}.`} Select Practical, Aggressive, or Custom to create a tester OpenAPI file.`, false);
        return;
      }

      try {
        const result = api.convert(originalSpec, options);
        convertedSpec = result.spec;
        output.value = api.stringify(convertedSpec);
        controls.setButtons(tool, controls.allExportActionsSelector, true);
        controls.setStatus(status, result.summary, false);
      } catch (error) {
        controls.setStatus(status, error.message, true);
      }
    }

    function loadSpec(spec, name) {
      originalSpec = spec;
      sourceName = name || 'openapi';
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
          loadSpec(spec, openApiUrl);
        })
        .catch(function (error) {
          originalSpec = null;
          convertedSpec = null;
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
          loadSpec(loader.parseOpenApiText(String(reader.result || ''), file.name), file.name);
        } catch (error) {
          originalSpec = null;
          convertedSpec = null;
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

    openSwaggerButton.addEventListener('click', function () {
      if (!convertedSpec) {
        return;
      }

      window.sessionStorage.setItem(SWAGGER_SESSION_SPEC_KEY, api.stringify(convertedSpec));
      window.sessionStorage.setItem(SWAGGER_SESSION_NAME_KEY, api.convertedFilename(sourceName));
      window.location.href = '/tools/online-clients/swagger?converted=session';
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
