import { expect, test } from '@playwright/test';

test('shows backend and database status', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveTitle(/Shooters Platform/);
  await expect(page.getByRole('heading', { name: 'Service status' })).toBeVisible();
  await expect(page.getByText('Backend')).toBeVisible();
  await expect(page.getByText('Database')).toBeVisible();
});
