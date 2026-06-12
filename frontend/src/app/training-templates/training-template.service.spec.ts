import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {afterEach, beforeEach, describe, expect, it} from 'vitest';

import {TrainingTemplateService} from './training-template.service';

describe('TrainingTemplateService', () => {
    let service: TrainingTemplateService;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [provideZonelessChangeDetection(), provideHttpClient(), provideHttpClientTesting()]
        });
        service = TestBed.inject(TrainingTemplateService);
        http = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        http.verify();
        TestBed.resetTestingModule();
    });

    it('loads templates from the dedicated API', async () => {
        const result = service.list();
        http.expectOne('/api/bookings/training-templates').flush([]);
        await expect(result).resolves.toEqual([]);
    });

    it('creates a template through the dedicated API', async () => {
        const requestBody = {
            name: 'Template',
            description: '',
            trainingLevel: 'BASIC' as const,
            location: {placeName: 'Range', address: 'Street 1', latitude: 52, longitude: 21},
            capacity: 8,
            cancellationDeadlineDays: 2,
            durationMinutes: 60,
            defaultStartTime: '09:00'
        };
        const result = service.create(requestBody);
        const request = http.expectOne('/api/bookings/training-templates');
        expect(request.request.method).toBe('POST');
        expect(request.request.body).toEqual(requestBody);
        request.flush({...requestBody, id: 'id', createdAt: '', updatedAt: ''});
        await expect(result).resolves.toMatchObject({id: 'id'});
    });
});
