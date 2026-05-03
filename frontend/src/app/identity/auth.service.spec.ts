import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { AuthService, AuthUser } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('registers with csrf and stores current user', async () => {
    const user: AuthUser = { id: 'user-id', email: 'owner@example.com', roles: ['USER'] };

    const registration = service.register({ email: 'owner@example.com', password: 'correct horse battery' });

    http.expectOne('/api/auth/csrf').flush('');
    const request = await nextRequest('/api/auth/register');
    expect(request.request.method).toBe('POST');
    request.flush(user);

    await expect(registration).resolves.toEqual(user);
    expect(service.currentUser()).toEqual(user);
  });

  it('loads anonymous state when /me returns unauthorized', async () => {
    const currentUser = service.loadCurrentUser();

    http.expectOne('/api/auth/me').flush(
      { title: 'Authentication required' },
      { status: 401, statusText: 'Unauthorized' }
    );

    await expect(currentUser).resolves.toBeNull();
    expect(service.currentUser()).toBeNull();
  });

  it('logs out with csrf and clears current user', async () => {
    const logout = service.logout();

    http.expectOne('/api/auth/csrf').flush('');
    const request = await nextRequest('/api/auth/logout');
    expect(request.request.method).toBe('POST');
    request.flush(null);

    await logout;
    expect(service.currentUser()).toBeNull();
  });

  async function nextRequest(url: string): Promise<TestRequest> {
    let requests: TestRequest[] = [];
    await vi.waitFor(() => {
      requests = http.match(url);
      expect(requests).toHaveLength(1);
    });
    return requests[0];
  }
});
