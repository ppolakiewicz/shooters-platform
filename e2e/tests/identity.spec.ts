import {expect, test} from '@playwright/test';

test('registers, logs out, and logs in again', async ({ page }) => {
  // given: a new user identity for the authentication journey
  const email = `e2e-${Date.now()}-${crypto.randomUUID()}@example.com`;
  const username = `E2E_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const password = 'correct horse battery';

  // when: the anonymous user opens the application
  await page.goto('/');

  // then: the application redirects to the login page
  await expect(page).toHaveTitle(/Shooters Platform/);
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Log in' })).toBeVisible();

  // when: the user starts registration
  await page.getByRole('link', { name: 'Create account' }).click();
  await expect(page.getByRole('heading', { name: 'Create account' })).toBeVisible();

  // when: the user creates an account
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Create account' }).click();

  // then: the user lands on the authenticated home page
    await expect(page.getByRole('heading', {name: 'Dashboard'})).toBeVisible();
  await expect(page.getByText(`Signed in as ${username}`)).toBeVisible();
    await expect(page.getByRole('link', {name: 'Bookings'})).toBeVisible();
    await expect(page.getByRole('link', {name: 'Public terms'})).toBeVisible();

  // when: the user logs out
  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page.getByRole('heading', { name: 'Log in' })).toBeVisible();

  // when: the user logs in again with the same credentials
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Log in' }).click();

  // then: the previous account is authenticated again
    await expect(page.getByRole('heading', {name: 'Dashboard'})).toBeVisible();
  await expect(page.getByText(`Signed in as ${username}`)).toBeVisible();
});
