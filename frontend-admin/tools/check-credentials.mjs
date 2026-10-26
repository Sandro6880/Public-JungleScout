import { readdir, readFile } from 'node:fs/promises';
import { resolve, relative, extname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { inflateSync } from 'node:zlib';

const root = process.argv[2] ? resolve(process.argv[2]) : fileURLToPath(new URL('../../', import.meta.url));
const excluded = new Set(['node_modules', '.git', '.angular', 'dist', 'out-tsc', 'test-results', 'playwright-report']);
const findings = [];
let count = 0, pdfStreams = 0;
const rules = [
  ['Google API key', /AIza[0-9A-Za-z_-]{35}/g],
  ['AWS access key', /\b(?:AKIA|ASIA)[A-Z0-9]{16}\b/g],
  ['GitHub token', /\b(?:gh[pousr]_[A-Za-z0-9]{20,255}|github_pat_[A-Za-z0-9_]{30,255})\b/g],
  ['GitLab token', /\bglpat-[A-Za-z0-9_-]{15,255}\b/g],
  ['Stripe secret', /\b(?:sk|rk)_(?:live|test)_[A-Za-z0-9]{10,255}\b/g],
  ['OpenAI style secret', /\bsk-(?:proj-|svcacct-)?[A-Za-z0-9_-]{20,255}\b/g],
  ['Slack token', /\bxox[baprs]-[A-Za-z0-9-]{10,255}\b/g],
  ['SendGrid token', /\bSG\.[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{20,}\b/g],
  ['JWT', /\beyJ[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\b/g],
  ['Private key', /-----BEGIN (?:RSA |EC |OPENSSH |DSA |ENCRYPTED )?PRIVATE KEY-----/g],
  ['Credential URL', /(?:https?|postgres(?:ql)?|mongodb(?:\+srv)?|mysql|redis):\/\/[^\s:/"'<>]+:[^\s/@"'<>]+@[^\s"'<>]+/g]
];
const add = (file, rule, text, offset = 0) => findings.push({ file, rule, line: text.slice(0, offset).split('\n').length });
function scan(text, file) {
  for (const [rule, pattern] of rules) {
    pattern.lastIndex = 0;
    for (const match of text.matchAll(pattern)) add(file, rule, text, match.index);
  }
}
function literal(value, file, field, text, index) {
  if (value && !value.startsWith('example_') && !value.startsWith('@string/')) add(file, `${field} must use an example_ placeholder`, text, index);
}

async function walk(directory) {
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    const file = resolve(directory, entry.name);
    if (entry.isDirectory()) { if (!excluded.has(entry.name)) await walk(file); continue; }
    if (!entry.isFile()) continue;
    count++;
    const name = relative(root, file);
    const bytes = await readFile(file);
    const text = bytes.toString('utf8');
    scan(text, name);
    scan(bytes.toString('utf16le'), `${name} (UTF-16)`);
    if (extname(name).toLowerCase() === '.pdf') {
      // Scan decoded Flate streams as well as the visible/raw PDF bytes.
      const raw = bytes.toString('latin1');
      for (const match of raw.matchAll(/stream\r?\n([\s\S]*?)\r?\nendstream/g)) {
        try { const decoded = inflateSync(Buffer.from(match[1], 'latin1')); scan(decoded.toString('utf8'), `${name} (decoded PDF stream)`); pdfStreams++; }
        catch { /* DCT image and other non-Flate streams are not text. */ }
      }
      continue;
    }
    if (!['.png', '.jpg', '.jpeg', '.jar'].includes(extname(name).toLowerCase())) {
      const assignment = /(?:^|[\s{,(;.])["']?(api_?key|client_?secret|access_?token|refresh_?token|password|passwd|pwd|pw|secret)\b["']?\s*[:=]\s*(["'])([^"'\r\n]*)\2/gim;
      for (const match of text.matchAll(assignment)) literal(match[3], name, match[1], text, match.index);
      for (const match of text.matchAll(/[?&](key|api_key|access_token|token)=([^\s"'<>+]+)/gi)) literal(match[2], name, match[1], text, match.index);
      for (const match of text.matchAll(/<string\b[^>]*name="google_maps_key"[^>]*>([^<]+)</g)) literal(match[1], name, 'google_maps_key', text, match.index);
      for (const match of text.matchAll(/<meta-data\b[^>]*android:name="com\.google\.android\.geo\.API_KEY"[^>]*android:value="([^"]+)"/g)) literal(match[1], name, 'Google Maps manifest key', text, match.index);
      for (const match of text.matchAll(/^\s*quarkus\.datasource\.(username|password)\s*=\s*([^\r\n]+)/gm)) literal(match[2].trim(), name, match[1], text, match.index);
      for (const match of text.matchAll(/String\s+(sender|pw)\s*=\s*"([^"]+)"/g)) literal(match[2], name, match[1], text, match.index);
    }
  }
}

await walk(root);
if (findings.length) {
  for (const finding of findings) console.error(`${finding.file}:${finding.line}: ${finding.rule} (value omitted)`);
  console.error(`FAIL: ${findings.length} finding(s).`);
  process.exitCode = 1;
} else {
  console.log(`PASS: scanned ${count} files and ${pdfStreams} decoded PDF streams.`);
  console.log('No supported credential patterns found; identified credential literals use example_ placeholders.');
  console.log('Scope: project files; dependency/build folders and Git history are excluded. This command does not perform image OCR or contact services.');
}
