(function (root, factory) {
  'use strict';

  const api = factory(root);

  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  }

  if (root) {
    root.ApiChallengesOpenApiTextLoader = api;
  }
}(typeof globalThis !== 'undefined' ? globalThis : this, function (root) {
  'use strict';

  function parseOpenApiText(text, filename) {
    const trimmed = String(text || '').trim();
    if (!trimmed) {
      throw new Error('The selected file is empty.');
    }

    if (trimmed.charAt(0) === '{' || trimmed.charAt(0) === '[') {
      return JSON.parse(trimmed);
    }

    if (root && root.jsyaml && typeof root.jsyaml.load === 'function') {
      return root.jsyaml.load(trimmed);
    }

    throw new Error(`${filename || 'This file'} is not JSON, and YAML parsing is unavailable.`);
  }

  function fetchOpenApi(url) {
    return root.fetch(url).then(function (response) {
      if (!response.ok) {
        throw new Error(`Could not load ${url}. The server returned HTTP ${response.status}.`);
      }

      return response.text();
    }).then(function (text) {
      return parseOpenApiText(text, url);
    });
  }

  return {
    parseOpenApiText: parseOpenApiText,
    fetchOpenApi: fetchOpenApi,
  };
}));
