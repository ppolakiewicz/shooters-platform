import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { TrainingService } from './training.service';

describe('TrainingService', () => {
  let service: TrainingService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(TrainingService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('loads training summaries', async () => {
    const response = [{
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
    }];

    const list = service.list();

    const request = http.expectOne('/api/trainings');
    expect(request.request.method).toBe('GET');
    request.flush(response);

    await expect(list).resolves.toEqual(response);
  });

  it('creates training with csrf', async () => {
    const create = service.create({
      name: 'Planned drills',
      place: 'Range A',
      description: 'Prepare classifier',
      performedOn: '2026-06-01',
      weaponType: 'PISTOL',
      scoringType: 'IDPA'
    });

    http.expectOne('/api/auth/csrf').flush('');
    const request = await nextRequest('/api/trainings');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.description).toBe('Prepare classifier');
    request.flush({
      id: 'training-id',
      name: 'Planned drills',
      place: 'Range A',
      description: 'Prepare classifier',
      performedOn: '2026-06-01',
      weaponType: 'PISTOL',
      scoringType: 'IDPA',
      tasks: [],
      createdAt: '2026-05-07T12:00:00Z',
      updatedAt: '2026-05-07T12:00:00Z'
    });

    await expect(create).resolves.toMatchObject({ id: 'training-id' });
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
