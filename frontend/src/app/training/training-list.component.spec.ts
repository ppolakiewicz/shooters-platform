import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { TrainingListComponent } from './training-list.component';
import { TrainingService } from './training.service';

describe('TrainingListComponent', () => {
  it('loads summaries into the list', async () => {
    const service = {
      list: vi.fn().mockResolvedValue([summary()]),
      create: vi.fn(),
      delete: vi.fn(),
      error: vi.fn()
    };

    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.items()).toHaveLength(1));
    expect(component.items()[0].name).toBe('Practice');
  });

  it('labels summary cells for compact mobile presentation', async () => {
    const service = {
      list: vi.fn().mockResolvedValue([summary()]),
      create: vi.fn(),
      delete: vi.fn(),
      error: vi.fn()
    };

    const { fixture } = await createComponent(service);

    await vi.waitFor(() => expect(fixture.nativeElement.querySelector('td[data-label="Date"]')).not.toBeNull());
    expect(fixture.nativeElement.querySelector('td[data-label="Name"]')?.textContent).toContain('Practice');
    expect(fixture.nativeElement.querySelector('td[data-label="Actions"]')).not.toBeNull();
  });

  it('creates a default draft and navigates to details', async () => {
    const service = {
      list: vi.fn().mockResolvedValue([]),
      create: vi.fn().mockResolvedValue({ id: 'new-training-id' }),
      delete: vi.fn(),
      error: vi.fn()
    };
    const { component, router } = await createComponent(service);

    await component.create();

    expect(service.create).toHaveBeenCalledWith(expect.objectContaining({
      name: 'New training',
      weaponType: 'PISTOL',
      scoringType: 'IDPA'
    }));
    expect(router.navigate).toHaveBeenCalledWith(['/trainings', 'new-training-id']);
  });
});

async function createComponent(service: unknown) {
  await TestBed.configureTestingModule({
    imports: [TrainingListComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      { provide: TrainingService, useValue: service }
    ]
  }).compileComponents();

  const router = TestBed.inject(Router);
  vi.spyOn(router, 'navigate').mockResolvedValue(true);
  const fixture = TestBed.createComponent(TrainingListComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as any, fixture, router };
}

function summary() {
  return {
    id: 'training-id',
    name: 'Practice',
    place: 'Range A',
    description: '',
    performedOn: '2026-06-01',
    weaponType: 'PISTOL',
    scoringType: 'IDPA',
    taskCount: 0,
    createdAt: '2026-05-07T12:00:00Z',
    updatedAt: '2026-05-07T12:00:00Z'
  };
}
