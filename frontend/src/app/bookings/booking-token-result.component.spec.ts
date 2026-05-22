import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { BookingTokenResultComponent } from './booking-token-result.component';
import { BookingService } from './booking.service';
import { ReservationSummary } from './booking.models';

describe('BookingTokenResultComponent', () => {
  it('confirms waitlist token from route data', async () => {
    const service = serviceMock();

    const { component } = await createComponent(service, 'confirm');

    await vi.waitFor(() => expect(service.confirmWaitlist).toHaveBeenCalledWith('token-value'));
    expect(component.reservation()?.status).toBe('CONFIRMED');
  });

  it('cancels reservation token from route data', async () => {
    const service = serviceMock();

    await createComponent(service, 'cancel');

    await vi.waitFor(() => expect(service.cancelByToken).toHaveBeenCalledWith('token-value'));
  });
});

async function createComponent(service: unknown, action: 'confirm' | 'cancel') {
  await TestBed.configureTestingModule({
    imports: [BookingTokenResultComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { paramMap: convertToParamMap({ token: 'token-value' }), data: { action } } }
      },
      { provide: BookingService, useValue: service }
    ]
  }).compileComponents();

  const fixture = TestBed.createComponent(BookingTokenResultComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as unknown as BookingTokenResultComponentTestAccess };
}

interface BookingTokenResultComponentTestAccess {
  reservation: () => ReservationSummary | null;
}

function serviceMock() {
  const reservation: ReservationSummary = {
    id: 'reservation-id',
    termId: 'term-id',
    participantUserId: null,
    firstName: 'Anna',
    lastName: 'Nowak',
    email: 'anna@example.com',
    phoneNumber: '+48111111111',
    status: 'CONFIRMED',
    waitlistOfferExpiresAt: null,
    createdAt: '2026-05-08T10:00:00Z',
    updatedAt: '2026-05-08T10:00:00Z'
  };
  return {
    confirmWaitlist: vi.fn().mockResolvedValue(reservation),
    cancelByToken: vi.fn().mockResolvedValue({ ...reservation, status: 'CANCELLED_BY_PARTICIPANT' }),
    error: vi.fn()
  };
}
