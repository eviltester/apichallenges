const assert = require('node:assert/strict');
const test = require('node:test');
const controls = require('../../main/resources/public/js/openapi-tool-controls.js');
const converter = require('../../main/resources/public/js/openapi-tester-converter.js');

function classList() {
  const classes = new Set();
  return {
    add(name) {
      classes.add(name);
    },
    toggle(name, enabled) {
      if (enabled) {
        classes.add(name);
      } else {
        classes.delete(name);
      }
    },
    contains(name) {
      return classes.has(name);
    },
  };
}

function testScope() {
  const profile = { value: 'original' };
  const customOptions = { open: false };
  const optionInputs = [
    { checked: false, dataset: { openapiOption: 'relaxSchemaConstraints' } },
    { checked: false, dataset: { openapiOption: 'makeRequestBodiesOptional' } },
  ];
  const verbInputs = [
    { checked: false, value: 'get' },
    { checked: false, value: 'post' },
    { checked: false, value: 'trace' },
  ];
  const exportButtons = [
    { disabled: false },
    { disabled: false },
    { disabled: false },
  ];

  return {
    profile,
    customOptions,
    optionInputs,
    verbInputs,
    exportButtons,
    querySelector(selector) {
      if (selector === '[data-openapi-profile]') {
        return profile;
      }
      if (selector === '[data-openapi-custom-options]') {
        return customOptions;
      }
      return null;
    },
    querySelectorAll(selector) {
      if (selector === '[data-openapi-option]') {
        return optionInputs;
      }
      if (selector === '[data-openapi-verb]') {
        return verbInputs;
      }
      if (selector === '[data-openapi-verb]:checked') {
        return verbInputs.filter((input) => input.checked);
      }
      if (selector === '[data-openapi-option], [data-openapi-verb]') {
        return optionInputs.concat(verbInputs);
      }
      if (selector === controls.allExportActionsSelector) {
        return exportButtons;
      }
      if (selector === controls.swaggerExportActionsSelector) {
        return exportButtons.slice(0, 2);
      }
      return [];
    },
  };
}

test('readOptions maps selected profile, options, and verbs from the tool UI', () => {
  const scope = testScope();
  scope.profile.value = 'custom';
  scope.optionInputs[0].checked = true;
  scope.verbInputs[1].checked = true;
  scope.verbInputs[2].checked = true;

  assert.deepEqual(controls.readOptions(scope), {
    profile: 'custom',
    verbs: ['post', 'trace'],
    relaxSchemaConstraints: true,
    makeRequestBodiesOptional: false,
  });
});

test('writeOptions applies conversion policy values to checkboxes', () => {
  const scope = testScope();

  controls.writeOptions(scope, {
    relaxSchemaConstraints: true,
    makeRequestBodiesOptional: false,
    verbs: ['get', 'trace'],
  });

  assert.equal(scope.optionInputs[0].checked, true);
  assert.equal(scope.optionInputs[1].checked, false);
  assert.equal(scope.verbInputs[0].checked, true);
  assert.equal(scope.verbInputs[1].checked, false);
  assert.equal(scope.verbInputs[2].checked, true);
});

test('writeOptions treats missing verbs as no selected generated methods', () => {
  const scope = testScope();
  scope.verbInputs.forEach((input) => {
    input.checked = true;
  });

  controls.writeOptions(scope, {
    relaxSchemaConstraints: true,
    makeRequestBodiesOptional: true,
  });

  assert.equal(scope.verbInputs[0].checked, false);
  assert.equal(scope.verbInputs[1].checked, false);
  assert.equal(scope.verbInputs[2].checked, false);
});

test('applyProfile writes named profile defaults without closing visible custom options', () => {
  const scope = testScope();
  scope.customOptions.open = true;
  scope.profile.value = 'practical';

  controls.applyProfile(scope, converter);

  assert.equal(scope.customOptions.open, true);
  assert.equal(scope.optionInputs[0].checked, true);
  assert.equal(scope.optionInputs[1].checked, true);
  assert.equal(scope.verbInputs[0].checked, true);
  assert.equal(scope.verbInputs[1].checked, true);
  assert.equal(scope.verbInputs[2].checked, false);
});

test('switchToCustomProfile opens custom options and seeds practical defaults when empty', () => {
  const scope = testScope();

  controls.switchToCustomProfile(scope, converter);

  assert.equal(scope.profile.value, 'custom');
  assert.equal(scope.customOptions.open, true);
  assert.equal(scope.optionInputs[0].checked, true);
  assert.equal(scope.optionInputs[1].checked, true);
  assert.equal(scope.verbInputs[0].checked, true);
});

test('setButtons can target all converter actions or only Swagger export actions', () => {
  const scope = testScope();

  controls.setButtons(scope, controls.swaggerExportActionsSelector, false);

  assert.equal(scope.exportButtons[0].disabled, true);
  assert.equal(scope.exportButtons[1].disabled, true);
  assert.equal(scope.exportButtons[2].disabled, false);

  controls.setButtons(scope, controls.allExportActionsSelector, false);
  assert.equal(scope.exportButtons[2].disabled, true);
});

test('setStatus writes status text and toggles the error class', () => {
  const status = {
    textContent: '',
    classList: classList(),
  };

  controls.setStatus(status, 'Failed', true);
  assert.equal(status.textContent, 'Failed');
  assert.equal(status.classList.contains('online-client-status-error'), true);

  controls.setStatus(status, 'Ready', false);
  assert.equal(status.textContent, 'Ready');
  assert.equal(status.classList.contains('online-client-status-error'), false);
});
