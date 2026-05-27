import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {describe, expect, it, vi} from 'vitest';

import {AuthService} from './auth.service';
import {ForgotPasswordComponent} from './forgot-password.component';

describe('ForgotPasswordComponent', () => {
    it('keeps the form invalid until a valid email is provided', async () => {
        const {component} = await createComponent();

        expect(component.resetRequestForm().invalid()).toBe(true);

        component.model.set({email: 'owner@example.com'});

        expect(component.resetRequestForm().valid()).toBe(true);
    });

    it('requests a password reset and shows generic success', async () => {
        const auth = {requestPasswordReset: vi.fn().mockResolvedValue(undefined), error: vi.fn()};
        const {component} = await createComponent(auth);

        component.model.set({email: 'owner@example.com'});
        component.onSubmit();
        await vi.waitFor(() => expect(auth.requestPasswordReset).toHaveBeenCalledWith({email: 'owner@example.com'}));

        expect(component.submitted()).toBe(true);
    });
});

async function createComponent(auth = {requestPasswordReset: vi.fn(), error: vi.fn()}) {
    await TestBed.configureTestingModule({
        imports: [ForgotPasswordComponent],
        providers: [
            provideZonelessChangeDetection(),
            provideRouter([]),
            {provide: AuthService, useValue: auth}
        ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ForgotPasswordComponent);
    await fixture.whenStable();
    return {component: fixture.componentInstance as unknown as ForgotPasswordComponentTestAccess};
}

interface ForgotPasswordComponentTestAccess {
    model: {
        set(value: { email: string }): void;
    };
    resetRequestForm: () => FormState;
    submitted: () => boolean;

    onSubmit(): void;
}

interface FormState {
    invalid(): boolean;

    valid(): boolean;
}
