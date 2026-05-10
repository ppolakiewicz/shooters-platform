import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { email, form, FormField, maxLength, minLength, pattern, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TranslatePipe } from '../shared/i18n/translate.pipe';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    RouterLink,
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    TranslatePipe
  ],
  templateUrl: './register.component.html',
  styleUrl: './auth-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly model = signal({ email: '', username: '', password: '' });
  protected readonly registerForm = form(this.model, (path) => {
    required(path.email, { message: 'validation.emailRequired' });
    email(path.email, { message: 'validation.emailValid' });
    required(path.username, { message: 'validation.usernameRequired' });
    minLength(path.username, 3, { message: 'validation.usernameMin' });
    maxLength(path.username, 32, { message: 'validation.usernameMax' });
    pattern(path.username, /^[A-Za-z0-9_-]+$/, { message: 'validation.usernamePattern' });
    required(path.password, { message: 'validation.passwordRequired' });
    minLength(path.password, 12, { message: 'validation.passwordMin' });
    maxLength(path.password, 128, { message: 'validation.passwordMax' });
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected onSubmit(): void {
    submit(this.registerForm, async () => {
      this.submitting.set(true);
      this.error.set(null);
      try {
        await this.auth.register(this.model());
        await this.router.navigateByUrl('/');
      } catch {
        this.error.set(this.auth.error() ?? 'errors.registrationFailed');
      } finally {
        this.submitting.set(false);
      }
    });
  }
}
