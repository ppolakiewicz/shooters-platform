import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

interface BackendHealth {
  backend: string;
  database: string;
  databaseProbe: number;
  timestamp: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private readonly http = inject(HttpClient);

  protected readonly health = signal<BackendHealth | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

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
}
