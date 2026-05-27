import {Routes} from '@angular/router';

import {authGuard} from './identity/auth.guard';
import {HomeComponent} from './home/home.component';
import {ForgotPasswordComponent} from './identity/forgot-password.component';
import {LoginComponent} from './identity/login.component';
import {RegisterComponent} from './identity/register.component';
import {ResetPasswordComponent} from './identity/reset-password.component';
import {BookingAdminComponent} from './bookings/booking-admin.component';
import {BookingPublicDetailComponent} from './bookings/booking-public-detail.component';
import {BookingPublicListComponent} from './bookings/booking-public-list.component';
import {BookingTokenResultComponent} from './bookings/booking-token-result.component';
import {TrainingDetailComponent} from './training/training-detail.component';
import {TrainingListComponent} from './training/training-list.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {path: 'forgot-password', component: ForgotPasswordComponent},
  {path: 'reset-password/:token', component: ResetPasswordComponent},
  { path: 'booking-terms', component: BookingPublicListComponent },
  { path: 'booking-terms/:id', component: BookingPublicDetailComponent },
  { path: 'booking-confirm/:token', component: BookingTokenResultComponent, data: { action: 'confirm' } },
  { path: 'booking-cancel/:token', component: BookingTokenResultComponent, data: { action: 'cancel' } },
  { path: 'bookings', component: BookingAdminComponent, canActivate: [authGuard] },
  { path: 'trainings', component: TrainingListComponent, canActivate: [authGuard] },
  { path: 'trainings/:id', component: TrainingDetailComponent, canActivate: [authGuard] },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
