import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {describe, expect, it, vi} from 'vitest';

import {BookingAdminComponent} from './booking-admin.component';
import {BookingService} from './booking.service';
import {ReservationSummary, Term, TrainingLevel, WaitlistEntrySummary} from './booking.models';

describe('BookingAdminComponent', () => {
    it('loads only owner terms for management', async () => {
    const service = serviceMock();

    const { component } = await createComponent(service);

        await vi.waitFor(() => expect(component.terms()).toHaveLength(1));
        expect(service.ownerTerms).toHaveBeenCalledOnce();
    expect(component.terms()[0].name).toBe('Basic pistol');
  });

    it('creates a term from manually entered training data', async () => {
        const service = serviceMock();
        const {component} = await createComponent(service);
        component.termModel.set({
            name: 'Advanced pistol',
            description: 'Manual term',
            trainingLevel: 'ADVANCED',
            placeName: 'Range B',
            address: 'Second Street 2',
            latitude: 50.0614,
            longitude: 19.9383,
            capacity: 10,
            cancellationDeadlineDays: 3,
            durationMinutes: 120,
            startsAt: '2026-06-15T18:00'
        });

        component.createTerm();

        await vi.waitFor(() => expect(service.createTerm).toHaveBeenCalledOnce());
        expect(service.createTerm).toHaveBeenCalledWith({
            name: 'Advanced pistol',
            description: 'Manual term',
            trainingLevel: 'ADVANCED',
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
        });
    });

  it('opens reservations for selected term', async () => {
    const service = serviceMock();
    const { component } = await createComponent(service);

    await component.openReservations(sampleTerm());

    expect(service.reservations).toHaveBeenCalledWith('term-id');
    expect(service.waitlistEntries).toHaveBeenCalledWith('term-id');
    expect(component.reservations()[0].status).toBe('CONFIRMED');
    expect(component.waitlistEntries()[0].position).toBe(1);
  });

  it('labels management tables for compact mobile presentation', async () => {
    const service = serviceMock();
    const { component, fixture } = await createComponent(service);

    await vi.waitFor(() => expect(fixture.nativeElement.querySelector('td[data-label="Start"]')).not.toBeNull());
    expect(fixture.nativeElement.querySelector('td[data-label="Name"]')?.textContent).toContain('Basic pistol');
      expect(fixture.nativeElement.querySelector('td[data-label="Training level"]')?.textContent).toContain('Intermediate');

    await component.openReservations(sampleTerm());
    fixture.detectChanges();

    await vi.waitFor(() => expect(fixture.nativeElement.querySelector('td[data-label="Participant"]')).not.toBeNull());
    expect(fixture.nativeElement.querySelector('td[data-label="Status"]')?.textContent).toContain('CONFIRMED');
    expect(fixture.nativeElement.querySelector('td[data-label="Position"]')?.textContent).toContain('1');
  });

});

async function createComponent(service: unknown) {
  await TestBed.configureTestingModule({
    imports: [BookingAdminComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      { provide: BookingService, useValue: service }
    ]
  }).compileComponents();

  const fixture = TestBed.createComponent(BookingAdminComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as unknown as BookingAdminComponentTestAccess, fixture };
}

interface BookingAdminComponentTestAccess {
  terms: () => Term[];
  reservations: () => ReservationSummary[];
  waitlistEntries: () => WaitlistEntrySummary[];
    termModel: {
        set(value: {
            name: string;
            description: string;
            trainingLevel: TrainingLevel;
            placeName: string;
            address: string;
            latitude: number;
            longitude: number;
            capacity: number;
            cancellationDeadlineDays: number;
            durationMinutes: number;
            startsAt: string;
        }): void;
    };

    createTerm(): void;
  openReservations(term: Term): Promise<void>;
}

function serviceMock() {
  return {
    ownerTerms: vi.fn().mockResolvedValue([sampleTerm()]),
      createTerm: vi.fn().mockResolvedValue(sampleTerm({name: 'Advanced pistol', trainingLevel: 'ADVANCED'})),
    reservations: vi.fn().mockResolvedValue([{
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
    }]),
    waitlistEntries: vi.fn().mockResolvedValue([{
      id: 'waitlist-entry-id',
      termId: 'term-id',
      participantUserId: null,
      firstName: 'Jan',
      lastName: 'Kowalski',
      email: 'jan@example.com',
      phoneNumber: '+48222222222',
      position: 1,
      createdAt: '2026-05-08T10:00:00Z',
      updatedAt: '2026-05-08T10:00:00Z'
    }]),
    cancelReservation: vi.fn(),
    expireOffers: vi.fn(),
    removeWaitlistEntry: vi.fn(),
    error: vi.fn()
  };
}

function sampleTerm(overrides: Partial<Term> = {}): Term {
  return {
      id: 'term-id',
    name: 'Basic pistol',
    description: '',
      trainingLevel: 'INTERMEDIATE',
    location: { placeName: 'Range A', address: 'Range Street 1', latitude: 52.2297, longitude: 21.0122 },
    capacity: 8,
      availablePlaces: 5,
    cancellationDeadlineDays: 2,
    durationMinutes: 90,
      startsAt: '2026-06-01T12:00:00',
    createdAt: '2026-05-08T10:00:00Z',
      updatedAt: '2026-05-08T10:00:00Z',
      ...overrides
  };
}
