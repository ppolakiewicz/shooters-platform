import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { Term } from './booking.models';
import { BookingService } from './booking.service';

@Component({
  selector: 'app-booking-public-list',
  standalone: true,
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './booking-public-list.component.html',
  styleUrl: './booking-public-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookingPublicListComponent {
  private readonly bookings = inject(BookingService);

  protected readonly terms = signal<Term[]>([]);
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
      this.error.set(this.bookings.error() ?? 'Could not load terms');
    } finally {
      this.loading.set(false);
    }
  }
}
