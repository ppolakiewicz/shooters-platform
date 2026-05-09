import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';

import { CreateReservation, CreatedReservation, ReservationSummary, Term, TrainingEnrollment, UpsertTerm, UpsertTrainingEnrollment } from './booking.models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly http = inject(HttpClient);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  async publicTerms(): Promise<Term[]> {
    return this.read(() => this.http.get<Term[]>('/api/bookings/public/terms'));
  }

  async publicTerm(id: string): Promise<Term> {
    return this.read(() => this.http.get<Term>(`/api/bookings/public/terms/${id}`));
  }

  async createReservation(termId: string, request: CreateReservation): Promise<CreatedReservation> {
    return this.mutate(() => this.http.post<CreatedReservation>('/api/bookings/reservations/reserve', { termId, ...request }));
  }

  async confirmWaitlist(token: string): Promise<ReservationSummary> {
    return this.httpPostNoCsrf('/api/bookings/reservations/confirm-waitlist-offer', { token });
  }

  async cancelByToken(token: string): Promise<ReservationSummary> {
    return this.httpPostNoCsrf('/api/bookings/reservations/cancel-by-participant', { token });
  }

  async enrollments(): Promise<TrainingEnrollment[]> {
    return this.read(() => this.http.get<TrainingEnrollment[]>('/api/bookings/enrollments'));
  }

  async createEnrollment(request: UpsertTrainingEnrollment): Promise<TrainingEnrollment> {
    return this.mutate(() => this.http.post<TrainingEnrollment>('/api/bookings/enrollments', request));
  }

  async updateEnrollment(id: string, request: UpsertTrainingEnrollment): Promise<TrainingEnrollment> {
    return this.mutate(() => this.http.put<TrainingEnrollment>(`/api/bookings/enrollments/${id}`, request));
  }

  async ownerTerms(): Promise<Term[]> {
    return this.read(() => this.http.get<Term[]>('/api/bookings/terms'));
  }

  async createTerm(request: UpsertTerm): Promise<Term> {
    return this.mutate(() => this.http.post<Term>('/api/bookings/terms', request));
  }

  async updateTerm(id: string, request: UpsertTerm): Promise<Term> {
    return this.mutate(() => this.http.put<Term>(`/api/bookings/terms/${id}`, request));
  }

  async reservations(termId: string): Promise<ReservationSummary[]> {
    return this.mutate(() => this.http.post<ReservationSummary[]>('/api/bookings/reservations/list', { termId }));
  }

  async cancelReservation(termId: string, reservationId: string): Promise<ReservationSummary> {
    return this.mutate(() => this.http.post<ReservationSummary>('/api/bookings/reservations/cancel-by-instructor', { termId, reservationId }));
  }

  async expireOffers(termId: string): Promise<{ expiredCount: number }> {
    return this.mutate(() => this.http.post<{ expiredCount: number }>('/api/bookings/reservations/expire-waitlist-offers', { termId }));
  }

  private async read<T>(request: () => Observable<T>): Promise<T> {
    this.loading.set(true);
    this.error.set(null);
    try {
      return await firstValueFrom(request());
    } catch (error) {
      this.error.set(this.errorMessage(error));
      throw error;
    } finally {
      this.loading.set(false);
    }
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

  private async httpPostNoCsrf(url: string, request: object): Promise<ReservationSummary> {
    this.loading.set(true);
    this.error.set(null);
    try {
      return await firstValueFrom(this.http.post<ReservationSummary>(url, request));
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

    return 'Booking request failed';
  }
}
