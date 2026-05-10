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

  it('shows term details in the public list', async () => {
    const service = {
      publicTerms: vi.fn().mockResolvedValue([sampleTerm()]),
      error: vi.fn()
    };

    const { fixture } = await createComponent(service);

    await vi.waitFor(() => expect(fixture.nativeElement.textContent).toContain('Basic pistol'));

    const content = fixture.nativeElement.textContent;
    const reserveLink = fixture.nativeElement.querySelector('a[href="/booking-terms/term-id"]');
    expect(content).toContain('Range A');
    expect(content).toContain('Range Street 1');
    expect(content).toContain('90 min');
    expect(content).toContain('8 places');
    expect(content).toContain('Cancel up to 2 days before start');
    expect(reserveLink).not.toBeNull();
  });

  it('renders terms as list rows with the current public fields', async () => {
    const service = {
      publicTerms: vi.fn().mockResolvedValue([
        sampleTerm({ id: 'first-term-id', name: 'First training', description: 'First description' }),
        sampleTerm({
          id: 'second-term-id',
          name: 'Second training',
          description: 'Second description',
          location: { placeName: 'Range B', address: 'Range Street 2', latitude: 52.2297, longitude: 21.0122 },
          capacity: 12,
          cancellationDeadlineDays: 3,
          durationMinutes: 120
        })
      ]),
      error: vi.fn()
    };

    const { fixture } = await createComponent(service);

    await vi.waitFor(() => expect(fixture.nativeElement.querySelectorAll('.term-row')).toHaveLength(2));

    const list = fixture.nativeElement.querySelector('.term-list');
    const rows = Array.from(fixture.nativeElement.querySelectorAll('.term-row')) as HTMLElement[];
    const firstRow = rows[0];
    expect(list).not.toBeNull();
    expect(firstRow.querySelector('.term-main')?.textContent).toContain('First training');
    expect(firstRow.querySelector('.term-main')?.textContent).toContain('First description');
    expect(fieldText(firstRow, 'Place')).toContain('Range A');
    expect(fieldText(firstRow, 'Place')).toContain('Range Street 1');
    expect(fieldText(firstRow, 'Duration')).toContain('90 min');
    expect(fieldText(firstRow, 'Capacity')).toContain('8 places');
    expect(fieldText(firstRow, 'Cancellation')).toContain('Cancel up to 2 days before start');
    expect(firstRow.querySelector('a[href="/booking-terms/first-term-id"]')?.textContent).toContain('Reserve');
  });

  it('sorts public terms by earliest start date', async () => {
    const service = {
      publicTerms: vi.fn().mockResolvedValue([
        sampleTerm({ id: 'later-term-id', name: 'Later training', startsAt: '2026-06-10T12:00:00' }),
        sampleTerm({ id: 'earlier-term-id', name: 'Earlier training', startsAt: '2026-06-01T12:00:00' })
      ]),
      error: vi.fn()
    };

    const { fixture } = await createComponent(service);

    await vi.waitFor(() => expect(fixture.nativeElement.querySelectorAll('.term-row')).toHaveLength(2));

    const rows = Array.from(fixture.nativeElement.querySelectorAll('.term-row')) as HTMLElement[];
    expect(rows[0].textContent).toContain('Earlier training');
    expect(rows[1].textContent).toContain('Later training');
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
  return { component: fixture.componentInstance as any, fixture };
}

function fieldText(row: HTMLElement, label: string): string {
  const facts = Array.from(row.querySelectorAll('.fact')) as HTMLElement[];
  return facts.find((fact) => fact.querySelector('dt')?.textContent?.trim() === label)?.textContent ?? '';
}

function sampleTerm(overrides: Partial<Term> = {}): Term {
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
    updatedAt: '2026-05-08T10:00:00Z',
    ...overrides
  };
}
