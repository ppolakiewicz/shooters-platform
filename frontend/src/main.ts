import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import localePl from '@angular/common/locales/pl';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { xsrfInterceptor } from './app/identity/xsrf.interceptor';

registerLocaleData(localePl);

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withXsrfConfiguration({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN'
    }), withInterceptors([xsrfInterceptor])),
    provideRouter(routes),
    provideAnimationsAsync(),
    provideZonelessChangeDetection()
  ]
}).catch((error: unknown) => console.error(error));
