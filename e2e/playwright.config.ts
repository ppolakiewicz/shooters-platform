import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  reporter: 'html',
  globalTeardown: './global-teardown.ts',
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:4200',
    trace: 'on-first-retry'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  webServer: process.env.E2E_SKIP_WEBSERVER
    ? undefined
    : [
        {
          command: 'powershell -NoProfile -ExecutionPolicy Bypass -Command "$repo = Get-Location; try { docker compose up -d postgres; for ($i = 0; $i -lt 60; $i++) { if ((docker inspect --format \\"{{.State.Health.Status}}\\" shooters-platform-postgres) -eq \\"healthy\\") { break }; Start-Sleep -Seconds 1 }; Set-Location backend; .\\gradlew.bat bootRun } finally { Set-Location $repo; docker compose down }"',
          cwd: '..',
            url: 'http://localhost:8080/actuator/health',
          reuseExistingServer: true,
          timeout: 180_000
        },
        {
          command: 'npm run start --workspace frontend',
          cwd: '..',
          url: 'http://localhost:4200',
          reuseExistingServer: true,
          timeout: 120_000
        }
      ]
});
