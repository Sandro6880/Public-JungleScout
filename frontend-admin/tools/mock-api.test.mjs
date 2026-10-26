import test from 'node:test';
import assert from 'node:assert/strict';
import { createMockServer } from './mock-api.mjs';
import { request } from './test-http.mjs';

async function setup(t, seed = false) {
  const server = createMockServer({ seed });
  await new Promise(accept => server.listen(0, '127.0.0.1', accept));
  t.after(() => new Promise(accept => server.close(accept)));
  const url = `http://127.0.0.1:${server.address().port}/api`;
  return {
    url, call: (path, options) => request(`${url}${path}`, options),
    cp: body => request(`${url}/checkpoints`, { method: 'POST', expected: 201, body: { name: 'Example checkpoint', gpsLat: '48.27', gpsLng: '14.25', ...body } }),
    path: body => request(`${url}/paths`, { method: 'POST', expected: 201, body: { name: 'Example path', ...body } })
  };
}

test('checkpoint CRUD normalizes coordinates and uses server-assigned IDs', async t => {
  const api = await setup(t);
  const created = await api.cp({ gpsLat: 0, gpsLng: 0 });
  assert.equal(created.data.gpsLat, '0');
  assert.equal(created.data.gpsLng, '0');
  assert.equal(created.data.id, 1);
  await api.call('/checkpoints/1', { method: 'PUT', body: { name: 'Example updated checkpoint' } });
  assert.equal((await api.call('/checkpoints/1')).data.name, 'Example updated checkpoint');
  await api.call('/checkpoints/1', { method: 'DELETE', expected: 204 });
  await api.call('/checkpoints/1', { expected: 404 });
});

test('invalid coordinate inputs are rejected without changing state', async t => {
  const api = await setup(t);
  for (const value of ['', null, true, 'not a number', 91, -91]) {
    await api.call('/checkpoints', { method: 'POST', expected: 400, body: { name: 'Example invalid checkpoint', gpsLat: value, gpsLng: 14 } });
  }
  assert.equal((await api.call('/state')).data.revision, 0);
  assert.equal((await api.call('/checkpoints')).data.length, 0);
});

test('names, body shape, unknown fields and explicit creation IDs are validated', async t => {
  const api = await setup(t);
  for (const body of [null, [], { name: '' }, { name: 4 }, { name: 'Example path', surprise: true }, { name: 'Example path', id: 10 }]) {
    await api.call('/paths', { method: 'POST', expected: 400, body });
  }
});

test('empty and single-checkpoint paths return valid zero metrics', async t => {
  const api = await setup(t);
  const empty = await api.path({});
  assert.deepEqual(empty.data.checkpoints, []);
  assert.equal(empty.data.pathLength, '0.00');
  assert.equal(empty.data.pathDuration, '00:00:00');
  const cp = await api.cp({});
  const single = await api.path({ checkpoints: [{ id: cp.data.id }] });
  assert.equal(single.data.pathLength, '0.00');
});

test('checkpoint membership preserves order and rejects missing or duplicate IDs', async t => {
  const api = await setup(t);
  await api.cp({}); await api.cp({ name: 'Example second checkpoint' });
  const path = await api.path({ checkpoints: [2, 1] });
  assert.deepEqual(path.data.checkpoints.map(cp => cp.id), [2, 1]);
  for (const checkpoints of [[999], [1, 1], 'invalid']) {
    await api.call('/paths', { method: 'POST', expected: 400, body: { name: 'Example invalid path', checkpoints } });
  }
});

test('path updates validate first and do not partially mutate on failure', async t => {
  const api = await setup(t);
  await api.path({});
  const before = (await api.call('/state')).data;
  await api.call('/paths/1', { method: 'PUT', expected: 400, body: { name: 'Example changed', checkpoints: [999] } });
  assert.deepEqual((await api.call('/state')).data, before);
});

test('checkpoint updates propagate into attached paths and deletion detaches references', async t => {
  const api = await setup(t);
  await api.cp({}); await api.path({ checkpoints: [1] });
  await api.call('/checkpoints/1', { method: 'PUT', body: { details: 'Example updated details' } });
  assert.equal((await api.call('/paths/1')).data.checkpoints[0].details, 'Example updated details');
  await api.call('/checkpoints/1', { method: 'DELETE', expected: 204 });
  assert.deepEqual((await api.call('/paths/1')).data.checkpoints, []);
});

test('checkpoint helper endpoints support attach, reorder and detach', async t => {
  const api = await setup(t);
  await api.cp({}); await api.cp({}); await api.path({});
  await api.call('/paths/1/checkpoints', { method: 'POST', body: [1, 2] });
  await api.call('/paths/1/checkpoints', { method: 'PUT', body: [2, 1] });
  assert.deepEqual((await api.call('/paths/1/checkpoints')).data.map(cp => cp.id), [2, 1]);
  await api.call('/paths/1/checkpoints', { method: 'DELETE', body: [2] });
  assert.deepEqual((await api.call('/paths/1/checkpoints')).data.map(cp => cp.id), [1]);
});

test('local route estimate is finite and near 111 km for one degree at the equator', async t => {
  const api = await setup(t);
  await api.cp({ gpsLat: '0', gpsLng: '0' });
  await api.cp({ gpsLat: '0', gpsLng: '1' });
  const path = await api.path({ checkpoints: [1, 2] });
  assert(Number(path.data.pathLength) > 110 && Number(path.data.pathLength) < 112);
});

test('unsupported methods and unknown routes return clear status codes', async t => {
  const api = await setup(t);
  await api.call('/missing', { expected: 404 });
  await api.call('/paths', { method: 'PATCH', expected: 405 });
  await api.call('/paths/999', { expected: 404 });
});

test('malformed JSON and unsupported content types return 400/415', async t => {
  const api = await setup(t);
  const malformed = await fetch(`${api.url}/paths`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{broken' });
  assert.equal(malformed.status, 400); await malformed.text();
  const wrongType = await fetch(`${api.url}/paths`, { method: 'POST', headers: { 'Content-Type': 'text/plain' }, body: '{"name":"Example"}' });
  assert.equal(wrongType.status, 415); await wrongType.text();
});

test('browser origins are restricted to the two loopback frontend URLs', async t => {
  const api = await setup(t);
  const good = await fetch(`${api.url}/health`, { headers: { Origin: 'http://127.0.0.1:4200' } });
  assert.equal(good.headers.get('access-control-allow-origin'), 'http://127.0.0.1:4200'); await good.text();
  const bad = await fetch(`${api.url}/health`, { headers: { Origin: 'https://example.invalid' } });
  assert.equal(bad.status, 403); await bad.text();
});

test('frontend receipts validate revisions and carry rendered entity IDs', async t => {
  const api = await setup(t);
  const receipt = { clientId: 'example_ui_client', revision: 0, route: '/paths', visiblePathIds: [], visibleCheckpointIds: [] };
  await api.call('/testing/frontend', { method: 'POST', body: receipt });
  const state = await api.call('/testing/frontend');
  assert.equal(state.data.clients[0].route, '/paths');
  await api.call('/testing/frontend', { method: 'POST', expected: 400, body: { ...receipt, revision: 999 } });
});

test('request diagnostics record status and request ID without request bodies', async t => {
  const api = await setup(t);
  const created = await api.path({});
  const { data } = await api.call('/testing/requests');
  const row = data.requests.find(row => row.requestId === created.requestId);
  assert.equal(row.method, 'POST'); assert.equal(row.status, 201);
  assert.equal(row.body, undefined);
});

test('reset restores seeded fixtures and keeps the revision monotonic', async t => {
  const api = await setup(t, true);
  const created = await api.path({});
  const reset = await api.call('/testing/reset', { method: 'POST', body: {} });
  assert(reset.revision > created.revision);
  assert.equal(reset.data.paths.length, 1);
  assert.equal(reset.data.checkpoints.length, 3);
});
