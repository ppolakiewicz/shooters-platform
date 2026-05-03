import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const knownUser = auth.currentUser();
  const user = knownUser === undefined ? await auth.loadCurrentUser() : knownUser;

  return user ? true : router.createUrlTree(['/login']);
};
