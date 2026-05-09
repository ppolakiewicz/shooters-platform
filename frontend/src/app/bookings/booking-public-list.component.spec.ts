import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { BookingPublicListComponent } from './booking-public-list.component';
import { BookingService } from './booking.service';
import { Term } from './booking.models';

describe('BookingPublicListComponent', () => {
  it('loads public terms', async () => {
    const service = {
      publicTerms: vi.fn().mockResolvedValue([sampleTerm()]),
      error: vi.fn()
    };

    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.terms()).toHaveLength(1));
    expect(component.terms()[0].name).toBe('Basic pistol');
  });
});

async function createComponent(service: unknown) {
  await TestBed.configureTestingModule({
    imports: [BookingPublicListComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      { provide: BookingService, useValue: service }
    ]
  }).compileComponents();

  const fixture = TestBed.createComponent(BookingPublicListComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as any };
}

function sampleTerm(): Term {
  return {
    id: 'term-id',
    name: 'Basic pistol',
    description: '',
    location: { placeName: 'Range A', address: 'Range Street 1', latitude: 52.2297, longitude: 21.0122 },
    capacity: 8,
    cancellationDeadlineDays: 2,
    durationMinutes: 90,
    startsAt: '2026-06-01T12:00:00',
    createdAt: '2026-05-08T10:00:00Z',
    updatedAt: '2026-05-08T10:00:00Z'
  };
}
