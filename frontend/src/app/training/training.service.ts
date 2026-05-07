import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';

import { Training, TrainingSummary, UpsertTask, UpsertTraining } from './training.models';

@Injectable({ providedIn: 'root' })
export class TrainingService {
  private readonly http = inject(HttpClient);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  async list(): Promise<TrainingSummary[]> {
    this.loading.set(true);
    this.error.set(null);
    try {
      return await firstValueFrom(this.http.get<TrainingSummary[]>('/api/trainings'));
    } catch (error) {
      this.error.set(this.errorMessage(error));
      throw error;
    } finally {
      this.loading.set(false);
    }
  }

  async get(id: string): Promise<Training> {
    this.loading.set(true);
    this.error.set(null);
    try {
      return await firstValueFrom(this.http.get<Training>(`/api/trainings/${id}`));
    } catch (error) {
      this.error.set(this.errorMessage(error));
      throw error;
    } finally {
      this.loading.set(false);
    }
  }

  async create(request: UpsertTraining): Promise<Training> {
    return this.mutate(() => this.http.post<Training>('/api/trainings', request));
  }

  async update(id: string, request: UpsertTraining): Promise<Training> {
    return this.mutate(() => this.http.put<Training>(`/api/trainings/${id}`, request));
  }

  async delete(id: string): Promise<void> {
    await this.mutate(() => this.http.delete<void>(`/api/trainings/${id}`));
  }

  async addTask(trainingId: string, request: UpsertTask): Promise<Training> {
    return this.mutate(() => this.http.post<Training>(`/api/trainings/${trainingId}/tasks`, request));
  }

  async updateTask(trainingId: string, taskId: string, request: UpsertTask): Promise<Training> {
    return this.mutate(() => this.http.put<Training>(`/api/trainings/${trainingId}/tasks/${taskId}`, request));
  }

  async deleteTask(trainingId: string, taskId: string): Promise<Training> {
    return this.mutate(() => this.http.delete<Training>(`/api/trainings/${trainingId}/tasks/${taskId}`));
  }

  private async mutate<T>(request: () => Observable<T>): Promise<T> {
    this.loading.set(true);
    this.error.set(null);
    try {
      await firstValueFrom(this.http.get('/api/auth/csrf', { responseType: 'text' }));
      return await firstValueFrom(request());
    } catch (error) {
      this.error.set(this.errorMessage(error));
      throw error;
    } finally {
      this.loading.set(false);
    }
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { detail?: string; title?: string } | string | null;
      if (typeof body === 'object' && body?.detail) {
        return body.detail;
      }
      if (typeof body === 'object' && body?.title) {
        return body.title;
      }
    }

    return 'Training request failed';
  }
}
