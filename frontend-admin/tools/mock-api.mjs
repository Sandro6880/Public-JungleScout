import http from 'node:http';
import { pathToFileURL } from 'node:url';
import { resolve } from 'node:path';
import { randomUUID } from 'node:crypto';

// Local development data only. Nothing is written to disk or sent to another service.
const seedCheckpoints = [
  { id: 1, name: 'Forest entrance', cpLocation: 'Leonding', gpsLat: '48.268', gpsLng: '14.252', details: 'Start of the demo walk.' },
  { id: 2, name: 'Woodland lookout', cpLocation: 'Leonding', gpsLat: '48.275', gpsLng: '14.242', details: 'A checkpoint for local testing.' },
  { id: 3, name: 'Riverside stop', cpLocation: 'Linz', gpsLat: '48.281', gpsLng: '14.258', details: 'End of the demo walk.' }
];

class ApiError extends Error {
  constructor(status, message) { super(message); this.status = status; }
}
const fail = (message, status = 400) => { throw new ApiError(status, message); };
const isObject = value => value !== null && typeof value === 'object' && !Array.isArray(value);

function object(value) {
  if (!isObject(value)) fail('The JSON body must be an object.');
  return value;
}

function text(value, field, required = false, limit = 2000) {
  if (value === undefined && !required) return '';
  if (typeof value !== 'string') fail(`${field} must be a string.`);
  const result = value.trim();
  if (required && !result) fail(`${field} is required.`);
  if (result.length > limit) fail(`${field} must contain at most ${limit} characters.`);
  return result;
}

function id(value, field = 'id') {
  if (!['string', 'number'].includes(typeof value) || String(value).trim() === '') fail(`${field} must be a positive integer.`);
  const result = Number(value);
  if (!Number.isSafeInteger(result) || result < 1) fail(`${field} must be a positive integer.`);
  return result;
}

function coordinate(value, field, max) {
  if (!['string', 'number'].includes(typeof value) || String(value).trim() === '') fail(`${field} is required and must be numeric.`);
  const result = Number(value);
  if (!Number.isFinite(result) || Math.abs(result) > max) fail(`${field} must be between ${-max} and ${max}.`);
  return String(result);
}

function knownFields(body, fields) {
  for (const field of Object.keys(body)) if (!fields.includes(field)) fail(`Unknown field: ${field}.`);
}

function metrics(checkpoints) {
  let km = 0;
  const rad = degrees => degrees * Math.PI / 180;
  for (let i = 1; i < checkpoints.length; i++) {
    const a = checkpoints[i - 1], b = checkpoints[i];
    const lat1 = rad(Number(a.gpsLat)), lat2 = rad(Number(b.gpsLat));
    const dLat = lat2 - lat1, dLng = rad(Number(b.gpsLng) - Number(a.gpsLng));
    const h = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;
    km += 6371 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, h))));
  }
  const seconds = Math.round(km / 5 * 3600);
  const time = [Math.floor(seconds / 3600), Math.floor(seconds / 60) % 60, seconds % 60]
    .map(value => String(value).padStart(2, '0')).join(':');
  return { pathLength: km.toFixed(2), pathDuration: time };
}

async function readJson(req) {
  const chunks = [];
  let length = 0;
  for await (const chunk of req) {
    length += chunk.length;
    if (length > 1024 * 1024) fail('JSON body exceeds the 1 MiB limit.', 413);
    chunks.push(chunk);
  }
  const raw = Buffer.concat(chunks).toString('utf8');
  if (!raw.trim()) return {};
  if (!/^application\/json(?:\s*;|$)/i.test(req.headers['content-type'] || '')) fail('Use Content-Type: application/json.', 415);
  try { return JSON.parse(raw); } catch { fail('Malformed JSON body.'); }
}

export function createMockServer({ seed = true } = {}) {
  const instanceId = randomUUID();
  let checkpoints = [], paths = [], revision = 0, nextCheckpoint = 1, nextPath = 1, requestNumber = 0;
  let requests = [];
  const frontendClients = new Map();

  const findCheckpoint = value => {
    const result = checkpoints.find(cp => cp.id === id(value));
    if (!result) fail('Checkpoint not found.', 404);
    return result;
  };
  const findPath = value => {
    const result = paths.find(path => path.id === id(value));
    if (!result) fail('Path not found.', 404);
    return result;
  };
  const serialize = path => {
    const members = path.checkpointIds.map(cpId => findCheckpoint(cpId));
    return { id: path.id, name: path.name, ...metrics(members), checkpoints: members.map(cp => ({ ...cp })) };
  };
  const snapshot = () => ({ revision, paths: paths.map(serialize), checkpoints: checkpoints.map(cp => ({ ...cp })) });
  const seedData = () => {
    checkpoints = seed ? structuredClone(seedCheckpoints) : [];
    paths = seed ? [{ id: 1, name: 'Demo woodland walk', checkpointIds: [1, 2, 3] }] : [];
    nextCheckpoint = seed ? 4 : 1;
    nextPath = seed ? 2 : 1;
  };
  seedData();

  const resolveCheckpointIds = value => {
    if (!Array.isArray(value) || value.length > 500) fail('checkpoints must be an array with at most 500 entries.');
    const values = value.map(entry => id(isObject(entry) ? entry.id : entry, 'checkpoint id'));
    if (new Set(values).size !== values.length) fail('A checkpoint may appear only once in a path.');
    for (const cpId of values) if (!checkpoints.some(cp => cp.id === cpId)) fail(`Unknown checkpoint id: ${cpId}.`);
    return values;
  };
  const checkpointBody = (body, current) => {
    object(body);
    knownFields(body, ['id', 'name', 'cpLocation', 'gpsLat', 'gpsLng', 'details']);
    if (body.id !== undefined && (!current || id(body.id) !== current.id)) fail('Omit id when creating; an update id must match the URL.');
    const data = { ...current, ...body };
    return {
      name: text(data.name, 'name', true, 160),
      cpLocation: text(data.cpLocation, 'cpLocation', false, 250),
      gpsLat: coordinate(data.gpsLat, 'gpsLat', 90),
      gpsLng: coordinate(data.gpsLng, 'gpsLng', 180),
      details: text(data.details, 'details', false, 4000)
    };
  };
  const pathBody = (body, current) => {
    object(body);
    knownFields(body, ['id', 'name', 'pathLength', 'pathDuration', 'checkpoints']);
    if (body.id !== undefined && (!current || id(body.id) !== current.id)) fail('Omit id when creating; an update id must match the URL.');
    // These legacy fields are returned for compatibility; local metrics are always recalculated.
    for (const field of ['pathLength', 'pathDuration']) if (body[field] !== undefined) text(body[field], field);
    return {
      name: text(body.name === undefined ? current?.name : body.name, 'name', true, 160),
      checkpointIds: body.checkpoints === undefined ? [...(current?.checkpointIds || [])] : resolveCheckpointIds(body.checkpoints)
    };
  };

  const server = http.createServer(async (req, res) => {
    const url = new URL(req.url || '/', 'http://127.0.0.1');
    const route = url.pathname.replace(/\/$/, '') || '/';
    const requestId = `req_${++requestNumber}`;
    const origin = req.headers.origin;
    res.setHeader('Cache-Control', 'no-store');
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('X-Request-ID', requestId);
    if (origin === 'http://127.0.0.1:4200' || origin === 'http://localhost:4200') {
      res.setHeader('Access-Control-Allow-Origin', origin);
      res.setHeader('Vary', 'Origin');
      res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
      res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
      res.setHeader('Access-Control-Expose-Headers', 'X-Mock-Revision, X-Request-ID');
    }
    const send = (status, payload) => {
      res.statusCode = status;
      res.setHeader('X-Mock-Revision', String(revision));
      if (status === 204) { res.end(); return; }
      res.setHeader('Content-Type', 'application/json; charset=utf-8');
      res.end(JSON.stringify(payload));
    };
    // Polling and browser receipts do not crowd out the business requests being tested.
    res.on('finish', () => {
      if (route !== '/api/state' && !route.startsWith('/api/testing/')) {
        requests.push({ requestId, method: req.method, path: route, status: res.statusCode, revision, time: new Date().toISOString() });
        requests = requests.slice(-200);
      }
    });
    try {
      if (origin && !['http://127.0.0.1:4200', 'http://localhost:4200'].includes(origin)) fail('This local API accepts browser requests from port 4200 only.', 403);
      if (req.method === 'OPTIONS') { send(204); return; }
      if (req.method === 'GET' && route === '/api/health') {
        send(200, { status: 'ok', mode: 'local-mock', instanceId, revision, storage: 'memory', port: server.address().port }); return;
      }
      if (req.method === 'GET' && route === '/api/state') { send(200, snapshot()); return; }
      if (req.method === 'GET' && route === '/api/testing/requests') {
        send(200, { requests: requests.slice(-50) }); return;
      }
      if (req.method === 'GET' && route === '/api/testing/frontend') {
        send(200, { revision, clients: [...frontendClients.values()].filter(client => Date.now() - client.lastSeenMs < 5000) }); return;
      }
      if (req.method === 'POST' && route === '/api/testing/frontend') {
        const body = object(await readJson(req));
        const clientId = text(body.clientId, 'clientId', true, 100);
        if (!Number.isSafeInteger(body.revision) || body.revision < 0 || body.revision > revision) fail('Invalid frontend revision.');
        const ids = (value, field) => {
          if (!Array.isArray(value) || value.length > 1000) fail(`${field} must be an array of ids.`);
          return value.map(value => id(value, field));
        };
        frontendClients.set(clientId, {
          clientId, revision: body.revision, route: text(body.route, 'route', true, 250),
          visiblePathIds: ids(body.visiblePathIds, 'visiblePathIds'),
          visibleCheckpointIds: ids(body.visibleCheckpointIds, 'visibleCheckpointIds'),
          lastSeenMs: Date.now()
        });
        if (frontendClients.size > 50) frontendClients.delete(frontendClients.keys().next().value);
        send(200, { received: true }); return;
      }
      if (req.method === 'POST' && route === '/api/testing/reset') {
        await readJson(req); seedData(); revision++; frontendClients.clear(); requests = [];
        send(200, snapshot()); return;
      }
      const collection = route.match(/^\/api\/(paths|checkpoints)$/);
      if (collection) {
        const kind = collection[1];
        if (req.method === 'GET') { send(200, kind === 'paths' ? paths.map(serialize) : checkpoints); return; }
        if (req.method === 'POST') {
          const body = await readJson(req);
          if (kind === 'paths') {
            const value = { ...pathBody(body), id: nextPath++ };
            paths.push(value); revision++; send(201, serialize(value));
          } else {
            const value = { ...checkpointBody(body), id: nextCheckpoint++ };
            checkpoints.push(value); revision++; send(201, value);
          }
          return;
        }
        res.setHeader('Allow', 'GET, POST'); fail('Method not allowed.', 405);
      }
      const pathCheckpoints = route.match(/^\/api\/paths\/(\d+)\/checkpoints$/);
      if (pathCheckpoints) {
        const path = findPath(pathCheckpoints[1]);
        if (req.method === 'GET') { send(200, serialize(path).checkpoints); return; }
        if (['POST', 'PUT', 'DELETE'].includes(req.method)) {
          const values = resolveCheckpointIds(await readJson(req));
          if (req.method === 'POST') path.checkpointIds = [...new Set([...path.checkpointIds, ...values])];
          if (req.method === 'PUT') path.checkpointIds = values;
          if (req.method === 'DELETE') path.checkpointIds = path.checkpointIds.filter(cpId => !values.includes(cpId));
          revision++; send(200, serialize(path)); return;
        }
        res.setHeader('Allow', 'GET, POST, PUT, DELETE'); fail('Method not allowed.', 405);
      }
      const member = route.match(/^\/api\/(paths|checkpoints)\/(\d+)$/);
      if (member) {
        const kind = member[1], value = kind === 'paths' ? findPath(member[2]) : findCheckpoint(member[2]);
        if (req.method === 'GET') { send(200, kind === 'paths' ? serialize(value) : value); return; }
        if (req.method === 'PUT') {
          const body = await readJson(req);
          Object.assign(value, kind === 'paths' ? pathBody(body, value) : checkpointBody(body, value));
          revision++; send(200, kind === 'paths' ? serialize(value) : value); return;
        }
        if (req.method === 'DELETE') {
          if (kind === 'paths') paths = paths.filter(path => path.id !== value.id);
          else {
            checkpoints = checkpoints.filter(cp => cp.id !== value.id);
            for (const path of paths) path.checkpointIds = path.checkpointIds.filter(cpId => cpId !== value.id);
          }
          revision++; send(204); return;
        }
        res.setHeader('Allow', 'GET, PUT, DELETE'); fail('Method not allowed.', 405);
      }
      fail('Endpoint not found.', 404);
    } catch (error) {
      if (!res.headersSent) send(error instanceof ApiError ? error.status : 500, {
        error: error instanceof ApiError ? error.message : 'Unexpected local API error.', requestId
      });
      else res.end();
    }
  });
  return server;
}

if (process.argv[1] && pathToFileURL(resolve(process.argv[1])).href === import.meta.url) {
  const port = Number(process.env.MOCK_API_PORT || 3100);
  if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('MOCK_API_PORT must be between 1 and 65535.');
  const server = createMockServer();
  server.on('error', error => { console.error(`Local API could not start: ${error.message}`); process.exitCode = 1; });
  server.listen(port, '127.0.0.1', () => console.log(`Local test API: http://127.0.0.1:${port}/api (memory only)`));
  for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => server.close(() => process.exit(0)));
}
