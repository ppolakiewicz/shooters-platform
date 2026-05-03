import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideZonelessChangeDetection } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';

import { AuthService } from './auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  it('keeps the form invalid until email and password are provided', async () => {
    const { component } = await createComponent();

    expect(component.loginForm().invalid()).toBe(true);

    component.model.set({ email: 'owner@example.com', password: 'correct horse battery' });

    expect(component.loginForm().valid()).toBe(true);
  });

  it('logs in and navigates home on submit', async () => {
    const auth = { login: vi.fn().mockResolvedValue({ id: 'user-id', email: 'owner@example.com', roles: ['USER'] }), error: vi.fn() };
    const { component, router } = await createComponent(auth);

    component.model.set({ email: 'owner@example.com', password: 'correct horse battery' });
    component.onSubmit();
    await vi.waitFor(() => expect(auth.login).toHaveBeenCalled());

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });
});

async function createComponent(auth = { login: vi.fn(), error: vi.fn() }) {
  await TestBed.configureTestingModule({
    imports: [LoginComponent],
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      { provide: AuthService, useValue: auth }
    ]
  }).compileComponents();

  const router = TestBed.inject(Router);
  vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  const fixture = TestBed.createComponent(LoginComponent);
  await fixture.whenStable();
  return { component: fixture.componentInstance as any, router };
}
