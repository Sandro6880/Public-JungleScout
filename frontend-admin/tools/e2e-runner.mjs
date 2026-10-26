import { fork, spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { request, waitFor } from './test-http.mjs';

const args = process.argv.slice(2);
if (args.some(arg => arg !== '--http-only')) throw new Error('Supported option: --http-only');
const root = fileURLToPath(new URL('..', import.meta.url));
const env = { ...process.env, FRONTEND_URL: 'http://127.0.0.1:4200', API_URL: 'http://127.0.0.1:3100' };
const app = fork(fileURLToPath(new URL('./start-local.mjs', import.meta.url)), [], {
  cwd: root, env, stdio: ['ignore', 'inherit', 'inherit', 'ipc']
});
let appExited = false;
app.once('exit', () => { appExited = true; });
try {
  const ready = await new Promise((accept, reject) => {
    const timer = setTimeout(() => reject(new Error('Local API startup timed out.')), 15000);
    app.once('message', message => { clearTimeout(timer); accept(message); });
    app.once('exit', code => { clearTimeout(timer); reject(new Error(`Local startup exited (${code}). Stop any existing servers on ports 4200/3100 first.`)); });
    app.once('error', reject);
  });
  if (ready.type !== 'local-api-ready') throw new Error('Unexpected startup response.');
  await waitFor(async () => {
    if (appExited) throw new Error('The local app exited during compilation.');
    return (await request('http://127.0.0.1:4200/api/health')).data.instanceId === ready.instanceId;
  }, 'Angular to compile and proxy to this run\'s API', 60000);
  const script = args.includes('--http-only') ? 'tools/smoke-test.mjs' : 'tools/browser-smoke.mjs';
  const check = spawn(process.execPath, [script], { cwd: root, env, stdio: 'inherit' });
  const code = await new Promise((accept, reject) => { check.once('exit', accept); check.once('error', reject); });
  if (code !== 0) throw new Error(`Test script exited (${code}).`);
  console.log('PASS: standalone frontend integration check finished.');
} catch (error) {
  console.error(`FAIL: ${error.message}`); process.exitCode = 1;
} finally {
  if (!appExited) {
    app.kill('SIGTERM');
    await new Promise(accept => {
      const timer = setTimeout(() => { app.kill('SIGKILL'); accept(); }, 5000);
      app.once('exit', () => { clearTimeout(timer); accept(); });
    });
  }
}
