import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';

import {AuthService} from './auth.service';

export const organizerGuard: CanActivateFn = async () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const knownUser = auth.currentUser();
    const user = knownUser === undefined ? await auth.loadCurrentUser() : knownUser;

    if (!user) {
        return router.createUrlTree(['/login']);
    }
    return user.roles.includes('ORGANIZER') ? true : router.createUrlTree(['/']);
};
