import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';

export default async function globalTeardown(): Promise<void> {
  if (process.env.E2E_SKIP_WEBSERVER) {
    return;
  }

  const result = spawnSync('docker', ['compose', 'down'], {
    cwd: resolve(__dirname, '..'),
    shell: process.platform === 'win32',
    stdio: 'inherit'
  });

  if (result.status !== 0) {
    throw new Error('Failed to stop Docker Compose services after E2E tests');
  }
}
