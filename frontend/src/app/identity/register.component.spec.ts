import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideZonelessChangeDetection } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';

import { AuthService } from './auth.service';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  it('requires a valid email username and at least 12 password characters', async () => {
    const { component } = await createComponent();

    component.model.set({ email: 'not-an-email', username: 'bad.name', password: 'short' });
    expect(component.registerForm().invalid()).toBe(true);

    component.model.set({ email: 'owner@example.com', username: 'Owner_One', password: 'correct horse battery' });
    expect(component.registerForm().valid()).toBe(true);
  });

  it('registers and navigates home on submit', async () => {
    const auth = {
      register: vi.fn().mockResolvedValue({
        id: 'user-id',
        email: 'owner@example.com',
        username: 'OwnerOne',
        roles: ['USER']
      }),
      error: vi.fn()
    };
    const { component, router } = await createComponent(auth);

    component.model.set({ email: 'owner@example.com', username: 'OwnerOne', password: 'correct horse battery' });
    component.onSubmit();
    await vi.waitFor(() => expect(auth.register).toHaveBeenCalled());

    expect(auth.register).toHaveBeenCalledWith({
      email: 'owner@example.com',
      username: 'OwnerOne',
      password: 'correct horse battery'
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });
});

async function createComponent(auth = { register: vi.fn(), error: vi.fn() }) {
  await TestBed.configureTestingModule({
    imports: [RegisterComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      { provide: AuthService, useValue: auth }
    ]
  }).compileComponents();

  const router = TestBed.inject(Router);
  vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  const fixture = TestBed.createComponent(RegisterComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as unknown as RegisterComponentTestAccess, router };
}

interface RegisterComponentTestAccess {
  model: {
    set(value: { email: string; username: string; password: string }): void;
  };
  registerForm: () => FormState;
  onSubmit(): void;
}

interface FormState {
  invalid(): boolean;
  valid(): boolean;
}
