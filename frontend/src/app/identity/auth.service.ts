import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface AuthUser {
  id: string;
  email: string;
  roles: string[];
}

interface Credentials {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly user = signal<AuthUser | null | undefined>(undefined);

  readonly currentUser = computed(() => this.user());
  readonly authenticated = computed(() => this.user() !== null && this.user() !== undefined);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  async loadCurrentUser(): Promise<AuthUser | null> {
    this.loading.set(true);
    try {
      const user = await firstValueFrom(this.http.get<AuthUser>('/api/auth/me'));
      this.user.set(user);
      this.error.set(null);
      return user;
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        this.user.set(null);
        return null;
      }
      this.error.set(this.errorMessage(error));
      this.user.set(null);
      return null;
    } finally {
      this.loading.set(false);
    }
  }

  async register(credentials: Credentials): Promise<AuthUser> {
    return this.submitWithCsrf('/api/auth/register', credentials);
  }

  async login(credentials: Credentials): Promise<AuthUser> {
    return this.submitWithCsrf('/api/auth/login', credentials);
  }

  async logout(): Promise<void> {
    await this.ensureCsrf();
    await firstValueFrom(this.http.post<void>('/api/auth/logout', {}));
    this.user.set(null);
    this.error.set(null);
  }

  private async submitWithCsrf(url: string, credentials: Credentials): Promise<AuthUser> {
    this.loading.set(true);
    this.error.set(null);
    try {
      await this.ensureCsrf();
      const user = await firstValueFrom(this.http.post<AuthUser>(url, credentials));
      this.user.set(user);
      return user;
    } catch (error) {
      this.error.set(this.errorMessage(error));
      throw error;
    } finally {
      this.loading.set(false);
    }
  }

  private async ensureCsrf(): Promise<void> {
    await firstValueFrom(this.http.get('/api/auth/csrf', { responseType: 'text' }));
    await this.waitForXsrfCookie();
  }

  private async waitForXsrfCookie(): Promise<void> {
    for (let attempt = 0; attempt < 5; attempt++) {
      if (document.cookie.split(';').some((cookie) => cookie.trim().startsWith('XSRF-TOKEN='))) {
        return;
      }
      await new Promise((resolve) => setTimeout(resolve, 0));
    }
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { detail?: string; title?: string } | string | null;
      if (typeof body === 'object' && body?.detail) {
        return body.detail;
      }
      if (typeof body === 'object' && body?.title) {
        return body.title;
      }
    }

    return 'Authentication request failed';
  }
}
