import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { TrainingDetailComponent } from './training-detail.component';
import { Training } from './training.models';
import { TrainingService } from './training.service';

describe('TrainingDetailComponent', () => {
  it('loads details and defaults new task settings from training', async () => {
    const service = serviceMock(sampleTraining());

    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.training()?.id).toBe('training-id'));
    expect(component.trainingModel().description).toBe('Prepare classifier');
    expect(component.taskModel().weaponType).toBe('RIFLE');
    expect(component.taskModel().scoringType).toBe('TARGET');
    expect(component.taskModel().score0).toBe(1);
  });

  it('saves a target task with score zero and converted duration', async () => {
    const service = serviceMock(sampleTraining());
    const { component } = await createComponent(service);

    await vi.waitFor(() => expect(component.training()).not.toBeNull());
    component.taskModel.set({
      ...component.taskModel(),
      scoringType: 'TARGET',
      weaponType: 'RIFLE',
      duration: '1:02.3',
      alpha: 0,
      score0: 1,
      score10: 2
    });

    component.saveTask();
    await vi.waitFor(() => expect(service.addTask).toHaveBeenCalled());

    expect(service.addTask).toHaveBeenCalledWith('training-id', expect.objectContaining({
      weaponType: 'RIFLE',
      scoringType: 'TARGET',
      durationTenths: 623,
      score: expect.objectContaining({ '0': 1, '10': 2 })
    }));
  });
});

async function createComponent(service: unknown) {
  await TestBed.configureTestingModule({
    imports: [TrainingDetailComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { paramMap: convertToParamMap({ id: 'training-id' }) } }
      },
      { provide: TrainingService, useValue: service }
    ]
  }).compileComponents();

  const fixture = TestBed.createComponent(TrainingDetailComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as any };
}

function serviceMock(training: Training) {
  return {
    get: vi.fn().mockResolvedValue(training),
    update: vi.fn().mockResolvedValue(training),
    delete: vi.fn().mockResolvedValue(undefined),
    addTask: vi.fn().mockResolvedValue(training),
    updateTask: vi.fn().mockResolvedValue(training),
    deleteTask: vi.fn().mockResolvedValue(training),
    error: vi.fn()
  };
}

function sampleTraining(): Training {
  return {
    id: 'training-id',
    name: 'Practice',
    place: 'Range A',
    description: 'Prepare classifier',
    performedOn: '2026-06-01',
    weaponType: 'RIFLE',
    scoringType: 'TARGET',
    tasks: [],
    createdAt: '2026-05-07T12:00:00Z',
    updatedAt: '2026-05-07T12:00:00Z'
  };
}
