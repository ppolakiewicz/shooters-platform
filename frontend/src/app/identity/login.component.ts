import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { email, form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
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
  templateUrl: './login.component.html',
  styleUrl: './auth-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly model = signal({ email: '', password: '' });
  protected readonly loginForm = form(this.model, (path) => {
    required(path.email, { message: 'Email is required' });
    email(path.email, { message: 'Use a valid email address' });
    required(path.password, { message: 'Password is required' });
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected onSubmit(): void {
    submit(this.loginForm, async () => {
      this.submitting.set(true);
      this.error.set(null);
      try {
        await this.auth.login(this.model());
        await this.router.navigateByUrl('/');
      } catch {
        this.error.set(this.auth.error() ?? 'Invalid email or password');
      } finally {
        this.submitting.set(false);
      }
    });
  }
}
