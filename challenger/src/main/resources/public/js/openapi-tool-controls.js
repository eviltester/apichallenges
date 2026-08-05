(function (root, factory) {
  'use strict';

  const api = factory(root);

  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  }

  if (root) {
    root.ApiChallengesOpenApiToolControls = api;
  }
}(typeof globalThis !== 'undefined' ? globalThis : this, function (root) {
  'use strict';

  const EXPORT_ACTION_SELECTOR = '[data-openapi-copy-converted], [data-openapi-download-converted], [data-openapi-open-swagger]';
  const SWAGGER_EXPORT_ACTION_SELECTOR = '[data-openapi-copy-converted], [data-openapi-download-converted]';

  function setStatus(statusElement, message, isError) {
    statusElement.textContent = message;
    statusElement.classList.toggle('online-client-status-error', isError === true);
  }

  function readOptions(scope) {
    const profile = scope.querySelector('[data-openapi-profile]').value;
    const verbs = [].slice.call(scope.querySelectorAll('[data-openapi-verb]:checked')).map(function (input) {
      return input.value;
    });
    const options = {
      profile: profile,
      verbs: verbs,
    };

    scope.querySelectorAll('[data-openapi-option]').forEach(function (input) {
      options[input.dataset.openapiOption] = input.checked;
    });

    return options;
  }

  function writeOptions(scope, options) {
    const selectedVerbs = options.verbs || [];

    scope.querySelectorAll('[data-openapi-option]').forEach(function (input) {
      input.checked = options[input.dataset.openapiOption] === true;
    });

    scope.querySelectorAll('[data-openapi-verb]').forEach(function (input) {
      input.checked = selectedVerbs.indexOf(input.value) >= 0;
    });
  }

  function hasSelectedTesterOptions(scope) {
    return [].slice.call(scope.querySelectorAll('[data-openapi-option], [data-openapi-verb]')).some(function (input) {
      return input.checked;
    });
  }

  function applyProfile(scope, converterApi) {
    const profile = scope.querySelector('[data-openapi-profile]').value;

    if (profile !== 'custom' && converterApi) {
      writeOptions(scope, converterApi.profileOptions(profile));
    } else if (profile === 'custom' && converterApi && !hasSelectedTesterOptions(scope)) {
      writeOptions(scope, converterApi.profileOptions('practical'));
    }

    const customOptions = scope.querySelector('[data-openapi-custom-options]');
    if (customOptions && profile === 'custom') {
      customOptions.open = true;
    }
  }

  function switchToCustomProfile(scope, converterApi) {
    const profile = scope.querySelector('[data-openapi-profile]');
    if (profile.value !== 'custom') {
      profile.value = 'custom';
      applyProfile(scope, converterApi);
    }
  }

  function setButtons(scope, selector, enabled) {
    scope.querySelectorAll(selector).forEach(function (button) {
      button.disabled = !enabled;
    });
  }

  function copyText(text, button) {
    function copied() {
      const originalText = button.textContent;
      button.textContent = 'Copied';
      root.setTimeout(function () {
        button.textContent = originalText;
      }, 1500);
    }

    if (root.navigator && root.navigator.clipboard && root.navigator.clipboard.writeText) {
      root.navigator.clipboard.writeText(text).then(copied);
      return;
    }

    const textarea = root.document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', 'readonly');
    textarea.style.position = 'fixed';
    textarea.style.left = '-9999px';
    root.document.body.appendChild(textarea);
    textarea.select();
    root.document.execCommand('copy');
    textarea.remove();
    copied();
  }

  function downloadJson(text, filename) {
    const blob = new root.Blob([text], { type: 'application/json' });
    const link = root.document.createElement('a');

    link.href = root.URL.createObjectURL(blob);
    link.download = filename;
    root.document.body.appendChild(link);
    link.click();
    link.remove();
    root.setTimeout(function () {
      root.URL.revokeObjectURL(link.href);
    }, 0);
  }

  return {
    allExportActionsSelector: EXPORT_ACTION_SELECTOR,
    swaggerExportActionsSelector: SWAGGER_EXPORT_ACTION_SELECTOR,
    setStatus: setStatus,
    readOptions: readOptions,
    writeOptions: writeOptions,
    applyProfile: applyProfile,
    switchToCustomProfile: switchToCustomProfile,
    setButtons: setButtons,
    copyText: copyText,
    downloadJson: downloadJson,
  };
}));
