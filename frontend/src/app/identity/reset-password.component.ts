import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {form, FormField, maxLength, minLength, required, submit} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {AuthService} from './auth.service';

@Component({
    selector: 'app-reset-password',
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
    templateUrl: './reset-password.component.html',
    styleUrl: './auth-form.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResetPasswordComponent {
    private readonly auth = inject(AuthService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    private readonly token = this.route.snapshot.paramMap.get('token') ?? '';

    protected readonly model = signal({password: '', confirmPassword: ''});
    protected readonly resetPasswordForm = form(this.model, (path) => {
        required(path.password, {message: 'validation.passwordRequired'});
        minLength(path.password, 12, {message: 'validation.passwordMin'});
        maxLength(path.password, 128, {message: 'validation.passwordMax'});
        required(path.confirmPassword, {message: 'validation.confirmPasswordRequired'});
    });
    protected readonly passwordsMismatch = computed(() => {
        const value = this.model();
        return value.confirmPassword.length > 0 && value.password !== value.confirmPassword;
    });
    protected readonly submitting = signal(false);
    protected readonly error = signal<string | null>(null);

    protected onSubmit(): void {
        if (this.passwordsMismatch()) {
            return;
        }

        submit(this.resetPasswordForm, async () => {
            this.submitting.set(true);
            this.error.set(null);
            try {
                await this.auth.resetPassword({token: this.token, password: this.model().password});
                await this.router.navigate(['/login'], {state: {passwordReset: true}});
            } catch {
                this.error.set(this.auth.error() ?? 'errors.passwordResetFailed');
            } finally {
                this.submitting.set(false);
            }
        });
    }
}
