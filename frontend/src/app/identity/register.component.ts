import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { email, form, FormField, maxLength, minLength, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './auth-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly model = signal({ email: '', password: '' });
  protected readonly registerForm = form(this.model, (path) => {
    required(path.email, { message: 'Email is required' });
    email(path.email, { message: 'Use a valid email address' });
    required(path.password, { message: 'Password is required' });
    minLength(path.password, 12, { message: 'Use at least 12 characters' });
    maxLength(path.password, 128, { message: 'Use at most 128 characters' });
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
        this.error.set(this.auth.error() ?? 'Registration failed');
      } finally {
        this.submitting.set(false);
      }
    });
  }
}
