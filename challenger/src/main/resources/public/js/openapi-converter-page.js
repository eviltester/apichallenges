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

  function setStatus(statusElement, message, isError) {
    statusElement.textContent = message;
    statusElement.classList.toggle('online-client-status-error', isError === true);
  }

  function readOptions(tool) {
    const profile = tool.querySelector('[data-openapi-profile]').value;
    const verbs = [].slice.call(tool.querySelectorAll('[data-openapi-verb]:checked')).map(function (input) {
      return input.value;
    });
    const options = {
      profile: profile,
      verbs: verbs,
    };

    tool.querySelectorAll('[data-openapi-option]').forEach(function (input) {
      options[input.dataset.openapiOption] = input.checked;
    });

    return options;
  }

  function writeOptions(tool, options) {
    tool.querySelectorAll('[data-openapi-option]').forEach(function (input) {
      input.checked = options[input.dataset.openapiOption] === true;
    });

    tool.querySelectorAll('[data-openapi-verb]').forEach(function (input) {
      input.checked = options.verbs.indexOf(input.value) >= 0;
    });
  }

  function hasSelectedTesterOptions(tool) {
    return [].slice.call(tool.querySelectorAll('[data-openapi-option], [data-openapi-verb]')).some(function (input) {
      return input.checked;
    });
  }

  function profileChanged(tool) {
    const profile = tool.querySelector('[data-openapi-profile]').value;
    const api = converterApi();

    if (profile !== 'custom' && api) {
      writeOptions(tool, api.profileOptions(profile));
    } else if (profile === 'custom' && api && !hasSelectedTesterOptions(tool)) {
      writeOptions(tool, api.profileOptions('practical'));
    }

    const customOptions = tool.querySelector('[data-openapi-custom-options]');
    if (customOptions && profile === 'custom') {
      customOptions.open = true;
    }
  }

  function setCustomProfile(tool) {
    const profile = tool.querySelector('[data-openapi-profile]');
    if (profile.value !== 'custom') {
      profile.value = 'custom';
      profileChanged(tool);
    }
  }

  function setButtons(tool, enabled) {
    tool.querySelectorAll('[data-openapi-copy-converted], [data-openapi-download-converted], [data-openapi-open-swagger]').forEach(function (button) {
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

  function initConverter(tool) {
    const api = converterApi();
    const loader = textLoaderApi();
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

    if (!api) {
      setStatus(status, 'The OpenAPI converter could not be loaded.', true);
      setButtons(tool, false);
      return;
    }

    if (!loader) {
      setStatus(status, 'The OpenAPI JSON/YAML loader could not be loaded.', true);
      setButtons(tool, false);
      return;
    }

    function renderConversion(loadedMessage) {
      const options = readOptions(tool);
      convertedSpec = null;
      output.value = '';
      setButtons(tool, false);

      if (!originalSpec) {
        setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a tester profile.', false);
        return;
      }

      if (options.profile === 'original') {
        setStatus(status, `${loadedMessage || `Loaded ${sourceName}.`} Select Practical, Aggressive, or Custom to create a tester OpenAPI file.`, false);
        return;
      }

      try {
        const result = api.convert(originalSpec, options);
        convertedSpec = result.spec;
        output.value = api.stringify(convertedSpec);
        setButtons(tool, true);
        setStatus(status, result.summary, false);
      } catch (error) {
        setStatus(status, error.message, true);
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
        setStatus(status, 'Enter an OpenAPI or Swagger URL to load.', true);
        return;
      }

      setStatus(status, `Loading OpenAPI from ${openApiUrl}`, false);
      loader.fetchOpenApi(openApiUrl)
        .then(function (spec) {
          loadSpec(spec, openApiUrl);
        })
        .catch(function (error) {
          originalSpec = null;
          convertedSpec = null;
          output.value = '';
          setButtons(tool, false);
          setStatus(status, error.message, true);
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
          setButtons(tool, false);
          setStatus(status, error.message, true);
        }
      });
      reader.addEventListener('error', function () {
        setStatus(status, `Could not read ${file.name}.`, true);
      });
      reader.readAsText(file);
    }

    profile.addEventListener('change', function () {
      profileChanged(tool);
      renderConversion();
    });

    tool.querySelectorAll('[data-openapi-option], [data-openapi-verb]').forEach(function (input) {
      input.addEventListener('change', function () {
        setCustomProfile(tool);
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
        copyText(api.stringify(convertedSpec), copyButton);
      }
    });

    downloadButton.addEventListener('click', function () {
      if (convertedSpec) {
        downloadJson(api.stringify(convertedSpec), api.convertedFilename(sourceName));
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
      profileChanged(tool);
      setButtons(tool, false);
      setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a tester profile.', false);
    }
  }

  onReady(function () {
    document.querySelectorAll('[data-openapi-converter]').forEach(initConverter);
  });
}());
