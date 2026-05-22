import {expect, test} from '@playwright/test';

test('creates a booking term and promotes waitlisted participant after cancellation', async ({ page }) => {
  // given: a registered instructor manages booking terms
  const instructorEmail = `booking-owner-${Date.now()}-${crypto.randomUUID()}@example.com`;
  const username = `Booking_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const password = 'correct horse battery';

  // when: the instructor registers and opens booking management
  await page.goto('/');
  await page.getByRole('link', { name: 'Create account' }).click();
  await page.getByLabel('Email').fill(instructorEmail);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Create account' }).click();
  await page.getByRole('link', { name: 'Bookings' }).click();

  // when: the instructor creates a one-seat training enrollment and term
  await expect(page.getByRole('heading', { name: 'Booking management' })).toBeVisible();
  const enrollmentForm = page.locator('form').filter({ has: page.getByRole('heading', { name: 'New TrainingEnrollment' }) });
  const termForm = page.locator('form').filter({ has: page.getByRole('heading', { name: 'New Term' }) });
  await enrollmentForm.getByLabel('Name').fill('Intro pistol');
  await enrollmentForm.getByLabel('Capacity').fill('1');
  const enrollmentResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/training-enrollments') && response.request().method() === 'POST'
  );
  await page.getByRole('button', { name: 'Create enrollment' }).click();
  expect((await enrollmentResponse).ok()).toBeTruthy();

  const termResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/terms') && response.request().method() === 'POST'
  );
  await termForm.getByRole('button', { name: 'Create term' }).click();
  const term = await (await termResponse).json() as { id: string };

  // when: the instructor logs out and the first guest reserves the only place
  await page.getByRole('link', { name: 'Home' }).click();
  await page.getByRole('button', { name: 'Logout' }).click();
  await page.goto(`/booking-terms/${term.id}`);
  await page.getByLabel('First name').fill('Anna');
  await page.getByLabel('Last name').fill('Nowak');
  await page.getByLabel('Email').fill(`anna-${Date.now()}@example.com`);
  await page.getByLabel('Phone number').fill('+48111111111');
  const firstReservationResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/reservations') && response.request().method() === 'POST'
  );
  await page.getByRole('button', { name: 'Reserve' }).click();
  const firstBooking = await (await firstReservationResponse).json() as {
    type: string;
    reservation: { status: string; cancellationToken: string };
  };

  // then: the first reservation is confirmed
  expect(firstBooking.type).toBe('RESERVATION');
  expect(firstBooking.reservation.status).toBe('CONFIRMED');
  await expect(page.getByRole('heading', { name: 'Reservation CONFIRMED' })).toBeVisible();

  // when: the second guest registers for the full term
  await page.getByLabel('First name').fill('Jan');
  await page.getByLabel('Last name').fill('Kowalski');
  await page.getByLabel('Email').fill(`jan-${Date.now()}@example.com`);
  await page.getByLabel('Phone number').fill('+48222222222');
  const secondReservationResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/reservations') && response.request().method() === 'POST'
  );
  await page.getByRole('button', { name: 'Reserve' }).click();
  const secondBooking = await (await secondReservationResponse).json() as {
    type: string;
    waitlistEntry: { position: number };
  };

  // then: the second reservation enters the waitlist
  expect(secondBooking.type).toBe('WAITLIST_ENTRY');
  expect(secondBooking.waitlistEntry.position).toBe(1);
  await expect(page.getByRole('heading', { name: 'Waitlist position 1' })).toBeVisible();

  // when: the first guest cancels through the token link
  const cancellationResponse = await page.request.post('/api/bookings/reservations/cancel-by-participant', {
    data: { token: firstBooking.reservation.cancellationToken }
  });
  expect(cancellationResponse.ok()).toBeTruthy();

  // when: the instructor logs in and reads the promoted waitlist reservation from the management API
  await page.goto('/login');
  await page.getByLabel('Email').fill(instructorEmail);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Log in' }).click();
  await expect(page.getByText(username)).toBeVisible();
  const csrfResponse = await page.request.get('/api/auth/csrf');
  expect(csrfResponse.ok()).toBeTruthy();
  const csrfCookie = (await page.context().cookies()).find((cookie) => cookie.name === 'XSRF-TOKEN');
  expect(csrfCookie?.value).toBeTruthy();
  const reservationsResponse = await page.request.get(`/api/bookings/terms/${term.id}/reservations`, {
    headers: { 'X-XSRF-TOKEN': csrfCookie?.value ?? '' }
  });
  expect(reservationsResponse.ok()).toBeTruthy();
  const reservations = await reservationsResponse.json() as Array<{ email: string; status: string; waitlistConfirmationToken?: string; cancellationToken?: string }>;
  const offered = reservations.find((reservation) => reservation.status === 'WAITLIST_OFFERED');

  // then: the waitlisted participant receives an offer without leaking secret tokens through management API
  expect(offered).toBeTruthy();
  expect(offered).not.toHaveProperty('waitlistConfirmationToken');
  expect(offered).not.toHaveProperty('cancellationToken');
});

test('updates available places on the public term list after reservation', async ({ page }) => {
  // given: a registered instructor creates a three-seat public term
  const instructorEmail = `availability-${Date.now()}@example.com`;
  const username = `Booking_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const password = 'correct horse battery';
  const termName = `Availability pistol ${crypto.randomUUID().slice(0, 8)}`;

  await page.goto('/');
  await page.getByRole('link', { name: 'Create account' }).click();
  await page.getByLabel('Email').fill(instructorEmail);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Create account' }).click();
  await page.getByRole('link', { name: 'Bookings' }).click();

  await expect(page.getByRole('heading', { name: 'Booking management' })).toBeVisible();
  const enrollmentForm = page.locator('form').filter({ has: page.getByRole('heading', { name: 'New TrainingEnrollment' }) });
  const termForm = page.locator('form').filter({ has: page.getByRole('heading', { name: 'New Term' }) });
  await enrollmentForm.getByLabel('Name').fill(termName);
  await enrollmentForm.getByLabel('Capacity').fill('3');
  const enrollmentResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/training-enrollments') && response.request().method() === 'POST'
  );
  await page.getByRole('button', { name: 'Create enrollment' }).click();
  expect((await enrollmentResponse).ok()).toBeTruthy();
  await expect(termForm.getByLabel('Name')).toHaveValue(termName);
  await expect(termForm.getByLabel('Capacity')).toHaveValue('3');

  const termResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/terms') && response.request().method() === 'POST'
  );
  await termForm.getByRole('button', { name: 'Create term' }).click();
  expect((await termResponse).ok()).toBeTruthy();

  // when: public users view the term before reservation
  await page.getByRole('link', { name: 'Public terms' }).click();
  const termRow = page.locator('.term-row').filter({ has: page.getByRole('heading', { name: termName }) });

  // then: all three places are available
  await expect(termRow).toContainText('Available');
  await expect(termRow).toContainText('3 places');

  // when: a guest reserves one place
  await termRow.getByRole('link', { name: 'Reserve' }).click();
  await page.getByLabel('First name').fill('Anna');
  await page.getByLabel('Last name').fill('Nowak');
  await page.getByLabel('Email').fill(`availability-${Date.now()}@example.com`);
  await page.getByLabel('Phone number').fill('+48111111111');
  const reservationResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/bookings/reservations') && response.request().method() === 'POST'
  );
  await page.getByRole('button', { name: 'Reserve' }).click();
  const reservation = await (await reservationResponse).json() as { type: string; reservation: { status: string } };
  expect(reservation.type).toBe('RESERVATION');
  expect(reservation.reservation.status).toBe('CONFIRMED');

  // then: public list shows one fewer available place
  await page.goto('/booking-terms');
  const updatedTermRow = page.locator('.term-row').filter({ has: page.getByRole('heading', { name: termName }) });
  await expect(updatedTermRow).toContainText('2 places');
});
