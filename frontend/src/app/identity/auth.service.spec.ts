import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting, TestRequest} from '@angular/common/http/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';

import {AuthService, AuthUser} from './auth.service';

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
    const user: AuthUser = { id: 'user-id', email: 'owner@example.com', username: 'OwnerOne', roles: ['USER'] };

    const registration = service.register({
      email: 'owner@example.com',
      username: 'OwnerOne',
      password: 'correct horse battery'
    });

    const request = await nextRequest('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      email: 'owner@example.com',
      username: 'OwnerOne',
      password: 'correct horse battery'
    });
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

    const request = await nextRequest('/api/auth/logout');
    expect(request.request.method).toBe('POST');
    request.flush(null);

    await logout;
    expect(service.currentUser()).toBeNull();
  });

  it('requests password reset with csrf', async () => {
    const requestReset = service.requestPasswordReset({email: 'owner@example.com'});

    const request = await nextRequest('/api/auth/password-reset-requests');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({email: 'owner@example.com'});
    request.flush(null);

    await expect(requestReset).resolves.toBeUndefined();
  });

  it('resets password with csrf', async () => {
    const reset = service.resetPassword({token: 'reset-token', password: 'new correct password'});

    const request = await nextRequest('/api/auth/password-reset');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({token: 'reset-token', password: 'new correct password'});
    request.flush(null);

    await expect(reset).resolves.toBeUndefined();
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
