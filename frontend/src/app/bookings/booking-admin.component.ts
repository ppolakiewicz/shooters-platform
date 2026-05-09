import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { form, FormField, max, maxLength, min, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { ReservationSummary, Term, TrainingEnrollment } from './booking.models';
import { BookingService } from './booking.service';

@Component({
  selector: 'app-booking-admin',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './booking-admin.component.html',
  styleUrl: './booking-admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookingAdminComponent {
  private readonly bookings = inject(BookingService);

  protected readonly enrollments = signal<TrainingEnrollment[]>([]);
  protected readonly terms = signal<Term[]>([]);
  protected readonly selectedTermId = signal<string | null>(null);
  protected readonly reservations = signal<ReservationSummary[]>([]);
  protected readonly loading = signal(false);
  protected readonly savingEnrollment = signal(false);
  protected readonly savingTerm = signal(false);
  protected readonly loadingReservations = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly selectedTerm = computed(() => this.terms().find((term) => term.id === this.selectedTermId()) ?? null);

  protected readonly enrollmentModel = signal({
    name: 'Basic pistol',
    description: '',
    placeName: 'Range A',
    address: 'Range Street 1',
    latitude: 52.2297,
    longitude: 21.0122,
    capacity: 8,
    cancellationDeadlineDays: 2,
    durationMinutes: 90
  });
  protected readonly enrollmentForm = form(this.enrollmentModel, (path) => {
    required(path.name, { message: 'Name is required' });
    maxLength(path.name, 120, { message: 'Use at most 120 characters' });
    maxLength(path.description, 2048, { message: 'Use at most 2048 characters' });
    required(path.placeName, { message: 'Place is required' });
    required(path.address, { message: 'Address is required' });
    min(path.latitude, -90, { message: 'Latitude must be at least -90' });
    max(path.latitude, 90, { message: 'Latitude must be at most 90' });
    min(path.longitude, -180, { message: 'Longitude must be at least -180' });
    max(path.longitude, 180, { message: 'Longitude must be at most 180' });
    min(path.capacity, 1, { message: 'Capacity must be at least 1' });
    min(path.cancellationDeadlineDays, 0, { message: 'Cancellation days cannot be negative' });
    min(path.durationMinutes, 1, { message: 'Duration must be at least 1 minute' });
  });

  protected readonly termModel = signal({
    enrollmentId: '',
    name: '',
    description: '',
    placeName: '',
    address: '',
    latitude: 52.2297,
    longitude: 21.0122,
    capacity: 1,
    cancellationDeadlineDays: 0,
    durationMinutes: 60,
    startsAt: nextWeekLocalDateTime()
  });
  protected readonly termForm = form(this.termModel, (path) => {
    required(path.enrollmentId, { message: 'Training enrollment is required' });
    required(path.name, { message: 'Name is required' });
    maxLength(path.name, 120, { message: 'Use at most 120 characters' });
    maxLength(path.description, 2048, { message: 'Use at most 2048 characters' });
    required(path.placeName, { message: 'Place is required' });
    required(path.address, { message: 'Address is required' });
    min(path.latitude, -90, { message: 'Latitude must be at least -90' });
    max(path.latitude, 90, { message: 'Latitude must be at most 90' });
    min(path.longitude, -180, { message: 'Longitude must be at least -180' });
    max(path.longitude, 180, { message: 'Longitude must be at most 180' });
    min(path.capacity, 1, { message: 'Capacity must be at least 1' });
    min(path.cancellationDeadlineDays, 0, { message: 'Cancellation days cannot be negative' });
    min(path.durationMinutes, 1, { message: 'Duration must be at least 1 minute' });
    required(path.startsAt, { message: 'Start date and time are required' });
  });

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [enrollments, terms] = await Promise.all([
        this.bookings.enrollments(),
        this.bookings.ownerTerms()
      ]);
      this.enrollments.set(enrollments);
      this.terms.set(terms);
      if (!this.termModel().enrollmentId && enrollments.length) {
        this.applyEnrollment(enrollments[0].id);
      }
    } catch {
      this.error.set(this.bookings.error() ?? 'Could not load booking admin data');
    } finally {
      this.loading.set(false);
    }
  }

  protected createEnrollment(): void {
    submit(this.enrollmentForm, async () => {
      const model = this.enrollmentModel();
      this.savingEnrollment.set(true);
      this.error.set(null);
      try {
        const enrollment = await this.bookings.createEnrollment({
          name: model.name,
          description: model.description,
          location: {
            placeName: model.placeName,
            address: model.address,
            latitude: model.latitude,
            longitude: model.longitude
          },
          capacity: model.capacity,
          cancellationDeadlineDays: model.cancellationDeadlineDays,
          durationMinutes: model.durationMinutes
        });
        this.enrollments.update((items) => [enrollment, ...items]);
        this.applyEnrollment(enrollment.id);
      } catch {
        this.error.set(this.bookings.error() ?? 'Could not create training enrollment');
      } finally {
        this.savingEnrollment.set(false);
      }
    });
  }

  protected createTerm(): void {
    submit(this.termForm, async () => {
      const model = this.termModel();
      this.savingTerm.set(true);
      this.error.set(null);
      try {
        const term = await this.bookings.createTerm({
          name: model.name,
          description: model.description,
          location: {
            placeName: model.placeName,
            address: model.address,
            latitude: model.latitude,
            longitude: model.longitude
          },
          capacity: model.capacity,
          cancellationDeadlineDays: model.cancellationDeadlineDays,
          durationMinutes: model.durationMinutes,
          startsAt: model.startsAt
        });
        this.terms.update((items) => [...items, term].sort((first, second) => first.startsAt.localeCompare(second.startsAt)));
        await this.openReservations(term);
      } catch {
        this.error.set(this.bookings.error() ?? 'Could not create term');
      } finally {
        this.savingTerm.set(false);
      }
    });
  }

  protected async openReservations(term: Term): Promise<void> {
    this.selectedTermId.set(term.id);
    this.loadingReservations.set(true);
    this.error.set(null);
    try {
      this.reservations.set(await this.bookings.reservations(term.id));
    } catch {
      this.error.set(this.bookings.error() ?? 'Could not load reservations');
    } finally {
      this.loadingReservations.set(false);
    }
  }

  protected applyEnrollment(enrollmentId: string): void {
    const enrollment = this.enrollments().find((item) => item.id === enrollmentId);
    if (!enrollment) {
      this.termModel.update((model) => ({ ...model, enrollmentId }));
      return;
    }

    this.termModel.update((model) => ({
      ...model,
      enrollmentId,
      name: enrollment.name,
      description: enrollment.description,
      placeName: enrollment.location.placeName,
      address: enrollment.location.address,
      latitude: enrollment.location.latitude,
      longitude: enrollment.location.longitude,
      capacity: enrollment.capacity,
      cancellationDeadlineDays: enrollment.cancellationDeadlineDays,
      durationMinutes: enrollment.durationMinutes
    }));
  }

  protected async cancelReservation(reservation: ReservationSummary): Promise<void> {
    const term = this.selectedTerm();
    if (!term || !window.confirm(`Cancel reservation for ${reservation.firstName} ${reservation.lastName}?`)) {
      return;
    }

    this.error.set(null);
    try {
      await this.bookings.cancelReservation(term.id, reservation.id);
      this.reservations.set(await this.bookings.reservations(term.id));
    } catch {
      this.error.set(this.bookings.error() ?? 'Could not cancel reservation');
    }
  }

  protected async expireOffers(): Promise<void> {
    const term = this.selectedTerm();
    if (!term) {
      return;
    }

    this.error.set(null);
    try {
      await this.bookings.expireOffers(term.id);
      this.reservations.set(await this.bookings.reservations(term.id));
    } catch {
      this.error.set(this.bookings.error() ?? 'Could not expire waitlist offers');
    }
  }
}

function nextWeekLocalDateTime(): string {
  const date = new Date();
  date.setDate(date.getDate() + 7);
  date.setHours(18, 0, 0, 0);
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}
