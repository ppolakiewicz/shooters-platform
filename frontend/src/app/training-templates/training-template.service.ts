import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {firstValueFrom} from 'rxjs';

import {TrainingTemplate, TrainingTemplateRequest} from './training-template.models';

@Injectable({providedIn: 'root'})
export class TrainingTemplateService {
    private readonly http = inject(HttpClient);
    private readonly baseUrl = '/api/bookings/training-templates';

    list(): Promise<TrainingTemplate[]> {
        return firstValueFrom(this.http.get<TrainingTemplate[]>(this.baseUrl));
    }

    get(id: string): Promise<TrainingTemplate> {
        return firstValueFrom(this.http.get<TrainingTemplate>(`${this.baseUrl}/${id}`));
    }

    create(request: TrainingTemplateRequest): Promise<TrainingTemplate> {
        return firstValueFrom(this.http.post<TrainingTemplate>(this.baseUrl, request));
    }

    update(id: string, request: TrainingTemplateRequest): Promise<TrainingTemplate> {
        return firstValueFrom(this.http.put<TrainingTemplate>(`${this.baseUrl}/${id}`, request));
    }

    async delete(id: string): Promise<void> {
        await firstValueFrom(this.http.delete<void>(`${this.baseUrl}/${id}`));
    }
}
