import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ReservationSummary } from './booking.models';
import { BookingService } from './booking.service';

@Component({
  selector: 'app-booking-token-result',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './booking-token-result.component.html',
  styleUrl: './booking-token-result.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookingTokenResultComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly bookings = inject(BookingService);
  private readonly token = this.route.snapshot.paramMap.get('token') ?? '';
  private readonly action = this.route.snapshot.data['action'] as 'confirm' | 'cancel';

  protected readonly loading = signal(false);
  protected readonly reservation = signal<ReservationSummary | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly title = this.action === 'confirm' ? 'Waitlist place confirmed' : 'Reservation cancelled';

  constructor() {
    void this.submit();
  }

  private async submit(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.reservation.set(this.action === 'confirm'
        ? await this.bookings.confirmWaitlist(this.token)
        : await this.bookings.cancelByToken(this.token));
    } catch {
      this.error.set(this.bookings.error() ?? 'Could not complete booking action');
    } finally {
      this.loading.set(false);
    }
  }
}
