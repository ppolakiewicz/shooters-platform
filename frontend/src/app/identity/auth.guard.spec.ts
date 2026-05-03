import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { authGuard } from './auth.guard';
import { AuthService, AuthUser } from './auth.service';

describe('authGuard', () => {
  let currentUser: ReturnType<typeof signal<AuthUser | null | undefined>>;
  let loadCurrentUser: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    currentUser = signal<AuthUser | null | undefined>(undefined);
    loadCurrentUser = vi.fn();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            currentUser,
            loadCurrentUser
          }
        }
      ]
    });
  });

  it('allows an already authenticated user', async () => {
    currentUser.set({ id: 'user-id', email: 'owner@example.com', roles: ['USER'] });

    const result = await TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBe(true);
    expect(loadCurrentUser).not.toHaveBeenCalled();
  });

  it('redirects anonymous users to login', async () => {
    loadCurrentUser.mockResolvedValue(null);

    const result = await TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    const router = TestBed.inject(Router);

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });
});
