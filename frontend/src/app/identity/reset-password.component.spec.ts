import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap, provideRouter, Router} from '@angular/router';
import {describe, expect, it, vi} from 'vitest';

import {AuthService} from './auth.service';
import {ResetPasswordComponent} from './reset-password.component';

describe('ResetPasswordComponent', () => {
    it('keeps the form invalid until matching passwords are provided', async () => {
        const {component} = await createComponent();

        expect(component.resetPasswordForm().invalid()).toBe(true);

        component.model.set({password: 'new correct password', confirmPassword: 'different password'});
        expect(component.passwordsMismatch()).toBe(true);

        component.model.set({password: 'new correct password', confirmPassword: 'new correct password'});
        expect(component.resetPasswordForm().valid()).toBe(true);
        expect(component.passwordsMismatch()).toBe(false);
    });

    it('resets password and navigates to login with success state', async () => {
        const auth = {resetPassword: vi.fn().mockResolvedValue(undefined), error: vi.fn()};
        const {component, router} = await createComponent(auth);

        component.model.set({password: 'new correct password', confirmPassword: 'new correct password'});
        component.onSubmit();
        await vi.waitFor(() => expect(auth.resetPassword).toHaveBeenCalledWith({
            token: 'route-token',
            password: 'new correct password'
        }));

        expect(router.navigate).toHaveBeenCalledWith(['/login'], {state: {passwordReset: true}});
    });
});

async function createComponent(auth = {resetPassword: vi.fn(), error: vi.fn()}) {
    await TestBed.configureTestingModule({
        imports: [ResetPasswordComponent],
        providers: [
            provideZonelessChangeDetection(),
            provideRouter([]),
            {provide: AuthService, useValue: auth},
            {
                provide: ActivatedRoute,
                useValue: {
                    snapshot: {
                        paramMap: convertToParamMap({token: 'route-token'})
                    }
                }
            }
        ]
    }).compileComponents();

    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ResetPasswordComponent);
    await fixture.whenStable();
    return {component: fixture.componentInstance as unknown as ResetPasswordComponentTestAccess, router};
}

interface ResetPasswordComponentTestAccess {
    model: {
        set(value: { password: string; confirmPassword: string }): void;
    };
    resetPasswordForm: () => FormState;
    passwordsMismatch: () => boolean;

    onSubmit(): void;
}

interface FormState {
    invalid(): boolean;

    valid(): boolean;
}
