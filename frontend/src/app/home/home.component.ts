import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

import {AuthService} from '../identity/auth.service';
import {TranslatePipe} from '../shared/i18n/translate.pipe';

@Component({
  selector: 'app-home',
  standalone: true,
    imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent {
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly signingOut = signal(false);

  protected async logout(): Promise<void> {
    this.signingOut.set(true);
    await this.auth.logout();
    await this.router.navigateByUrl('/login');
  }
}
