import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {email, form, FormField, required, submit} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {AuthService} from './auth.service';

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
    MatProgressSpinnerModule,
    TranslatePipe
  ],
  templateUrl: './login.component.html',
  styleUrl: './auth-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

    protected readonly success = signal(history.state?.passwordReset ? 'auth.resetPasswordSuccess' : null);
  protected readonly model = signal({ email: '', password: '' });
  protected readonly loginForm = form(this.model, (path) => {
    required(path.email, { message: 'validation.emailRequired' });
    email(path.email, { message: 'validation.emailValid' });
    required(path.password, { message: 'validation.passwordRequired' });
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected onSubmit(): void {
    submit(this.loginForm, async () => {
      this.submitting.set(true);
      this.error.set(null);
        this.success.set(null);
      try {
        await this.auth.login(this.model());
        await this.router.navigateByUrl('/');
      } catch {
        this.error.set(this.auth.error() ?? 'errors.invalidCredentials');
      } finally {
        this.submitting.set(false);
      }
    });
  }
}
