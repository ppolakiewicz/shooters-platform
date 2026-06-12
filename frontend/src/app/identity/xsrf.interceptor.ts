import {HttpBackend, HttpClient, HttpInterceptorFn, HttpRequest} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {defer, firstValueFrom, from, switchMap} from 'rxjs';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);
const CSRF_URL = '/api/auth/csrf';
const XSRF_COOKIE = 'XSRF-TOKEN';
const XSRF_HEADER = 'X-XSRF-TOKEN';

export const xsrfInterceptor: HttpInterceptorFn = (request, next) => {
    if (SAFE_METHODS.has(request.method) || !request.url.startsWith('/api/') || request.url === CSRF_URL) {
    return next(request);
  }

    const token = readCookie(XSRF_COOKIE);
    if (token || request.headers.has(XSRF_HEADER)) {
        return next(withXsrfToken(request, token));
  }

    const csrf = inject(CsrfTokenService);
    return defer(() => from(csrf.ensureToken())).pipe(
        switchMap((loadedToken) => next(withXsrfToken(request, loadedToken)))
    );
};

function readCookie(name: string): string | null {
  const prefix = `${name}=`;
  const cookie = document.cookie
    .split(';')
    .map((part) => part.trim())
    .find((part) => part.startsWith(prefix));

  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : null;
}

function withXsrfToken(request: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
    if (!token || request.headers.has(XSRF_HEADER)) {
        return request;
    }
    return request.clone({setHeaders: {[XSRF_HEADER]: token}});
}

@Injectable({providedIn: 'root'})
class CsrfTokenService {
    private readonly http = new HttpClient(inject(HttpBackend));
    private pendingRequest: Promise<string | null> | null = null;

    ensureToken(): Promise<string | null> {
        const existingToken = readCookie(XSRF_COOKIE);
        if (existingToken) {
            return Promise.resolve(existingToken);
        }

        this.pendingRequest ??= this.loadToken().finally(() => {
            this.pendingRequest = null;
        });
        return this.pendingRequest;
    }

    private async loadToken(): Promise<string | null> {
        await firstValueFrom(this.http.get(CSRF_URL, {responseType: 'text'}));
        for (let attempt = 0; attempt < 5; attempt++) {
            const token = readCookie(XSRF_COOKIE);
            if (token) {
                return token;
            }
            await new Promise((resolve) => setTimeout(resolve, 0));
        }
        return null;
    }
}
