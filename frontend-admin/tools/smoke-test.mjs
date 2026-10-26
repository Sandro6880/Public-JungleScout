import assert from 'node:assert/strict';
import { apiUrl, frontendUrl, request, requireLocalApi, waitFor } from './test-http.mjs';

const args = new Set(process.argv.slice(2));
for (const arg of args) if (!['--require-ui', '--keep', '--help'].includes(arg)) throw new Error(`Unknown option: ${arg}`);
if (args.has('--help')) {
  console.log('npm run test:smoke -- [--require-ui] [--keep]\nStart npm start first. --require-ui also requires an open /paths browser tab. --keep leaves only this test\'s demo records.');
  process.exit(0);
}
const keep = args.has('--keep');
let checkpointId, pathId, completed = false;
try {
  await requireLocalApi();
  console.log('PASS: frontend port 4200 proxies to the local API on port 3100.');
  const label = `example_smoke_${Date.now()}`;
  const cp = await request(`${apiUrl}/api/checkpoints`, { method: 'POST', expected: 201, body: {
    name: `${label}_checkpoint`, cpLocation: 'Example local location', gpsLat: '48.27', gpsLng: '14.25', details: 'Created by the smoke test.'
  } });
  checkpointId = cp.data.id;
  const path = await request(`${apiUrl}/api/paths`, { method: 'POST', expected: 201, body: { name: `${label}_path`, checkpoints: [{ id: checkpointId }] } });
  pathId = path.data.id;
  const throughFrontend = await request(`${frontendUrl}/api/paths/${pathId}`);
  assert.equal(throughFrontend.data.name, `${label}_path`);
  assert.equal(throughFrontend.data.checkpoints[0].id, checkpointId);
  console.log('PASS: posted values are readable through the frontend API proxy.');
  const update = await request(`${apiUrl}/api/paths/${pathId}`, { method: 'PUT', body: { name: `${label}_updated` } });
  assert.equal((await request(`${frontendUrl}/api/paths/${pathId}`)).data.name, `${label}_updated`);
  await request(`${apiUrl}/api/checkpoints`, { method: 'POST', expected: 400, body: { name: 'Example invalid latitude', gpsLat: 999, gpsLng: 14 } });
  console.log('PASS: updates work and invalid coordinates return HTTP 400.');
  if (args.has('--require-ui')) {
    await waitFor(async () => {
      const { data } = await request(`${apiUrl}/api/testing/frontend`);
      return data.clients.some(client => client.route === '/paths' && client.revision >= update.revision && client.visiblePathIds.includes(pathId));
    }, `a rendered browser receipt. Open ${frontendUrl}/paths and rerun with --require-ui`, 15000);
    console.log('PASS: the open Angular page rendered the created path and acknowledged its revision.');
  } else {
    console.log('INFO: this run verifies HTTP delivery. Add --require-ui with /paths open to verify browser rendering too.');
  }
  const { data: history } = await request(`${apiUrl}/api/testing/requests`);
  assert(history.requests.some(row => row.method === 'POST' && row.path === '/api/paths' && row.status === 201));
  console.log('PASS: the local API request log contains the successful write.');
  if (keep) console.log(`Kept path ${pathId} and checkpoint ${checkpointId}. View: ${frontendUrl}/paths`);
  completed = true;
} catch (error) {
  console.error(`FAIL: ${error.message}`);
  process.exitCode = 1;
} finally {
  if (!keep || !completed) {
    for (const [kind, value] of [['paths', pathId], ['checkpoints', checkpointId]]) {
      if (value !== undefined) {
        try { await request(`${apiUrl}/api/${kind}/${value}`, { method: 'DELETE', expected: [204, 404] }); }
        catch (error) { console.error(`Cleanup failed for ${kind}/${value}: ${error.message}`); process.exitCode = 1; }
      }
    }
  }
}
