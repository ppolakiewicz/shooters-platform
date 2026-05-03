import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../identity/auth.service';
import { BackendHealth } from './backend-health';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly health = signal<BackendHealth | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly signingOut = signal(false);

  constructor() {
    this.refreshHealth();
  }

  protected refreshHealth(): void {
    this.loading.set(true);
    this.error.set(null);

    this.http.get<BackendHealth>('/api/health').subscribe({
      next: (health) => {
        this.health.set(health);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Backend is not reachable');
        this.health.set(null);
        this.loading.set(false);
      }
    });
  }

  protected async logout(): Promise<void> {
    this.signingOut.set(true);
    await this.auth.logout();
    await this.router.navigateByUrl('/login');
  }
}
