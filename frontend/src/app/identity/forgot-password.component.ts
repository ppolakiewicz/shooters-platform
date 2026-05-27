import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
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
    selector: 'app-forgot-password',
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
    templateUrl: './forgot-password.component.html',
    styleUrl: './auth-form.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForgotPasswordComponent {
    private readonly auth = inject(AuthService);

    protected readonly model = signal({email: ''});
    protected readonly resetRequestForm = form(this.model, (path) => {
        required(path.email, {message: 'validation.emailRequired'});
        email(path.email, {message: 'validation.emailValid'});
    });
    protected readonly submitting = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly submitted = signal(false);

    protected onSubmit(): void {
        submit(this.resetRequestForm, async () => {
            this.submitting.set(true);
            this.error.set(null);
            this.submitted.set(false);
            try {
                await this.auth.requestPasswordReset(this.model());
                this.submitted.set(true);
            } catch {
                this.error.set(this.auth.error() ?? 'errors.passwordResetRequestFailed');
            } finally {
                this.submitting.set(false);
            }
        });
    }
}
