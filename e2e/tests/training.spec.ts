import { expect, test } from '@playwright/test';

test('manages planned training and shooting tasks', async ({ page }) => {
  // given: a registered user is needed to manage trainings
  const email = `training-${Date.now()}-${crypto.randomUUID()}@example.com`;
  const username = `Training_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const password = 'correct horse battery';

  // when: the user registers and enters the authenticated application
  await page.goto('/');
  await page.getByRole('link', { name: 'Create account' }).click();
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Create account' }).click();

  // when: the user opens the training module
  await page.getByRole('link', { name: 'Trainings' }).click();

  // then: the training list is visible
  await expect(page.getByRole('heading', { name: 'Trainings' })).toBeVisible();

  // when: the user creates a training draft
  await page.getByRole('button', { name: 'New training' }).click();
  await expect(page.getByRole('heading', { name: 'New training' })).toBeVisible();

  // when: the user fills planned training details
  await page.getByLabel('Name').fill('Future classifier');
  await page.getByLabel('Place').fill('Range 12');
  await page.getByLabel('Date').fill('2026-06-01');
  await page.getByLabel('Description').fill('Bring IDPA timer and target patches');
  await page.getByRole('button', { name: 'Save training' }).click();

  // then: the updated training details are shown
  await expect(page.getByRole('heading', { name: 'Future classifier' })).toBeVisible();

  // when: the user saves the default IDPA shooting task
  const taskForm = page.locator('form.task-form');
  await taskForm.getByRole('button', { name: 'Save task' }).click();

  // then: the first run is recorded with IDPA score
  await expect(page.getByRole('cell', { name: '1', exact: true })).toBeVisible();
  await expect(page.getByText('A 1 / C 0 / D 0 / M 0')).toBeVisible();

  // when: the user adds a target scoring task with a zero-value hit
  await taskForm.getByLabel('Scoring').click();
  await page.getByRole('option', { name: 'TARGET' }).click();
  await taskForm.getByLabel('0', { exact: true }).fill('1');
  await taskForm.getByLabel('10', { exact: true }).fill('2');
  await taskForm.getByRole('button', { name: 'Save task' }).click();

  // then: the second run is recorded with target score details
  await expect(page.getByRole('cell', { name: '2', exact: true })).toBeVisible();
  await expect(page.getByText('0:1 / 10:2')).toBeVisible();

  // when: the user edits the first task score
  const firstRun = page.getByRole('row').filter({ hasText: 'A 1 / C 0 / D 0 / M 0' });
  await firstRun.getByRole('button', { name: 'Edit task' }).click();
  await taskForm.getByLabel('Miss').fill('1');
  await taskForm.getByRole('button', { name: 'Save task' }).click();

  // then: the updated IDPA score is visible
  await expect(page.getByText('A 1 / C 0 / D 0 / M 1')).toBeVisible();

  // when: the user deletes the edited first task
  const updatedFirstRun = page.getByRole('row').filter({ hasText: 'A 1 / C 0 / D 0 / M 1' });
  page.once('dialog', (dialog) => dialog.accept());
  await updatedFirstRun.getByRole('button', { name: 'Delete task' }).click();

  // then: the deleted task is no longer visible
  await expect(page.getByText('A 1 / C 0 / D 0 / M 1')).toBeHidden();

  // when: the user returns to the training list
  await page.getByRole('link', { name: 'Trainings' }).click();

  // then: the planned training summary is visible
  await expect(page.getByText('Future classifier')).toBeVisible();
  await expect(page.getByText('Bring IDPA timer and target patches')).toBeVisible();

  // when: the user deletes the training
  page.once('dialog', (dialog) => dialog.accept());
  await page.getByRole('button', { name: 'Delete training' }).click();

  // then: the training list is empty again
  await expect(page.getByText('No trainings yet')).toBeVisible();
});
