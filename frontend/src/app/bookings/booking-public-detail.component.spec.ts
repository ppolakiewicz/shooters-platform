import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { BookingPublicDetailComponent } from './booking-public-detail.component';
import { BookingService } from './booking.service';
import { CreatedReservation, Term } from './booking.models';

describe('BookingPublicDetailComponent', () => {
  it('loads term and prepares an OpenStreetMap preview URL', async () => {
    const service = serviceMock(sampleTerm());

    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.term()?.id).toBe('term-id'));
    expect(component.mapUrl()).not.toBeNull();
  });

  it('submits participant data with optional account fields', async () => {
    const service = serviceMock(sampleTerm());
    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.term()).not.toBeNull());
    component.model.set({
      firstName: 'Anna',
      lastName: 'Nowak',
      email: 'anna@example.com',
      phoneNumber: '+48111111111',
      createAccount: true,
      username: 'AnnaNowak',
      password: 'correct horse battery'
    });

    component.reserve();
    await vi.waitFor(() => expect(service.createReservation).toHaveBeenCalled());

    expect(service.createReservation).toHaveBeenCalledWith('term-id', expect.objectContaining({
      firstName: 'Anna',
      createAccount: true,
      username: 'AnnaNowak'
    }));
  });
});

async function createComponent(service: unknown) {
  await TestBed.configureTestingModule({
    imports: [BookingPublicDetailComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { paramMap: convertToParamMap({ id: 'term-id' }) } }
      },
      { provide: BookingService, useValue: service }
    ]
  }).compileComponents();

  const fixture = TestBed.createComponent(BookingPublicDetailComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as any };
}

function serviceMock(term: Term) {
  const reservation: CreatedReservation = {
    id: 'reservation-id',
    termId: term.id,
    participantUserId: null,
    firstName: 'Anna',
    lastName: 'Nowak',
    email: 'anna@example.com',
    phoneNumber: '+48111111111',
    status: 'CONFIRMED',
    waitlistPosition: 0,
    cancellationToken: 'cancel-token',
    waitlistOfferExpiresAt: null,
    createdAt: '2026-05-08T10:00:00Z',
    updatedAt: '2026-05-08T10:00:00Z'
  };
  return {
    publicTerm: vi.fn().mockResolvedValue(term),
    createReservation: vi.fn().mockResolvedValue(reservation),
    error: vi.fn()
  };
}

function sampleTerm(): Term {
  return {
    id: 'term-id',
    name: 'Basic pistol',
    description: 'Safety and stance',
    location: { placeName: 'Range A', address: 'Range Street 1', latitude: 52.2297, longitude: 21.0122 },
    capacity: 8,
    cancellationDeadlineDays: 2,
    durationMinutes: 90,
    startsAt: '2026-06-01T12:00:00',
    createdAt: '2026-05-08T10:00:00Z',
    updatedAt: '2026-05-08T10:00:00Z'
  };
}
