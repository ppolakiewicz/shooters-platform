import { HttpInterceptorFn } from '@angular/common/http';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

export const xsrfInterceptor: HttpInterceptorFn = (request, next) => {
  if (SAFE_METHODS.has(request.method) || !request.url.startsWith('/api/')) {
    return next(request);
  }

  const token = readCookie('XSRF-TOKEN');
  if (!token || request.headers.has('X-XSRF-TOKEN')) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: {
      'X-XSRF-TOKEN': token
    }
  }));
};

function readCookie(name: string): string | null {
  const prefix = `${name}=`;
  const cookie = document.cookie
    .split(';')
    .map((part) => part.trim())
    .find((part) => part.startsWith(prefix));

  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : null;
}
