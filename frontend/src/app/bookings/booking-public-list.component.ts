import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TranslationService} from '../shared/i18n/translation.service';
import {Term, TrainingLevel} from './booking.models';
import {BookingService} from './booking.service';

@Component({
  selector: 'app-booking-public-list',
  standalone: true,
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslatePipe],
  templateUrl: './booking-public-list.component.html',
  styleUrl: './booking-public-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookingPublicListComponent {
  private static readonly lowAvailabilityThreshold = 2;

  private readonly bookings = inject(BookingService);
  protected readonly i18n = inject(TranslationService);

  protected readonly terms = signal<Term[]>([]);
  protected readonly sortedTerms = computed(() =>
    [...this.terms()].sort((left, right) => Date.parse(left.startsAt) - Date.parse(right.startsAt))
  );
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.terms.set(await this.bookings.publicTerms());
    } catch {
      this.error.set(this.bookings.error() ?? 'errors.loadTermsFailed');
    } finally {
      this.loading.set(false);
    }
  }

  protected hasDescription(term: Term): boolean {
    return term.description.trim().length > 0;
  }

  protected isWaitlistTerm(term: Term): boolean {
    return term.availablePlaces <= 0;
  }

  protected availabilityStatusKey(term: Term): string {
    if (this.isWaitlistTerm(term)) {
      return 'bookings.public.noPlacesStatus';
    }
    if (term.availablePlaces <= BookingPublicListComponent.lowAvailabilityThreshold) {
      return 'bookings.public.lastPlacesStatus';
    }
    return 'bookings.public.availablePlacesStatus';
  }

  protected reserveActionKey(term: Term): string {
    return this.isWaitlistTerm(term) ? 'bookings.public.joinWaitlist' : 'common.reserve';
  }

  protected trainingLevelKey(level: TrainingLevel): string {
    return `bookings.trainingLevel.${level}`;
  }
}
