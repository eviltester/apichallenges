const { spawnSync } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const projectRoot = path.resolve(__dirname, '..', '..', '..');
const filesToSyntaxCheck = [
  'src/main/resources/public/js/openapi-tester-converter.js',
  'src/main/resources/public/js/openapi-text-loader.js',
  'src/main/resources/public/js/openapi-tool-controls.js',
  'src/main/resources/public/js/openapi-converter-page.js',
  'src/main/resources/public/js/online-swagger-client.js',
  'src/main/resources/public/js/online-openapi-ui-client.js',
  'src/main/resources/public/js/vendor/js-yaml.min.js',
];

function runNode(args) {
  const result = spawnSync(process.execPath, args, {
    cwd: projectRoot,
    stdio: 'inherit',
  });

  if (result.status !== 0) {
    process.exit(result.status || 1);
  }
}

filesToSyntaxCheck.forEach((file) => runNode(['--check', file]));

const testFiles = fs.readdirSync(__dirname)
  .filter((file) => file.endsWith('.test.js'))
  .sort()
  .map((file) => path.join('src/test/javascript', file));

runNode(['--test'].concat(testFiles));
