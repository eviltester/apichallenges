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

  function parameterPlacementApi() {
    return window.ApiChallengesOpenApiParameterPlacement;
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

  function textMetrics(value) {
    const text = String(value || '');
    return {
      characterCount: text.length,
      lineCount: text.length === 0 ? 0 : text.split(/\r\n|\r|\n/).length,
    };
  }

  function signedDifference(value) {
    return value >= 0 ? `+${value}` : String(value);
  }

  function initConverter(tool) {
    const api = converterApi();
    const loader = textLoaderApi();
    const controls = controlsApi();
    const form = tool.querySelector('[data-openapi-url-form]');
    const urlInput = tool.querySelector('[data-openapi-url]');
    const loadStatus = tool.querySelector('[data-openapi-load-status]');
    const fileInput = tool.querySelector('[data-openapi-file]');
    const profile = tool.querySelector('[data-openapi-profile]');
    const status = tool.querySelector('[data-openapi-status]');
    const placementStatus = tool.querySelector('[data-openapi-placement-status]');
    const placementMessage = tool.querySelector('[data-openapi-placement-message]');
    const placementComparison = tool.querySelector('[data-openapi-placement-comparison]');
    const placementCurrent = tool.querySelector('[data-openapi-placement-current]');
    const placementOutput = tool.querySelector('[data-openapi-placement-output]');
    const placementWarnings = tool.querySelector('[data-openapi-placement-warnings]');
    const evaluatePlacementButton = tool.querySelector('[data-openapi-evaluate-placement]');
    const output = tool.querySelector('[data-openapi-output]');
    const outputLabel = tool.querySelector('[data-openapi-output-label]');
    const copyButton = tool.querySelector('[data-openapi-copy-converted]');
    const downloadButton = tool.querySelector('[data-openapi-download-converted]');
    const openClientButtons = [].slice.call(tool.querySelectorAll('[data-openapi-open-client]'));
    let originalSpec = null;
    let convertedSpec = null;
    let sourceName = 'openapi';
    let sourceUrl = '';
    let originalMetrics = textMetrics('');

    function originalMetricsSummary() {
      return `Original length: ${originalMetrics.characterCount} bytes; lines: ${originalMetrics.lineCount} lines.`;
    }

    function setOutputMetrics(convertedText) {
      if (!outputLabel) {
        return;
      }

      if (!convertedText) {
        outputLabel.textContent = 'Converted OpenAPI JSON';
        return;
      }

      const convertedMetrics = textMetrics(convertedText);
      const characterDifference = convertedMetrics.characterCount - originalMetrics.characterCount;
      const lineDifference = convertedMetrics.lineCount - originalMetrics.lineCount;
      outputLabel.textContent = `Converted OpenAPI JSON - length: ${convertedMetrics.characterCount} bytes (${signedDifference(characterDifference)} diff), lines: ${convertedMetrics.lineCount} lines (${signedDifference(lineDifference)} diff)`;
    }

    function showPlacementMessage(message, isError) {
      placementStatus.classList.toggle('online-client-status-error', isError === true);
      placementMessage.textContent = message;
      placementMessage.hidden = false;
      placementComparison.hidden = true;
      placementCurrent.textContent = '';
      placementOutput.textContent = '';
    }

    function showPlacementComparison(currentSummary, outputSummary, prefix) {
      placementStatus.classList.toggle('online-client-status-error', false);
      placementMessage.textContent = String(prefix || '').trim();
      placementMessage.hidden = !placementMessage.textContent;
      placementCurrent.textContent = `Current: ${currentSummary}`;
      placementOutput.textContent = `Output: ${outputSummary}`;
      placementComparison.hidden = false;
    }

    function clearLoadStatus() {
      if (!loadStatus) {
        return;
      }

      loadStatus.textContent = '';
      loadStatus.hidden = true;
    }

    function showLoadStatus(message, isError) {
      if (!loadStatus) {
        return;
      }

      controls.setStatus(loadStatus, message, isError);
      loadStatus.hidden = false;
    }

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

    function setPlacementWarnings(warnings) {
      if (!placementWarnings) {
        return;
      }

      const messages = warnings || [];
      placementWarnings.textContent = messages.length > 0
        ? `Warnings: ${messages.join(' ')}`
        : '';
      placementWarnings.hidden = messages.length === 0;
    }

    function showPlacementEvaluation(spec, placementOptions, explicitEvaluation) {
      const placementApi = parameterPlacementApi();
      const prefix = explicitEvaluation ? 'Evaluation complete. ' : '';
      if (!placementApi) {
        showPlacementMessage(
          'The path parameter placement converter could not be loaded.',
          true,
        );
        setPlacementWarnings([]);
        return null;
      }

      try {
        if (placementOptions.target === 'keep') {
          const evaluation = placementApi.evaluate(spec, placementOptions);
          showPlacementMessage(`${prefix}${evaluation.summary}`, false);
          setPlacementWarnings(evaluation.warnings);
          return {
            changed: false,
            spec: spec,
            summary: evaluation.summary,
          };
        }

        const result = placementApi.convert(spec, placementOptions);
        if (result.changed) {
          showPlacementComparison(result.before.summary, result.after.summary, prefix);
        } else {
          showPlacementMessage(`${prefix}${result.summary}`, false);
        }
        setPlacementWarnings(result.warnings);
        return result;
      } catch (error) {
        showPlacementMessage(error.message, true);
        setPlacementWarnings([]);
        return null;
      }
    }

    function renderConversion(loadedMessage, explicitEvaluation) {
      const options = controls.readOptions(tool);
      const placementOptions = controls.readPlacementOptions(tool);
      const transformationRequested = options.profile !== 'original'
        || placementOptions.target !== 'keep';
      convertedSpec = null;
      output.value = '';
      setOutputMetrics('');
      controls.setButtons(tool, controls.swaggerExportActionsSelector, false);

      if (!originalSpec) {
        controls.setButtons(tool, controls.embeddedClientActionsSelector, false);
        controls.setPlacementControlsEnabled(tool, false);
        controls.setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a conversion.', false);
        showPlacementMessage('Load an OpenAPI specification to evaluate parameter placement.', false);
        setPlacementWarnings([]);
        return;
      }

      controls.setButtons(tool, controls.embeddedClientActionsSelector, true);
      controls.setPlacementControlsEnabled(tool, true);

      try {
        let currentSpec = originalSpec;
        let changed = false;
        const summaries = [];

        if (options.profile !== 'original') {
          const expansionResult = api.convert(currentSpec, options);
          currentSpec = expansionResult.spec;
          changed = expansionResult.converted;
          summaries.push(expansionResult.summary);
        }

        const placementResult = showPlacementEvaluation(
          currentSpec,
          placementOptions,
          explicitEvaluation,
        );
        if (placementResult && placementOptions.target !== 'keep') {
          currentSpec = placementResult.spec;
          changed = changed || placementResult.changed;
          summaries.push(placementResult.summary);
        }

        if (placementOptions.target !== 'keep' && !placementResult) {
          controls.setStatus(status, 'The requested parameter placement conversion could not be completed.', true);
          return;
        }

        if (changed || transformationRequested) {
          convertedSpec = currentSpec;
          const convertedText = api.stringify(convertedSpec);
          output.value = convertedText;
          setOutputMetrics(convertedText);
          controls.setButtons(tool, controls.swaggerExportActionsSelector, true);
          controls.setStatus(status, summaries.join(' '), false);
          return;
        }

        controls.setStatus(
          status,
          `${loadedMessage || `Loaded ${sourceName}.`} Open it in an embedded client, or select an expansion profile or parameter placement conversion.`,
          false,
        );
      } catch (error) {
        controls.setStatus(status, error.message, true);
      }
    }

    function loadSpec(spec, name, url, sourceText) {
      originalSpec = spec;
      sourceName = name || 'openapi';
      sourceUrl = url || '';
      originalMetrics = textMetrics(sourceText || api.stringify(spec));
      renderConversion(`Loaded ${sourceName}. ${originalMetricsSummary()}`);
    }

    function loadUrl(rawUrl) {
      const openApiUrl = rawUrl.trim();
      if (!openApiUrl) {
        const message = 'Enter an OpenAPI or Swagger URL to load.';
        showLoadStatus(message, true);
        controls.setStatus(status, message, true);
        return;
      }

      clearLoadStatus();
      showLoadStatus(`Loading OpenAPI from ${openApiUrl}...`, false);
      controls.setStatus(status, `Loading OpenAPI from ${openApiUrl}`, false);
      loader.fetchOpenApiDocument(openApiUrl)
        .then(function (loadedDocument) {
          loadSpec(loadedDocument.spec, openApiUrl, openApiUrl, loadedDocument.text);
          showLoadStatus(`Loaded OpenAPI from ${openApiUrl}. ${originalMetricsSummary()}`, false);
        })
        .catch(function (error) {
          originalSpec = null;
          convertedSpec = null;
          sourceUrl = '';
          originalMetrics = textMetrics('');
          output.value = '';
          setOutputMetrics('');
          controls.setButtons(tool, controls.allExportActionsSelector, false);
          controls.setPlacementControlsEnabled(tool, false);
          showPlacementMessage('OpenAPI could not be loaded.', true);
          setPlacementWarnings([]);
          const message = error && error.message
            ? error.message
            : `Could not load ${openApiUrl}.`;
          showLoadStatus(message, true);
          controls.setStatus(status, message, true);
        });
    }

    function loadFile(file) {
      if (!file) {
        return;
      }

      clearLoadStatus();
      const reader = new FileReader();
      reader.addEventListener('load', function () {
        try {
          const sourceText = String(reader.result || '');
          loadSpec(loader.parseOpenApiText(sourceText, file.name), file.name, '', sourceText);
          showLoadStatus(`Loaded OpenAPI from ${file.name}. ${originalMetricsSummary()}`, false);
        } catch (error) {
          originalSpec = null;
          convertedSpec = null;
          sourceUrl = '';
          originalMetrics = textMetrics('');
          output.value = '';
          setOutputMetrics('');
          controls.setButtons(tool, controls.allExportActionsSelector, false);
          controls.setPlacementControlsEnabled(tool, false);
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

    tool.querySelectorAll('[data-openapi-placement-scope], [data-openapi-placement-target]').forEach(function (input) {
      input.addEventListener('change', function () {
        renderConversion();
      });
    });

    evaluatePlacementButton.addEventListener('click', function () {
      renderConversion(undefined, true);
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
      controls.setPlacementControlsEnabled(tool, false);
      controls.setStatus(status, 'Load an OpenAPI JSON or YAML file, then choose a conversion.', false);
      showPlacementMessage('Load an OpenAPI specification to evaluate parameter placement.', false);
      setPlacementWarnings([]);
    }
  }

  onReady(function () {
    document.querySelectorAll('[data-openapi-converter]').forEach(initConverter);
  });
}());
