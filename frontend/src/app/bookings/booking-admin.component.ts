import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {form, FormField, max, maxLength, min, required, submit} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';

import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TranslationService} from '../shared/i18n/translation.service';
import {ReservationSummary, Term, TrainingEnrollment, TrainingLevel, WaitlistEntrySummary} from './booking.models';
import {BookingService} from './booking.service';

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
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './booking-admin.component.html',
  styleUrl: './booking-admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookingAdminComponent {
  private readonly bookings = inject(BookingService);
  protected readonly i18n = inject(TranslationService);

  protected readonly enrollments = signal<TrainingEnrollment[]>([]);
  protected readonly terms = signal<Term[]>([]);
  protected readonly selectedTermId = signal<string | null>(null);
  protected readonly reservations = signal<ReservationSummary[]>([]);
  protected readonly waitlistEntries = signal<WaitlistEntrySummary[]>([]);
  protected readonly loading = signal(false);
  protected readonly savingEnrollment = signal(false);
  protected readonly savingTerm = signal(false);
  protected readonly loadingReservations = signal(false);
  protected readonly error = signal<string | null>(null);
    protected readonly trainingLevels: readonly TrainingLevel[] = ['BASIC', 'INTERMEDIATE', 'ADVANCED'];

  protected readonly selectedTerm = computed(() => this.terms().find((term) => term.id === this.selectedTermId()) ?? null);

  protected readonly enrollmentModel = signal({
    name: 'Basic pistol',
    description: '',
      trainingLevel: 'BASIC' as TrainingLevel,
    placeName: 'Range A',
    address: 'Range Street 1',
    latitude: 52.2297,
    longitude: 21.0122,
    capacity: 8,
    cancellationDeadlineDays: 2,
    durationMinutes: 90
  });
  protected readonly enrollmentForm = form(this.enrollmentModel, (path) => {
    required(path.name, { message: 'validation.nameRequired' });
    maxLength(path.name, 120, { message: 'validation.useAtMost' });
    maxLength(path.description, 2048, { message: 'validation.useAtMost' });
      required(path.trainingLevel, {message: 'validation.trainingLevelRequired'});
    required(path.placeName, { message: 'validation.placeRequired' });
    required(path.address, { message: 'validation.addressRequired' });
    min(path.latitude, -90, { message: 'validation.latitudeMin' });
    max(path.latitude, 90, { message: 'validation.latitudeMax' });
    min(path.longitude, -180, { message: 'validation.longitudeMin' });
    max(path.longitude, 180, { message: 'validation.longitudeMax' });
    min(path.capacity, 1, { message: 'validation.capacityAtLeastOne' });
    min(path.cancellationDeadlineDays, 0, { message: 'validation.cancellationDaysNonNegative' });
    min(path.durationMinutes, 1, { message: 'validation.durationAtLeastOne' });
  });

  protected readonly termModel = signal({
    enrollmentId: '',
    name: '',
    description: '',
      trainingLevel: 'BASIC' as TrainingLevel,
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
    required(path.enrollmentId, { message: 'validation.trainingEnrollmentRequired' });
    required(path.name, { message: 'validation.nameRequired' });
    maxLength(path.name, 120, { message: 'validation.useAtMost' });
    maxLength(path.description, 2048, { message: 'validation.useAtMost' });
      required(path.trainingLevel, {message: 'validation.trainingLevelRequired'});
    required(path.placeName, { message: 'validation.placeRequired' });
    required(path.address, { message: 'validation.addressRequired' });
    min(path.latitude, -90, { message: 'validation.latitudeMin' });
    max(path.latitude, 90, { message: 'validation.latitudeMax' });
    min(path.longitude, -180, { message: 'validation.longitudeMin' });
    max(path.longitude, 180, { message: 'validation.longitudeMax' });
    min(path.capacity, 1, { message: 'validation.capacityAtLeastOne' });
    min(path.cancellationDeadlineDays, 0, { message: 'validation.cancellationDaysNonNegative' });
    min(path.durationMinutes, 1, { message: 'validation.durationAtLeastOne' });
    required(path.startsAt, { message: 'validation.startRequired' });
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
      this.error.set(this.bookings.error() ?? 'errors.loadBookingAdminFailed');
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
            trainingLevel: model.trainingLevel,
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
        this.error.set(this.bookings.error() ?? 'errors.createTrainingEnrollmentFailed');
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
            trainingLevel: model.trainingLevel,
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
        this.error.set(this.bookings.error() ?? 'errors.createTermFailed');
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
      const [reservations, waitlistEntries] = await Promise.all([
        this.bookings.reservations(term.id),
        this.bookings.waitlistEntries(term.id)
      ]);
      this.reservations.set(reservations);
      this.waitlistEntries.set(waitlistEntries);
    } catch {
      this.error.set(this.bookings.error() ?? 'errors.loadReservationsFailed');
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
        trainingLevel: enrollment.trainingLevel,
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
    if (!term || !window.confirm(this.i18n.translate('bookings.confirmCancelReservation', { name: `${reservation.firstName} ${reservation.lastName}` }))) {
      return;
    }

    this.error.set(null);
    try {
      await this.bookings.cancelReservation(term.id, reservation.id);
      await this.openReservations(term);
    } catch {
      this.error.set(this.bookings.error() ?? 'errors.cancelReservationFailed');
    }
  }

  protected async removeWaitlistEntry(entry: WaitlistEntrySummary): Promise<void> {
    const term = this.selectedTerm();
    if (!term || !window.confirm(this.i18n.translate('bookings.confirmRemoveWaitlistEntry', { name: `${entry.firstName} ${entry.lastName}` }))) {
      return;
    }

    this.error.set(null);
    try {
      await this.bookings.removeWaitlistEntry(term.id, entry.id);
      await this.openReservations(term);
    } catch {
      this.error.set(this.bookings.error() ?? 'errors.removeWaitlistEntryFailed');
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
      await this.openReservations(term);
    } catch {
      this.error.set(this.bookings.error() ?? 'errors.expireOffersFailed');
    }
  }

    protected trainingLevelKey(level: TrainingLevel): string {
        return `bookings.trainingLevel.${level}`;
    }
}

function nextWeekLocalDateTime(): string {
  const date = new Date();
  date.setDate(date.getDate() + 7);
  date.setHours(18, 0, 0, 0);
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}
