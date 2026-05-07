import { Routes } from '@angular/router';

import { authGuard } from './identity/auth.guard';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './identity/login.component';
import { RegisterComponent } from './identity/register.component';
import { TrainingDetailComponent } from './training/training-detail.component';
import { TrainingListComponent } from './training/training-list.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'trainings', component: TrainingListComponent, canActivate: [authGuard] },
  { path: 'trainings/:id', component: TrainingDetailComponent, canActivate: [authGuard] },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
