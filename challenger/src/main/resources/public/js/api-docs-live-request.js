(function () {
  'use strict';

  const API_CHALLENGE_PREFIXES = '/api||/todos||/todo||/challenges||/challenger||/secret||/heartbeat';
  const DOC_CONFIGS = {
    '/api/docs': {
      allowedPathPrefixes: API_CHALLENGE_PREFIXES,
      useChallenger: 'true',
    },
    '/simpleapi/docs': {
      allowedPathPrefixes: '/simpleapi',
      useChallenger: 'false',
      simpleApiRandomIsbn: true,
    },
    '/shop/docs': {
      allowedPathPrefixes: '/shop',
      useChallenger: 'false',
    },
  };

  function onReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback);
      return;
    }
    callback();
  }

  function pathMatchesPrefix(path, prefix) {
    return path === prefix || path.indexOf(`${prefix}/`) === 0;
  }

  function isAllowedEndpoint(path, prefixes) {
    return prefixes.some(function (prefix) {
      return pathMatchesPrefix(path, prefix);
    });
  }

  function endpointMethodsForHeading(heading) {
    const methods = [];
    let sibling = heading.nextElementSibling;
    while (sibling && sibling.tagName !== 'H4') {
      sibling.querySelectorAll('li.endpoint strong').forEach(function (methodHeading) {
        const match = methodHeading.textContent.trim().match(/^([A-Z]+)\s+/);
        if (match && methods.indexOf(match[1]) === -1) {
          methods.push(match[1]);
        }
      });
      sibling = sibling.nextElementSibling;
    }
    return methods;
  }

  function insertBeforeFirstEndpointList(heading, details) {
    const parent = heading.parentNode;
    let sibling = heading.nextElementSibling;
    while (sibling && sibling.tagName !== 'H4') {
      if (sibling.tagName === 'UL') {
        parent.insertBefore(details, sibling);
        return;
      }
      sibling = sibling.nextElementSibling;
    }
    parent.insertBefore(details, sibling || null);
  }

  function buildLiveRequestDetails(endpointPath, method, config) {
    const details = document.createElement('details');
    details.className = 'sim-live-request-details api-docs-live-request-details';

    const summary = document.createElement('summary');
    summary.textContent = 'Experiment with this endpoint';
    details.appendChild(summary);

    const widget = document.createElement('div');
    widget.className = 'api-live-request';
    widget.dataset.method = method;
    widget.dataset.path = endpointPath;
    widget.dataset.editable = 'true';
    widget.dataset.editMode = 'adhoc';
    widget.dataset.allowedPathPrefixes = config.allowedPathPrefixes;
    widget.dataset.useChallenger = config.useChallenger;
    widget.dataset.headers = 'Accept: application/json';
    details.appendChild(widget);

    return details;
  }

  function buildEndpointPracticeTools(endpointPath, method, config) {
    const fragment = document.createDocumentFragment();
    fragment.appendChild(buildLiveRequestDetails(endpointPath, method, config));
    if (
      config.simpleApiRandomIsbn
        && window.ApiChallengesSimpleApiRandomIsbn
        && window.ApiChallengesSimpleApiRandomIsbn.buildDetails
    ) {
      fragment.appendChild(window.ApiChallengesSimpleApiRandomIsbn.buildDetails());
    }
    return fragment;
  }

  function enhanceDocs() {
    const config = DOC_CONFIGS[window.location.pathname];
    if (!config) {
      return;
    }

    const allowedPrefixes = config.allowedPathPrefixes.split('||');
    const container = document.querySelector('main') || document.body;
    container.querySelectorAll('h4').forEach(function (heading) {
      const endpointPath = heading.textContent.trim();
      if (!endpointPath.startsWith('/') || !isAllowedEndpoint(endpointPath, allowedPrefixes)) {
        return;
      }

      if (heading.dataset.liveRequestEnhanced === 'true') {
        return;
      }

      const methods = endpointMethodsForHeading(heading);
      const method = methods.indexOf('GET') === -1 ? (methods[0] || 'GET') : 'GET';
      heading.dataset.liveRequestEnhanced = 'true';
      insertBeforeFirstEndpointList(
        heading,
        buildEndpointPracticeTools(endpointPath, method, config));
    });

    if (window.ApiChallengesLiveRequest && window.ApiChallengesLiveRequest.renderAll) {
      window.ApiChallengesLiveRequest.renderAll();
    }
  }

  onReady(enhanceDocs);
}());
