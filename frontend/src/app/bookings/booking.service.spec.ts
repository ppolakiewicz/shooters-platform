import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting, TestRequest} from '@angular/common/http/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';

import {BookingService} from './booking.service';

describe('BookingService', () => {
  let service: BookingService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(BookingService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('loads public terms without csrf', async () => {
    const response = [sampleTerm()];

    const terms = service.publicTerms();

    const request = http.expectOne('/api/bookings/public/terms');
    expect(request.request.method).toBe('GET');
    request.flush(response);

    await expect(terms).resolves.toEqual(response);
  });

  it('creates reservation with csrf and optional account data', async () => {
    const create = service.createReservation('term-id', {
      firstName: 'Anna',
      lastName: 'Nowak',
      email: 'anna@example.com',
      phoneNumber: '+48111111111',
      createAccount: true,
      username: 'AnnaNowak',
      password: 'correct horse battery'
    });

    const request = await nextRequest('/api/bookings/reservations');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      termId: 'term-id',
      firstName: 'Anna',
      lastName: 'Nowak',
      email: 'anna@example.com',
      phoneNumber: '+48111111111',
      createAccount: true,
      username: 'AnnaNowak',
      password: 'correct horse battery'
    });
    request.flush({ type: 'RESERVATION', reservation: sampleCreatedReservation('CONFIRMED'), waitlistEntry: null });

    await expect(create).resolves.toMatchObject({ type: 'RESERVATION', reservation: { status: 'CONFIRMED', cancellationToken: 'cancel-token' } });
  });

  it('confirms waitlist offer without csrf', async () => {
    const confirm = service.confirmWaitlist('token-value');

    const request = http.expectOne('/api/bookings/reservations/confirm-waitlist-offer');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ token: 'token-value' });
    request.flush(sampleReservation('CONFIRMED'));

    await expect(confirm).resolves.toMatchObject({ status: 'CONFIRMED' });
  });

    it('loads owner terms without requesting templates', async () => {
        const response = [sampleTerm()];

        const terms = service.ownerTerms();

        const request = http.expectOne('/api/bookings/terms');
        expect(request.request.method).toBe('GET');
        request.flush(response);

        await expect(terms).resolves.toEqual(response);
    });

    it('creates a term with explicit training data', async () => {
        const requestBody = {
            name: 'Advanced pistol',
            description: 'Manual term',
            trainingLevel: 'ADVANCED' as const,
            location: {
                placeName: 'Range B',
                address: 'Second Street 2',
                latitude: 50.0614,
                longitude: 19.9383
            },
            capacity: 10,
            cancellationDeadlineDays: 3,
            durationMinutes: 120,
            startsAt: '2026-06-15T18:00'
        };

        const create = service.createTerm(requestBody);

        const request = await nextRequest('/api/bookings/terms');
        expect(request.request.method).toBe('POST');
        expect(request.request.body).toEqual(requestBody);
        request.flush(sampleTerm());

        await expect(create).resolves.toEqual(sampleTerm());
    });

  async function nextRequest(url: string): Promise<TestRequest> {
    let requests: TestRequest[] = [];
    await vi.waitFor(() => {
      requests = http.match(url);
      expect(requests).toHaveLength(1);
    });
    return requests[0];
  }
});

function sampleTerm() {
  return {
    id: 'term-id',
    name: 'Basic pistol',
    description: '',
      trainingLevel: 'BASIC',
    location: { placeName: 'Range A', address: 'Range Street 1', latitude: 52.2297, longitude: 21.0122 },
    capacity: 8,
    availablePlaces: 5,
    cancellationDeadlineDays: 2,
    durationMinutes: 90,
    startsAt: '2026-06-01T12:00:00',
    createdAt: '2026-05-08T10:00:00Z',
    updatedAt: '2026-05-08T10:00:00Z'
  };
}

function sampleReservation(status: string) {
  return {
    id: 'reservation-id',
    termId: 'term-id',
    participantUserId: null,
    firstName: 'Anna',
    lastName: 'Nowak',
    email: 'anna@example.com',
    phoneNumber: '+48111111111',
    status,
    waitlistOfferExpiresAt: null,
    createdAt: '2026-05-08T10:00:00Z',
    updatedAt: '2026-05-08T10:00:00Z'
  };
}

function sampleCreatedReservation(status: string) {
  return {
    ...sampleReservation(status),
    cancellationToken: 'cancel-token'
  };
}
