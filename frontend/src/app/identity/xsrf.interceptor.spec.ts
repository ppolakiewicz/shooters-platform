import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting, TestRequest} from '@angular/common/http/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {firstValueFrom} from 'rxjs';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';

import {xsrfInterceptor} from './xsrf.interceptor';

describe('xsrfInterceptor', () => {
    let httpClient: HttpClient;
    let http: HttpTestingController;

    beforeEach(() => {
        clearXsrfCookie();
        TestBed.configureTestingModule({
            providers: [
                provideZonelessChangeDetection(),
                provideHttpClient(withInterceptors([xsrfInterceptor])),
                provideHttpClientTesting()
            ]
        });
        httpClient = TestBed.inject(HttpClient);
        http = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        try {
            http.verify();
        } finally {
            clearXsrfCookie();
            TestBed.resetTestingModule();
        }
    });

    it('loads a token before an unsafe API request and adds its header', async () => {
        const result = firstValueFrom(httpClient.post('/api/example', {value: 1}));

        const csrfRequest = http.expectOne('/api/auth/csrf');
        document.cookie = 'XSRF-TOKEN=token-value; path=/';
        csrfRequest.flush('');

        const mutation = await nextRequest('/api/example');
        expect(mutation.request.headers.get('X-XSRF-TOKEN')).toBe('token-value');
        mutation.flush({ok: true});
        await expect(result).resolves.toEqual({ok: true});
    });

    it('reuses one token request for concurrent mutations', async () => {
        const first = firstValueFrom(httpClient.post('/api/first', {}));
        const second = firstValueFrom(httpClient.delete('/api/second'));

        const csrfRequest = http.expectOne('/api/auth/csrf');
        expect(http.match('/api/auth/csrf')).toHaveLength(0);
        document.cookie = 'XSRF-TOKEN=shared-token; path=/';
        csrfRequest.flush('');

        let mutations: TestRequest[] = [];
        await vi.waitFor(() => {
            mutations = http.match((request) => request.url === '/api/first' || request.url === '/api/second');
            expect(mutations).toHaveLength(2);
        });
        for (const mutation of mutations) {
            expect(mutation.request.headers.get('X-XSRF-TOKEN')).toBe('shared-token');
            mutation.flush({});
        }
        await Promise.all([first, second]);
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

function clearXsrfCookie(): void {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
}
