const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const embedHtml = fs.readFileSync(
  path.resolve(__dirname, '../../main/resources/public/zudoku-embed.html'),
  'utf8',
);

test('Zudoku standalone embed uses canonical OpenAPI docs fallback URLs', () => {
  [
    'data-api-url="/api/docs/openapi.json"',
    "window.location.origin + '/api/docs/openapi.json'",
    "new URL('/api/docs/openapi.json'",
  ].forEach((canonicalFallback) => {
    assert.ok(embedHtml.includes(canonicalFallback), canonicalFallback);
  });

  [
    'data-api-url="/docs/openapi.json"',
    "window.location.origin + '/docs/openapi.json'",
    "new URL('/docs/openapi.json'",
  ].forEach((legacyFallback) => {
    assert.equal(embedHtml.includes(legacyFallback), false, legacyFallback);
  });
});
