import { expect, test } from '@playwright/test';

test('registers, logs out, and logs in again', async ({ page }) => {
  const email = `e2e-${Date.now()}-${crypto.randomUUID()}@example.com`;
  const password = 'correct horse battery';

  await page.goto('/');

  await expect(page).toHaveTitle(/Shooters Platform/);
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Log in' })).toBeVisible();

  await page.getByRole('link', { name: 'Create account' }).click();
  await expect(page.getByRole('heading', { name: 'Create account' })).toBeVisible();

  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Create account' }).click();

  await expect(page.getByRole('heading', { name: 'Service status' })).toBeVisible();
  await expect(page.getByText(`Signed in as ${email}`)).toBeVisible();
  await expect(page.getByText('Backend')).toBeVisible();
  await expect(page.getByText('Database')).toBeVisible();

  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page.getByRole('heading', { name: 'Log in' })).toBeVisible();

  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Log in' }).click();

  await expect(page.getByRole('heading', { name: 'Service status' })).toBeVisible();
  await expect(page.getByText(`Signed in as ${email}`)).toBeVisible();
});
