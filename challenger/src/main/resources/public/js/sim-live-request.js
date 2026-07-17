(function () {
  'use strict';

  const WIDGET_SELECTOR = '.sim-live-request';
  const DEFAULT_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD', 'TRACE'];

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

  function escapeShellSingleQuotes(value) {
    return value.replace(/'/g, "'\"'\"'");
  }

  function defaultHeadersFor(request) {
    const headers = [{ name: 'Accept', value: 'application/json' }];
    if (request.body) {
      headers.push({ name: 'Content-Type', value: 'application/json' });
    }
    return headers;
  }

  function cloneHeaders(headers) {
    return headers.map(function (header) {
      return { name: header.name, value: header.value };
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

  function formatBody(text, contentType) {
    if (!text) {
      return '(no response body)';
    }

    if (contentType && contentType.toLowerCase().includes('json')) {
      try {
        return JSON.stringify(JSON.parse(text), null, 2);
      } catch (ignored) {
        return text;
      }
    }

    return text;
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

    const resetButton = document.createElement('button');
    resetButton.type = 'button';
    resetButton.className = 'sim-live-reset';
    resetButton.textContent = 'Reset';

    function syncRequestFromControls() {
      request.method = methodSelect.value;
      request.url = absoluteUrl(urlInput.value);
      urlInput.value = request.url;
      request.headers = parseEditableHeaders(headersTextarea.value);
      notifyChanged();
    }

    methodSelect.addEventListener('change', syncRequestFromControls);
    urlInput.addEventListener('change', syncRequestFromControls);
    headersTextarea.addEventListener('input', syncRequestFromControls);
    resetButton.addEventListener('click', function () {
      request.method = defaultRequest.method;
      request.url = defaultRequest.url;
      request.headers = cloneHeaders(defaultRequest.headers);
      methodSelect.value = request.method;
      urlInput.value = request.url;
      headersTextarea.value = headersToEditableText(request.headers);
      notifyChanged();
    });

    controls.appendChild(methodLabel);
    controls.appendChild(urlLabel);
    controls.appendChild(headersLabel);
    controls.appendChild(resetButton);
    return controls;
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

    if (request.editable) {
      panel.appendChild(renderEditableControls(request, defaultRequest, function () {
        method.textContent = request.method;
        url.textContent = request.url;
        notifyChanged();
      }));
    }

    if (request.body) {
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

      const options = {
        method: request.method,
        headers: headersAsObject(request.headers),
      };

      if (request.body && request.method !== 'GET' && request.method !== 'HEAD') {
        options.body = request.body;
      }

      fetch(request.url, options)
        .then(function (response) {
          const contentType = response.headers.get('content-type') || '';
          responseArea.status.textContent =
              `${response.status} ${response.statusText || ''}`.trim();
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

    return panel;
  }

  function renderWidget(placeholder) {
    const request = {
      method: placeholder.dataset.method,
      path: placeholder.dataset.path,
      body: placeholder.dataset.body || '',
      editable: placeholder.dataset.editable === 'true',
    };
    request.url = absoluteUrl(request.path);
    request.headers = defaultHeadersFor(request);

    const defaultRequest = {
      method: request.method,
      path: request.path,
      url: request.url,
      body: request.body,
      headers: cloneHeaders(request.headers),
    };

    const widget = document.createElement('section');
    widget.className = request.editable
        ? 'sim-live-widget sim-live-widget-editable'
        : 'sim-live-widget';
    widget.setAttribute('aria-label', 'Try this simulator request');

    const title = document.createElement('div');
    title.className = 'sim-live-title';
    title.textContent = request.editable ? 'Try your own request' : 'Try it now';
    widget.appendChild(title);

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
    browserPanel.className += ' sim-live-panel';
    browserPanel.dataset.panel = 'browser';
    widget.appendChild(browserPanel);

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
  }

  onReady(function () {
    document.querySelectorAll(WIDGET_SELECTOR).forEach(renderWidget);
  });
}());
