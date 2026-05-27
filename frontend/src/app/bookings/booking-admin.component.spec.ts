import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {describe, expect, it, vi} from 'vitest';

import {BookingAdminComponent} from './booking-admin.component';
import {BookingService} from './booking.service';
import {ReservationSummary, Term, TrainingEnrollment, WaitlistEntrySummary} from './booking.models';

describe('BookingAdminComponent', () => {
  it('loads enrollments and terms for management', async () => {
    const service = serviceMock();

    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.enrollments()).toHaveLength(1));
    expect(component.terms()[0].name).toBe('Basic pistol');
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

    it('sends training level when creating an enrollment', async () => {
        const service = serviceMock();
        service.createEnrollment.mockResolvedValue(sampleEnrollment({
            id: 'new-enrollment-id',
            trainingLevel: 'ADVANCED'
        }));
        const {component} = await createComponent(service);

        component.enrollmentModel.set({
            name: 'Advanced pistol',
            description: '',
            trainingLevel: 'ADVANCED',
            placeName: 'Range A',
            address: 'Range Street 1',
            latitude: 52.2297,
            longitude: 21.0122,
            capacity: 8,
            cancellationDeadlineDays: 2,
            durationMinutes: 90
        });
        component.createEnrollment();

        await vi.waitFor(() => expect(service.createEnrollment).toHaveBeenCalled());
        expect(service.createEnrollment).toHaveBeenCalledWith(expect.objectContaining({trainingLevel: 'ADVANCED'}));
    });

    it('copies enrollment training level to the term form', async () => {
        const service = serviceMock();
        const {component} = await createComponent(service);

        await vi.waitFor(() => expect(component.termModel().trainingLevel).toBe('INTERMEDIATE'));
    });

    it('sends copied training level when creating a term', async () => {
        const service = serviceMock();
        service.createTerm.mockResolvedValue(sampleTerm({trainingLevel: 'INTERMEDIATE'}));
        const {component} = await createComponent(service);

        await vi.waitFor(() => expect(component.termModel().trainingLevel).toBe('INTERMEDIATE'));
        component.createTerm();

        await vi.waitFor(() => expect(service.createTerm).toHaveBeenCalled());
        expect(service.createTerm).toHaveBeenCalledWith(expect.objectContaining({trainingLevel: 'INTERMEDIATE'}));
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
  enrollments: () => TrainingEnrollment[];
  terms: () => Term[];
  reservations: () => ReservationSummary[];
  waitlistEntries: () => WaitlistEntrySummary[];
    enrollmentModel: {
        set(value: {
            name: string;
            description: string;
            trainingLevel: 'BASIC' | 'INTERMEDIATE' | 'ADVANCED';
            placeName: string;
            address: string;
            latitude: number;
            longitude: number;
            capacity: number;
            cancellationDeadlineDays: number;
            durationMinutes: number;
        }): void;
    };
    termModel: () => {
        trainingLevel: 'BASIC' | 'INTERMEDIATE' | 'ADVANCED';
    };

    createEnrollment(): void;

    createTerm(): void;
  openReservations(term: Term): Promise<void>;
}

function serviceMock() {
  return {
    enrollments: vi.fn().mockResolvedValue([sampleEnrollment()]),
    ownerTerms: vi.fn().mockResolvedValue([sampleTerm()]),
    createEnrollment: vi.fn(),
    createTerm: vi.fn(),
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

function sampleEnrollment(overrides: Partial<TrainingEnrollment> = {}): TrainingEnrollment {
  return {
    id: 'enrollment-id',
    name: 'Basic pistol',
    description: '',
      trainingLevel: 'INTERMEDIATE',
    location: { placeName: 'Range A', address: 'Range Street 1', latitude: 52.2297, longitude: 21.0122 },
    capacity: 8,
    cancellationDeadlineDays: 2,
    durationMinutes: 90,
    createdAt: '2026-05-08T10:00:00Z',
      updatedAt: '2026-05-08T10:00:00Z',
      ...overrides
  };
}

function sampleTerm(overrides: Partial<Term> = {}): Term {
  return {
    ...sampleEnrollment(),
    id: 'term-id',
    availablePlaces: 5,
      startsAt: '2026-06-01T12:00:00',
      ...overrides
  };
}
