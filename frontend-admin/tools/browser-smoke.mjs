import assert from 'node:assert/strict';
import { chromium } from 'playwright';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { apiUrl, frontendUrl, request, requireLocalApi, waitFor } from './test-http.mjs';

let browser;
const createdPaths = [], createdCheckpoints = [];
const errors = [], externalRequests = [];
const name = `example_browser_${Date.now()}`;
try {
  await requireLocalApi();
  browser = await chromium.launch({
    headless: true,
    executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH || undefined,
    channel: process.env.PLAYWRIGHT_CHANNEL || undefined,
    args: process.env.PLAYWRIGHT_CHROMIUM_ARGS ? JSON.parse(process.env.PLAYWRIGHT_CHROMIUM_ARGS) : []
  });
  const page = await browser.newPage();
  page.on('pageerror', error => errors.push(error.message));
  page.on('request', req => {
    const url = new URL(req.url());
    if (['http:', 'https:'].includes(url.protocol) && !['127.0.0.1', 'localhost', '[::1]'].includes(url.hostname)) externalRequests.push(req.url());
  });
  await page.goto(`${frontendUrl}/paths`);
  await page.getByRole('cell', { name: 'Demo woodland walk', exact: true }).waitFor();
  const cp = await request(`${apiUrl}/api/checkpoints`, { method: 'POST', expected: 201, body: { name: `${name}_cp`, gpsLat: 48.27, gpsLng: 14.25 } });
  createdCheckpoints.push(cp.data.id);
  const path = await request(`${apiUrl}/api/paths`, { method: 'POST', expected: 201, body: { name: `${name}_path`, checkpoints: [cp.data.id] } });
  createdPaths.push(path.data.id);
  await page.getByRole('cell', { name: `${name}_path`, exact: true }).waitFor();
  console.log('PASS: an API-created path appeared in the Angular table without a reload.');
  await waitFor(async () => (await request(`${apiUrl}/api/testing/frontend`)).data.clients.some(client => client.visiblePathIds.includes(path.data.id)), 'a DOM receipt');

  const smoke = spawn(process.execPath, ['tools/smoke-test.mjs', '--require-ui'], { cwd: fileURLToPath(new URL('..', import.meta.url)), env: process.env, stdio: 'inherit' });
  const smokeCode = await new Promise(accept => smoke.on('exit', accept));
  assert.equal(smokeCode, 0, 'The public smoke script should also confirm browser delivery.');

  await page.locator(`[data-path-id="${path.data.id}"]`).getByRole('link', { name: 'Edit path' }).click();
  await page.getByTestId('local-map').waitFor();
  await request(`${apiUrl}/api/paths/${path.data.id}`, { method: 'PUT', body: { name: `${name}_updated` } });
  await waitFor(async () => (await page.getByTestId('path-heading').textContent()) === `${name}_updated`, 'the updated path heading');
  await page.locator('#edit-path-name').fill(`${name}_from_ui`);
  await page.getByRole('button', { name: 'Save path', exact: true }).click();
  await waitFor(async () => (await request(`${apiUrl}/api/paths/${path.data.id}`)).data.name === `${name}_from_ui`, 'the UI write to reach the API');
  console.log('PASS: path detail renders with one checkpoint; external updates and UI saves work in both directions.');

  const empty = await request(`${apiUrl}/api/paths`, { method: 'POST', expected: 201, body: { name: `${name}_empty`, checkpoints: [] } });
  createdPaths.push(empty.data.id);
  await page.goto(`${frontendUrl}/path-view/${empty.data.id}`);
  await page.getByText('This path has no checkpoints.', { exact: true }).waitFor();
  await page.getByTestId('local-map').click({ position: { x: 160, y: 90 } });
  await page.locator('#map-cp-name').fill(`${name}_map_cp`);
  await page.getByRole('button', { name: 'Create and attach', exact: true }).click();
  await waitFor(async () => (await request(`${apiUrl}/api/paths/${empty.data.id}`)).data.checkpoints.length === 1, 'a map-created checkpoint');
  const attached = (await request(`${apiUrl}/api/paths/${empty.data.id}`)).data.checkpoints[0];
  createdCheckpoints.push(attached.id);
  console.log('PASS: empty paths work, and map clicks can create and attach checkpoints locally.');

  await page.goto(`${frontendUrl}/create-checkpoint`);
  await page.getByTestId('checkpoint-name').fill(`${name}_form_cp`);
  await page.getByTestId('checkpoint-lat').fill('48.28');
  await page.getByTestId('checkpoint-lng').fill('14.26');
  await page.getByRole('button', { name: 'Create checkpoint', exact: true }).click();
  await page.getByRole('cell', { name: `${name}_form_cp`, exact: true }).waitFor();
  const fromForm = (await request(`${apiUrl}/api/checkpoints`)).data.find(cp => cp.name === `${name}_form_cp`);
  assert(fromForm); createdCheckpoints.push(fromForm.id);
  await page.goto(`${frontendUrl}/create-path`);
  await page.getByTestId('path-name').fill(`${name}_form_path`);
  await page.getByRole('button', { name: 'Create path', exact: true }).click();
  await page.getByTestId('path-heading').waitFor();
  const fromPathForm = (await request(`${apiUrl}/api/paths`)).data.find(path => path.name === `${name}_form_path`);
  assert(fromPathForm); createdPaths.push(fromPathForm.id);
  console.log('PASS: checkpoint and path forms submit successfully.');
  assert.deepEqual(errors, [], 'The browser must not report uncaught JavaScript errors.');
  assert.deepEqual(externalRequests, [], 'Local mode must not call Google Maps, font CDNs or other external URLs.');
  console.log('PASS: no uncaught browser errors and no external HTTP requests.');
} catch (error) {
  console.error(`FAIL: ${error.stack || error.message}`);
  process.exitCode = 1;
} finally {
  if (browser) await browser.close();
  for (const [kind, values] of [['paths', createdPaths], ['checkpoints', createdCheckpoints]]) {
    for (const value of values) {
      try { await request(`${apiUrl}/api/${kind}/${value}`, { method: 'DELETE', expected: [204, 404] }); }
      catch (error) { console.error(`Cleanup failed: ${error.message}`); process.exitCode = 1; }
    }
  }
}
