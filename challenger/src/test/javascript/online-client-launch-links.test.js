const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const contentRoot = path.resolve(
  __dirname,
  '../../main/resources/content/tools/online-clients',
);

function markdownFiles(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      return markdownFiles(entryPath);
    }
    return entry.name.endsWith('.md') ? [entryPath] : [];
  });
}

test('hosted OpenAPI client launch links use default OpenAPI aliases', () => {
  const versionedLaunchLinks = [];

  markdownFiles(contentRoot).forEach((file) => {
    const content = fs.readFileSync(file, 'utf8');
    const launchLinkPattern = /openapi-ui-launch-link" href="([^"]+)"/g;
    let match = launchLinkPattern.exec(content);
    while (match) {
      if (/openapi-3\.[0-9]\.json/.test(match[1])) {
        versionedLaunchLinks.push(`${path.relative(contentRoot, file)}: ${match[1]}`);
      }
      match = launchLinkPattern.exec(content);
    }
  });

  assert.deepEqual(versionedLaunchLinks, []);
});
