(function () {
  'use strict';

  const WIDGET_SELECTOR = '.sim-live-request, .api-live-request';
  const DEFAULT_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD', 'TRACE', 'QUERY'];
  const CUSTOM_METHOD_VALUE = '__custom__';
  const BODY_METHODS = ['POST', 'PUT', 'PATCH', 'QUERY'];
  const EDIT_MODES = ['readonly', 'fixed', 'adhoc'];
  const BROWSER_UNSUPPORTED_METHODS = ['CONNECT', 'TRACE', 'TRACK'];
  const BROWSER_UNSUPPORTED_METHOD_OVERRIDE_HEADERS = [
    'x-http-method',
    'x-http-method-override',
    'x-method-override',
  ];
  const LIVE_WIDGET_HEADER = 'X-API-Challenges-Live-Widget';
  const CHALLENGER_COOKIE = 'X-CHALLENGER';
  const LEGACY_CHALLENGER_COOKIE = 'X-THINGIFIER-DATABASE-NAME';
  const USE_CURL_EXE_COOKIE = 'USE_CURL_EXE';
  const renderedWidgets = [];

  function onReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback);
      return;
    }
    callback();
  }

  function absoluteUrl(path) {
    return new URL(path, window.location.origin).toString();
  }

  function normalizeMethod(method) {
    return (method || 'GET').trim().toUpperCase();
  }

  function appendDefaultMethodOptions(select) {
    DEFAULT_METHODS.forEach(function (method) {
      const option = document.createElement('option');
      option.value = method;
      option.textContent = method;
      select.appendChild(option);
    });
  }

  function methodAllowsBody(method, request) {
    if (request && request.bodyMethods === 'all') {
      return true;
    }
    return BODY_METHODS.indexOf(normalizeMethod(method)) !== -1;
  }

  function readableUrl(url) {
    return String(url || '')
      .replace(/%3E/gi, '>')
      .replace(/%3C/gi, '<')
      .replace(/%7E/gi, '~')
      .replace(/%2A/gi, '*');
  }

  function pathFromUrl(url) {
    try {
      return new URL(url, window.location.origin).pathname;
    } catch (error) {
      return '';
    }
  }

  function pathAndQueryFromUrl(url) {
    try {
      const parsed = new URL(url, window.location.origin);
      return `${parsed.pathname}${parsed.search}`;
    } catch (error) {
      return '';
    }
  }

  function queryFromUrl(url) {
    try {
      return new URL(url, window.location.origin).search.replace(/^\?/, '');
    } catch (error) {
      return '';
    }
  }

  function urlWithQuery(url, query) {
    const parsed = new URL(url, window.location.origin);
    const trimmedQuery = (query || '').trim();
    parsed.search = trimmedQuery
      ? (trimmedQuery.charAt(0) === '?' ? trimmedQuery : `?${trimmedQuery}`)
      : '';
    return parsed.toString();
  }

  function parseAllowedPathPrefixes(value) {
    return (value || '').split('||')
      .map(function (prefix) {
        return prefix.trim();
      })
      .filter(function (prefix) {
        return prefix.length > 0;
      });
  }

  function pathMatchesPrefix(path, prefix) {
    return path === prefix || path.indexOf(`${prefix}/`) === 0;
  }

  function hasUnresolvedPathParameter(path) {
    return /\/:[^/?#]+/.test(path);
  }

  function validateRequestTarget(request) {
    if (!request.allowedPathPrefixes || request.allowedPathPrefixes.length === 0) {
      return { valid: true, message: '' };
    }

    let parsed;
    try {
      parsed = new URL(request.url, window.location.origin);
    } catch (error) {
      return { valid: false, message: 'Request blocked: enter a valid same-origin URL.' };
    }

    if (parsed.origin !== window.location.origin) {
      return { valid: false, message: 'Request blocked: live clients only send same-origin requests.' };
    }

    if (hasUnresolvedPathParameter(parsed.pathname)) {
      return {
        valid: false,
        message: 'Request blocked: replace any path parameters such as :id before sending.',
      };
    }

    const allowed = request.allowedPathPrefixes.some(function (prefix) {
      return pathMatchesPrefix(parsed.pathname, prefix);
    });
    if (!allowed) {
      return {
        valid: false,
        message: `Request blocked: path must start with ${request.allowedPathPrefixes.join(', ')}.`,
      };
    }

    return { valid: true, message: '' };
  }

  function editModeFor(placeholder) {
    const explicitMode = (placeholder.dataset.editMode || '').trim().toLowerCase();
    if (EDIT_MODES.indexOf(explicitMode) !== -1) {
      return explicitMode;
    }
    return placeholder.dataset.editable === 'true' ? 'adhoc' : 'readonly';
  }

  function requestBodyAllowed(request) {
    return request.bodyEditable && methodAllowsBody(request.method, request);
  }

  function bodyForRequest(request) {
    return requestBodyAllowed(request) ? request.body : '';
  }

  function todoIdFromLocation(location) {
    if (!location) {
      return '';
    }
    const match = pathFromUrl(location).match(/\/todos\/([^/?#]+)$/);
    return match ? match[1] : '';
  }

  function createdTodoIdFromResponse(request, response) {
    if (request.method !== 'POST' || pathFromUrl(request.url) !== '/todos' || response.status !== 201) {
      return '';
    }
    return response.headers.get('X-Thing-Instance-Primary-Key')
      || todoIdFromLocation(response.headers.get('Location'));
  }

  function simpleApiItemIdFromLocation(location) {
    if (!location) {
      return '';
    }
    const match = pathFromUrl(location).match(/\/simpleapi\/items\/([^/?#]+)$/);
    return match ? match[1] : '';
  }

  function createdSimpleApiItemIdFromResponse(request, response) {
    if (request.method !== 'POST'
        || pathFromUrl(request.url) !== '/simpleapi/items'
        || response.status !== 201) {
      return '';
    }
    return response.headers.get('X-Thing-Instance-Primary-Key')
      || simpleApiItemIdFromLocation(response.headers.get('Location'));
  }

  function createdSimpleApiItemIsbnFromRequest(request, response) {
    if (request.method !== 'POST'
        || pathFromUrl(request.url) !== '/simpleapi/items'
        || response.status !== 201) {
      return '';
    }
    try {
      const body = JSON.parse(request.body || '{}');
      return body.isbn13 || '';
    } catch (ignored) {
      return '';
    }
  }

  function escapeShellSingleQuotes(value) {
    return value.replace(/'/g, "'\"'\"'");
  }

  function setCookie(name, value, days) {
    let expires = '';
    if (days !== undefined) {
      const expiresAt = new Date();
      expiresAt.setTime(expiresAt.getTime() + (days * 24 * 60 * 60 * 1000));
      expires = `expires=${expiresAt.toUTCString()};`;
    }
    document.cookie = `${name}=${encodeURIComponent(value)};${expires}path=/`;
  }

  function getCookie(name) {
    const cookieName = `${name}=`;
    const cookies = decodeURIComponent(document.cookie || '').split(';');
    for (let index = 0; index < cookies.length; index += 1) {
      let cookie = cookies[index];
      while (cookie.charAt(0) === ' ') {
        cookie = cookie.substring(1);
      }
      if (cookie.indexOf(cookieName) === 0) {
        return cookie.substring(cookieName.length);
      }
    }
    return '';
  }

  function browserPlatform() {
    if (navigator.userAgentData && navigator.userAgentData.platform) {
      return navigator.userAgentData.platform;
    }
    return navigator.userAgent || '';
  }

  function isWindowsPlatform() {
    return /(windows|win32|win64|wow64)/.test(browserPlatform().toLowerCase());
  }

  function useCurlExePreference() {
    const storedPreference = getCookie(USE_CURL_EXE_COOKIE).toLowerCase();
    if (storedPreference === 'true') {
      return true;
    }
    if (storedPreference === 'false') {
      return false;
    }
    return isWindowsPlatform();
  }

  function setUseCurlExePreference(useCurlExe) {
    setCookie(USE_CURL_EXE_COOKIE, useCurlExe ? 'true' : 'false', 365);
  }

  function curlExecutable() {
    return useCurlExePreference() ? 'curl.exe' : 'curl';
  }

  function refreshCurlCommands(useCurlExe) {
    document.querySelectorAll('.sim-live-curl-exe-checkbox').forEach(function (checkbox) {
      checkbox.checked = useCurlExe;
    });
    renderedWidgets.forEach(function (widgetState) {
      widgetState.updateCommands();
    });
  }

  function currentChallenger() {
    return getCookie(CHALLENGER_COOKIE) || getCookie(LEGACY_CHALLENGER_COOKIE);
  }

  function oversizedChallengerValue(challenger) {
    const prefix = challenger || 'x'.repeat(36);
    return `${prefix}${'x'.repeat(Math.max(101 - prefix.length, 0))}`;
  }

  function authTokenStorageKey(challenger) {
    return `apichallenges.${challenger}.xAuthToken`;
  }

  function readOnlyAuthTokenStorageKey() {
    return 'apichallenges.readonly.xAuthToken';
  }

  function authTokenStorageKeyForRequest(request) {
    if (request && request.useChallenger === false) {
      return readOnlyAuthTokenStorageKey();
    }
    const challenger = currentChallenger();
    return challenger ? authTokenStorageKey(challenger) : readOnlyAuthTokenStorageKey();
  }

  function lastCreatedTodoStorageKey(challenger) {
    return `apichallenges.${challenger}.lastCreatedTodoId`;
  }

  function lastCreatedSimpleApiItemIdStorageKey() {
    return 'apichallenges.simpleapi.lastCreatedItemId';
  }

  function lastCreatedSimpleApiItemIsbnStorageKey() {
    return 'apichallenges.simpleapi.lastCreatedItemIsbn';
  }

  function currentAuthToken(request) {
    return localStorage.getItem(authTokenStorageKeyForRequest(request)) || '';
  }

  function currentLastCreatedTodoId() {
    const challenger = currentChallenger();
    if (!challenger) {
      return '1';
    }
    return localStorage.getItem(lastCreatedTodoStorageKey(challenger)) || '1';
  }

  function currentLastCreatedSimpleApiItemId() {
    return localStorage.getItem(lastCreatedSimpleApiItemIdStorageKey()) || '1';
  }

  function currentLastCreatedSimpleApiItemIsbn() {
    return localStorage.getItem(lastCreatedSimpleApiItemIsbnStorageKey()) || '1234567890123';
  }

  function storeChallenger(challenger) {
    if (!challenger || challenger.toUpperCase().indexOf('UNKNOWN CHALLENGER') === 0) {
      return;
    }
    setCookie(CHALLENGER_COOKIE, challenger, 365);
    setCookie(LEGACY_CHALLENGER_COOKIE, challenger, 365);
  }

  function storeLastCreatedTodoId(todoId) {
    const challenger = currentChallenger();
    if (!challenger || !todoId) {
      return;
    }
    localStorage.setItem(lastCreatedTodoStorageKey(challenger), String(todoId));
  }

  function storeLastCreatedSimpleApiItemId(itemId) {
    if (!itemId) {
      return;
    }
    localStorage.setItem(lastCreatedSimpleApiItemIdStorageKey(), String(itemId));
  }

  function storeLastCreatedSimpleApiItemIsbn(isbn) {
    if (!isbn) {
      return;
    }
    localStorage.setItem(lastCreatedSimpleApiItemIsbnStorageKey(), String(isbn));
  }

  function storeAuthToken(token, request) {
    if (!token) {
      return;
    }
    localStorage.setItem(authTokenStorageKeyForRequest(request), token);
  }

  function normalizeHeaderName(name) {
    return (name || '').toLowerCase();
  }

  function cloneHeaders(headers) {
    return headers.map(function (header) {
      return { name: header.name, value: header.value };
    });
  }

  function findHeader(headers, name) {
    const normalizedName = normalizeHeaderName(name);
    return headers.find(function (header) {
      return normalizeHeaderName(header.name) === normalizedName;
    });
  }

  function upsertHeader(headers, name, value) {
    const existing = findHeader(headers, name);
    if (existing) {
      existing.value = value;
      return;
    }
    headers.push({ name: name, value: value });
  }

  function parseHeaders(value) {
    if (!value) {
      return [];
    }
    return value.split('||')
      .map(function (line) {
        return line.trim();
      })
      .filter(function (line) {
        return line.length > 0;
      })
      .map(function (line) {
        const separator = line.indexOf(':');
        if (separator === -1) {
          return null;
        }
        return {
          name: line.substring(0, separator).trim(),
          value: line.substring(separator + 1).trim(),
        };
      })
      .filter(function (header) {
        return header && header.name.length > 0;
      });
  }

  function headersToEditableText(headers) {
    return headers.map(function (header) {
      return `${header.name}: ${header.value}`;
    }).join('\n');
  }

  function parseEditableHeaders(value) {
    return value.split(/\r?\n/)
      .map(function (line) {
        return line.trim();
      })
      .filter(function (line) {
        return line.length > 0;
      })
      .map(function (line) {
        const separator = line.indexOf(':');
        if (separator === -1) {
          return null;
        }
        return {
          name: line.substring(0, separator).trim(),
          value: line.substring(separator + 1).trim(),
        };
      })
      .filter(function (header) {
        return header && header.name.length > 0;
      });
  }

  function headersAsObject(headers) {
    const object = {};
    headers.forEach(function (header) {
      object[header.name] = header.value;
    });
    return object;
  }

  function browserRequestHeaders(headers) {
    const object = headersAsObject(headers);
    object[LIVE_WIDGET_HEADER] = 'true';
    return object;
  }

  function defaultHeadersFor(request, placeholder) {
    const headers = placeholder.hasAttribute('data-headers')
      ? parseHeaders(placeholder.dataset.headers)
      : [{ name: 'Accept', value: placeholder.dataset.accept || 'application/json' }];

    if (placeholder.dataset.accept && !findHeader(headers, 'Accept')) {
      headers.push({ name: 'Accept', value: placeholder.dataset.accept });
    }

    request.autoChallengerHeader = request.useChallenger && !findHeader(headers, 'X-CHALLENGER');
    if (request.autoChallengerHeader) {
      upsertHeader(headers, 'X-CHALLENGER', currentChallenger());
    }

    if (request.body) {
      const contentType = placeholder.dataset.contentType || 'application/json';
      if (contentType.toLowerCase() !== 'none' && !findHeader(headers, 'Content-Type')) {
        headers.push({ name: 'Content-Type', value: contentType });
      }
    }

    return headers;
  }

  function buildCurlCommand(request) {
    const parts = [`${curlExecutable()} -i`, '-X', request.method, `"${readableUrl(request.url)}"`];
    request.headers.forEach(function (header) {
      parts.push(`-H "${header.name}: ${header.value}"`);
    });
    const body = bodyForRequest(request);
    if (body) {
      parts.push(`--data '${escapeShellSingleQuotes(body)}'`);
    }
    return parts.join(' ');
  }

  function buildWgetCommand(request) {
    const parts = [
      'wget -S -O -',
      `--method=${request.method}`,
    ];
    request.headers.forEach(function (header) {
      parts.push(`--header="${header.name}: ${header.value}"`);
    });
    const body = bodyForRequest(request);
    if (body) {
      parts.push(`--body-data='${escapeShellSingleQuotes(body)}'`);
    }
    parts.push(`"${readableUrl(request.url)}"`);
    return parts.join(' ');
  }

  function buildRestrictedCommand(request, builder) {
    const validation = validateRequestTarget(request);
    if (!validation.valid) {
      return `${validation.message} Fix the request target before copying a command.`;
    }
    return builder(request);
  }

  function copyText(value, button) {
    const copiedText = 'Copied';
    const originalText = button.textContent;
    const markCopied = function () {
      button.textContent = copiedText;
      window.setTimeout(function () {
        button.textContent = originalText;
      }, 1400);
    };

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(value).then(markCopied);
      return;
    }

    const fallback = document.createElement('textarea');
    fallback.value = value;
    fallback.setAttribute('readonly', 'readonly');
    fallback.style.position = 'absolute';
    fallback.style.left = '-9999px';
    document.body.appendChild(fallback);
    fallback.select();
    document.execCommand('copy');
    document.body.removeChild(fallback);
    markCopied();
  }

  function activateTab(widget, tabName, groupSelector, panelSelector) {
    widget.querySelectorAll(groupSelector).forEach(function (tab) {
      const selected = tab.dataset.tab === tabName;
      tab.classList.toggle('active', selected);
      tab.setAttribute('aria-selected', selected ? 'true' : 'false');
    });
    widget.querySelectorAll(panelSelector).forEach(function (panel) {
      panel.hidden = panel.dataset.panel !== tabName;
    });
  }

  function isJsonContentType(contentType) {
    return contentType && contentType.toLowerCase().includes('json');
  }

  function isXmlContentType(contentType) {
    return contentType && contentType.toLowerCase().includes('xml');
  }

  function bodyLooksLikeJson(text) {
    const trimmedBody = text.trim();
    return trimmedBody.startsWith('{') || trimmedBody.startsWith('[');
  }

  function bodyLooksLikeXml(text) {
    return text.trim().startsWith('<');
  }

  function formatXml(text) {
    if (!text || !bodyLooksLikeXml(text) || typeof DOMParser === 'undefined') {
      return text;
    }

    const parsed = new DOMParser().parseFromString(text, 'application/xml');
    if (parsed.getElementsByTagName('parsererror').length > 0) {
      return text;
    }

    const serialized = new XMLSerializer().serializeToString(parsed);
    const lines = serialized.replace(/>\s*</g, '>\n<').split('\n');
    let indent = 0;
    return lines.map(function (line) {
      const trimmed = line.trim();
      if (!trimmed) {
        return '';
      }
      if (/^<\//.test(trimmed)) {
        indent = Math.max(indent - 1, 0);
      }
      const formatted = `${'  '.repeat(indent)}${trimmed}`;
      if (
        /^<[^!?/][^>]*[^/]?>$/.test(trimmed)
        && !/^<[^>]+>.*<\/[^>]+>$/.test(trimmed)
      ) {
        indent += 1;
      }
      return formatted;
    }).filter(function (line) {
      return line.length > 0;
    }).join('\n');
  }

  function formatBody(text, contentType) {
    if (!text) {
      return '(no response body)';
    }

    if (isJsonContentType(contentType)) {
      try {
        return JSON.stringify(JSON.parse(text), null, 2);
      } catch (ignored) {
        return text;
      }
    }

    if (isXmlContentType(contentType) || bodyLooksLikeXml(text)) {
      return formatXml(text);
    }

    return text;
  }

  function requestContentType(request) {
    const contentTypeHeader = findHeader(request.headers || [], 'Content-Type');
    return contentTypeHeader ? contentTypeHeader.value.toLowerCase() : '';
  }

  function methodUnsupportedByBrowser(method) {
    return BROWSER_UNSUPPORTED_METHODS.indexOf((method || '').trim().toUpperCase()) !== -1;
  }

  function browserUnsupportedMethodMessage(method) {
    return `${method} cannot be sent from the In Browser tab because browser JavaScript `
      + 'blocks this HTTP method. Use the cURL or wget tabs, or another API client.';
  }

  function unsupportedMethodOverrideHeader(request) {
    return (request.headers || []).find(function (header) {
      return BROWSER_UNSUPPORTED_METHOD_OVERRIDE_HEADERS.indexOf(
        normalizeHeaderName(header.name)) !== -1
        && methodUnsupportedByBrowser(header.value);
    });
  }

  function browserUnsupportedRequestMessage(request) {
    if (methodUnsupportedByBrowser(request.method)) {
      return browserUnsupportedMethodMessage(request.method);
    }

    const overrideHeader = unsupportedMethodOverrideHeader(request);
    if (overrideHeader) {
      return `${overrideHeader.name}: ${overrideHeader.value} cannot be sent from the `
        + 'In Browser tab because browser JavaScript blocks method override headers '
        + 'for CONNECT, TRACE, and TRACK. Use the cURL or wget tabs, or another API client.';
    }

    return '';
  }

  function formatRequestBody(request) {
    if (!request.body || request.body.indexOf('{{') !== -1) {
      return request.body;
    }
    const contentType = requestContentType(request);
    if (isJsonContentType(contentType) || (!contentType && bodyLooksLikeJson(request.body))) {
      try {
        return JSON.stringify(JSON.parse(request.body), null, 2);
      } catch (error) {
        return request.body;
      }
    }
    if (isXmlContentType(contentType) || bodyLooksLikeXml(request.body)) {
      return formatXml(request.body);
    }
    return request.body;
  }

  function responseHeadersToText(response) {
    const headerLines = [];
    response.headers.forEach(function (value, name) {
      headerLines.push(`${name}: ${value}`);
    });

    if (headerLines.length === 0) {
      return '(no response headers available)';
    }

    return headerLines.sort().join('\n');
  }

  function rawResponseToText(response, bodyText) {
    const statusText = response.statusText ? ` ${response.statusText}` : '';
    const headersText = responseHeadersToText(response);
    const exposedHeaders = headersText === '(no response headers available)' ? '' : headersText;
    const rawBody = bodyText || '';
    return [`HTTP ${response.status}${statusText}`, exposedHeaders, rawBody].join('\n').trimEnd();
  }

  function showChallengeFeedback(feedback, passed) {
    if (!feedback) {
      return;
    }
    if (feedback.hideTimer) {
      window.clearTimeout(feedback.hideTimer);
    }
    feedback.element.textContent = passed ? 'Challenge Passed' : 'Challenge Not Passed Yet';
    feedback.element.className = passed
      ? 'sim-live-challenge-feedback is-passed'
      : 'sim-live-challenge-feedback is-not-passed';
    feedback.element.hidden = false;
    feedback.hideTimer = window.setTimeout(function () {
      feedback.element.hidden = true;
    }, 10000);
  }

  function showChallengeFireworks() {
    if (window.matchMedia
        && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    const previousFireworks = document.querySelector('.sim-live-fireworks');
    if (previousFireworks) {
      previousFireworks.remove();
    }

    const overlay = document.createElement('div');
    overlay.className = 'sim-live-fireworks';
    overlay.setAttribute('aria-hidden', 'true');

    const colors = [
      '#18a058',
      '#f59e0b',
      '#2563eb',
      '#db2777',
      '#7c3aed',
      '#ef4444',
      '#06b6d4',
      '#facc15',
    ];
    const bursts = [
      { x: 18, y: 24, delay: 0, size: 1.15 },
      { x: 78, y: 20, delay: 180, size: 1.1 },
      { x: 48, y: 30, delay: 360, size: 1.35 },
      { x: 28, y: 52, delay: 560, size: 1 },
      { x: 70, y: 50, delay: 760, size: 1.2 },
      { x: 52, y: 66, delay: 980, size: 0.95 },
    ];

    bursts.forEach(function (burst, burstIndex) {
      const burstElement = document.createElement('span');
      burstElement.className = 'sim-live-firework-burst';
      burstElement.style.setProperty('--x', `${burst.x}vw`);
      burstElement.style.setProperty('--y', `${burst.y}vh`);
      burstElement.style.setProperty('--delay', `${burst.delay}ms`);
      burstElement.style.setProperty('--size', burst.size);

      const ring = document.createElement('span');
      ring.className = 'sim-live-firework-ring';
      ring.style.setProperty('--color', colors[burstIndex % colors.length]);
      ring.style.setProperty('--delay', `${burst.delay}ms`);
      burstElement.appendChild(ring);

      for (let index = 0; index < 28; index += 1) {
        const spark = document.createElement('span');
        const angle = ((Math.PI * 2) / 28) * index;
        const distance = 86 + ((index + burstIndex) % 7) * 13;
        spark.className = 'sim-live-firework-spark';
        spark.style.setProperty('--angle', `${angle}rad`);
        spark.style.setProperty('--distance', `${distance}px`);
        spark.style.setProperty('--color', colors[(index + burstIndex) % colors.length]);
        spark.style.setProperty('--delay', `${burst.delay}ms`);
        burstElement.appendChild(spark);
      }

      overlay.appendChild(burstElement);
    });

    for (let index = 0; index < 90; index += 1) {
      const confetti = document.createElement('span');
      confetti.className = 'sim-live-firework-confetti';
      confetti.style.setProperty('--left', `${(index * 37) % 100}vw`);
      confetti.style.setProperty('--delay', `${260 + ((index * 53) % 1600)}ms`);
      confetti.style.setProperty('--duration', `${2200 + ((index * 41) % 1100)}ms`);
      confetti.style.setProperty('--drift', `${-80 + ((index * 29) % 160)}px`);
      confetti.style.setProperty('--spin', `${360 + ((index * 43) % 540)}deg`);
      confetti.style.setProperty('--color', colors[index % colors.length]);
      overlay.appendChild(confetti);
    }

    document.body.appendChild(overlay);
    window.setTimeout(function () {
      overlay.remove();
    }, 4600);
  }

  function showChallengeCompletedBanner(challengeId) {
    if (!challengeId) {
      return;
    }
    document.querySelectorAll('.solution-challenge-completed[data-challenge-id]')
      .forEach(function (banner) {
        if (banner.dataset.challengeId === String(challengeId)) {
          banner.hidden = false;
        }
      });
  }

  function dispatchChallengePassedEvent(challengeId) {
    if (!challengeId) {
      return;
    }
    window.dispatchEvent(new CustomEvent('apiChallenges:challenge-passed', {
      detail: { challengeId: String(challengeId) },
    }));
  }

  function hideChallengeCompletedBanner(challengeId) {
    if (!challengeId) {
      return;
    }
    document.querySelectorAll('.solution-challenge-completed[data-challenge-id]')
      .forEach(function (banner) {
        if (banner.dataset.challengeId === String(challengeId)) {
          banner.hidden = true;
        }
      });
  }

  function updateChallengeCompletedBanners() {
    document.querySelectorAll('.solution-challenge-completed[data-challenge-id]')
      .forEach(function (banner) {
        const challengeId = banner.dataset.challengeId || '';
        if (!challengeId) {
          return;
        }
        checkChallengePassed({ challengeId: challengeId }).then(function (passed) {
          if (passed) {
            showChallengeCompletedBanner(challengeId);
          } else {
            hideChallengeCompletedBanner(challengeId);
          }
        });
      });
  }

  function clearChallengeFeedback(feedback) {
    if (!feedback) {
      return;
    }
    if (feedback.hideTimer) {
      window.clearTimeout(feedback.hideTimer);
    }
    feedback.element.hidden = true;
    feedback.element.textContent = '';
    feedback.element.className = 'sim-live-challenge-feedback';
  }

  function checkChallengePassed(request) {
    const challenger = currentChallenger();
    if (!request.challengeId || !challenger) {
      return Promise.resolve(false);
    }

    return fetch(absoluteUrl(`/gui/challenge-status/${encodeURIComponent(request.challengeId)}`), {
      method: 'GET',
      headers: {
        Accept: 'application/json',
        'X-CHALLENGER': challenger,
        [LIVE_WIDGET_HEADER]: 'true',
      },
    })
      .then(function (response) {
        if (!response.ok) {
          return false;
        }
        return response.json();
      })
      .then(function (json) {
        return json && json.status === true;
      })
      .catch(function () {
        return false;
      });
  }

  function renderCurlExeToggle() {
    const label = document.createElement('label');
    label.className = 'sim-live-curl-exe-toggle';
    label.title = 'Use curl.exe instead of curl in the generated command';

    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.className = 'sim-live-curl-exe-checkbox';
    checkbox.checked = useCurlExePreference();
    checkbox.addEventListener('change', function () {
      setUseCurlExePreference(checkbox.checked);
      refreshCurlCommands(checkbox.checked);
    });

    label.appendChild(checkbox);
    label.appendChild(document.createTextNode('.exe'));
    return label;
  }

  function renderCommandPanel(commandText, options) {
    const panel = document.createElement('div');
    panel.className = 'sim-live-command-panel';
    const commandOptions = options || {};

    const pre = document.createElement('pre');
    pre.className = 'sim-live-command';
    pre.textContent = commandText;

    const copyButton = document.createElement('button');
    copyButton.type = 'button';
    copyButton.className = 'sim-live-copy';
    copyButton.textContent = 'Copy';
    copyButton.addEventListener('click', function () {
      copyText(pre.textContent, copyButton);
    });

    const actions = document.createElement('div');
    actions.className = 'sim-live-command-actions';
    actions.appendChild(copyButton);
    if (commandOptions.curlExeToggle) {
      actions.appendChild(renderCurlExeToggle());
    }

    panel.appendChild(pre);
    panel.appendChild(actions);
    return {
      panel: panel,
      pre: pre,
    };
  }

  function renderResponseArea(widget) {
    const status = document.createElement('div');
    status.className = 'sim-live-status';
    status.textContent = 'Not run yet - click Execute request';

    const responseTabs = document.createElement('div');
    responseTabs.className = 'sim-live-response-tabs';
    responseTabs.setAttribute('role', 'tablist');

    const bodyTab = document.createElement('button');
    bodyTab.type = 'button';
    bodyTab.dataset.tab = 'body';
    bodyTab.className = 'sim-live-response-tab active';
    bodyTab.setAttribute('aria-selected', 'true');
    bodyTab.textContent = 'Body';

    const headersTab = document.createElement('button');
    headersTab.type = 'button';
    headersTab.dataset.tab = 'headers';
    headersTab.className = 'sim-live-response-tab';
    headersTab.setAttribute('aria-selected', 'false');
    headersTab.textContent = 'Headers';

    const rawTab = document.createElement('button');
    rawTab.type = 'button';
    rawTab.dataset.tab = 'raw';
    rawTab.className = 'sim-live-response-tab';
    rawTab.setAttribute('aria-selected', 'false');
    rawTab.textContent = 'Raw';

    responseTabs.appendChild(bodyTab);
    responseTabs.appendChild(headersTab);
    responseTabs.appendChild(rawTab);

    const bodyPanel = document.createElement('pre');
    bodyPanel.className = 'sim-live-response-panel';
    bodyPanel.dataset.panel = 'body';
    bodyPanel.textContent = '(execute the request to see the response body)';

    const headersPanel = document.createElement('pre');
    headersPanel.className = 'sim-live-response-panel';
    headersPanel.dataset.panel = 'headers';
    headersPanel.hidden = true;
    headersPanel.textContent = '(execute the request to see the response headers)';

    const rawPanel = document.createElement('pre');
    rawPanel.className = 'sim-live-response-panel';
    rawPanel.dataset.panel = 'raw';
    rawPanel.hidden = true;
    rawPanel.textContent = '(execute the request to see the raw response)';

    const responseBodyCopyButton = document.createElement('button');
    responseBodyCopyButton.type = 'button';
    responseBodyCopyButton.className = 'sim-live-copy';
    responseBodyCopyButton.textContent = 'Copy body';
    responseBodyCopyButton.addEventListener('click', function () {
      copyText(bodyPanel.textContent, responseBodyCopyButton);
    });

    const responseHeadersCopyButton = document.createElement('button');
    responseHeadersCopyButton.type = 'button';
    responseHeadersCopyButton.className = 'sim-live-copy';
    responseHeadersCopyButton.textContent = 'Copy headers';
    responseHeadersCopyButton.addEventListener('click', function () {
      copyText(headersPanel.textContent, responseHeadersCopyButton);
    });

    const responseRawCopyButton = document.createElement('button');
    responseRawCopyButton.type = 'button';
    responseRawCopyButton.className = 'sim-live-copy';
    responseRawCopyButton.textContent = 'Copy raw';
    responseRawCopyButton.addEventListener('click', function () {
      copyText(rawPanel.textContent, responseRawCopyButton);
    });

    const responseActions = document.createElement('div');
    responseActions.className = 'sim-live-response-actions';
    responseActions.appendChild(responseBodyCopyButton);
    responseActions.appendChild(responseHeadersCopyButton);
    responseActions.appendChild(responseRawCopyButton);

    responseTabs.addEventListener('click', function (event) {
      if (event.target.matches('.sim-live-response-tab')) {
        activateTab(widget, event.target.dataset.tab, '.sim-live-response-tab',
          '.sim-live-response-panel');
      }
    });

    return {
      status: status,
      bodyPanel: bodyPanel,
      headersPanel: headersPanel,
      rawPanel: rawPanel,
      elements: [
        status,
        responseTabs,
        bodyPanel,
        headersPanel,
        rawPanel,
        responseActions,
      ],
    };
  }

  function renderEditableControls(request, defaultRequest, notifyChanged) {
    const controls = document.createElement('div');
    controls.className = 'sim-live-edit-controls';

    let methodLabel = null;
    let methodSelect = null;
    let methodInput = null;
    let urlLabel = null;
    let urlInput = null;
    let queryLabel = null;
    let queryTextarea = null;
    let lockedFields = null;
    if (request.editMode === 'adhoc') {
      methodLabel = document.createElement('label');
      methodLabel.textContent = 'Verb';
      if (request.customMethod) {
        methodSelect = document.createElement('select');
        methodSelect.className = 'sim-live-edit-method';
        appendDefaultMethodOptions(methodSelect);
        const customOption = document.createElement('option');
        customOption.value = CUSTOM_METHOD_VALUE;
        customOption.textContent = 'Custom...';
        methodSelect.appendChild(customOption);
        methodSelect.setAttribute('aria-label', 'HTTP method');
        methodLabel.appendChild(methodSelect);

        methodInput = document.createElement('input');
        methodInput.className = 'sim-live-edit-method sim-live-edit-method-custom';
        methodInput.type = 'text';
        methodInput.placeholder = 'CUSTOM';
        methodInput.value = request.method;
        methodInput.setAttribute('aria-label', 'Custom HTTP method');
        methodLabel.appendChild(methodInput);
      } else {
        methodSelect = document.createElement('select');
        methodSelect.className = 'sim-live-edit-method';
        appendDefaultMethodOptions(methodSelect);
        methodSelect.value = request.method;
        methodLabel.appendChild(methodSelect);
      }

      urlLabel = document.createElement('label');
      urlLabel.textContent = 'URL';
      urlInput = document.createElement('input');
      urlInput.className = 'sim-live-edit-url';
      urlInput.type = 'url';
      urlInput.value = readableUrl(request.url);
      urlLabel.appendChild(urlInput);
    } else {
      lockedFields = document.createElement('div');
      lockedFields.className = 'sim-live-locked-fields';
      lockedFields.textContent = `${request.method} ${pathAndQueryFromUrl(request.url)}`;
      controls.appendChild(lockedFields);

      if (request.queryEditable) {
        queryLabel = document.createElement('label');
        queryLabel.textContent = 'Query string';
        queryTextarea = document.createElement('textarea');
        queryTextarea.className = 'sim-live-edit-query';
        queryTextarea.rows = 2;
        queryTextarea.value = queryFromUrl(request.url);
        queryLabel.appendChild(queryTextarea);
      }
    }

    const headersLabel = document.createElement('label');
    headersLabel.textContent = 'Headers';
    const headersTextarea = document.createElement('textarea');
    headersTextarea.className = 'sim-live-edit-headers';
    headersTextarea.rows = 4;
    headersTextarea.value = headersToEditableText(request.headers);
    headersLabel.appendChild(headersTextarea);

    let bodyTextarea = null;
    let bodyLabel = null;
    let prettyPrintButton = null;
    if (request.bodyEditable) {
      bodyLabel = document.createElement('label');
      bodyLabel.textContent = 'Body';
      bodyTextarea = document.createElement('textarea');
      bodyTextarea.className = 'sim-live-edit-body';
      bodyTextarea.rows = 6;
      bodyTextarea.value = formatRequestBody(request);
      bodyLabel.appendChild(bodyTextarea);

      prettyPrintButton = document.createElement('button');
      prettyPrintButton.type = 'button';
      prettyPrintButton.className = 'sim-live-pretty-print';
      prettyPrintButton.textContent = 'Pretty print body';
      prettyPrintButton.addEventListener('click', function () {
        request.userEdited = true;
        request.headers = parseEditableHeaders(headersTextarea.value);
        request.body = bodyTextarea.value;
        request.body = formatRequestBody(request);
        bodyTextarea.value = request.body;
        syncBodyControlVisibility();
        notifyChanged();
      });
    }

    const resetButton = document.createElement('button');
    resetButton.type = 'button';
    resetButton.className = 'sim-live-reset';
    resetButton.textContent = 'Reset';
    const editActions = document.createElement('div');
    editActions.className = 'sim-live-edit-actions';
    editActions.appendChild(resetButton);
    if (prettyPrintButton) {
      editActions.appendChild(prettyPrintButton);
    }

    function syncBodyControlVisibility() {
      if (!bodyLabel) {
        return;
      }
      const showBody = requestBodyAllowed(request);
      bodyLabel.hidden = !showBody;
      if (prettyPrintButton) {
        prettyPrintButton.hidden = !showBody;
      }
    }

    function syncLockedFields() {
      if (lockedFields) {
        lockedFields.textContent = `${request.method} ${pathAndQueryFromUrl(request.url)}`;
      }
    }

    function syncMethodControlState(forceCustom) {
      if (methodSelect && request.customMethod && methodInput) {
        const customSelected = forceCustom || DEFAULT_METHODS.indexOf(request.method) === -1;
        methodSelect.value = customSelected ? CUSTOM_METHOD_VALUE : request.method;
        methodInput.hidden = !customSelected;
        methodInput.value = request.method;
        return;
      }
      if (methodSelect) {
        methodSelect.value = request.method;
      }
      if (methodInput) {
        methodInput.value = request.method;
      }
    }

    function syncRequestFromControls() {
      request.userEdited = true;
      const selectedCustomMethod = methodSelect
        && request.customMethod
        && methodSelect.value === CUSTOM_METHOD_VALUE
        && methodInput;
      if (methodSelect) {
        if (selectedCustomMethod) {
          request.method = normalizeMethod(methodInput.value);
          methodInput.value = request.method;
        } else {
          request.method = normalizeMethod(methodSelect.value);
        }
      } else if (methodInput) {
        request.method = normalizeMethod(methodInput.value);
        methodInput.value = request.method;
      }
      if (urlInput) {
        request.url = absoluteUrl(urlInput.value);
        urlInput.value = readableUrl(request.url);
      } else if (queryTextarea) {
        request.url = urlWithQuery(request.url, queryTextarea.value);
      }
      request.headers = parseEditableHeaders(headersTextarea.value);
      if (bodyTextarea) {
        request.body = bodyTextarea.value;
      }
      syncLockedFields();
      syncMethodControlState(selectedCustomMethod);
      syncBodyControlVisibility();
      notifyChanged();
    }

    if (methodSelect) {
      methodSelect.addEventListener('change', function () {
        syncRequestFromControls();
        if (request.customMethod
            && methodSelect.value === CUSTOM_METHOD_VALUE
            && methodInput) {
          methodInput.focus();
          methodInput.select();
        }
      });
    }
    if (methodInput) {
      methodInput.addEventListener('change', syncRequestFromControls);
      methodInput.addEventListener('blur', syncRequestFromControls);
    }
    if (urlInput) {
      urlInput.addEventListener('change', syncRequestFromControls);
    }
    if (queryTextarea) {
      queryTextarea.addEventListener('input', syncRequestFromControls);
    }
    headersTextarea.addEventListener('input', syncRequestFromControls);
    if (bodyTextarea) {
      bodyTextarea.addEventListener('input', syncRequestFromControls);
    }
    resetButton.addEventListener('click', function () {
      request.userEdited = false;
      request.method = defaultRequest.method;
      request.url = defaultRequest.url;
      request.body = defaultRequest.body;
      request.headers = cloneHeaders(defaultRequest.headers);
      syncMethodControlState();
      if (urlInput) {
        urlInput.value = readableUrl(request.url);
      }
      if (queryTextarea) {
        queryTextarea.value = queryFromUrl(request.url);
      }
      headersTextarea.value = headersToEditableText(request.headers);
      if (bodyTextarea) {
        bodyTextarea.value = formatRequestBody(request);
      }
      syncLockedFields();
      syncBodyControlVisibility();
      notifyChanged();
    });

    if (methodLabel) {
      controls.appendChild(methodLabel);
    }
    if (urlLabel) {
      controls.appendChild(urlLabel);
    }
    if (queryLabel) {
      controls.appendChild(queryLabel);
    }
    controls.appendChild(headersLabel);
    if (bodyLabel) {
      controls.appendChild(bodyLabel);
    }
    controls.appendChild(editActions);
    syncMethodControlState();
    syncBodyControlVisibility();
    return {
      element: controls,
      methodSelect: methodSelect,
      methodInput: methodInput,
      urlInput: urlInput,
      headersTextarea: headersTextarea,
      bodyTextarea: bodyTextarea,
      queryTextarea: queryTextarea,
      syncMethodControlState: syncMethodControlState,
      syncLockedFields: syncLockedFields,
      syncBodyControlVisibility: syncBodyControlVisibility,
    };
  }

  function widgetFetchHeaders(challenger) {
    const headers = { Accept: 'application/json' };
    if (challenger) {
      headers['X-CHALLENGER'] = challenger;
    }
    return headers;
  }

  function fetchTodosForCurrentChallenger() {
    return fetch(absoluteUrl('/todos'), {
      method: 'GET',
      headers: widgetFetchHeaders(currentChallenger()),
    }).then(function (response) {
      if (!response.ok) {
        return [];
      }
      return response.json().then(function (json) {
        return json.todos || [];
      });
    }).catch(function () {
      return [];
    });
  }

  function fetchText(path) {
    const challenger = currentChallenger();
    if (!challenger) {
      return Promise.resolve('{}');
    }
    return fetch(absoluteUrl(path), {
      method: 'GET',
      headers: widgetFetchHeaders(challenger),
    }).then(function (response) {
      if (!response.ok) {
        return '{}';
      }
      return response.text();
    }).catch(function () {
      return '{}';
    });
  }

  function randomSimpleApiIsbn() {
    return fetch(absoluteUrl('/simpleapi/randomisbn'), {
      method: 'GET',
      headers: { Accept: 'text/plain' },
    }).then(function (response) {
      if (!response.ok) {
        return '123-4-56-789012-3';
      }
      return response.text();
    }).then(function (text) {
      return text.trim() || '123-4-56-789012-3';
    }).catch(function () {
      return '123-4-56-789012-3';
    });
  }

  function buildSimpleApiRandomIsbnDetails() {
    const details = document.createElement('details');
    details.className = 'simpleapi-random-isbn-details';

    const summary = document.createElement('summary');
    summary.textContent = 'Generate Random SimpleAPI ISBN';
    details.appendChild(summary);

    const form = document.createElement('form');
    form.className = 'simpleapi-random-isbn-form';
    form.setAttribute('onsubmit', 'return false;');

    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = 'Generate Random ISBN';
    form.appendChild(button);

    const output = document.createElement('input');
    output.setAttribute('data-simpleapi-random-isbn', '');
    output.setAttribute('aria-label', 'Generated SimpleAPI ISBN');
    output.setAttribute('readonly', 'readonly');
    form.appendChild(output);

    details.appendChild(form);
    enhanceSimpleApiRandomIsbnDetails(details);
    return details;
  }

  function enhanceSimpleApiRandomIsbnDetails(details) {
    if (!details || details.dataset.simpleApiRandomIsbnEnhanced === 'true') {
      return;
    }

    let form = details.querySelector('.simpleapi-random-isbn-form');
    if (!form) {
      form = document.createElement('form');
      form.className = 'simpleapi-random-isbn-form';
      details.appendChild(form);
    }
    form.addEventListener('submit', function (event) {
      event.preventDefault();
    });

    let button = form.querySelector('button');
    if (!button) {
      button = document.createElement('button');
      button.textContent = 'Generate Random ISBN';
      form.insertBefore(button, form.firstChild);
    }
    button.type = 'button';

    let output = form.querySelector('[data-simpleapi-random-isbn]');
    if (!output) {
      output = document.createElement('input');
      output.setAttribute('data-simpleapi-random-isbn', '');
      form.appendChild(output);
    }
    output.setAttribute('aria-label', 'Generated SimpleAPI ISBN');
    output.setAttribute('readonly', 'readonly');

    button.addEventListener('click', function () {
      button.disabled = true;
      randomSimpleApiIsbn().then(function (isbn) {
        output.value = isbn;
        output.focus();
        output.select();
      }).finally(function () {
        button.disabled = false;
      });
    });

    details.dataset.simpleApiRandomIsbnEnhanced = 'true';
  }

  function enhanceSimpleApiRandomIsbnDetailsAll() {
    document.querySelectorAll('.simpleapi-random-isbn-details')
      .forEach(enhanceSimpleApiRandomIsbnDetails);
  }

  function currentChallengerJson() {
    const challenger = currentChallenger();
    if (!challenger) {
      return Promise.resolve('{}');
    }
    return fetchText(`/challenger/${challenger}`);
  }

  function currentTodosJson() {
    const challenger = currentChallenger();
    if (!challenger) {
      return Promise.resolve('{}');
    }
    return fetchText(`/challenger/database/${challenger}`);
  }

  function mismatchedChallenger() {
    const challenger = currentChallenger();
    if (!challenger || challenger.length === 0) {
      return '00000000-0000-4000-8000-000000000001';
    }
    const lastCharacter = challenger.charAt(challenger.length - 1);
    const replacement = lastCharacter === '1' ? '2' : '1';
    return `${challenger.substring(0, challenger.length - 1)}${replacement}`;
  }

  function restoredChallenger() {
    return mismatchedChallenger();
  }

  function currentChallengerJsonForRestoredChallenger() {
    const challenger = currentChallenger();
    const restored = restoredChallenger();
    if (!challenger) {
      return Promise.resolve('{}');
    }
    return currentChallengerJson().then(function (text) {
      try {
        const json = JSON.parse(text);
        json.xChallenger = restored;
        return JSON.stringify(json, null, 2);
      } catch (ignored) {
        return text.split(challenger).join(restored);
      }
    });
  }

  function createTodoForCurrentChallenger() {
    return fetch(absoluteUrl('/todos'), {
      method: 'POST',
      headers: Object.assign(widgetFetchHeaders(currentChallenger()), {
        'Content-Type': 'application/json',
      }),
      body: JSON.stringify({
        title: 'solution widget todo',
        doneStatus: false,
        description: '',
      }),
    }).then(function (response) {
      if (!response.ok) {
        return '1';
      }
      return response.json().then(function (json) {
        return String(json.id || '1');
      });
    }).catch(function () {
      return '1';
    });
  }

  function firstTodoId(request) {
    return fetchTodosForCurrentChallenger().then(function (todos) {
      if (todos.length > 0) {
        return String(todos[0].id);
      }
      if (request.autoCreateFirstTodo) {
        return createTodoForCurrentChallenger();
      }
      return '1';
    });
  }

  function missingTodoId() {
    return fetchTodosForCurrentChallenger().then(function (todos) {
      const maxId = todos.reduce(function (maximum, todo) {
        return Math.max(maximum, Number(todo.id) || 0);
      }, 0);
      return String(Math.max(9999, maxId + 1000));
    });
  }

  function usesPlaceholder(request, name) {
    const placeholder = `{{${name}}}`;
    const rawHeaders = headersToEditableText(request.rawHeaders);
    return request.rawPath.indexOf(placeholder) !== -1
      || request.rawBody.indexOf(placeholder) !== -1
      || rawHeaders.indexOf(placeholder) !== -1;
  }

  function dynamicValues(request) {
    const challenger = currentChallenger();
    return Promise.all([
      usesPlaceholder(request, 'firstTodoId') ? firstTodoId(request) : Promise.resolve(''),
      usesPlaceholder(request, 'missingTodoId') ? missingTodoId() : Promise.resolve(''),
      usesPlaceholder(request, 'currentChallengerJson')
        ? currentChallengerJson()
        : Promise.resolve(''),
      usesPlaceholder(request, 'currentTodosJson') ? currentTodosJson() : Promise.resolve(''),
      usesPlaceholder(request, 'lastCreatedTodoId')
        ? Promise.resolve(currentLastCreatedTodoId())
        : Promise.resolve(''),
      usesPlaceholder(request, 'currentChallengerJsonForRestoredChallenger')
        ? currentChallengerJsonForRestoredChallenger()
        : Promise.resolve(''),
      usesPlaceholder(request, 'randomSimpleApiIsbn')
        ? randomSimpleApiIsbn()
        : Promise.resolve(''),
      usesPlaceholder(request, 'lastCreatedSimpleApiItemId')
        ? Promise.resolve(currentLastCreatedSimpleApiItemId())
        : Promise.resolve(''),
      usesPlaceholder(request, 'lastCreatedSimpleApiItemIsbn')
        ? Promise.resolve(currentLastCreatedSimpleApiItemIsbn())
        : Promise.resolve(''),
    ]).then(function (values) {
      return {
        currentChallenger: challenger,
        mismatchedChallenger: mismatchedChallenger(),
        restoredChallenger: restoredChallenger(),
        authToken: currentAuthToken(request),
        firstTodoId: values[0],
        missingTodoId: values[1],
        currentChallengerJson: values[2],
        currentTodosJson: values[3],
        lastCreatedTodoId: values[4],
        currentChallengerJsonForRestoredChallenger: values[5],
        randomSimpleApiIsbn: values[6],
        lastCreatedSimpleApiItemId: values[7],
        lastCreatedSimpleApiItemIsbn: values[8],
        oversizedChallenger: oversizedChallengerValue(challenger),
        title50: '2*4*6*8*11*14*17*20*23*26*29*32*35*38*41*44*47*50*',
        title51: '*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*',
        description200: '*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*54*57*60*63*66*69*72*75*78*81*84*87*90*93*96*100*104*108*112*116*120*124*128*132*136*140*144*148*152*156*160*164*168*172*176*180*184*188*192*196*200*',
        description201: '*3*5*7*10*13*16*19*22*25*28*31*34*37*40*43*46*49*52*55*58*61*64*67*70*73*76*79*82*85*88*91*94*97*101*105*109*113*117*121*125*129*133*137*141*145*149*153*157*161*165*169*173*177*181*185*189*193*197*201*',
        description5000: 'D'.repeat(5001),
      };
    });
  }

  function replacePlaceholders(value, values) {
    let replaced = value || '';
    Object.keys(values).forEach(function (key) {
      replaced = replaced.split(`{{${key}}}`).join(values[key]);
    });
    return replaced;
  }

  function resolveDynamicRequest(request) {
    return dynamicValues(request).then(function (values) {
      request.url = absoluteUrl(replacePlaceholders(request.rawPath, values));
      request.body = replacePlaceholders(request.rawBody, values);
      request.headers = cloneHeaders(request.rawHeaders).map(function (header) {
        return {
          name: header.name,
          value: replacePlaceholders(header.value, values),
        };
      });
      if (request.autoChallengerHeader) {
        upsertHeader(request.headers, 'X-CHALLENGER', values.currentChallenger);
      }
      request.body = formatRequestBody(request);
    });
  }

  function renderBrowserPanel(widget, request, defaultRequest, notifyChanged) {
    const panel = document.createElement('div');
    panel.className = 'sim-live-browser-panel';

    const requestLine = document.createElement('div');
    requestLine.className = 'sim-live-request-line';

    const method = document.createElement('strong');
    method.className = 'sim-live-method';
    method.textContent = request.method;

    const url = document.createElement('code');
    url.textContent = readableUrl(request.url);

    requestLine.appendChild(method);
    requestLine.appendChild(url);
    panel.appendChild(requestLine);

    const validationMessage = document.createElement('div');
    validationMessage.className = 'sim-live-validation';
    validationMessage.setAttribute('role', 'status');
    validationMessage.hidden = true;
    panel.appendChild(validationMessage);

    function refreshRequestDisplay() {
      method.textContent = request.method;
      url.textContent = readableUrl(request.url);
      const validation = validateRequestTarget(request);
      validationMessage.textContent = validation.message;
      validationMessage.hidden = validation.valid;
    }

    let controls = null;
    if (request.editable) {
      controls = renderEditableControls(request, defaultRequest, function () {
        refreshRequestDisplay();
        notifyChanged();
      });
      panel.appendChild(controls.element);
    }

    refreshRequestDisplay();

    if (request.body && !requestBodyAllowed(request)) {
      const body = document.createElement('pre');
      body.className = 'sim-live-request-body';
      body.textContent = request.body;
      panel.appendChild(body);
    }

    const executeRow = document.createElement('div');
    executeRow.className = 'sim-live-execute-row';

    const executeButton = document.createElement('button');
    executeButton.type = 'button';
    executeButton.className = 'sim-live-execute';
    const executeIcon = document.createElement('span');
    executeIcon.className = 'sim-live-execute-icon';
    executeIcon.setAttribute('aria-hidden', 'true');
    executeIcon.textContent = '▶';
    executeButton.appendChild(executeIcon);
    executeButton.appendChild(document.createTextNode('Execute request'));
    executeRow.appendChild(executeButton);

    let challengeFeedback = null;
    if (request.challengeId) {
      const feedbackElement = document.createElement('span');
      feedbackElement.className = 'sim-live-challenge-feedback';
      feedbackElement.setAttribute('role', 'status');
      feedbackElement.hidden = true;
      challengeFeedback = {
        element: feedbackElement,
        hideTimer: null,
      };
      executeRow.appendChild(feedbackElement);
    }
    panel.appendChild(executeRow);

    const responseArea = renderResponseArea(widget);
    responseArea.elements.forEach(function (element) {
      panel.appendChild(element);
    });

    executeButton.addEventListener('click', function () {
      executeButton.disabled = true;
      let wasChallengePassedBeforeRequest = false;
      clearChallengeFeedback(challengeFeedback);
      responseArea.status.textContent = 'Running...';
      responseArea.bodyPanel.textContent = '';
      responseArea.headersPanel.textContent = '';
      responseArea.rawPanel.textContent = '';

      Promise.resolve()
        .then(function () {
          const unresolvedDynamicUrl = request.url.indexOf('{{') !== -1;
          if (request.hasDynamicValues
              && !request.userEdited
              && (request.resolveDynamicOnExecute || unresolvedDynamicUrl)) {
            return resolveDynamicRequest(request).then(function () {
              method.textContent = request.method;
              url.textContent = readableUrl(request.url);
              if (controls) {
                if (controls.urlInput) {
                  controls.urlInput.value = readableUrl(request.url);
                }
                if (controls.queryTextarea) {
                  controls.queryTextarea.value = queryFromUrl(request.url);
                }
                controls.headersTextarea.value = headersToEditableText(request.headers);
                if (controls.bodyTextarea) {
                  controls.bodyTextarea.value = request.body;
                }
                controls.syncMethodControlState();
                controls.syncLockedFields();
                controls.syncBodyControlVisibility();
              }
              refreshRequestDisplay();
              notifyChanged();
            });
          }
          return null;
        })
        .then(function () {
          if (!request.challengeId) {
            return false;
          }
          return checkChallengePassed(request);
        })
        .then(function (passedBeforeRequest) {
          wasChallengePassedBeforeRequest = passedBeforeRequest === true;
          const requestTargetValidation = validateRequestTarget(request);
          if (!requestTargetValidation.valid) {
            responseArea.status.textContent = 'Request blocked';
            responseArea.bodyPanel.textContent = requestTargetValidation.message;
            responseArea.headersPanel.textContent = '(request was not sent)';
            responseArea.rawPanel.textContent = '(request was not sent)';
            refreshRequestDisplay();
            return null;
          }

          const unsupportedRequestMessage = browserUnsupportedRequestMessage(request);
          if (unsupportedRequestMessage) {
            responseArea.status.textContent = 'Cannot execute in browser';
            responseArea.bodyPanel.textContent = unsupportedRequestMessage;
            responseArea.headersPanel.textContent = '(request was not sent)';
            responseArea.rawPanel.textContent = '(request was not sent)';
            return null;
          }

          const options = {
            method: request.method,
            headers: browserRequestHeaders(request.headers),
          };

          const body = bodyForRequest(request);
          if (body) {
            options.body = body;
          }

          return fetch(request.url, options);
        })
        .then(function (response) {
          if (!response) {
            return false;
          }

          const contentType = response.headers.get('content-type') || '';
          const responseChallenger =
              response.headers.get('X-Challenger') || response.headers.get('X-CHALLENGER');
          const responseAuthToken = response.headers.get('X-Auth-Token');
          let shouldRefreshDynamicWidgets = false;
          if (responseChallenger) {
            storeChallenger(responseChallenger);
            shouldRefreshDynamicWidgets = true;
          }
          if (responseAuthToken) {
            storeAuthToken(responseAuthToken, request);
            shouldRefreshDynamicWidgets = true;
          }
          const createdTodoId = createdTodoIdFromResponse(request, response);
          if (createdTodoId) {
            storeLastCreatedTodoId(createdTodoId);
            shouldRefreshDynamicWidgets = true;
          }
          const createdSimpleApiItemId = createdSimpleApiItemIdFromResponse(request, response);
          if (createdSimpleApiItemId) {
            storeLastCreatedSimpleApiItemId(createdSimpleApiItemId);
            shouldRefreshDynamicWidgets = true;
          }
          const createdSimpleApiItemIsbn =
              createdSimpleApiItemIsbnFromRequest(request, response);
          if (createdSimpleApiItemIsbn) {
            storeLastCreatedSimpleApiItemIsbn(createdSimpleApiItemIsbn);
            shouldRefreshDynamicWidgets = true;
          }
          if (shouldRefreshDynamicWidgets && request.refreshAfterExecute) {
            updateRenderedWidgetsFromSession();
          }
          responseArea.status.textContent =
              `${response.status} ${response.statusText || ''}`.trim();
          if (request.expectedStatus && Number(request.expectedStatus) === response.status) {
            responseArea.status.textContent += ' - expected status received';
          }
          responseArea.headersPanel.textContent = responseHeadersToText(response);
          return response.text().then(function (text) {
            responseArea.bodyPanel.textContent = formatBody(text, contentType);
            responseArea.rawPanel.textContent = rawResponseToText(response, text);
            return true;
          });
        })
        .then(function (requestWasSent) {
          if (!requestWasSent || !request.challengeId) {
            return;
          }
          return checkChallengePassed(request).then(function (passed) {
            showChallengeFeedback(challengeFeedback, passed);
            if (passed) {
              showChallengeCompletedBanner(request.challengeId);
              dispatchChallengePassedEvent(request.challengeId);
              if (!wasChallengePassedBeforeRequest) {
                showChallengeFireworks();
              }
            }
          });
        })
        .catch(function (error) {
          responseArea.status.textContent = 'Request failed';
          responseArea.bodyPanel.textContent = error.message;
          responseArea.headersPanel.textContent = '(no response headers available)';
          responseArea.rawPanel.textContent = '(no response received)';
        })
        .finally(function () {
          executeButton.disabled = false;
        });
    });

    return {
      panel: panel,
      controls: controls,
      method: method,
      url: url,
    };
  }

  function hasDynamicValues(request) {
    const allValues = [
      request.rawPath,
      request.rawBody,
      headersToEditableText(request.rawHeaders),
    ].join('\n');
    return allValues.indexOf('{{') !== -1;
  }

  function updateRequestView(widgetState) {
    widgetState.method.textContent = widgetState.request.method;
    widgetState.url.textContent = readableUrl(widgetState.request.url);
    if (widgetState.controls) {
      if (widgetState.controls.methodSelect) {
        widgetState.controls.methodSelect.value = widgetState.request.method;
      }
      if (widgetState.controls.methodInput) {
        widgetState.controls.methodInput.value = widgetState.request.method;
      }
      widgetState.controls.syncMethodControlState();
      if (widgetState.controls.urlInput) {
        widgetState.controls.urlInput.value = readableUrl(widgetState.request.url);
      }
      if (widgetState.controls.queryTextarea) {
        widgetState.controls.queryTextarea.value = queryFromUrl(widgetState.request.url);
      }
      widgetState.controls.headersTextarea.value = headersToEditableText(
        widgetState.request.headers);
      if (widgetState.controls.bodyTextarea) {
        widgetState.request.body = formatRequestBody(widgetState.request);
        widgetState.controls.bodyTextarea.value = widgetState.request.body;
      }
      widgetState.controls.syncLockedFields();
      widgetState.controls.syncBodyControlVisibility();
    }
    widgetState.updateCommands();
  }

  function updateRenderedWidgetsFromSession() {
    renderedWidgets.forEach(function (widgetState) {
      if (widgetState.request.userEdited
          || (!widgetState.request.useChallenger && !widgetState.request.hasDynamicValues)) {
        return;
      }
      resolveDynamicRequest(widgetState.request).then(function () {
        updateRequestView(widgetState);
      });
    });
  }

  function renderWidget(placeholder) {
    const isApiRequest = placeholder.classList.contains('api-live-request');
    const editMode = editModeFor(placeholder);
    const request = {
      method: normalizeMethod(placeholder.dataset.method),
      rawPath: placeholder.dataset.path || '/',
      rawBody: placeholder.dataset.body || '',
      expectedStatus: placeholder.dataset.expectedStatus || '',
      editable: editMode !== 'readonly',
      editMode: editMode,
      bodyEditable: placeholder.dataset.bodyEditable !== 'false',
      bodyMethods: (placeholder.dataset.bodyMethods || '').trim().toLowerCase(),
      customMethod: placeholder.dataset.customMethod === 'true',
      queryEditable: placeholder.dataset.queryEditable !== 'false',
      allowedPathPrefixes: parseAllowedPathPrefixes(placeholder.dataset.allowedPathPrefixes),
      useChallenger: isApiRequest && placeholder.dataset.useChallenger !== 'false',
      autoCreateFirstTodo: placeholder.dataset.autoCreateFirstTodo !== 'false',
      refreshAfterExecute: placeholder.dataset.refreshAfterExecute !== 'false',
      resolveDynamicOnExecute: placeholder.dataset.resolveDynamicOnExecute !== 'false',
      challengeId: placeholder.dataset.challengeId || '',
      userEdited: false,
    };
    request.url = absoluteUrl(request.rawPath);
    request.body = request.rawBody;
    request.rawHeaders = defaultHeadersFor(request, placeholder);
    request.headers = cloneHeaders(request.rawHeaders);
    request.body = formatRequestBody(request);
    request.hasDynamicValues = hasDynamicValues(request);

    const defaultRequest = {
      method: request.method,
      url: request.url,
      body: request.body,
      headers: cloneHeaders(request.headers),
    };

    const widget = document.createElement('section');
    widget.className = request.editable
      ? 'sim-live-widget sim-live-widget-editable'
      : 'sim-live-widget';
    if (isApiRequest) {
      widget.className += ' api-live-widget';
    }
    widget.setAttribute('aria-label', isApiRequest
      ? 'Try this API challenge solution request'
      : 'Try this simulator request');

    const title = document.createElement('div');
    title.className = 'sim-live-title';
    title.textContent = request.editable ? 'Try your own request' : 'Try it now';
    widget.appendChild(title);

    if (request.expectedStatus) {
      const expected = document.createElement('div');
      expected.className = 'sim-live-expected';
      expected.textContent = `Expected status: ${request.expectedStatus}`;
      widget.appendChild(expected);
    }

    const tabs = document.createElement('div');
    tabs.className = 'sim-live-tabs';
    tabs.setAttribute('role', 'tablist');
    [
      ['browser', 'In Browser'],
      ['curl', 'cURL'],
      ['wget', 'wget'],
    ].forEach(function (tabDefinition, index) {
      const tab = document.createElement('button');
      tab.type = 'button';
      tab.dataset.tab = tabDefinition[0];
      tab.className = index === 0 ? 'sim-live-tab active' : 'sim-live-tab';
      tab.setAttribute('aria-selected', index === 0 ? 'true' : 'false');
      tab.textContent = tabDefinition[1];
      tabs.appendChild(tab);
    });
    widget.appendChild(tabs);

    let curlCommand;
    let wgetCommand;
    const updateCommands = function () {
      if (curlCommand) {
        curlCommand.textContent = buildRestrictedCommand(request, buildCurlCommand);
      }
      if (wgetCommand) {
        wgetCommand.textContent = buildRestrictedCommand(request, buildWgetCommand);
      }
    };

    const browserPanel = renderBrowserPanel(widget, request, defaultRequest, updateCommands);
    browserPanel.panel.className += ' sim-live-panel';
    browserPanel.panel.dataset.panel = 'browser';
    widget.appendChild(browserPanel.panel);

    const curlPanel = renderCommandPanel(
      buildRestrictedCommand(request, buildCurlCommand), { curlExeToggle: true });
    curlCommand = curlPanel.pre;
    curlPanel.panel.className += ' sim-live-panel';
    curlPanel.panel.dataset.panel = 'curl';
    curlPanel.panel.hidden = true;
    widget.appendChild(curlPanel.panel);

    const wgetPanel = renderCommandPanel(buildRestrictedCommand(request, buildWgetCommand));
    wgetCommand = wgetPanel.pre;
    wgetPanel.panel.className += ' sim-live-panel';
    wgetPanel.panel.dataset.panel = 'wget';
    wgetPanel.panel.hidden = true;
    widget.appendChild(wgetPanel.panel);

    tabs.addEventListener('click', function (event) {
      if (event.target.matches('.sim-live-tab')) {
        activateTab(widget, event.target.dataset.tab, '.sim-live-tab', '.sim-live-panel');
      }
    });

    placeholder.replaceWith(widget);

    const widgetState = {
      request: request,
      controls: browserPanel.controls,
      method: browserPanel.method,
      url: browserPanel.url,
      updateCommands: updateCommands,
    };
    renderedWidgets.push(widgetState);

    if (request.hasDynamicValues || request.useChallenger) {
      resolveDynamicRequest(request).then(function () {
        defaultRequest.url = request.url;
        defaultRequest.body = request.body;
        defaultRequest.headers = cloneHeaders(request.headers);
        updateRequestView(widgetState);
      });
    }
  }

  function renderAll() {
    document.querySelectorAll(WIDGET_SELECTOR).forEach(renderWidget);
    enhanceSimpleApiRandomIsbnDetailsAll();
    updateChallengeCompletedBanners();
  }

  window.ApiChallengesSimpleApiRandomIsbn = window.ApiChallengesSimpleApiRandomIsbn || {};
  window.ApiChallengesSimpleApiRandomIsbn.buildDetails = buildSimpleApiRandomIsbnDetails;
  window.ApiChallengesSimpleApiRandomIsbn.enhanceAll = enhanceSimpleApiRandomIsbnDetailsAll;
  window.ApiChallengesSimpleApiRandomIsbn.randomIsbn = randomSimpleApiIsbn;

  window.ApiChallengesLiveRequest = window.ApiChallengesLiveRequest || {};
  window.ApiChallengesLiveRequest.renderAll = renderAll;
  window.ApiChallengesLiveRequest.showFireworks = showChallengeFireworks;

  onReady(renderAll);
}());
