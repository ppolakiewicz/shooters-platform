import { Routes } from '@angular/router';

import { authGuard } from './identity/auth.guard';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './identity/login.component';
import { RegisterComponent } from './identity/register.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
