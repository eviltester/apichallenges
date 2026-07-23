(function () {
  'use strict';

  const WIDGET_SELECTOR = '.sim-live-request, .api-live-request';
  const DEFAULT_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD', 'TRACE'];
  const BROWSER_UNSUPPORTED_METHODS = ['CONNECT', 'TRACE', 'TRACK'];
  const BROWSER_UNSUPPORTED_METHOD_OVERRIDE_HEADERS = [
    'x-http-method',
    'x-http-method-override',
    'x-method-override',
  ];
  const LIVE_WIDGET_HEADER = 'X-API-Challenges-Live-Widget';
  const CHALLENGER_COOKIE = 'X-CHALLENGER';
  const LEGACY_CHALLENGER_COOKIE = 'X-THINGIFIER-DATABASE-NAME';
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

  function pathFromUrl(url) {
    try {
      return new URL(url, window.location.origin).pathname;
    } catch (error) {
      return '';
    }
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

  function lastCreatedTodoStorageKey(challenger) {
    return `apichallenges.${challenger}.lastCreatedTodoId`;
  }

  function currentAuthToken() {
    const challenger = currentChallenger();
    if (!challenger) {
      return '';
    }
    return localStorage.getItem(authTokenStorageKey(challenger)) || '';
  }

  function currentLastCreatedTodoId() {
    const challenger = currentChallenger();
    if (!challenger) {
      return '1';
    }
    return localStorage.getItem(lastCreatedTodoStorageKey(challenger)) || '1';
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

  function storeAuthToken(token) {
    const challenger = currentChallenger();
    if (!challenger || !token) {
      return;
    }
    localStorage.setItem(authTokenStorageKey(challenger), token);
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
    const parts = ['curl -i', '-X', request.method, `"${request.url}"`];
    request.headers.forEach(function (header) {
      parts.push(`-H "${header.name}: ${header.value}"`);
    });
    if (request.body && request.method !== 'GET' && request.method !== 'HEAD') {
      parts.push(`--data '${escapeShellSingleQuotes(request.body)}'`);
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
    if (request.body && request.method !== 'GET' && request.method !== 'HEAD') {
      parts.push(`--body-data='${escapeShellSingleQuotes(request.body)}'`);
    }
    parts.push(`"${request.url}"`);
    return parts.join(' ');
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

  function renderCommandPanel(commandText) {
    const panel = document.createElement('div');
    panel.className = 'sim-live-command-panel';

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

    panel.appendChild(pre);
    panel.appendChild(copyButton);
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

    responseTabs.appendChild(bodyTab);
    responseTabs.appendChild(headersTab);

    const bodyPanel = document.createElement('pre');
    bodyPanel.className = 'sim-live-response-panel';
    bodyPanel.dataset.panel = 'body';
    bodyPanel.textContent = '(execute the request to see the response body)';

    const headersPanel = document.createElement('pre');
    headersPanel.className = 'sim-live-response-panel';
    headersPanel.dataset.panel = 'headers';
    headersPanel.hidden = true;
    headersPanel.textContent = '(execute the request to see the response headers)';

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
      elements: [status, responseTabs, bodyPanel, headersPanel],
    };
  }

  function renderEditableControls(request, defaultRequest, notifyChanged) {
    const controls = document.createElement('div');
    controls.className = 'sim-live-edit-controls';

    const methodLabel = document.createElement('label');
    methodLabel.textContent = 'Verb';
    const methodSelect = document.createElement('select');
    methodSelect.className = 'sim-live-edit-method';
    DEFAULT_METHODS.forEach(function (method) {
      const option = document.createElement('option');
      option.value = method;
      option.textContent = method;
      methodSelect.appendChild(option);
    });
    methodSelect.value = request.method;
    methodLabel.appendChild(methodSelect);

    const urlLabel = document.createElement('label');
    urlLabel.textContent = 'URL';
    const urlInput = document.createElement('input');
    urlInput.className = 'sim-live-edit-url';
    urlInput.type = 'url';
    urlInput.value = request.url;
    urlLabel.appendChild(urlInput);

    const headersLabel = document.createElement('label');
    headersLabel.textContent = 'Headers';
    const headersTextarea = document.createElement('textarea');
    headersTextarea.className = 'sim-live-edit-headers';
    headersTextarea.rows = 4;
    headersTextarea.value = headersToEditableText(request.headers);
    headersLabel.appendChild(headersTextarea);

    let bodyTextarea = null;
    let bodyLabel = null;
    if (request.bodyEditable && request.body) {
      bodyLabel = document.createElement('label');
      bodyLabel.textContent = 'Body';
      bodyTextarea = document.createElement('textarea');
      bodyTextarea.className = 'sim-live-edit-body';
      bodyTextarea.rows = 6;
      bodyTextarea.value = formatRequestBody(request);
      bodyLabel.appendChild(bodyTextarea);

      const prettyPrintButton = document.createElement('button');
      prettyPrintButton.type = 'button';
      prettyPrintButton.className = 'sim-live-pretty-print';
      prettyPrintButton.textContent = 'Pretty print body';
      prettyPrintButton.addEventListener('click', function () {
        request.userEdited = true;
        request.headers = parseEditableHeaders(headersTextarea.value);
        request.body = bodyTextarea.value;
        request.body = formatRequestBody(request);
        bodyTextarea.value = request.body;
        notifyChanged();
      });
      bodyLabel.appendChild(prettyPrintButton);
    }

    const resetButton = document.createElement('button');
    resetButton.type = 'button';
    resetButton.className = 'sim-live-reset';
    resetButton.textContent = 'Reset';

    function syncRequestFromControls() {
      request.userEdited = true;
      request.method = methodSelect.value;
      request.url = absoluteUrl(urlInput.value);
      urlInput.value = request.url;
      request.headers = parseEditableHeaders(headersTextarea.value);
      if (bodyTextarea) {
        request.body = bodyTextarea.value;
      }
      notifyChanged();
    }

    methodSelect.addEventListener('change', syncRequestFromControls);
    urlInput.addEventListener('change', syncRequestFromControls);
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
      methodSelect.value = request.method;
      urlInput.value = request.url;
      headersTextarea.value = headersToEditableText(request.headers);
      if (bodyTextarea) {
        bodyTextarea.value = formatRequestBody(request);
      }
      notifyChanged();
    });

    controls.appendChild(methodLabel);
    controls.appendChild(urlLabel);
    controls.appendChild(headersLabel);
    if (bodyLabel) {
      controls.appendChild(bodyLabel);
    }
    controls.appendChild(resetButton);
    return {
      element: controls,
      methodSelect: methodSelect,
      urlInput: urlInput,
      headersTextarea: headersTextarea,
      bodyTextarea: bodyTextarea,
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
    ]).then(function (values) {
      return {
        currentChallenger: challenger,
        mismatchedChallenger: mismatchedChallenger(),
        authToken: currentAuthToken(),
        firstTodoId: values[0],
        missingTodoId: values[1],
        currentChallengerJson: values[2],
        currentTodosJson: values[3],
        lastCreatedTodoId: values[4],
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
    url.textContent = request.url;

    requestLine.appendChild(method);
    requestLine.appendChild(url);
    panel.appendChild(requestLine);

    let controls = null;
    if (request.editable) {
      controls = renderEditableControls(request, defaultRequest, function () {
        method.textContent = request.method;
        url.textContent = request.url;
        notifyChanged();
      });
      panel.appendChild(controls.element);
    }

    if (request.body && !request.bodyEditable) {
      const body = document.createElement('pre');
      body.className = 'sim-live-request-body';
      body.textContent = request.body;
      panel.appendChild(body);
    }

    const executeButton = document.createElement('button');
    executeButton.type = 'button';
    executeButton.className = 'sim-live-execute';
    const executeIcon = document.createElement('span');
    executeIcon.className = 'sim-live-execute-icon';
    executeIcon.setAttribute('aria-hidden', 'true');
    executeIcon.textContent = '▶';
    executeButton.appendChild(executeIcon);
    executeButton.appendChild(document.createTextNode('Execute request'));
    panel.appendChild(executeButton);

    const responseArea = renderResponseArea(widget);
    responseArea.elements.forEach(function (element) {
      panel.appendChild(element);
    });

    executeButton.addEventListener('click', function () {
      executeButton.disabled = true;
      responseArea.status.textContent = 'Running...';
      responseArea.bodyPanel.textContent = '';
      responseArea.headersPanel.textContent = '';

      Promise.resolve()
        .then(function () {
          const unresolvedDynamicUrl = request.url.indexOf('{{') !== -1;
          if (request.hasDynamicValues
              && !request.userEdited
              && (request.resolveDynamicOnExecute || unresolvedDynamicUrl)) {
            return resolveDynamicRequest(request).then(function () {
              method.textContent = request.method;
              url.textContent = request.url;
              if (controls) {
                controls.urlInput.value = request.url;
                controls.headersTextarea.value = headersToEditableText(request.headers);
                if (controls.bodyTextarea) {
                  controls.bodyTextarea.value = request.body;
                }
              }
              notifyChanged();
            });
          }
          return null;
        })
        .then(function () {
          const unsupportedRequestMessage = browserUnsupportedRequestMessage(request);
          if (unsupportedRequestMessage) {
            responseArea.status.textContent = 'Cannot execute in browser';
            responseArea.bodyPanel.textContent = unsupportedRequestMessage;
            responseArea.headersPanel.textContent = '(request was not sent)';
            return null;
          }

          const options = {
            method: request.method,
            headers: browserRequestHeaders(request.headers),
          };

          if (request.body && request.method !== 'GET' && request.method !== 'HEAD') {
            options.body = request.body;
          }

          return fetch(request.url, options);
        })
        .then(function (response) {
          if (!response) {
            return;
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
            storeAuthToken(responseAuthToken);
            shouldRefreshDynamicWidgets = true;
          }
          const createdTodoId = createdTodoIdFromResponse(request, response);
          if (createdTodoId) {
            storeLastCreatedTodoId(createdTodoId);
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
          });
        })
        .catch(function (error) {
          responseArea.status.textContent = 'Request failed';
          responseArea.bodyPanel.textContent = error.message;
          responseArea.headersPanel.textContent = '(no response headers available)';
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
    widgetState.url.textContent = widgetState.request.url;
    if (widgetState.controls) {
      widgetState.controls.methodSelect.value = widgetState.request.method;
      widgetState.controls.urlInput.value = widgetState.request.url;
      widgetState.controls.headersTextarea.value = headersToEditableText(
        widgetState.request.headers);
      if (widgetState.controls.bodyTextarea) {
        widgetState.request.body = formatRequestBody(widgetState.request);
        widgetState.controls.bodyTextarea.value = widgetState.request.body;
      }
    }
    widgetState.updateCommands();
  }

  function updateRenderedWidgetsFromSession() {
    renderedWidgets.forEach(function (widgetState) {
      if (!widgetState.request.useChallenger || widgetState.request.userEdited) {
        return;
      }
      resolveDynamicRequest(widgetState.request).then(function () {
        updateRequestView(widgetState);
      });
    });
  }

  function renderWidget(placeholder) {
    const isApiRequest = placeholder.classList.contains('api-live-request');
    const request = {
      method: placeholder.dataset.method,
      rawPath: placeholder.dataset.path,
      rawBody: placeholder.dataset.body || '',
      expectedStatus: placeholder.dataset.expectedStatus || '',
      editable: placeholder.dataset.editable === 'true',
      bodyEditable: isApiRequest && placeholder.dataset.bodyEditable !== 'false',
      useChallenger: isApiRequest && placeholder.dataset.useChallenger !== 'false',
      autoCreateFirstTodo: placeholder.dataset.autoCreateFirstTodo !== 'false',
      refreshAfterExecute: placeholder.dataset.refreshAfterExecute !== 'false',
      resolveDynamicOnExecute: placeholder.dataset.resolveDynamicOnExecute !== 'false',
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
        curlCommand.textContent = buildCurlCommand(request);
      }
      if (wgetCommand) {
        wgetCommand.textContent = buildWgetCommand(request);
      }
    };

    const browserPanel = renderBrowserPanel(widget, request, defaultRequest, updateCommands);
    browserPanel.panel.className += ' sim-live-panel';
    browserPanel.panel.dataset.panel = 'browser';
    widget.appendChild(browserPanel.panel);

    const curlPanel = renderCommandPanel(buildCurlCommand(request));
    curlCommand = curlPanel.pre;
    curlPanel.panel.className += ' sim-live-panel';
    curlPanel.panel.dataset.panel = 'curl';
    curlPanel.panel.hidden = true;
    widget.appendChild(curlPanel.panel);

    const wgetPanel = renderCommandPanel(buildWgetCommand(request));
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

  onReady(function () {
    document.querySelectorAll(WIDGET_SELECTOR).forEach(renderWidget);
  });
}());
