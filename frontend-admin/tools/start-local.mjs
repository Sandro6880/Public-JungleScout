import { spawn } from 'node:child_process';
import net from 'node:net';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { createMockServer } from './mock-api.mjs';

const root = fileURLToPath(new URL('..', import.meta.url));
const cli = resolve(root, 'node_modules/@angular/cli/bin/ng.js');
if (!existsSync(cli)) {
  console.error('Install the dependencies first: npm ci');
  process.exit(1);
}
const probe = net.createServer();
await new Promise((accept, reject) => {
  probe.once('error', reject);
  probe.listen(4200, '127.0.0.1', () => probe.close(accept));
}).catch(error => { console.error(`Port 4200 is unavailable: ${error.message}`); process.exit(1); });

const api = createMockServer();
await new Promise((accept, reject) => {
  api.once('error', reject);
  api.listen(3100, '127.0.0.1', accept);
}).catch(error => { console.error(`Port 3100 is unavailable: ${error.message}`); process.exit(1); });
console.log('Local test API: http://127.0.0.1:3100/api');
console.log('Admin frontend: http://127.0.0.1:4200 (ready after Angular finishes compiling)');
console.log('No Java, PostgreSQL, Google Maps key or account is needed. Ctrl+C stops both servers.');
const frontend = spawn(process.execPath, [cli, 'serve', '--configuration', 'development', '--host', '127.0.0.1', '--port', '4200'], {
  cwd: root, stdio: 'inherit', env: process.env
});
if (process.send) {
  const health = await (await fetch('http://127.0.0.1:3100/api/health')).json();
  process.send({ type: 'local-api-ready', instanceId: health.instanceId });
}
let stopping = false;
function stop(code = 0) {
  if (stopping) return;
  stopping = true;
  frontend.kill('SIGTERM');
  api.close(() => { process.exitCode = code; });
  setTimeout(() => process.exit(code), 3000).unref();
}
frontend.on('error', error => { console.error(error.message); stop(1); });
frontend.on('exit', code => stop(code || 0));
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => stop(0));
