export const frontendUrl = (process.env.FRONTEND_URL || 'http://127.0.0.1:4200').replace(/\/$/, '');
export const apiUrl = (process.env.API_URL || 'http://127.0.0.1:3100').replace(/\/$/, '');

export async function request(url, { method = 'GET', body, expected = 200 } = {}) {
  let response;
  try {
    response = await fetch(url, {
      method, headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(8000)
    });
  } catch (error) {
    throw new Error(`Cannot reach ${url}: ${error.cause?.message || error.message}. Start npm start in this same machine/workspace.`);
  }
  const raw = await response.text();
  let data;
  try { data = raw ? JSON.parse(raw) : null; }
  catch { throw new Error(`${method} ${url} did not return JSON (${response.status}).`); }
  const allowed = Array.isArray(expected) ? expected : [expected];
  if (!allowed.includes(response.status)) throw new Error(`${method} ${url}: expected ${allowed.join('/')}, received ${response.status}: ${data?.error || raw}`);
  return { data, status: response.status, revision: Number(response.headers.get('x-mock-revision')), requestId: response.headers.get('x-request-id') };
}

export async function waitFor(check, label, timeout = 15000) {
  const deadline = Date.now() + timeout;
  let lastError;
  do {
    try { if (await check()) return; } catch (error) { lastError = error; }
    await new Promise(accept => setTimeout(accept, 250));
  } while (Date.now() < deadline);
  throw new Error(`Timed out waiting for ${label}.${lastError ? ` ${lastError.message}` : ''}`);
}

export async function requireLocalApi() {
  const direct = await request(`${apiUrl}/api/health`);
  const proxy = await request(`${frontendUrl}/api/health`);
  if (direct.data?.mode !== 'local-mock' || direct.data.instanceId !== proxy.data?.instanceId) {
    throw new Error('The API and frontend proxy are not connected to the same local test server.');
  }
}
