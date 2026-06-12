import {TestBed} from '@angular/core/testing';
import {provideRouter, UrlTree} from '@angular/router';
import {describe, expect, it, vi} from 'vitest';

import {AuthService} from './auth.service';
import {organizerGuard} from './organizer.guard';

describe('organizerGuard', () => {
    it('allows organizers', async () => {
        const result = await runGuard({roles: ['USER', 'ORGANIZER']});
        expect(result).toBe(true);
    });

    it('redirects regular users home', async () => {
        const result = await runGuard({roles: ['USER']});
        expect(result).toBeInstanceOf(UrlTree);
        expect((result as UrlTree).toString()).toBe('/');
    });
});

async function runGuard(user: { roles: string[] }) {
    const auth = {
        currentUser: vi.fn().mockReturnValue({id: 'id', email: 'user@example.com', username: 'User', ...user}),
        loadCurrentUser: vi.fn()
    };
    TestBed.configureTestingModule({
        providers: [
            provideRouter([]),
            {provide: AuthService, useValue: auth},
        ]
    });
    return TestBed.runInInjectionContext(() => organizerGuard({} as never, {} as never));
}
