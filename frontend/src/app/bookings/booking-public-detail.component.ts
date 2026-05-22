import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { email, form, FormField, maxLength, minLength, pattern, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TranslatePipe } from '../shared/i18n/translate.pipe';
import { TranslationService } from '../shared/i18n/translation.service';
import { CreatedBooking, Term } from './booking.models';
import { BookingService } from './booking.service';

@Component({
  selector: 'app-booking-public-detail',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    FormField,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    TranslatePipe
  ],
  templateUrl: './booking-public-detail.component.html',
  styleUrl: './booking-public-detail.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookingPublicDetailComponent {
  private readonly bookings = inject(BookingService);
  private readonly route = inject(ActivatedRoute);
  private readonly sanitizer = inject(DomSanitizer);
  protected readonly i18n = inject(TranslationService);
  private readonly termId = this.route.snapshot.paramMap.get('id') ?? '';

  protected readonly term = signal<Term | null>(null);
  protected readonly booking = signal<CreatedBooking | null>(null);
  protected readonly loading = signal(false);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly mapUrl = computed<SafeResourceUrl | null>(() => {
    const location = this.term()?.location;
    if (!location) {
      return null;
    }
    const lat = location.latitude;
    const lon = location.longitude;
    const bbox = `${lon - 0.01},${lat - 0.01},${lon + 0.01},${lat + 0.01}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(
      `https://www.openstreetmap.org/export/embed.html?bbox=${bbox}&layer=mapnik&marker=${lat},${lon}`
    );
  });

  protected readonly model = signal({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    createAccount: false,
    username: '',
    password: ''
  });
  protected readonly reservationForm = form(this.model, (path) => {
    required(path.firstName, { message: 'validation.firstNameRequired' });
    maxLength(path.firstName, 80, { message: 'validation.useAtMost' });
    required(path.lastName, { message: 'validation.lastNameRequired' });
    maxLength(path.lastName, 80, { message: 'validation.useAtMost' });
    required(path.email, { message: 'validation.emailRequired' });
    email(path.email, { message: 'validation.emailValid' });
    required(path.phoneNumber, { message: 'validation.phoneRequired' });
    maxLength(path.phoneNumber, 40, { message: 'validation.useAtMost' });
    minLength(path.username, 3, { message: 'validation.usernameMin' });
    maxLength(path.username, 32, { message: 'validation.usernameMax' });
    pattern(path.username, /^[A-Za-z0-9_-]*$/, { message: 'validation.usernamePattern' });
    minLength(path.password, 12, { message: 'validation.passwordMin' });
    maxLength(path.password, 128, { message: 'validation.passwordMax' });
  });

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.term.set(await this.bookings.publicTerm(this.termId));
    } catch {
      this.error.set(this.bookings.error() ?? 'errors.loadTermFailed');
    } finally {
      this.loading.set(false);
    }
  }

  protected reserve(): void {
    submit(this.reservationForm, async () => {
      const value = this.model();
      if (value.createAccount && (!value.username || !value.password)) {
        this.error.set('errors.usernamePasswordRequired');
        return;
      }

      this.submitting.set(true);
      this.error.set(null);
      try {
        this.booking.set(await this.bookings.createReservation(this.termId, {
          firstName: value.firstName,
          lastName: value.lastName,
          email: value.email,
          phoneNumber: value.phoneNumber,
          createAccount: value.createAccount,
          username: value.createAccount ? value.username : null,
          password: value.createAccount ? value.password : null
        }));
      } catch {
        this.error.set(this.bookings.error() ?? 'errors.createReservationFailed');
      } finally {
        this.submitting.set(false);
      }
    });
  }
}
